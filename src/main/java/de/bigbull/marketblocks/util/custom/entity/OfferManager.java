package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.packets.OfferStatusPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;

/**
 * Manages the logic for creating and validating trade offers within a {@link SmallShopBlockEntity}.
 * This class performs server-side validation of the items used to create an offer,
 * extracts them from the shop's temporary slots, and returns any leftovers to the player.
 *
 * @param shopEntity The block entity this manager belongs to.
 */
public record OfferManager(SmallShopBlockEntity shopEntity) {
    private static final int PAYMENT_SLOT_COUNT = 2;
    private static final int RESULT_SLOT_INDEX = 2;
    private static final int TOTAL_OFFER_SLOTS = 3;

    /**
     * A record to hold the result of an offer validation check.
     * @param valid    True if the offer is valid, false otherwise.
     * @param errorKey A translation key for the error message if the offer is invalid.
     */
    public record OfferValidation(boolean valid, String errorKey) {
        public static final OfferValidation VALID = new OfferValidation(true, null);
    }

    /**
     * The main entry point for creating an offer. It validates the proposed offer,
     * extracts the items, creates the offer in the shop, and returns any remaining items to the player.
     *
     * @param player   The player creating the offer.
     * @param payment1 The first payment item from the client.
     * @param payment2 The second payment item from the client.
     * @param result   The result item from the client.
     * @return True if the offer was successfully created, false otherwise.
     */
    public boolean applyOffer(ServerPlayer player, ItemStack payment1, ItemStack payment2, ItemStack result) {
        OfferValidation validation = validateOffer(payment1, payment2, result);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.translatable(validation.errorKey()));
            return false;
        }

        ItemStack[] slotCopies = copyOfferSlotsFromShop();
        ItemStack[] extractedItems = extractItemsFromOfferSlots(slotCopies);

        shopEntity.createOffer(slotCopies[0], slotCopies[1], slotCopies[RESULT_SLOT_INDEX]);

        // Notify clients that the offer has changed
        PacketDistributor.sendToPlayersTrackingChunk(player.serverLevel(), new ChunkPos(shopEntity.getBlockPos()),
                new OfferStatusPacket(shopEntity.getBlockPos(), true));

        shopEntity.sync(); // Sync block entity data

        returnStacksToPlayer(player, extractedItems);

        MarketBlocks.LOGGER.info("Player {} created offer at {}", player.getName().getString(), shopEntity.getBlockPos());

        return true;
    }

    /**
     * Validates the proposed offer against the items currently in the shop's offer slots.
     */
    private OfferValidation validateOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        if (result.isEmpty()) {
            return new OfferValidation(false, "gui.marketblocks.error.no_result_item");
        }
        if (payment1.isEmpty() && payment2.isEmpty()) {
            return new OfferValidation(false, "gui.marketblocks.error.no_payment_items");
        }

        ItemStack[] itemsInSlots = copyOfferSlotsFromShop();
        ItemStack[] expectedItems = new ItemStack[]{payment1, payment2, result};
        if (!areOfferSlotsConsistent(expectedItems, itemsInSlots)) {
            return new OfferValidation(false, "gui.marketblocks.error.invalid_offer");
        }

        return OfferValidation.VALID;
    }

    /**
     * Copies the items from the shop's payment and result slots into a new array.
     */
    private ItemStack[] copyOfferSlotsFromShop() {
        ItemStack[] slots = new ItemStack[TOTAL_OFFER_SLOTS];
        copyRange(shopEntity.getPaymentHandler(), slots, 0, 0, PAYMENT_SLOT_COUNT);
        copyRange(shopEntity.getOfferHandler(), slots, RESULT_SLOT_INDEX, 0, 1);
        return slots;
    }

    /**
     * A generic helper to iterate over a range of slots.
     */
    private void forRange(int start, int length, IntFunction<ItemStack> supplier, BiConsumer<Integer, ItemStack> consumer) {
        for (int i = 0; i < length; i++) {
            consumer.accept(i, supplier.apply(start + i));
        }
    }

    /**
     * Copies items from an item handler to a destination array.
     */
    private void copyRange(IItemHandler handler, ItemStack[] dest, int destStart, int handlerStart, int length) {
        forRange(handlerStart, length, handler::getStackInSlot,
                (i, stack) -> dest[destStart + i] = stack.copy());
    }

    /**
     * Checks if the items expected from the client match the items actually in the server-side slots.
     * This is a security check to prevent data mismatch.
     */
    private boolean areOfferSlotsConsistent(ItemStack[] expected, ItemStack[] actual) {
        // Check the result slot
        if (!isItemBasicallyEqual(expected[RESULT_SLOT_INDEX], actual[RESULT_SLOT_INDEX])) {
            return false;
        }

        // Check the payment slots (order-agnostic)
        boolean[] matched = new boolean[PAYMENT_SLOT_COUNT];
        for (int i = 0; i < PAYMENT_SLOT_COUNT; i++) {
            ItemStack exp = expected[i];
            if (exp.isEmpty()) continue;

            boolean foundMatch = false;
            for (int j = 0; j < PAYMENT_SLOT_COUNT; j++) {
                if (!matched[j] && isItemBasicallyEqual(exp, actual[j])) {
                    matched[j] = true;
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }

        // Ensure no extra items are in the payment slots
        for (int j = 0; j < PAYMENT_SLOT_COUNT; j++) {
            if (!matched[j] && !actual[j].isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extracts all items from the offer slots based on a copy of their contents.
     */
    private ItemStack[] extractItemsFromOfferSlots(ItemStack[] slotContents) {
        ItemStack[] extracted = new ItemStack[slotContents.length];
        extractRange(shopEntity.getPaymentHandler(), slotContents, extracted, 0, 0, PAYMENT_SLOT_COUNT);
        extractRange(shopEntity.getOfferHandler(), slotContents, extracted, RESULT_SLOT_INDEX, RESULT_SLOT_INDEX, 1);
        return extracted;
    }

    /**
     * Extracts items from an item handler.
     */
    private void extractRange(IItemHandler handler, ItemStack[] slots, ItemStack[] dest, int handlerStart, int destStart, int length) {
        forRange(destStart, length, idx -> slots[idx], (i, stack) ->
                dest[i] = stack.isEmpty() ? ItemStack.EMPTY
                        : handler.extractItem(handlerStart + (i - destStart), stack.getCount(), false));
    }

    /**
     * Compares two ItemStacks to see if they are the same item with the same components and count.
     */
    private boolean isItemBasicallyEqual(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty()) {
            return actual.isEmpty();
        }
        return !actual.isEmpty()
                && ItemStack.isSameItemSameComponents(actual, expected)
                && actual.getCount() == expected.getCount();
    }

    /**
     * Gives a list of item stacks back to the player, dropping any that don't fit in their inventory.
     */
    private void returnStacksToPlayer(ServerPlayer player, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
                }
            }
        }
    }
}