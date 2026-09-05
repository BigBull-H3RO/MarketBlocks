package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Central common configuration for the MarketBlocks mod.
 * Contains global settings (General, Integrations).
 * Feature-specific configs are in their own classes (SingleOfferConfig, MarketplaceConfig, TraderConfig, ClientConfig).
 */
public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    // General Settings
    public static final ModConfigSpec.BooleanValue GIVE_TRADE_BOOK_ON_FIRST_JOIN;
    public static final ModConfigSpec.BooleanValue ALLOW_NON_OP_TELEPORT;
    public static final ModConfigSpec.BooleanValue SHOW_SHOP_ID_WITH_NAME;

    // Integrations
    public static final ModConfigSpec.BooleanValue ENABLE_XAEROS_COMPAT;
    public static final ModConfigSpec.BooleanValue ENABLE_JOURNEYMAP_COMPAT;

    static {
        COMMON_BUILDER.push("General Settings");
        GIVE_TRADE_BOOK_ON_FIRST_JOIN = COMMON_BUILDER
                .comment("Give the interactive Trade Book to players when they join the world/server for the first time.")
                .define("giveTradeBookOnFirstJoin", true);
        ALLOW_NON_OP_TELEPORT = COMMON_BUILDER
                .comment("Allow non-OP players to use the [TP] button in the shop/marketplace list")
                .define("allowNonOpTeleport", false);
        SHOW_SHOP_ID_WITH_NAME = COMMON_BUILDER
                .comment("If true, shows the auto-generated Shop ID alongside custom shop names (e.g. 'My Shop (#A1F2)'). If false, only shows the custom name.")
                .define("showShopIdWithName", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Integrations");
        ENABLE_XAEROS_COMPAT = COMMON_BUILDER
                .comment("Enable Xaero's Minimap chat waypoint suggestions.")
                .define("enableXaerosCompat", true);
        ENABLE_JOURNEYMAP_COMPAT = COMMON_BUILDER
                .comment("Enable JourneyMap chat waypoint suggestions and map icons.")
                .define("enableJourneyMapCompat", true);
        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
