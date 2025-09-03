package de.bigbull.marketblocks.util.custom.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A wrapper for an {@link IItemHandler} that makes it read-only from an external perspective.
 * This is used to protect chests connected to a shop from being modified by pipes or hoppers,
 * forcing all item insertion and extraction to go through the shop's own sided logic.
 */
public final class LockedChestWrapper implements IItemHandler {
    private final IItemHandler delegate;
    private final UUID owner;

    public LockedChestWrapper(@NotNull IItemHandler delegate, @NotNull UUID owner) {
        this.delegate = delegate;
        this.owner = owner;
    }

    /**
     * Gets the UUID of the owner of the shop this wrapper belongs to.
     */
    public UUID getOwnerId() {
        return owner;
    }

    /**
     * Gets the underlying {@link IItemHandler} that this wrapper delegates to.
     * Used by the {@link SmallShopBlockEntity} to perform actual item transfers.
     */
    public IItemHandler getDelegate() {
        return delegate;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    /**
     * Blocks all item insertion from external sources.
     * @return The original stack, indicating that no items were inserted.
     */
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    /**
     * Blocks all item extraction from external sources.
     * @return An empty ItemStack, indicating that no items were extracted.
     */
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}