package de.bigbull.marketblocks.compat.journeymap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
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
        Constants.LOG.info("MarketBlocks: JourneyMap API initialized.");
    }

    @Override
    public String getModId() {
        return Constants.MOD_ID;
    }

    public boolean createWaypoint(String name, BlockPos pos, ResourceLocation dimension) {
        if (jmApi == null) {
            Constants.LOG.warn("Cannot create waypoint '{}' at {}: JourneyMap API not initialized.", name, pos);
            return false;
        }

        try {
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimension);
            Waypoint waypoint = WaypointFactory.createWaypoint(
                    Constants.MOD_ID,
                    pos,
                    name,
                    dimKey,
                    true
            );
            waypoint.setColor(0x00D4FF);
            waypoint.setShowBeacon(true);
            waypoint.setShowOnMap(true);
            waypoint.setShowInWorld(true);

            try {
                ResourceLocation icon = name.toLowerCase(Locale.ROOT).contains("marketplace")
                        ? ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/journeymap/marketplace.png")
                        : ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/journeymap/singleoffershop.png");
                waypoint.setIconResourceLoctaion(icon);
                waypoint.setIconTextureSize(16, 16);
            } catch (Throwable ignored) {
            }

            jmApi.addWaypoint(Constants.MOD_ID, waypoint);
            Constants.LOG.info("MarketBlocks: Created JourneyMap waypoint '{}' at {} in {}", name, pos, dimension);
            return true;
        } catch (Exception e) {
            Constants.LOG.error("Failed to create JourneyMap waypoint '{}' at {}", name, pos, e);
            return false;
        }
    }

    public void addShopMarker(SingleOfferShopBlockEntity shop) {
        if (jmApi == null) return;
        
        BlockPos pos = shop.getBlockPos();
        if (activeMarkers.containsKey(pos)) return;

        try {
            ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/journeymap/singleoffershop.png");
            MapImage icon = new MapImage(iconLoc, 16, 16).setDisplayWidth(16).setDisplayHeight(16).centerAnchors();
            
            String shopName = shop.getSettingsManager().getGeneralSettings().shopName();
            if (shopName == null || shopName.isEmpty()) shopName = "Shop";

            MarkerOverlay marker = new MarkerOverlay(Constants.MOD_ID, pos, icon);
            marker.setDimension(shop.getLevel().dimension());
            marker.setTitle(shopName);
            
            jmApi.show(marker);
            activeMarkers.put(pos, marker);
        } catch (Exception e) {
            Constants.LOG.error("Failed to add JourneyMap marker for shop at " + pos, e);
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
                    ResourceLocation iconLoc = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/journeymap/marketplace.png");
                    MapImage icon = new MapImage(iconLoc, 16, 16).setDisplayWidth(16).setDisplayHeight(16).centerAnchors();

                    MarkerOverlay marker = new MarkerOverlay(Constants.MOD_ID, globalPos.pos(), icon);
                    marker.setDimension(globalPos.dimension());
                    marker.setTitle("Marketplace");
                    
                    jmApi.show(marker);
                    marketplaceMarkers.add(marker);
                } catch (Exception e) {
                    Constants.LOG.error("Failed to add JourneyMap marker for marketplace at " + globalPos.pos(), e);
                }
            }
        }
    }
}