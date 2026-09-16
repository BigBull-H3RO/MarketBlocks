package de.bigbull.marketblocks.feature.singleoffer.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import de.bigbull.marketblocks.platform.network.PacketContext;

/**
 * A packet sent from the client to the server to update the redstone emission
 * state of the shop.
 */
public record UpdateRedstoneSettingPacket(BlockPos pos, boolean enabled) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateRedstoneSettingPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "update_redstone_setting"));

    public static final StreamCodec<ByteBuf, UpdateRedstoneSettingPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateRedstoneSettingPacket::pos,
            ByteBufCodecs.BOOL,
            UpdateRedstoneSettingPacket::enabled,
            UpdateRedstoneSettingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateRedstoneSettingPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            if (player.containerMenu instanceof de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu menu
                    && menu.getBlockEntity() == level.getBlockEntity(packet.pos())
                    && menu.stillValid(player)
                    && menu.getBlockEntity() instanceof SingleOfferShopBlockEntity blockEntity
                    && blockEntity.isOwner(player)) {
                blockEntity.setEmitRedstone(packet.enabled(), true);
            }
        });
    }
}
