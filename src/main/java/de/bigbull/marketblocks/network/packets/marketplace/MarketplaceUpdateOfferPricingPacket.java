package de.bigbull.marketblocks.network.packets.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceManager;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceSerialization;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MarketplaceUpdateOfferPricingPacket(UUID offerId, CompoundTag pricingData) implements CustomPacketPayload {
    public static final Type<MarketplaceUpdateOfferPricingPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_update_pricing"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceUpdateOfferPricingPacket> CODEC = new StreamCodec<>() {
        @Override
        public MarketplaceUpdateOfferPricingPacket decode(RegistryFriendlyByteBuf buf) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            CompoundTag tag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            return new MarketplaceUpdateOfferPricingPacket(id, tag);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, MarketplaceUpdateOfferPricingPacket value) {
            UUIDUtil.STREAM_CODEC.encode(buf, value.offerId());
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.pricingData() == null ? new CompoundTag() : value.pricingData());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceUpdateOfferPricingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !MarketplaceManager.get().canEdit(player)) {
                return;
            }
            MarketplaceSerialization.decodePricing(packet.pricingData(), player.server.registryAccess()).result()
                    .ifPresent(pricing -> {
                        if (MarketplaceManager.get().updateOfferPricing(packet.offerId(), pricing)) {
                            MarketplaceManager.get().syncOpenViewers(player);
                        }
                    });
        });
    }
}