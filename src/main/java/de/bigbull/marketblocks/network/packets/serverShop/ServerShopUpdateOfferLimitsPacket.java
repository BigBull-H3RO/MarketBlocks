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

public record ServerShopUpdateOfferLimitsPacket(UUID offerId, CompoundTag limitData) implements CustomPacketPayload {
    public static final Type<ServerShopUpdateOfferLimitsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_update_limits"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopUpdateOfferLimitsPacket> CODEC = new StreamCodec<>() {
        @Override
        public ServerShopUpdateOfferLimitsPacket decode(RegistryFriendlyByteBuf buf) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            CompoundTag tag = ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf);
            return new ServerShopUpdateOfferLimitsPacket(id, tag);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerShopUpdateOfferLimitsPacket value) {
            UUIDUtil.STREAM_CODEC.encode(buf, value.offerId());
            ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, value.limitData() == null ? new CompoundTag() : value.limitData());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopUpdateOfferLimitsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !ServerShopManager.get().canEdit(player)) {
                return;
            }
            ServerShopSerialization.decodeLimit(packet.limitData(), player.server.registryAccess()).result()
                    .ifPresent(limit -> {
                        if (ServerShopManager.get().updateOfferLimits(packet.offerId(), limit)) {
                            ServerShopManager.get().syncOpenViewers(player);
                        }
                    });
        });
    }
}