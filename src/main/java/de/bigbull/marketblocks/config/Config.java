package de.bigbull.marketblocks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    static {
        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
