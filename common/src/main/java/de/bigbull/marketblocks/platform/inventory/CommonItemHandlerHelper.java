package de.bigbull.marketblocks.platform.inventory;

import net.minecraft.world.item.ItemStack;

public class CommonItemHandlerHelper {

    public static ItemStack insertItem(ICommonItemHandler dest, ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty()) return stack;

        ItemStack current = stack.copy();
        for (int i = 0; i < dest.getSlots(); i++) {
            current = dest.insertItem(i, current, simulate);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return current;
    }

    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty() || !ItemStack.isSameItemSameComponents(a, b)) {
            return false;
        }
        return a.isStackable();
    }
}