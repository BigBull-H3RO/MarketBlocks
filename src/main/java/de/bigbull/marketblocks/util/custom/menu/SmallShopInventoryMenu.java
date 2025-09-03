package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * The menu for the "Inventory" tab of the Small Shop.
 * This UI is only accessible to the owner and is used to manage the shop's input and output item buffers.
 */
public class SmallShopInventoryMenu extends AbstractSmallShopMenu implements ShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final ContainerData data;

    private static final int INPUT_SLOTS = 12;
    private static final int OUTPUT_SLOTS = 12;
    private static final int CONTAINER_SLOT_COUNT = INPUT_SLOTS + OUTPUT_SLOTS;

    /**
     * Server-side constructor.
     */
    public SmallShopInventoryMenu(int containerId, @NotNull Inventory playerInventory, @NotNull SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.createMenuFlags(playerInventory.player);

        this.blockEntity.ensureOwner(playerInventory.player);
        this.addDataSlots(this.data);
        this.initSlots(playerInventory);
    }

    /**
     * Client-side constructor.
     */
    public SmallShopInventoryMenu(int containerId, @NotNull Inventory playerInventory, @NotNull RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);
        this.blockEntity = readBlockEntity(playerInventory, buf);
        this.data = new SimpleContainerData(1);

        this.blockEntity.ensureOwner(playerInventory.player);
        this.addDataSlots(this.data);
        this.initSlots(playerInventory);
    }

    @Override
    protected void addCustomSlots(final @NotNull Inventory playerInventory) {
        final IItemHandler inputHandler = this.blockEntity.getInputHandler();
        final IItemHandler outputHandler = this.blockEntity.getOutputHandler();

        // Input Inventory (4x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new InputSlot(inputHandler, row * 4 + col, 8 + col * 18, 18 + row * 18, this));
            }
        }

        // Output Inventory (4x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OutputSlot(outputHandler, row * 4 + col, 98 + col * 18, 18 + row * 18));
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        // --- Move from Container to Player ---
        if (index < CONTAINER_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, CONTAINER_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }
        // --- Move from Player to Container ---
        else {
            // Try to move into the input slots only. Output slots are not valid targets.
            if (!this.moveItemStackTo(sourceStack, 0, INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.blockEntity.stillValid(player);
    }

    @Override
    public @NotNull SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getFlags() {
        return data.get(0);
    }

    /**
     * Custom slot for the input buffer. Only owners can interact with it.
     */
    public static class InputSlot extends SlotItemHandler {
        private final ShopMenu menu;

        public InputSlot(IItemHandler handler, int slot, int x, int y, ShopMenu menu) {
            super(handler, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return menu.isOwner();
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            return menu.isOwner();
        }
    }

    /**
     * Custom slot for the output buffer. Players can only take items from it, not place them.
     */
    public static class OutputSlot extends SlotItemHandler {
        public OutputSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}