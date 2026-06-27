package de.bigbull.marketblocks.compat.journeymap;

import java.util.List;
import net.minecraft.core.GlobalPos;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.ModList;

public class JourneyMapCompat {
    public static void addShopMarker(SingleOfferShopBlockEntity shop) {
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            if (MarketBlocksJourneyMapPlugin.getInstance() != null) {
                MarketBlocksJourneyMapPlugin.getInstance().addShopMarker(shop);
            }
        }
    }

    public static void removeShopMarker(BlockPos pos) {
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            if (MarketBlocksJourneyMapPlugin.getInstance() != null) {
                MarketBlocksJourneyMapPlugin.getInstance().removeShopMarker(pos);
            }
        }
    }
    
    public static void updateMarketplaceMarkers(List<GlobalPos> linkedBlocks) {
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            if (MarketBlocksJourneyMapPlugin.getInstance() != null) {
                MarketBlocksJourneyMapPlugin.getInstance().updateMarketplaceMarkers(linkedBlocks);
            }
        }
    }
}

