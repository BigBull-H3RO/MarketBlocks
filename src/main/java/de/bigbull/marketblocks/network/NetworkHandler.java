package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.packets.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MarketBlocks.MODID)
                .versioned("1.0.0")
                .optional();

        registrar.playToServer(
                CreateOfferPacket.TYPE,
                CreateOfferPacket.CODEC,
                CreateOfferPacket::handle
        );

        registrar.playToServer(
                DeleteOfferPacket.TYPE,
                DeleteOfferPacket.CODEC,
                DeleteOfferPacket::handle
        );

        registrar.playToServer(
                SwitchTabPacket.TYPE,
                SwitchTabPacket.CODEC,
                SwitchTabPacket::handle
        );

        registrar.playToServer(
                AutoFillPaymentPacket.TYPE,
                AutoFillPaymentPacket.CODEC,
                AutoFillPaymentPacket::handle
        );

        registrar.playToClient(
                OfferStatusPacket.TYPE,
                OfferStatusPacket.CODEC,
                OfferStatusPacket::handle
        );
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}