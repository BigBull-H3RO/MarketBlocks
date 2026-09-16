package de.bigbull.marketblocks;

import de.bigbull.marketblocks.core.config.*;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;
import de.bigbull.marketblocks.platform.Services;

public class CommonClass {

    public static void init() {
        Constants.LOG.info("Initializing {} common on {} ({})", Constants.MOD_NAME, Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        // Register and load all 7 MarketBlocks TOML configurations
        TomlConfigManager.register(Config.COMMON_SPEC, "marketblocks/main.toml");
        TomlConfigManager.register(ClientConfig.SPEC, "marketblocks/client.toml");
        TomlConfigManager.register(MarketplaceConfig.SPEC, "marketblocks/marketplace.toml");
        TomlConfigManager.register(TraderConfig.SPEC, "marketblocks/trader/trader.toml");
        TomlConfigManager.register(SingleOfferConfig.SPEC, "marketblocks/singleoffer/general.toml");
        TomlConfigManager.register(TradeStandConfig.SPEC, "marketblocks/singleoffer/tradestand.toml");
        TomlConfigManager.register(MarketCrateConfig.SPEC, "marketblocks/singleoffer/marketcrate.toml");

        TomlConfigManager.loadAll();
    }
}