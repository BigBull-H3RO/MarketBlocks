package de.bigbull.marketblocks.shop.singleoffer.menu;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.init.RegistriesInit;
import de.bigbull.marketblocks.shop.log.TransactionLogEntry;
import de.bigbull.marketblocks.shop.singleoffer.SideMode;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unified menu for the Trade Stand. Handles all tabs without reopening the
 * container on tab changes.
 */
public class SingleOfferShopMenu extends AbstractSingleOfferShopMenu implements ShopMenu {
    private final SingleOfferShopBlockEntity blockEntity;
    private final Player player;

    private final IItemHandler paymentHandler;
    private final IItemHandler offerHandler;
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;

    private final ContainerData flags;
    private final ContainerData tabData = new SimpleContainerData(1);

    // Side mode handling for settings tab
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, SideMode> initialModes = new EnumMap<>(Direction.class);
    private List<TransactionLogEntry> transactionLogEntries = List.of();

    private static final int PAYMENT_SLOTS = 2;
    private static final int OFFER_SLOTS = 1;
    private static final int INPUT_SLOTS = 12;
    private static final int OUTPUT_SLOTS = 12;
    private static final int TOTAL_SLOTS = PAYMENT_SLOTS + OFFER_SLOTS + INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int OFFER_SLOT_INDEX = PAYMENT_SLOTS; // after payment slots
    private static final Direction[] DIRECTIONS = Direction.values();

    private static SingleOfferShopBlockEntity createClientFallbackBlockEntity() {
        return new SingleOfferShopBlockEntity(BlockPos.ZERO, RegistriesInit.TRADE_STAND_BLOCK.get().defaultBlockState());
    }

    // Server ctor
    public SingleOfferShopMenu(int containerId, Inventory inv, SingleOfferShopBlockEntity be) {
        super(RegistriesInit.SINGLE_OFFER_SHOP_MENU.get(), containerId);
        this.blockEntity = be;
        this.player = inv.player;
        this.paymentHandler = be.getPaymentHandler();
        this.offerHandler = be.getOfferHandler();
        this.inputHandler = be.getInputHandler();
        this.outputHandler = be.getOutputHandler();
        this.flags = be.createMenuFlags(player);

        for (Direction dir : DIRECTIONS) {
            SideMode mode = be.getMode(dir);
            sideModes.put(dir, mode);
            initialModes.put(dir, mode);
        }

        addDataSlots(this.flags);
        addDataSlots(this.tabData);
        blockEntity.ensureOwner(player);
        initSlots(inv);
    }

    // Client ctor
    public SingleOfferShopMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SINGLE_OFFER_SHOP_MENU.get(), containerId);
        SingleOfferShopBlockEntity be = readBlockEntity(inv, buf);
        if (be == null) {
            inv.player.closeContainer();
            be = createClientFallbackBlockEntity();
        }
        this.blockEntity = be;
        this.player = inv.player;
        this.paymentHandler = be.getPaymentHandler();
        this.offerHandler = be.getOfferHandler();
        this.inputHandler = be.getInputHandler();
        this.outputHandler = be.getOutputHandler();
        this.flags = be.createMenuFlags(player);

        for (Direction dir : DIRECTIONS) {
            SideMode mode = be.getMode(dir);
            sideModes.put(dir, mode);
            initialModes.put(dir, mode);
        }

        addDataSlots(this.flags);
        addDataSlots(this.tabData);
        be.ensureOwner(player);
        initSlots(inv);
    }

    @Override
    protected void addCustomSlots(Inventory playerInventory) {
        // Payment slots
        addSlot(new GatedSlot(paymentHandler, 0, 36, 52, ShopTab.OFFERS));
        addSlot(new GatedSlot(paymentHandler, 1, 62, 52, ShopTab.OFFERS));

        // Offer slot
        addSlot(new OfferSlot(offerHandler, 0, 120, 52));

        // Input inventory (4x3 grid starting at 8,18)
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OwnerGatedSlot(inputHandler, index++, 8 + col * 18, 18 + row * 18, ShopTab.INVENTORY));
            }
        }

        // Output inventory (4x3 grid starting at 98,18)
        index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OutputGatedSlot(outputHandler, index++, 98 + col * 18, 18 + row * 18, ShopTab.INVENTORY));
            }
        }
    }

    @Override
    protected void addPlayerInventory(Inventory playerInventory, int startY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new PlayerSlot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        int hotbarY = startY + 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new PlayerSlot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    private int calculateMaxFitInPlayerInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int perTradeCount = stack.getCount();
        if (perTradeCount <= 0) {
            return 0;
        }
        int totalCapacity = calculatePlayerTransferCapacity(stack);
        return totalCapacity / perTradeCount;
    }

    private int calculatePlayerTransferCapacity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int totalSpace = 0;
        for (int i = TOTAL_SLOTS; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            int slotLimit = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
            if (slotLimit <= 0) {
                continue;
            }
            if (!slot.hasItem()) {
                totalSpace += slotLimit;
                continue;
            }
            ItemStack invStack = slot.getItem();
            if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(invStack, stack)) {
                int freeSpace = slotLimit - invStack.getCount();
                if (freeSpace > 0) {
                    totalSpace += freeSpace;
                }
            }
        }
        return totalSpace;
    }

    // --- Tab API ---
    public ShopTab getActiveTab() {
        return ShopTab.fromId(tabData.get(0));
    }

    public boolean isTab(ShopTab tab) {
        return getActiveTab() == tab;
    }

    /** Sets the active tab on the server side and broadcasts changes. */
    public void setActiveTabServer(ShopTab tab) {
        tabData.set(0, sanitizeRequestedTab(tab).ordinal());
        broadcastChanges();
    }

    /** Sets the active tab on the client side without notifying the server. */
    public void setActiveTabClient(ShopTab tab) {
        tabData.set(0, sanitizeRequestedTab(tab).ordinal());
    }

    public boolean canUseTab(ShopTab tab) {
        return tab != null && sanitizeRequestedTab(tab) == tab;
    }

    private ShopTab sanitizeRequestedTab(ShopTab tab) {
        if (tab == null) {
            return ShopTab.OFFERS;
        }
        return switch (tab) {
            case OFFERS -> ShopTab.OFFERS;
            case INVENTORY -> canUseInventoryTab() ? ShopTab.INVENTORY : ShopTab.OFFERS;
            case SETTINGS -> canUseSettingsTab() ? ShopTab.SETTINGS : ShopTab.OFFERS;
            case LOG -> canUseLogTab() ? ShopTab.LOG : ShopTab.OFFERS;
        };
    }

    private boolean canUseInventoryTab() {
        return isOwner() && !blockEntity.isAdminShopEnabled();
    }

    private boolean canUseSettingsTab() {
        return isOwner() || (isOperator() && isGlobalAdminModeEnabled());
    }

    private boolean canUseLogTab() {
        return isOwner();
    }

    // --- Settings helpers ---
    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode) {
        sideModes.put(dir, mode);
    }

    public void resetModes() {
        sideModes.clear();
        sideModes.putAll(initialModes);
    }

    public Map<UUID, String> getAdditionalOwners() {
        return blockEntity.getAdditionalOwners();
    }

    public List<TransactionLogEntry> getTransactionLogEntries() {
        return transactionLogEntries;
    }

    public void setTransactionLogEntries(List<TransactionLogEntry> entries) {
        this.transactionLogEntries = entries == null ? List.of() : List.copyOf(entries);
    }

    /**
     * Füllt die Zahlungsslots mit den benötigten Items aus dem Spielerinventar.
     */
    public void fillPaymentSlots(ItemStack... required) {
        clearPaymentSlots();
        for (int i = 0; i < PAYMENT_SLOTS && i < required.length; i++) {
            ItemStack req = required[i];
            if (req.isEmpty()) {
                continue;
            }
            transferRequiredItems(req, i);
        }
    }

    private void clearPaymentSlots() {
        for (int i = 0; i < PAYMENT_SLOTS; i++) {
            Slot s = this.slots.get(i);
            ItemStack stack = s.getItem();
            if (!stack.isEmpty() && this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                s.set(stack); // Stack is already EMPTY if fully moved, no need for ternary
                s.setChanged();
            }
        }
    }


    /**
     * Transfers required items from player inventory to payment slot.
     * OPTIMIZATION: Early exit when slot is full to avoid unnecessary iteration.
     * Caches slot references to avoid repeated list lookups.
     */
    private void transferRequiredItems(ItemStack required, int slotIndex) {
        if (required == null || required.isEmpty()) return;

        Slot targetSlot = this.slots.get(slotIndex);
        ItemStack cur = targetSlot.getItem();
        int maxTargetStack = Math.min(required.getMaxStackSize(), targetSlot.getMaxStackSize());

        // Early exit if target slot is already full
        if (!cur.isEmpty() && cur.getCount() >= maxTargetStack) {
            return;
        }

        for (int i = TOTAL_SLOTS; i < this.slots.size(); i++) {
            Slot sourceSlot = this.slots.get(i);
            ItemStack invStack = sourceSlot.getItem();

            if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(invStack, required)) {
                if (cur.isEmpty() || ItemStack.isSameItemSameComponents(invStack, cur)) {
                    int space = maxTargetStack - cur.getCount();
                    if (space <= 0) break; // Slot is full

                    int move = Math.min(space, invStack.getCount());
                    if (move > 0) {
                        ItemStack newStack = invStack.copy();
                        newStack.setCount(cur.getCount() + move);
                        invStack.shrink(move);
                        sourceSlot.set(invStack); // Already EMPTY if fully moved
                        targetSlot.set(newStack);
                        cur = newStack; // Update reference for next iteration

                        if (cur.getCount() >= maxTargetStack) {
                            break; // Slot is now full
                        }
                    }
                }
            }
        }
    }

    // --- Quick move / shift click ---
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isTab(ShopTab.SETTINGS) || isTab(ShopTab.LOG)) return ItemStack.EMPTY;
        if (isTab(ShopTab.INVENTORY) && blockEntity.isAdminShopEnabled()) return ItemStack.EMPTY;

        if (index == OFFER_SLOT_INDEX && isTab(ShopTab.OFFERS)) {
            Slot slot = this.slots.get(index);

            if (!blockEntity.hasOffer()) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                // SAFETY: Only owner can remove items from offer slot in template mode
                if (!isOwner()) {
                    return ItemStack.EMPTY;
                }

                ItemStack ret = stack.copy();
                if (!this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if (stack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                blockEntity.updateOfferSlot();
                return ret;
            }

            boolean adminShop = blockEntity.isAdminShopEnabled();
            if (!adminShop && !blockEntity.hasResultItemInInput(false)) {
                if (!player.level().isClientSide) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.out_of_stock"));
                }
                return ItemStack.EMPTY;
            }

            if (!adminShop && blockEntity.isOutputSpaceMissing()) {
                if (!player.level().isClientSide) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.output_full"));
                }
                return ItemStack.EMPTY;
            }

            // NEW: Efficient bulk purchase with atomicity guarantee
            ItemStack offerResult = blockEntity.getOfferResult();
            if (offerResult.isEmpty()) return ItemStack.EMPTY;

            // 1. Calculate how many transactions fit in player inventory
            int maxPlayerFit = calculateMaxFitInPlayerInventory(offerResult);
            if (maxPlayerFit <= 0) return ItemStack.EMPTY;

            // 2. Execute bulk purchase on the server block entity
            // The BlockEntity handles payment reduction, stock reduction, and output space check.
            // It returns the actual number of transactions performed.
            int bought = blockEntity.processBulkPurchase(maxPlayerFit, player, true);

            if (bought > 0) {
                // 3. Give items to player (capacity was precomputed from exact slot limits)
                long remaining = (long) offerResult.getCount() * bought;
                if (remaining <= 0L) {
                    return ItemStack.EMPTY;
                }
                int maxStack = offerResult.getMaxStackSize();

                ItemStack totalBoughtCopy = offerResult.copy();
                totalBoughtCopy.setCount((int) Math.min(Integer.MAX_VALUE, remaining));

                while (remaining > 0L) {
                    int chunkSize = (int) Math.min(remaining, maxStack);
                    ItemStack chunk = offerResult.copy();
                    chunk.setCount(chunkSize);

                    if (!this.moveItemStackTo(chunk, TOTAL_SLOTS, this.slots.size(), true)) {
                        // This should not happen due to pre-check, but failsafe: drop items
                        if (!chunk.isEmpty()) {
                            player.drop(chunk, false);
                            MarketBlocks.LOGGER.warn("Had to drop items during bulk purchase - pre-check failed for player {}", player.getName().getString());
                        }
                    } else {
                         // Double check if moveItemStackTo left something in chunk
                         if (!chunk.isEmpty()) {
                              player.drop(chunk, false);
                          }
                    }
                    remaining -= (long) chunkSize;
                }

                blockEntity.updateOfferSlot();
                return totalBoughtCopy; // Return full amount for UI/Sound
            } else {
                 return ItemStack.EMPTY;
            }
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack ret = stack.copy();

        if (index < TOTAL_SLOTS) {
            if (!this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (isTab(ShopTab.OFFERS)) {
                if (!blockEntity.hasOffer()) {
                    this.moveItemStackTo(stack, 0, PAYMENT_SLOTS, false);
                    if (!stack.isEmpty()) {
                        Slot offerSlot = this.slots.get(OFFER_SLOT_INDEX);
                        if (offerSlot.getItem().isEmpty()) {
                            this.moveItemStackTo(stack, OFFER_SLOT_INDEX, OFFER_SLOT_INDEX + 1, false);
                        }
                    }
                    if (!stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stack, 0, PAYMENT_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (isTab(ShopTab.INVENTORY) && isOwner()) {
                int start = PAYMENT_SLOTS + OFFER_SLOTS;
                int end = start + INPUT_SLOTS;
                if (!this.moveItemStackTo(stack, start, end, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == ret.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return ret;
    }

    /**
     * Override clicked to handle special cases for double-click collection (PICKUP_ALL).
     * PICKUP_ALL is blocked for:
     * 1. Payment slots (0-1): Prevents accidental clearing while player is setting up an offer.
     *    During offer creation, the player places items in payment slots, and double-clicking
     *    would collect them all back, which is usually not intended.
     * 2. Offer slot in template mode: When no offer exists, the offer slot is used to preview
     *    what item will be sold. Double-clicking shouldn't collect this preview item.
     * This improves UX by preventing accidental disruption of the offer creation workflow.
     */
    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        boolean trackBuyerContext = !player.level().isClientSide
                && slotId == OFFER_SLOT_INDEX
                && type == ClickType.PICKUP
                && blockEntity.hasOffer();
        if (trackBuyerContext) {
            blockEntity.beginPurchaseContext(player);
        }

        try {
        if (!player.level().isClientSide
                && slotId == OFFER_SLOT_INDEX
                && type == ClickType.PICKUP
                && blockEntity.hasOffer()
                && blockEntity.getOfferHandler().getStackInSlot(0).isEmpty()) {
            boolean adminShop = blockEntity.isAdminShopEnabled();
            if (!adminShop && !blockEntity.hasResultItemInInput(false)) {
                player.sendSystemMessage(Component.translatable("gui.marketblocks.out_of_stock"));
                return;
            }
            if (!adminShop && blockEntity.isOutputSpaceMissing()) {
                player.sendSystemMessage(Component.translatable("gui.marketblocks.output_full"));
                return;
            }
        }

        if (type == ClickType.PICKUP_ALL) {
            if (slotId >= 0 && slotId < PAYMENT_SLOTS) {
                return; // Block double-click collect for payment slots
            }
            if (slotId == OFFER_SLOT_INDEX && !blockEntity.hasOffer()) {
                return; // Block double-click collect for offer preview slot
            }
        }
        super.clicked(slotId, button, type, player);
        } finally {
            if (trackBuyerContext) {
                blockEntity.clearPurchaseContext();
            }
        }
    }

    // --- ShopMenu impl ---
    @Override
    public SingleOfferShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getFlags() {
        return flags.get(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    // --- Slot wrappers ---
    private abstract class BaseSlot extends SlotItemHandler {
        private final ShopTab tab;
        BaseSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y);
            this.tab = tab;
        }

        @Override
        public boolean isActive() {
            return isTab(tab);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (this.getItemHandler() instanceof IItemHandlerModifiable mod) {
                mod.setStackInSlot(this.getSlotIndex(), this.getItem());
            }
        }
    }

    private class GatedSlot extends BaseSlot {
        GatedSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y, tab);
        }
    }

    private class OwnerGatedSlot extends BaseSlot {
        OwnerGatedSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y, tab);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            return isOwner();
        }
    }

    private class OfferSlot extends BaseSlot {
        OfferSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y, ShopTab.OFFERS);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Angebot existiert -> nie überschreiben
            if (blockEntity.hasOffer()) return false;
            // Template-Modus: nur Owner dürfen vorlegen
            return isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            if (player.level().isClientSide) {
                if (!blockEntity.hasOffer()) {
                    return isOwner();
                }
                return true;
            }

            // Server-side: Full validation with messages
            if (!blockEntity.hasOffer()) {
                // Template-Modus: Owner dürfen falsch gelegte Items wieder rausnehmen
                return isOwner();
            }

            boolean adminShop = blockEntity.isAdminShopEnabled();
            if (!adminShop && !blockEntity.hasResultItemInInput(false)) {
                player.sendSystemMessage(Component.translatable("gui.marketblocks.out_of_stock"));
                return false;
            }

            if (!adminShop && blockEntity.isOutputSpaceMissing()) {
                player.sendSystemMessage(Component.translatable("gui.marketblocks.output_full"));
                return false;
            }

            // Angebots-Modus: Kauf erlaubt, wenn verfügbar (Owner darf auch kaufen)
            return blockEntity.isOfferAvailable();
        }

        @Override
        public ItemStack remove(int amount) {
            if (!blockEntity.hasOffer()) {
                // Template-Modus: normal entfernen (Owner-Check übernimmt mayPickup)
                return super.remove(amount);
            }
            // Angebots-Modus: nur wenn verfügbar
            if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) {
                return super.remove(amount);
            }
            return blockEntity.isOfferAvailable() ? super.remove(amount) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            // Bestehendes Angebot nie per Set überschreiben
            if (blockEntity.hasOffer()) return;
            // Template-Modus: nur Owner dürfen setzen
            if (!isOwner()) return;
            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            blockEntity.updateOfferSlot();
        }

    }

    private class OutputGatedSlot extends BaseSlot {
        OutputGatedSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y, tab);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private class PlayerSlot extends Slot {
        PlayerSlot(Inventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isActive() {
            return !isTab(ShopTab.SETTINGS) && !isTab(ShopTab.LOG);
        }
    }
}



