package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

// direction: -1 für hoch, +1 für runter
public record MarketplaceMoveOfferPacket(UUID offerId, String targetPage, int direction) implements CustomPacketPayload {
    public static final Type<MarketplaceMoveOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_move_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceMoveOfferPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MarketplaceMoveOfferPacket::offerId,
            ByteBufCodecs.STRING_UTF8,
            MarketplaceMoveOfferPacket::targetPage,
            ByteBufCodecs.VAR_INT,
            MarketplaceMoveOfferPacket::direction,
            MarketplaceMoveOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceMoveOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && MarketplaceManager.get().canEdit(player) &&
                    MarketplaceManager.get().moveOffer(packet.offerId(), packet.targetPage(), packet.direction())) {
                MarketplaceManager.get().syncOpenViewers(player);
            }
        });
    }
}