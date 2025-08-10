package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Menü für den Angebots-Modus des SmallShop
 */
public class SmallShopOffersMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final Level level;
    private final Container container;

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
        this.level = playerInventory.player.level();
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

    // Constructor für Client
    public SmallShopOffersMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SmallShopBlockEntity(
                playerInventory.player.blockPosition(),
                playerInventory.player.level().getBlockState(playerInventory.player.blockPosition())
        ));
    }

    private void setupSlots(Inventory playerInventory) {
        // Payment Slots (2 Slots) - Slots 0-1
        addSlot(new PaymentSlot(container, 24, 44, 35)); // Slot 24 in BlockEntity = Payment 1
        addSlot(new PaymentSlot(container, 25, 62, 35)); // Slot 25 in BlockEntity = Payment 2

        // Offer Result Slot - Slot 2
        addSlot(new OfferSlot(container, 26, 120, 35)); // Slot 26 in BlockEntity = Offer Result

        // Spieler Inventar - Slots 3-38
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Spieler Hotbar - Slots 39-47
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
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
        public OfferSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; // Offer-Slot akzeptiert keine Items direkt
        }

        @Override
        public ItemStack remove(int amount) {
            return super.remove(amount);
        }
    }
}