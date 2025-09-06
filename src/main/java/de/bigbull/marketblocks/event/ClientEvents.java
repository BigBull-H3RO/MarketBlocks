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

@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), SmallShopOffersScreen::new);
        event.register(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), SmallShopInventoryScreen::new);
        event.register(RegistriesInit.SMALL_SHOP_CONFIG_MENU.get(), SmallShopSettingsScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), SmallShopBlockEntityRenderer::new);
    }
}
