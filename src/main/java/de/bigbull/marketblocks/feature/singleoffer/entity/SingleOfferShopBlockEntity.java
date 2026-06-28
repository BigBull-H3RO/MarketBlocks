package de.bigbull.marketblocks.feature.singleoffer.entity;

import java.util.function.Supplier;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.visual.npc.IVisualShopNPC;
import de.bigbull.marketblocks.feature.visual.npc.ShopNpcAnimationState;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoRedstoneControl;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
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
    private static final double MAX_PLAYER_DISTANCE_SQUARED = 64.0;
    private static final String NBT_HAS_OFFER = "HasOffer";
    private static final String KEY_PAYMENT1 = "OfferPayment1";
    private static final String KEY_PAYMENT2 = "OfferPayment2";
    private static final String KEY_RESULT = "OfferResult";
    private static final String NBT_TOTAL_SALES = "TotalSales";

    private static final String HANDLER_INPUT = "InputInventory";
    private static final String HANDLER_OUTPUT = "OutputInventory";
    private static final String HANDLER_PAYMENT = "PaymentSlots";
    private static final String HANDLER_OFFER = "OfferSlot";

    public static final int HAS_OFFER_FLAG = 1;
    public static final int OFFER_AVAILABLE_FLAG = 2;
    public static final int OWNER_FLAG = 4;
    public static final int PRIMARY_OWNER_FLAG = 8;
    public static final int OPERATOR_FLAG = 16;
    public static final int GLOBAL_ADMIN_MODE_FLAG = 32;

    private ItemStack offerPayment1 = ItemStack.EMPTY;
    private ItemStack offerPayment2 = ItemStack.EMPTY;
    private ItemStack offerResult = ItemStack.EMPTY;
    private boolean hasOffer = false;
    private int totalSales = 0;
    private Double salePercent = null;
    private long saleEndTimestamp = 0L;
    private static final String NBT_SALE_PERCENT = "SalePercent";
    private static final String NBT_SALE_END_TIMESTAMP = "SaleEndTimestamp";
    public static final int MAX_TRANSACTION_LOG_ENTRIES = 100;

    private final ShopInventoryManager inventoryManager = new ShopInventoryManager(this);
    private final ShopSettingsManager settingsManager = new ShopSettingsManager(this);
    private final ShopVisualManager visualManager = new ShopVisualManager(this);
    private final ShopAccessManager accessManager = new ShopAccessManager(this);
    private final ShopRedstoneManager redstoneManager = new ShopRedstoneManager(this);

    public ShopInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ShopAccessManager getAccessManager() {
        return accessManager;
    }

    public ShopRedstoneManager getRedstoneManager() {
        return redstoneManager;
    }

    private class TrackedItemStackHandler extends ItemStackHandler {
        TrackedItemStackHandler(int size) {
            super(size);
        }

        @Override
        public boolean isItemValid(int slot, @org.jetbrains.annotations.NotNull ItemStack stack) {
            if (isAdminShopEnabled()) {
                return false;
            }
            if (!hasOffer) {
                return false;
            }
            ItemStack expectedItem = getOfferResult();
            if (expectedItem.isEmpty()) {
                return false;
            }
            return ItemStack.isSameItemSameComponents(stack, expectedItem);
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
            updateOfferSlot();
            needsOfferRefresh = true;
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
            visualManager.handlePaymentFeedbackChange(slot, paymentHandler);
        }
    };

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            boolean offerActive = hasOffer();

            if (offerActive) {
                ItemStack currentOffer = getOfferResult();
                if (currentOffer.isEmpty())
                    return ItemStack.EMPTY;

                if (amount < currentOffer.getCount()) {
                    return ItemStack.EMPTY;
                }
            }

            if (simulate) {
                return super.extractItem(slot, amount, true);
            }

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

    private final IItemHandler inputOnly = new SidedWrapper(inputHandler, false,
            () -> this.canProcessIo(SideMode.INPUT));
    private final IItemHandler outputOnly = new SidedWrapper(outputHandler, true,
            () -> this.canProcessIo(SideMode.OUTPUT));

    public boolean canProcessIo(SideMode mode) {
        IoSettings io = getIoSettings();
        if (!io.allowIo())
            return false;

        IoRedstoneControl rc = io.redstoneControl();
        if (rc == IoRedstoneControl.REQUIRE_SIGNAL)
            return redstoneManager.isPoweredByRedstone();
        if (rc == IoRedstoneControl.REQUIRE_NO_SIGNAL)
            return !redstoneManager.isPoweredByRedstone();
        return true;
    }

    private final OfferManager offerManager = new OfferManager(this);

    private int tickCounter = 0;
    private boolean needsOfferRefresh = false;

    private long lastOutOfStockNotifyTime = -1;
    private long lastOutputFullNotifyTime = -1;

    public SingleOfferShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * A wrapper for IItemHandler that restricts insertion or extraction.
     * 
     * @param backing     The backing item handler.
     * @param extractOnly If true, only extraction is allowed. If false, only
     *                    insertion is allowed.
     */
    record SidedWrapper(IItemHandler backing, boolean extractOnly, Supplier<Boolean> isActive) implements IItemHandler {
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
            if (extractOnly || !isActive.get()) {
                return stack;
            }
            return backing.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!extractOnly || !isActive.get()) {
                return ItemStack.EMPTY;
            }
            return backing.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return backing.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !extractOnly && isActive.get() && backing.isItemValid(slot, stack);
        }
    }

    public void updateShopDirectory() {
        if (level instanceof ServerLevel serverLevel) {
            ShopDirectorySavedData data = ShopDirectorySavedData.get(serverLevel);
            GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
            data.registerOrUpdateShop(globalPos, getOwnerId(), getOwnerName(), getShopName(),
                    getGeneralSettings().isClosed(), settingsManager.getShopCategory(), getOfferPayment1(),
                    getOfferPayment2(), getOfferResult(), totalSales, isAdminShopEnabled());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.marketblocks.trade_stand");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SingleOfferShopMenu(containerId, playerInventory, this);
    }

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

    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= MAX_PLAYER_DISTANCE_SQUARED;
    }

    public void setOwner(Player player) {
        accessManager.setOwner(player);
    }

    public void addOwner(UUID id, String name) {
        accessManager.addOwner(id, name);
    }

    @ApiStatus.Internal
    public void addOwnerClient(UUID id, String name) {
        accessManager.addOwnerClient(id, name);
    }

    public void removeOwner(UUID id) {
        accessManager.removeOwner(id);
    }

    public Set<UUID> getOwners() {
        return accessManager.getOwners();
    }

    public Map<UUID, String> getAdditionalOwners() {
        return accessManager.getAdditionalOwners();
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        accessManager.setAdditionalOwners(owners);
    }

    public boolean isOwner(Player player) {
        return accessManager.isOwner(player);
    }

    public boolean isOwnerByUUID(UUID uuid) {
        return accessManager.isOwnerByUUID(uuid);
    }

    public boolean canPlayerBuy(Player player) {
        return accessManager.canPlayerBuy(player);
    }

    public boolean canPlayerBuyByUUID(UUID uuid) {
        return accessManager.canPlayerBuyByUUID(uuid);
    }

    public boolean isPrimaryOwner(Player player) {
        return accessManager.isPrimaryOwner(player);
    }

    public void ensureOwner(Player player) {
        accessManager.ensureOwner(player);
    }

    public UUID getOwnerId() {
        return accessManager.getOwnerId();
    }

    public String getOwnerName() {
        return accessManager.getOwnerName();
    }

    public void beginPurchaseContext(@Nullable Player player) {
        accessManager.beginPurchaseContext(player);
    }

    public void clearPurchaseContext() {
        accessManager.clearPurchaseContext();
    }

    public NotificationSettings getNotificationSettings() {
        return settingsManager.getNotificationSettings();
    }

    public void setNotificationSettings(NotificationSettings settings, boolean sync) {
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
            NotificationSettings notifications) {

        boolean oldAdminShop = isAdminShopEnabled();

        settingsManager.setIoSettings(io, false);
        settingsManager.setGeneralSettings(general, false);
        settingsManager.setVillagerSettings(villager, false);
        settingsManager.setOfferItemSettings(offerItem, false);
        settingsManager.setAccessSettings(access, false);
        settingsManager.setNotificationSettings(notifications, false);

        if (oldAdminShop != access.adminShopEnabled()) {
            needsOfferRefresh = true;
            updateOfferSlot(false);
        }

        if (level != null && !level.isClientSide) {
            unlockAdjacentChests();
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

    public long getLastOutOfStockNotifyTime() {
        return lastOutOfStockNotifyTime;
    }

    public void setLastOutOfStockNotifyTime(long time) {
        this.lastOutOfStockNotifyTime = time;
    }

    public long getLastOutputFullNotifyTime() {
        return lastOutputFullNotifyTime;
    }

    public void setLastOutputFullNotifyTime(long time) {
        this.lastOutputFullNotifyTime = time;
    }

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
        setGeneralSettings(new GeneralSettings(name, current.emitRedstone(), current.purchaseXpFeedbackSound(),
                current.isClosed(), current.shopCategory()), sync);
        if (level != null && !level.isClientSide) {
            updateShopDirectory();
        }
    }

    public boolean isEmitRedstone() {
        return settingsManager.isEmitRedstone();
    }

    public void setEmitRedstone(boolean emitRedstone, boolean sync) {
        GeneralSettings current = getGeneralSettings();
        setGeneralSettings(
                new GeneralSettings(current.shopName(), emitRedstone, current.purchaseXpFeedbackSound(),
                        current.isClosed(), current.shopCategory()),
                sync);
    }

    public boolean isPurchaseXpFeedbackSound() {
        return settingsManager.isPurchaseXpFeedbackSound();
    }

    public void setPurchaseXpFeedbackSound(boolean enabled, boolean sync) {
        GeneralSettings current = getGeneralSettings();
        setGeneralSettings(
                new GeneralSettings(current.shopName(), current.emitRedstone(), enabled, current.isClosed(),
                        current.shopCategory()),
                sync);
    }

    public VillagerSettings getVillagerSettings() {
        return settingsManager.getVillagerSettings();
    }

    public void setVillagerSettings(VillagerSettings settings, boolean sync) {
        settingsManager.setVillagerSettings(settings, sync);
    }

    public OfferItemSettings getOfferItemSettings() {
        return settingsManager.getOfferItemSettings();
    }

    public void setOfferItemSettings(OfferItemSettings settings, boolean sync) {
        settingsManager.setOfferItemSettings(settings, sync);
    }

    @ApiStatus.Internal
    public void triggerNpcAnimationEvent(byte event) {
        visualManager.triggerNpcAnimationEvent(event);
    }

    public void invalidateCapabilitiesAndNeighbor(Direction dir) {
        redstoneManager.invalidateCapabilitiesAndNeighbor(dir);
    }

    public IItemHandler getValidNeighborHandler(Direction dir) {
        return inventoryManager.getValidNeighborHandler(dir);
    }

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

    public boolean isSaleActive() {
        if (!isAdminShopEnabled() || salePercent == null) {
            return false;
        }
        if (saleEndTimestamp > 0L && System.currentTimeMillis() >= saleEndTimestamp) {
            salePercent = null;
            saleEndTimestamp = 0L;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return false;
        }
        return true;
    }

    public Double getSalePercent() {
        return isSaleActive() ? salePercent : null;
    }

    public void setSale(Double salePercent, long durationMillis) {
        this.salePercent = salePercent;
        this.saleEndTimestamp = durationMillis > 0 ? System.currentTimeMillis() + durationMillis : 0L;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        updateShopDirectory();
    }

    public ItemStack getOfferPayment1() {
        if (isSaleActive() && !offerPayment1.isEmpty()) {
            double multiplier = Math.max(0.0, 1.0 + (salePercent / 100.0));
            ItemStack adjusted = offerPayment1.copy();
            adjusted.setCount(de.bigbull.marketblocks.feature.marketplace.data.MarketplaceRuntimeMath
                    .scalePaymentCount(offerPayment1.getCount(), multiplier));
            return adjusted;
        }
        return offerPayment1;
    }

    public ItemStack getOfferPayment2() {
        if (isSaleActive() && !offerPayment2.isEmpty()) {
            double multiplier = Math.max(0.0, 1.0 + (salePercent / 100.0));
            ItemStack adjusted = offerPayment2.copy();
            adjusted.setCount(de.bigbull.marketblocks.feature.marketplace.data.MarketplaceRuntimeMath
                    .scalePaymentCount(offerPayment2.getCount(), multiplier));
            return adjusted;
        }
        return offerPayment2;
    }

    public ItemStack getOriginalOfferPayment1() {
        return offerPayment1;
    }

    public ItemStack getOriginalOfferPayment2() {
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

    public ShopVisualManager getVisualManager() {
        return visualManager;
    }

    public void incrementVisualPurchaseCounter(int amount) {
        visualManager.incrementVisualPurchaseCounter(amount);
    }

    public void playPurchaseXpSound(int actualAmount) {
        visualManager.playPurchaseXpSound(actualAmount);
    }

    public void triggerRedstonePulse() {
        redstoneManager.triggerRedstonePulse();
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
        if (getGeneralSettings().isClosed())
            return false;
        if (accessManager.purchaseContextBuyerId != null) {
            if (!canPlayerBuyByUUID(accessManager.purchaseContextBuyerId))
                return false;
        }
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        boolean stockReady = isAdminShopEnabled() || offerManager.hasResultItemInInput(false);
        boolean outputReady = isAdminShopEnabled() || (!isOutputFull() && inventoryManager.hasOutputSpace(p1, p2));
        return hasOffer && offerManager.canAfford() && stockReady && outputReady;
    }

    public int getAnalogSignal(Direction readSide) {
        return redstoneManager.getAnalogSignal(readSide);
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
        return accessManager.createMenuFlags(player);
    }

    private void dropItems(Level level, BlockPos pos, ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        dropItems(level, pos, inputHandler);
        dropItems(level, pos, outputHandler);
        dropItems(level, pos, paymentHandler);
        if (!hasOffer) {
            dropItems(level, pos, offerHandler);
        }
    }

    public void unlockAdjacentChests() {
        redstoneManager.unlockAdjacentChests();
    }

    public void lockAdjacentChest(Direction side) {
        redstoneManager.lockAdjacentChest(side);
    }

    public void lockAdjacentChests() {
        redstoneManager.lockAdjacentChests();
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
                be.sync();
            }
            be.updateOutputFullness();
        }

        int chestInterval = Config.CHEST_IO_INTERVAL.get();
        if (chestExtensionEnabled && chestInterval > 0 && be.tickCounter % chestInterval == 0) {
            be.updateNeighborCache();
            if (be.getIoSettings().autoIo()) {
                if (be.canProcessIo(SideMode.INPUT)) {
                    be.inventoryManager.pullFromInputChest(be.inputHandler);
                }
                if (be.canProcessIo(SideMode.OUTPUT)) {
                    be.inventoryManager.pushToOutputChest(be.outputHandler);
                }
            }
        }
    }

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
        if (level != null && level.isClientSide()) {
            de.bigbull.marketblocks.compat.journeymap.JourneyMapCompat.removeShopMarker(worldPosition);
        }
        super.setRemoved();
        unlockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide()) {
            de.bigbull.marketblocks.compat.journeymap.JourneyMapCompat.addShopMarker(this);
        }
        if (level != null && !level.isClientSide && getBlockState().is(RegistriesInit.TRADE_STAND_BLOCK.get())) {
            TradeStandBlock.ensureTopBlock(level, worldPosition);
        }
        updateNeighborCache();

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
        visualManager.load(tag);

        if (tag.contains(NBT_TOTAL_SALES)) {
            totalSales = tag.getInt(NBT_TOTAL_SALES);
        }

        if (tag.contains(NBT_SALE_PERCENT)) {
            salePercent = tag.getDouble(NBT_SALE_PERCENT);
            saleEndTimestamp = tag.getLong(NBT_SALE_END_TIMESTAMP);
        } else {
            salePercent = null;
            saleEndTimestamp = 0L;
        }

        tickCounter = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveHandlers(tag, registries);
        saveOffer(tag, registries);
        settingsManager.save(tag);
        visualManager.save(tag);

        tag.putInt(NBT_TOTAL_SALES, totalSales);

        if (salePercent != null) {
            tag.putDouble(NBT_SALE_PERCENT, salePercent);
            tag.putLong(NBT_SALE_END_TIMESTAMP, saleEndTimestamp);
        }
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
        CompoundTag tag = new CompoundTag();

        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
        if (hasOffer) {
            if (!offerPayment1.isEmpty())
                tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
            if (!offerPayment2.isEmpty())
                tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
            if (!offerResult.isEmpty())
                tag.put(KEY_RESULT, offerResult.save(registries));
        }

        settingsManager.save(tag);
        visualManager.save(tag);

        if (salePercent != null) {
            tag.putDouble(NBT_SALE_PERCENT, salePercent);
            tag.putLong(NBT_SALE_END_TIMESTAMP, saleEndTimestamp);
        }

        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {

        offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1));
        offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2));
        offerResult = ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT));
        hasOffer = tag.getBoolean(NBT_HAS_OFFER);

        if (tag.contains(NBT_SALE_PERCENT)) {
            salePercent = tag.getDouble(NBT_SALE_PERCENT);
            saleEndTimestamp = tag.getLong(NBT_SALE_END_TIMESTAMP);
        } else {
            salePercent = null;
            saleEndTimestamp = 0L;
        }

        settingsManager.load(tag);
        visualManager.load(tag);

        updateOfferSlot();
        visualManager.refreshPaymentFeedbackSnapshot(paymentHandler);
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
        return visualManager.getVisualAnimationNonce();
    }

    @Override
    public byte getVisualAnimationEvent() {
        return visualManager.getVisualAnimationEvent();
    }

    @Override
    public int getVisualPurchaseCounter() {
        return visualManager.getVisualPurchaseCounter();
    }

    @Override
    public int getVisualPaymentSuccessCounter() {
        return visualManager.getVisualPaymentSuccessCounter();
    }

    @Override
    public int getVisualPaymentFailCounter() {
        return visualManager.getVisualPaymentFailCounter();
    }

    public int getTotalSales() {
        return totalSales;
    }

    public void incrementTotalSales(int amount) {
        this.totalSales += amount;
        setChanged();
        if (level != null && !level.isClientSide) {
            updateShopDirectory();
        }
    }

    @Override
    public boolean isVisualXpPurchaseFeedbackEnabled() {
        return isPurchaseXpFeedbackSound();
    }

    @Override
    public ShopNpcAnimationState getVisualAnimationState() {
        return visualManager.getVisualAnimationState();
    }

}
