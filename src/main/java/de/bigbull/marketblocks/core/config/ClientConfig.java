package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-specific configuration settings for MarketBlocks (rendering, visuals).
 * Loaded on physical clients only via {@code marketblocks/client.toml}.
 */
public class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SHOP_ITEM_RENDERING;

    static {
        BUILDER.push("Rendering");
        ENABLE_SHOP_ITEM_RENDERING = BUILDER
                .comment("Enable or disable all item rendering on and around shops",
                        "(floating offer items, crate contents, and front recipe displays).",
                        "Disable this on low-end PCs to maximize FPS in shopping areas.")
                .define("enableShopItemRendering", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
