package de.bigbull.marketblocks.util.custom.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class LockedChestWrapper implements IItemHandler {
    private final IItemHandler delegate;
    private final java.util.UUID owner;

    public LockedChestWrapper(IItemHandler delegate, java.util.UUID owner) {
        this.delegate = delegate;
        this.owner = owner;
    }

    public java.util.UUID getOwnerId() {
        return owner;
    }

    public IItemHandler getDelegate() {
        return delegate;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}