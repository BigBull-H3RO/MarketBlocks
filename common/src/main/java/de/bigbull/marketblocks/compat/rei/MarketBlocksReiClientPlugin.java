package de.bigbull.marketblocks.compat.rei;

import de.bigbull.marketblocks.feature.marketplace.client.screen.MarketplaceScreen;
import de.bigbull.marketblocks.feature.singleoffer.client.screen.SingleOfferShopScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

public class MarketBlocksReiClientPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(SingleOfferShopScreen.class, screen -> {
            List<Rectangle> list = new ArrayList<>();
            for (Rect2i rect : screen.getExtraAreas()) {
                list.add(new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
            }
            return list;
        });

        zones.register(MarketplaceScreen.class, screen -> {
            List<Rectangle> list = new ArrayList<>();
            for (Rect2i rect : screen.getExtraAreas()) {
                list.add(new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
            }
            return list;
        });
    }
}
