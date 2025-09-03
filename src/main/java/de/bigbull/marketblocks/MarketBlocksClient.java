package de.bigbull.marketblocks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Handles client-side specific mod initialization.
 * This class is only loaded on the client.
 */
@Mod(value = MarketBlocks.MODID, dist = Dist.CLIENT)
public class MarketBlocksClient {

    /**
     * The client-side constructor for the mod.
     * It registers the factory for the in-game configuration screen.
     *
     * @param container The mod container, provided by the mod loading system.
     */
    public MarketBlocksClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
