package de.bigbull.marketblocks;

import de.bigbull.marketblocks.core.config.*;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;
import de.bigbull.marketblocks.core.config.toml.TomlConfigType;
import de.bigbull.marketblocks.platform.Services;

public class CommonClass {

    public static void init() {
        Constants.LOG.info("Initializing {} common on {} ({})", Constants.MOD_NAME, Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        // Register and load all 7 MarketBlocks TOML configurations with appropriate sync types
        TomlConfigManager.register(Config.COMMON_SPEC, "marketblocks/main.toml", TomlConfigType.SERVER);
        TomlConfigManager.register(ClientConfig.SPEC, "marketblocks/client.toml", TomlConfigType.CLIENT);
        TomlConfigManager.register(MarketplaceConfig.SPEC, "marketblocks/marketplace.toml", TomlConfigType.SERVER);
        TomlConfigManager.register(TraderConfig.SPEC, "marketblocks/trader/trader.toml", TomlConfigType.SERVER);
        TomlConfigManager.register(SingleOfferConfig.SPEC, "marketblocks/singleoffer/general.toml", TomlConfigType.SERVER);
        TomlConfigManager.register(TradeStandConfig.SPEC, "marketblocks/singleoffer/tradestand.toml", TomlConfigType.SERVER);
        TomlConfigManager.register(MarketCrateConfig.SPEC, "marketblocks/singleoffer/marketcrate.toml", TomlConfigType.SERVER);

        TomlConfigManager.loadAll();
    }
}
