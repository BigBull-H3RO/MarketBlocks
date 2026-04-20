package de.bigbull.marketblocks.event;

import com.mojang.blaze3d.platform.InputConstants;
import de.bigbull.marketblocks.util.render.blockentity.SingleOfferShopBlockEntityRenderer;
import de.bigbull.marketblocks.init.RegistriesInit;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.util.screen.marketplace.MarketplaceScreen;
import de.bigbull.marketblocks.util.screen.singleoffer.SingleOfferShopScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MarketBlocks.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static final KeyMapping OPEN_MARKETPLACE = new KeyMapping(
            "key.marketblocks.open_marketplace",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.marketblocks");

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        // New unified screen
        event.register(RegistriesInit.SINGLE_OFFER_SHOP_MENU.get(), SingleOfferShopScreen::new);
        event.register(RegistriesInit.MARKETPLACE_MENU.get(), MarketplaceScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(), SingleOfferShopBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MARKETPLACE);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(RegistriesInit.TRADE_STAND_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(RegistriesInit.TRADE_STAND_BLOCK_TOP.get(), RenderType.cutout());

            ItemProperties.register(RegistriesInit.TRADE_STAND_BLOCK.get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "has_showcase"),
                    (stack, level, entity, seed) -> {
                        BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
                        if (properties != null && Boolean.TRUE.equals(properties.get(TradeStandBlock.HAS_SHOWCASE))) {
                            return 1.0F;
                        }
                        return 0.0F;
                    });
        });
    }

    public static KeyMapping getOpenMarketplaceKey() {
        return OPEN_MARKETPLACE;
    }
}

