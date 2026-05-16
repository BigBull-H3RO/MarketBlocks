package de.bigbull.marketblocks.feature.visual.npc;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShopVisualSettings(
        boolean npcEnabled,
        String npcName,
        VillagerVisualProfession profession,
        boolean purchaseParticlesEnabled,
        boolean purchaseSoundsEnabled,
        boolean paymentSlotSoundsEnabled,
        boolean offerItemVisible,
        boolean offerItemFullbright,
        float offerItemScale,
        float offerItemSpeed,
        float offerItemHeightOffset,
        boolean offerItemBobbing,
        int offerItemCount,
        float offerItemRotation,
        boolean offerItemChaos,
        float offerItemSpread
) {
    private static final int MAX_NPC_NAME_LENGTH = 32;
    private static final float MIN_OFFER_ITEM_SCALE = 0.1f;
    private static final float MAX_OFFER_ITEM_SCALE = 4.0f;
    private static final float MIN_OFFER_ITEM_SPEED = 0.0f;
    private static final float MAX_OFFER_ITEM_SPEED = 20.0f;
    private static final float MIN_OFFER_ITEM_HEIGHT = -2.0f;
    private static final float MAX_OFFER_ITEM_HEIGHT = 4.0f;
    private static final int MIN_OFFER_ITEM_COUNT = 1;
    private static final int MAX_OFFER_ITEM_COUNT = 64;
    private static final float MIN_OFFER_ITEM_SPREAD = 0.0f;
    private static final float MAX_OFFER_ITEM_SPREAD = 4.0f;
    private static final float DEFAULT_OFFER_ITEM_SCALE = 1.0f;
    private static final float DEFAULT_OFFER_ITEM_SPEED = 2.0f;
    private static final float DEFAULT_OFFER_ITEM_HEIGHT = 0.0f;
    private static final int DEFAULT_OFFER_ITEM_COUNT = 1;
    private static final float DEFAULT_OFFER_ITEM_ROTATION = 0.0f;
    private static final float DEFAULT_OFFER_ITEM_SPREAD = 0.2f;

    private static final String KEY_NPC_ENABLED = "NpcEnabled";
    private static final String KEY_NPC_NAME = "NpcName";
    private static final String KEY_PROFESSION = "NpcProfession";
    private static final String KEY_PURCHASE_PARTICLES = "PurchaseParticles";
    private static final String KEY_PURCHASE_SOUNDS = "PurchaseSounds";
    private static final String KEY_PAYMENT_SLOT_SOUNDS = "PaymentSlotSounds";

    private static final String KEY_OFFER_ITEM_VISIBLE = "OfferItemVisible";
    private static final String KEY_OFFER_ITEM_FULLBRIGHT = "OfferItemFullbright";
    private static final String KEY_OFFER_ITEM_SCALE = "OfferItemScale";
    private static final String KEY_OFFER_ITEM_SPEED = "OfferItemSpeed";
    private static final String KEY_OFFER_ITEM_HEIGHT = "OfferItemHeight";
    private static final String KEY_OFFER_ITEM_BOBBING = "OfferItemBobbing";
    private static final String KEY_OFFER_ITEM_COUNT = "OfferItemCount";
    private static final String KEY_OFFER_ITEM_ROTATION = "OfferItemRotation";
    private static final String KEY_OFFER_ITEM_CHAOS = "OfferItemChaos";
    private static final String KEY_OFFER_ITEM_SPREAD = "OfferItemSpread";

    public static final ShopVisualSettings DEFAULT = new ShopVisualSettings(
            false, "", VillagerVisualProfession.NONE, true, true, true,
            true, false, DEFAULT_OFFER_ITEM_SCALE, DEFAULT_OFFER_ITEM_SPEED, DEFAULT_OFFER_ITEM_HEIGHT,
            true, DEFAULT_OFFER_ITEM_COUNT, DEFAULT_OFFER_ITEM_ROTATION, false, DEFAULT_OFFER_ITEM_SPREAD
    );

    public static final StreamCodec<ByteBuf, ShopVisualSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                ByteBufCodecs.BOOL.encode(buf, settings.npcEnabled());
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.npcName());
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.profession().serializedName());
                ByteBufCodecs.BOOL.encode(buf, settings.purchaseParticlesEnabled());
                ByteBufCodecs.BOOL.encode(buf, settings.purchaseSoundsEnabled());
                ByteBufCodecs.BOOL.encode(buf, settings.paymentSlotSoundsEnabled());

                ByteBufCodecs.BOOL.encode(buf, settings.offerItemVisible());
                ByteBufCodecs.BOOL.encode(buf, settings.offerItemFullbright());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemScale());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemSpeed());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemHeightOffset());
                ByteBufCodecs.BOOL.encode(buf, settings.offerItemBobbing());
                ByteBufCodecs.INT.encode(buf, settings.offerItemCount());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemRotation());
                ByteBufCodecs.BOOL.encode(buf, settings.offerItemChaos());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemSpread());
            },
            buf -> new ShopVisualSettings(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    VillagerVisualProfession.fromSerialized(ByteBufCodecs.STRING_UTF8.decode(buf)),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),

                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf)
            )
    );

    public ShopVisualSettings {
        npcName = sanitizeNpcName(npcName);
        profession = profession == null ? VillagerVisualProfession.NONE : profession;
        offerItemScale = clampFinite(offerItemScale, MIN_OFFER_ITEM_SCALE, MAX_OFFER_ITEM_SCALE, DEFAULT_OFFER_ITEM_SCALE);
        offerItemSpeed = clampFinite(offerItemSpeed, MIN_OFFER_ITEM_SPEED, MAX_OFFER_ITEM_SPEED, DEFAULT_OFFER_ITEM_SPEED);
        offerItemHeightOffset = clampFinite(offerItemHeightOffset, MIN_OFFER_ITEM_HEIGHT, MAX_OFFER_ITEM_HEIGHT, DEFAULT_OFFER_ITEM_HEIGHT);
        offerItemCount = Math.max(MIN_OFFER_ITEM_COUNT, Math.min(MAX_OFFER_ITEM_COUNT, offerItemCount));
        offerItemRotation = normalizeDegrees(offerItemRotation);
        offerItemSpread = clampFinite(offerItemSpread, MIN_OFFER_ITEM_SPREAD, MAX_OFFER_ITEM_SPREAD, DEFAULT_OFFER_ITEM_SPREAD);
    }

    public ShopVisualSettings withNpcEnabled(boolean enabled) {
        return new ShopVisualSettings(
                enabled, npcName, profession, purchaseParticlesEnabled, purchaseSoundsEnabled, paymentSlotSoundsEnabled,
                offerItemVisible, offerItemFullbright, offerItemScale, offerItemSpeed, offerItemHeightOffset,
                offerItemBobbing, offerItemCount, offerItemRotation, offerItemChaos, offerItemSpread
        );
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_NPC_ENABLED, npcEnabled);
        tag.putString(KEY_NPC_NAME, npcName);
        tag.putString(KEY_PROFESSION, profession.serializedName());
        tag.putBoolean(KEY_PURCHASE_PARTICLES, purchaseParticlesEnabled);
        tag.putBoolean(KEY_PURCHASE_SOUNDS, purchaseSoundsEnabled);
        tag.putBoolean(KEY_PAYMENT_SLOT_SOUNDS, paymentSlotSoundsEnabled);

        tag.putBoolean(KEY_OFFER_ITEM_VISIBLE, offerItemVisible);
        tag.putBoolean(KEY_OFFER_ITEM_FULLBRIGHT, offerItemFullbright);
        tag.putFloat(KEY_OFFER_ITEM_SCALE, offerItemScale);
        tag.putFloat(KEY_OFFER_ITEM_SPEED, offerItemSpeed);
        tag.putFloat(KEY_OFFER_ITEM_HEIGHT, offerItemHeightOffset);
        tag.putBoolean(KEY_OFFER_ITEM_BOBBING, offerItemBobbing);
        tag.putInt(KEY_OFFER_ITEM_COUNT, offerItemCount);
        tag.putFloat(KEY_OFFER_ITEM_ROTATION, offerItemRotation);
        tag.putBoolean(KEY_OFFER_ITEM_CHAOS, offerItemChaos);
        tag.putFloat(KEY_OFFER_ITEM_SPREAD, offerItemSpread);
        return tag;
    }

    public static ShopVisualSettings load(CompoundTag tag) {
        if (tag == null) {
            return DEFAULT;
        }
        return new ShopVisualSettings(
                tag.getBoolean(KEY_NPC_ENABLED),
                tag.getString(KEY_NPC_NAME),
                VillagerVisualProfession.fromSerialized(tag.getString(KEY_PROFESSION)),
                !tag.contains(KEY_PURCHASE_PARTICLES) || tag.getBoolean(KEY_PURCHASE_PARTICLES),
                !tag.contains(KEY_PURCHASE_SOUNDS) || tag.getBoolean(KEY_PURCHASE_SOUNDS),
                !tag.contains(KEY_PAYMENT_SLOT_SOUNDS) || tag.getBoolean(KEY_PAYMENT_SLOT_SOUNDS),

                !tag.contains(KEY_OFFER_ITEM_VISIBLE) || tag.getBoolean(KEY_OFFER_ITEM_VISIBLE),
                tag.getBoolean(KEY_OFFER_ITEM_FULLBRIGHT),
                tag.contains(KEY_OFFER_ITEM_SCALE) ? tag.getFloat(KEY_OFFER_ITEM_SCALE) : DEFAULT_OFFER_ITEM_SCALE,
                tag.contains(KEY_OFFER_ITEM_SPEED) ? tag.getFloat(KEY_OFFER_ITEM_SPEED) : DEFAULT_OFFER_ITEM_SPEED,
                tag.contains(KEY_OFFER_ITEM_HEIGHT) ? tag.getFloat(KEY_OFFER_ITEM_HEIGHT) : DEFAULT_OFFER_ITEM_HEIGHT,
                !tag.contains(KEY_OFFER_ITEM_BOBBING) || tag.getBoolean(KEY_OFFER_ITEM_BOBBING),
                tag.contains(KEY_OFFER_ITEM_COUNT) ? tag.getInt(KEY_OFFER_ITEM_COUNT) : DEFAULT_OFFER_ITEM_COUNT,
                tag.contains(KEY_OFFER_ITEM_ROTATION) ? tag.getFloat(KEY_OFFER_ITEM_ROTATION) : DEFAULT_OFFER_ITEM_ROTATION,
                tag.getBoolean(KEY_OFFER_ITEM_CHAOS),
                tag.contains(KEY_OFFER_ITEM_SPREAD) ? tag.getFloat(KEY_OFFER_ITEM_SPREAD) : DEFAULT_OFFER_ITEM_SPREAD
        );
    }

    private static float clampFinite(float value, float min, float max, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static float normalizeDegrees(float value) {
        if (!Float.isFinite(value)) {
            return DEFAULT_OFFER_ITEM_ROTATION;
        }
        float normalized = value % 360.0f;
        return normalized < 0.0f ? normalized + 360.0f : normalized;
    }

    public static String sanitizeNpcName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String sanitized = raw.strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
        if (sanitized.length() > MAX_NPC_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_NPC_NAME_LENGTH);
        }
        return sanitized;
    }
}

