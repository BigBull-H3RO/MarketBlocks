package de.bigbull.marketblocks.shop.marketplace.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceClientState;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceManager;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceOffer;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceOfferViewState;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Container-Menü für den blocklosen Marktplatz.
 */
public class MarketplaceMenu extends AbstractContainerMenu {
    public static final int TEMPLATE_SLOTS = 3;
    private static final int PAYMENT_SLOT_0 = 0;
    private static final int PAYMENT_SLOT_1 = 1;
    private static final int PAYMENT_SLOTS_END_EXCLUSIVE = PAYMENT_SLOT_1 + 1;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int PLAYER_INV_START_Y = 125;
    private static final int RESULT_SLOT = 2;

    private final Container tradeContainer = new SimpleContainer(TEMPLATE_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            MarketplaceMenu.this.slotsChanged(this);
        }
    };

    private boolean hasEditPermission;
    private boolean globalEditModeEnabled;
    private boolean isEditMode = false;
    private final DataSlot selectedPage;
    private final Inventory inventory;

    private MarketplaceOffer currentTradingOffer;

    public MarketplaceMenu(int containerId, Inventory inventory, boolean canEdit, boolean globalEditModeEnabled) {
        this(RegistriesInit.MARKETPLACE_MENU.get(), containerId, inventory, canEdit, globalEditModeEnabled);
    }

    public MarketplaceMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean(), buf.readBoolean());
    }

    private MarketplaceMenu(MenuType<?> menuType, int containerId, Inventory inventory, boolean canEdit, boolean globalEditModeEnabled) {
        super(menuType, containerId);
        this.inventory = inventory;
        this.hasEditPermission = canEdit;
        this.globalEditModeEnabled = globalEditModeEnabled;
        this.selectedPage = new DataSlot() {
            private int value = 0;
            @Override public int get() { return value; }
            @Override public void set(int newValue) { this.value = Math.max(0, newValue); }
        };
        addDataSlot(this.selectedPage);

        addTemplateSlots();
        addPlayerInventory(inventory);
    }

    private void addTemplateSlots() {
        addSlot(new Slot(tradeContainer, PAYMENT_SLOT_0, 136, 78) {
            @Override public void setChanged() { super.setChanged(); slotsChanged(tradeContainer); }
        });
        addSlot(new Slot(tradeContainer, PAYMENT_SLOT_1, 162, 78) {
            @Override public void setChanged() { super.setChanged(); slotsChanged(tradeContainer); }
        });
        addSlot(new MarketplaceResultSlot(tradeContainer, RESULT_SLOT, 220, 78));
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
        int end = this.slots.size();

        int totalSpace = 0;
        int maxStackSize = Math.min(stack.getMaxStackSize(), 64);

        for (int i = TEMPLATE_SLOTS; i < end; i++) {
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
        if (offerId == null) {
            setCurrentTradingOffer(null);
            return;
        }
        if (!MarketplaceManager.get().isOfferOnPage(offerId, selectedPage())) {
            setCurrentTradingOffer(null);
            return;
        }
        MarketplaceOffer offer = MarketplaceManager.get().findOffer(offerId);
        setCurrentTradingOffer(offer);
    }

    public void setCurrentTradingOffer(MarketplaceOffer offer) {
        this.currentTradingOffer = offer;
        clearResultIfNeeded();
        slotsChanged(tradeContainer);
    }

    public void setEditMode(boolean enable) {
        if (enable && !canUseEditMode()) return;
        this.isEditMode = enable;
        slotsChanged(tradeContainer);
    }

    public void setEditPermissionClient(boolean canEdit, boolean globalEnabled) {
        this.hasEditPermission = canEdit;
        this.globalEditModeEnabled = globalEnabled;
        if (!canUseEditMode() && isEditMode) {
            setEditMode(false);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (isEditMode) return;

        if (currentTradingOffer == null) {
            clearResultIfNeeded();
            return;
        }

        PaymentMatch paymentMatch = resolvePaymentMatch();
        int maxPurchasesByLimit = getCurrentMaxPurchasable();
        ItemStack currentResult = container.getItem(RESULT_SLOT);
        if (paymentMatch != null && paymentMatch.maxPurchases() > 0 && maxPurchasesByLimit > 0) {
            ItemStack expectedResult = currentTradingOffer.result();
            if (currentResult.isEmpty() || !ItemStack.isSameItemSameComponents(currentResult, expectedResult)
                    || currentResult.getCount() != expectedResult.getCount()) {
                container.setItem(RESULT_SLOT, expectedResult.copy());
            }
        } else if (!currentResult.isEmpty()) {
            container.setItem(RESULT_SLOT, ItemStack.EMPTY);
        }
    }

    public void autoFillPayment(Player player, MarketplaceOffer offer) {
        if (offer == null) return;

        if (!isEditMode) {
            clearTemplate(player);
        }

        List<ItemStack> effectivePayments = offer.effectivePayments();
        if (!effectivePayments.isEmpty()) {
            transferItemsToSlot(player, effectivePayments.getFirst(), PAYMENT_SLOT_0);
        }
        if (effectivePayments.size() > 1) {
            transferItemsToSlot(player, effectivePayments.get(1), PAYMENT_SLOT_1);
        }
    }

    private void transferItemsToSlot(Player player, ItemStack required, int slotIndex) {
        if (required.isEmpty()) return;

        Inventory inv = player.getInventory();
        int maxStackSize = required.getMaxStackSize();
        int collected = 0;

        for (int i = 0; i < inv.items.size() && collected < maxStackSize; i++) {
            ItemStack invStack = inv.items.get(i);
            if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(invStack, required)) {
                int toMove = Math.min(maxStackSize - collected, invStack.getCount());
                inv.removeItem(i, toMove);
                collected += toMove;
            }
        }

        if (collected > 0) {
            ItemStack newStack = required.copy();
            newStack.setCount(collected);
            tradeContainer.setItem(slotIndex, newStack);
        }
    }

    private PaymentMatch resolvePaymentMatch() {
        if (currentTradingOffer == null) {
            return null;
        }

        List<ItemStack> requiredPayments = new ArrayList<>();
        for (ItemStack payment : currentTradingOffer.effectivePayments()) {
            if (!payment.isEmpty()) {
                requiredPayments.add(payment);
            }
        }

        ItemStack slot0 = tradeContainer.getItem(PAYMENT_SLOT_0);
        ItemStack slot1 = tradeContainer.getItem(PAYMENT_SLOT_1);

        if (requiredPayments.isEmpty()) {
            return new PaymentMatch(ItemStack.EMPTY, ItemStack.EMPTY, Integer.MAX_VALUE);
        }

        if (requiredPayments.size() == 1) {
            ItemStack required = requiredPayments.getFirst();
            PaymentMatch slot0Match = buildSingleSlotMatch(slot0, required, PAYMENT_SLOT_0);
            PaymentMatch slot1Match = buildSingleSlotMatch(slot1, required, PAYMENT_SLOT_1);
            return selectBetterMatch(slot0Match, slot1Match);
        }

        PaymentMatch directMatch = buildTwoSlotMatch(slot0, slot1, requiredPayments.get(0), requiredPayments.get(1));
        PaymentMatch swappedMatch = buildTwoSlotMatch(slot0, slot1, requiredPayments.get(1), requiredPayments.get(0));
        return selectBetterMatch(directMatch, swappedMatch);
    }

    private PaymentMatch buildSingleSlotMatch(ItemStack slotStack, ItemStack required, int slotIndex) {
        if (required.isEmpty() || slotStack.isEmpty() || !ItemStack.isSameItemSameComponents(slotStack, required)
                || slotStack.getCount() < required.getCount()) {
            return null;
        }
        return slotIndex == PAYMENT_SLOT_0
                ? new PaymentMatch(required, ItemStack.EMPTY, slotStack.getCount() / required.getCount())
                : new PaymentMatch(ItemStack.EMPTY, required, slotStack.getCount() / required.getCount());
    }

    private PaymentMatch buildTwoSlotMatch(ItemStack slot0, ItemStack slot1, ItemStack requiredForSlot0, ItemStack requiredForSlot1) {
        if (requiredForSlot0.isEmpty() || requiredForSlot1.isEmpty()) {
            return null;
        }
        if (slot0.isEmpty() || slot1.isEmpty()) {
            return null;
        }
        if (!ItemStack.isSameItemSameComponents(slot0, requiredForSlot0) || slot0.getCount() < requiredForSlot0.getCount()) {
            return null;
        }
        if (!ItemStack.isSameItemSameComponents(slot1, requiredForSlot1) || slot1.getCount() < requiredForSlot1.getCount()) {
            return null;
        }
        int maxPurchases = Math.min(slot0.getCount() / requiredForSlot0.getCount(), slot1.getCount() / requiredForSlot1.getCount());
        return new PaymentMatch(requiredForSlot0, requiredForSlot1, maxPurchases);
    }

    private PaymentMatch selectBetterMatch(PaymentMatch first, PaymentMatch second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return second.maxPurchases() > first.maxPurchases() ? second : first;
    }

    private void removeMatchedPayments(PaymentMatch match, int amount) {
        if (match == null || amount <= 0) {
            return;
        }
        removePayment(PAYMENT_SLOT_0, match.slot0Payment(), amount);
        removePayment(PAYMENT_SLOT_1, match.slot1Payment(), amount);
    }

    public void clearTemplate(Player player) {
        for (int i = 0; i < tradeContainer.getContainerSize(); i++) {
            if (!isEditMode && i == RESULT_SLOT) {
                tradeContainer.setItem(i, ItemStack.EMPTY);
                continue;
            }
            ItemStack stack = tradeContainer.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                giveItemToPlayer(player, stack);
            }
        }
    }

    private void giveItemToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        player.getInventory().placeItemBackInInventory(stack);
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
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack newStack = slot.getItem();
        ItemStack originalStack = newStack.copy();

        if (index == RESULT_SLOT) {
            if (isEditMode) {
                if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (currentTradingOffer == null) return ItemStack.EMPTY;
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return ItemStack.EMPTY;

                ItemStack resultProto = currentTradingOffer.result();
                PaymentMatch paymentMatch = resolvePaymentMatch();
                if (paymentMatch == null) {
                    return ItemStack.EMPTY;
                }

                int affordable = paymentMatch.maxPurchases();
                int fit = calculateMaxFitInPlayerInventory(resultProto);
                int maxByLimit = MarketplaceManager.get().getMaximumPurchasableNow(currentTradingOffer.id(), serverPlayer.getUUID());
                int amount = Math.min(Math.min(affordable, fit), maxByLimit);
                if (amount <= 0) return ItemStack.EMPTY;

                if (MarketplaceManager.get().processPurchaseTransactionSlotBased(serverPlayer, currentTradingOffer.id(), amount)) {
                    removeMatchedPayments(paymentMatch, amount);

                    int remaining = resultProto.getCount() * amount;
                    int maxStack = resultProto.getMaxStackSize();

                    ItemStack totalBoughtCopy = resultProto.copy();
                    totalBoughtCopy.setCount(remaining);

                    while (remaining > 0) {
                        int chunkSize = Math.min(remaining, maxStack);
                        ItemStack chunk = resultProto.copy();
                        chunk.setCount(chunkSize);

                        if (!this.moveItemStackTo(chunk, TEMPLATE_SLOTS, this.slots.size(), true) && !chunk.isEmpty()) {
                            player.drop(chunk, false);
                        } else if (!chunk.isEmpty()) {
                            player.drop(chunk, false);
                        }
                        remaining -= chunkSize;
                    }

                    slotsChanged(tradeContainer);
                    return totalBoughtCopy;
                }
                return ItemStack.EMPTY;
            }
        } else if (index < TEMPLATE_SLOTS) {
            if (!this.moveItemStackTo(newStack, TEMPLATE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (isEditMode) {
                if (!this.moveItemStackTo(newStack, PAYMENT_SLOT_0, TEMPLATE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(newStack, PAYMENT_SLOT_0, PAYMENT_SLOTS_END_EXCLUSIVE, false)) {
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
    public boolean isGlobalEditModeEnabled() { return globalEditModeEnabled; }
    public boolean canUseEditMode() { return hasEditPermission && globalEditModeEnabled; }
    public boolean isEditor() { return isEditMode; }
    public int selectedPage() { return selectedPage.get(); }
    public void setSelectedPageServer(int page) { selectedPage.set(page); broadcastChanges(); }
    public void setSelectedPageClient(int page) { selectedPage.set(page); }
    public void clampSelectedPage(int pageCount) {
        if (pageCount <= 0) {
            selectedPage.set(0);
            return;
        }
        selectedPage.set(Math.min(selectedPage.get(), pageCount - 1));
    }
    public Container templateContainer() { return tradeContainer; }
    public ItemStack getTemplateStack(int slot) { return tradeContainer.getItem(slot); }
    public MarketplaceOfferViewState currentOfferViewState() { return currentTradingOffer == null ? MarketplaceOfferViewState.empty() : MarketplaceClientState.offerViewState(currentTradingOffer.id()); }

    private int getCurrentMaxPurchasable() {
        if (currentTradingOffer == null) {
            return 0;
        }
        if (inventory.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            return MarketplaceManager.get().getMaximumPurchasableNow(currentTradingOffer.id(), serverPlayer.getUUID());
        }
        return currentOfferViewState().maxPurchasable();
    }

    private void removePayment(int slotIndex, ItemStack required, int amount) {
        if (required.isEmpty() || amount <= 0) return;
        ItemStack inSlot = tradeContainer.getItem(slotIndex);
        if (!inSlot.isEmpty()) {
            int toRemove = Math.min(required.getCount() * amount, inSlot.getCount());
            tradeContainer.removeItem(slotIndex, toRemove);
        }
    }

    private void clearResultIfNeeded() {
        if (!isEditMode && !tradeContainer.getItem(RESULT_SLOT).isEmpty()) {
            tradeContainer.setItem(RESULT_SLOT, ItemStack.EMPTY);
        }
    }

    private record PaymentMatch(ItemStack slot0Payment, ItemStack slot1Payment, int maxPurchases) {
    }

    private class MarketplaceResultSlot extends Slot {
        public MarketplaceResultSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isEditMode;
        }

        @Override
        public boolean mayPickup(Player player) {
            if (isEditMode) return true;
            return currentTradingOffer != null && !this.getItem().isEmpty();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (!isEditMode && currentTradingOffer != null && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                PaymentMatch paymentMatch = resolvePaymentMatch();
                if (paymentMatch == null || MarketplaceManager.get().getMaximumPurchasableNow(currentTradingOffer.id(), serverPlayer.getUUID()) <= 0
                        || !MarketplaceManager.get().processPurchaseTransactionSlotBased(serverPlayer, currentTradingOffer.id(), 1)) {
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                    return;
                }
                removeMatchedPayments(paymentMatch, 1);
                slotsChanged(tradeContainer);
            }
            super.onTake(player, stack);
        }
    }
}

