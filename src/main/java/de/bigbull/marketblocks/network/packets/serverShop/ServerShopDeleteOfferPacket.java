package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ServerShopDeleteOfferPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<ServerShopDeleteOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_delete_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopDeleteOfferPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ServerShopDeleteOfferPacket::offerId,
            ServerShopDeleteOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopDeleteOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().canEdit(player) &&
                    ServerShopManager.get().deleteOffer(packet.offerId())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}