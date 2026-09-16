package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import de.bigbull.marketblocks.platform.services.IItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class NeoForgeItemTransferHelper implements IItemTransferHelper {

    @Nullable
    @Override
    public ICommonItemHandler getNeighborItemHandler(Level level, BlockPos pos, Direction side) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) {
            handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        }
        if (handler == null) return null;

        final IItemHandler target = handler;
        return new ICommonItemHandler() {
            @Override
            public int getSlots() {
                return target.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return target.getStackInSlot(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return target.insertItem(slot, stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return target.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return target.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return target.isItemValid(slot, stack);
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                // If the target supports direct setting, or ignore
                if (target instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
                    modifiable.setStackInSlot(slot, stack);
                }
            }
        };
    }
}