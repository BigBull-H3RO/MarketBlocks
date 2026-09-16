package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.core.config.toml.TomlConfigBuilder;
import de.bigbull.marketblocks.core.config.toml.TomlConfigSpec;
import de.bigbull.marketblocks.core.config.toml.TomlConfigValue;

/**
 * Client-specific configuration settings for MarketBlocks (rendering, visuals).
 * Loaded on physical clients only via config/marketblocks/client.toml.
 */
public class ClientConfig {
    public static final TomlConfigBuilder BUILDER = new TomlConfigBuilder();
    public static final TomlConfigSpec SPEC;

    public static final TomlConfigValue.BooleanValue ENABLE_SHOP_ITEM_RENDERING;

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