package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_CHEST_SUPPORT;
    public static final ModConfigSpec.BooleanValue ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL;
    public static final ModConfigSpec.IntValue OFFER_UPDATE_INTERVAL;
    public static final ModConfigSpec.IntValue CHEST_IO_INTERVAL;
    public static final ModConfigSpec.BooleanValue ENABLE_OUTPUT_WARNING;
    public static final ModConfigSpec.IntValue OUTPUT_WARNING_PERCENT;
    public static final ModConfigSpec.IntValue NOTIFICATION_COOLDOWN;
    public static final ModConfigSpec.BooleanValue MARKETPLACE_GLOBAL_DAILY_LIMIT;
    public static final ModConfigSpec.BooleanValue MARKETBLOCKS_ADMIN_MODE_ENABLED;
    public static final ModConfigSpec.IntValue MAX_CO_OWNERS_PER_SHOP;
    public static final ModConfigSpec.BooleanValue ENABLE_MIXIN_DESYNC_LOGGING;
    public static final ModConfigSpec.BooleanValue VISUAL_NPC_FORCE_OFFSCREEN_RENDERING;
    public static final ModConfigSpec.IntValue VISUAL_NPC_RENDER_VIEW_DISTANCE;
    public static final ModConfigSpec.BooleanValue ENABLE_GLOBAL_OFFER_ITEM_RENDERING;
    public static final ModConfigSpec.DoubleValue SHOP_BLAST_RESISTANCE;

    // SingleOfferShop Tab Visibilities
    public static final ModConfigSpec.BooleanValue SHOP_TAB_GENERAL_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_IO_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_VILLAGER_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_VISUALS_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_NOTIFICATIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_ACCESS_ENABLED;

    // SingleOfferShop Settings Defaults (Fallback when tabs are disabled)
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_EMIT_REDSTONE;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_PURCHASE_XP_SOUND;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_IS_CLOSED;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_VILLAGER_NPC_ENABLED;
    public static final ModConfigSpec.EnumValue<de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession> SHOP_DEFAULT_VILLAGER_PROFESSION;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_PURCHASE_PARTICLES;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_PURCHASE_SOUNDS;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_PAYMENT_SLOT_SOUNDS;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_USE_PLAYER_SKIN;
    
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_ITEM_VISIBLE;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_ITEM_FULLBRIGHT;
    public static final ModConfigSpec.DoubleValue SHOP_DEFAULT_ITEM_SCALE;
    public static final ModConfigSpec.DoubleValue SHOP_DEFAULT_ITEM_SPEED;
    public static final ModConfigSpec.DoubleValue SHOP_DEFAULT_ITEM_HEIGHT_OFFSET;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_ITEM_BOBBING;
    public static final ModConfigSpec.IntValue SHOP_DEFAULT_ITEM_COUNT;
    public static final ModConfigSpec.EnumValue<de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode> SHOP_DEFAULT_ITEM_LAYOUT_MODE;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_ITEM_DYNAMIC_FILL;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_NOTIFY_PURCHASE;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_NOTIFY_OUT_OF_STOCK;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_NOTIFY_OUTPUT_FULL;
    public static final ModConfigSpec.BooleanValue SHOP_DEFAULT_NOTIFY_CO_OWNERS;

    static {
        // --- SHOP CORE ---
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

        // --- SHOP LOGIC ---
        COMMON_BUILDER.push("Shop Logic");
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
        COMMON_BUILDER.pop();

        // --- MARKETPLACE ---
        COMMON_BUILDER.push("Marketplace");
        MARKETPLACE_GLOBAL_DAILY_LIMIT = COMMON_BUILDER
                .comment("If true, Marketplace daily limits are shared globally. If false, they apply per player.")
                .define("marketplaceGlobalDailyLimit", false);
        MARKETBLOCKS_ADMIN_MODE_ENABLED = COMMON_BUILDER
                .comment("Global admin mode controlled by /marketblocks adminmode.",
                        "Enables Marketplace edit mode and OP-only Admin-Shop controls in SingleOffer settings.")
                .define("marketblocksAdminModeEnabled", false);
        MAX_CO_OWNERS_PER_SHOP = COMMON_BUILDER
                .comment("Maximum number of co-owners allowed per SingleOfferShop.")
                .defineInRange("maxCoOwnersPerShop", 10, 0, 100);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("SingleOfferShop - Tab Visibility");
        SHOP_TAB_GENERAL_ENABLED = COMMON_BUILDER.define("shopTabGeneralEnabled", true);
        SHOP_TAB_IO_ENABLED = COMMON_BUILDER.define("shopTabIoEnabled", true);
        SHOP_TAB_VILLAGER_ENABLED = COMMON_BUILDER.define("shopTabVillagerEnabled", true);
        SHOP_TAB_VISUALS_ENABLED = COMMON_BUILDER.define("shopTabVisualsEnabled", true);
        SHOP_TAB_NOTIFICATIONS_ENABLED = COMMON_BUILDER.define("shopTabNotificationsEnabled", true);
        SHOP_TAB_ACCESS_ENABLED = COMMON_BUILDER.define("shopTabAccessEnabled", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("SingleOfferShop - Default Values");
        COMMON_BUILDER.comment("These values are used as starting values for new shops. If a tab is disabled above, these serve as fixed forced values.");
        
        COMMON_BUILDER.push("General Defaults");
        SHOP_DEFAULT_EMIT_REDSTONE = COMMON_BUILDER.define("shopDefaultEmitRedstone", false);
        SHOP_DEFAULT_PURCHASE_XP_SOUND = COMMON_BUILDER.define("shopDefaultPurchaseXpSound", true);
        SHOP_DEFAULT_IS_CLOSED = COMMON_BUILDER.define("shopDefaultIsClosed", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Villager Defaults");
        SHOP_DEFAULT_VILLAGER_NPC_ENABLED = COMMON_BUILDER.define("shopDefaultVillagerNpcEnabled", true);
        SHOP_DEFAULT_VILLAGER_PROFESSION = COMMON_BUILDER.defineEnum("shopDefaultVillagerProfession", de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession.NONE);
        SHOP_DEFAULT_PURCHASE_PARTICLES = COMMON_BUILDER.define("shopDefaultPurchaseParticles", true);
        SHOP_DEFAULT_PURCHASE_SOUNDS = COMMON_BUILDER.define("shopDefaultPurchaseSounds", true);
        SHOP_DEFAULT_PAYMENT_SLOT_SOUNDS = COMMON_BUILDER.define("shopDefaultPaymentSlotSounds", true);
        SHOP_DEFAULT_USE_PLAYER_SKIN = COMMON_BUILDER.define("shopDefaultUsePlayerSkin", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Visuals Defaults");
        SHOP_DEFAULT_ITEM_VISIBLE = COMMON_BUILDER.define("shopDefaultItemVisible", true);
        SHOP_DEFAULT_ITEM_FULLBRIGHT = COMMON_BUILDER.define("shopDefaultItemFullbright", false);
        SHOP_DEFAULT_ITEM_SCALE = COMMON_BUILDER.defineInRange("shopDefaultItemScale", 0.75, 0.1, 4.0);
        SHOP_DEFAULT_ITEM_SPEED = COMMON_BUILDER.defineInRange("shopDefaultItemSpeed", 2.0, 0.0, 20.0);
        SHOP_DEFAULT_ITEM_HEIGHT_OFFSET = COMMON_BUILDER.defineInRange("shopDefaultItemHeightOffset", 0.0, -2.0, 4.0);
        SHOP_DEFAULT_ITEM_BOBBING = COMMON_BUILDER.define("shopDefaultItemBobbing", true);
        SHOP_DEFAULT_ITEM_COUNT = COMMON_BUILDER.defineInRange("shopDefaultItemCount", 1, 1, 96);
        SHOP_DEFAULT_ITEM_LAYOUT_MODE = COMMON_BUILDER.defineEnum("shopDefaultItemLayoutMode", de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode.GESTAPELT);
        SHOP_DEFAULT_ITEM_DYNAMIC_FILL = COMMON_BUILDER.define("shopDefaultItemDynamicFill", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Notification Defaults");
        SHOP_DEFAULT_NOTIFY_PURCHASE = COMMON_BUILDER.define("shopDefaultNotifyPurchase", false);
        SHOP_DEFAULT_NOTIFY_OUT_OF_STOCK = COMMON_BUILDER.define("shopDefaultNotifyOutOfStock", false);
        SHOP_DEFAULT_NOTIFY_OUTPUT_FULL = COMMON_BUILDER.define("shopDefaultNotifyOutputFull", false);
        SHOP_DEFAULT_NOTIFY_CO_OWNERS = COMMON_BUILDER.define("shopDefaultNotifyCoOwners", false);
        COMMON_BUILDER.pop();
        
        COMMON_BUILDER.pop(); // End of Default Values

        // --- CLIENT / RENDERING ---
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

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
