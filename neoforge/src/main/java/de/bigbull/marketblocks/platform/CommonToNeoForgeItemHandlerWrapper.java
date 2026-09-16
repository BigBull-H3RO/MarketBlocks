package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class CommonToNeoForgeItemHandlerWrapper implements IItemHandler {
    private final ICommonItemHandler common;

    public CommonToNeoForgeItemHandlerWrapper(ICommonItemHandler common) {
        this.common = common;
    }

    @Override
    public int getSlots() {
        return common.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return common.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return common.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return common.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return common.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return common.isItemValid(slot, stack);
    }
}
