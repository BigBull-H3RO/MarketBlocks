package de.bigbull.marketblocks.client.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.marketplace.MarketplaceOpenRequestPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void handleClientTick(ClientTickEvent.Post event) {
        while (ClientEvents.getOpenMarketplaceKey().consumeClick()) {
            NetworkHandler.sendToServer(new MarketplaceOpenRequestPacket());
        }
    }
}
