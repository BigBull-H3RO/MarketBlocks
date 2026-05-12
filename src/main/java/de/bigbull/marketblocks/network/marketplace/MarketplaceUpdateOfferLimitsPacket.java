package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceSerialization;
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

public record MarketplaceUpdateOfferLimitsPacket(UUID offerId, CompoundTag limitData) implements CustomPacketPayload {
    public static final Type<MarketplaceUpdateOfferLimitsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_update_limits"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceUpdateOfferLimitsPacket> CODEC = new StreamCodec<>() {
        @Override
        public MarketplaceUpdateOfferLimitsPacket decode(RegistryFriendlyByteBuf buf) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            CompoundTag tag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            return new MarketplaceUpdateOfferLimitsPacket(id, tag);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, MarketplaceUpdateOfferLimitsPacket value) {
            UUIDUtil.STREAM_CODEC.encode(buf, value.offerId());
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.limitData() == null ? new CompoundTag() : value.limitData());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceUpdateOfferLimitsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !MarketplaceManager.get().canEdit(player)) {
                return;
            }
            MarketplaceSerialization.decodeLimit(packet.limitData(), player.server.registryAccess()).result()
                    .ifPresent(limit -> {
                        if (MarketplaceManager.get().updateOfferLimits(packet.offerId(), limit)) {
                            MarketplaceManager.get().syncOpenViewers(player);
                        }
                    });
        });
    }
}