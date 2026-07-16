package de.bigbull.marketblocks;

import com.mojang.logging.LogUtils;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.config.MarketplaceConfig;
import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.core.config.SingleOfferConfig;
import de.bigbull.marketblocks.core.config.TradeStandConfig;
import de.bigbull.marketblocks.core.config.MarketCrateConfig;
import de.bigbull.marketblocks.core.event.ModCapabilityEvents;
import de.bigbull.marketblocks.data.DataGenerators;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.core.init.CreativeTabInit;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(MarketBlocks.MODID)
public class MarketBlocks {
    public static final String MODID = "marketblocks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarketBlocks(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(NetworkHandler::register);
        modEventBus.addListener(ModCapabilityEvents::registerCapabilities);
        modEventBus.addListener(de.bigbull.marketblocks.core.event.ModEntityEvents::onEntityAttributeCreation);

        RegistriesInit.register(modEventBus);
        CreativeTabInit.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "marketblocks/main.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, MarketplaceConfig.SPEC, "marketblocks/marketplace.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, TraderConfig.SPEC, "marketblocks/trader/trader.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, SingleOfferConfig.SPEC, "marketblocks/singleoffer/general.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, TradeStandConfig.SPEC, "marketblocks/singleoffer/tradestand.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, MarketCrateConfig.SPEC, "marketblocks/singleoffer/marketcrate.toml");
    }
}
