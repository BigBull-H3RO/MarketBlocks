package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopPurchasePacket;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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

    private final Container tradeContainer;
    private final boolean canEdit;
    private final DataSlot selectedPage;
    private UUID selectedOfferId;
    private ServerShopOffer selectedOffer;
    private final Inventory inventory;

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
        this.tradeContainer  = new SimpleContainer(TEMPLATE_SLOTS);
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
        addSlot(new Slot(tradeContainer, 0, 136, 78));
        addSlot(new Slot(tradeContainer, 1, 162, 78));
        // FIX BUG 1: Result Slot Logik angepasst
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

    public void setSelectedOffer(UUID offerId) {
        this.selectedOfferId = offerId;
        this.selectedOffer = ServerShopManager.get().findOffer(offerId);
        if (selectedOffer != null) {
            this.tradeContainer.setItem(0, selectedOffer.payments().get(0));
            this.tradeContainer.setItem(1, selectedOffer.payments().get(1));
            this.tradeContainer.setItem(RESULT_SLOT, selectedOffer.result());
        } else {
            this.tradeContainer.clearContent();
        }
        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack newStack = slot.getItem();
        ItemStack originalStack = newStack.copy();

        if (index == RESULT_SLOT) {
            if (canEdit) {
                // Im Editor-Modus einfach nur Item nehmen (zum Bearbeiten)
                if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Im Kauf-Modus: KAUFEN
                if (selectedOffer == null) return ItemStack.EMPTY;
                if (!player.level().isClientSide) {
                    if (ServerShopManager.get().purchaseOffer((ServerPlayer) player, selectedOfferId, 1)) {
                        // Erfolg
                    }
                }
                return ItemStack.EMPTY;
            }
        } else if (index >= TEMPLATE_SLOTS) {
            if (index < TEMPLATE_SLOTS + 27) {
                if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS + 27, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, TEMPLATE_SLOTS + 27, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
            return ItemStack.EMPTY;
        }

        if (newStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return originalStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Im Editor-Modus Items zurückgeben, sonst löschen
        if (canEdit && !player.level().isClientSide) {
            clearContainer(player, tradeContainer);
        } else {
            tradeContainer.clearContent();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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
        return tradeContainer;
    }

    public void clearTemplate() {
        tradeContainer.clearContent();
    }

    public ItemStack getTemplateStack(int slot) {
        return tradeContainer.getItem(slot);
    }

    private class ServerShopResultSlot extends Slot {
        public ServerShopResultSlot(Player player, Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // FIX BUG 1: Im Editor-Modus darf man Items hier reinlegen!
            return canEdit;
        }

        @Override
        public void onTake(Player pPlayer, ItemStack pStack) {
            // Kauf nur auslösen, wenn NICHT im Editor
            if (!canEdit && !pPlayer.level().isClientSide && selectedOfferId != null) {
                NetworkHandler.sendToServer(new ServerShopPurchasePacket(selectedOfferId, pStack.getCount()));
            }
            super.onTake(pPlayer, pStack);
        }
    }
}