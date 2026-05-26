package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcAnimationEvent;
import net.minecraft.nbt.CompoundTag;

public class ShopSettingsManager {
    private static final int MAX_SHOP_NAME_LENGTH = 32;

    private final SingleOfferShopBlockEntity blockEntity;
    private String shopName = "";
    private boolean emitRedstone = false;
    private boolean outputAlmostFull = false;
    private boolean outputFull = false;
    private boolean adminShopEnabled = false;
    private boolean purchaseXpFeedbackSound = true;
    private boolean globalOfferItemRenderingEnabled = true;
    private ShopVisualSettings visualSettings = ShopVisualSettings.DEFAULT;

    public ShopSettingsManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String name, boolean sync) {
        if (name.length() > MAX_SHOP_NAME_LENGTH) {
            name = name.substring(0, MAX_SHOP_NAME_LENGTH);
        }
        this.shopName = name;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    public boolean isEmitRedstone() {
        return emitRedstone;
    }

    public void setEmitRedstone(boolean emitRedstone, boolean sync) {
        this.emitRedstone = emitRedstone;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    public boolean isAdminShopEnabled() {
        return adminShopEnabled;
    }

    public void setAdminShopEnabled(boolean enabled) {
        this.adminShopEnabled = enabled;
        blockEntity.setChanged();
    }

    public boolean isGlobalOfferItemRenderingEnabled() {
        return globalOfferItemRenderingEnabled;
    }

    public void setGlobalOfferItemRenderingEnabled(boolean enabled) {
        this.globalOfferItemRenderingEnabled = enabled;
        blockEntity.setChanged();
    }

    public boolean isPurchaseXpFeedbackSound() {
        return purchaseXpFeedbackSound;
    }

    public void setPurchaseXpFeedbackSound(boolean enabled, boolean sync) {
        this.purchaseXpFeedbackSound = enabled;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
    }

    public ShopVisualSettings getVisualSettings() {
        return visualSettings;
    }

    public void setVisualSettings(ShopVisualSettings visualSettings, boolean sync) {
        ShopVisualSettings previous = this.visualSettings;
        this.visualSettings = visualSettings == null ? ShopVisualSettings.DEFAULT : visualSettings;
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) return;
        if (previous.npcEnabled() != this.visualSettings.npcEnabled()) {
            blockEntity.triggerNpcAnimationEvent(this.visualSettings.npcEnabled() ? VisualNpcAnimationEvent.SPAWN : VisualNpcAnimationEvent.DESPAWN);
        }
        blockEntity.setChanged();
        if (sync) blockEntity.sync();
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
        tag.putString("ShopName", shopName);
        tag.putBoolean("EmitRedstone", emitRedstone);
        tag.putBoolean("AdminShopEnabled", adminShopEnabled);
        tag.putBoolean("PurchaseXpFeedbackSound", purchaseXpFeedbackSound);
        tag.putBoolean("GlobalOfferItemRendering", globalOfferItemRenderingEnabled);
        tag.put("Visuals", visualSettings.save());
        if (Config.ENABLE_OUTPUT_WARNING.get()) {
            tag.putBoolean("OutputWarning", outputAlmostFull);
        }
        tag.putBoolean("OutputFull", outputFull);
    }

    public void load(CompoundTag tag) {
        shopName = tag.getString("ShopName");
        emitRedstone = tag.getBoolean("EmitRedstone");
        adminShopEnabled = tag.getBoolean("AdminShopEnabled");
        if (tag.contains("PurchaseXpFeedbackSound")) {
            purchaseXpFeedbackSound = tag.getBoolean("PurchaseXpFeedbackSound");
        } else {
            purchaseXpFeedbackSound = true;
        }
        if (tag.contains("GlobalOfferItemRendering")) {
            globalOfferItemRenderingEnabled = tag.getBoolean("GlobalOfferItemRendering");
        } else {
            globalOfferItemRenderingEnabled = true;
        }
        if (tag.contains("Visuals")) {
            visualSettings = ShopVisualSettings.load(tag.getCompound("Visuals"));
        } else {
            visualSettings = ShopVisualSettings.DEFAULT;
        }
        outputAlmostFull = tag.getBoolean("OutputWarning");
        outputFull = tag.getBoolean("OutputFull");
    }
}
