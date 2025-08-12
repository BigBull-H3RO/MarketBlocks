package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Antwort vom Server nach erfolgreichem Löschen eines Angebots.
 */
public record DeleteOfferResponsePacket(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DeleteOfferResponsePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "delete_offer_response"));

    public static final StreamCodec<ByteBuf, DeleteOfferResponsePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DeleteOfferResponsePacket::pos,
            DeleteOfferResponsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeleteOfferResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof de.bigbull.marketblocks.util.custom.screen.SmallShopOffersScreen screen) {
                screen.onOfferDeleted();
            }
        });
    }
}