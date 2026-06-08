package de.bigbull.marketblocks.compat.journeymap;

import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.ModList;

public class JourneyMapCompat {
    public static void addShopMarker(SingleOfferShopBlockEntity shop) {
        if (de.bigbull.marketblocks.core.config.Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            if (MarketBlocksJourneyMapPlugin.getInstance() != null) {
                MarketBlocksJourneyMapPlugin.getInstance().addShopMarker(shop);
            }
        }
    }

    public static void removeShopMarker(BlockPos pos) {
        if (de.bigbull.marketblocks.core.config.Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            if (MarketBlocksJourneyMapPlugin.getInstance() != null) {
                MarketBlocksJourneyMapPlugin.getInstance().removeShopMarker(pos);
            }
        }
    }
    
    public static void addMarketplaceMarker(de.bigbull.marketblocks.feature.marketplace.entity.MarketplaceBlockEntity marketplace) {
        if (de.bigbull.marketblocks.core.config.Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            if (MarketBlocksJourneyMapPlugin.getInstance() != null) {
                MarketBlocksJourneyMapPlugin.getInstance().addMarketplaceMarker(marketplace);
            }
        }
    }
}
