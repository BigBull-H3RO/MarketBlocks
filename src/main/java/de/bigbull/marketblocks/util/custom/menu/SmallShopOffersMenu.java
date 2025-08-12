package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menü für den Angebots-Modus des SmallShop
 */
public class SmallShopOffersMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final Container container;
    private boolean creatingOffer = false;

    // Slot-Indizes für Offers-Modus
    private static final int PAYMENT_SLOTS = 2; // 2 Bezahlslots
    private static final int OFFER_SLOT = 1; // 1 Angebots-Slot
    private static final int PLAYER_INVENTORY_START = PAYMENT_SLOTS + OFFER_SLOT;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;

    // Data Slots für Client-Server Sync
    private final ContainerData data;

    // Constructor für Server
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.container = blockEntity;

        this.data = new SimpleContainerData(6) {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.hasOffer() ? 1 : 0;
                    case 1 -> blockEntity.isOfferAvailable() ? 1 : 0;
                    case 2 -> blockEntity.isOwner(playerInventory.player) ? 1 : 0;
                    case 3 -> blockEntity.getOwnerId() != null ? 1 : 0;
                    case 4 -> 0; // Reserviert für weitere Flags
                    case 5 -> 0; // Reserviert für weitere Flags
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Data wird vom Server gesteuert
            }
        };

        addDataSlots(this.data);

        // Setup der Slots
        setupSlots(playerInventory);

        // Owner setzen falls noch nicht gesetzt
        if (blockEntity.getOwnerId() == null) {
            blockEntity.setOwner(playerInventory.player);
        }
    }

    // Constructor für Client - FIXED VERSION
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);

        // BlockPos aus Buffer lesen
        BlockPos pos = buf.readBlockPos();

        // Versuche BlockEntity aus der Welt zu bekommen
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof SmallShopBlockEntity shopEntity) {
            this.blockEntity = shopEntity;
        } else {
            // Fallback: Dummy BlockEntity erstellen (sollte nicht passieren)
            this.blockEntity = new SmallShopBlockEntity(pos, RegistriesInit.SMALL_SHOP_BLOCK.get().defaultBlockState());
        }

        this.container = this.blockEntity;

        this.data = new SimpleContainerData(6);
        addDataSlots(this.data);

        // Setup der Slots
        setupSlots(playerInventory);
    }

    // Alternative: Statische Factory-Methode für Client-Constructor
    public static SmallShopOffersMenu createClientMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof SmallShopBlockEntity shopEntity) {
            return new SmallShopOffersMenu(containerId, playerInventory, shopEntity);
        }
        // Fallback - erstelle Dummy Entity
        SmallShopBlockEntity dummy = new SmallShopBlockEntity(pos, RegistriesInit.SMALL_SHOP_BLOCK.get().defaultBlockState());
        return new SmallShopOffersMenu(containerId, playerInventory, dummy);
    }

    private void setupSlots(Inventory playerInventory) {
        // Payment Slots (2 Slots) - Slots 0-1
        addSlot(new PaymentSlot(container, SmallShopBlockEntity.PAYMENT_SLOT_1, 44, 35));
        addSlot(new PaymentSlot(container, SmallShopBlockEntity.PAYMENT_SLOT_2, 62, 35));

        // Offer Result Slot mit Menu-Referenz - Slot 2
        addSlot(new OfferSlot(container, SmallShopBlockEntity.OFFER_RESULT_SLOT, 120, 35, this));

        // Spieler Inventar - Slots 3-38
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18,
                        GuiConstants.PLAYER_INV_Y_START + row * 18));
            }
        }

        // Spieler Hotbar - Slots 39-47
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, GuiConstants.HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Von Container zu Spieler
            if (index < PLAYER_INVENTORY_START) {
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Von Spieler zu Container (nur Payment Slots)
            else if (index >= PLAYER_INVENTORY_START) {
                // Versuche in Payment-Slots zu verschieben
                if (!this.moveItemStackTo(itemstack1, 0, PAYMENT_SLOTS, false)) {
                    if (index < HOTBAR_START) {
                        // Von Inventar zu Hotbar
                        if (!this.moveItemStackTo(itemstack1, HOTBAR_START, HOTBAR_START + 9, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        // Von Hotbar zu Inventar
                        if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
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

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    // Getter für UI
    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean hasOffer() {
        return data.get(0) == 1;
    }

    public boolean isOfferAvailable() {
        return data.get(1) == 1;
    }

    public boolean isOwner() {
        return data.get(2) == 1;
    }

    public boolean hasOwner() {
        return data.get(3) == 1;
    }

    public boolean isCreatingOffer() {
        return creatingOffer;
    }

    public void setCreatingOffer(boolean creatingOffer) {
        this.creatingOffer = creatingOffer;
    }

    // Custom Slot Klassen
    public static class PaymentSlot extends Slot {
        public PaymentSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return true; // Payment-Slots akzeptieren alle Items
        }
    }

    public static class OfferSlot extends Slot {
        private final SmallShopOffersMenu menu;

        public OfferSlot(Container container, int slot, int x, int y, SmallShopOffersMenu menu) {
            super(container, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Erlaube Items nur, wenn der Spieler der Besitzer ist und noch kein Angebot existiert
            return menu.isOwner() && !menu.hasOffer();
        }

        @Override
        public boolean mayPickup(Player player) {
            // Owner kann Items entfernen, wenn kein Angebot existiert oder während der Erstellung
            if (menu.isOwner() && (!menu.hasOffer() || menu.isCreatingOffer())) {
                return true;
            }
            // Normale Kauf-Logik: Nur wenn Angebot verfügbar ist
            return menu.hasOffer() && menu.isOfferAvailable();
        }

        @Override
        public ItemStack remove(int amount) {
            // Owner darf Items jederzeit entfernen, solange kein aktives Angebot besteht
            if (!menu.hasOffer() || menu.isCreatingOffer()) {
                return super.remove(amount);
            }

            // Käufer können nur entnehmen, wenn ein Angebot verfügbar ist
            if (menu.hasOffer() && menu.isOfferAvailable()) {
                return super.remove(amount);
            }

            return ItemStack.EMPTY;
        }
    }
}