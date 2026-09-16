package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import de.bigbull.marketblocks.platform.services.IItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class FabricItemTransferHelper implements IItemTransferHelper {

    @Nullable
    @Override
    public ICommonItemHandler getNeighborItemHandler(Level level, BlockPos pos, Direction side) {
        BlockPos targetPos = pos.relative(side);
        BlockEntity be = level.getBlockEntity(targetPos);
        if (be instanceof Container container) {
            return new ICommonItemHandler() {
                @Override
                public int getSlots() {
                    return container.getContainerSize();
                }

                @Override
                public ItemStack getStackInSlot(int slot) {
                    return container.getItem(slot);
                }

                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    if (stack.isEmpty() || slot >= container.getContainerSize()) return stack;
                    if (!container.canPlaceItem(slot, stack)) return stack;

                    ItemStack existing = container.getItem(slot);
                    int limit = Math.min(container.getMaxStackSize(), stack.getMaxStackSize());

                    if (existing.isEmpty()) {
                        int toInsert = Math.min(stack.getCount(), limit);
                        if (!simulate) {
                            container.setItem(slot, stack.copyWithCount(toInsert));
                            container.setChanged();
                        }
                        return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert) : ItemStack.EMPTY;
                    }

                    if (!ItemStack.isSameItemSameComponents(existing, stack)) return stack;

                    int space = limit - existing.getCount();
                    if (space <= 0) return stack;

                    int toInsert = Math.min(stack.getCount(), space);
                    if (!simulate) {
                        existing.grow(toInsert);
                        container.setChanged();
                    }
                    return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert) : ItemStack.EMPTY;
                }

                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (amount <= 0 || slot >= container.getContainerSize()) return ItemStack.EMPTY;
                    ItemStack existing = container.getItem(slot);
                    if (existing.isEmpty()) return ItemStack.EMPTY;

                    int toExtract = Math.min(amount, existing.getCount());
                    ItemStack extracted = existing.copyWithCount(toExtract);
                    if (!simulate) {
                        existing.shrink(toExtract);
                        if (existing.isEmpty()) {
                            container.setItem(slot, ItemStack.EMPTY);
                        }
                        container.setChanged();
                    }
                    return extracted;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return container.getMaxStackSize();
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return container.canPlaceItem(slot, stack);
                }

                @Override
                public void setStackInSlot(int slot, ItemStack stack) {
                    container.setItem(slot, stack);
                    container.setChanged();
                }
            };
        }
        return null;
    }
}