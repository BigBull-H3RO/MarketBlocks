package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopOffer;
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

import java.util.UUID;

/**
 * Container-Menü für den blocklosen Server-Shop.
 */
public class ServerShopMenu extends AbstractContainerMenu {
    public static final int TEMPLATE_SLOTS = 3;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int PLAYER_INV_START_Y = 125;
    private static final int RESULT_SLOT = 2;

    private final Container tradeContainer = new SimpleContainer(TEMPLATE_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            ServerShopMenu.this.slotsChanged(this);
        }
    };

    private final boolean canEdit;
    private final DataSlot selectedPage;
    private final Inventory inventory;

    private ServerShopOffer currentTradingOffer;

    public ServerShopMenu(int containerId, Inventory inventory, boolean canEdit, int initialPage) {
        this(RegistriesInit.SERVER_SHOP_MENU.get(), containerId, inventory, canEdit, initialPage);
    }

    public ServerShopMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean(), buf.readVarInt());
    }

    private ServerShopMenu(MenuType<?> menuType, int containerId, Inventory inventory, boolean canEdit, int initialPage) {
        super(menuType, containerId);
        this.inventory = inventory;
        this.canEdit = canEdit;

        this.selectedPage = new DataSlot() {
            private int value = Math.max(0, initialPage);
            @Override public int get() { return value; }
            @Override public void set(int newValue) { this.value = Math.max(0, newValue); }
        };
        addDataSlot(this.selectedPage);

        addTemplateSlots();
        addPlayerInventory(inventory);
    }

    private void addTemplateSlots() {
        addSlot(new Slot(tradeContainer, 0, 136, 78) {
            @Override public void setChanged() { super.setChanged(); slotsChanged(tradeContainer); }
        });
        addSlot(new Slot(tradeContainer, 1, 162, 78) {
            @Override public void setChanged() { super.setChanged(); slotsChanged(tradeContainer); }
        });
        addSlot(new ServerShopResultSlot(inventory.player, tradeContainer, RESULT_SLOT, 220, 78));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                addSlot(new Slot(inventory, col + row * PLAYER_INV_COLS + 9, 108 + col * 18,
                        PLAYER_INV_START_Y + row * 18));
            }
        }
        int hotbarY = PLAYER_INV_START_Y + 58;
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            addSlot(new Slot(inventory, col, 108 + col * 18, hotbarY));
        }
    }

    // --- API für Pakete ---

    public void setSelectedOffer(UUID offerId) {
        ServerShopOffer offer = ServerShopManager.get().findOffer(offerId);
        setCurrentTradingOffer(offer);
    }

    public void setCurrentTradingOffer(ServerShopOffer offer) {
        this.currentTradingOffer = offer;
        // Bei Angebotswechsel leeren wir zur Sicherheit den Output-Slot, damit kein "Geister-Item" drin bleibt
        if (!canEdit && !tradeContainer.getItem(RESULT_SLOT).isEmpty()) {
            tradeContainer.setItem(RESULT_SLOT, ItemStack.EMPTY);
        }
        slotsChanged(tradeContainer);
    }

    @Override
    public void slotsChanged(Container container) {
        if (canEdit) return;

        ItemStack pay1 = container.getItem(0);
        ItemStack pay2 = container.getItem(1);

        if (currentTradingOffer == null) {
            if (!container.getItem(RESULT_SLOT).isEmpty()) {
                container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            }
            return;
        }

        boolean valid = true;
        if (!currentTradingOffer.payments().isEmpty()) {
            ItemStack required1 = currentTradingOffer.payments().get(0);
            if (!required1.isEmpty()) {
                if (pay1.isEmpty() || !ItemStack.isSameItemSameComponents(pay1, required1) || pay1.getCount() < required1.getCount()) {
                    valid = false;
                }
            }
        }

        if (valid && currentTradingOffer.payments().size() > 1) {
            ItemStack required2 = currentTradingOffer.payments().get(1);
            if (!required2.isEmpty()) {
                if (pay2.isEmpty() || !ItemStack.isSameItemSameComponents(pay2, required2) || pay2.getCount() < required2.getCount()) {
                    valid = false;
                }
            }
        }

        ItemStack currentResult = container.getItem(RESULT_SLOT);
        if (valid) {
            if (currentResult.isEmpty()) {
                container.setItem(RESULT_SLOT, currentTradingOffer.result().copy());
            }
        } else {
            if (!currentResult.isEmpty()) {
                container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            }
        }
    }

    public void autoFillPayment(Player player, ServerShopOffer offer) {
        if (offer == null) return;

        // Zuerst alte Items raus, damit Platz ist und nichts gemischt wird
        if (!canEdit) {
            clearTemplate(player);
        }

        if (!offer.payments().isEmpty()) {
            transferItemsToSlot(player, offer.payments().get(0), 0);
        }
        if (offer.payments().size() > 1) {
            transferItemsToSlot(player, offer.payments().get(1), 1);
        }
    }

    private void transferItemsToSlot(Player player, ItemStack required, int slotIndex) {
        if (required.isEmpty()) return;
        Inventory inv = player.getInventory();
        ItemStack currentInSlot = tradeContainer.getItem(slotIndex);
        int needed = required.getCount() - currentInSlot.getCount();

        if (needed <= 0) return;

        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack invStack = inv.items.get(i);
            if (ItemStack.isSameItemSameComponents(invStack, required)) {
                int toMove = Math.min(needed, invStack.getCount());

                ItemStack newStack = currentInSlot.isEmpty() ? invStack.copy() : currentInSlot.copy();
                newStack.setCount(newStack.getCount() + toMove);
                tradeContainer.setItem(slotIndex, newStack);

                inv.removeItem(i, toMove);
                needed -= toMove;
                currentInSlot = tradeContainer.getItem(slotIndex);
                if (needed <= 0) break;
            }
        }
    }

    public void clearTemplate(Player player) {
        for (int i = 0; i < tradeContainer.getContainerSize(); i++) {
            if (!canEdit && i == RESULT_SLOT) {
                tradeContainer.setItem(i, ItemStack.EMPTY);
                continue;
            }
            ItemStack stack = tradeContainer.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            clearTemplate(player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack newStack = slot.getItem();
        ItemStack originalStack = newStack.copy();

        if (index == RESULT_SLOT) {
            if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(newStack, originalStack);
        } else if (index >= TEMPLATE_SLOTS) {
            if (!this.moveItemStackTo(newStack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
            return ItemStack.EMPTY;
        }

        if (newStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (newStack.getCount() == originalStack.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, newStack);
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) { return true; }
    public boolean isEditor() { return canEdit; }
    public int selectedPage() { return selectedPage.get(); }
    public void setSelectedPageServer(int page) { selectedPage.set(page); broadcastChanges(); }
    public void setSelectedPageClient(int page) { selectedPage.set(page); }
    public Container templateContainer() { return tradeContainer; }
    public ItemStack getTemplateStack(int slot) { return tradeContainer.getItem(slot); }

    private class ServerShopResultSlot extends Slot {
        public ServerShopResultSlot(Player player, Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return canEdit; }
        @Override public void onTake(Player player, ItemStack stack) {
            if (!canEdit) {
                this.checkTakeAchievements(stack);
                if (currentTradingOffer != null) {
                    removePayment(0, currentTradingOffer.payments().isEmpty() ? ItemStack.EMPTY : currentTradingOffer.payments().get(0));
                    removePayment(1, currentTradingOffer.payments().size() > 1 ? currentTradingOffer.payments().get(1) : ItemStack.EMPTY);
                }
                slotsChanged(tradeContainer);
            }
            super.onTake(player, stack);
        }
        private void removePayment(int slotIndex, ItemStack required) {
            if (required.isEmpty()) return;
            tradeContainer.removeItem(slotIndex, required.getCount());
        }
    }
}