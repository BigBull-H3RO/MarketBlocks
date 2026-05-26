package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.log.ShopTransactionLogSavedData;
import de.bigbull.marketblocks.feature.log.TransactionLogEntry;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.visual.npc.IVisualShopNPC;
import de.bigbull.marketblocks.feature.visual.npc.ShopNpcAnimationState;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcAnimationEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the core logic for the Trade Stand block. This BlockEntity handles
 * everything from inventory management, offer creation, player ownership,
 * and interaction with adjacent blocks.
 */
public class SingleOfferShopBlockEntity extends BlockEntity implements MenuProvider, IVisualShopNPC {
    // Constants
    private static final int MAX_SHOP_NAME_LENGTH = 32;
    private static final double MAX_PLAYER_DISTANCE_SQUARED = 64.0; // 8 blocks
    private static final Direction[] DIRECTIONS = Direction.values();

    // NBT Keys
    private static final String NBT_HAS_OFFER = "HasOffer";
    private static final String NBT_SHOP_NAME = "ShopName";
    private static final String NBT_EMIT_REDSTONE = "EmitRedstone";
    private static final String NBT_SIDE_MODES = "SideModes";
    private static final String NBT_DIRECTION = "Direction";
    private static final String NBT_MODE = "Mode";
    private static final String KEY_PAYMENT1 = "OfferPayment1";
    private static final String KEY_PAYMENT2 = "OfferPayment2";
    private static final String KEY_RESULT = "OfferResult";
    private static final String NBT_ADMIN_SHOP_ENABLED = "AdminShopEnabled";
    private static final String NBT_OUTPUT_WARNING = "OutputWarning";
    private static final String NBT_OUTPUT_FULL = "OutputFull";
    private static final String NBT_VISUALS = "Visuals";
    private static final String NBT_VISUAL_ANIMATION_NONCE = "VisualAnimationNonce";
    private static final String NBT_VISUAL_ANIMATION_EVENT = "VisualAnimationEvent";
    private static final String NBT_VISUAL_PURCHASE_COUNTER = "VisualPurchaseCounter";
    private static final String NBT_VISUAL_PAYMENT_SUCCESS_COUNTER = "VisualPaymentSuccessCounter";
    private static final String NBT_VISUAL_PAYMENT_FAIL_COUNTER = "VisualPaymentFailCounter";
    private static final String NBT_PURCHASE_XP_FEEDBACK_SOUND = "PurchaseXpFeedbackSound";
    private static final String NBT_GLOBAL_OFFER_ITEM_RENDERING = "GlobalOfferItemRendering";

    // Inventory Handler Names
    private static final String HANDLER_INPUT = "InputInventory";
    private static final String HANDLER_OUTPUT = "OutputInventory";
    private static final String HANDLER_PAYMENT = "PaymentSlots";
    private static final String HANDLER_OFFER = "OfferSlot";

    // Menu Flags
    public static final int HAS_OFFER_FLAG = 1;
    public static final int OFFER_AVAILABLE_FLAG = 2;
    public static final int OWNER_FLAG = 4;
    public static final int PRIMARY_OWNER_FLAG = 8;
    public static final int OPERATOR_FLAG = 16;
    public static final int GLOBAL_ADMIN_MODE_FLAG = 32;

    // Offer System
    private ItemStack offerPayment1 = ItemStack.EMPTY;
    private ItemStack offerPayment2 = ItemStack.EMPTY;
    private ItemStack offerResult = ItemStack.EMPTY;
    private boolean hasOffer = false;
    private static final String SHOP_LOG_TYPE = ShopTransactionLogSavedData.SINGLE_OFFER_SHOP_TYPE;
    public static final int MAX_TRANSACTION_LOG_ENTRIES = 100;

    @Nullable
    private UUID purchaseContextBuyerId;
    private String purchaseContextBuyerName = "";

    // Owner System
    private final ShopOwnerManager ownerManager = new ShopOwnerManager(this);
    private final ShopInventoryManager inventoryManager = new ShopInventoryManager(this);

    // Shop Settings
    private final ShopSettingsManager settingsManager = new ShopSettingsManager(this);

    // Side Configuration
    private final ShopSideManager sideManager = new ShopSideManager(this);
    private int visualAnimationNonce = 0;
    private byte visualAnimationEvent = VisualNpcAnimationEvent.NONE;
    private int visualPurchaseCounter = 0;
    private int visualPaymentSuccessCounter = 0;
    private int visualPaymentFailCounter = 0;
    private final ShopNpcAnimationState visualAnimationState = new ShopNpcAnimationState();
    private final ItemStack[] paymentFeedbackSnapshot = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};
    private long lastPurchaseXpSoundTick = -1L;



    // --- CORE HANDLERS ---

    // --- Inventories ---
    private class TrackedItemStackHandler extends ItemStackHandler {
        TrackedItemStackHandler(int size) {
            super(size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            boolean wasAvailable = hasOffer && !offerHandler.getStackInSlot(0).isEmpty();
            updateOfferSlot();
            boolean isAvailable = hasOffer && !offerHandler.getStackInSlot(0).isEmpty();
            if (wasAvailable != isAvailable) {
                sync();
            }
            needsOfferRefresh = true;
        }
    }

    private final ItemStackHandler inputHandler = new TrackedItemStackHandler(12);

    private final ItemStackHandler outputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateOfferSlot(); // Immediate update for UI responsiveness
            needsOfferRefresh = true; // Flag for tick-based checks
            // Note: updateOutputFullness() and sync() are batched in tick()
        }
    };

    private final ItemStackHandler paymentHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            boolean wasAvailable = hasOffer && !offerHandler.getStackInSlot(0).isEmpty();
            updateOfferSlot();
            boolean isAvailable = hasOffer && !offerHandler.getStackInSlot(0).isEmpty();
            if (wasAvailable != isAvailable) {
                sync();
            }
            needsOfferRefresh = true;
            handlePaymentFeedbackChange(slot);
        }
    };

    // Reused snapshot handler to avoid per-call allocations during output-space simulations.
    private final ItemStackHandler outputSimulationHandler = new ItemStackHandler(outputHandler.getSlots());

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            boolean offerActive = hasOffer();

            // Ensure we only allow extraction of the FULL offer amount while an offer is active.
            if (offerActive) {
                ItemStack currentOffer = getOfferResult();
                if (currentOffer.isEmpty()) return ItemStack.EMPTY;

                // Enforce all-or-nothing extraction to prevent partial payment
                if (amount < currentOffer.getCount()) {
                    return ItemStack.EMPTY;
                }
            }

            if (simulate) {
                return super.extractItem(slot, amount, true);
            }

            // Purchase logic only applies if an offer exists
            if (offerActive) {
                if (!isReadyToPurchase()) {
                    updateOfferSlot();
                    return ItemStack.EMPTY;
                }
            }

            ItemStack result = super.extractItem(slot, amount, false);

            if (offerActive && !result.isEmpty()) {
                processPurchase();
            }

            return result;
        }
    };

    private final Map<String, ItemStackHandler> handlerMap = Map.of(
            HANDLER_INPUT, inputHandler,
            HANDLER_OUTPUT, outputHandler,
            HANDLER_PAYMENT, paymentHandler,
            HANDLER_OFFER, offerHandler
    );

    private final IItemHandler inputOnly = new SidedWrapper(inputHandler, false);
    private final IItemHandler outputOnly = new SidedWrapper(outputHandler, true);

    private final OfferManager offerManager = new OfferManager(this);

    private int tickCounter = 0;
    private boolean needsOfferRefresh = false;


    public SingleOfferShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    // --- Sided Wrapper for Item Handlers ---
    /**
     * A wrapper for IItemHandler that restricts insertion or extraction.
     * @param backing The backing item handler.
     * @param extractOnly If true, only extraction is allowed. If false, only insertion is allowed.
     */
    record SidedWrapper(IItemHandler backing, boolean extractOnly) implements IItemHandler {
        @Override
        public int getSlots() {
            return backing.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return backing.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (extractOnly) {
                return stack; // Cannot insert into an extract-only wrapper
            }
            return backing.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!extractOnly) {
                return ItemStack.EMPTY; // Cannot extract from an insert-only wrapper
            }
            return backing.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return backing.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !extractOnly && backing.isItemValid(slot, stack);
        }
    }

    // --- Menu Provider ---
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.marketblocks.trade_stand");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SingleOfferShopMenu(containerId, playerInventory, this);
    }

    // --- Getters for Handlers ---
    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    public ItemStackHandler getPaymentHandler() {
        return paymentHandler;
    }

    public ItemStackHandler getOfferHandler() {
        return offerHandler;
    }

    public OfferManager getOfferManager() {
        return offerManager;
    }

    public IItemHandler getInputOnly() {
        return inputOnly;
    }

    public IItemHandler getOutputOnly() {
        return outputOnly;
    }


    // --- Core BlockEntity Methods ---
    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        }
    }

    public void updateNeighborCache() {
        inventoryManager.updateNeighborCache();
    }

    private boolean isChestIoExtensionEnabled() {
        return Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get();
    }

    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= MAX_PLAYER_DISTANCE_SQUARED;
    }

    // --- Owner System ---
    public void setOwner(Player player) {
        ownerManager.setOwner(player);
    }

    @SuppressWarnings("unused")
    public void addOwner(UUID id, String name) {
        ownerManager.addOwner(id, name);
    }

    @ApiStatus.Internal
    public void addOwnerClient(UUID id, String name) {
        ownerManager.addOwnerClient(id, name);
    }

    @SuppressWarnings("unused")
    public void removeOwner(UUID id) {
        ownerManager.removeOwner(id);
    }

    public Set<UUID> getOwners() {
        return ownerManager.getOwners();
    }

    public Map<UUID, String> getAdditionalOwners() {
        return ownerManager.getAdditionalOwners();
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        ownerManager.setAdditionalOwners(owners);
    }

    public boolean isOwner(Player player) {
        return ownerManager.isOwner(player);
    }

    public boolean isPrimaryOwner(Player player) {
        return ownerManager.isPrimaryOwner(player);
    }

    public void ensureOwner(Player player) {
        ownerManager.ensureOwner(player);
    }

    public UUID getOwnerId() {
        return ownerManager.getOwnerId();
    }

    public String getOwnerName() {
        return ownerManager.getOwnerName();
    }

    public void beginPurchaseContext(@Nullable Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }
        purchaseContextBuyerId = player.getUUID();
        purchaseContextBuyerName = player.getGameProfile().getName();
    }

    public void clearPurchaseContext() {
        purchaseContextBuyerId = null;
        purchaseContextBuyerName = "";
    }

    // --- Shop Settings ---
    public String getShopName() {
        return settingsManager.getShopName();
    }

    public boolean isAdminShopEnabled() {
        return settingsManager.isAdminShopEnabled();
    }

    public boolean isGlobalAdminModeEnabled() {
        return Config.MARKETBLOCKS_ADMIN_MODE_ENABLED.get();
    }

    public boolean isOfferItemRenderingGloballyEnabled() {
        return level != null && level.isClientSide ? settingsManager.isGlobalOfferItemRenderingEnabled() : Config.ENABLE_GLOBAL_OFFER_ITEM_RENDERING.get();
    }

    @ApiStatus.Internal
    public void setOfferItemRenderingGloballyEnabledClient(boolean enabled) {
        settingsManager.setGlobalOfferItemRenderingEnabled(enabled);
    }

    public void setAdminShopEnabled(boolean enabled) {
        if (settingsManager.isAdminShopEnabled() == enabled) {
            return;
        }
        settingsManager.setAdminShopEnabled(enabled);
        needsOfferRefresh = true;
        updateOfferSlot(false);
        sync();
    }

    @ApiStatus.Internal
    public void setAdminShopEnabledClient(boolean enabled) {
        settingsManager.setAdminShopEnabled(enabled);
        needsOfferRefresh = true;
        updateOfferSlot(false);
    }

    // --- Side Configuration ---
    public SideMode getMode(Direction dir) {
        return sideManager.getMode(dir);
    }

    public void setMode(Direction dir, SideMode mode, boolean sync) {
        sideManager.setMode(dir, mode, sync);
    }

    /**
     * Batch update for shop settings to avoid multiple sync() calls.
     * Used by UpdateSettingsPacket to update all settings at once.
     */
    public void updateSettingsBatch(Direction left, SideMode leftMode,
                                    Direction right, SideMode rightMode,
                                    Direction bottom, SideMode bottomMode,
                                    Direction back, SideMode backMode,
                                    String name, boolean redstone,
                                    ShopVisualSettings visuals,
                                    boolean xpFeedbackSound) {
        setMode(left, leftMode, false);
        setMode(right, rightMode, false);
        setMode(bottom, bottomMode, false);
        setMode(back, backMode, false);
        settingsManager.setShopName(name, false);
        settingsManager.setEmitRedstone(redstone, false);
        settingsManager.setVisualSettings(visuals, false);
        settingsManager.setPurchaseXpFeedbackSound(xpFeedbackSound, false);
        updateNeighborCache();
        sync();
    }

    public ShopVisualSettings getVisualSettings() {
        return settingsManager.getVisualSettings();
    }

    public void setVisualSettings(ShopVisualSettings visualSettings, boolean sync) {
        settingsManager.setVisualSettings(visualSettings, sync);
    }

    public void setShopName(String name, boolean sync) {
        settingsManager.setShopName(name, sync);
    }

    public boolean isEmitRedstone() {
        return settingsManager.isEmitRedstone();
    }

    public void setEmitRedstone(boolean emitRedstone, boolean sync) {
        settingsManager.setEmitRedstone(emitRedstone, sync);
    }

    public boolean isPurchaseXpFeedbackSound() {
        return settingsManager.isPurchaseXpFeedbackSound();
    }

    public void setPurchaseXpFeedbackSound(boolean enabled, boolean sync) {
        settingsManager.setPurchaseXpFeedbackSound(enabled, sync);
    }

    @ApiStatus.Internal
    public void triggerNpcAnimationEvent(byte event) {
        this.visualAnimationEvent = event;
        this.visualAnimationNonce++;
    }

    public void invalidateCapabilitiesAndNeighbor(Direction dir) {
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            BlockPos neighbour = worldPosition.relative(dir);
            level.invalidateCapabilities(neighbour);
        }
    }

    public IItemHandler getValidNeighborHandler(Direction dir) {
        return inventoryManager.getValidNeighborHandler(dir);
    }

    // --- Offer System ---
    public void createOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        ItemStack normalizedPayment1 = payment1 == null ? ItemStack.EMPTY : payment1.copy();
        ItemStack normalizedPayment2 = payment2 == null ? ItemStack.EMPTY : payment2.copy();
        if (normalizedPayment1.isEmpty() && !normalizedPayment2.isEmpty()) {
            normalizedPayment1 = normalizedPayment2;
            normalizedPayment2 = ItemStack.EMPTY;
        }

        this.offerPayment1 = normalizedPayment1;
        this.offerPayment2 = normalizedPayment2;
        this.offerResult = result == null ? ItemStack.EMPTY : result.copy();
        this.hasOffer = true;
        sync();
        needsOfferRefresh = true;
    }

    public void clearOffer() {
        offerPayment1 = ItemStack.EMPTY;
        offerPayment2 = ItemStack.EMPTY;
        offerResult = ItemStack.EMPTY;
        this.hasOffer = false;
        this.offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        sync();
        needsOfferRefresh = true;
    }

    public boolean hasOffer() {
        return hasOffer;
    }

    /**
     * Sets the offer status on the client side.
     * Client setters should ONLY update state, no side effects.
     */
    public void setHasOfferClient(boolean hasOffer) {
        this.hasOffer = hasOffer;
    }

    public ItemStack getOfferPayment1() {
        return offerPayment1;
    }

    public ItemStack getOfferPayment2() {
        return offerPayment2;
    }

    public ItemStack getOfferResult() {
        return offerResult;
    }

    /**
     * Counts matching payment items in payment slots.
     *
     * @param target The target ItemStack to match (must not be null)
     * @return Total count of matching items, or 0 if target is null/empty
     */
    private int countMatchingPayment(ItemStack target) {
        if (target.isEmpty()) return 0;

        int total = 0;
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private boolean hasEnoughPayment(ItemStack required) {
        return required.isEmpty() || countMatchingPayment(required) >= required.getCount();
    }

    public void updateOfferSlot() {
        updateOfferSlot(false);
    }

    private void updateOfferSlot(boolean checkNeighbors) {
        if (!hasOffer) {
            return;
        }

        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        boolean stockAvailable = isAdminShopEnabled() || hasResultItemInInput(checkNeighbors);
        boolean outputReady = isAdminShopEnabled() || (!isOutputFull() && hasOutputSpace(p1, p2));
        if (canAfford() && stockAvailable && outputReady) {
            if (offerHandler.getStackInSlot(0).isEmpty()) {
                offerHandler.setStackInSlot(0, getOfferResult().copy());
            }
        } else {
            offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public boolean canAfford() {
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        if (p1.isEmpty() && p2.isEmpty()) {
            return true; // Free trade
        }

        if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
            int required = p1.getCount() + p2.getCount();
            return countMatchingPayment(p1) >= required;
        }

        return hasEnoughPayment(p1) && hasEnoughPayment(p2);
    }

    @SuppressWarnings("unused")
    public boolean hasResultItemInInput() {
        return hasResultItemInInput(false);
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
        ItemStack result = getOfferResult();
        if (result.isEmpty()) return false;
        if (isAdminShopEnabled()) return true;

        return countMatchingInput(result, checkNeighbors) >= result.getCount();
    }

    public int getAnalogSignal(Direction readSide) {
        SideMode mode = getMode(readSide);
        if (mode == SideMode.OUTPUT) {
            return calculateComparatorSignal(outputHandler);
        } else if (mode == SideMode.INPUT) {
            return calculateComparatorSignal(inputHandler);
        }
        return 0;
    }

    private int calculateComparatorSignal(IItemHandler handler) {
        if (handler == null) return 0;

        int totalSlots = handler.getSlots();
        if (totalSlots == 0) return 0;

        float fullness = 0.0F;
        boolean hasItem = false;

        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                fullness += (float) stack.getCount() / (float) Math.min(handler.getSlotLimit(i), stack.getMaxStackSize());
                hasItem = true;
            }
        }

        fullness /= (float) totalSlots;
        return Mth.floor(fullness * 14.0F) + (hasItem ? 1 : 0);
    }

    public void processPurchase() {
        processPurchase(null);
    }

    public void processPurchase(@Nullable Player buyer) {
        processBulkPurchase(1, buyer, false);
    }

    /**
     * Executes the purchase logic for a specified maximum amount of trades.
     * This logic relies on pre-calculating the exact number of possible trades using three constraints:
     * 1) Affordability (based on matching items in the payment slots)
     * 2) In-stock limits (based on the output stack count in the input handler)
     * 3) Output space limits (based on space availability in the output handler)
     * Output space is simulated iteratively to ensure accurate evaluation of max stacking.
     */
    public int processBulkPurchase(int maxAmount) {
        return processBulkPurchase(maxAmount, null, false);
    }

    public int processBulkPurchase(int maxAmount, @Nullable Player buyer) {
        return processBulkPurchase(maxAmount, buyer, false);
    }

    public int processBulkPurchase(int maxAmount, @Nullable Player buyer, boolean shiftPurchase) {
        if (maxAmount <= 0) return 0;
        boolean adminShop = isAdminShopEnabled();

        if (!adminShop && isChestIoExtensionEnabled()) {
            updateNeighborCache();
            inventoryManager.pullFromInputChest(inputHandler);
        }

        if (!hasOffer) return 0;
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        ItemStack result = getOfferResult();
        if (result.isEmpty()) return 0;

        // 1. Calculate how many we can afford
        int affordable = Integer.MAX_VALUE;
        if (!p1.isEmpty()) {
            affordable = Math.min(affordable, countMatchingPayment(p1) / p1.getCount());
        }
        if (!p2.isEmpty()) {
            // Special handling if p1 and p2 are same item type
            if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
                int totalReqPerUnit = p1.getCount() + p2.getCount();
                affordable = countMatchingPayment(p1) / totalReqPerUnit;
            } else {
                affordable = Math.min(affordable, countMatchingPayment(p2) / p2.getCount());
            }
        }
        if (p1.isEmpty() && p2.isEmpty()) affordable = maxAmount; // Free

        // 2. Calculate how many we have in stock (Input)
        int inStock = adminShop ? Integer.MAX_VALUE : countMatchingInput(result, true) / result.getCount();

        // 3. Calculate output space
        int actualAmount = Math.min(maxAmount, Math.min(affordable, inStock));
        if (actualAmount <= 0) return 0;

        // Fast path for single transactions; run iterative simulation only for bulk purchases.
        int validAmount = adminShop
                ? actualAmount
                : (actualAmount == 1
                ? (hasOutputSpace(p1, p2) ? 1 : 0)
                : simulateOutputSpace(p1, p2, actualAmount));
        actualAmount = validAmount;

        if (actualAmount <= 0) {
            if (!adminShop) {
                // Output full
                updateOutputFullness();
            }
            return 0;
        }

        executeTrades(p1, p2, result, actualAmount, adminShop);
        visualPurchaseCounter += actualAmount;

        if (isPurchaseXpFeedbackSound() && level != null) {
            long now = level.getGameTime();
            if (now - lastPurchaseXpSoundTick > 4L) {
                // Pitch increases with purchase amount - sounds satisfying for bulk purchases
                float pitch = Math.min(0.7F + actualAmount * 0.06F, 1.6F);
                level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.BLOCKS, 0.4F, pitch);
                lastPurchaseXpSoundTick = now;
            }
        }

        appendTransactionEntry(resolveBuyerIdentity(buyer), p1, p2, result, actualAmount, shiftPurchase);

        sync();
        triggerRedstonePulse();
        needsOfferRefresh = true;
        return actualAmount;
    }

    private void appendTransactionEntry(@Nullable BuyerIdentity buyer, ItemStack payment1, ItemStack payment2, ItemStack result, int tradeCount, boolean shiftPurchase) {
        if (!(level instanceof ServerLevel serverLevel) || tradeCount <= 0) {
            return;
        }

        List<ItemStack> paidStacks = new ArrayList<>(2);
        ItemStack paidOne = TransactionLogEntry.scaleStack(payment1, tradeCount);
        ItemStack paidTwo = TransactionLogEntry.scaleStack(payment2, tradeCount);
        if (!paidOne.isEmpty()) {
            paidStacks.add(paidOne);
        }
        if (!paidTwo.isEmpty()) {
            paidStacks.add(paidTwo);
        }

        List<ItemStack> boughtStacks = new ArrayList<>(1);
        ItemStack bought = TransactionLogEntry.scaleStack(result, tradeCount);
        if (!bought.isEmpty()) {
            boughtStacks.add(bought);
        }

        UUID buyerId = buyer != null ? buyer.uuid() : new UUID(0L, 0L);
        String buyerName = buyer != null ? buyer.name() : "";

        TransactionLogEntry entry = TransactionLogEntry.now(
                buyerId,
                buyerName,
                paidStacks,
                boughtStacks,
                shiftPurchase ? TransactionLogEntry.PurchaseKind.SHIFT : TransactionLogEntry.PurchaseKind.SINGLE
        );

        ShopTransactionLogSavedData.get(serverLevel).appendEntry(
                SHOP_LOG_TYPE,
                serverLevel.dimension(),
                worldPosition,
                entry,
                MAX_TRANSACTION_LOG_ENTRIES
        );
    }

    private @Nullable BuyerIdentity resolveBuyerIdentity(@Nullable Player directBuyer) {
        if (directBuyer != null) {
            return new BuyerIdentity(directBuyer.getUUID(), directBuyer.getGameProfile().getName());
        }
        if (purchaseContextBuyerId != null) {
            return new BuyerIdentity(purchaseContextBuyerId, purchaseContextBuyerName);
        }
        return null;
    }

    private record BuyerIdentity(UUID uuid, String name) {
    }

    /**
     * Simulates how many transactions can fit in output inventory.
     *
     * @param p1 First payment item (can be empty)
     * @param p2 Second payment item (can be empty)
     * @param maxTransactions Maximum number of transactions to simulate
     * @return Number of transactions that actually fit (may be less than maxTransactions)
     */
    private int simulateOutputSpace(ItemStack p1, ItemStack p2, int maxTransactions) {
        ItemStackHandler testHandler = prepareOutputSimulationHandler();

        int validAmount = 0;
        for (int i = 0; i < maxTransactions; i++) {
            boolean fits = true;

            if (!p1.isEmpty()) {
                if (!ItemHandlerHelper.insertItem(testHandler, p1.copy(), false).isEmpty()) {
                    fits = false;
                }
            }

            if (fits && !p2.isEmpty()) {
                if (!ItemHandlerHelper.insertItem(testHandler, p2.copy(), false).isEmpty()) {
                    fits = false;
                }
            }

            if (fits) {
                validAmount++;
            } else {
                break; // No more space
            }
        }

        return validAmount;
    }

    /**
     * Counts how many items of the target type exist in input inventory and neighbor chests.
     *
     * SAFETY: Creates defensive copies when reading from neighbor inventories to prevent
     * ConcurrentModificationException if neighbor inventory is modified during iteration.
     */
    private int countMatchingInput(ItemStack target, boolean checkNeighbors) {
        if (target.isEmpty()) return 0;

        int found = 0;
        // Check internal inventory
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                found += stack.getCount();
            }
        }
        // Check neighbor inventories (with defensive copies)
        if (checkNeighbors && isChestIoExtensionEnabled() && level != null) {
            for (Direction dir : DIRECTIONS) {
                if (getMode(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour != null) {
                        for (int i = 0; i < neighbour.getSlots(); i++) {
                            ItemStack stack = neighbour.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                // Defensive copy to prevent concurrent modification issues
                                ItemStack safeCopy = stack.copy();
                                if (ItemStack.isSameItemSameComponents(safeCopy, target)) {
                                    found += safeCopy.getCount();
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    private boolean isReadyToPurchase() {
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        boolean stockReady = isAdminShopEnabled() || hasResultItemInInput(false);
        boolean outputReady = isAdminShopEnabled() || (!isOutputFull() && hasOutputSpace(p1, p2));
        return hasOffer && canAfford() && stockReady && outputReady;
    }

    private void executeTrades(ItemStack p1, ItemStack p2, ItemStack result, int tradeCount, boolean adminShop) {
        if (tradeCount <= 0) {
            return;
        }

        ItemStack totalP1 = multiplyStackForTrades(p1, tradeCount);
        ItemStack totalP2 = multiplyStackForTrades(p2, tradeCount);
        ItemStack totalResult = multiplyStackForTrades(result, tradeCount);

        if (!totalP1.isEmpty()) {
            removePayment(totalP1);
        }
        if (!totalP2.isEmpty()) {
            removePayment(totalP2);
        }
        if (!adminShop && !totalResult.isEmpty()) {
            removeFromInput(totalResult);
        }

        if (!adminShop) {
            addToOutputBatched(p1, tradeCount);
            addToOutputBatched(p2, tradeCount);
        }
    }

    private ItemStack multiplyStackForTrades(ItemStack stack, int tradeCount) {
        if (stack == null || stack.isEmpty() || tradeCount <= 0) {
            return ItemStack.EMPTY;
        }
        long total = (long) stack.getCount() * tradeCount;
        if (total <= 0L) {
            return ItemStack.EMPTY;
        }
        ItemStack multiplied = stack.copy();
        multiplied.setCount((int) Math.min(Integer.MAX_VALUE, total));
        return multiplied;
    }

    private void addToOutputBatched(ItemStack stack, int times) {
        if (stack == null || stack.isEmpty() || times <= 0) {
            return;
        }

        long total = (long) stack.getCount() * times;
        if (total <= 0L) {
            return;
        }

        int maxStack = stack.getMaxStackSize();
        while (total > 0L) {
            ItemStack chunk = stack.copy();
            chunk.setCount((int) Math.min(total, maxStack));
            addToOutput(chunk);
            total -= chunk.getCount();
        }
    }

    private void triggerRedstonePulse() {
        if (level == null || level.isClientSide || !isEmitRedstone()) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof BaseShopBlock block) {
            level.setBlock(worldPosition, state.setValue(BaseShopBlock.POWERED, true), 3);
            level.updateNeighborsAt(worldPosition, block);
            level.scheduleTick(worldPosition, block, 20);
        }
    }

    private void removeFromInput(ItemStack toRemove) {
        if (toRemove == null || toRemove.isEmpty()) return;

        int remaining = toRemove.getCount();
        if (remaining <= 0) return;

        // First, remove from internal inventory
        for (int i = 0; i < inputHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                inputHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }

        // Then, remove from connected neighbor inventories
        if (isChestIoExtensionEnabled() && remaining > 0 && level != null && !level.isClientSide) {
            for (Direction dir : DIRECTIONS) {
                if (getMode(dir) != SideMode.INPUT) continue;

                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;

                for (int i = 0; i < neighbour.getSlots() && remaining > 0; i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                        int toTake = Math.min(remaining, stack.getCount());
                        neighbour.extractItem(i, toTake, false);
                        remaining -= toTake;
                    }
                }
                if (remaining <= 0) break;
            }
        }
    }

    private void removePayment(ItemStack required) {
        if (required == null || required.isEmpty()) return;

        int remaining = required.getCount();
        if (remaining <= 0) {
            return;
        }

        // Iterate through both payment slots and remove the required amount
        for (int i = 0; i < paymentHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toTake = Math.min(remaining, stack.getCount());
                paymentHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }
    }

    private boolean hasOutputSpace(ItemStack... stacks) {
        ItemStackHandler testHandler = prepareOutputSimulationHandler();

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            ItemStack remaining = ItemHandlerHelper.insertItem(testHandler, stack.copy(), false);
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private ItemStackHandler prepareOutputSimulationHandler() {
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            outputSimulationHandler.setStackInSlot(i, outputHandler.getStackInSlot(i).copy());
        }
        return outputSimulationHandler;
    }

    private void addToOutput(ItemStack toAdd) {
        ItemHandlerHelper.insertItem(outputHandler, toAdd, false);
    }

    public boolean isOutputAlmostFull() {
        return settingsManager.isOutputAlmostFull();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOutputFull() {
        return settingsManager.isOutputFull();
    }

    public boolean isOutputSpaceMissing() {
        if (isAdminShopEnabled()) {
            return false;
        }
        if (!hasResultItemInInput(false)) {
            return false;
        }
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        return !hasOutputSpace(p1, p2);
    }

    private void updateOutputFullness() {
        if (level == null || level.isClientSide) {
            return;
        }

        int total = 0;
        int filled = 0;

        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemStack stack = outputHandler.getStackInSlot(i);
            int limit = outputHandler.getSlotLimit(i);
            if (!stack.isEmpty()) {
                limit = Math.min(limit, stack.getMaxStackSize());
            }
            total += limit;
            filled += stack.getCount();
        }

        if (isChestIoExtensionEnabled()) {
            for (Direction dir : DIRECTIONS) {
                if (getMode(dir) != SideMode.OUTPUT) continue;
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;
                for (int i = 0; i < neighbour.getSlots(); i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    int limit = neighbour.getSlotLimit(i);
                    if (!stack.isEmpty()) {
                        limit = Math.min(limit, stack.getMaxStackSize());
                    }
                    total += limit;
                    filled += stack.getCount();
                }
            }
        }

        boolean newOutputFull = total > 0 && filled >= total;

        boolean newOutputAlmostFull = false;
        if (Config.ENABLE_OUTPUT_WARNING.get()) {
            int threshold = Config.OUTPUT_WARNING_PERCENT.get();
            newOutputAlmostFull = total > 0 && (filled * 100 >= total * threshold);
        }

        if (newOutputFull != settingsManager.isOutputFull() || newOutputAlmostFull != settingsManager.isOutputAlmostFull()) {
            settingsManager.setOutputFullness(newOutputFull, newOutputAlmostFull);
            sync();
        }
    }

    public boolean isOfferAvailable() {
        return hasOffer && !offerHandler.getStackInSlot(0).isEmpty();
    }

    public ContainerData createMenuFlags(Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int flags = 0;
                    if (hasOffer()) flags |= HAS_OFFER_FLAG;
                    if (isOfferAvailable()) flags |= OFFER_AVAILABLE_FLAG;
                    if (isOwner(player)) flags |= OWNER_FLAG;
                    if (isPrimaryOwner(player)) flags |= PRIMARY_OWNER_FLAG;
                    if (player != null && player.hasPermissions(2)) flags |= OPERATOR_FLAG;
                    if (isGlobalAdminModeEnabled()) flags |= GLOBAL_ADMIN_MODE_FLAG;
                    return flags;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                // Not needed on client side
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    // --- Item and Chest Handling ---
    private void dropItems(Level level, BlockPos pos, ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        dropItems(level, pos, inputHandler);
        dropItems(level, pos, outputHandler);
        dropItems(level, pos, paymentHandler);
        // During an active offer, slot 0 contains only a UI copy of the offer result.
        // The real stock item remains in inputHandler and is dropped above.
        if (!hasOffer) {
            dropItems(level, pos, offerHandler);
        }
    }

    public void unlockAdjacentChests() {
        if (level == null || !isChestIoExtensionEnabled()) return;
        for (Direction dir : DIRECTIONS) {
            invalidateNeighbor(dir);
        }
    }

    public void lockAdjacentChest(Direction side) {
        if (level == null || !isChestIoExtensionEnabled()) return;
        invalidateNeighbor(side);
    }

    private void invalidateNeighbor(Direction dir) {
        BlockPos neighbour = worldPosition.relative(dir);
        level.invalidateCapabilities(neighbour);
    }

    public void lockAdjacentChests() {
        if (level == null || !isChestIoExtensionEnabled()) return;
        for (Direction dir : DIRECTIONS) {
            if (getMode(dir) == SideMode.INPUT || getMode(dir) == SideMode.OUTPUT) {
                lockAdjacentChest(dir);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SingleOfferShopBlockEntity be) {
        if (level.isClientSide) {
            return;
        }

        be.tickCounter++;
        boolean chestExtensionEnabled = Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get();

        int offerInterval = Config.OFFER_UPDATE_INTERVAL.get();
        if (offerInterval > 0 && be.tickCounter % offerInterval == 0) {
            if (chestExtensionEnabled) {
                be.updateNeighborCache();
            }
            if (be.needsOfferRefresh) {
                be.updateOfferSlot(false);
                be.needsOfferRefresh = false;
                be.sync(); // Sync after offer refresh
            }
            be.updateOutputFullness(); // Moved inside interval
        }

        int chestInterval = Config.CHEST_IO_INTERVAL.get();
        if (chestExtensionEnabled && chestInterval > 0 && be.tickCounter % chestInterval == 0) {
            be.updateNeighborCache();
            be.inventoryManager.pullFromInputChest(be.inputHandler);
            be.inventoryManager.pushToOutputChest(be.outputHandler);
        }
    }

    // --- NBT HANDLING ---

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unlockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        unlockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && getBlockState().is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
            TradeStandBlock.ensureTopBlock(level, worldPosition);
        }
        updateNeighborCache();

        // Perform chunk-dependent operations here instead of in loadAdditional()
        // At this point, the level is guaranteed to be set and neighbors are accessible
        lockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadHandlers(tag, registries);
        ownerManager.load(tag);
        loadOffer(tag, registries);
        settingsManager.load(tag);
        sideManager.load(tag);

        this.visualAnimationNonce = tag.getInt(NBT_VISUAL_ANIMATION_NONCE);
        this.visualAnimationEvent = tag.getByte(NBT_VISUAL_ANIMATION_EVENT);
        this.visualPurchaseCounter = tag.getInt(NBT_VISUAL_PURCHASE_COUNTER);
        this.visualPaymentSuccessCounter = tag.getInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER);
        this.visualPaymentFailCounter = tag.getInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER);

        // Moved to onLoad(): lockAdjacentChests() and invalidateCapabilities()
        // This ensures level is not null and neighbor chunks are loaded
        tickCounter = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveHandlers(tag, registries);
        ownerManager.save(tag);
        saveOffer(tag, registries);
        settingsManager.save(tag);
        sideManager.save(tag);

        tag.putInt(NBT_VISUAL_ANIMATION_NONCE, visualAnimationNonce);
        tag.putByte(NBT_VISUAL_ANIMATION_EVENT, visualAnimationEvent);
        tag.putInt(NBT_VISUAL_PURCHASE_COUNTER, visualPurchaseCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, visualPaymentSuccessCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER, visualPaymentFailCounter);
    }

    private void loadHandlers(CompoundTag tag, HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> {
            if (tag.contains(name)) {
                handler.deserializeNBT(registries, tag.getCompound(name));
            }
        });
    }

    private void saveHandlers(CompoundTag tag, HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> tag.put(name, handler.serializeNBT(registries)));
    }

    private void loadOffer(CompoundTag tag, HolderLookup.Provider registries) {
        offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1));
        offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2));
        offerResult = ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT));
        hasOffer = tag.getBoolean(NBT_HAS_OFFER);
    }

    private void saveOffer(CompoundTag tag, HolderLookup.Provider registries) {
        if (!offerPayment1.isEmpty()) tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
        if (!offerPayment2.isEmpty()) tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
        if (!offerResult.isEmpty()) tag.put(KEY_RESULT, offerResult.save(registries));
        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
    }




    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // SECURITY: Only send client-relevant data, not full inventory contents
        CompoundTag tag = new CompoundTag();

        // Offer data (needed for rendering)
        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
        if (hasOffer) {
            if (!offerPayment1.isEmpty()) tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
            if (!offerPayment2.isEmpty()) tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
            if (!offerResult.isEmpty()) tag.put(KEY_RESULT, offerResult.save(registries));
        }

        // Shop settings (needed for UI)
        settingsManager.save(tag);
        tag.putInt(NBT_VISUAL_ANIMATION_NONCE, visualAnimationNonce);
        tag.putByte(NBT_VISUAL_ANIMATION_EVENT, visualAnimationEvent);
        tag.putInt(NBT_VISUAL_PURCHASE_COUNTER, visualPurchaseCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, visualPaymentSuccessCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER, visualPaymentFailCounter);

        // Owner info (needed for permissions check)
        ownerManager.save(tag);

        // Side modes (needed for rendering/capabilities)
        sideManager.save(tag);

        // Note: Inventory handlers (input/output/payment) are NOT sent to clients
        // for security reasons - they contain owner's items

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        // CRITICAL: Only load the data that was sent via getUpdateTag()
        // Do NOT call loadAdditional() as it expects full NBT data!

        // Offer data
        offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1));
        offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2));
        offerResult = ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT));
        hasOffer = tag.getBoolean(NBT_HAS_OFFER);

        // Shop settings
        settingsManager.load(tag);
        visualAnimationNonce = tag.getInt(NBT_VISUAL_ANIMATION_NONCE);
        visualAnimationEvent = tag.getByte(NBT_VISUAL_ANIMATION_EVENT);
        visualPurchaseCounter = tag.getInt(NBT_VISUAL_PURCHASE_COUNTER);
        visualPaymentSuccessCounter = tag.getInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER);
        visualPaymentFailCounter = tag.getInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER);

        // Owner info
        ownerManager.load(tag);

        // Side modes
        sideManager.load(tag);

        // Update client-side state
        updateOfferSlot();
        refreshPaymentFeedbackSnapshot();
    }

    @Override
    public BlockPos getVisualShopPos() {
        return worldPosition;
    }

    @Override
    public Level getVisualLevel() {
        return level;
    }

    @Override
    public Direction getVisualFacing() {
        return getBlockState().hasProperty(BaseShopBlock.FACING)
                ? getBlockState().getValue(BaseShopBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public int getVisualAnimationNonce() {
        return visualAnimationNonce;
    }

    @Override
    public byte getVisualAnimationEvent() {
        return visualAnimationEvent;
    }

    @Override
    public int getVisualPurchaseCounter() {
        return visualPurchaseCounter;
    }

    @Override
    public int getVisualPaymentSuccessCounter() {
        return visualPaymentSuccessCounter;
    }

    @Override
    public int getVisualPaymentFailCounter() {
        return visualPaymentFailCounter;
    }

    @Override
    public boolean isVisualXpPurchaseFeedbackEnabled() {
        return isPurchaseXpFeedbackSound();
    }

    @Override
    public ShopNpcAnimationState getVisualAnimationState() {
        return visualAnimationState;
    }

    private void handlePaymentFeedbackChange(int slot) {
        if (slot < 0 || slot >= paymentFeedbackSnapshot.length) {
            return;
        }

        ItemStack previous = paymentFeedbackSnapshot[slot];
        ItemStack current = paymentHandler.getStackInSlot(slot).copy();
        paymentFeedbackSnapshot[slot] = current.copy();

        if (level == null || level.isClientSide || !hasOffer || !getVisualSettings().paymentSlotSoundsEnabled()) {
            return;
        }

        if (!isPaymentInsertion(previous, current)) {
            return;
        }

        if (matchesOfferPayment(current)) {
            visualPaymentSuccessCounter++;
        } else {
            visualPaymentFailCounter++;
        }
    }

    private static boolean isPaymentInsertion(ItemStack previous, ItemStack current) {
        if (current.isEmpty()) {
            return false;
        }
        if (previous == null || previous.isEmpty()) {
            return true;
        }
        if (ItemStack.isSameItemSameComponents(previous, current)) {
            return current.getCount() > previous.getCount();
        }
        return true;
    }

    private boolean matchesOfferPayment(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return (!offerPayment1.isEmpty() && ItemStack.isSameItemSameComponents(offerPayment1, stack))
                || (!offerPayment2.isEmpty() && ItemStack.isSameItemSameComponents(offerPayment2, stack));
    }

    private void refreshPaymentFeedbackSnapshot() {
        for (int i = 0; i < paymentFeedbackSnapshot.length; i++) {
            paymentFeedbackSnapshot[i] = paymentHandler.getStackInSlot(i).copy();
        }
    }
}



