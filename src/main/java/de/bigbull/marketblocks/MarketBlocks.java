package de.bigbull.marketblocks;

import com.mojang.logging.LogUtils;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.BlockInit;
import de.bigbull.marketblocks.util.CreativeTabInit;
import de.bigbull.marketblocks.util.ItemInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(MarketBlocks.MODID)
public class MarketBlocks {
    public static final String MODID = "marketblocks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarketBlocks(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ItemInit.ITEMS.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        CreativeTabInit.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
