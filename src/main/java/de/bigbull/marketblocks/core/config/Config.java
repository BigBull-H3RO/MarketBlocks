package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoRedstoneControl;
import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Central configuration registry for the MarketBlocks mod.
 * Defines and stores all configuration options available to server admins and players,
 * categorized into logical sections like Shop Core, Shop Logic, Marketplace, and Visuals.
 */
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

        public static final ModConfigSpec.BooleanValue ENABLE_XAEROS_COMPAT;
        public static final ModConfigSpec.BooleanValue ENABLE_JOURNEYMAP_COMPAT;

        public static final ModConfigSpec.BooleanValue SHOP_TAB_GENERAL_ENABLED;
        public static final ModConfigSpec.BooleanValue SHOP_TAB_IO_ENABLED;
        public static final ModConfigSpec.BooleanValue SHOP_TAB_VILLAGER_ENABLED;
        public static final ModConfigSpec.BooleanValue SHOP_TAB_VISUALS_ENABLED;
        public static final ModConfigSpec.BooleanValue SHOP_TAB_NOTIFICATIONS_ENABLED;
        public static final ModConfigSpec.BooleanValue SHOP_TAB_ACCESS_ENABLED;

        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_EMIT_REDSTONE;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PURCHASE_XP_SOUND;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_IS_CLOSED;

        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_VILLAGER_NPC_ENABLED;
        public static final ModConfigSpec.EnumValue<VillagerVisualProfession> TRADESTAND_DEFAULT_VILLAGER_PROFESSION;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PURCHASE_PARTICLES;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PURCHASE_SOUNDS;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PAYMENT_SLOT_SOUNDS;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_USE_PLAYER_SKIN;

        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ITEM_VISIBLE;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ITEM_FULLBRIGHT;
        public static final ModConfigSpec.DoubleValue TRADESTAND_DEFAULT_ITEM_SCALE;
        public static final ModConfigSpec.DoubleValue TRADESTAND_DEFAULT_ITEM_SPEED;
        public static final ModConfigSpec.DoubleValue TRADESTAND_DEFAULT_ITEM_HEIGHT_OFFSET;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ITEM_BOBBING;

        public static final ModConfigSpec.EnumValue<IoRedstoneControl> TRADESTAND_DEFAULT_REDSTONE_CONTROL;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ALLOW_IO;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_AUTO_IO;

        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_PURCHASE;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_OUT_OF_STOCK;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_OUTPUT_FULL;
        public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_CO_OWNERS;

        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_EMIT_REDSTONE;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PURCHASE_XP_SOUND;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_IS_CLOSED;

        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_VILLAGER_NPC_ENABLED;
        public static final ModConfigSpec.EnumValue<VillagerVisualProfession> MARKETCRATE_DEFAULT_VILLAGER_PROFESSION;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PURCHASE_PARTICLES;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PURCHASE_SOUNDS;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PAYMENT_SLOT_SOUNDS;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_USE_PLAYER_SKIN;

        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ITEM_VISIBLE;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ITEM_FULLBRIGHT;
        public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_SCALE;
        public static final ModConfigSpec.IntValue MARKETCRATE_DEFAULT_ITEM_COUNT;
        public static final ModConfigSpec.EnumValue<CrateLayoutMode> MARKETCRATE_DEFAULT_ITEM_LAYOUT_MODE;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ITEM_DYNAMIC_FILL;
        public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_ROTATION;
        public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_SPACING_XZ;
        public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_SPACING_Y;
        public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_CHAOS_ROTATION;

        public static final ModConfigSpec.EnumValue<IoRedstoneControl> MARKETCRATE_DEFAULT_REDSTONE_CONTROL;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ALLOW_IO;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_AUTO_IO;

        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_PURCHASE;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_OUT_OF_STOCK;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_OUTPUT_FULL;
        public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_CO_OWNERS;

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

                COMMON_BUILDER.push("TradeStand - Default Values");
                COMMON_BUILDER.comment(
                                "These values are used as starting values for new TradeStands. If a tab is disabled above, these serve as fixed forced values.");
                COMMON_BUILDER.push("General Defaults");
                TRADESTAND_DEFAULT_EMIT_REDSTONE = COMMON_BUILDER.define("tradestandDefaultEmitRedstone", false);
                TRADESTAND_DEFAULT_PURCHASE_XP_SOUND = COMMON_BUILDER.define("tradestandDefaultPurchaseXpSound", true);
                TRADESTAND_DEFAULT_IS_CLOSED = COMMON_BUILDER.define("tradestandDefaultIsClosed", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("Villager Defaults");
                TRADESTAND_DEFAULT_VILLAGER_NPC_ENABLED = COMMON_BUILDER.define("tradestandDefaultVillagerNpcEnabled",
                                false);
                TRADESTAND_DEFAULT_VILLAGER_PROFESSION = COMMON_BUILDER.defineEnum(
                                "tradestandDefaultVillagerProfession",
                                VillagerVisualProfession.NONE);
                TRADESTAND_DEFAULT_PURCHASE_PARTICLES = COMMON_BUILDER.define("tradestandDefaultPurchaseParticles",
                                true);
                TRADESTAND_DEFAULT_PURCHASE_SOUNDS = COMMON_BUILDER.define("tradestandDefaultPurchaseSounds", true);
                TRADESTAND_DEFAULT_PAYMENT_SLOT_SOUNDS = COMMON_BUILDER.define("tradestandDefaultPaymentSlotSounds",
                                true);
                TRADESTAND_DEFAULT_USE_PLAYER_SKIN = COMMON_BUILDER.define("tradestandDefaultUsePlayerSkin", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("Visuals Defaults");
                TRADESTAND_DEFAULT_ITEM_VISIBLE = COMMON_BUILDER.define("tradestandDefaultItemVisible", true);
                TRADESTAND_DEFAULT_ITEM_FULLBRIGHT = COMMON_BUILDER.define("tradestandDefaultItemFullbright", false);
                TRADESTAND_DEFAULT_ITEM_SCALE = COMMON_BUILDER.defineInRange("tradestandDefaultItemScale", 0.75, 0.5,
                                1.5);
                TRADESTAND_DEFAULT_ITEM_SPEED = COMMON_BUILDER.defineInRange("tradestandDefaultItemSpeed", 1.0, 0.0,
                                1.5);
                TRADESTAND_DEFAULT_ITEM_HEIGHT_OFFSET = COMMON_BUILDER
                                .defineInRange("tradestandDefaultItemHeightOffset", 0.0, -0.25, 0.25);
                TRADESTAND_DEFAULT_ITEM_BOBBING = COMMON_BUILDER.define("tradestandDefaultItemBobbing", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("I/O Defaults");
                TRADESTAND_DEFAULT_REDSTONE_CONTROL = COMMON_BUILDER.defineEnum("tradestandDefaultRedstoneControl",
                                IoRedstoneControl.IGNORED);
                TRADESTAND_DEFAULT_ALLOW_IO = COMMON_BUILDER.define("tradestandDefaultAllowIo", false);
                TRADESTAND_DEFAULT_AUTO_IO = COMMON_BUILDER.define("tradestandDefaultAutoIo", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("Notification Defaults");
                TRADESTAND_DEFAULT_NOTIFY_PURCHASE = COMMON_BUILDER.define("tradestandDefaultNotifyPurchase", false);
                TRADESTAND_DEFAULT_NOTIFY_OUT_OF_STOCK = COMMON_BUILDER.define("tradestandDefaultNotifyOutOfStock",
                                false);
                TRADESTAND_DEFAULT_NOTIFY_OUTPUT_FULL = COMMON_BUILDER.define("tradestandDefaultNotifyOutputFull",
                                false);
                TRADESTAND_DEFAULT_NOTIFY_CO_OWNERS = COMMON_BUILDER.define("tradestandDefaultNotifyCoOwners", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.pop();

                COMMON_BUILDER.push("MarketCrate - Default Values");
                COMMON_BUILDER.comment(
                                "These values are used as starting values for new MarketCrates. If a tab is disabled above, these serve as fixed forced values.");
                COMMON_BUILDER.push("General Defaults");
                MARKETCRATE_DEFAULT_EMIT_REDSTONE = COMMON_BUILDER.define("marketcrateDefaultEmitRedstone", false);
                MARKETCRATE_DEFAULT_PURCHASE_XP_SOUND = COMMON_BUILDER.define("marketcrateDefaultPurchaseXpSound",
                                true);
                MARKETCRATE_DEFAULT_IS_CLOSED = COMMON_BUILDER.define("marketcrateDefaultIsClosed", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("Villager Defaults");
                MARKETCRATE_DEFAULT_VILLAGER_NPC_ENABLED = COMMON_BUILDER.define("marketcrateDefaultVillagerNpcEnabled",
                                false);
                MARKETCRATE_DEFAULT_VILLAGER_PROFESSION = COMMON_BUILDER.defineEnum(
                                "marketcrateDefaultVillagerProfession",
                                VillagerVisualProfession.NONE);
                MARKETCRATE_DEFAULT_PURCHASE_PARTICLES = COMMON_BUILDER.define("marketcrateDefaultPurchaseParticles",
                                true);
                MARKETCRATE_DEFAULT_PURCHASE_SOUNDS = COMMON_BUILDER.define("marketcrateDefaultPurchaseSounds", true);
                MARKETCRATE_DEFAULT_PAYMENT_SLOT_SOUNDS = COMMON_BUILDER.define("marketcrateDefaultPaymentSlotSounds",
                                true);
                MARKETCRATE_DEFAULT_USE_PLAYER_SKIN = COMMON_BUILDER.define("marketcrateDefaultUsePlayerSkin", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("Visuals Defaults");
                MARKETCRATE_DEFAULT_ITEM_VISIBLE = COMMON_BUILDER.define("marketcrateDefaultItemVisible", true);
                MARKETCRATE_DEFAULT_ITEM_FULLBRIGHT = COMMON_BUILDER.define("marketcrateDefaultItemFullbright", false);
                MARKETCRATE_DEFAULT_ITEM_SCALE = COMMON_BUILDER.defineInRange("marketcrateDefaultItemScale", 1.0, 0.25,
                                1.5);
                MARKETCRATE_DEFAULT_ITEM_COUNT = COMMON_BUILDER.defineInRange("marketcrateDefaultItemCount", 1, 1, 96);
                MARKETCRATE_DEFAULT_ITEM_LAYOUT_MODE = COMMON_BUILDER.defineEnum("marketcrateDefaultItemLayoutMode",
                                CrateLayoutMode.STACKED);
                MARKETCRATE_DEFAULT_ITEM_DYNAMIC_FILL = COMMON_BUILDER.define("marketcrateDefaultItemDynamicFill",
                                false);
                MARKETCRATE_DEFAULT_ITEM_ROTATION = COMMON_BUILDER.defineInRange("marketcrateDefaultItemRotation", 0.0,
                                0.0, 360.0);
                MARKETCRATE_DEFAULT_ITEM_SPACING_XZ = COMMON_BUILDER.defineInRange("marketcrateDefaultItemSpacingXZ",
                                0.0, -0.5, 0.5);
                MARKETCRATE_DEFAULT_ITEM_SPACING_Y = COMMON_BUILDER.defineInRange("marketcrateDefaultItemSpacingY", 0.0,
                                0.0, 2.0);
                MARKETCRATE_DEFAULT_ITEM_CHAOS_ROTATION = COMMON_BUILDER
                                .defineInRange("marketcrateDefaultItemChaosRotation", 0.0, 0.0, 1.0);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("I/O Defaults");
                MARKETCRATE_DEFAULT_REDSTONE_CONTROL = COMMON_BUILDER.defineEnum("marketcrateDefaultRedstoneControl",
                                IoRedstoneControl.IGNORED);
                MARKETCRATE_DEFAULT_ALLOW_IO = COMMON_BUILDER.define("marketcrateDefaultAllowIo", false);
                MARKETCRATE_DEFAULT_AUTO_IO = COMMON_BUILDER.define("marketcrateDefaultAutoIo", false);
                COMMON_BUILDER.pop();
                COMMON_BUILDER.push("Notification Defaults");
                MARKETCRATE_DEFAULT_NOTIFY_PURCHASE = COMMON_BUILDER.define("marketcrateDefaultNotifyPurchase", false);
                MARKETCRATE_DEFAULT_NOTIFY_OUT_OF_STOCK = COMMON_BUILDER.define("marketcrateDefaultNotifyOutOfStock",
                                false);
                MARKETCRATE_DEFAULT_NOTIFY_OUTPUT_FULL = COMMON_BUILDER.define("marketcrateDefaultNotifyOutputFull",
                                false);
                MARKETCRATE_DEFAULT_NOTIFY_CO_OWNERS = COMMON_BUILDER.define("marketcrateDefaultNotifyCoOwners", false);
                COMMON_BUILDER.pop();
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
