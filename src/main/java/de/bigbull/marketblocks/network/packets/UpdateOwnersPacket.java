package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public record UpdateOwnersPacket(BlockPos pos, List<UUID> owners) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateOwnersPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_owners"));

    public static final StreamCodec<ByteBuf, UpdateOwnersPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.pos());
                ByteBufCodecs.VAR_INT.encode(buf, packet.owners().size());
                for (UUID id : packet.owners()) {
                    UUIDUtil.STREAM_CODEC.encode(buf, id);
                }
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                List<UUID> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(UUIDUtil.STREAM_CODEC.decode(buf));
                }
                return new UpdateOwnersPacket(pos, list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateOwnersPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                Map<UUID, String> map = new HashMap<>();
                for (UUID id : packet.owners()) {
                    ServerPlayer sp = level.getServer().getPlayerList().getPlayer(id);
                    String name = sp != null ? sp.getName().getString() : "";
                    map.put(id, name);
                }
                blockEntity.setAdditionalOwners(map);
                blockEntity.sync();
                blockEntity.setChanged();
            }
        });
    }
}