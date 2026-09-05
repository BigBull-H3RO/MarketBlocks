package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration settings specific to SingleOffer shops (Trade Stand & Market Crate).
 * Saved in {@code marketblocks/singleoffer/general.toml}.
 */
public class SingleOfferConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // --- Shop Mechanics ---
    public static final ModConfigSpec.DoubleValue SHOP_BLAST_RESISTANCE;
    public static final ModConfigSpec.IntValue MAX_SHOPS_PER_PLAYER_SURVIVAL;
    public static final ModConfigSpec.IntValue MAX_CO_OWNERS_PER_SHOP;
    public static final ModConfigSpec.BooleanValue ENABLE_CHEST_EXTENSION;
    public static final ModConfigSpec.IntValue CHEST_IO_INTERVAL;
    public static final ModConfigSpec.BooleanValue ENABLE_OUTPUT_WARNING;
    public static final ModConfigSpec.IntValue OUTPUT_WARNING_PERCENT;
    public static final ModConfigSpec.IntValue NOTIFICATION_COOLDOWN;

    // --- Notifications ---
    public static final ModConfigSpec.BooleanValue BUYER_CHAT_MESSAGE;
    public static final ModConfigSpec.BooleanValue BROADCAST_PURCHASE_TO_ALL;

    // --- Tab Visibility ---
    public static final ModConfigSpec.BooleanValue SHOP_TAB_VILLAGER_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_VISUALS_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_NOTIFICATIONS_ENABLED;

    static {
        BUILDER.push("ShopMechanics");
        SHOP_BLAST_RESISTANCE = BUILDER
                .comment("Explosion resistance for shop blocks (Trade Stand, Market Crate, etc.).",
                        "Default: 3600000.0 (bedrock level, prevents explosion griefing).",
                        "Set to 6.0 for obsidian-level resistance, or 3.0 for wood-like resistance.")
                .defineInRange("shopBlastResistance", 3600000.0, 3.0, 3600000.0);
        MAX_SHOPS_PER_PLAYER_SURVIVAL = BUILDER
                .comment("Maximum number of shops a player can place in Survival mode (-1 for unlimited)")
                .defineInRange("maxShopsPerPlayerSurvival", 10, -1, Integer.MAX_VALUE);
        MAX_CO_OWNERS_PER_SHOP = BUILDER
                .comment("Maximum number of co-owners allowed per SingleOffer shop")
                .defineInRange("maxCoOwnersPerShop", 10, 0, 100);
        ENABLE_CHEST_EXTENSION = BUILDER
                .comment("Enable automatic pulling from and pushing to adjacent chests (chest extension).",
                        "Default: false (Optional feature, use at own risk. Hides the I/O tab in the settings menu when disabled).")
                .define("enableChestExtension", false);
        CHEST_IO_INTERVAL = BUILDER
                .comment("Ticks between adjacent chest input/output transfers (Default: 20 ticks = 1 second)")
                .defineInRange("chestIoInterval", 20, 1, Integer.MAX_VALUE);
        ENABLE_OUTPUT_WARNING = BUILDER
                .comment("Show warning icon when output inventory is (almost) full")
                .define("enableOutputWarning", true);
        OUTPUT_WARNING_PERCENT = BUILDER
                .comment("Percentage of output inventory considered (almost) full")
                .defineInRange("outputWarningPercent", 90, 1, 100);
        NOTIFICATION_COOLDOWN = BUILDER
                .comment("Ticks to wait before sending another 'Out of Stock' or 'Output Full' notification (Default: 1200 = 1 Minute)")
                .defineInRange("notificationCooldownTicks", 1200, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Notifications");
        BUYER_CHAT_MESSAGE = BUILDER
                .comment("Send a private chat confirmation to the buyer upon a successful purchase (Default: true)")
                .define("buyerChatMessage", true);
        BROADCAST_PURCHASE_TO_ALL = BUILDER
                .comment("Broadcast the purchase message to all online players on the server (Default: false)")
                .define("broadcastPurchaseToAll", false);
        BUILDER.pop();

        BUILDER.push("Tabs");
        SHOP_TAB_VILLAGER_ENABLED = BUILDER
                .comment("Enable the Villager / Visual NPC settings tab in the shop GUI")
                .define("villager", true);
        SHOP_TAB_VISUALS_ENABLED = BUILDER
                .comment("Enable the Visuals settings tab in the shop GUI")
                .define("visuals", true);
        SHOP_TAB_NOTIFICATIONS_ENABLED = BUILDER
                .comment("Enable the Notifications settings tab in the shop GUI")
                .define("notifications", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
