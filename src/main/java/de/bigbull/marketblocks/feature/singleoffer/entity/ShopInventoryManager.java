package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
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

    private final ItemStackHandler outputSimulationHandler = new ItemStackHandler(12);

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
            SideMode mode = blockEntity.getMode(dir);

            if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                IItemHandler handler = findNeighborHandler(level, blockEntity.getBlockPos(), dir);
                if (handler != null) {
                    cachedNeighbors.put(dir, handler);
                } else {
                    cachedNeighbors.remove(dir);
                }
            } else {
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
                if (extracted.isEmpty()) continue;
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
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) return;
        for (Direction dir : DIRECTIONS) {
            if (blockEntity.getMode(dir) == SideMode.INPUT) {
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
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) return;
        for (Direction dir : DIRECTIONS) {
            if (blockEntity.getMode(dir) == SideMode.OUTPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(outputHandler, neighbour);
                }
            }
        }
    }

    public int countMatchingPayment(ItemStack target) {
        if (target.isEmpty()) return 0;
        int total = 0;
        ItemStackHandler paymentHandler = blockEntity.getPaymentHandler();
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public int countMatchingInput(ItemStack target, boolean checkNeighbors) {
        if (target.isEmpty()) return 0;
        int found = 0;
        ItemStackHandler inputHandler = blockEntity.getInputHandler();
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                found += stack.getCount();
            }
        }
        if (checkNeighbors && Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get() && blockEntity.getLevel() != null) {
            for (Direction dir : DIRECTIONS) {
                if (blockEntity.getMode(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour != null) {
                        for (int i = 0; i < neighbour.getSlots(); i++) {
                            ItemStack stack = neighbour.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                ItemStack safeCopy = stack.copy();
                                if (ItemStack.isSameItemSameComponents(safeCopy, target)) {
                                    found += safeCopy.getCount();
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    public boolean hasOutputSpace(ItemStack... stacks) {
        ItemStackHandler testHandler = prepareOutputSimulationHandler();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            ItemStack remaining = ItemHandlerHelper.insertItem(testHandler, stack.copy(), false);
            if (!remaining.isEmpty()) return false;
        }
        return true;
    }

    private ItemStackHandler prepareOutputSimulationHandler() {
        ItemStackHandler outputHandler = blockEntity.getOutputHandler();
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            outputSimulationHandler.setStackInSlot(i, outputHandler.getStackInSlot(i).copy());
        }
        return outputSimulationHandler;
    }

    public int simulateOutputSpace(ItemStack p1, ItemStack p2, int maxTransactions) {
        ItemStackHandler testHandler = prepareOutputSimulationHandler();
        int validAmount = 0;
        for (int i = 0; i < maxTransactions; i++) {
            boolean fits = true;
            if (!p1.isEmpty()) {
                if (!ItemHandlerHelper.insertItem(testHandler, p1.copy(), false).isEmpty()) fits = false;
            }
            if (fits && !p2.isEmpty()) {
                if (!ItemHandlerHelper.insertItem(testHandler, p2.copy(), false).isEmpty()) fits = false;
            }
            if (fits) {
                validAmount++;
            } else {
                break;
            }
        }
        return validAmount;
    }

    public void updateOutputFullness() {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) return;

        int total = 0;
        int filled = 0;
        ItemStackHandler outputHandler = blockEntity.getOutputHandler();
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemStack stack = outputHandler.getStackInSlot(i);
            int limit = outputHandler.getSlotLimit(i);
            if (!stack.isEmpty()) limit = Math.min(limit, stack.getMaxStackSize());
            total += limit;
            filled += stack.getCount();
        }

        if (Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
            for (Direction dir : DIRECTIONS) {
                if (blockEntity.getMode(dir) != SideMode.OUTPUT) continue;
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;
                for (int i = 0; i < neighbour.getSlots(); i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    int limit = neighbour.getSlotLimit(i);
                    if (!stack.isEmpty()) limit = Math.min(limit, stack.getMaxStackSize());
                    total += limit;
                    filled += stack.getCount();
                }
            }
        }

        boolean newOutputFull = total > 0 && filled >= total;
        boolean newOutputAlmostFull = false;
        if (Config.ENABLE_OUTPUT_WARNING.get()) {
            int threshold = Config.OUTPUT_WARNING_PERCENT.get();
            newOutputAlmostFull = total > 0 && (filled * 100 >= total * threshold);
        }

        if (newOutputFull != blockEntity.isOutputFull() || newOutputAlmostFull != blockEntity.isOutputAlmostFull()) {
            blockEntity.getSettingsManager().setOutputFullness(newOutputFull, newOutputAlmostFull);
            blockEntity.sync();
        }
    }

    public void addToOutputBatched(ItemStack stack, int times) {
        if (stack == null || stack.isEmpty() || times <= 0) return;
        long total = (long) stack.getCount() * times;
        if (total <= 0L) return;

        int maxStack = stack.getMaxStackSize();
        while (total > 0L) {
            ItemStack chunk = stack.copy();
            chunk.setCount((int) Math.min(total, maxStack));
            ItemHandlerHelper.insertItem(blockEntity.getOutputHandler(), chunk, false);
            total -= chunk.getCount();
        }
    }

    public void removeFromInput(ItemStack toRemove) {
        if (toRemove == null || toRemove.isEmpty()) return;
        int remaining = toRemove.getCount();
        if (remaining <= 0) return;

        ItemStackHandler inputHandler = blockEntity.getInputHandler();
        for (int i = 0; i < inputHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                inputHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }

        if (Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get() && remaining > 0 && blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
            for (Direction dir : DIRECTIONS) {
                if (blockEntity.getMode(dir) != SideMode.INPUT) continue;
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;

                for (int i = 0; i < neighbour.getSlots() && remaining > 0; i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                        int toTake = Math.min(remaining, stack.getCount());
                        neighbour.extractItem(i, toTake, false);
                        remaining -= toTake;
                    }
                }
                if (remaining <= 0) break;
            }
        }
    }

    public void removePayment(ItemStack required) {
        if (required == null || required.isEmpty()) return;
        int remaining = required.getCount();
        if (remaining <= 0) return;

        ItemStackHandler paymentHandler = blockEntity.getPaymentHandler();
        for (int i = 0; i < paymentHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toTake = Math.min(remaining, stack.getCount());
                paymentHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }
    }
}
