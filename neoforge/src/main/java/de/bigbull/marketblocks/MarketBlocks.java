package de.bigbull.marketblocks;

import de.bigbull.marketblocks.event.ModCapabilityEvents;
import de.bigbull.marketblocks.event.ModEntityEvents;
import de.bigbull.marketblocks.init.NeoForgeRegistries;
import de.bigbull.marketblocks.network.NeoForgeNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class MarketBlocks {

    public MarketBlocks(IEventBus modEventBus, ModContainer modContainer) {
        // Initialize common logic and custom TOML configs
        CommonClass.init();

        // Register Deferred Registers
        NeoForgeRegistries.init();
        NeoForgeRegistries.register(modEventBus);

        // Mod bus event listeners
        modEventBus.addListener(NeoForgeNetwork::register);
        modEventBus.addListener(ModCapabilityEvents::registerCapabilities);
        modEventBus.addListener(ModEntityEvents::onEntityAttributeCreation);

        Constants.LOG.info("MarketBlocks NeoForge initialized successfully.");
    }
}
