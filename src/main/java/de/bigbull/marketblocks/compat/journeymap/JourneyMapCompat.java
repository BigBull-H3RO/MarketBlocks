package de.bigbull.marketblocks.compat.journeymap;

import java.util.List;
import net.minecraft.core.GlobalPos;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.ModList;

public class JourneyMapCompat {
    public static void addShopMarker(SingleOfferShopBlockEntity shop) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            try {
                Class<?> pluginClass = Class.forName("de.bigbull.marketblocks.compat.journeymap.MarketBlocksJourneyMapPlugin");
                Object instance = pluginClass.getMethod("getInstance").invoke(null);
                if (instance != null) {
                    pluginClass.getMethod("addShopMarker", SingleOfferShopBlockEntity.class).invoke(instance, shop);
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static void removeShopMarker(BlockPos pos) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            try {
                Class<?> pluginClass = Class.forName("de.bigbull.marketblocks.compat.journeymap.MarketBlocksJourneyMapPlugin");
                Object instance = pluginClass.getMethod("getInstance").invoke(null);
                if (instance != null) {
                    pluginClass.getMethod("removeShopMarker", BlockPos.class).invoke(instance, pos);
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }
    
    public static void updateMarketplaceMarkers(List<GlobalPos> linkedBlocks) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            try {
                Class<?> pluginClass = Class.forName("de.bigbull.marketblocks.compat.journeymap.MarketBlocksJourneyMapPlugin");
                Object instance = pluginClass.getMethod("getInstance").invoke(null);
                if (instance != null) {
                    pluginClass.getMethod("updateMarketplaceMarkers", List.class).invoke(instance, linkedBlocks);
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }
}

