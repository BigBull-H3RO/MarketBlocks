package de.bigbull.marketblocks.client.event;

import de.bigbull.marketblocks.Constants;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceOpenRequestPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;

/**
 * Handles gameplay-related client events.
 * Currently listens for the custom "Open Marketplace" keybind and sends a
 * packet
 * to the server to open the UI when pressed.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void handleClientTick(ClientTickEvent.Post event) {
        while (ClientEvents.getOpenMarketplaceKey().consumeClick()) {
            NetworkHandler.sendToServer(new MarketplaceOpenRequestPacket());
        }
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TomlConfigManager.onClientDisconnect();
    }
}
