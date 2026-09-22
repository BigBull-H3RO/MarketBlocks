package de.bigbull.marketblocks.compat.journeymap;

import java.util.List;
import net.minecraft.core.GlobalPos;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import de.bigbull.marketblocks.platform.Services;

public class JourneyMapCompat {
    public static boolean createWaypoint(String name, BlockPos pos, String dimensionStr) {
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && Services.PLATFORM.isModLoaded("journeymap")) {
            try {
                Class<?> pluginClass = Class.forName("de.bigbull.marketblocks.compat.journeymap.MarketBlocksJourneyMapPlugin");
                Object instance = pluginClass.getMethod("getInstance").invoke(null);
                if (instance != null) {
                    ResourceLocation dimLoc = ResourceLocation.parse(dimensionStr);
                    Object result = pluginClass.getMethod("createWaypoint", String.class, BlockPos.class, ResourceLocation.class)
                            .invoke(instance, name, pos, dimLoc);
                    return Boolean.TRUE.equals(result);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    public static void addShopMarker(SingleOfferShopBlockEntity shop) {
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && Services.PLATFORM.isModLoaded("journeymap")) {
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
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && Services.PLATFORM.isModLoaded("journeymap")) {
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
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && Services.PLATFORM.isModLoaded("journeymap")) {
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