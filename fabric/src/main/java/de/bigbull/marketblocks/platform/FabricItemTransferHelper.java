package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import de.bigbull.marketblocks.platform.services.IItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FabricItemTransferHelper implements IItemTransferHelper {

    @Nullable
    @Override
    public ICommonItemHandler getNeighborItemHandler(Level level, BlockPos pos, Direction side) {
        if (level == null || !level.hasChunkAt(pos)) {
            return null;
        }
        Container container = null;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
        } else {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container c) {
                container = c;
            }
        }

        if (container == null)
            return null;

        final Container finalContainer = container;
        return new ICommonItemHandler() {
            @Override
            public int getSlots() {
                return finalContainer.getContainerSize();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return finalContainer.getItem(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty() || slot >= finalContainer.getContainerSize())
                    return stack;
                if (!finalContainer.canPlaceItem(slot, stack))
                    return stack;

                ItemStack existing = finalContainer.getItem(slot);
                int limit = Math.min(finalContainer.getMaxStackSize(), stack.getMaxStackSize());

                if (existing.isEmpty()) {
                    int toInsert = Math.min(stack.getCount(), limit);
                    if (!simulate) {
                        finalContainer.setItem(slot, stack.copyWithCount(toInsert));
                        finalContainer.setChanged();
                    }
                    return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert)
                            : ItemStack.EMPTY;
                }

                if (!ItemStack.isSameItemSameComponents(existing, stack))
                    return stack;

                int space = limit - existing.getCount();
                if (space <= 0)
                    return stack;

                int toInsert = Math.min(stack.getCount(), space);
                if (!simulate) {
                    existing.grow(toInsert);
                    finalContainer.setChanged();
                }
                return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert) : ItemStack.EMPTY;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount <= 0 || slot >= finalContainer.getContainerSize())
                    return ItemStack.EMPTY;
                ItemStack existing = finalContainer.getItem(slot);
                if (existing.isEmpty())
                    return ItemStack.EMPTY;

                int toExtract = Math.min(amount, existing.getCount());
                ItemStack extracted = existing.copyWithCount(toExtract);
                if (!simulate) {
                    existing.shrink(toExtract);
                    if (existing.isEmpty()) {
                        finalContainer.setItem(slot, ItemStack.EMPTY);
                    }
                    finalContainer.setChanged();
                }
                return extracted;
            }

            @Override
            public int getSlotLimit(int slot) {
                return finalContainer.getMaxStackSize();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return finalContainer.canPlaceItem(slot, stack);
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                finalContainer.setItem(slot, stack);
                finalContainer.setChanged();
            }
        };
    }
}
