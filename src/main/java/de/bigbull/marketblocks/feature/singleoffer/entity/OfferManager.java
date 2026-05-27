package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.log.ShopTransactionLogSavedData;
import de.bigbull.marketblocks.feature.log.TransactionLogEntry;
import de.bigbull.marketblocks.network.singleoffer.OfferStatusPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OfferManager(SingleOfferShopBlockEntity shopEntity) {
    private static final int PAYMENT_SLOT_COUNT = 2;
    private static final int RESULT_SLOT_INDEX = 2;
    private static final int TOTAL_OFFER_SLOTS = 3;

    public record OfferValidation(boolean valid, String errorKey) {
        public static final OfferValidation VALID = new OfferValidation(true, null);
    }

    public boolean applyOffer(ServerPlayer player, ItemStack payment1, ItemStack payment2, ItemStack result) {
        OfferValidation validation = validateOffer(payment1, payment2, result);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.translatable(validation.errorKey()));
            return false;
        }

        ItemStack[] slotCopies = copyOfferSlotsFromShop();
        ItemStack[] extractedItems = extractItemsFromOfferSlots(slotCopies);

        shopEntity.createOffer(slotCopies[0], slotCopies[1], slotCopies[RESULT_SLOT_INDEX]);

        PacketDistributor.sendToPlayersTrackingChunk(player.serverLevel(), new ChunkPos(shopEntity.getBlockPos()),
                new OfferStatusPacket(shopEntity.getBlockPos(), true));

        returnStacksToPlayer(player, extractedItems);
        MarketBlocks.LOGGER.info("Player {} created offer at {}", player.getName().getString(), shopEntity.getBlockPos());
        return true;
    }

    private OfferValidation validateOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        if (result.isEmpty()) return new OfferValidation(false, "gui.marketblocks.error.no_result_item");
        if (payment1.isEmpty() && payment2.isEmpty()) return new OfferValidation(false, "gui.marketblocks.error.no_payment_items");
        ItemStack[] itemsInSlots = copyOfferSlotsFromShop();
        ItemStack[] expectedItems = new ItemStack[]{payment1, payment2, result};
        if (!areOfferSlotsConsistent(expectedItems, itemsInSlots)) {
            return new OfferValidation(false, "gui.marketblocks.error.invalid_offer");
        }
        return OfferValidation.VALID;
    }

    private ItemStack[] copyOfferSlotsFromShop() {
        ItemStack[] slots = new ItemStack[TOTAL_OFFER_SLOTS];
        copyRange(shopEntity.getPaymentHandler(), slots, 0, 0, PAYMENT_SLOT_COUNT);
        copyRange(shopEntity.getOfferHandler(), slots, RESULT_SLOT_INDEX, 0, 1);
        return slots;
    }

    private void copyRange(IItemHandler handler, ItemStack[] dest, int destStart, int handlerStart, int length) {
        for (int i = 0; i < length; i++) {
            ItemStack stack = handler.getStackInSlot(handlerStart + i);
            dest[destStart + i] = stack.copy();
        }
    }

    private boolean areOfferSlotsConsistent(ItemStack[] expected, ItemStack[] actual) {
        if (!isItemBasicallyEqual(expected[RESULT_SLOT_INDEX], actual[RESULT_SLOT_INDEX])) return false;
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
        for (int j = 0; j < PAYMENT_SLOT_COUNT; j++) {
            if (!matched[j] && !actual[j].isEmpty()) return false;
        }
        return true;
    }

    private ItemStack[] extractItemsFromOfferSlots(ItemStack[] slotContents) {
        ItemStack[] extracted = new ItemStack[slotContents.length];
        extractRange(shopEntity.getPaymentHandler(), slotContents, extracted, 0, 0, PAYMENT_SLOT_COUNT);
        extractRange(shopEntity.getOfferHandler(), slotContents, extracted, 0, RESULT_SLOT_INDEX, 1);
        return extracted;
    }

    private void extractRange(IItemHandler handler, ItemStack[] slots, ItemStack[] dest, int handlerStart, int destStart, int length) {
        for (int i = 0; i < length; i++) {
            int index = destStart + i;
            ItemStack stack = slots[index];
            dest[index] = stack.isEmpty() ? ItemStack.EMPTY : handler.extractItem(handlerStart + i, stack.getCount(), false);
        }
    }

    private boolean isItemBasicallyEqual(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty()) return actual.isEmpty();
        return !actual.isEmpty() && ItemStack.isSameItemSameComponents(actual, expected) && actual.getCount() == expected.getCount();
    }

    private void returnStacksToPlayer(ServerPlayer player, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                if (!stack.isEmpty()) Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            }
        }
    }

    public boolean canAfford() {
        ItemStack p1 = shopEntity.getOfferPayment1();
        ItemStack p2 = shopEntity.getOfferPayment2();
        if (p1.isEmpty() && p2.isEmpty()) return true;

        ShopInventoryManager invManager = new ShopInventoryManager(shopEntity); // Using temporary instance is fine, but better to get from ShopEntity
        // Wait, ShopEntity has private final ShopInventoryManager inventoryManager; I need an accessor.
        // Or I can just pass it or get it. Let's create `getInventoryManager()` in ShopEntity.
        ShopInventoryManager inv = shopEntity.getInventoryManager();

        if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
            int required = p1.getCount() + p2.getCount();
            return inv.countMatchingPayment(p1) >= required;
        }

        return (p1.isEmpty() || inv.countMatchingPayment(p1) >= p1.getCount()) &&
               (p2.isEmpty() || inv.countMatchingPayment(p2) >= p2.getCount());
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
        ItemStack result = shopEntity.getOfferResult();
        if (result.isEmpty()) return false;
        if (shopEntity.isAdminShopEnabled()) return true;
        return shopEntity.getInventoryManager().countMatchingInput(result, checkNeighbors) >= result.getCount();
    }

    public int processBulkPurchase(int maxAmount, @Nullable Player buyer, boolean shiftPurchase) {
        if (maxAmount <= 0) return 0;
        boolean adminShop = shopEntity.isAdminShopEnabled();

        ShopInventoryManager inv = shopEntity.getInventoryManager();

        if (!adminShop && de.bigbull.marketblocks.core.config.Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
            inv.updateNeighborCache();
            inv.pullFromInputChest(shopEntity.getInputHandler());
        }

        if (!shopEntity.hasOffer()) return 0;
        ItemStack p1 = shopEntity.getOfferPayment1();
        ItemStack p2 = shopEntity.getOfferPayment2();
        ItemStack result = shopEntity.getOfferResult();
        if (result.isEmpty()) return 0;

        int affordable = Integer.MAX_VALUE;
        if (!p1.isEmpty()) affordable = Math.min(affordable, inv.countMatchingPayment(p1) / p1.getCount());
        if (!p2.isEmpty()) {
            if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
                int totalReqPerUnit = p1.getCount() + p2.getCount();
                affordable = inv.countMatchingPayment(p1) / totalReqPerUnit;
            } else {
                affordable = Math.min(affordable, inv.countMatchingPayment(p2) / p2.getCount());
            }
        }
        if (p1.isEmpty() && p2.isEmpty()) affordable = maxAmount;

        int inStock = adminShop ? Integer.MAX_VALUE : inv.countMatchingInput(result, true) / result.getCount();

        int actualAmount = Math.min(maxAmount, Math.min(affordable, inStock));
        if (actualAmount <= 0) return 0;

        int validAmount = adminShop ? actualAmount : (actualAmount == 1 ? (inv.hasOutputSpace(p1, p2) ? 1 : 0) : inv.simulateOutputSpace(p1, p2, actualAmount));
        actualAmount = validAmount;

        if (actualAmount <= 0) {
            if (!adminShop) inv.updateOutputFullness();
            return 0;
        }

        executeTrades(p1, p2, result, actualAmount, adminShop, inv);
        
        shopEntity.incrementVisualPurchaseCounter(actualAmount);
        shopEntity.playPurchaseXpSound(actualAmount);

        appendTransactionEntry(resolveBuyerIdentity(buyer), p1, p2, result, actualAmount, shiftPurchase);

        shopEntity.sync();
        shopEntity.triggerRedstonePulse();
        // Since needsOfferRefresh is private, we can call updateOfferSlot
        shopEntity.updateOfferSlot();
        return actualAmount;
    }

    private void executeTrades(ItemStack p1, ItemStack p2, ItemStack result, int tradeCount, boolean adminShop, ShopInventoryManager inv) {
        if (tradeCount <= 0) return;

        ItemStack totalP1 = multiplyStackForTrades(p1, tradeCount);
        ItemStack totalP2 = multiplyStackForTrades(p2, tradeCount);
        ItemStack totalResult = multiplyStackForTrades(result, tradeCount);

        if (!totalP1.isEmpty()) inv.removePayment(totalP1);
        if (!totalP2.isEmpty()) inv.removePayment(totalP2);
        if (!adminShop && !totalResult.isEmpty()) inv.removeFromInput(totalResult);

        if (!adminShop) {
            inv.addToOutputBatched(p1, tradeCount);
            inv.addToOutputBatched(p2, tradeCount);
        }
    }

    private ItemStack multiplyStackForTrades(ItemStack stack, int tradeCount) {
        if (stack == null || stack.isEmpty() || tradeCount <= 0) return ItemStack.EMPTY;
        long total = (long) stack.getCount() * tradeCount;
        if (total <= 0L) return ItemStack.EMPTY;
        ItemStack multiplied = stack.copy();
        multiplied.setCount((int) Math.min(Integer.MAX_VALUE, total));
        return multiplied;
    }

    private void appendTransactionEntry(@Nullable BuyerIdentity buyer, ItemStack payment1, ItemStack payment2, ItemStack result, int tradeCount, boolean shiftPurchase) {
        if (!(shopEntity.getLevel() instanceof ServerLevel serverLevel) || tradeCount <= 0) return;

        List<ItemStack> paidStacks = new ArrayList<>(2);
        ItemStack paidOne = TransactionLogEntry.scaleStack(payment1, tradeCount);
        ItemStack paidTwo = TransactionLogEntry.scaleStack(payment2, tradeCount);
        if (!paidOne.isEmpty()) paidStacks.add(paidOne);
        if (!paidTwo.isEmpty()) paidStacks.add(paidTwo);

        List<ItemStack> boughtStacks = new ArrayList<>(1);
        ItemStack bought = TransactionLogEntry.scaleStack(result, tradeCount);
        if (!bought.isEmpty()) boughtStacks.add(bought);

        UUID buyerId = buyer != null ? buyer.uuid() : new UUID(0L, 0L);
        String buyerName = buyer != null ? buyer.name() : "";

        TransactionLogEntry entry = TransactionLogEntry.now(
                buyerId, buyerName, paidStacks, boughtStacks,
                shiftPurchase ? TransactionLogEntry.PurchaseKind.SHIFT : TransactionLogEntry.PurchaseKind.SINGLE
        );

        ShopTransactionLogSavedData.get(serverLevel).appendEntry(
                ShopTransactionLogSavedData.SINGLE_OFFER_SHOP_TYPE,
                serverLevel.dimension(),
                shopEntity.getBlockPos(),
                entry,
                100 // MAX_TRANSACTION_LOG_ENTRIES
        );
    }

    private record BuyerIdentity(UUID uuid, String name) {}

    private @Nullable BuyerIdentity resolveBuyerIdentity(@Nullable Player directBuyer) {
        if (directBuyer != null) {
            return new BuyerIdentity(directBuyer.getUUID(), directBuyer.getGameProfile().getName());
        }
        if (shopEntity.purchaseContextBuyerId != null) {
            return new BuyerIdentity(shopEntity.purchaseContextBuyerId, shopEntity.purchaseContextBuyerName);
        }
        return null;
    }
}
