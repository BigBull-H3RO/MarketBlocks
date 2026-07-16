package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Central configuration registry for the MarketBlocks mod.
 * Contains general mod settings (Shop Core, Logic, Security, Rendering, Integrations).
 * Feature-specific configs (Trader, Marketplace, SingleOffer) are in their own classes.
 */
public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_CHEST_SUPPORT;
    public static final ModConfigSpec.DoubleValue SHOP_BLAST_RESISTANCE;

    public static final ModConfigSpec.IntValue OFFER_UPDATE_INTERVAL;
    public static final ModConfigSpec.IntValue CHEST_IO_INTERVAL;
    public static final ModConfigSpec.BooleanValue ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL;
    public static final ModConfigSpec.BooleanValue ENABLE_OUTPUT_WARNING;
    public static final ModConfigSpec.IntValue OUTPUT_WARNING_PERCENT;
    public static final ModConfigSpec.IntValue NOTIFICATION_COOLDOWN;
    public static final ModConfigSpec.BooleanValue GIVE_TRADE_BOOK_ON_FIRST_JOIN;
    public static final ModConfigSpec.IntValue MAX_CO_OWNERS_PER_SHOP;
    public static final ModConfigSpec.IntValue MAX_SHOPS_PER_PLAYER_SURVIVAL;
    public static final ModConfigSpec.BooleanValue ALLOW_NON_OP_TELEPORT;
    public static final ModConfigSpec.BooleanValue SHOW_SHOP_ID_WITH_NAME;
    public static final ModConfigSpec.BooleanValue SHOP_BUYER_MESSAGE;
    public static final ModConfigSpec.BooleanValue SHOP_BUYER_MESSAGE_GLOBAL;

    public static final ModConfigSpec.BooleanValue ENABLE_PACKET_RATE_LIMITING;
    public static final ModConfigSpec.IntValue PACKET_COOLDOWN_MS;
    public static final ModConfigSpec.IntValue MAX_SHOP_NAME_LENGTH;
    public static final ModConfigSpec.BooleanValue BLOCK_FORMATTING_IN_SHOP_NAME;
    public static final ModConfigSpec.BooleanValue MARKETBLOCKS_ADMIN_MODE_ENABLED;

    public static final ModConfigSpec.BooleanValue VISUAL_NPC_FORCE_OFFSCREEN_RENDERING;
    public static final ModConfigSpec.IntValue VISUAL_NPC_RENDER_VIEW_DISTANCE;
    public static final ModConfigSpec.BooleanValue ENABLE_GLOBAL_OFFER_ITEM_RENDERING;

    public static final ModConfigSpec.BooleanValue ENABLE_MIXIN_DESYNC_LOGGING;

    public static final ModConfigSpec.BooleanValue ENABLE_XAEROS_COMPAT;
    public static final ModConfigSpec.BooleanValue ENABLE_JOURNEYMAP_COMPAT;

    static {
        COMMON_BUILDER.push("Shop Core");
        ENABLE_DOUBLE_CHEST_SUPPORT = COMMON_BUILDER
                .comment("Allow double chests next to Trade Stand")
                .define("enableDoubleChestSupport", false);
        SHOP_BLAST_RESISTANCE = COMMON_BUILDER
                .comment("Explosion resistance for all shop blocks (Trade Stand, Market Crate, etc.).",
                        "Default: 3600000.0 (same as bedrock, prevents explosion griefing).",
                        "Set to 6.0 for obsidian-level resistance, or 3.0 for wood-like resistance.")
                .defineInRange("shopBlastResistance", 3600000.0, 3.0, 3600000.0);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Shop Logic");
        GIVE_TRADE_BOOK_ON_FIRST_JOIN = COMMON_BUILDER
                .comment("Give the interactive Trade Book to players when they join the world/server for the first time.")
                .define("giveTradeBookOnFirstJoin", true);
        MAX_CO_OWNERS_PER_SHOP = COMMON_BUILDER
                .comment("Maximum number of co-owners allowed per SingleOfferShop.")
                .defineInRange("maxCoOwnersPerShop", 10, 0, 100);
        OFFER_UPDATE_INTERVAL = COMMON_BUILDER
                .comment("Ticks between offer slot updates")
                .defineInRange("offerUpdateInterval", 5, 1, Integer.MAX_VALUE);
        CHEST_IO_INTERVAL = COMMON_BUILDER
                .comment("Ticks between chest input/output transfers")
                .defineInRange("chestIoInterval", 20, 1, Integer.MAX_VALUE);
        ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL = COMMON_BUILDER
                .comment("EXPERIMENTAL: Enable Trade Stand input/output chest extension and neighbor chest integration",
                        "Default: false for release stability")
                .define("enableChestIoExtensionExperimental", false);
        ENABLE_OUTPUT_WARNING = COMMON_BUILDER
                .comment("Show warning icon when output inventory is (almost) full")
                .define("enableOutputWarning", true);
        OUTPUT_WARNING_PERCENT = COMMON_BUILDER
                .comment("Percentage of output inventory considered (almost) full")
                .defineInRange("outputWarningPercent", 90, 1, 100);
        NOTIFICATION_COOLDOWN = COMMON_BUILDER
                .comment("Ticks to wait before sending another 'Out of Stock' or 'Output Full' notification (Default: 1200 = 1 Minute)")
                .defineInRange("notificationCooldownTicks", 1200, 0, Integer.MAX_VALUE);
        MAX_SHOPS_PER_PLAYER_SURVIVAL = COMMON_BUILDER
                .comment("Maximum number of shops a player can place in Survival mode (-1 for unlimited)")
                .defineInRange("maxShopsPerPlayerSurvival", 10, -1, Integer.MAX_VALUE);
        ALLOW_NON_OP_TELEPORT = COMMON_BUILDER
                .comment("Allow non-OP players to use the [TP] button in the shop list")
                .define("allowNonOpTeleport", false);
        SHOW_SHOP_ID_WITH_NAME = COMMON_BUILDER
                .comment("If true, shows the auto-generated Shop ID alongside custom shop names (e.g. 'My Shop (#A1F2)'). If false, only shows the custom name.")
                .define("showShopIdWithName", true);
        SHOP_BUYER_MESSAGE = COMMON_BUILDER
                .comment("Send chat message upon successful purchase at a SingleOfferShop (Default: false)")
                .define("shopBuyerMessage", false);
        SHOP_BUYER_MESSAGE_GLOBAL = COMMON_BUILDER
                .comment("Broadcast the SingleOfferShop purchase message globally to all players instead of just the buyer (Default: false)")
                .define("shopBuyerMessageGlobal", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Security & Robustness");
        ENABLE_PACKET_RATE_LIMITING = COMMON_BUILDER
                .comment("Enable rate limiting for network packets to prevent spamming")
                .define("enablePacketRateLimiting", true);
        PACKET_COOLDOWN_MS = COMMON_BUILDER
                .comment("Cooldown in milliseconds between network packets per player")
                .defineInRange("packetCooldownMs", 100, 0, 5000);
        MAX_SHOP_NAME_LENGTH = COMMON_BUILDER
                .comment("Maximum allowed length for a shop name")
                .defineInRange("maxShopNameLength", 32, 1, 256);
        BLOCK_FORMATTING_IN_SHOP_NAME = COMMON_BUILDER
                .comment("Block chat formatting codes (like &c or §c) in shop names")
                .define("blockFormattingInShopName", true);
        MARKETBLOCKS_ADMIN_MODE_ENABLED = COMMON_BUILDER
                .comment("Global admin mode controlled by /marketblocks adminmode.",
                        "Enables Marketplace edit mode and OP-only Admin-Shop controls in SingleOffer settings.")
                .define("marketblocksAdminModeEnabled", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Client and Rendering");
        VISUAL_NPC_FORCE_OFFSCREEN_RENDERING = COMMON_BUILDER
                .comment("If true, visual shop NPCs render even when they are near screen borders / off-center.",
                        "Disable for stricter culling and potentially better performance.")
                .define("visualNpcForceOffscreenRendering", true);
        VISUAL_NPC_RENDER_VIEW_DISTANCE = COMMON_BUILDER
                .comment("Maximum distance in blocks for rendering visual shop NPCs.")
                .defineInRange("visualNpcRenderViewDistance", 128, 16, 512);
        ENABLE_GLOBAL_OFFER_ITEM_RENDERING = COMMON_BUILDER
                .comment("Global master switch to enable/disable offer item rendering for all shops. Disable to save performance.")
                .define("enableGlobalOfferItemRendering", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Debug Settings");
        ENABLE_MIXIN_DESYNC_LOGGING = COMMON_BUILDER
                .comment("Enable debug logs for client-side mining target fallback in Trade Stand mixins")
                .define("enableMixinDesyncLogging", false);
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
