package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.network.*;
import de.bigbull.marketblocks.feature.singleoffer.network.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Handles the registration and distribution of network packets for the mod.
 * This class ensures that all custom packets are known to NeoForge's networking system
 * and can be sent between the client and server.
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0.0";

    /**
     * Registers all packet handlers for the mod.
     * This method is called by the NeoForge event bus during mod setup.
     * It registers which packets go from client to server and which go from server to client.
     *
     * @param event The registration event provided by NeoForge.
     */
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MarketBlocks.MODID)
                .versioned(PROTOCOL_VERSION);

        registerServerPacket(registrar, CreateOfferPacket.TYPE, CreateOfferPacket.CODEC, CreateOfferPacket::handle);
        registerServerPacket(registrar, DeleteOfferPacket.TYPE, DeleteOfferPacket.CODEC, DeleteOfferPacket::handle);
        registerServerPacket(registrar, SwitchTabPacket.TYPE, SwitchTabPacket.CODEC, SwitchTabPacket::handle);
        registerServerPacket(registrar, AutoFillPaymentPacket.TYPE, AutoFillPaymentPacket.CODEC, AutoFillPaymentPacket::handle);
        registerServerPacket(registrar, UpdateRedstoneSettingPacket.TYPE, UpdateRedstoneSettingPacket.CODEC, UpdateRedstoneSettingPacket::handle);
        registerServerPacket(registrar, UpdateSettingsPacket.TYPE, UpdateSettingsPacket.CODEC, UpdateSettingsPacket::handle);
        registerServerPacket(registrar, ToggleAdminShopModePacket.TYPE, ToggleAdminShopModePacket.CODEC, ToggleAdminShopModePacket::handle);

        registerServerPacket(registrar, ClearTransactionLogPacket.TYPE, ClearTransactionLogPacket.CODEC, ClearTransactionLogPacket::handle);

        registerServerPacket(registrar, MarketplaceOpenRequestPacket.TYPE, MarketplaceOpenRequestPacket.CODEC, MarketplaceOpenRequestPacket::handle);
        registerServerPacket(registrar, MarketplaceToggleEditModePacket.TYPE, MarketplaceToggleEditModePacket.CODEC, MarketplaceToggleEditModePacket::handle);
        registerServerPacket(registrar, MarketplaceSelectPagePacket.TYPE, MarketplaceSelectPagePacket.CODEC, MarketplaceSelectPagePacket::handle);
        registerServerPacket(registrar, MarketplaceCreatePagePacket.TYPE, MarketplaceCreatePagePacket.CODEC, MarketplaceCreatePagePacket::handle);
        registerServerPacket(registrar, MarketplaceRenamePagePacket.TYPE, MarketplaceRenamePagePacket.CODEC, MarketplaceRenamePagePacket::handle);
        registerServerPacket(registrar, MarketplaceDeletePagePacket.TYPE, MarketplaceDeletePagePacket.CODEC, MarketplaceDeletePagePacket::handle);
        registerServerPacket(registrar, MarketplaceAddOfferPacket.TYPE, MarketplaceAddOfferPacket.CODEC, MarketplaceAddOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceMoveOfferPacket.TYPE, MarketplaceMoveOfferPacket.CODEC, MarketplaceMoveOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceDeleteOfferPacket.TYPE, MarketplaceDeleteOfferPacket.CODEC, MarketplaceDeleteOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceUpdateOfferLimitsPacket.TYPE, MarketplaceUpdateOfferLimitsPacket.CODEC, MarketplaceUpdateOfferLimitsPacket::handle);
        registerServerPacket(registrar, MarketplaceUpdateOfferPricingPacket.TYPE, MarketplaceUpdateOfferPricingPacket.CODEC, MarketplaceUpdateOfferPricingPacket::handle);
        registerServerPacket(registrar, MarketplaceAutoFillPacket.TYPE, MarketplaceAutoFillPacket.CODEC, MarketplaceAutoFillPacket::handle);
        registerServerPacket(registrar, MarketplaceSetOfferPacket.TYPE, MarketplaceSetOfferPacket.CODEC, MarketplaceSetOfferPacket::handle);

        registrar.playToClient(OfferStatusPacket.TYPE, OfferStatusPacket.CODEC, OfferStatusPacket::handle);
        registrar.playToClient(TransactionLogSyncPacket.TYPE, TransactionLogSyncPacket.CODEC, TransactionLogSyncPacket::handle);

        registrar.playToClient(MarketplaceSyncPacket.TYPE, MarketplaceSyncPacket.CODEC, MarketplaceSyncPacket::handle);
        registrar.playToClient(LinkedBlocksSyncPacket.TYPE, LinkedBlocksSyncPacket.CODEC, LinkedBlocksSyncPacket::handle);
    }

    private static <T extends CustomPacketPayload> void registerServerPacket(
            PayloadRegistrar registrar, 
            CustomPacketPayload.Type<T> type, 
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, 
            IPayloadHandler<T> handler) {
        
        registrar.playToServer(type, codec, (packet, context) -> {
            if (context.player() instanceof Player player) {
                if (!PacketRateLimiter.canProcessPacket(player)) {
                    return;
                }
            }
            handler.handle(packet, context);
        });
    }

    /**
     * A helper method to send a packet from the client to the server.
     *
     * @param packet The packet payload to send.
     */
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    /**
     * A helper method to send a packet from the server to a specific player.
     *
     * @param player The server player to receive the packet.
     * @param packet The packet payload to send.
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}
