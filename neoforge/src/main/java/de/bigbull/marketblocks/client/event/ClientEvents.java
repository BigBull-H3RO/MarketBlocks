package de.bigbull.marketblocks.client.event;

import de.bigbull.marketblocks.Constants;

import com.mojang.blaze3d.platform.InputConstants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.marketplace.client.screen.MarketplaceScreen;
import de.bigbull.marketblocks.feature.singleoffer.client.screen.SingleOfferShopScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Main client-side event handler for MarketBlocks.
 * Responsible for registering screens, block entity renderers, and keybindings.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "marketblocks"));
    private static final KeyMapping OPEN_MARKETPLACE = new KeyMapping(
            "key.marketblocks.open_marketplace",
            InputConstants.KEY_K,
            CATEGORY);

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RegistriesInit.SINGLE_OFFER_SHOP_MENU.get(), SingleOfferShopScreen::new);
        event.register(RegistriesInit.MARKETPLACE_MENU.get(), MarketplaceScreen::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(),
                de.bigbull.marketblocks.client.render.NeoForgeSingleOfferShopBlockEntityRenderer::new);
        event.registerEntityRenderer(RegistriesInit.SHOP_BUYER.get(),
                de.bigbull.marketblocks.feature.trader.client.ShopBuyerRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_MARKETPLACE);
    }

    public static KeyMapping getOpenMarketplaceKey() {
        return OPEN_MARKETPLACE;
    }
}
