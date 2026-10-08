package de.bigbull.marketblocks.compat.emi;

import de.bigbull.marketblocks.feature.marketplace.client.screen.MarketplaceScreen;
import de.bigbull.marketblocks.feature.singleoffer.client.screen.SingleOfferShopScreen;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;

@EmiEntrypoint
public class MarketBlocksEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(SingleOfferShopScreen.class, (screen, consumer) -> {
            for (Rect2i rect : screen.getExtraAreas()) {
                consumer.accept(new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
            }
        });

        registry.addExclusionArea(MarketplaceScreen.class, (screen, consumer) -> {
            for (Rect2i rect : screen.getExtraAreas()) {
                consumer.accept(new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
            }
        });
    }
}
