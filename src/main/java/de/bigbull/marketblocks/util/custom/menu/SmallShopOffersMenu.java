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

import java.util.ArrayList;
import java.util.List;

/**
 * Menü für den Angebots-Modus des SmallShop
 */
public class SmallShopOffersMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler paymentHandler;
    private final IItemHandler offerHandler;

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
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;

        final Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        final ItemStack stackInSlot = slot.getItem();
        ItemStack ret = stackInSlot.copy();

        // === RESULT SLOT (Index 2) - Bulk-Kauf ===
        if (index == 2) {
            if (isOwner()) {
                // Owner: normales Verschieben ins Spielerinventar (kein Kauf)
                if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
                else slot.setChanged();
                slot.onTake(player, stackInSlot);
                return ret;
            }

            // Nicht-Owner: Bulk-Kauf
            return performBulkPurchase(player, slot);
        }

        // === ALLE ANDEREN SLOTS ===
        if (index < PLAYER_INVENTORY_START) {
            // Vom Container (Payment) -> Spieler
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Vom Spieler -> bevorzugt in Payment-Slots (0..2 exklusiv)
            if (!this.moveItemStackTo(stackInSlot, 0, PAYMENT_SLOTS, false)) {
                // Inventar <-> Hotbar fallback (Vanilla)
                if (index < HOTBAR_START) {
                    if (!this.moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_START + 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stackInSlot.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        if (stackInSlot.getCount() == ret.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stackInSlot);
        return ret;
    }

    /**
     * Führt einen Bulk-Kauf durch (Shift+Click auf Result-Slot)
     */
    private ItemStack performBulkPurchase(Player player, Slot offerSlot) {
        if (!hasOffer() || !isOfferAvailable()) {
            return ItemStack.EMPTY;
        }

        final ItemStack resultTemplate = blockEntity.getOfferResult();
        if (resultTemplate.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Berechne maximale Anzahl möglicher Käufe
        final int maxBatches = blockEntity.calculateMaxPurchasable(resultTemplate);
        if (maxBatches <= 0) {
            return ItemStack.EMPTY;
        }

        // Simuliere, wie viele Items ins Spielerinventar passen
        final int batchSize = Math.max(1, resultTemplate.getCount());
        final int maxItemsFromInventorySpace = calculateMaxItemsFromInventorySpace(player, resultTemplate);
        final int maxPossibleBatches = Math.min(maxBatches, maxItemsFromInventorySpace / batchSize);

        if (maxPossibleBatches <= 0) {
            return ItemStack.EMPTY;
        }

        // Führe den Kauf durch - OHNE über den OfferSlot zu gehen
        int totalItemsBought = 0;
        for (int batch = 0; batch < maxPossibleBatches; batch++) {
            if (!blockEntity.canAfford() || !blockEntity.hasResultItemInInput()) {
                break; // Nicht mehr genug Ressourcen
            }

            // Einzelkauf mit Kapazitätsprüfung des Output-Inventars
            if (performSinglePurchase()) {
                totalItemsBought += batchSize;

                // Gib gekaufte Items an Spieler
                ItemStack purchasedItem = resultTemplate.copy();
                purchasedItem.setCount(batchSize);

                if (!this.moveItemStackTo(purchasedItem, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                    // Kein Platz mehr beim Spieler
                    break;
                }
            } else {
                // Kauf nicht möglich (z.\,B. Output voll)
                break;
            }
        }

        // Update den Offer-Slot nach allen Käufen
        blockEntity.updateOfferSlot();

        if (totalItemsBought > 0) {
            ItemStack result = resultTemplate.copy();
            result.setCount(totalItemsBought);
            return result;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Führt einen einzelnen Kauf durch, mit Vorabprüfung der Output-Kapazität.
     */
    private boolean performSinglePurchase() {
        if (!blockEntity.canAfford() || !blockEntity.hasResultItemInInput()) {
            return false;
        }

        // Angebots-Items
        ItemStack offerPayment1 = blockEntity.getOfferPayment1();
        ItemStack offerPayment2 = blockEntity.getOfferPayment2();
        ItemStack offerResult = blockEntity.getOfferResult();

        // Vorab prüfen, ob das Output-Inventar die Bezahlung vollständig aufnehmen kann
        ItemStack toAdd1 = offerPayment1.isEmpty() ? ItemStack.EMPTY : offerPayment1.copy();
        ItemStack toAdd2 = offerPayment2.isEmpty() ? ItemStack.EMPTY : offerPayment2.copy();
        if (!canOutputAccept(toAdd1, toAdd2)) {
            return false; // Output voll → kein Kauf
        }

        // Entferne Bezahlung aus Payment-Slots
        if (!offerPayment1.isEmpty() && !removePaymentFromSlots(offerPayment1, offerPayment1.getCount())) {
            return false;
        }
        if (!offerPayment2.isEmpty() && !removePaymentFromSlots(offerPayment2, offerPayment2.getCount())) {
            return false;
        }

        // Entferne Result-Item aus Input-Inventar
        if (!removeFromInputInventory(offerResult)) {
            return false;
        }

        // Füge Bezahlung zu Output-Inventar hinzu (jetzt garantiert passend)
        if (!addToOutputInventory(toAdd1)) return false;
        if (!addToOutputInventory(toAdd2)) return false;

        blockEntity.setChanged();
        return true;
    }

    /**
     * Entfernt Payment-Items aus den Payment-Slots
     */
    private boolean removePaymentFromSlots(ItemStack required, int amount) {
        int remaining = amount;

        for (int i = 0; i < PAYMENT_SLOTS && remaining > 0; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toTake = Math.min(remaining, stack.getCount());
                ItemStack extracted = paymentHandler.extractItem(i, toTake, false);
                remaining -= extracted.getCount();
            }
        }

        return remaining == 0;
    }

    /**
     * Entfernt Items aus dem Input-Inventar
     */
    private boolean removeFromInputInventory(ItemStack toRemove) {
        int remaining = toRemove.getCount();
        IItemHandler inputHandler = blockEntity.getInputHandler();

        for (int i = 0; i < inputHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                ItemStack extracted = inputHandler.extractItem(i, toTake, false);
                remaining -= extracted.getCount();
            }
        }

        return remaining == 0;
    }

    /**
     * Fügt Items zum Output-Inventar hinzu. Liefert false, wenn nicht alles passt.
     */
    private boolean addToOutputInventory(ItemStack toAdd) {
        if (toAdd.isEmpty()) return true;

        IItemHandler outputHandler = blockEntity.getOutputHandler();
        ItemStack remaining = toAdd.copy();

        for (int i = 0; i < outputHandler.getSlots(); i++) {
            remaining = outputHandler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) {
                return true;
            }
        }
        // Nichts droppen, stattdessen Kauf fehlschlagen lassen
        return false;
    }

    /**
     * Prüft, ob das Output-Inventar beide Payment-Stacks vollständig aufnehmen kann (ohne Änderung am Inventar).
     */
    private boolean canOutputAccept(ItemStack itemA, ItemStack itemB) {
        IItemHandler out = blockEntity.getOutputHandler();
        int slots = out.getSlots();

        // Liste der zu prüfenden Stacks zusammenfassen (max. 2 Typen)
        List<ItemStack> toInsert = new ArrayList<>();
        if (itemA != null && !itemA.isEmpty()) toInsert.add(itemA.copy());
        if (itemB != null && !itemB.isEmpty()) toInsert.add(itemB.copy());

        if (toInsert.isEmpty()) return true;

        // Gleiche Typen zusammenfassen
        if (toInsert.size() == 2 && ItemStack.isSameItemSameComponents(toInsert.get(0), toInsert.get(1))) {
            toInsert.get(0).grow(toInsert.get(1).getCount());
            toInsert.remove(1);
        }

        // Zuerst Merge-Kapazität in bestehenden Stacks abziehen
        int[] remaining = new int[toInsert.size()];
        for (int t = 0; t < toInsert.size(); t++) {
            ItemStack tmpl = toInsert.get(t);
            int need = tmpl.getCount();
            for (int i = 0; i < slots && need > 0; i++) {
                ItemStack curr = out.getStackInSlot(i);
                if (ItemStack.isSameItemSameComponents(curr, tmpl) && !curr.isEmpty()) {
                    int cap = Math.min(curr.getMaxStackSize(), out.getSlotLimit(i)) - curr.getCount();
                    if (cap > 0) {
                        int used = Math.min(need, cap);
                        need -= used;
                    }
                }
            }
            remaining[t] = need;
        }

        // Zähle leere Slots
        int emptySlots = 0;
        for (int i = 0; i < slots; i++) {
            if (out.getStackInSlot(i).isEmpty()) emptySlots++;
        }

        // Erforderliche leere Slots berechnen
        int requiredEmpty = 0;
        for (int t = 0; t < toInsert.size(); t++) {
            int need = remaining[t];
            if (need > 0) {
                int maxStack = Math.min(toInsert.get(t).getMaxStackSize(), 64);
                requiredEmpty += (need + maxStack - 1) / maxStack;
            }
        }

        return requiredEmpty <= emptySlots;
    }

    /**
     * Berechnet, wie viele Items ins Spielerinventar passen
     */
    private int calculateMaxItemsFromInventorySpace(Player player, ItemStack template) {
        int totalSpace = 0;

        // Prüfe alle Spielerinventar-Slots
        for (int i = PLAYER_INVENTORY_START; i < this.slots.size(); i++) {
            Slot invSlot = this.slots.get(i);
            ItemStack currentStack = invSlot.getItem();

            if (currentStack.isEmpty()) {
                totalSpace += template.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(currentStack, template)) {
                totalSpace += template.getMaxStackSize() - currentStack.getCount();
            }
        }

        return totalSpace;
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
            // Bei aktivem Angebot: niemals platzieren
            if (menu.hasOffer()) return false;
            // Angebotserstellung: nur Owner
            return menu.isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            if (menu.isOwner()) return true;
            return menu.hasOffer() && menu.isOfferAvailable();
        }

        @Override
        public ItemStack remove(int amount) {
            if (menu.isOwner() || (menu.hasOffer() && menu.isOfferAvailable())) {
                return super.remove(amount);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            if (menu.hasOffer() && !stack.isEmpty()) {
                return; // blockieren wie ein reiner Result-Slot
            }
            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            // UI frisch halten
            menu.blockEntity.updateOfferSlot();
        }
    }
}