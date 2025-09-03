package de.bigbull.marketblocks;

import com.mojang.logging.LogUtils;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.CreativeTabInit;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * The main class of the Market Blocks mod.
 * This class is the entry point for the mod, responsible for initializing registries,
 * configurations, and event listeners.
 */
@Mod(MarketBlocks.MODID)
public class MarketBlocks {
    public static final String MODID = "marketblocks";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * The constructor for the mod. It is called by the NeoForge mod loading system.
     * @param modEventBus The event bus for mod-specific events.
     * @param modContainer The container for this mod.
     */
    public MarketBlocks(IEventBus modEventBus, ModContainer modContainer) {
        // Register all initializers
        RegistriesInit.register(modEventBus);
        CreativeTabInit.CREATIVE_MODE_TABS.register(modEventBus);

        // Register the configuration file
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "marketblocks-common.toml");
    }

    /**
     * Creates a {@link ResourceLocation} with the mod's ID.
     * @param path The path for the resource location.
     * @return A new ResourceLocation instance.
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
