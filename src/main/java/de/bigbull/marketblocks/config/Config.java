package de.bigbull.marketblocks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Manages the configuration settings for the Market Blocks mod.
 * This class uses NeoForge's config system to define and register settings
 * that can be customized by players or server administrators.
 */
public class Config {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    private static final String CATEGORY_GENERAL = "General Settings";
    private static final String KEY_ENABLE_DOUBLE_CHEST_SUPPORT = "enableDoubleChestSupport";
    private static final String KEY_OFFER_UPDATE_INTERVAL = "offerUpdateInterval";
    private static final String KEY_CHEST_IO_INTERVAL = "chestIoInterval";

    /**
     * If true, the Small Shop block can connect to and use a double chest as its storage.
     * If false, it is limited to a single chest.
     */
    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_CHEST_SUPPORT;

    /**
     * The time in ticks between updates for the shop's offers.
     * Lower values result in more frequent checks but may impact performance.
     */
    public static final ModConfigSpec.IntValue OFFER_UPDATE_INTERVAL;

    /**
     * The time in ticks between item transfers between the shop and an adjacent chest.
     * Lower values result in faster transfers but may impact performance.
     */
    public static final ModConfigSpec.IntValue CHEST_IO_INTERVAL;

    static {
        COMMON_BUILDER.push(CATEGORY_GENERAL);

        ENABLE_DOUBLE_CHEST_SUPPORT = COMMON_BUILDER
                .comment("If true, the Small Shop can use an adjacent double chest as its inventory.")
                .define(KEY_ENABLE_DOUBLE_CHEST_SUPPORT, false);

        OFFER_UPDATE_INTERVAL = COMMON_BUILDER
                .comment("The interval in ticks at which the shop's offers are updated. Lower values are faster but may increase server load.")
                .defineInRange(KEY_OFFER_UPDATE_INTERVAL, 5, 1, Integer.MAX_VALUE);

        CHEST_IO_INTERVAL = COMMON_BUILDER
                .comment("The interval in ticks for transferring items between the shop and an adjacent chest. Lower values are faster but may increase server load.")
                .defineInRange(KEY_CHEST_IO_INTERVAL, 20, 1, Integer.MAX_VALUE);

        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
