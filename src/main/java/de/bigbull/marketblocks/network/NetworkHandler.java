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
 * This class ensures that all custom packets are known to NeoForge's networking system
 * and can be sent between the client and server.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public class NetworkHandler {

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
                .versioned("1.0.0")
                .optional();

        // Client to Server packets
        registrar.playToServer(CreateOfferPacket.TYPE, CreateOfferPacket.CODEC, CreateOfferPacket::handle);
        registrar.playToServer(DeleteOfferPacket.TYPE, DeleteOfferPacket.CODEC, DeleteOfferPacket::handle);
        registrar.playToServer(SwitchTabPacket.TYPE, SwitchTabPacket.CODEC, SwitchTabPacket::handle);
        registrar.playToServer(AutoFillPaymentPacket.TYPE, AutoFillPaymentPacket.CODEC, AutoFillPaymentPacket::handle);
        registrar.playToServer(UpdateRedstoneSettingPacket.TYPE, UpdateRedstoneSettingPacket.CODEC, UpdateRedstoneSettingPacket::handle);
        registrar.playToServer(UpdateSettingsPacket.TYPE, UpdateSettingsPacket.CODEC, UpdateSettingsPacket::handle);
        registrar.playToServer(UpdateOwnersPacket.TYPE, UpdateOwnersPacket.CODEC, UpdateOwnersPacket::handle);

        // Server to Client packets
        registrar.playToClient(OfferStatusPacket.TYPE, OfferStatusPacket.CODEC, OfferStatusPacket::handle);
    }

    /**
     * A helper method to send a packet from the client to the server.
     *
     * @param packet The packet payload to send.
     */
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}