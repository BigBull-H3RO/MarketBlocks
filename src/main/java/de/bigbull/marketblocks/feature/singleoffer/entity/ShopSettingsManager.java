package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcAnimationEvent;
import net.minecraft.nbt.CompoundTag;

public class ShopSettingsManager {
    private static final String KEY_GENERAL = "General";
    private static final String KEY_VILLAGER = "Villager";
    private static final String KEY_OFFER_ITEM = "OfferItem";
    private static final String KEY_IO = "IO";
    private static final String KEY_ACCESS = "Access";

    private final SingleOfferShopBlockEntity blockEntity;
    private GeneralSettings generalSettings = GeneralSettings.DEFAULT;
    private VillagerSettings villagerSettings = VillagerSettings.DEFAULT;
    private OfferItemSettings offerItemSettings = OfferItemSettings.DEFAULT;
    private IoSettings ioSettings = IoSettings.DEFAULT;
    private AccessSettings accessSettings = AccessSettings.DEFAULT;

    private boolean outputAlmostFull = false;
    private boolean outputFull = false;
    private boolean globalOfferItemRenderingEnabled = true;

    public ShopSettingsManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    // --- General Settings ---

    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(GeneralSettings settings, boolean sync) {
        this.generalSettings = settings == null ? GeneralSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    public String getShopName() {
        return generalSettings.shopName();
    }

    public boolean isEmitRedstone() {
        return generalSettings.emitRedstone();
    }

    public boolean isPurchaseXpFeedbackSound() {
        return generalSettings.purchaseXpFeedbackSound();
    }

    // --- Villager Settings ---

    public VillagerSettings getVillagerSettings() {
        return villagerSettings;
    }

    public void setVillagerSettings(VillagerSettings settings, boolean sync) {
        VillagerSettings previous = this.villagerSettings;
        this.villagerSettings = settings == null ? VillagerSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        if (previous.npcEnabled() != this.villagerSettings.npcEnabled()) {
            blockEntity.triggerNpcAnimationEvent(this.villagerSettings.npcEnabled() ? VisualNpcAnimationEvent.SPAWN : VisualNpcAnimationEvent.DESPAWN);
        }
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    // --- Offer Item Settings ---

    public OfferItemSettings getOfferItemSettings() {
        return offerItemSettings;
    }

    public void setOfferItemSettings(OfferItemSettings settings, boolean sync) {
        this.offerItemSettings = settings == null ? OfferItemSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    // --- I/O Settings ---

    public IoSettings getIoSettings() {
        return ioSettings;
    }

    public void setIoSettings(IoSettings settings, boolean sync) {
        this.ioSettings = settings == null ? IoSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        
        blockEntity.setChanged();
        
        // Notify capabilities of potential access changes
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            blockEntity.invalidateCapabilitiesAndNeighbor(dir);
        }
        
        if (sync) {
            blockEntity.sync();
            blockEntity.updateNeighborCache();
        }
    }

    // --- Access Settings ---

    public AccessSettings getAccessSettings() {
        return accessSettings;
    }

    public void setAccessSettings(AccessSettings settings, boolean sync) {
        this.accessSettings = settings == null ? AccessSettings.DEFAULT : settings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    public boolean isAdminShopEnabled() {
        return accessSettings.adminShopEnabled();
    }

    // --- Global/Runtime Data ---

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

    // --- NBT ---

    public void save(CompoundTag tag) {
        tag.put(KEY_GENERAL, generalSettings.save());
        tag.put(KEY_VILLAGER, villagerSettings.save());
        tag.put(KEY_OFFER_ITEM, offerItemSettings.save());
        tag.put(KEY_IO, ioSettings.save());
        tag.put(KEY_ACCESS, accessSettings.save());
        
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
            generalSettings = GeneralSettings.DEFAULT;
        }
        if (tag.contains(KEY_VILLAGER)) {
            villagerSettings = VillagerSettings.load(tag.getCompound(KEY_VILLAGER));
        } else {
            villagerSettings = VillagerSettings.DEFAULT;
        }
        if (tag.contains(KEY_OFFER_ITEM)) {
            offerItemSettings = OfferItemSettings.load(tag.getCompound(KEY_OFFER_ITEM));
        } else {
            offerItemSettings = OfferItemSettings.DEFAULT;
        }
        if (tag.contains(KEY_IO)) {
            ioSettings = IoSettings.load(tag.getCompound(KEY_IO));
        } else {
            ioSettings = IoSettings.DEFAULT;
        }
        if (tag.contains(KEY_ACCESS)) {
            accessSettings = AccessSettings.load(tag.getCompound(KEY_ACCESS));
        } else {
            accessSettings = AccessSettings.DEFAULT;
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
