package de.bigbull.marketblocks.feature.singleoffer.entity;

import net.minecraft.core.Direction;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopVisualType;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.ShopCategory;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcAnimationEvent;
import net.minecraft.nbt.CompoundTag;

/**
 * Manages all configuration settings for a single-offer shop block entity.
 * Handles general, villager, offer item, IO, access, and notification settings, as well as NBT serialization.
 */
public class ShopSettingsManager {
    private static final String KEY_GENERAL = "General";
    private static final String KEY_VILLAGER = "Villager";
    private static final String KEY_OFFER_ITEM = "OfferItem";
    private static final String KEY_IO = "IO";
    private static final String KEY_ACCESS = "Access";

    private final SingleOfferShopBlockEntity blockEntity;
    private GeneralSettings generalSettings;
    private VillagerSettings villagerSettings;
    private OfferItemSettings offerItemSettings;
    private IoSettings ioSettings;
    private AccessSettings accessSettings;
    private NotificationSettings notificationSettings;

    private boolean outputAlmostFull = false;
    private boolean outputFull = false;
    private boolean globalOfferItemRenderingEnabled = true;

    public ShopSettingsManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.generalSettings = createDefaultGeneralSettings();
        this.villagerSettings = createDefaultVillagerSettings();
        this.offerItemSettings = createDefaultOfferItemSettings();
        this.ioSettings = createDefaultIoSettings();
        this.accessSettings = AccessSettings.DEFAULT;
        this.notificationSettings = createDefaultNotificationSettings();
    }

    private IoSettings createDefaultIoSettings() {
        boolean isMarketCrate = isMarketCrate();
        return new IoSettings(
                IoSettings.DEFAULT.left(),
                IoSettings.DEFAULT.right(),
                IoSettings.DEFAULT.bottom(),
                IoSettings.DEFAULT.back(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_REDSTONE_CONTROL.get() : Config.TRADESTAND_DEFAULT_REDSTONE_CONTROL.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ALLOW_IO.get() : Config.TRADESTAND_DEFAULT_ALLOW_IO.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_AUTO_IO.get() : Config.TRADESTAND_DEFAULT_AUTO_IO.get());
    }

    private GeneralSettings createDefaultGeneralSettings() {
        boolean isMarketCrate = isMarketCrate();
        return new GeneralSettings(
                "",
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_EMIT_REDSTONE.get() : Config.TRADESTAND_DEFAULT_EMIT_REDSTONE.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_PURCHASE_XP_SOUND.get() : Config.TRADESTAND_DEFAULT_PURCHASE_XP_SOUND.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_IS_CLOSED.get() : Config.TRADESTAND_DEFAULT_IS_CLOSED.get(),
                ShopCategory.NONE);
    }

    private VillagerSettings createDefaultVillagerSettings() {
        boolean isMarketCrate = isMarketCrate();
        return new VillagerSettings(
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_VILLAGER_NPC_ENABLED.get() : Config.TRADESTAND_DEFAULT_VILLAGER_NPC_ENABLED.get(),
                "",
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_VILLAGER_PROFESSION.get() : Config.TRADESTAND_DEFAULT_VILLAGER_PROFESSION.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_PURCHASE_PARTICLES.get() : Config.TRADESTAND_DEFAULT_PURCHASE_PARTICLES.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_PURCHASE_SOUNDS.get() : Config.TRADESTAND_DEFAULT_PURCHASE_SOUNDS.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_PAYMENT_SLOT_SOUNDS.get() : Config.TRADESTAND_DEFAULT_PAYMENT_SLOT_SOUNDS.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_USE_PLAYER_SKIN.get() : Config.TRADESTAND_DEFAULT_USE_PLAYER_SKIN.get(),
                "");
    }

    private OfferItemSettings createDefaultOfferItemSettings() {
        boolean isMarketCrate = isMarketCrate();
        return new OfferItemSettings(
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_VISIBLE.get() : Config.TRADESTAND_DEFAULT_ITEM_VISIBLE.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_FULLBRIGHT.get() : Config.TRADESTAND_DEFAULT_ITEM_FULLBRIGHT.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_SCALE.get().floatValue() : Config.TRADESTAND_DEFAULT_ITEM_SCALE.get().floatValue(),
                isMarketCrate ? 0.0f : Config.TRADESTAND_DEFAULT_ITEM_SPEED.get().floatValue(),
                isMarketCrate ? 0.0f : Config.TRADESTAND_DEFAULT_ITEM_HEIGHT_OFFSET.get().floatValue(),
                isMarketCrate ? false : Config.TRADESTAND_DEFAULT_ITEM_BOBBING.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_COUNT.get() : 1,
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_ROTATION.get().floatValue() : 0.0f,
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_LAYOUT_MODE.get() : CrateLayoutMode.STACKED,
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_SPACING_XZ.get().floatValue() : 0.0f,
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_SPACING_Y.get().floatValue() : 0.0f,
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_CHAOS_ROTATION.get().floatValue() : 0.1f,
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_ITEM_DYNAMIC_FILL.get() : false);
    }

    private NotificationSettings createDefaultNotificationSettings() {
        boolean isMarketCrate = isMarketCrate();
        return new NotificationSettings(
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_NOTIFY_PURCHASE.get() : Config.TRADESTAND_DEFAULT_NOTIFY_PURCHASE.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_NOTIFY_OUT_OF_STOCK.get() : Config.TRADESTAND_DEFAULT_NOTIFY_OUT_OF_STOCK.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_NOTIFY_OUTPUT_FULL.get() : Config.TRADESTAND_DEFAULT_NOTIFY_OUTPUT_FULL.get(),
                isMarketCrate ? Config.MARKETCRATE_DEFAULT_NOTIFY_CO_OWNERS.get() : Config.TRADESTAND_DEFAULT_NOTIFY_CO_OWNERS.get());
    }

    private boolean isMarketCrate() {
        return ShopVisualType.from(blockEntity.getBlockState().getBlock()) == ShopVisualType.MARKET_CRATE;
    }


    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(GeneralSettings settings, boolean sync) {
        this.generalSettings = settings == null ? GeneralSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide)
            return;
        blockEntity.setChanged();
        if (sync)
            blockEntity.sync();
    }

    public String getShopName() {
        return generalSettings.shopName();
    }

    public ShopCategory getShopCategory() {
        return generalSettings.shopCategory();
    }

    public boolean isEmitRedstone() {
        return generalSettings.emitRedstone();
    }

    public boolean isPurchaseXpFeedbackSound() {
        return generalSettings.purchaseXpFeedbackSound();
    }


    public VillagerSettings getVillagerSettings() {
        return villagerSettings;
    }

    public void setVillagerSettings(VillagerSettings settings, boolean sync) {
        VillagerSettings previous = this.villagerSettings;
        this.villagerSettings = settings == null ? VillagerSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide)
            return;
        if (previous.npcEnabled() != this.villagerSettings.npcEnabled()) {
            blockEntity.triggerNpcAnimationEvent(this.villagerSettings.npcEnabled() ? VisualNpcAnimationEvent.SPAWN
                    : VisualNpcAnimationEvent.DESPAWN);
        }
        blockEntity.setChanged();
        if (sync)
            blockEntity.sync();
    }


    public OfferItemSettings getOfferItemSettings() {
        return offerItemSettings;
    }

    public void setOfferItemSettings(OfferItemSettings settings, boolean sync) {
        this.offerItemSettings = settings == null ? OfferItemSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide)
            return;
        blockEntity.setChanged();
        if (sync)
            blockEntity.sync();
    }


    public IoSettings getIoSettings() {
        return ioSettings;
    }

    public void setIoSettings(IoSettings settings, boolean sync) {
        this.ioSettings = settings == null ? createDefaultIoSettings() : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide)
            return;

        blockEntity.setChanged();

        for (Direction dir : Direction.values()) {
            blockEntity.invalidateCapabilitiesAndNeighbor(dir);
        }

        if (sync) {
            blockEntity.sync();
            blockEntity.updateNeighborCache();
        }
    }


    public AccessSettings getAccessSettings() {
        return accessSettings;
    }

    public void setAccessSettings(AccessSettings settings, boolean sync) {
        this.accessSettings = settings == null ? AccessSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide)
            return;
        blockEntity.setChanged();
        if (sync)
            blockEntity.sync();
    }

    public boolean isAdminShopEnabled() {
        return accessSettings.adminShopEnabled();
    }


    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettings settings, boolean sync) {
        this.notificationSettings = settings == null ? NotificationSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide)
            return;
        blockEntity.setChanged();
        if (sync)
            blockEntity.sync();
    }


    public boolean isGlobalOfferItemRenderingEnabled() {
        return globalOfferItemRenderingEnabled;
    }

    public void setGlobalOfferItemRenderingEnabled(boolean enabled) {
        this.globalOfferItemRenderingEnabled = enabled;
        blockEntity.setChanged();
    }

    public boolean isOutputFull() {
        return outputFull;
    }

    public boolean isOutputAlmostFull() {
        return outputAlmostFull;
    }

    public void setOutputFullness(boolean full, boolean almostFull) {
        this.outputFull = full;
        this.outputAlmostFull = almostFull;
    }


    public void save(CompoundTag tag) {
        tag.put(KEY_GENERAL, generalSettings.save());
        tag.put(KEY_VILLAGER, villagerSettings.save());
        tag.put(KEY_OFFER_ITEM, offerItemSettings.save());
        tag.put(KEY_IO, ioSettings.save());
        tag.put(KEY_ACCESS, accessSettings.save());
        tag.put("Notification", notificationSettings.save());

        tag.putBoolean("GlobalOfferItemRendering", globalOfferItemRenderingEnabled);
        if (Config.ENABLE_OUTPUT_WARNING.get()) {
            tag.putBoolean("OutputWarning", outputAlmostFull);
        }
        tag.putBoolean("OutputFull", outputFull);
    }

    public void load(CompoundTag tag) {
        if (tag.contains(KEY_GENERAL)) {
            generalSettings = GeneralSettings.load(tag.getCompound(KEY_GENERAL));
        } else {
            generalSettings = createDefaultGeneralSettings();
        }
        if (tag.contains(KEY_VILLAGER)) {
            villagerSettings = VillagerSettings.load(tag.getCompound(KEY_VILLAGER));
        } else {
            villagerSettings = createDefaultVillagerSettings();
        }
        if (tag.contains(KEY_OFFER_ITEM)) {
            offerItemSettings = OfferItemSettings.load(tag.getCompound(KEY_OFFER_ITEM));
        } else {
            offerItemSettings = createDefaultOfferItemSettings();
        }
        if (tag.contains(KEY_IO)) {
            ioSettings = IoSettings.load(tag.getCompound(KEY_IO));
        } else {
            ioSettings = createDefaultIoSettings();
        }
        if (tag.contains(KEY_ACCESS)) {
            accessSettings = AccessSettings.load(tag.getCompound(KEY_ACCESS));
        } else {
            accessSettings = AccessSettings.DEFAULT;
        }
        if (tag.contains("Notification")) {
            notificationSettings = NotificationSettings.load(tag.getCompound("Notification"));
        } else {
            notificationSettings = createDefaultNotificationSettings();
        }

        if (tag.contains("GlobalOfferItemRendering")) {
            globalOfferItemRenderingEnabled = tag.getBoolean("GlobalOfferItemRendering");
        } else {
            globalOfferItemRenderingEnabled = true;
        }
        outputAlmostFull = tag.getBoolean("OutputWarning");
        outputFull = tag.getBoolean("OutputFull");
    }
}

