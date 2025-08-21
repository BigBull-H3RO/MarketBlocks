package de.bigbull.marketblocks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_CHEST_SUPPORT;

    static {
        COMMON_BUILDER.push("General Settings");
        ENABLE_DOUBLE_CHEST_SUPPORT = COMMON_BUILDER
                .comment("Allow double chests next to Small Shop")
                .define("enableDoubleChestSupport", false);
        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
