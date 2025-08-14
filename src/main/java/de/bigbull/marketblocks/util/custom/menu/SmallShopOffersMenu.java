package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menü für den Angebots-Modus des SmallShop
 */
public class SmallShopOffersMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler paymentHandler;
    private final IItemHandler offerHandler;
    // ENTFERNT: private boolean creatingOffer = false;

    private static final int PAYMENT_SLOTS = 2;
    private static final int OFFER_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = PAYMENT_SLOTS + OFFER_SLOT;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;

    private final ContainerData data;

    // Constructor für Server
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.paymentHandler = blockEntity.getPaymentHandler();
        this.offerHandler = blockEntity.getOfferHandler();
        this.data = SmallShopMenuData.create(blockEntity, playerInventory.player);

        addDataSlots(this.data);
        setupSlots(playerInventory);

        if (!playerInventory.player.level().isClientSide() && blockEntity.getOwnerId() == null) {
            blockEntity.setOwner(playerInventory.player);
        }
    }

    // Constructor für Client
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, MenuUtils.readBlockEntity(playerInventory, buf));
    }

    private void setupSlots(Inventory playerInventory) {
        // Payment Slots
        addSlot(new PaymentSlot(paymentHandler, 0, 36, 52));
        addSlot(new PaymentSlot(paymentHandler, 1, 62, 52));

        // Offer Result Slot
        addSlot(new OfferSlot(offerHandler, 0, 120, 52, this));

        MenuUtils.addPlayerInventory(this::addSlot, playerInventory, GuiConstants.PLAYER_INV_Y_START);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == 2 && !isOwner()) {
            return quickMoveResult(player);
        }
        return QuickMoveHelper.quickMoveStack(this, player, index,
                PLAYER_INVENTORY_START, HOTBAR_START,
                true, 0, PAYMENT_SLOTS,
                this::moveItemStackTo);
    }

    private ItemStack quickMoveResult(Player player) {
        if (!hasOffer()) {
            return ItemStack.EMPTY;
        }

        ItemStack p1 = blockEntity.getOfferPayment1();
        ItemStack p2 = blockEntity.getOfferPayment2();
        if (p1.isEmpty() && !p2.isEmpty()) {
            p1 = p2;
            p2 = ItemStack.EMPTY;
        }

        // Prüfe wie viele Transaktionen möglich sind
        int count1 = countPayment(p1);
        int count2 = p2.isEmpty() ? Integer.MAX_VALUE : countPayment(p2);
        int tradeSize1 = p1.isEmpty() ? 0 : p1.getCount();
        int tradeSize2 = p2.isEmpty() ? 0 : p2.getCount();

        int maxTrades = 0;
        if (tradeSize1 > 0) {
            maxTrades = count1 / tradeSize1;
            if (!p2.isEmpty() && tradeSize2 > 0) {
                maxTrades = Math.min(maxTrades, count2 / tradeSize2);
            }
        }

        if (maxTrades <= 0) {
            return ItemStack.EMPTY;
        }

        // Extrahiere so viele Result-Items wie möglich (aber maximal was verfügbar ist)
        ItemStack resultTemplate = blockEntity.getOfferResult();
        ItemStack totalResult = ItemStack.EMPTY;

        for (int i = 0; i < maxTrades; i++) {
            // Prüfe ob noch genug Input-Items vorhanden sind
            if (!blockEntity.hasResultItemInInput()) {
                break;
            }

            // Prüfe ob noch genug Payment-Items vorhanden sind
            if (!blockEntity.canAfford()) {
                break;
            }

            // Simuliere die Extraktion des Result-Items
            ItemStack extracted = blockEntity.getOfferHandler().extractItem(0, resultTemplate.getCount(), true);
            if (extracted.isEmpty()) {
                break;
            }

            // Führe die echte Extraktion durch (dies löst automatisch processPurchase() aus)
            extracted = blockEntity.getOfferHandler().extractItem(0, resultTemplate.getCount(), false);
            if (extracted.isEmpty()) {
                break;
            }

            if (totalResult.isEmpty()) {
                totalResult = extracted.copy();
            } else {
                totalResult.grow(extracted.getCount());
            }

        }

        // Verschiebe die extrahierten Items ins Spieler-Inventar
        if (!totalResult.isEmpty()) {
            ItemStack remaining = totalResult;
            if (!moveItemStackTo(remaining, PLAYER_INVENTORY_START, slots.size(), true)) {
                // Falls das Inventar voll ist, droppe die Items
                player.drop(remaining, false);
                return ItemStack.EMPTY;
            }
            return totalResult;
        }

        return ItemStack.EMPTY;
    }

    private int countPayment(ItemStack required) {
        if (required.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < PAYMENT_SLOTS; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

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

    // ENTFERNTE METHODEN:
    // public boolean isCreatingOffer()
    // public void setCreatingOffer(boolean creatingOffer)

    public static class PaymentSlot extends SlotItemHandler {
        public PaymentSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return true;
        }
    }

    public static class OfferSlot extends SlotItemHandler {
        private final SmallShopOffersMenu menu;

        public OfferSlot(IItemHandler handler, int slot, int x, int y, SmallShopOffersMenu menu) {
            super(handler, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Owner kann immer Items platzieren (für Angebotserstellung)
            // Nicht-Owner können keine Items platzieren
            return menu.isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            // Owner kann immer Items nehmen
            if (menu.isOwner()) {
                return true;
            }

            // Nicht-Owner können nur kaufen wenn Angebot verfügbar ist
            return menu.hasOffer() && menu.isOfferAvailable();
        }

        @Override
        public ItemStack remove(int amount) {
            // Standard-Verhalten für Owner oder Käufe
            if (menu.isOwner() || (menu.hasOffer() && menu.isOfferAvailable())) {
                return super.remove(amount);
            }

            return ItemStack.EMPTY;
        }
    }
}