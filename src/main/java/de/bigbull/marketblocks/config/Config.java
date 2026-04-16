package de.bigbull.marketblocks.config;

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
    public static final ModConfigSpec.BooleanValue SERVER_SHOP_GLOBAL_DAILY_LIMIT;
    public static final ModConfigSpec.BooleanValue SERVER_SHOP_EDIT_MODE_ENABLED;
    public static final ModConfigSpec.BooleanValue ENABLE_MIXIN_DESYNC_LOGGING;

    static {
        COMMON_BUILDER.push("General Settings");
        ENABLE_DOUBLE_CHEST_SUPPORT = COMMON_BUILDER
                .comment("Allow double chests next to Small Shop")
                .define("enableDoubleChestSupport", false);
        ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL = COMMON_BUILDER
                .comment("EXPERIMENTAL: Enable SmallShop Input/Output chest extension and neighbor chest integration",
                        "Default: false for release stability")
                .define("enableChestIoExtensionExperimental", false);
        OFFER_UPDATE_INTERVAL = COMMON_BUILDER
                .comment("Ticks between offer slot updates")
                .defineInRange("offerUpdateInterval", 5, 1, Integer.MAX_VALUE);
        CHEST_IO_INTERVAL = COMMON_BUILDER
                .comment("Ticks between chest input/output transfers")
                .defineInRange("chestIoInterval", 20, 1, Integer.MAX_VALUE);
        ENABLE_OUTPUT_WARNING = COMMON_BUILDER
                .comment("Show warning icon when output inventory is (almost) full")
                .define("enableOutputWarning", true);
        OUTPUT_WARNING_PERCENT = COMMON_BUILDER
                .comment("Percentage of output inventory considered (almost) full")
                .defineInRange("outputWarningPercent", 90, 1, 100);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Server Shop");
        SERVER_SHOP_GLOBAL_DAILY_LIMIT = COMMON_BUILDER
                .comment("If true, ServerShop daily limits are shared globally. If false, they apply per player.")
                .define("serverShopGlobalDailyLimit", false);
        SERVER_SHOP_EDIT_MODE_ENABLED = COMMON_BUILDER
                .comment("If true, the ServerShop edit mode toggle is available for admins. If false, the edit toggle is hidden and disabled globally.")
                .define("serverShopEditModeEnabled", false);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Debug Settings");
        ENABLE_MIXIN_DESYNC_LOGGING = COMMON_BUILDER
                .comment("Enable debug logs for client-side mining target fallback in SmallShop mixins")
                .define("enableMixinDesyncLogging", false);
        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
