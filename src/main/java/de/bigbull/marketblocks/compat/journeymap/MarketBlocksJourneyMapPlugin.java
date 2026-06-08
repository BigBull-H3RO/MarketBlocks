package de.bigbull.marketblocks.compat.journeymap;

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
            ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/block/trade_stand_particle.png");
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

    public void addMarketplaceMarker(de.bigbull.marketblocks.feature.marketplace.entity.MarketplaceBlockEntity marketplace) {
        if (jmApi == null) return;
        
        BlockPos pos = marketplace.getBlockPos();
        if (activeMarkers.containsKey(pos)) return;

        try {
            ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/block/marketcrate.png");
            MapImage icon = new MapImage(iconLoc, 16, 16).setDisplayWidth(16).setDisplayHeight(16).centerAnchors();

            MarkerOverlay marker = new MarkerOverlay(MarketBlocks.MODID, pos, icon);
            marker.setDimension(marketplace.getLevel().dimension());
            marker.setTitle("Marketplace");
            
            jmApi.show(marker);
            activeMarkers.put(pos, marker);
        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Failed to add JourneyMap marker for marketplace at " + pos, e);
        }
    }
}
