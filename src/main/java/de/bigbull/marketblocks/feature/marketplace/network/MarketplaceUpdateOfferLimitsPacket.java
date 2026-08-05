package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.OfferLimit;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MarketplaceUpdateOfferLimitsPacket(UUID offerId, OfferLimit limit) implements CustomPacketPayload {
    public static final Type<MarketplaceUpdateOfferLimitsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_update_limits"));

    private static final StreamCodec<RegistryFriendlyByteBuf, OfferLimit> LIMIT_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public OfferLimit decode(RegistryFriendlyByteBuf buf) {
            boolean unlimited = buf.readBoolean();
            Integer daily = buf.readBoolean() ? buf.readInt() : null;
            Integer stock = buf.readBoolean() ? buf.readInt() : null;
            Integer restock = buf.readBoolean() ? buf.readInt() : null;
            return new OfferLimit(unlimited, daily, stock, restock);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, OfferLimit value) {
            buf.writeBoolean(value.isUnlimited());
            buf.writeBoolean(value.dailyLimit().isPresent());
            value.dailyLimit().ifPresent(buf::writeInt);
            buf.writeBoolean(value.stockLimit().isPresent());
            value.stockLimit().ifPresent(buf::writeInt);
            buf.writeBoolean(value.restockSeconds().isPresent());
            value.restockSeconds().ifPresent(buf::writeInt);
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceUpdateOfferLimitsPacket> CODEC = StreamCodec
            .composite(
                    UUIDUtil.STREAM_CODEC, MarketplaceUpdateOfferLimitsPacket::offerId,
                    LIMIT_STREAM_CODEC, MarketplaceUpdateOfferLimitsPacket::limit,
                    MarketplaceUpdateOfferLimitsPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceUpdateOfferLimitsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !MarketplaceManager.get().canEdit(player)) {
                return;
            }
            if (MarketplaceManager.get().updateOfferLimits(packet.offerId(), packet.limit())) {
                MarketplaceManager.get().syncOpenViewers(player);
            }
        });
    }
}
