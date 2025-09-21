package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container-Menü für den blocklosen Server-Shop.
 */
public class ServerShopMenu extends AbstractContainerMenu {
    public static final int TEMPLATE_SLOTS = 3;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int PLAYER_INV_START_Y = 84;

    private final Container templateContainer;
    private final boolean canEdit;
    private final DataSlot selectedPage;

    public ServerShopMenu(int containerId, Inventory inventory, boolean canEdit, int initialPage) {
        this(RegistriesInit.SERVER_SHOP_MENU.get(), containerId, inventory, canEdit, initialPage);
    }

    public ServerShopMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean(), buf.readVarInt());
    }

    private ServerShopMenu(MenuType<?> menuType, int containerId, Inventory inventory, boolean canEdit, int initialPage) {
        super(menuType, containerId);
        this.canEdit = canEdit;
        this.templateContainer = new SimpleContainer(TEMPLATE_SLOTS) {
            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
        this.selectedPage = new DataSlot() {
            private int value = Math.max(0, initialPage);

            @Override
            public int get() {
                return value;
            }

            @Override
            public void set(int newValue) {
                this.value = Math.max(0, newValue);
            }
        };
        addDataSlot(this.selectedPage);
        addTemplateSlots();
        addPlayerInventory(inventory);
    }

    private void addTemplateSlots() {
        int startX = 44;
        int y = 32;
        for (int i = 0; i < TEMPLATE_SLOTS; i++) {
            int x = startX + i * 26;
            addSlot(new TemplateSlot(templateContainer, i, x, y));
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                addSlot(new Slot(inventory, col + row * PLAYER_INV_COLS + 9, 8 + col * 18,
                        PLAYER_INV_START_Y + row * 18));
            }
        }

        int hotbarY = PLAYER_INV_START_Y + 58;
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSlots = TEMPLATE_SLOTS;
        if (index < containerSlots) {
            Slot slot = this.slots.get(index);
            if (!slot.hasItem()) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = slot.getItem();
            ItemStack copy = stack.copy();
            if (!moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
            return copy;
        }

        if (!canEdit) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (!moveItemStackTo(stack, 0, TEMPLATE_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < templateContainer.getContainerSize(); i++) {
                ItemStack stack = templateContainer.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }

    public boolean isEditor() {
        return canEdit;
    }

    public int selectedPage() {
        return selectedPage.get();
    }

    public void setSelectedPageServer(int page) {
        selectedPage.set(page);
        broadcastChanges();
    }

    public void setSelectedPageClient(int page) {
        selectedPage.set(page);
    }

    public Container templateContainer() {
        return templateContainer;
    }

    public void clearTemplate() {
        for (int i = 0; i < templateContainer.getContainerSize(); i++) {
            templateContainer.setItem(i, ItemStack.EMPTY);
        }
    }

    public ItemStack getTemplateStack(int slot) {
        return templateContainer.getItem(slot);
    }

    private class TemplateSlot extends Slot {
        TemplateSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return canEdit && !stack.isEmpty();
        }

        @Override
        public boolean mayPickup(Player player) {
            return canEdit;
        }
    }
}