package de.bigbull.marketblocks.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class MenuTransferHelper {

    /**
     * Transfers purchased items to the player's inventory, dropping any remainder
     * on the floor.
     * 
     * @param player          The purchasing player
     * @param resultStack     The base item stack being purchased
     * @param purchaseCount   The number of times the resultStack was purchased
     * @param moveItemStackTo Callback that attempts to move the stack into the
     *                        player's inventory slots.
     * @return An ItemStack representing the total bought amount for UI tracking.
     */
    public static ItemStack handlePurchaseTransfer(Player player, ItemStack resultStack, int purchaseCount,
            Consumer<ItemStack> moveItemStackTo) {
        if (purchaseCount <= 0 || resultStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        long remaining = (long) resultStack.getCount() * purchaseCount;
        if (remaining <= 0L) {
            return ItemStack.EMPTY;
        }

        int maxStack = resultStack.getMaxStackSize();
        ItemStack totalBoughtCopy = resultStack.copy();
        totalBoughtCopy.setCount((int) Math.min(Integer.MAX_VALUE, remaining));

        while (remaining > 0L) {
            int chunkSize = (int) Math.min(remaining, maxStack);
            ItemStack chunk = resultStack.copy();
            chunk.setCount(chunkSize);

            moveItemStackTo.accept(chunk);
            if (!chunk.isEmpty()) {
                player.drop(chunk, false);
            }
            remaining -= chunkSize;
        }

        return totalBoughtCopy;
    }
}
