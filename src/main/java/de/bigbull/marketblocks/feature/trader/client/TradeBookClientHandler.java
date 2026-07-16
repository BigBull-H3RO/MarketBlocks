package de.bigbull.marketblocks.feature.trader.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket.ShopOfferData;

public class TradeBookClientHandler {
    public static void openScreen(List<Component> pages, Map<String, ShopOfferData> offers) {
        Minecraft.getInstance().setScreen(new TradeBookScreen(pages, offers));
    }
}
