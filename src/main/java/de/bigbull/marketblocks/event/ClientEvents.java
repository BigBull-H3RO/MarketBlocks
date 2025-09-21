package de.bigbull.marketblocks.event;

import com.mojang.blaze3d.platform.InputConstants;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopOpenRequestPacket;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.renderer.SmallShopBlockEntityRenderer;
import de.bigbull.marketblocks.util.custom.screen.ServerShopScreen;
import de.bigbull.marketblocks.util.custom.screen.SmallShopScreen;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static final KeyMapping OPEN_SERVER_SHOP = new KeyMapping(
            "key.marketblocks.open_server_shop",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.marketblocks");

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        // New unified screen
        event.register(RegistriesInit.SMALL_SHOP_MENU.get(), SmallShopScreen::new);
        event.register(RegistriesInit.SERVER_SHOP_MENU.get(), ServerShopScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), SmallShopBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SERVER_SHOP);
    }

    @SubscribeEvent
    public static void handleClientTick(ClientTickEvent.Post event) {
        while (OPEN_SERVER_SHOP.consumeClick()) {
            NetworkHandler.sendToServer(new ServerShopOpenRequestPacket());
        }
    }
}
