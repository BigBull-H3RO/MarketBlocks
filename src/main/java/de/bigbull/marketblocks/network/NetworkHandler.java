package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.packets.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles the registration and distribution of network packets for the mod.
 * This class ensures that all custom packets are registered with NeoForge's networking system
 * and can be sent between the client and server.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0";

    private NetworkHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Registers all packet payload handlers for the mod.
     * This method is called by the NeoForge event bus during mod setup.
     * It registers which packets go from client to server and which go from server to client.
     *
     * @param event The registration event provided by NeoForge.
     */
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MarketBlocks.MODID)
                .versioned(PROTOCOL_VERSION)
                .optional();

        // --- Client to Server Packets ---
        registrar.playToServer(CreateOfferPacket.TYPE, CreateOfferPacket.CODEC, CreateOfferPacket::handle);
        registrar.playToServer(DeleteOfferPacket.TYPE, DeleteOfferPacket.CODEC, DeleteOfferPacket::handle);
        registrar.playToServer(SwitchTabPacket.TYPE, SwitchTabPacket.CODEC, SwitchTabPacket::handle);
        registrar.playToServer(AutoFillPaymentPacket.TYPE, AutoFillPaymentPacket.CODEC, AutoFillPaymentPacket::handle);
        registrar.playToServer(UpdateRedstoneSettingPacket.TYPE, UpdateRedstoneSettingPacket.CODEC, UpdateRedstoneSettingPacket::handle);
        registrar.playToServer(UpdateSettingsPacket.TYPE, UpdateSettingsPacket.CODEC, UpdateSettingsPacket::handle);
        registrar.playToServer(UpdateOwnersPacket.TYPE, UpdateOwnersPacket.CODEC, UpdateOwnersPacket::handle);

        // --- Server to Client Packets ---
        registrar.playToClient(OfferStatusPacket.TYPE, OfferStatusPacket.CODEC, OfferStatusPacket::handle);
    }

    /**
     * Helper method to send a packet from the client to the server.
     *
     * @param payload The packet payload to send.
     */
    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}