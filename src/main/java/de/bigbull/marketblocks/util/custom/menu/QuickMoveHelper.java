package de.bigbull.marketblocks.util.custom.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Hilfsmethoden für das Shift-Klicken in Menüs.
 */
public final class QuickMoveHelper {

    /**
     * Funktionales Interface, das {@code moveItemStackTo} kapselt.
     */
    @FunctionalInterface
    public interface MoveItemStackTo {
        boolean move(ItemStack stack, int start, int end, boolean reverse);
    }

    private QuickMoveHelper() {
    }

    /**
     * Verschiebt Items beim Shift-Klicken.
     *
     * @param menu                 Das aufrufende Menü
     * @param player               Der Spieler
     * @param index                Slot-Index
     * @param playerInventoryStart Startindex des Spielerinventars
     * @param hotbarStart          Startindex der Hotbar
     * @param canMoveToContainer   Ob Items in Container-Slots verschoben werden dürfen
     * @param containerStart       Startindex der Container-Zielfelder
     * @param containerEnd         Endindex der Container-Zielfelder (exklusiv)
     * @param mover                Funktion zum Verschieben von ItemStacks (delegiert an {@code moveItemStackTo})
     * @return Die verschobene ItemStack-Kopie oder {@link ItemStack#EMPTY}
     */
    public static ItemStack quickMoveStack(AbstractContainerMenu menu, Player player, int index,
                                           int playerInventoryStart, int hotbarStart,
                                           boolean canMoveToContainer, int containerStart, int containerEnd,
                                           MoveItemStackTo mover) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = menu.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Vom Container zum Spieler
            if (index < playerInventoryStart) {
                if (!mover.move(itemstack1, playerInventoryStart, menu.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean movedToContainer = false;
                if (canMoveToContainer) {
                    movedToContainer = mover.move(itemstack1, containerStart, containerEnd, false);
                }

                if (!movedToContainer) {
                    if (index < hotbarStart) {
                        if (!mover.move(itemstack1, hotbarStart, hotbarStart + 9, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!mover.move(itemstack1, playerInventoryStart, hotbarStart, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}