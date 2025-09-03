package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.renderer.SmallShopBlockEntityRenderer;
import de.bigbull.marketblocks.util.custom.screen.SmallShopSettingsScreen;
import de.bigbull.marketblocks.util.custom.screen.SmallShopInventoryScreen;
import de.bigbull.marketblocks.util.custom.screen.SmallShopOffersScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Handles client-side event subscriptions, such as registering screens and renderers.
 * This class is only loaded on the client.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
        // Private constructor to prevent instantiation
    }

    /**
     * Registers the menu screens for the mod.
     * This connects the menu container types to their corresponding screen implementations.
     * @param event The event for registering menu screens.
     */
    @SubscribeEvent
    public static void registerScreens(final RegisterMenuScreensEvent event) {
        event.register(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), SmallShopOffersScreen::new);
        event.register(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), SmallShopInventoryScreen::new);
        event.register(RegistriesInit.SMALL_SHOP_SETTINGS_MENU.get(), SmallShopSettingsScreen::new);
    }

    /**
     * Registers the block entity renderers for the mod.
     * This connects the block entity type to its corresponding renderer implementation.
     * @param event The event for registering entity renderers.
     */
    @SubscribeEvent
    public static void registerBlockEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), SmallShopBlockEntityRenderer::new);
    }
}
