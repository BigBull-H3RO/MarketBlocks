package de.bigbull.marketblocks.network.packets.singleOfferShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet sent from server to client to update the offer status of a shop.
 * 
 * NOTE: This packet exists in addition to the standard BlockEntity sync (getUpdateTag/handleUpdateTag)
 * for the following reasons:
 * 
 * 1. Immediate notification: When an offer is created via OfferManager.applyOffer(), we want to
 *    immediately notify all tracking players without waiting for the next chunk update.
 * 
 * 2. Targeted update: This packet only updates the hasOffer flag, making it lightweight for
 *    quick state changes when offers are created/deleted.
 * 
 * 3. Chunk tracking: Sent via PacketDistributor.sendToPlayersTrackingChunk() to notify all players
 *    who have the chunk loaded, not just the player who created the offer.
 * 
 * The full shop state (offer items, settings, etc.) is still synchronized via getUpdateTag/handleUpdateTag.
 */
public record OfferStatusPacket(BlockPos pos, boolean hasOffer) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OfferStatusPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "offer_status"));

    public static final StreamCodec<ByteBuf, OfferStatusPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OfferStatusPacket::pos,
            ByteBufCodecs.BOOL,
            OfferStatusPacket::hasOffer,
            OfferStatusPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OfferStatusPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            if (level.getBlockEntity(packet.pos()) instanceof SingleOfferShopBlockEntity shopEntity) {
                shopEntity.setHasOfferClient(packet.hasOffer());

                // Keep currently opened menu in sync with the same block entity instance.
                if (context.player().containerMenu instanceof SingleOfferShopMenu menu) {
                    if (menu.getBlockEntity().getBlockPos().equals(packet.pos())) {
                        menu.getBlockEntity().setHasOfferClient(packet.hasOffer());
                    }
                }
            }
        });
    }
}
