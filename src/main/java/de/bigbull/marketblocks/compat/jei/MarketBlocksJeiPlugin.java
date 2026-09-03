package de.bigbull.marketblocks.compat.jei;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.client.screen.MarketplaceScreen;
import de.bigbull.marketblocks.feature.singleoffer.client.screen.SingleOfferShopScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public class MarketBlocksJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(SingleOfferShopScreen.class, new IGuiContainerHandler<SingleOfferShopScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(SingleOfferShopScreen containerScreen) {
                return containerScreen.getExtraAreas();
            }
        });

        registration.addGuiContainerHandler(MarketplaceScreen.class, new IGuiContainerHandler<MarketplaceScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(MarketplaceScreen containerScreen) {
                return containerScreen.getExtraAreas();
            }
        });
    }
}
