package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Antwort vom Server nach erfolgreichem Abbruch der Angebotserstellung.
 */
public record CancelOfferResponsePacket(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CancelOfferResponsePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "cancel_offer_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelOfferResponsePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CancelOfferResponsePacket::pos,
            CancelOfferResponsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CancelOfferResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof de.bigbull.marketblocks.util.custom.screen.SmallShopOffersScreen screen) {
                screen.onOfferCreationCancelled();
            }
        });
    }
}