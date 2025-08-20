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

public record UpdateSideSettingsPacket(BlockPos pos, SideMode left, SideMode right, SideMode bottom, SideMode back) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSideSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_side_config"));

    public static final StreamCodec<ByteBuf, UpdateSideSettingsPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateSideSettingsPacket::pos,
            ByteBufCodecs.INT, p -> p.left().ordinal(),
            ByteBufCodecs.INT, p -> p.right().ordinal(),
            ByteBufCodecs.INT, p -> p.bottom().ordinal(),
            ByteBufCodecs.INT, p -> p.back().ordinal(),
            (pos, l, r, b, ba) -> new UpdateSideSettingsPacket(pos,
                    SideMode.values()[l], SideMode.values()[r], SideMode.values()[b], SideMode.values()[ba])
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
            }
        });
    }
}