package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import de.bigbull.marketblocks.platform.network.PacketContext;

import java.util.UUID;

public record MarketplaceDeleteOfferPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<MarketplaceDeleteOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_delete_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceDeleteOfferPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MarketplaceDeleteOfferPacket::offerId,
            MarketplaceDeleteOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceDeleteOfferPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && MarketplaceManager.get().canEdit(player) &&
                    MarketplaceManager.get().deleteOffer(packet.offerId())) {
                MarketplaceManager.get().syncOpenViewers(player);
            }
        });
    }
}
