package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menü für den Inventar-Modus des SmallShop
 */
public class SmallShopInventoryMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;

    private static final int INPUT_SLOTS = 12;
    private static final int OUTPUT_SLOTS = 12;
    private static final int PLAYER_INVENTORY_START = INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;

    private final ContainerData data;

    // Constructor für Server
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.inputHandler = blockEntity.getInputHandler();
        this.outputHandler = blockEntity.getOutputHandler();
        this.data = SmallShopMenuData.create(blockEntity, playerInventory.player);

        addDataSlots(this.data);
        setupSlots(playerInventory);

        if (!playerInventory.player.level().isClientSide() && blockEntity.getOwnerId() == null) {
            blockEntity.setOwner(playerInventory.player);
        }
    }

    // Constructor für Client
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, MenuUtils.readBlockEntity(playerInventory, buf));
    }

    private void setupSlots(Inventory playerInventory) {
        // Input Inventar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new InputSlot(blockEntity, inputHandler, row * 4 + col, 8 + col * 18, 18 + row * 18, playerInventory.player));
            }
        }

        // Output Inventar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OutputSlot(outputHandler, row * 4 + col,
                        98 + col * 18, 18 + row * 18));
            }
        }

        MenuUtils.addPlayerInventory(this::addSlot, playerInventory, GuiConstants.PLAYER_INV_Y_START);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;

        ItemStack ret;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        // Container-Slots: 0..INPUT_SLOTS-1 (Input), INPUT_SLOTS..INPUT_SLOTS+OUTPUT_SLOTS-1 (Output)
        int inputStart  = 0;

        if (index < PLAYER_INVENTORY_START) {
            // Vom Container -> Spielerinventar
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Vom Spieler -> wenn Owner: versuche Input
            boolean moved = false;
            if (isOwner()) {
                moved = this.moveItemStackTo(stack, inputStart, INPUT_SLOTS, false);
            }
            if (!moved) {
                // Andernfalls normales Inventar/Hotbar Shiften (Vanilla-Verhalten)
                if (index < HOTBAR_START) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_START + 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == ret.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return ret;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isOwner() {
        return data.get(2) == 1;
    }

    public static class InputSlot extends SlotItemHandler {
        private final SmallShopBlockEntity blockEntity;
        private final Player player;

        public InputSlot(SmallShopBlockEntity blockEntity, IItemHandler handler, int slot, int x, int y, Player player) {
            super(handler, slot, x, y);
            this.blockEntity = blockEntity;
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return blockEntity.isOwner(player);
        }

        @Override
        public boolean mayPickup(Player player) {
            return blockEntity.isOwner(player);
        }
    }

    public static class OutputSlot extends SlotItemHandler {
        public OutputSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}