package de.bigbull.marketblocks.platform.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CommonSlotItemHandler extends Slot {
    private static final Container EMPTY_CONTAINER = new SimpleContainer(0);
    protected final ICommonItemHandler itemHandler;
    protected final int index;

    public CommonSlotItemHandler(ICommonItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(EMPTY_CONTAINER, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return itemHandler.isItemValid(index, stack);
    }

    @Override
    public ItemStack getItem() {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public void set(ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return itemHandler.getSlotLimit(index);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), getMaxStackSize());
    }

    @Override
    public boolean mayPickup(Player player) {
        return !itemHandler.extractItem(index, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return itemHandler.extractItem(index, amount, false);
    }

        public int getSlotIndex() {
        return index;
    }

    public ICommonItemHandler getItemHandler() {
        return itemHandler;
    }
}