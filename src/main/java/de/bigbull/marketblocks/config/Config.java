package de.bigbull.marketblocks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_CHEST_SUPPORT;
    public static final ModConfigSpec.IntValue OFFER_UPDATE_INTERVAL;
    public static final ModConfigSpec.IntValue CHEST_IO_INTERVAL;

    static {
        COMMON_BUILDER.push("General Settings");
        ENABLE_DOUBLE_CHEST_SUPPORT = COMMON_BUILDER
                .comment("Allow double chests next to Small Shop")
                .define("enableDoubleChestSupport", false);
        OFFER_UPDATE_INTERVAL = COMMON_BUILDER
                .comment("Ticks between offer slot updates")
                .defineInRange("offerUpdateInterval", 5, 1, Integer.MAX_VALUE);
        CHEST_IO_INTERVAL = COMMON_BUILDER
                .comment("Ticks between chest input/output transfers")
                .defineInRange("chestIoInterval", 20, 1, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
