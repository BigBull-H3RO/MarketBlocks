package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract base class for all shop-related menus.
 * It provides common functionality for adding player inventory slots and a
 * basic implementation for handling shift-clicking (quickMoveStack).
 */
public abstract class AbstractSmallShopMenu extends AbstractContainerMenu {
    protected static final int PLAYER_INVENTORY_ROWS = 3;
    protected static final int PLAYER_INVENTORY_COLUMNS = 9;
    protected static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS;
    protected static final int HOTBAR_SLOT_COUNT = 9;
    protected static final int TOTAL_PLAYER_SLOTS = PLAYER_INVENTORY_SLOT_COUNT + HOTBAR_SLOT_COUNT;

    protected AbstractSmallShopMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    /**
     * Helper method to read the {@link SmallShopBlockEntity} from the network buffer on the client side.
     * @throws IllegalStateException if the block entity at the given position is not a SmallShopBlockEntity.
     */
    @NotNull
    protected static SmallShopBlockEntity readBlockEntity(final @NotNull Inventory playerInventory, final @NotNull RegistryFriendlyByteBuf buf) {
        final BlockPos pos = buf.readBlockPos();
        final BlockEntity be = playerInventory.player.level().getBlockEntity(pos);

        if (be instanceof SmallShopBlockEntity shopEntity) {
            return shopEntity;
        }
        throw new IllegalStateException("Expected a SmallShopBlockEntity at " + pos + " but found " + be);
    }

    /**
     * Adds the standard player inventory and hotbar slots to the menu.
     */
    protected void addPlayerInventory(final @NotNull Inventory playerInventory, int startY) {
        // Player Inventory
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + HOTBAR_SLOT_COUNT, 8 + col * 18, startY + row * 18));
            }
        }

        // Hotbar
        int hotbarY = startY + 58;
        for (int col = 0; col < HOTBAR_SLOT_COUNT; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    /**
     * A template method to initialize all slots. It calls abstract methods
     * to add custom container slots before adding the player inventory.
     */
    protected final void initSlots(final @NotNull Inventory playerInventory) {
        addCustomSlots(playerInventory);
        if (showPlayerInventory()) {
            addPlayerInventory(playerInventory, GuiConstants.PLAYER_INV_Y_START);
        }
    }

    /**
     * Determines whether the player inventory should be shown.
     * Can be overridden by subclasses if needed.
     */
    protected boolean showPlayerInventory() {
        return true;
    }

    /**
     * Abstract method for subclasses to implement their custom slot layouts.
     */
    protected abstract void addCustomSlots(final @NotNull Inventory playerInventory);

    /**
     * A simplified base implementation for handling shift-clicks.
     * This implementation handles moving items between the player's inventory and the container.
     * Subclasses should override this and call super.quickMoveStack, and then handle
     * moving items between their own custom slots.
     *
     * @param player The player who clicked.
     * @param index  The index of the clicked slot.
     * @return The moved item stack, or an empty stack if the move failed.
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int containerSlots = this.slots.size() - TOTAL_PLAYER_SLOTS;
        final int playerInvStart = containerSlots;
        final int hotbarStart = playerInvStart + PLAYER_INVENTORY_SLOT_COUNT;
        final int playerInvEnd = hotbarStart + HOTBAR_SLOT_COUNT;

        Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        // --- Move from container to player inventory ---
        if (index < containerSlots) {
            if (!this.moveItemStackTo(sourceStack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        }
        // --- Move from player inventory to container ---
        else if (index < playerInvEnd) {
            // This is the part subclasses will typically need to customize,
            // as they know which container slots are valid insertion targets.
            // The default is to not move anything into the container.
            // If you want to allow moving into the container, override this method,
            // call super.quickMoveStack, and if it returns EMPTY, handle your custom logic.
            return ItemStack.EMPTY;
        } else {
            // Clicked outside of any known inventory area
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == copyStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return copyStack;
    }
}