package de.bigbull.marketblocks.feature.singleoffer.entity;

import net.minecraft.world.item.ItemStack;
import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;

import java.util.UUID;

/**
 * A wrapper for an item handler that allows tracking the chest's owner.
 * Ensures that shop automated IO only interacts with chests belonging to the shop's owners.
 */
public class LockedChestWrapper implements ICommonItemHandler {
    private final ICommonItemHandler delegate;
    private final UUID owner;

    public LockedChestWrapper(ICommonItemHandler delegate, UUID owner) {
        this.delegate = delegate;
        this.owner = owner;
    }

    public UUID getOwnerId() {
        return owner;
    }

    public ICommonItemHandler getDelegate() {
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
        return false;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
    }
}
