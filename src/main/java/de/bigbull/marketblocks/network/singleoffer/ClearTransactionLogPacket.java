package de.bigbull.marketblocks.network.singleoffer;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.log.ShopTransactionLogSavedData;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Clears the transaction log of one single-offer shop (primary-owner only).
 */
public record ClearTransactionLogPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ClearTransactionLogPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "single_offer_clear_transaction_log"));

    public static final StreamCodec<ByteBuf, ClearTransactionLogPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClearTransactionLogPacket::pos,
            ClearTransactionLogPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClearTransactionLogPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (!(player.level() instanceof ServerLevel level)) {
                return;
            }

            if (!(player.containerMenu instanceof SingleOfferShopMenu menu)) {
                return;
            }

            if (!menu.getBlockEntity().getBlockPos().equals(packet.pos())) {
                return;
            }

            if (!(level.getBlockEntity(packet.pos()) instanceof SingleOfferShopBlockEntity blockEntity)) {
                return;
            }

            if (!blockEntity.isPrimaryOwner(player)) {
                return;
            }

            ShopTransactionLogSavedData storage = ShopTransactionLogSavedData.get(level);
            storage.clearEntries(ShopTransactionLogSavedData.SINGLE_OFFER_SHOP_TYPE, level.dimension(), packet.pos());

            NetworkHandler.sendToPlayer(player, TransactionLogSyncPacket.fromEntries(
                    packet.pos(),
                    List.of(),
                    level.registryAccess()
            ));
        });
    }
}
