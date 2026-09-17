package de.bigbull.marketblocks.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.marketplace.client.screen.MarketplaceScreen;
import de.bigbull.marketblocks.feature.marketplace.network.LinkedBlocksSyncPacket;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceOpenRequestPacket;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceSyncPacket;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import de.bigbull.marketblocks.feature.singleoffer.client.render.SingleOfferShopBlockEntityRenderer;
import de.bigbull.marketblocks.feature.singleoffer.client.screen.SingleOfferShopScreen;
import de.bigbull.marketblocks.feature.singleoffer.network.OfferStatusPacket;
import de.bigbull.marketblocks.feature.singleoffer.network.TransactionLogSyncPacket;
import de.bigbull.marketblocks.feature.trader.client.ShopBuyerRenderer;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket;
import de.bigbull.marketblocks.platform.Services;
import de.bigbull.marketblocks.platform.network.PacketContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.lwjgl.glfw.GLFW;

public class MarketBlocksClient implements ClientModInitializer {

    public static final KeyMapping OPEN_MARKETPLACE = new KeyMapping(
            "key.marketblocks.open_marketplace",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.marketblocks");

    @Override
    public void onInitializeClient() {
        // Screens
        MenuScreens.register(RegistriesInit.SINGLE_OFFER_SHOP_MENU.get(), SingleOfferShopScreen::new);
        MenuScreens.register(RegistriesInit.MARKETPLACE_MENU.get(), MarketplaceScreen::new);

        // Renderers
        BlockEntityRenderers.register(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(), SingleOfferShopBlockEntityRenderer::new);
        EntityRendererRegistry.register(RegistriesInit.SHOP_BUYER.get(), ShopBuyerRenderer::new);

        // Render layers
        BlockRenderLayerMap.INSTANCE.putBlock(RegistriesInit.TRADE_STAND_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(RegistriesInit.TRADE_STAND_BLOCK_TOP.get(), RenderType.cutout());

        // Item Properties
        ItemProperties.register(RegistriesInit.TRADE_STAND_BLOCK.get().asItem(),
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "has_showcase"),
                (stack, level, entity, seed) -> {
                    BlockItemStateProperties properties = stack.get(DataComponents.BLOCK_STATE);
                    if (properties != null && Boolean.TRUE.equals(properties.get(TradeStandBlock.HAS_SHOWCASE))) {
                        return 1.0F;
                    }
                    return 0.0F;
                });

        // Keybindings
        KeyBindingHelper.registerKeyBinding(OPEN_MARKETPLACE);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MARKETPLACE.consumeClick()) {
                Services.NETWORK.sendToServer(new MarketplaceOpenRequestPacket());
            }
        });

        // Client packet receivers
        ClientPlayNetworking.registerGlobalReceiver(OfferStatusPacket.TYPE, (payload, context) -> {
            OfferStatusPacket.handle(payload, makeContext(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(TransactionLogSyncPacket.TYPE, (payload, context) -> {
            TransactionLogSyncPacket.handle(payload, makeContext(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(MarketplaceSyncPacket.TYPE, (payload, context) -> {
            MarketplaceSyncPacket.handle(payload, makeContext(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(LinkedBlocksSyncPacket.TYPE, (payload, context) -> {
            payload.handle(makeContext(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(TradeBookOpenPacket.TYPE, (payload, context) -> {
            TradeBookOpenPacket.handle(payload, makeContext(context));
        });
    }

    private PacketContext makeContext(ClientPlayNetworking.Context context) {
        return new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public void enqueueWork(Runnable runnable) {
                context.client().execute(runnable);
            }
        };
    }
}
