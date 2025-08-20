package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
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

public record UpdateSideSettingsPacket(BlockPos pos, SideMode left, SideMode right, SideMode bottom, SideMode back, String name, boolean redstone) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSideSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_side_config"));

    public static final StreamCodec<ByteBuf, UpdateSideSettingsPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.pos());
                ByteBufCodecs.INT.encode(buf, packet.left().ordinal());
                ByteBufCodecs.INT.encode(buf, packet.right().ordinal());
                ByteBufCodecs.INT.encode(buf, packet.bottom().ordinal());
                ByteBufCodecs.INT.encode(buf, packet.back().ordinal());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.name());
                ByteBufCodecs.BOOL.encode(buf, packet.redstone());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                SideMode left = SideMode.values()[ByteBufCodecs.INT.decode(buf)];
                SideMode right = SideMode.values()[ByteBufCodecs.INT.decode(buf)];
                SideMode bottom = SideMode.values()[ByteBufCodecs.INT.decode(buf)];
                SideMode back = SideMode.values()[ByteBufCodecs.INT.decode(buf)];
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                boolean redstone = ByteBufCodecs.BOOL.decode(buf);
                return new UpdateSideSettingsPacket(pos, left, right, bottom, back, name, redstone);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSideSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                blockEntity.setLeftMode(packet.left());
                blockEntity.setRightMode(packet.right());
                blockEntity.setBottomMode(packet.bottom());
                blockEntity.setBackMode(packet.back());
                blockEntity.setShopName(packet.name());
                blockEntity.setEmitRedstone(packet.redstone());
            }
        });
    }
}