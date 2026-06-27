package de.bigbull.marketblocks.feature.singleoffer.settings;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import de.bigbull.marketblocks.util.NameValidator;

/**
 * Settings for the General tab: shop name, redstone emission, and XP feedback
 * sound.
 */
public record GeneralSettings(
        String shopName,
        boolean emitRedstone,
        boolean purchaseXpFeedbackSound,
        boolean isClosed,
        ShopCategory shopCategory) {
    private static final int MAX_SHOP_NAME_LENGTH = 32;

    private static final String KEY_SHOP_NAME = "ShopName";
    private static final String KEY_EMIT_REDSTONE = "EmitRedstone";
    private static final String KEY_PURCHASE_XP_FEEDBACK_SOUND = "PurchaseXpFeedbackSound";
    private static final String KEY_IS_CLOSED = "IsClosed";
    private static final String KEY_SHOP_CATEGORY = "ShopCategory";

    public static final GeneralSettings DEFAULT = new GeneralSettings("", false, false, false, ShopCategory.NONE);

    public static final StreamCodec<ByteBuf, GeneralSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.shopName());
                ByteBufCodecs.BOOL.encode(buf, settings.emitRedstone());
                ByteBufCodecs.BOOL.encode(buf, settings.purchaseXpFeedbackSound());
                ByteBufCodecs.BOOL.encode(buf, settings.isClosed());
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.shopCategory().getId());
            },
            buf -> new GeneralSettings(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ShopCategory.fromId(ByteBufCodecs.STRING_UTF8.decode(buf))));

    public GeneralSettings {
        shopName = sanitizeName(shopName);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_SHOP_NAME, shopName);
        tag.putBoolean(KEY_EMIT_REDSTONE, emitRedstone);
        tag.putBoolean(KEY_PURCHASE_XP_FEEDBACK_SOUND, purchaseXpFeedbackSound);
        tag.putBoolean(KEY_IS_CLOSED, isClosed);
        tag.putString(KEY_SHOP_CATEGORY, shopCategory.getId());
        return tag;
    }

    public static GeneralSettings load(CompoundTag tag) {
        if (tag == null)
            return DEFAULT;
        return new GeneralSettings(
                tag.getString(KEY_SHOP_NAME),
                tag.getBoolean(KEY_EMIT_REDSTONE),
                !tag.contains(KEY_PURCHASE_XP_FEEDBACK_SOUND) || tag.getBoolean(KEY_PURCHASE_XP_FEEDBACK_SOUND),
                tag.getBoolean(KEY_IS_CLOSED),
                tag.contains(KEY_SHOP_CATEGORY) ? ShopCategory.fromId(tag.getString(KEY_SHOP_CATEGORY)) : ShopCategory.NONE);
    }

    private static String sanitizeName(String raw) {
        return NameValidator.sanitizeName(raw);
    }

    /**
     * Mutable draft for the General settings tab in the GUI.
     */
    public static final class Draft {
        private String shopName;
        private boolean emitRedstone;
        private boolean purchaseXpFeedbackSound;
        private boolean isClosed;
        private ShopCategory shopCategory;

        public Draft(GeneralSettings settings) {
            GeneralSettings s = settings == null ? DEFAULT : settings;
            this.shopName = s.shopName();
            this.emitRedstone = s.emitRedstone();
            this.purchaseXpFeedbackSound = s.purchaseXpFeedbackSound();
            this.isClosed = s.isClosed();
            this.shopCategory = s.shopCategory();
        }

        public String shopName() {
            return shopName;
        }

        public Draft setShopName(String shopName) {
            this.shopName = shopName == null ? "" : shopName;
            return this;
        }

        public boolean emitRedstone() {
            return emitRedstone;
        }

        public Draft setEmitRedstone(boolean emitRedstone) {
            this.emitRedstone = emitRedstone;
            return this;
        }

        public boolean purchaseXpFeedbackSound() {
            return purchaseXpFeedbackSound;
        }

        public Draft setPurchaseXpFeedbackSound(boolean v) {
            this.purchaseXpFeedbackSound = v;
            return this;
        }

        public boolean isClosed() {
            return isClosed;
        }

        public Draft setIsClosed(boolean isClosed) {
            this.isClosed = isClosed;
            return this;
        }

        public ShopCategory shopCategory() {
            return shopCategory;
        }

        public Draft setShopCategory(ShopCategory shopCategory) {
            this.shopCategory = shopCategory;
            return this;
        }

        public GeneralSettings toSettings() {
            return new GeneralSettings(shopName, emitRedstone, purchaseXpFeedbackSound, isClosed, shopCategory);
        }
    }
}
