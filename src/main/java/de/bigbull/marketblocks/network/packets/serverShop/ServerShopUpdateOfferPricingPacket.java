package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopSerialization;
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

public record ServerShopUpdateOfferPricingPacket(UUID offerId, CompoundTag pricingData) implements CustomPacketPayload {
    public static final Type<ServerShopUpdateOfferPricingPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_update_pricing"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopUpdateOfferPricingPacket> CODEC = new StreamCodec<>() {
        @Override
        public ServerShopUpdateOfferPricingPacket decode(RegistryFriendlyByteBuf buf) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            CompoundTag tag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            return new ServerShopUpdateOfferPricingPacket(id, tag);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerShopUpdateOfferPricingPacket value) {
            UUIDUtil.STREAM_CODEC.encode(buf, value.offerId());
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.pricingData() == null ? new CompoundTag() : value.pricingData());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopUpdateOfferPricingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !ServerShopManager.get().canEdit(player)) {
                return;
            }
            ServerShopSerialization.decodePricing(packet.pricingData(), player.server.registryAccess()).result()
                    .ifPresent(pricing -> {
                        if (ServerShopManager.get().updateOfferPricing(packet.offerId(), pricing)) {
                            ServerShopManager.get().syncOpenViewers(player);
                        }
                    });
        });
    }
}