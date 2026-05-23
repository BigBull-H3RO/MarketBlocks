package de.bigbull.marketblocks.feature.visual.npc;

import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
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
        CrateLayoutMode offerItemLayoutMode,
        float offerItemSpacingXZ,
        float offerItemSpacingY,
        float offerItemChaosRotation,
        boolean dynamicFillLevel
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
    // rotation bounds removed (unused) - rotation normalization uses modulo logic
    private static final float MIN_OFFER_ITEM_SPACING = -0.5f;
    private static final float MAX_OFFER_ITEM_SPACING = 2.0f;
    private static final float MIN_OFFER_ITEM_CHAOS_ROTATION = 0.0f;
    private static final float MAX_OFFER_ITEM_CHAOS_ROTATION = 1.0f;
    private static final float DEFAULT_OFFER_ITEM_SCALE = 1.0f;
    private static final float DEFAULT_OFFER_ITEM_SPEED = 2.0f;
    private static final float DEFAULT_OFFER_ITEM_HEIGHT = 0.0f;
    private static final int DEFAULT_OFFER_ITEM_COUNT = 1;
    private static final float DEFAULT_OFFER_ITEM_ROTATION = 0.0f;
    private static final CrateLayoutMode DEFAULT_OFFER_ITEM_LAYOUT_MODE = CrateLayoutMode.LOSE;
    private static final float DEFAULT_OFFER_ITEM_SPACING_XZ = 0.1f;
    private static final float DEFAULT_OFFER_ITEM_SPACING_Y = 0.1f;
    private static final float DEFAULT_OFFER_ITEM_CHAOS_ROTATION = 0.1f;
    private static final boolean DEFAULT_DYNAMIC_FILL_LEVEL = false;

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
    private static final String KEY_OFFER_ITEM_LAYOUT_MODE = "OfferItemLayoutMode";
    private static final String KEY_OFFER_ITEM_SPACING_XZ = "OfferItemSpacingXZ";
    private static final String KEY_OFFER_ITEM_SPACING_Y = "OfferItemSpacingY";
    private static final String KEY_OFFER_ITEM_CHAOS_ROTATION = "OfferItemChaosRotation";
    private static final String KEY_DYNAMIC_FILL_LEVEL = "DynamicFillLevel";

    public static final ShopVisualSettings DEFAULT = new ShopVisualSettings(
            false, "", VillagerVisualProfession.NONE, true, true, true,
            true, false, DEFAULT_OFFER_ITEM_SCALE, DEFAULT_OFFER_ITEM_SPEED, DEFAULT_OFFER_ITEM_HEIGHT,
            true, DEFAULT_OFFER_ITEM_COUNT, DEFAULT_OFFER_ITEM_ROTATION, DEFAULT_OFFER_ITEM_LAYOUT_MODE,
            DEFAULT_OFFER_ITEM_SPACING_XZ, DEFAULT_OFFER_ITEM_SPACING_Y, DEFAULT_OFFER_ITEM_CHAOS_ROTATION, DEFAULT_DYNAMIC_FILL_LEVEL
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
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.offerItemLayoutMode().serializedName());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemSpacingXZ());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemSpacingY());
                ByteBufCodecs.FLOAT.encode(buf, settings.offerItemChaosRotation());
                ByteBufCodecs.BOOL.encode(buf, settings.dynamicFillLevel());
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
                    CrateLayoutMode.fromSerialized(ByteBufCodecs.STRING_UTF8.decode(buf)),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    public ShopVisualSettings {
        npcName = sanitizeNpcName(npcName);
        profession = profession == null ? VillagerVisualProfession.NONE : profession;
        offerItemScale = clampFinite(offerItemScale, MIN_OFFER_ITEM_SCALE, MAX_OFFER_ITEM_SCALE, DEFAULT_OFFER_ITEM_SCALE);
        offerItemSpeed = clampFinite(offerItemSpeed, MIN_OFFER_ITEM_SPEED, MAX_OFFER_ITEM_SPEED, DEFAULT_OFFER_ITEM_SPEED);
        offerItemHeightOffset = clampFinite(offerItemHeightOffset, MIN_OFFER_ITEM_HEIGHT, MAX_OFFER_ITEM_HEIGHT, DEFAULT_OFFER_ITEM_HEIGHT);
        offerItemCount = Math.clamp(offerItemCount, MIN_OFFER_ITEM_COUNT, MAX_OFFER_ITEM_COUNT);
        offerItemRotation = normalizeDegrees(offerItemRotation);
        offerItemLayoutMode = offerItemLayoutMode == null ? DEFAULT_OFFER_ITEM_LAYOUT_MODE : offerItemLayoutMode;
        offerItemSpacingXZ = clampFinite(offerItemSpacingXZ, MIN_OFFER_ITEM_SPACING, MAX_OFFER_ITEM_SPACING, DEFAULT_OFFER_ITEM_SPACING_XZ);
        offerItemSpacingY = clampFinite(offerItemSpacingY, MIN_OFFER_ITEM_SPACING, MAX_OFFER_ITEM_SPACING, DEFAULT_OFFER_ITEM_SPACING_Y);
        offerItemChaosRotation = clampFinite(offerItemChaosRotation, MIN_OFFER_ITEM_CHAOS_ROTATION, MAX_OFFER_ITEM_CHAOS_ROTATION, DEFAULT_OFFER_ITEM_CHAOS_ROTATION);
    }

    public Draft toDraft(String shopName, boolean emitRedstoneEnabled, boolean purchaseXpFeedbackSound) {
        return new Draft(shopName, emitRedstoneEnabled, purchaseXpFeedbackSound, this);
    }

    public ShopVisualSettings withNpcEnabled(boolean enabled) {
        return new ShopVisualSettings(
                enabled, npcName, profession, purchaseParticlesEnabled, purchaseSoundsEnabled, paymentSlotSoundsEnabled,
                offerItemVisible, offerItemFullbright, offerItemScale, offerItemSpeed, offerItemHeightOffset,
                offerItemBobbing, offerItemCount, offerItemRotation, offerItemLayoutMode, offerItemSpacingXZ, offerItemSpacingY, offerItemChaosRotation, dynamicFillLevel
        );
    }

    public static final class Draft {
        private String shopName;
        private boolean emitRedstoneEnabled;
        private boolean purchaseXpFeedbackSound;

        private boolean npcEnabled;
        private String npcName;
        private VillagerVisualProfession profession;
        private boolean purchaseParticlesEnabled;
        private boolean purchaseSoundsEnabled;
        private boolean paymentSlotSoundsEnabled;

        private boolean offerItemVisible;
        private boolean offerItemFullbright;
        private float offerItemScale;
        private float offerItemSpeed;
        private float offerItemHeightOffset;
        private boolean offerItemBobbing;
        private int offerItemCount;
        private float offerItemRotation;
        private CrateLayoutMode offerItemLayoutMode;
        private float offerItemSpacingXZ;
        private float offerItemSpacingY;
        private float offerItemChaosRotation;
        private boolean dynamicFillLevel;

        private Draft(String shopName, boolean emitRedstoneEnabled, boolean purchaseXpFeedbackSound, ShopVisualSettings visualSettings) {
            this.shopName = shopName == null ? "" : shopName;
            this.emitRedstoneEnabled = emitRedstoneEnabled;
            this.purchaseXpFeedbackSound = purchaseXpFeedbackSound;
            applyVisualSettings(visualSettings == null ? DEFAULT : visualSettings);
        }

        public String shopName() {
            return shopName;
        }

        public Draft setShopName(String shopName) {
            this.shopName = shopName == null ? "" : shopName;
            return this;
        }

        public boolean emitRedstoneEnabled() {
            return emitRedstoneEnabled;
        }

        public Draft setEmitRedstoneEnabled(boolean emitRedstoneEnabled) {
            this.emitRedstoneEnabled = emitRedstoneEnabled;
            return this;
        }

        public boolean purchaseXpFeedbackSound() {
            return purchaseXpFeedbackSound;
        }

        public Draft setPurchaseXpFeedbackSound(boolean purchaseXpFeedbackSound) {
            this.purchaseXpFeedbackSound = purchaseXpFeedbackSound;
            return this;
        }

        public boolean npcEnabled() {
            return npcEnabled;
        }

        public Draft setNpcEnabled(boolean npcEnabled) {
            this.npcEnabled = npcEnabled;
            return this;
        }

        public Draft toggleNpcEnabled() {
            this.npcEnabled = !this.npcEnabled;
            return this;
        }

        public String npcName() {
            return npcName;
        }

        public Draft setNpcName(String npcName) {
            this.npcName = sanitizeNpcName(npcName);
            return this;
        }

        public VillagerVisualProfession profession() {
            return profession;
        }

        public Draft setProfession(VillagerVisualProfession profession) {
            this.profession = profession == null ? VillagerVisualProfession.NONE : profession;
            return this;
        }

        public Draft cycleProfession() {
            this.profession = (this.profession == null ? VillagerVisualProfession.NONE : this.profession).next();
            return this;
        }

        public boolean purchaseParticlesEnabled() {
            return purchaseParticlesEnabled;
        }

        public Draft setPurchaseParticlesEnabled(boolean purchaseParticlesEnabled) {
            this.purchaseParticlesEnabled = purchaseParticlesEnabled;
            return this;
        }

        public boolean purchaseSoundsEnabled() {
            return purchaseSoundsEnabled;
        }

        public Draft setPurchaseSoundsEnabled(boolean purchaseSoundsEnabled) {
            this.purchaseSoundsEnabled = purchaseSoundsEnabled;
            return this;
        }

        public boolean paymentSlotSoundsEnabled() {
            return paymentSlotSoundsEnabled;
        }

        public Draft setPaymentSlotSoundsEnabled(boolean paymentSlotSoundsEnabled) {
            this.paymentSlotSoundsEnabled = paymentSlotSoundsEnabled;
            return this;
        }

        public boolean offerItemVisible() {
            return offerItemVisible;
        }

        public Draft setOfferItemVisible(boolean offerItemVisible) {
            this.offerItemVisible = offerItemVisible;
            return this;
        }

        public boolean offerItemFullbright() {
            return offerItemFullbright;
        }

        public Draft setOfferItemFullbright(boolean offerItemFullbright) {
            this.offerItemFullbright = offerItemFullbright;
            return this;
        }

        public float offerItemScale() {
            return offerItemScale;
        }

        public Draft setOfferItemScale(float offerItemScale) {
            this.offerItemScale = offerItemScale;
            return this;
        }

        public float offerItemSpeed() {
            return offerItemSpeed;
        }

        public Draft setOfferItemSpeed(float offerItemSpeed) {
            this.offerItemSpeed = offerItemSpeed;
            return this;
        }

        public float offerItemHeightOffset() {
            return offerItemHeightOffset;
        }

        public Draft setOfferItemHeightOffset(float offerItemHeightOffset) {
            this.offerItemHeightOffset = offerItemHeightOffset;
            return this;
        }

        public boolean offerItemBobbing() {
            return offerItemBobbing;
        }

        public Draft setOfferItemBobbing(boolean offerItemBobbing) {
            this.offerItemBobbing = offerItemBobbing;
            return this;
        }

        public int offerItemCount() {
            return offerItemCount;
        }

        public Draft setOfferItemCount(int offerItemCount) {
            this.offerItemCount = offerItemCount;
            return this;
        }

        public float offerItemRotation() {
            return offerItemRotation;
        }

        public Draft setOfferItemRotation(float offerItemRotation) {
            this.offerItemRotation = offerItemRotation;
            return this;
        }

        public CrateLayoutMode offerItemLayoutMode() {
            return offerItemLayoutMode;
        }

        public Draft setOfferItemLayoutMode(CrateLayoutMode offerItemLayoutMode) {
            this.offerItemLayoutMode = offerItemLayoutMode == null ? DEFAULT_OFFER_ITEM_LAYOUT_MODE : offerItemLayoutMode;
            return this;
        }

        public float offerItemSpacingXZ() {
            return offerItemSpacingXZ;
        }

        public Draft setOfferItemSpacingXZ(float offerItemSpacingXZ) {
            this.offerItemSpacingXZ = offerItemSpacingXZ;
            return this;
        }

        public float offerItemSpacingY() {
            return offerItemSpacingY;
        }

        public Draft setOfferItemSpacingY(float offerItemSpacingY) {
            this.offerItemSpacingY = offerItemSpacingY;
            return this;
        }

        public float offerItemChaosRotation() {
            return offerItemChaosRotation;
        }

        public Draft setOfferItemChaosRotation(float offerItemChaosRotation) {
            this.offerItemChaosRotation = offerItemChaosRotation;
            return this;
        }

        public boolean dynamicFillLevel() {
            return dynamicFillLevel;
        }

        public Draft setDynamicFillLevel(boolean dynamicFillLevel) {
            this.dynamicFillLevel = dynamicFillLevel;
            return this;
        }

        public void applyVisualSettings(ShopVisualSettings visualSettings) {
            ShopVisualSettings settings = visualSettings == null ? DEFAULT : visualSettings;
            this.npcEnabled = settings.npcEnabled();
            this.npcName = settings.npcName();
            this.profession = settings.profession();
            this.purchaseParticlesEnabled = settings.purchaseParticlesEnabled();
            this.purchaseSoundsEnabled = settings.purchaseSoundsEnabled();
            this.paymentSlotSoundsEnabled = settings.paymentSlotSoundsEnabled();
            this.offerItemVisible = settings.offerItemVisible();
            this.offerItemFullbright = settings.offerItemFullbright();
            this.offerItemScale = settings.offerItemScale();
            this.offerItemSpeed = settings.offerItemSpeed();
            this.offerItemHeightOffset = settings.offerItemHeightOffset();
            this.offerItemBobbing = settings.offerItemBobbing();
            this.offerItemCount = settings.offerItemCount();
            this.offerItemRotation = settings.offerItemRotation();
            this.offerItemLayoutMode = settings.offerItemLayoutMode();
            this.offerItemSpacingXZ = settings.offerItemSpacingXZ();
            this.offerItemSpacingY = settings.offerItemSpacingY();
            this.offerItemChaosRotation = settings.offerItemChaosRotation();
            this.dynamicFillLevel = settings.dynamicFillLevel();
        }

        public ShopVisualSettings toVisualSettings() {
            return new ShopVisualSettings(
                    npcEnabled,
                    npcName,
                    profession,
                    purchaseParticlesEnabled,
                    purchaseSoundsEnabled,
                    paymentSlotSoundsEnabled,
                    offerItemVisible,
                    offerItemFullbright,
                    offerItemScale,
                    offerItemSpeed,
                    offerItemHeightOffset,
                    offerItemBobbing,
                    offerItemCount,
                    offerItemRotation,
                    offerItemLayoutMode,
                    offerItemSpacingXZ,
                    offerItemSpacingY,
                    offerItemChaosRotation,
                    dynamicFillLevel
            );
        }
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
        tag.putString(KEY_OFFER_ITEM_LAYOUT_MODE, offerItemLayoutMode.serializedName());
        tag.putFloat(KEY_OFFER_ITEM_SPACING_XZ, offerItemSpacingXZ);
        tag.putFloat(KEY_OFFER_ITEM_SPACING_Y, offerItemSpacingY);
        tag.putFloat(KEY_OFFER_ITEM_CHAOS_ROTATION, offerItemChaosRotation);
        tag.putBoolean(KEY_DYNAMIC_FILL_LEVEL, dynamicFillLevel);
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
                tag.contains(KEY_OFFER_ITEM_LAYOUT_MODE) ? CrateLayoutMode.fromSerialized(tag.getString(KEY_OFFER_ITEM_LAYOUT_MODE)) : DEFAULT_OFFER_ITEM_LAYOUT_MODE,
                tag.contains(KEY_OFFER_ITEM_SPACING_XZ) ? tag.getFloat(KEY_OFFER_ITEM_SPACING_XZ) : DEFAULT_OFFER_ITEM_SPACING_XZ,
                tag.contains(KEY_OFFER_ITEM_SPACING_Y) ? tag.getFloat(KEY_OFFER_ITEM_SPACING_Y) : DEFAULT_OFFER_ITEM_SPACING_Y,
                tag.contains(KEY_OFFER_ITEM_CHAOS_ROTATION) ? tag.getFloat(KEY_OFFER_ITEM_CHAOS_ROTATION) : DEFAULT_OFFER_ITEM_CHAOS_ROTATION,
                tag.contains(KEY_DYNAMIC_FILL_LEVEL) ? tag.getBoolean(KEY_DYNAMIC_FILL_LEVEL) : DEFAULT_DYNAMIC_FILL_LEVEL
        );
    }

    private static float clampFinite(float value, float min, float max, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return Math.clamp(value, min, max);
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