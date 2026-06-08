package de.bigbull.marketblocks;

import com.mojang.logging.LogUtils;
import de.bigbull.marketblocks.core.config.Config;
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

        RegistriesInit.register(modEventBus);
        CreativeTabInit.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "marketblocks-common.toml");
    }
}
