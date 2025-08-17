package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.packets.OfferStatusPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public record OfferManager(SmallShopBlockEntity shopEntity) {
    public boolean applyOffer(ServerPlayer player, ItemStack payment1, ItemStack payment2, ItemStack result) {
        SlotData data = copySlots();
        ItemStack[] slotCopies = data.slots();

        if (!slotsAreValid(new ItemStack[]{payment1, payment2, result}, slotCopies)) {
            return false;
        }

        ItemStack[] extracted = extractItems(data);

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

    private record SlotData(ItemStack[] slots, boolean swapped) {
    }

    private SlotData copySlots() {
        ItemStack slot0 = shopEntity.getPaymentHandler().getStackInSlot(0).copy();
        ItemStack slot1 = shopEntity.getPaymentHandler().getStackInSlot(1).copy();
        boolean swapped = false;
        if (slot0.isEmpty() && !slot1.isEmpty()) {
            slot0 = slot1;
            slot1 = ItemStack.EMPTY;
            swapped = true;
        }

        ItemStack result = shopEntity.getOfferHandler().getStackInSlot(0).copy();
        return new SlotData(new ItemStack[]{slot0, slot1, result}, swapped);
    }

    private boolean slotsAreValid(ItemStack[] expected, ItemStack[] slots) {
        for (int i = 0; i < expected.length; i++) {
            if (!validatePaymentSlot(expected[i], slots[i])) {
                return false;
            }
        }
        return true;
    }

    private ItemStack[] extractItems(SlotData data) {
        ItemStack[] slots = data.slots();
        ItemStack[] extracted = new ItemStack[3];
        if (data.swapped()) {
            extracted[0] = shopEntity.getPaymentHandler().extractItem(1, slots[0].getCount(), false);
            extracted[1] = ItemStack.EMPTY;
        } else {
            extracted[0] = shopEntity.getPaymentHandler().extractItem(0, slots[0].getCount(), false);
            extracted[1] = shopEntity.getPaymentHandler().extractItem(1, slots[1].getCount(), false);
        }
        extracted[2] = shopEntity.getOfferHandler().extractItem(0, slots[2].getCount(), false);
        return extracted;
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