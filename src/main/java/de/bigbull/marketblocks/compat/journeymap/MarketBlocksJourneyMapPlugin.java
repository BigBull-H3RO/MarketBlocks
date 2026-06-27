package de.bigbull.marketblocks.compat.journeymap;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.client.display.MarkerOverlay;
import journeymap.api.v2.client.model.MapImage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class MarketBlocksJourneyMapPlugin implements IClientPlugin {

    private static MarketBlocksJourneyMapPlugin instance;
    private IClientAPI jmApi;
    private final Map<BlockPos, MarkerOverlay> activeMarkers = new HashMap<>();

    public MarketBlocksJourneyMapPlugin() {
        instance = this;
    }

    public static MarketBlocksJourneyMapPlugin getInstance() {
        return instance;
    }

    @Override
    public void initialize(IClientAPI jmClientApi) {
        this.jmApi = jmClientApi;
        MarketBlocks.LOGGER.info("MarketBlocks: JourneyMap API initialized.");
    }

    @Override
    public String getModId() {
        return MarketBlocks.MODID;
    }

    public void addShopMarker(SingleOfferShopBlockEntity shop) {
        if (jmApi == null) return;
        
        BlockPos pos = shop.getBlockPos();
        if (activeMarkers.containsKey(pos)) return;

        try {
            ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/journeymap/singleoffershop.png");
            MapImage icon = new MapImage(iconLoc, 16, 16).setDisplayWidth(16).setDisplayHeight(16).centerAnchors();
            
            String shopName = shop.getSettingsManager().getGeneralSettings().shopName();
            if (shopName == null || shopName.isEmpty()) shopName = "Shop";

            MarkerOverlay marker = new MarkerOverlay(MarketBlocks.MODID, pos, icon);
            marker.setDimension(shop.getLevel().dimension());
            marker.setTitle(shopName);
            
            jmApi.show(marker);
            activeMarkers.put(pos, marker);
        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Failed to add JourneyMap marker for shop at " + pos, e);
        }
    }

    public void removeShopMarker(BlockPos pos) {
        if (jmApi == null) return;
        
        MarkerOverlay marker = activeMarkers.remove(pos);
        if (marker != null) {
            jmApi.remove(marker);
        }
    }

    private final List<MarkerOverlay> marketplaceMarkers = new ArrayList<>();

    public void updateMarketplaceMarkers(List<GlobalPos> linkedBlocks) {
        if (jmApi == null) return;
        
        for (MarkerOverlay marker : marketplaceMarkers) {
            jmApi.remove(marker);
        }
        marketplaceMarkers.clear();

        for (GlobalPos globalPos : linkedBlocks) {
            if (Minecraft.getInstance().level != null && 
                globalPos.dimension().equals(Minecraft.getInstance().level.dimension())) {
                try {
                    ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/journeymap/MarketPlace.png");
                    MapImage icon = new MapImage(iconLoc, 16, 16).setDisplayWidth(16).setDisplayHeight(16).centerAnchors();

                    MarkerOverlay marker = new MarkerOverlay(MarketBlocks.MODID, globalPos.pos(), icon);
                    marker.setDimension(globalPos.dimension());
                    marker.setTitle("Marketplace");
                    
                    jmApi.show(marker);
                    marketplaceMarkers.add(marker);
                } catch (Exception e) {
                    MarketBlocks.LOGGER.error("Failed to add JourneyMap marker for marketplace at " + globalPos.pos(), e);
                }
            }
        }
    }
}

