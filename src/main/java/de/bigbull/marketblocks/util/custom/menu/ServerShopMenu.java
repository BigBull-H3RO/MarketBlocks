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

    private final boolean hasEditPermission;
    private boolean isEditMode = false;
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
        this.hasEditPermission = canEdit;
        // Standardmäßig im View-Mode, auch wenn Permission da ist
        this.isEditMode = false;

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
            @Override public boolean mayPlace(ItemStack stack) { return true; }
        });
        addSlot(new Slot(tradeContainer, 1, 162, 78) {
            @Override public void setChanged() { super.setChanged(); slotsChanged(tradeContainer); }
            @Override public boolean mayPlace(ItemStack stack) { return true; }
        });
        // Result Slot - Im View Mode NIEMALS beschreibbar
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

    private int calculateMaxFitInPlayerInventory(ItemStack stack) {
        // Player inventory slots are from TEMPLATE_SLOTS to end
        int start = TEMPLATE_SLOTS;
        int end = this.slots.size();

        int totalSpace = 0;
        int maxStackSize = Math.min(stack.getMaxStackSize(), 64);

        for (int i = start; i < end; i++) {
            Slot s = this.slots.get(i);
            if (!s.hasItem()) {
                 totalSpace += maxStackSize;
            } else {
                ItemStack invStack = s.getItem();
                if (ItemStack.isSameItemSameComponents(invStack, stack)) {
                    int space = maxStackSize - invStack.getCount();
                    if (space > 0) {
                        totalSpace += space;
                    }
                }
            }
        }

        return totalSpace / stack.getCount();
    }

    public void setSelectedOffer(UUID offerId) {
        ServerShopOffer offer = ServerShopManager.get().findOffer(offerId);
        setCurrentTradingOffer(offer);
    }

    public void setCurrentTradingOffer(ServerShopOffer offer) {
        this.currentTradingOffer = offer;
        // Bei Angebotswechsel leeren wir zur Sicherheit den Output-Slot
        if (!isEditMode && !tradeContainer.getItem(RESULT_SLOT).isEmpty()) {
            tradeContainer.setItem(RESULT_SLOT, ItemStack.EMPTY);
        }
        slotsChanged(tradeContainer);
    }

    public void setEditMode(boolean enable) {
        if (enable && !hasEditPermission) return;
        this.isEditMode = enable;

        // Wenn wir in den View-Mode wechseln, sicherstellen, dass wir Slots updaten
        // Wenn wir in den Edit-Mode wechseln, Slots nicht anfassen (bleiben wie sie sind oder leer)
        slotsChanged(tradeContainer);
    }

    @Override
    public void slotsChanged(Container container) {
        if (isEditMode) return;

        if (currentTradingOffer == null) {
            if (!container.getItem(RESULT_SLOT).isEmpty()) {
                container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            }
            return;
        }

        ItemStack pay1 = container.getItem(0);
        ItemStack pay2 = container.getItem(1);
        boolean valid = true;

        // Prüfe Payment 1
        if (!currentTradingOffer.payments().isEmpty()) {
            ItemStack required1 = currentTradingOffer.payments().get(0);
            if (!required1.isEmpty()) {
                if (pay1.isEmpty() || !ItemStack.isSameItemSameComponents(pay1, required1) || pay1.getCount() < required1.getCount()) {
                    valid = false;
                }
            }
        }

        // Prüfe Payment 2
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
            ItemStack expectedResult = currentTradingOffer.result();
            if (currentResult.isEmpty() || !ItemStack.isSameItemSameComponents(currentResult, expectedResult)
                    || currentResult.getCount() != expectedResult.getCount()) {
                container.setItem(RESULT_SLOT, expectedResult.copy());
            }
        } else {
            if (!currentResult.isEmpty()) {
                container.setItem(RESULT_SLOT, ItemStack.EMPTY);
            }
        }
    }

    public void autoFillPayment(Player player, ServerShopOffer offer) {
        if (offer == null) return;

        // 1. Slots leeren (alte Items zurück ins Inventar)
        if (!isEditMode) {
            clearTemplate(player);
        }

        // 2. Neue Items reinlegen (NUR die benötigte Menge)
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

        // Maximale Stack-Größe für diesen Item-Typ
        int maxStackSize = required.getMaxStackSize();

        int collected = 0;

        // Sammle ALLE passenden Items bis zum Stack-Limit
        for (int i = 0; i < inv.items.size() && collected < maxStackSize; i++) {
            ItemStack invStack = inv.items.get(i);
            if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(invStack, required)) {
                int toMove = Math.min(maxStackSize - collected, invStack.getCount());

                // Entferne aus Inventar
                inv.removeItem(i, toMove);
                collected += toMove;
            }
        }

        // Setze den Slot auf die gesammelte Menge
        if (collected > 0) {
            ItemStack newStack = required.copy();
            newStack.setCount(collected);
            tradeContainer.setItem(slotIndex, newStack);
        }
    }

    public void clearTemplate(Player player) {
        for (int i = 0; i < tradeContainer.getContainerSize(); i++) {
            // Im View Mode lassen wir den Result Slot in Ruhe (wird durch slotsChanged geregelt)
            if (!isEditMode && i == RESULT_SLOT) {
                tradeContainer.setItem(i, ItemStack.EMPTY);
                continue;
            }
            ItemStack stack = tradeContainer.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                // FIX: Sicherstellen, dass das Item zurückgegeben oder gedroppt wird
                giveItemToPlayer(player, stack);
            }
        }
    }

    // Hilfsmethode für sicheres Item-Zurückgeben
    private void giveItemToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        player.getInventory().placeItemBackInInventory(stack);
        // Wenn placeItemBackInInventory nicht alles aufnehmen konnte (Inventar voll),
        // bleibt ein Rest im Stack. Diesen müssen wir manuell droppen.
        if (!stack.isEmpty()) {
            player.drop(stack, false);
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

        // Wenn Klick auf den ERGEBNIS-SLOT (Kauf)
        if (index == RESULT_SLOT) {
            if (isEditMode) {
                // Im Editor-Modus: Einfach ins Inventar legen
                if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Im Kauf-Modus: Optimierte Massenkauf Logik (ohne Loop-Risiko)
                if (currentTradingOffer == null) return ItemStack.EMPTY;
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return ItemStack.EMPTY;

                ItemStack resultProto = currentTradingOffer.result();

                // 1. Calculate Affordable (based on Payment Slots)
                int affordable = Integer.MAX_VALUE;
                ItemStack p1 = tradeContainer.getItem(0);
                ItemStack p2 = tradeContainer.getItem(1);

                if (!currentTradingOffer.payments().isEmpty()) {
                    ItemStack cost1 = currentTradingOffer.payments().get(0);
                    if (!cost1.isEmpty()) {
                        if (p1.isEmpty() || !ItemStack.isSameItemSameComponents(p1, cost1)) affordable = 0;
                        else affordable = Math.min(affordable, p1.getCount() / cost1.getCount());
                    }
                }
                if (currentTradingOffer.payments().size() > 1) {
                    ItemStack cost2 = currentTradingOffer.payments().get(1);
                    if (!cost2.isEmpty()) {
                        if (p2.isEmpty() || !ItemStack.isSameItemSameComponents(p2, cost2)) affordable = 0;
                        else affordable = Math.min(affordable, p2.getCount() / cost2.getCount());
                    }
                }
                if (affordable == Integer.MAX_VALUE) affordable = 0; // Should not happen unless free, handle free?
                if (currentTradingOffer.payments().stream().allMatch(ItemStack::isEmpty)) affordable = 6400; // Cap free items

                // 2. Calculate Fit
                int fit = calculateMaxFitInPlayerInventory(resultProto);

                // 3. Amount to buy
                int amount = Math.min(affordable, fit);
                if (amount <= 0) return ItemStack.EMPTY;

                // 4. Execute Transaction (Limits check & update)
                // This deducts from Limits but NOT payment slots (as they are in Container)
                if (ServerShopManager.get().processPurchaseTransactionSlotBased(serverPlayer, currentTradingOffer.id(), amount)) {
                    // 5. Deduct Payment from Slots
                    if (!currentTradingOffer.payments().isEmpty()) {
                        ItemStack cost1 = currentTradingOffer.payments().get(0);
                        if (!cost1.isEmpty()) {
                            tradeContainer.removeItem(0, cost1.getCount() * amount);
                        }
                    }
                    if (currentTradingOffer.payments().size() > 1) {
                        ItemStack cost2 = currentTradingOffer.payments().get(1);
                        if (!cost2.isEmpty()) {
                            tradeContainer.removeItem(1, cost2.getCount() * amount);
                        }
                    }

                    // 6. Give Items (Chunked)
                    int remaining = resultProto.getCount() * amount;
                    int maxStack = resultProto.getMaxStackSize();

                    ItemStack totalBoughtCopy = resultProto.copy();
                    totalBoughtCopy.setCount(remaining);

                    while (remaining > 0) {
                        int chunkSize = Math.min(remaining, maxStack);
                        ItemStack chunk = resultProto.copy();
                        chunk.setCount(chunkSize);

                        if (!this.moveItemStackTo(chunk, TEMPLATE_SLOTS, this.slots.size(), true)) {
                            // Should not happen
                            if (!chunk.isEmpty()) player.drop(chunk, false);
                        } else {
                            if (!chunk.isEmpty()) player.drop(chunk, false);
                        }
                        remaining -= chunkSize;
                    }

                    slotsChanged(tradeContainer);
                    return totalBoughtCopy;
                }
                return ItemStack.EMPTY;
            }
        }
        // Klick im Template/Bezahl-Bereich -> ins Inventar
        else if (index < TEMPLATE_SLOTS) {
            if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }
        // Klick im Inventar -> In die Bezahl-Slots
        else {
            if (isEditMode) {
                // Im Editor: In alle Slots möglich
                if (!this.moveItemStackTo(newStack, 0, TEMPLATE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Im Kauf-Modus: Nur in Slot 0 und 1 (Bezahlung) - Shift-Klick in Result verboten
                if (!this.moveItemStackTo(newStack, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (newStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (newStack.getCount() == originalStack.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, newStack);
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) { return true; }
    public boolean hasEditPermission() { return hasEditPermission; }
    public boolean isEditor() { return isEditMode; }
    public int selectedPage() { return selectedPage.get(); }
    public void setSelectedPageServer(int page) { selectedPage.set(page); broadcastChanges(); }
    public void setSelectedPageClient(int page) { selectedPage.set(page); }
    public Container templateContainer() { return tradeContainer; }
    public ItemStack getTemplateStack(int slot) { return tradeContainer.getItem(slot); }

    private class ServerShopResultSlot extends Slot {
        private final Player player;

        public ServerShopResultSlot(Player player, Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Im View-Mode darf NIEMAND Items hier reinlegen - nur im Edit Mode
            return isEditMode;
        }

        @Override
        public boolean mayPickup(Player player) {
            if (isEditMode) return true;
            return currentTradingOffer != null && !this.getItem().isEmpty();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (!isEditMode && currentTradingOffer != null) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    // Serverseitig: Transaktion durchführen (Limits + Bezahlung aus Inventar!)
                    // ACHTUNG: Die Bezahlung liegt aber in den SLOTS (tradeContainer), nicht im Spieler-Inventar direkt?
                    // Nein, ServerShop ist "Villager-Style" oder "Click-Buy"?
                    // Spec: "Spieler legen Bezahl-Items in die zwei Bezahl-Slots"
                    // ServerShopManager.purchaseOffer prüft player.getInventory().contains(required).
                    // Das passt NICHT zusammen. ServerShopManager geht davon aus, dass Items im Inventar sind.
                    // ABER: ServerShopMenu legt Items in Slots 0 und 1. Diese sind "fake" Slots oder Container?
                    // tradeContainer ist ein SimpleContainer.
                    // Wenn der Spieler Items in Slot 0/1 legt, sind sie NICHT mehr im Inventar.

                    // KORREKTUR:
                    // ServerShopManager.processPurchaseTransaction prüft Inventar.
                    // Das ist für den "Auto-Buy" via Packet/Button gut.
                    // Aber hier im GUI liegen die Items in den Slots.
                    // Wir müssen also:
                    // 1. Prüfen, ob die Items in den Slots reichen.
                    // 2. Limits prüfen (via Manager Helper?).
                    // 3. Wenn OK: Items aus Slots entfernen, Limit updaten.

                    // Wir können processPurchaseTransaction NICHT direkt nutzen, weil es im Inventar sucht.
                    // Wir müssen eine Variante haben, die die Slots nutzt oder wir machen es hier manuell aber korrekt.

                    // Da wir Limits haben, die im Manager verwaltet werden, müssen wir den Manager nutzen.
                    // Wir erweitern processPurchaseTransaction oder nutzen eine neue Methode für "Slot-Based".

                    // Aber halt: Spec sagt "Spieler legen Bezahl-Items in die zwei Bezahl-Slots".
                    // ServerShopManager.purchaseOffer (und processPurchaseTransaction) zieht Items aus dem INVENTAR ab.
                    // Das ist ein Widerspruch im bestehenden Code vs. Spec/GUI-Design.
                    // Der Packet-Code (ServerShopPurchasePacket) nutzt purchaseOffer. Das würde bedeuten, man braucht Items im Inventar.
                    // Aber das GUI hat Bezahl-Slots.

                    // Entscheidung: Wir implementieren die Logik hier im Menu korrekt für Slots.
                    // Und wir nutzen Manager NUR für Limits.

                    // Limit Check & Update
                    if (!ServerShopManager.get().processPurchaseTransactionSlotBased(serverPlayer, currentTradingOffer.id(), 1)) {
                        // Fehlgeschlagen (z.B. Limit erreicht) -> Aktion abbrechen!
                        // Der Spieler hat das Item auf dem Cursor (clientseitig), aber die Transaktion wurde serverseitig abgelehnt.
                        // Wir dürfen das Item NICHT ins Inventar legen, da es noch nicht bezahlt wurde!

                        // Item vom Cursor entfernen
                        player.containerMenu.setCarried(ItemStack.EMPTY);

                        // Das Item ist damit vernichtet (was korrekt ist, da es aus dem Nichts erzeugt wurde).
                        return;
                    }

                    // Wenn wir hier sind, hat processPurchaseTransactionSlotBased das Limit geupdated.
                    // Jetzt müssen wir die Bezahl-Items aus den Slots entfernen.
                    if (!currentTradingOffer.payments().isEmpty()) {
                        removePayment(0, currentTradingOffer.payments().get(0));
                    }
                    if (currentTradingOffer.payments().size() > 1) {
                        removePayment(1, currentTradingOffer.payments().get(1));
                    }
                    slotsChanged(tradeContainer);
                }
            }
            super.onTake(player, stack);
        }

        private void removePayment(int slotIndex, ItemStack required) {
            if (required.isEmpty()) return;
            ItemStack inSlot = tradeContainer.getItem(slotIndex);
            if (!inSlot.isEmpty()) {
                int toRemove = Math.min(required.getCount(), inSlot.getCount());
                tradeContainer.removeItem(slotIndex, toRemove);
            }
        }
    }
}