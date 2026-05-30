package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.log.ShopTransactionLogSavedData;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.visual.npc.IVisualShopNPC;
import de.bigbull.marketblocks.feature.visual.npc.ShopNpcAnimationState;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcAnimationEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.neoforged.neoforge.items.IItemHandler;
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
    public UUID purchaseContextBuyerId;
    public String purchaseContextBuyerName = "";

    private final ShopInventoryManager inventoryManager = new ShopInventoryManager(this);

    public ShopInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    // Shop Settings
    private final ShopSettingsManager settingsManager = new ShopSettingsManager(this);
    private int visualAnimationNonce = 0;
    private byte visualAnimationEvent = VisualNpcAnimationEvent.NONE;
    private int visualPurchaseCounter = 0;
    private int visualPaymentSuccessCounter = 0;
    private int visualPaymentFailCounter = 0;
    private final ShopNpcAnimationState visualAnimationState = new ShopNpcAnimationState();
    private final ItemStack[] paymentFeedbackSnapshot = new ItemStack[] { ItemStack.EMPTY, ItemStack.EMPTY };
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

    // Reused snapshot handler to avoid per-call allocations during output-space
    // simulations.
    private final ItemStackHandler outputSimulationHandler = new ItemStackHandler(outputHandler.getSlots());

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            boolean offerActive = hasOffer();

            // Ensure we only allow extraction of the FULL offer amount while an offer is
            // active.
            if (offerActive) {
                ItemStack currentOffer = getOfferResult();
                if (currentOffer.isEmpty())
                    return ItemStack.EMPTY;

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
            HANDLER_OFFER, offerHandler);

    private final IItemHandler inputOnly = new SidedWrapper(inputHandler, false);
    private final IItemHandler outputOnly = new SidedWrapper(outputHandler, true);

    private final OfferManager offerManager = new OfferManager(this);

    private int tickCounter = 0;
    private boolean needsOfferRefresh = false;
    
    private long lastOutOfStockNotifyTime = -1;
    private long lastOutputFullNotifyTime = -1;

    public SingleOfferShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    // --- Sided Wrapper for Item Handlers ---
    /**
     * A wrapper for IItemHandler that restricts insertion or extraction.
     * 
     * @param backing     The backing item handler.
     * @param extractOnly If true, only extraction is allowed. If false, only
     *                    insertion is allowed.
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

    public void updateShopDirectory() {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            de.bigbull.marketblocks.core.data.ShopDirectorySavedData data = de.bigbull.marketblocks.core.data.ShopDirectorySavedData.get(serverLevel);
            net.minecraft.core.GlobalPos globalPos = net.minecraft.core.GlobalPos.of(serverLevel.dimension(), getBlockPos());
            data.registerOrUpdateShop(globalPos, getOwnerId(), getOwnerName(), getShopName(), getGeneralSettings().isClosed());
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
        settingsManager.setAccessSettings(
                settingsManager.getAccessSettings().withOwner(player.getUUID(), player.getName().getString()), true);
        if (level != null && !level.isClientSide) {
            updateShopDirectory();
        }
    }

    @SuppressWarnings("unused")
    public void addOwner(UUID id, String name) {
        Map<UUID, String> newOwners = new HashMap<>(settingsManager.getAccessSettings().additionalOwners());
        newOwners.put(id, name);
        settingsManager.setAccessSettings(settingsManager.getAccessSettings().withAdditionalOwners(newOwners), true);
    }

    @ApiStatus.Internal
    public void addOwnerClient(UUID id, String name) {
        Map<UUID, String> newOwners = new HashMap<>(settingsManager.getAccessSettings().additionalOwners());
        newOwners.put(id, name);
        settingsManager.setAccessSettings(settingsManager.getAccessSettings().withAdditionalOwners(newOwners), false);
    }

    @SuppressWarnings("unused")
    public void removeOwner(UUID id) {
        Map<UUID, String> newOwners = new HashMap<>(settingsManager.getAccessSettings().additionalOwners());
        if (newOwners.remove(id) != null) {
            settingsManager.setAccessSettings(settingsManager.getAccessSettings().withAdditionalOwners(newOwners),
                    true);
        }
    }

    public Set<UUID> getOwners() {
        Set<UUID> set = new HashSet<>();
        UUID ownerId = settingsManager.getAccessSettings().ownerId();
        if (ownerId != null)
            set.add(ownerId);
        set.addAll(settingsManager.getAccessSettings().additionalOwners().keySet());
        return set;
    }

    public Map<UUID, String> getAdditionalOwners() {
        return new HashMap<>(settingsManager.getAccessSettings().additionalOwners());
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        settingsManager.setAccessSettings(settingsManager.getAccessSettings().withAdditionalOwners(owners), true);
    }

    public boolean isOwner(Player player) {
        if (isAdminShopEnabled() && player.hasPermissions(2))
            return true;
        return isOwnerByUUID(player.getUUID());
    }

    public boolean isOwnerByUUID(UUID uuid) {
        AccessSettings acc = settingsManager.getAccessSettings();
        return uuid.equals(acc.ownerId()) || acc.additionalOwners().containsKey(uuid);
    }

    public boolean canPlayerBuy(Player player) {
        if (player == null) return !getGeneralSettings().isClosed();
        if (isAdminShopEnabled() && player.hasPermissions(2)) return true;
        return canPlayerBuyByUUID(player.getUUID());
    }

    public boolean canPlayerBuyByUUID(UUID uuid) {
        if (getGeneralSettings().isClosed()) {
            return false;
        }
        if (uuid == null || isOwnerByUUID(uuid)) {
            return true;
        }
        AccessSettings acc = getAccessSettings();
        AccessMode mode = acc.accessMode();
        if (mode == AccessMode.WHITELIST) {
            return acc.accessList().containsKey(uuid);
        } else if (mode == AccessMode.BLACKLIST) {
            return !acc.accessList().containsKey(uuid);
        }
        return true;
    }

    public boolean isPrimaryOwner(Player player) {
        if (isAdminShopEnabled() && player.hasPermissions(2))
            return true;
        return player.getUUID().equals(settingsManager.getAccessSettings().ownerId());
    }

    public void ensureOwner(Player player) {
        if (settingsManager.getAccessSettings().ownerId() == null) {
            setOwner(player);
        }
    }

    public UUID getOwnerId() {
        return settingsManager.getAccessSettings().ownerId();
    }

    public String getOwnerName() {
        return settingsManager.getAccessSettings().ownerName();
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

    public de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings getNotificationSettings() {
        return settingsManager.getNotificationSettings();
    }

    public void setNotificationSettings(de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings settings, boolean sync) {
        settingsManager.setNotificationSettings(settings, sync);
    }

    public boolean isAdminShopEnabled() {
        return settingsManager.isAdminShopEnabled();
    }

    public boolean isGlobalAdminModeEnabled() {
        return Config.MARKETBLOCKS_ADMIN_MODE_ENABLED.get();
    }

    public boolean isOfferItemRenderingGloballyEnabled() {
        return level != null && level.isClientSide ? settingsManager.isGlobalOfferItemRenderingEnabled()
                : Config.ENABLE_GLOBAL_OFFER_ITEM_RENDERING.get();
    }

    @ApiStatus.Internal
    public void setOfferItemRenderingGloballyEnabledClient(boolean enabled) {
        settingsManager.setGlobalOfferItemRenderingEnabled(enabled);
    }

    public void setAdminShopEnabled(boolean enabled) {
        if (settingsManager.isAdminShopEnabled() == enabled) {
            return;
        }
        settingsManager.setAccessSettings(settingsManager.getAccessSettings().withAdminShopEnabled(enabled), true);
        needsOfferRefresh = true;
        updateOfferSlot(false);
    }

    @ApiStatus.Internal
    public void setAdminShopEnabledClient(boolean enabled) {
        settingsManager.setAccessSettings(settingsManager.getAccessSettings().withAdminShopEnabled(enabled), false);
        needsOfferRefresh = true;
        updateOfferSlot(false);
    }

    public AccessSettings getAccessSettings() {
        return settingsManager.getAccessSettings();
    }

    public void setAccessSettings(AccessSettings accessSettings, boolean sync) {
        settingsManager.setAccessSettings(accessSettings, sync);
    }

    public IoSettings getIoSettings() {
        return settingsManager.getIoSettings();
    }

    public void setIoSettings(IoSettings ioSettings, boolean sync) {
        settingsManager.setIoSettings(ioSettings, sync);
    }

    // --- Side Configuration ---
    public SideMode getMode(Direction absoluteDir) {
        return settingsManager.getIoSettings().getMode(absoluteDir, getBlockState().getValue(BaseShopBlock.FACING));
    }

    public void setMode(Direction absoluteDir, SideMode mode, boolean sync) {
        Direction facing = getBlockState().getValue(BaseShopBlock.FACING);
        SideMode oldMode = getMode(absoluteDir);
        settingsManager.setIoSettings(settingsManager.getIoSettings().withMode(absoluteDir, facing, mode), sync);
        if (level != null && !level.isClientSide) {
            if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                lockAdjacentChest(absoluteDir);
            } else if (oldMode != SideMode.DISABLED) {
                unlockAdjacentChests();
            }
        }
    }

    /**
     * Batch update for shop settings to avoid multiple sync() calls.
     * Used by UpdateSettingsPacket to update all settings at once.
     */
    public void updateSettingsBatch(IoSettings io,
            GeneralSettings general,
            VillagerSettings villager,
            OfferItemSettings offerItem,
            AccessSettings access,
            de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings notifications) {

        settingsManager.setIoSettings(io, false);
        settingsManager.setGeneralSettings(general, false);
        settingsManager.setVillagerSettings(villager, false);
        settingsManager.setOfferItemSettings(offerItem, false);
        settingsManager.setAccessSettings(access, false);
        settingsManager.setNotificationSettings(notifications, false);

        if (level != null && !level.isClientSide) {
            unlockAdjacentChests(); // Safe reset
            lockAdjacentChests();
        }

        setChanged();
        sync();
        updateNeighborCache();
        
        if (level != null && !level.isClientSide) {
            updateShopDirectory();
        }
    }

    public void onAccessSettingsChanged() {
        if (level != null && !level.isClientSide) {
            updateShopDirectory();
        }
    }

    public long getLastOutOfStockNotifyTime() { return lastOutOfStockNotifyTime; }
    public void setLastOutOfStockNotifyTime(long time) { this.lastOutOfStockNotifyTime = time; }

    public long getLastOutputFullNotifyTime() { return lastOutputFullNotifyTime; }
    public void setLastOutputFullNotifyTime(long time) { this.lastOutputFullNotifyTime = time; }

    // --- General Settings Accessors ---

    public GeneralSettings getGeneralSettings() {
        return settingsManager.getGeneralSettings();
    }

    public void setGeneralSettings(GeneralSettings settings, boolean sync) {
        settingsManager.setGeneralSettings(settings, sync);
    }

    public String getShopName() {
        return settingsManager.getShopName();
    }

    public void setShopName(String name, boolean sync) {
        GeneralSettings current = getGeneralSettings();
        setGeneralSettings(new GeneralSettings(name, current.emitRedstone(), current.purchaseXpFeedbackSound(), current.isClosed()), sync);
        if (level != null && !level.isClientSide) {
            updateShopDirectory();
        }
    }

    public boolean isEmitRedstone() {
        return settingsManager.isEmitRedstone();
    }

    public void setEmitRedstone(boolean emitRedstone, boolean sync) {
        GeneralSettings current = getGeneralSettings();
        setGeneralSettings(new GeneralSettings(current.shopName(), emitRedstone, current.purchaseXpFeedbackSound(), current.isClosed()),
                sync);
    }

    public boolean isPurchaseXpFeedbackSound() {
        return settingsManager.isPurchaseXpFeedbackSound();
    }

    public void setPurchaseXpFeedbackSound(boolean enabled, boolean sync) {
        GeneralSettings current = getGeneralSettings();
        setGeneralSettings(new GeneralSettings(current.shopName(), current.emitRedstone(), enabled, current.isClosed()), sync);
    }

    // --- Villager Settings Accessors ---

    public VillagerSettings getVillagerSettings() {
        return settingsManager.getVillagerSettings();
    }

    public void setVillagerSettings(VillagerSettings settings, boolean sync) {
        settingsManager.setVillagerSettings(settings, sync);
    }

    // --- Offer Item Settings Accessors ---

    public OfferItemSettings getOfferItemSettings() {
        return settingsManager.getOfferItemSettings();
    }

    public void setOfferItemSettings(OfferItemSettings settings, boolean sync) {
        settingsManager.setOfferItemSettings(settings, sync);
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
    public ShopSettingsManager getSettingsManager() {
        return settingsManager;
    }

    public void incrementVisualPurchaseCounter(int amount) {
        visualPurchaseCounter += amount;
    }

    public void playPurchaseXpSound(int actualAmount) {
        if (isPurchaseXpFeedbackSound() && level != null) {
            long now = level.getGameTime();
            if (now - lastPurchaseXpSoundTick > 4L) {
                float pitch = Math.min(0.7F + actualAmount * 0.06F, 1.6F);
                level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, pitch);
                lastPurchaseXpSoundTick = now;
            }
        }
    }

    public void triggerRedstonePulse() {
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

    public void updateOfferSlot() {
        updateOfferSlot(false);
    }

    public void updateOfferSlot(boolean checkNeighbors) {
        if (!hasOffer) {
            return;
        }

        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        boolean stockAvailable = isAdminShopEnabled() || offerManager.hasResultItemInInput(checkNeighbors);
        boolean outputReady = isAdminShopEnabled() || (!isOutputFull() && inventoryManager.hasOutputSpace(p1, p2));
        if (offerManager.canAfford() && stockAvailable && outputReady) {
            if (offerHandler.getStackInSlot(0).isEmpty()) {
                offerHandler.setStackInSlot(0, getOfferResult().copy());
            }
        } else {
            offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
        return offerManager.hasResultItemInInput(checkNeighbors);
    }

    private boolean isReadyToPurchase() {
        if (getGeneralSettings().isClosed()) return false;
        if (purchaseContextBuyerId != null) {
            if (!canPlayerBuyByUUID(purchaseContextBuyerId)) return false;
        }
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        boolean stockReady = isAdminShopEnabled() || offerManager.hasResultItemInInput(false);
        boolean outputReady = isAdminShopEnabled() || (!isOutputFull() && inventoryManager.hasOutputSpace(p1, p2));
        return hasOffer && offerManager.canAfford() && stockReady && outputReady;
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
        if (handler == null)
            return 0;

        int totalSlots = handler.getSlots();
        if (totalSlots == 0)
            return 0;

        float fullness = 0.0F;
        boolean hasItem = false;

        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                fullness += (float) stack.getCount()
                        / (float) Math.min(handler.getSlotLimit(i), stack.getMaxStackSize());
                hasItem = true;
            }
        }

        fullness /= (float) totalSlots;
        return Mth.floor(fullness * 14.0F) + (hasItem ? 1 : 0);
    }

    public void processPurchase() {
        offerManager.processBulkPurchase(1, null, false);
    }

    public void processPurchase(@Nullable Player buyer) {
        offerManager.processBulkPurchase(1, buyer, false);
    }

    public int processBulkPurchase(int maxAmount) {
        return offerManager.processBulkPurchase(maxAmount, null, false);
    }

    public int processBulkPurchase(int maxAmount, @Nullable Player buyer) {
        return offerManager.processBulkPurchase(maxAmount, buyer, false);
    }

    public int processBulkPurchase(int maxAmount, @Nullable Player buyer, boolean shiftPurchase) {
        return offerManager.processBulkPurchase(maxAmount, buyer, shiftPurchase);
    }

    public boolean isOutputAlmostFull() {
        return settingsManager.isOutputAlmostFull();
    }

    public boolean isOutputFull() {
        return settingsManager.isOutputFull();
    }

    public boolean isOutputSpaceMissing() {
        if (isAdminShopEnabled()) {
            return false;
        }
        if (!offerManager.hasResultItemInInput(false)) {
            return false;
        }
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        return !inventoryManager.hasOutputSpace(p1, p2);
    }

    public void updateOutputFullness() {
        inventoryManager.updateOutputFullness();
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
                    if (hasOffer())
                        flags |= HAS_OFFER_FLAG;
                    if (isOfferAvailable())
                        flags |= OFFER_AVAILABLE_FLAG;
                    if (isOwner(player))
                        flags |= OWNER_FLAG;
                    if (isPrimaryOwner(player))
                        flags |= PRIMARY_OWNER_FLAG;
                    if (player != null && player.hasPermissions(2))
                        flags |= OPERATOR_FLAG;
                    if (isGlobalAdminModeEnabled())
                        flags |= GLOBAL_ADMIN_MODE_FLAG;
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
        if (level == null || !isChestIoExtensionEnabled())
            return;
        for (Direction dir : DIRECTIONS) {
            invalidateNeighbor(dir);
        }
    }

    public void lockAdjacentChest(Direction side) {
        if (level == null || !isChestIoExtensionEnabled())
            return;
        invalidateNeighbor(side);
    }

    private void invalidateNeighbor(Direction dir) {
        BlockPos neighbour = worldPosition.relative(dir);
        level.invalidateCapabilities(neighbour);
    }

    public void lockAdjacentChests() {
        if (level == null || !isChestIoExtensionEnabled())
            return;
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
            if (!level.isClientSide) {
                updateShopDirectory();
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadHandlers(tag, registries);
        loadOffer(tag, registries);
        settingsManager.load(tag);

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
        saveOffer(tag, registries);
        settingsManager.save(tag);

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
        if (!offerPayment1.isEmpty())
            tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
        if (!offerPayment2.isEmpty())
            tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
        if (!offerResult.isEmpty())
            tag.put(KEY_RESULT, offerResult.save(registries));
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
            if (!offerPayment1.isEmpty())
                tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
            if (!offerPayment2.isEmpty())
                tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
            if (!offerResult.isEmpty())
                tag.put(KEY_RESULT, offerResult.save(registries));
        }

        // Shop settings (includes I/O and Access)
        settingsManager.save(tag);
        tag.putInt(NBT_VISUAL_ANIMATION_NONCE, visualAnimationNonce);
        tag.putByte(NBT_VISUAL_ANIMATION_EVENT, visualAnimationEvent);
        tag.putInt(NBT_VISUAL_PURCHASE_COUNTER, visualPurchaseCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER, visualPaymentSuccessCounter);
        tag.putInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER, visualPaymentFailCounter);

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

        // Shop settings (includes I/O and Access)
        settingsManager.load(tag);
        visualAnimationNonce = tag.getInt(NBT_VISUAL_ANIMATION_NONCE);
        visualAnimationEvent = tag.getByte(NBT_VISUAL_ANIMATION_EVENT);
        visualPurchaseCounter = tag.getInt(NBT_VISUAL_PURCHASE_COUNTER);
        visualPaymentSuccessCounter = tag.getInt(NBT_VISUAL_PAYMENT_SUCCESS_COUNTER);
        visualPaymentFailCounter = tag.getInt(NBT_VISUAL_PAYMENT_FAIL_COUNTER);

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

        if (level == null || level.isClientSide || !hasOffer || !getVillagerSettings().paymentSlotSoundsEnabled()) {
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
