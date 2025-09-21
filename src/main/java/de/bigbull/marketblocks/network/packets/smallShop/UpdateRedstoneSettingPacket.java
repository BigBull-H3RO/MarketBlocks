package de.bigbull.marketblocks.network.packets.smallShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateRedstoneSettingPacket(BlockPos pos, boolean enabled) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateRedstoneSettingPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_redstone_setting"));

    public static final StreamCodec<ByteBuf, UpdateRedstoneSettingPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateRedstoneSettingPacket::pos,
            ByteBufCodecs.BOOL,
            UpdateRedstoneSettingPacket::enabled,
            UpdateRedstoneSettingPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateRedstoneSettingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                blockEntity.setEmitRedstone(packet.enabled());
            }
        });
    }
}