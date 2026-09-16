package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.DemandPricing;
import de.bigbull.marketblocks.feature.marketplace.data.Volatility;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MarketplaceUpdateOfferPricingPacket(UUID offerId, DemandPricing pricing) implements CustomPacketPayload {
    public static final Type<MarketplaceUpdateOfferPricingPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_update_pricing"));

    private static final StreamCodec<RegistryFriendlyByteBuf, DemandPricing> PRICING_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DemandPricing decode(RegistryFriendlyByteBuf buf) {
            boolean enabled = buf.readBoolean();
            double baseMultiplier = buf.readDouble();
            Volatility volatility;
            try {
                volatility = Volatility.valueOf(ByteBufCodecs.STRING_UTF8.decode(buf));
            } catch (IllegalArgumentException e) {
                volatility = Volatility.NORMAL;
            }
            double minMultiplier = buf.readDouble();
            double maxMultiplier = buf.readDouble();
            return new DemandPricing(enabled, baseMultiplier, volatility, minMultiplier, maxMultiplier);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DemandPricing value) {
            buf.writeBoolean(value.enabled());
            buf.writeDouble(value.baseMultiplier());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.volatility().name());
            buf.writeDouble(value.minMultiplier());
            buf.writeDouble(value.maxMultiplier());
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceUpdateOfferPricingPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MarketplaceUpdateOfferPricingPacket::offerId,
            PRICING_STREAM_CODEC, MarketplaceUpdateOfferPricingPacket::pricing,
            MarketplaceUpdateOfferPricingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceUpdateOfferPricingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !MarketplaceManager.get().canEdit(player)) {
                return;
            }
            if (MarketplaceManager.get().updateOfferPricing(packet.offerId(), packet.pricing())) {
                MarketplaceManager.get().syncOpenViewers(player);
            }
        });
    }
}
