package de.bigbull.marketblocks.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.marketplace.client.screen.MarketplaceScreen;
import de.bigbull.marketblocks.feature.marketplace.network.LinkedBlocksSyncPacket;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceOpenRequestPacket;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceSyncPacket;
import de.bigbull.marketblocks.feature.singleoffer.client.render.SingleOfferShopBlockEntityRenderer;
import de.bigbull.marketblocks.feature.singleoffer.client.screen.SingleOfferShopScreen;
import de.bigbull.marketblocks.feature.singleoffer.network.OfferStatusPacket;
import de.bigbull.marketblocks.feature.singleoffer.network.TransactionLogSyncPacket;
import de.bigbull.marketblocks.feature.trader.client.ShopBuyerRenderer;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket;
import de.bigbull.marketblocks.core.config.network.ConfigSyncPacket;
import de.bigbull.marketblocks.feature.waypoint.network.CreateWaypointPacket;
import de.bigbull.marketblocks.core.config.toml.TomlConfigManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import de.bigbull.marketblocks.platform.Services;
import de.bigbull.marketblocks.platform.network.PacketContext;
import net.fabricmc.api.ClientModInitializer;
import de.bigbull.marketblocks.Constants;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;

public class MarketBlocksClient implements ClientModInitializer {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "marketblocks"));

    public static final KeyMapping OPEN_MARKETPLACE = new KeyMapping(
            "key.marketblocks.open_marketplace",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY);

    @Override
    public void onInitializeClient() {
        // Block Outlines
        FabricBlockOutlineHandler.init();

        // Screens
        MenuScreens.register(RegistriesInit.SINGLE_OFFER_SHOP_MENU.get(), SingleOfferShopScreen::new);
        MenuScreens.register(RegistriesInit.MARKETPLACE_MENU.get(), MarketplaceScreen::new);

        // Renderers
        BlockEntityRenderers.register(RegistriesInit.SINGLE_OFFER_SHOP_BLOCK_ENTITY.get(), SingleOfferShopBlockEntityRenderer::new);
        EntityRenderers.register(RegistriesInit.SHOP_BUYER.get(), ShopBuyerRenderer::new);

        // Render layers
        BlockRenderLayerMap.putBlock(RegistriesInit.TRADE_STAND_BLOCK.get(), ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(RegistriesInit.TRADE_STAND_BLOCK_TOP.get(), ChunkSectionLayer.CUTOUT);

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
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPacket.TYPE, (payload, context) -> {
            ConfigSyncPacket.handle(payload, makeContext(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(CreateWaypointPacket.TYPE, (payload, context) -> {
            CreateWaypointPacket.handle(payload, makeContext(context));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            TomlConfigManager.onClientDisconnect();
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
