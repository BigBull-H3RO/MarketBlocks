package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.packets.OfferStatusPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;

public record OfferManager(SmallShopBlockEntity shopEntity) {
    public boolean applyOffer(ServerPlayer player, ItemStack payment1, ItemStack payment2, ItemStack result) {
        ItemStack[] slotCopies = copySlots();
        ItemStack[] expected = new ItemStack[]{payment1, payment2, result};

        if (!slotsAreValid(expected, slotCopies)) {
            return false;
        }

        ItemStack[] extracted = extractItems(slotCopies);

        shopEntity.createOffer(slotCopies[0], slotCopies[1], slotCopies[2]);

        PacketDistributor.sendToPlayersTrackingChunk(player.serverLevel(), new ChunkPos(shopEntity.getBlockPos()),
                new OfferStatusPacket(shopEntity.getBlockPos(), true));

        if (shopEntity.getLevel() != null) {
            shopEntity.getLevel().sendBlockUpdated(shopEntity.getBlockPos(),
                    shopEntity.getLevel().getBlockState(shopEntity.getBlockPos()),
                    shopEntity.getLevel().getBlockState(shopEntity.getBlockPos()), 3);
        }

        returnStacksToPlayer(player, extracted);

        MarketBlocks.LOGGER.info("Player {} created offer at {}", player.getName().getString(), shopEntity.getBlockPos());

        return true;
    }

    private ItemStack[] copySlots() {
        ItemStack[] slots = new ItemStack[3];
        copyRange(shopEntity.getPaymentHandler(), slots, 0, 0, 2);
        copyRange(shopEntity.getOfferHandler(), slots, 2, 0, 1);
        return slots;
    }

    private void forRange(int start, int length, IntFunction<ItemStack> supplier, BiConsumer<Integer, ItemStack> consumer) {
        for (int i = 0; i < length; i++) {
            int index = start + i;
            consumer.accept(i, supplier.apply(index));
        }
    }

    private void copyRange(IItemHandler handler, ItemStack[] dest, int destStart, int handlerStart, int length) {
        forRange(handlerStart, length, handler::getStackInSlot,
                (i, stack) -> dest[destStart + i] = stack.copy());
    }

    private boolean slotsAreValid(ItemStack[] expected, ItemStack[] slots) {
        if (!validateRange(expected, slots, 2, 3)) {
            return false;
        }

        boolean[] matched = new boolean[2];
        for (int i = 0; i < 2; i++) {
            ItemStack exp = expected[i];
            if (exp.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (int j = 0; j < 2; j++) {
                if (!matched[j] && validatePaymentSlot(exp, slots[j])) {
                    matched[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        for (int j = 0; j < 2; j++) {
            if (!matched[j] && !slots[j].isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean validateRange(ItemStack[] expected, ItemStack[] slots, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!validatePaymentSlot(expected[i], slots[i])) {
                return false;
            }
        }
        return true;
    }

    private ItemStack[] extractItems(ItemStack[] slots) {
        ItemStack[] extracted = new ItemStack[slots.length];
        extractRange(shopEntity.getPaymentHandler(), slots, extracted, 0, 0, 2);
        extractRange(shopEntity.getOfferHandler(), slots, extracted, 0, 2, 1);
        return extracted;
    }

    private void extractRange(IItemHandler handler, ItemStack[] slots, ItemStack[] dest, int handlerStart, int destStart, int length) {
        forRange(destStart, length, idx -> slots[idx], (i, stack) ->
                dest[destStart + i] = stack.isEmpty() ? ItemStack.EMPTY
                        : handler.extractItem(handlerStart + i, stack.getCount(), false));
    }

    private boolean validatePaymentSlot(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty()) {
            return actual.isEmpty();
        }
        return !actual.isEmpty()
                && ItemStack.isSameItemSameComponents(actual, expected)
                && actual.getCount() == expected.getCount();
    }

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