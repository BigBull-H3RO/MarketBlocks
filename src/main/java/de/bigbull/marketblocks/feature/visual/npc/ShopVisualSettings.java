package de.bigbull.marketblocks.feature.visual.npc;

import net.minecraft.nbt.CompoundTag;

public record ShopVisualSettings(
        boolean npcEnabled,
        String npcName,
        VillagerVisualProfession profession,
        boolean purchaseParticlesEnabled,
        boolean purchaseSoundsEnabled,
        boolean paymentSlotSoundsEnabled,
        boolean offerItemVisualizationEnabled,
        float tradeStandOfferScaleMultiplier,
        float tradeStandOfferRotationSpeed,
        float tradeStandOfferHeightOffset,
        int marketCrateDisplayCount,
        float marketCrateOfferHeightOffset,
        float marketCrateOfferRotationSpeed,
        boolean marketCrateRandomPlacement,
        boolean marketCrateStableRandom
) {
    private static final int MAX_NPC_NAME_LENGTH = 32;
    private static final int MIN_MARKET_CRATE_DISPLAY_COUNT = 1;
    private static final int MAX_MARKET_CRATE_DISPLAY_COUNT = 12;
    private static final float MIN_TRADE_STAND_OFFER_SCALE_MULTIPLIER = 0.60F;
    private static final float MAX_TRADE_STAND_OFFER_SCALE_MULTIPLIER = 1.40F;
    private static final float MIN_OFFER_ROTATION_SPEED = 0.0F;
    private static final float MAX_OFFER_ROTATION_SPEED = 12.0F;
    private static final float MIN_OFFER_HEIGHT_OFFSET = -0.35F;
    private static final float MAX_OFFER_HEIGHT_OFFSET = 0.35F;

    private static final String KEY_NPC_ENABLED = "NpcEnabled";
    private static final String KEY_NPC_NAME = "NpcName";
    private static final String KEY_PROFESSION = "NpcProfession";
    private static final String KEY_PURCHASE_PARTICLES = "PurchaseParticles";
    private static final String KEY_PURCHASE_SOUNDS = "PurchaseSounds";
    private static final String KEY_PAYMENT_SLOT_SOUNDS = "PaymentSlotSounds";
    private static final String KEY_OFFER_ITEM_VISUALIZATION_ENABLED = "OfferItemVisualizationEnabled";
    private static final String KEY_TRADE_STAND_OFFER_SCALE_MULTIPLIER = "TradeStandOfferScaleMultiplier";
    private static final String KEY_TRADE_STAND_OFFER_ROTATION_SPEED = "TradeStandOfferRotationSpeed";
    private static final String KEY_TRADE_STAND_OFFER_HEIGHT_OFFSET = "TradeStandOfferHeightOffset";
    private static final String KEY_MARKET_CRATE_DISPLAY_COUNT = "MarketCrateDisplayCount";
    private static final String KEY_MARKET_CRATE_OFFER_HEIGHT_OFFSET = "MarketCrateOfferHeightOffset";
    private static final String KEY_MARKET_CRATE_OFFER_ROTATION_SPEED = "MarketCrateOfferRotationSpeed";
    private static final String KEY_MARKET_CRATE_RANDOM_PLACEMENT = "MarketCrateRandomPlacement";
    private static final String KEY_MARKET_CRATE_STABLE_RANDOM = "MarketCrateStableRandom";

    public static final boolean DEFAULT_OFFER_ITEM_VISUALIZATION_ENABLED = true;
    public static final float DEFAULT_TRADE_STAND_OFFER_SCALE_MULTIPLIER = 1.0F;
    public static final float DEFAULT_TRADE_STAND_OFFER_ROTATION_SPEED = 2.0F;
    public static final float DEFAULT_TRADE_STAND_OFFER_HEIGHT_OFFSET = 0.0F;
    public static final int DEFAULT_MARKET_CRATE_DISPLAY_COUNT = 6;
    public static final float DEFAULT_MARKET_CRATE_OFFER_HEIGHT_OFFSET = 0.0F;
    public static final float DEFAULT_MARKET_CRATE_OFFER_ROTATION_SPEED = 0.0F;
    public static final boolean DEFAULT_MARKET_CRATE_RANDOM_PLACEMENT = true;
    public static final boolean DEFAULT_MARKET_CRATE_STABLE_RANDOM = true;

    public static final ShopVisualSettings DEFAULT = new ShopVisualSettings(
            false, "", VillagerVisualProfession.NONE, true, true, true,
            DEFAULT_OFFER_ITEM_VISUALIZATION_ENABLED,
            DEFAULT_TRADE_STAND_OFFER_SCALE_MULTIPLIER,
            DEFAULT_TRADE_STAND_OFFER_ROTATION_SPEED,
            DEFAULT_TRADE_STAND_OFFER_HEIGHT_OFFSET,
            DEFAULT_MARKET_CRATE_DISPLAY_COUNT,
            DEFAULT_MARKET_CRATE_OFFER_HEIGHT_OFFSET,
            DEFAULT_MARKET_CRATE_OFFER_ROTATION_SPEED,
            DEFAULT_MARKET_CRATE_RANDOM_PLACEMENT,
            DEFAULT_MARKET_CRATE_STABLE_RANDOM
    );

    public ShopVisualSettings {
        npcName = sanitizeNpcName(npcName);
        profession = profession == null ? VillagerVisualProfession.NONE : profession;
        tradeStandOfferScaleMultiplier = clampTradeStandOfferScaleMultiplier(tradeStandOfferScaleMultiplier);
        tradeStandOfferRotationSpeed = clampOfferRotationSpeed(tradeStandOfferRotationSpeed);
        tradeStandOfferHeightOffset = clampOfferHeightOffset(tradeStandOfferHeightOffset);
        marketCrateDisplayCount = clampMarketCrateDisplayCount(marketCrateDisplayCount);
        marketCrateOfferHeightOffset = clampOfferHeightOffset(marketCrateOfferHeightOffset);
        marketCrateOfferRotationSpeed = clampOfferRotationSpeed(marketCrateOfferRotationSpeed);
    }

    public ShopVisualSettings withNpcEnabled(boolean enabled) {
        return new ShopVisualSettings(
                enabled, npcName, profession, purchaseParticlesEnabled, purchaseSoundsEnabled, paymentSlotSoundsEnabled,
                offerItemVisualizationEnabled, tradeStandOfferScaleMultiplier, tradeStandOfferRotationSpeed, tradeStandOfferHeightOffset,
                marketCrateDisplayCount, marketCrateOfferHeightOffset, marketCrateOfferRotationSpeed,
                marketCrateRandomPlacement, marketCrateStableRandom
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
        tag.putBoolean(KEY_OFFER_ITEM_VISUALIZATION_ENABLED, offerItemVisualizationEnabled);
        tag.putFloat(KEY_TRADE_STAND_OFFER_SCALE_MULTIPLIER, tradeStandOfferScaleMultiplier);
        tag.putFloat(KEY_TRADE_STAND_OFFER_ROTATION_SPEED, tradeStandOfferRotationSpeed);
        tag.putFloat(KEY_TRADE_STAND_OFFER_HEIGHT_OFFSET, tradeStandOfferHeightOffset);
        tag.putInt(KEY_MARKET_CRATE_DISPLAY_COUNT, marketCrateDisplayCount);
        tag.putFloat(KEY_MARKET_CRATE_OFFER_HEIGHT_OFFSET, marketCrateOfferHeightOffset);
        tag.putFloat(KEY_MARKET_CRATE_OFFER_ROTATION_SPEED, marketCrateOfferRotationSpeed);
        tag.putBoolean(KEY_MARKET_CRATE_RANDOM_PLACEMENT, marketCrateRandomPlacement);
        tag.putBoolean(KEY_MARKET_CRATE_STABLE_RANDOM, marketCrateStableRandom);
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
                !tag.contains(KEY_OFFER_ITEM_VISUALIZATION_ENABLED) || tag.getBoolean(KEY_OFFER_ITEM_VISUALIZATION_ENABLED),
                tag.contains(KEY_TRADE_STAND_OFFER_SCALE_MULTIPLIER) ? tag.getFloat(KEY_TRADE_STAND_OFFER_SCALE_MULTIPLIER) : DEFAULT_TRADE_STAND_OFFER_SCALE_MULTIPLIER,
                tag.contains(KEY_TRADE_STAND_OFFER_ROTATION_SPEED) ? tag.getFloat(KEY_TRADE_STAND_OFFER_ROTATION_SPEED) : DEFAULT_TRADE_STAND_OFFER_ROTATION_SPEED,
                tag.contains(KEY_TRADE_STAND_OFFER_HEIGHT_OFFSET) ? tag.getFloat(KEY_TRADE_STAND_OFFER_HEIGHT_OFFSET) : DEFAULT_TRADE_STAND_OFFER_HEIGHT_OFFSET,
                tag.contains(KEY_MARKET_CRATE_DISPLAY_COUNT) ? tag.getInt(KEY_MARKET_CRATE_DISPLAY_COUNT) : DEFAULT_MARKET_CRATE_DISPLAY_COUNT,
                tag.contains(KEY_MARKET_CRATE_OFFER_HEIGHT_OFFSET) ? tag.getFloat(KEY_MARKET_CRATE_OFFER_HEIGHT_OFFSET) : DEFAULT_MARKET_CRATE_OFFER_HEIGHT_OFFSET,
                tag.contains(KEY_MARKET_CRATE_OFFER_ROTATION_SPEED) ? tag.getFloat(KEY_MARKET_CRATE_OFFER_ROTATION_SPEED) : DEFAULT_MARKET_CRATE_OFFER_ROTATION_SPEED,
                !tag.contains(KEY_MARKET_CRATE_RANDOM_PLACEMENT) || tag.getBoolean(KEY_MARKET_CRATE_RANDOM_PLACEMENT),
                !tag.contains(KEY_MARKET_CRATE_STABLE_RANDOM) || tag.getBoolean(KEY_MARKET_CRATE_STABLE_RANDOM)
        );
    }

    public static int clampMarketCrateDisplayCount(int raw) {
        return Math.max(MIN_MARKET_CRATE_DISPLAY_COUNT, Math.min(MAX_MARKET_CRATE_DISPLAY_COUNT, raw));
    }

    public static float clampTradeStandOfferScaleMultiplier(float raw) {
        return Math.max(MIN_TRADE_STAND_OFFER_SCALE_MULTIPLIER, Math.min(MAX_TRADE_STAND_OFFER_SCALE_MULTIPLIER, raw));
    }

    public static float clampOfferRotationSpeed(float raw) {
        return Math.max(MIN_OFFER_ROTATION_SPEED, Math.min(MAX_OFFER_ROTATION_SPEED, raw));
    }

    public static float clampOfferHeightOffset(float raw) {
        return Math.max(MIN_OFFER_HEIGHT_OFFSET, Math.min(MAX_OFFER_HEIGHT_OFFSET, raw));
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
