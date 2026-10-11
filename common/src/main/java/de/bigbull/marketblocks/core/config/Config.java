package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.core.config.toml.TomlConfigBuilder;
import de.bigbull.marketblocks.core.config.toml.TomlConfigSpec;
import de.bigbull.marketblocks.core.config.toml.TomlConfigValue;

/**
 * Central common configuration for the MarketBlocks mod.
 * Contains global settings (General, Integrations).
 * Saved in config/marketblocks/main.toml.
 */
public class Config {
    public static final TomlConfigBuilder COMMON_BUILDER = new TomlConfigBuilder();
    public static final TomlConfigSpec COMMON_SPEC;

    // General Settings
    public static final TomlConfigValue.BooleanValue GIVE_TRADE_BOOK_ON_FIRST_JOIN;
    public static final TomlConfigValue.BooleanValue ENABLE_TELEPORT;
    public static final TomlConfigValue.BooleanValue TELEPORT_REQUIRES_OP;
    public static final TomlConfigValue.BooleanValue SHOW_SHOP_ID_WITH_NAME;

    // Integrations
    public static final TomlConfigValue.BooleanValue ENABLE_XAEROS_COMPAT;
    public static final TomlConfigValue.BooleanValue ENABLE_JOURNEYMAP_COMPAT;
    public static final TomlConfigValue.BooleanValue ENABLE_JADE_COMPAT;
    public static final TomlConfigValue.BooleanValue ENABLE_JADE_TRADER_BUDGET;

    static {
        COMMON_BUILDER.push("General Settings");
        GIVE_TRADE_BOOK_ON_FIRST_JOIN = COMMON_BUILDER
                .comment("Give the interactive Trade Book to players when they join the world/server for the first time.")
                .define("giveTradeBookOnFirstJoin", true);
        ENABLE_TELEPORT = COMMON_BUILDER
                .comment("Master switch for shop teleportation (via Trade Book coordinate clicks and chat commands).",
                        "Default: false (Disabled by default for a vanilla survival experience).")
                .define("enableTeleport", false);
        TELEPORT_REQUIRES_OP = COMMON_BUILDER
                .comment("If enableTeleport is true, requires OP permissions (permission level 2) to teleport to shops.",
                        "Default: false (When teleportation is enabled, all players can use it).")
                .define("teleportRequiresOp", false);
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
        ENABLE_JADE_COMPAT = COMMON_BUILDER
                .comment("Enable Jade tooltip integration for shops and traders.")
                .define("enableJadeCompat", true);
        ENABLE_JADE_TRADER_BUDGET = COMMON_BUILDER
                .comment("Show the current coin budget of Shop Buyer (Trader) NPCs in the Jade tooltip.",
                        "Default: false (Disabled by default to avoid spoiling budget/wealth rank).")
                .define("enableJadeTraderBudget", false);
        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }

    /**
     * Determines whether the given player is permitted to teleport to shops
     * based on the master switch and optional OP permission requirement.
     */
    public static boolean canPlayerTeleport(net.minecraft.world.entity.player.Player player) {
        if (!ENABLE_TELEPORT.get()) {
            return false;
        }
        if (TELEPORT_REQUIRES_OP.get()) {
            return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        }
        return true;
    }
}