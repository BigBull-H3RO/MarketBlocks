package de.bigbull.marketblocks.block.entity;

import de.bigbull.marketblocks.util.custom.block.SideMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.EnumMap;
import java.util.Map;

public class ShopInventoryManager {

    private final SmallShopBlockEntity blockEntity;
    private final Map<Direction, IItemHandler> cachedNeighbors = new EnumMap<>(Direction.class);

    public ShopInventoryManager(SmallShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void updateNeighborCache() {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        cachedNeighbors.clear();

        for (Direction dir : Direction.values()) {
            SideMode mode = blockEntity.getModeForSide(dir);
            if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                IItemHandler handler = findNeighborHandler(level, blockEntity.getBlockPos(), dir);
                if (handler != null) {
                    cachedNeighbors.put(dir, handler);
                }
            }
        }
    }

    private IItemHandler findNeighborHandler(Level level, BlockPos pos, Direction dir) {
        BlockPos neighbourPos = pos.relative(dir);
        IItemHandler neighbour = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, dir.getOpposite());
        if (neighbour == null) {
            neighbour = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, null);
        }
        if (neighbour instanceof LockedChestWrapper locked) {
            if (locked.getOwnerId() != null && blockEntity.getOwners().contains(locked.getOwnerId())) {
                return locked.getDelegate();
            } else {
                return null;
            }
        }
        return neighbour;
    }

    public IItemHandler getValidNeighborHandler(Direction dir) {
        return cachedNeighbors.get(dir);
    }

    public void transferItems(IItemHandler from, IItemHandler to) {
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stackInSlot = from.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            ItemStack remainderSim = ItemHandlerHelper.insertItem(to, stackInSlot.copy(), true);
            int transferable = stackInSlot.getCount() - remainderSim.getCount();
            if (transferable > 0) {
                ItemStack extracted = from.extractItem(i, transferable, false);
                ItemStack leftover = ItemHandlerHelper.insertItem(to, extracted, false);
                if (!leftover.isEmpty()) {
                    from.insertItem(i, leftover, false);
                }
            }
        }
    }

    public void pullFromInputChest(ItemStackHandler inputHandler) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;
        for (Direction dir : Direction.values()) {
            if (blockEntity.getModeForSide(dir) == SideMode.INPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(neighbour, inputHandler);
                }
            }
        }
    }

    public void pushToOutputChest(ItemStackHandler outputHandler) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;
        for (Direction dir : Direction.values()) {
            if (blockEntity.getModeForSide(dir) == SideMode.OUTPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(outputHandler, neighbour);
                }
            }
        }
    }
}
