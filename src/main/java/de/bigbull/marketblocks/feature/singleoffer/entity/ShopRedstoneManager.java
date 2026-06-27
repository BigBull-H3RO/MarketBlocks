package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class ShopRedstoneManager {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final SingleOfferShopBlockEntity shop;

    public ShopRedstoneManager(SingleOfferShopBlockEntity shop) {
        this.shop = shop;
    }

    public boolean isPoweredByRedstone() {
        return shop.getLevel() != null && shop.getLevel().hasNeighborSignal(shop.getBlockPos());
    }

    public void triggerRedstonePulse() {
        if (shop.getLevel() == null || shop.getLevel().isClientSide || !shop.isEmitRedstone()) {
            return;
        }
        BlockState state = shop.getLevel().getBlockState(shop.getBlockPos());
        if (state.getBlock() instanceof BaseShopBlock block) {
            shop.getLevel().setBlock(shop.getBlockPos(), state.setValue(BaseShopBlock.POWERED, true), 3);
            shop.getLevel().updateNeighborsAt(shop.getBlockPos(), block);
            shop.getLevel().scheduleTick(shop.getBlockPos(), block, 20);
        }
    }

    public int getAnalogSignal(Direction readSide) {
        SideMode mode = shop.getMode(readSide);
        if (mode == SideMode.OUTPUT) {
            return calculateComparatorSignal(shop.getOutputHandler());
        } else if (mode == SideMode.INPUT) {
            return calculateComparatorSignal(shop.getInputHandler());
        }
        return 0;
    }

    private int calculateComparatorSignal(IItemHandler handler) {
        if (handler == null)
            return 0;

        int totalSlots = handler.getSlots();
        if (totalSlots == 0)
            return 0;

        float fullness = 0.0F;
        boolean hasItem = false;

        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                fullness += (float) stack.getCount()
                        / (float) Math.min(handler.getSlotLimit(i), stack.getMaxStackSize());
                hasItem = true;
            }
        }

        fullness /= (float) totalSlots;
        return Mth.floor(fullness * 14.0F) + (hasItem ? 1 : 0);
    }

    private boolean isChestIoExtensionEnabled() {
        return Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get();
    }

    public void unlockAdjacentChests() {
        if (shop.getLevel() == null || !isChestIoExtensionEnabled())
            return;
        for (Direction dir : DIRECTIONS) {
            invalidateNeighbor(dir);
        }
    }

    public void lockAdjacentChest(Direction side) {
        if (shop.getLevel() == null || !isChestIoExtensionEnabled())
            return;
        invalidateNeighbor(side);
    }

    private void invalidateNeighbor(Direction dir) {
        BlockPos neighbour = shop.getBlockPos().relative(dir);
        shop.getLevel().invalidateCapabilities(neighbour);
    }

    public void lockAdjacentChests() {
        if (shop.getLevel() == null || !isChestIoExtensionEnabled())
            return;
        for (Direction dir : DIRECTIONS) {
            if (shop.getMode(dir) == SideMode.INPUT || shop.getMode(dir) == SideMode.OUTPUT) {
                lockAdjacentChest(dir);
            }
        }
    }

    public void invalidateCapabilitiesAndNeighbor(Direction dir) {
        if (shop.getLevel() != null) {
            shop.getLevel().invalidateCapabilities(shop.getBlockPos());
            BlockPos neighbour = shop.getBlockPos().relative(dir);
            shop.getLevel().invalidateCapabilities(neighbour);
        }
    }
}
