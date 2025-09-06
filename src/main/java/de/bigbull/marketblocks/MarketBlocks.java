package de.bigbull.marketblocks;

import com.mojang.logging.LogUtils;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.data.DataGenerators;
import de.bigbull.marketblocks.util.CreativeTabInit;
import de.bigbull.marketblocks.util.RegistriesInit;
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

        RegistriesInit.register(modEventBus);
        CreativeTabInit.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "marketblocks-common.toml");
    }
}
