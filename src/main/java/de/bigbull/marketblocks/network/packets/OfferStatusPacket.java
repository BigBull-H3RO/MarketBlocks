package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity) {
                shopEntity.setHasOfferClient(packet.hasOffer());
            }
            if (context.player().containerMenu instanceof SmallShopOffersMenu menu) {
                menu.setCreatingOffer(false);
            }
        });
    }
}