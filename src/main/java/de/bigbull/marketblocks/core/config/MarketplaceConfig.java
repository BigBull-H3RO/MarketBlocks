package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.feature.marketplace.data.Volatility;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MarketplaceConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue MARKETPLACE_GLOBAL_DAILY_LIMIT;
    public static final ModConfigSpec.BooleanValue MARKETPLACE_GLOBAL_PRICING_ENABLED;
    public static final ModConfigSpec.IntValue MARKETPLACE_GLOBAL_PRICING_MIN_PERCENT;
    public static final ModConfigSpec.IntValue MARKETPLACE_GLOBAL_PRICING_MAX_PERCENT;
    public static final ModConfigSpec.EnumValue<Volatility> MARKETPLACE_GLOBAL_PRICING_VOLATILITY;
    public static final ModConfigSpec.BooleanValue MARKETPLACE_GLOBAL_LIMITS_ENABLED;
    public static final ModConfigSpec.IntValue MARKETPLACE_GLOBAL_LIMITS_DAILY_LIMIT;
    public static final ModConfigSpec.IntValue MARKETPLACE_GLOBAL_LIMITS_STOCK_LIMIT;
    public static final ModConfigSpec.IntValue MARKETPLACE_GLOBAL_LIMITS_RESTOCK_SECONDS;
    public static final ModConfigSpec.BooleanValue MARKETPLACE_BUYER_MESSAGE;
    public static final ModConfigSpec.BooleanValue MARKETPLACE_BUYER_MESSAGE_GLOBAL;

    static {
        BUILDER.push("Marketplace");
        MARKETPLACE_BUYER_MESSAGE = BUILDER
                .comment("Send chat message upon successful purchase at the Marketplace (Default: false)")
                .define("marketplaceBuyerMessage", false);
        MARKETPLACE_BUYER_MESSAGE_GLOBAL = BUILDER
                .comment("Broadcast the Marketplace purchase message globally to all players instead of just the buyer (Default: false)")
                .define("marketplaceBuyerMessageGlobal", false);
        MARKETPLACE_GLOBAL_DAILY_LIMIT = BUILDER
                .comment("If true, Marketplace daily limits are shared globally. If false, they apply per player.")
                .define("marketplaceGlobalDailyLimit", false);
        MARKETPLACE_GLOBAL_PRICING_ENABLED = BUILDER
                .comment("If true, dynamic demand pricing is forced for ALL offers in the Marketplace, overriding individual settings.")
                .define("marketplaceGlobalPricingEnabled", false);
        MARKETPLACE_GLOBAL_PRICING_MIN_PERCENT = BUILDER
                .comment("Global dynamic pricing minimum price in percent (e.g. 50 = 50% discount).")
                .defineInRange("marketplaceGlobalPricingMinPercent", 50, 1, 1000);
        MARKETPLACE_GLOBAL_PRICING_MAX_PERCENT = BUILDER
                .comment("Global dynamic pricing maximum price in percent (e.g. 200 = 2x price).")
                .defineInRange("marketplaceGlobalPricingMaxPercent", 200, 100, 10000);
        MARKETPLACE_GLOBAL_PRICING_VOLATILITY = BUILDER
                .comment("Global dynamic pricing volatility (SLOW, NORMAL, FAST).")
                .defineEnum("marketplaceGlobalPricingVolatility", Volatility.NORMAL);
        MARKETPLACE_GLOBAL_LIMITS_ENABLED = BUILDER
                .comment("If true, limits are forced for ALL offers in the Marketplace, overriding individual settings.")
                .define("marketplaceGlobalLimitsEnabled", false);
        MARKETPLACE_GLOBAL_LIMITS_DAILY_LIMIT = BUILDER
                .comment("Global daily purchase limit per offer (-1 for unlimited).")
                .defineInRange("marketplaceGlobalLimitsDailyLimit", -1, -1, Integer.MAX_VALUE);
        MARKETPLACE_GLOBAL_LIMITS_STOCK_LIMIT = BUILDER
                .comment("Global maximum stock cap per offer (-1 for unlimited).")
                .defineInRange("marketplaceGlobalLimitsStockLimit", -1, -1, Integer.MAX_VALUE);
        MARKETPLACE_GLOBAL_LIMITS_RESTOCK_SECONDS = BUILDER
                .comment("Global restock interval in seconds per offer (-1 for no restock).")
                .defineInRange("marketplaceGlobalLimitsRestockSeconds", -1, -1, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
