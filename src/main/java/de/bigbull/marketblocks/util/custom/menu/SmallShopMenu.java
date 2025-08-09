package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallShopMenu extends AbstractContainerMenu {
    private final Container container;
    private final SmallShopBlockEntity blockEntity;
    private final boolean ownerView;
    private int activeTab = 0;

    public static final int BUTTON_CONFIRM = 0;
    public static final int BUTTON_BUY = 1;
    public static final int BUTTON_REMOVE = 2;
    public static final int BUTTON_TAB_OFFER = 3;
    public static final int BUTTON_TAB_STORAGE = 4;

    // Slot-Indizes
    private static final int PAYMENT_SLOT_A = 25;
    private static final int PAYMENT_SLOT_B = 26;
    private static final int SALE_SLOT = 24;
    private static final int PURCHASE_SLOT = 27; // Neuer Slot für Kaufitems

    public SmallShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, true);
    }

    public SmallShopMenu(int id, Inventory playerInventory, boolean ownerView) {
        this(id, playerInventory, null, ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, SmallShopBlockEntity blockEntity, boolean ownerView) {
        this(id, playerInventory, new SimpleContainer(28), blockEntity, ownerView); // +1 für Purchase slot
    }

    public SmallShopMenu(int id, Inventory playerInventory, Container container, SmallShopBlockEntity blockEntity, boolean ownerView) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), id);
        checkContainerSize(container, 28);
        this.container = container;
        this.blockEntity = blockEntity;
        this.ownerView = ownerView;
        container.startOpen(playerInventory.player);

        setupSlots();
        addPlayerInventorySlots(playerInventory);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        updatePurchaseSlot();
    }

    private void setupSlots() {
        // Zahlungs-Lager (Output) - Slots 0-11
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 4; ++col) {
                int index = col + row * 4;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false; // Nur für erhaltene Zahlungen
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return ownerView && activeTab == 1;
                    }

                    @Override
                    public boolean isActive() {
                        return ownerView && activeTab == 1;
                    }
                });
            }
        }

        // Verkaufs-Lager (Input) - Slots 12-23
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 4; ++col) {
                int index = 12 + col + row * 4;
                int x = 98 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return ownerView && activeTab == 1 &&
                                (blockEntity == null || blockEntity.getSaleItem().isEmpty() ||
                                        ItemStack.isSameItemSameComponents(stack, blockEntity.getSaleItem()));
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return ownerView && activeTab == 1;
                    }

                    @Override
                    public boolean isActive() {
                        return ownerView && activeTab == 1;
                    }
                });
            }
        }

        // Angebots-Template Slots
        int centeredX = (176 - 3 * 18) / 2;

        // Verkaufsitem-Slot (24)
        this.addSlot(new Slot(container, SALE_SLOT, centeredX + 59, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ownerView && activeTab == 0 && !hasActiveOffer();
            }

            @Override
            public boolean mayPickup(Player player) {
                return ownerView && activeTab == 0 && !hasActiveOffer();
            }

            @Override
            public boolean isActive() {
                return activeTab == 0;
            }
        });

        // Bezahlslot A (25)
        this.addSlot(new Slot(container, PAYMENT_SLOT_A, centeredX - 25, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (ownerView && activeTab == 0 && !hasActiveOffer()) {
                    return true; // Template-Erstellung
                }
                return !ownerView && hasActiveOffer(); // Käufer-Bezahlung
            }

            @Override
            public boolean mayPickup(Player player) {
                if (ownerView && activeTab == 0 && !hasActiveOffer()) {
                    return true;
                }
                return !ownerView;
            }

            @Override
            public boolean isActive() {
                return activeTab == 0;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                updatePurchaseSlot();
            }
        });

        // Bezahlslot B (26)
        this.addSlot(new Slot(container, PAYMENT_SLOT_B, centeredX + 1, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (ownerView && activeTab == 0 && !hasActiveOffer()) {
                    return true;
                }
                return !ownerView && hasActiveOffer();
            }

            @Override
            public boolean mayPickup(Player player) {
                if (ownerView && activeTab == 0 && !hasActiveOffer()) {
                    return true;
                }
                return !ownerView;
            }

            @Override
            public boolean isActive() {
                return activeTab == 0;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                updatePurchaseSlot();
            }
        });

        // Kaufslot (27) - Neuer Slot für automatisch platzierte Kaufitems
        this.addSlot(new Slot(container, PURCHASE_SLOT, centeredX + 85, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Nur automatisch befüllt
            }

            @Override
            public boolean mayPickup(Player player) {
                return !ownerView && hasActiveOffer() && hasItem();
            }

            @Override
            public boolean isActive() {
                return !ownerView && activeTab == 0;
            }

            @Override
            public ItemStack remove(int amount) {
                ItemStack result = super.remove(amount);
                if (!result.isEmpty()) {
                    // Nach Entnahme sofort prüfen, ob neues Item platziert werden kann
                    SmallShopMenu.this.updatePurchaseSlot();
                }
                return result;
            }
        });
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        // Spieler-Inventar
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = 84 + row * 18;
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            int x = 8 + col * 18;
            int y = 142;
            this.addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    /**
     * Aktualisiert den Kaufslot basierend auf den Bezahlslots
     */
    private void updatePurchaseSlot() {
        if (ownerView || blockEntity == null || !hasActiveOffer()) {
            container.setItem(PURCHASE_SLOT, ItemStack.EMPTY);
            return;
        }

        // Prüfe, ob genug Bezahlung vorhanden ist
        ItemStack payA = container.getItem(PAYMENT_SLOT_A);
        ItemStack payB = container.getItem(PAYMENT_SLOT_B);
        ItemStack reqPayA = blockEntity.getPayItemA();
        ItemStack reqPayB = blockEntity.getPayItemB();

        boolean hasEnoughPayA = !reqPayA.isEmpty() &&
                ItemStack.isSameItemSameComponents(payA, reqPayA) &&
                payA.getCount() >= reqPayA.getCount();

        boolean hasEnoughPayB = reqPayB.isEmpty() ||
                (ItemStack.isSameItemSameComponents(payB, reqPayB) &&
                        payB.getCount() >= reqPayB.getCount());

        if (hasEnoughPayA && hasEnoughPayB && blockEntity.hasStock()) {
            // Berechne maximale Anzahl möglicher Trades
            int maxTradesA = payA.getCount() / reqPayA.getCount();
            int maxTradesB = reqPayB.isEmpty() ? Integer.MAX_VALUE : payB.getCount() / reqPayB.getCount();
            int maxTrades = Math.min(maxTradesA, maxTradesB);

            // Prüfe verfügbaren Stock
            int availableStock = getAvailableStock();
            int trades = Math.min(maxTrades, availableStock / blockEntity.getSaleItem().getCount());

            if (trades > 0) {
                ItemStack purchaseItem = blockEntity.getSaleItem().copy();
                purchaseItem.setCount(purchaseItem.getCount() * trades);
                container.setItem(PURCHASE_SLOT, purchaseItem);
            } else {
                container.setItem(PURCHASE_SLOT, ItemStack.EMPTY);
            }
        } else {
            container.setItem(PURCHASE_SLOT, ItemStack.EMPTY);
        }
        broadcastChanges();
    }

    /**
     * Berechnet verfügbaren Stock im Verkaufslager
     */
    private int getAvailableStock() {
        if (blockEntity == null) return 0;

        ItemStack saleItem = blockEntity.getSaleItem();
        int totalStock = 0;

        for (int i = 0; i < blockEntity.getStock().getSlots(); i++) {
            ItemStack stack = blockEntity.getStock().getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                totalStock += stack.getCount();
            }
        }
        return totalStock;
    }

    public void setActiveTab(int tab) {
        this.activeTab = tab;
        broadcastChanges();
    }

    public int getActiveTab() {
        return activeTab;
    }

    public boolean hasActiveOffer() {
        return blockEntity != null && !blockEntity.getSaleItem().isEmpty();
    }

    public boolean hasStock() {
        return blockEntity != null && blockEntity.hasStock();
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null) {
            return player.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= 64;
        }
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            // Aus Container in Spielerinventar
            if (index < 28) {
                if (!this.moveItemStackTo(stack, 28, 64, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Aus Spielerinventar in Container
            else {
                // Versuche smart-placement basierend auf Item-Typ
                boolean moved = false;

                if (!ownerView && hasActiveOffer()) {
                    // Käufer: Versuche Bezahlitems in Bezahlslots zu platzieren
                    moved = tryMoveToPaymentSlots(stack);
                }

                if (!moved && ownerView && activeTab == 1) {
                    // Owner im Storage-Tab: In Verkaufslager
                    moved = this.moveItemStackTo(stack, 12, 24, false);
                }

                if (!moved && ownerView && activeTab == 0 && !hasActiveOffer()) {
                    // Owner im Offer-Tab ohne aktives Angebot: In Template-Slots
                    moved = this.moveItemStackTo(stack, 24, 27, false);
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    private boolean tryMoveToPaymentSlots(ItemStack stack) {
        if (blockEntity == null) return false;

        boolean moved = false;

        // Versuche Slot A
        if (ItemStack.isSameItemSameComponents(stack, blockEntity.getPayItemA())) {
            moved |= moveToSpecificSlot(stack, PAYMENT_SLOT_A, blockEntity.getPayItemA());
        }

        // Versuche Slot B
        if (ItemStack.isSameItemSameComponents(stack, blockEntity.getPayItemB())) {
            moved |= moveToSpecificSlot(stack, PAYMENT_SLOT_B, blockEntity.getPayItemB());
        }

        return moved;
    }

    private boolean moveToSpecificSlot(ItemStack from, int slotIndex, ItemStack required) {
        if (required.isEmpty()) return false;

        ItemStack slotStack = container.getItem(slotIndex);
        if (!slotStack.isEmpty() && !ItemStack.isSameItemSameComponents(slotStack, required)) {
            return false;
        }

        int maxStackSize = Math.min(required.getMaxStackSize(), 64);
        int spaceAvailable = maxStackSize - slotStack.getCount();
        int toMove = Math.min(from.getCount(), spaceAvailable);

        if (toMove > 0) {
            if (slotStack.isEmpty()) {
                container.setItem(slotIndex, from.split(toMove));
            } else {
                from.shrink(toMove);
                slotStack.grow(toMove);
            }
            return true;
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        // Spezialbehandlung für Kaufslot
        if (slotId == PURCHASE_SLOT && !ownerView) {
            handlePurchaseSlotClick(player, clickType);
            return;
        }

        super.clicked(slotId, dragType, clickType, player);

        // Nach jedem Klick Purchase-Slot aktualisieren
        if (slotId == PAYMENT_SLOT_A || slotId == PAYMENT_SLOT_B) {
            updatePurchaseSlot();
        }
    }

    private void handlePurchaseSlotClick(Player player, ClickType clickType) {
        if (blockEntity == null || !hasActiveOffer()) return;

        ItemStack purchaseItem = container.getItem(PURCHASE_SLOT);
        if (purchaseItem.isEmpty()) return;

        // Prüfe und konsumiere Bezahlung
        if (consumePayment()) {
            // Konsumiere Stock
            consumeStock(purchaseItem.getCount());

            // Gib Item an Spieler
            if (!player.getInventory().add(purchaseItem.copy())) {
                player.drop(purchaseItem, false);
            }

            // Leere Kaufslot und update
            container.setItem(PURCHASE_SLOT, ItemStack.EMPTY);
            updatePurchaseSlot(); // Prüfe auf weitere mögliche Käufe

            blockEntity.setChanged();
        }
    }

    private boolean consumePayment() {
        if (blockEntity == null) return false;

        ItemStack payA = container.getItem(PAYMENT_SLOT_A);
        ItemStack payB = container.getItem(PAYMENT_SLOT_B);
        ItemStack reqPayA = blockEntity.getPayItemA();
        ItemStack reqPayB = blockEntity.getPayItemB();

        // Validierung
        if (reqPayA.isEmpty() || payA.getCount() < reqPayA.getCount()) {
            return false;
        }

        if (!reqPayB.isEmpty() && payB.getCount() < reqPayB.getCount()) {
            return false;
        }

        // Konsumiere Bezahlung
        payA.shrink(reqPayA.getCount());
        if (payA.isEmpty()) {
            container.setItem(PAYMENT_SLOT_A, ItemStack.EMPTY);
        }

        if (!reqPayB.isEmpty()) {
            payB.shrink(reqPayB.getCount());
            if (payB.isEmpty()) {
                container.setItem(PAYMENT_SLOT_B, ItemStack.EMPTY);
            }
        }

        // Füge Bezahlung zum Owner-Lager hinzu
        addToPaymentStorage(reqPayA.copy());
        if (!reqPayB.isEmpty()) {
            addToPaymentStorage(reqPayB.copy());
        }

        return true;
    }

    private void addToPaymentStorage(ItemStack payment) {
        if (blockEntity == null) return;

        for (int i = 0; i < blockEntity.getPayments().getSlots(); i++) {
            payment = blockEntity.getPayments().insertItem(i, payment, false);
            if (payment.isEmpty()) break;
        }

        // Falls kein Platz im Lager, update Container für Owner
        if (!payment.isEmpty()) {
            for (int i = 0; i < 12; i++) {
                ItemStack existing = container.getItem(i);
                if (existing.isEmpty()) {
                    container.setItem(i, payment);
                    break;
                } else if (ItemStack.isSameItemSameComponents(existing, payment)) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    int toAdd = Math.min(space, payment.getCount());
                    existing.grow(toAdd);
                    payment.shrink(toAdd);
                    if (payment.isEmpty()) break;
                }
            }
        }
    }

    private void consumeStock(int amount) {
        if (blockEntity == null) return;

        ItemStack saleItem = blockEntity.getSaleItem();
        int remaining = amount;

        for (int i = 0; i < blockEntity.getStock().getSlots() && remaining > 0; i++) {
            ItemStack stack = blockEntity.getStock().getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                blockEntity.getStock().setStackInSlot(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                remaining -= toRemove;
            }
        }

        // Update auch Container für Owner-View
        if (ownerView) {
            for (int i = 12; i < 24 && remaining > 0; i++) {
                ItemStack stack = container.getItem(i);
                if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                    int toRemove = Math.min(stack.getCount(), remaining);
                    stack.shrink(toRemove);
                    container.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                    remaining -= toRemove;
                }
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return switch (id) {
            case BUTTON_CONFIRM -> {
                confirmOffer(player);
                yield true;
            }
            case BUTTON_BUY ->
                // Legacy-Support, aber Purchase-Slot wird jetzt automatisch verwaltet
                    true;
            case BUTTON_REMOVE -> {
                removeOffer(player);
                yield true;
            }
            case BUTTON_TAB_OFFER -> {
                setActiveTab(0);
                yield true;
            }
            case BUTTON_TAB_STORAGE -> {
                setActiveTab(1);
                yield true;
            }
            default -> false;
        };
    }

    private void confirmOffer(Player player) {
        if (!ownerView || blockEntity == null) return;

        ItemStack saleItem = container.getItem(SALE_SLOT);
        ItemStack payItemA = container.getItem(PAYMENT_SLOT_A);
        ItemStack payItemB = container.getItem(PAYMENT_SLOT_B);

        if (saleItem.isEmpty() || payItemA.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.marketblocks.small_shop.invalid_offer"));
            return;
        }

        // Speichere Angebot
        blockEntity.setSaleItem(saleItem.copy());
        blockEntity.setPayItemA(payItemA.copy());
        blockEntity.setPayItemB(payItemB.isEmpty() ? ItemStack.EMPTY : payItemB.copy());

        // Gib Template-Items zurück an Spieler
        player.getInventory().add(saleItem);
        player.getInventory().add(payItemA);
        if (!payItemB.isEmpty()) {
            player.getInventory().add(payItemB);
        }

        // Leere Template-Slots
        container.setItem(SALE_SLOT, ItemStack.EMPTY);
        container.setItem(PAYMENT_SLOT_A, ItemStack.EMPTY);
        container.setItem(PAYMENT_SLOT_B, ItemStack.EMPTY);

        blockEntity.setChanged();
        broadcastChanges();
    }

    private void removeOffer(Player player) {
        if (!ownerView || blockEntity == null) return;

        blockEntity.setSaleItem(ItemStack.EMPTY);
        blockEntity.setPayItemA(ItemStack.EMPTY);
        blockEntity.setPayItemB(ItemStack.EMPTY);

        // Leere auch Container-Slots
        container.setItem(SALE_SLOT, ItemStack.EMPTY);
        container.setItem(PAYMENT_SLOT_A, ItemStack.EMPTY);
        container.setItem(PAYMENT_SLOT_B, ItemStack.EMPTY);
        container.setItem(PURCHASE_SLOT, ItemStack.EMPTY);

        blockEntity.setChanged();
        broadcastChanges();
    }

    public ItemStack getSaleItem() {
        return blockEntity != null ? blockEntity.getSaleItem() : ItemStack.EMPTY;
    }

    public ItemStack getPayItemA() {
        return blockEntity != null ? blockEntity.getPayItemA() : ItemStack.EMPTY;
    }

    public ItemStack getPayItemB() {
        return blockEntity != null ? blockEntity.getPayItemB() : ItemStack.EMPTY;
    }

    public boolean isOwnerView() {
        return ownerView;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}