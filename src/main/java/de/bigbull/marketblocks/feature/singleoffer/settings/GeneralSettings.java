package de.bigbull.marketblocks.feature.singleoffer.settings;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Settings for the General tab: shop name, redstone emission, and XP feedback sound.
 */
public record GeneralSettings(
        String shopName,
        boolean emitRedstone,
        boolean purchaseXpFeedbackSound,
        boolean isClosed
) {
    private static final int MAX_SHOP_NAME_LENGTH = 32;

    private static final String KEY_SHOP_NAME = "ShopName";
    private static final String KEY_EMIT_REDSTONE = "EmitRedstone";
    private static final String KEY_PURCHASE_XP_FEEDBACK_SOUND = "PurchaseXpFeedbackSound";
    private static final String KEY_IS_CLOSED = "IsClosed";

    public static final GeneralSettings DEFAULT = new GeneralSettings("", false, true, false);

    public static final StreamCodec<ByteBuf, GeneralSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.shopName());
                ByteBufCodecs.BOOL.encode(buf, settings.emitRedstone());
                ByteBufCodecs.BOOL.encode(buf, settings.purchaseXpFeedbackSound());
                ByteBufCodecs.BOOL.encode(buf, settings.isClosed());
            },
            buf -> new GeneralSettings(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    public GeneralSettings {
        shopName = sanitizeName(shopName);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_SHOP_NAME, shopName);
        tag.putBoolean(KEY_EMIT_REDSTONE, emitRedstone);
        tag.putBoolean(KEY_PURCHASE_XP_FEEDBACK_SOUND, purchaseXpFeedbackSound);
        tag.putBoolean(KEY_IS_CLOSED, isClosed);
        return tag;
    }

    public static GeneralSettings load(CompoundTag tag) {
        if (tag == null) return DEFAULT;
        return new GeneralSettings(
                tag.getString(KEY_SHOP_NAME),
                tag.getBoolean(KEY_EMIT_REDSTONE),
                !tag.contains(KEY_PURCHASE_XP_FEEDBACK_SOUND) || tag.getBoolean(KEY_PURCHASE_XP_FEEDBACK_SOUND),
                tag.getBoolean(KEY_IS_CLOSED)
        );
    }

    private static String sanitizeName(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String sanitized = raw.strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
        if (sanitized.length() > MAX_SHOP_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_SHOP_NAME_LENGTH);
        }
        return sanitized;
    }

    /**
     * Mutable draft for the General settings tab in the GUI.
     */
    public static final class Draft {
        private String shopName;
        private boolean emitRedstone;
        private boolean purchaseXpFeedbackSound;
        private boolean isClosed;

        public Draft(GeneralSettings settings) {
            GeneralSettings s = settings == null ? DEFAULT : settings;
            this.shopName = s.shopName();
            this.emitRedstone = s.emitRedstone();
            this.purchaseXpFeedbackSound = s.purchaseXpFeedbackSound();
            this.isClosed = s.isClosed();
        }

        public String shopName() { return shopName; }
        public Draft setShopName(String shopName) { this.shopName = shopName == null ? "" : shopName; return this; }

        public boolean emitRedstone() { return emitRedstone; }
        public Draft setEmitRedstone(boolean emitRedstone) { this.emitRedstone = emitRedstone; return this; }

        public boolean purchaseXpFeedbackSound() { return purchaseXpFeedbackSound; }
        public Draft setPurchaseXpFeedbackSound(boolean v) { this.purchaseXpFeedbackSound = v; return this; }

        public boolean isClosed() { return isClosed; }
        public Draft setIsClosed(boolean isClosed) { this.isClosed = isClosed; return this; }

        public GeneralSettings toSettings() {
            return new GeneralSettings(shopName, emitRedstone, purchaseXpFeedbackSound, isClosed);
        }
    }
}
