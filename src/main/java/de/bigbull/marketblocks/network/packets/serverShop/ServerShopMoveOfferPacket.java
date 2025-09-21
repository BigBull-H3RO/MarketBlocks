package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ServerShopMoveOfferPacket(UUID offerId, String targetPage, String targetCategory, int targetIndex)
        implements CustomPacketPayload {
    public static final Type<ServerShopMoveOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_move_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopMoveOfferPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ServerShopMoveOfferPacket::offerId,
            ByteBufCodecs.STRING_UTF8,
            ServerShopMoveOfferPacket::targetPage,
            ByteBufCodecs.STRING_UTF8,
            ServerShopMoveOfferPacket::targetCategory,
            ByteBufCodecs.VAR_INT,
            ServerShopMoveOfferPacket::targetIndex,
            ServerShopMoveOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopMoveOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().canEdit(player) &&
                    ServerShopManager.get().moveOffer(packet.offerId(), packet.targetPage(), packet.targetCategory(), packet.targetIndex())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}