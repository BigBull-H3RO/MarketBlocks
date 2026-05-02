package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.entity.LockedChestWrapper;
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

    private static final Direction[] DIRECTIONS = Direction.values();

    private final SingleOfferShopBlockEntity blockEntity;
    private final Map<Direction, IItemHandler> cachedNeighbors = new EnumMap<>(Direction.class);

    public ShopInventoryManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void updateNeighborCache() {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
            cachedNeighbors.clear();
            return;
        }

        for (Direction dir : DIRECTIONS) {
            SideMode mode = blockEntity.getModeForSide(dir);

            if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                IItemHandler handler = findNeighborHandler(level, blockEntity.getBlockPos(), dir);
                if (handler != null) {
                    cachedNeighbors.put(dir, handler);
                } else {
                    cachedNeighbors.remove(dir);
                }
            } else {
                // Mode is DISABLED, clear cache
                cachedNeighbors.remove(dir);
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
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
            return null;
        }
        return cachedNeighbors.get(dir);
    }

    public void transferItems(IItemHandler from, IItemHandler to) {
        if (from == null || to == null || from == to) {
            return;
        }
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stackInSlot = from.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            ItemStack remainderSim = ItemHandlerHelper.insertItem(to, stackInSlot.copy(), true);
            int transferable = stackInSlot.getCount() - remainderSim.getCount();
            if (transferable > 0) {
                ItemStack extracted = from.extractItem(i, transferable, false);
                if (extracted.isEmpty()) {
                    continue;
                }
                ItemStack leftover = ItemHandlerHelper.insertItem(to, extracted, false);
                if (!leftover.isEmpty()) {
                    from.insertItem(i, leftover, false);
                }
            }
        }
    }

    /**
     * Pulls items from connected neighbor input chests into the shop's input inventory.
     *
     * SIDE EFFECTS: This method MODIFIES neighbor inventories by extracting items.
     * Should only be called on the server side to prevent desync.
     *
     * @param inputHandler The shop's input inventory to insert items into
     */
    public void pullFromInputChest(ItemStackHandler inputHandler) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) return;
        for (Direction dir : DIRECTIONS) {
            if (blockEntity.getModeForSide(dir) == SideMode.INPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(neighbour, inputHandler);
                }
            }
        }
    }

    /**
     * Pushes items from the shop's output inventory to connected neighbor output chests.
     *
     * SIDE EFFECTS: This method MODIFIES neighbor inventories by inserting items.
     * Should only be called on the server side to prevent desync.
     *
     * @param outputHandler The shop's output inventory to extract items from
     */
    public void pushToOutputChest(ItemStackHandler outputHandler) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) return;
        for (Direction dir : DIRECTIONS) {
            if (blockEntity.getModeForSide(dir) == SideMode.OUTPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(outputHandler, neighbour);
                }
            }
        }
    }
}


