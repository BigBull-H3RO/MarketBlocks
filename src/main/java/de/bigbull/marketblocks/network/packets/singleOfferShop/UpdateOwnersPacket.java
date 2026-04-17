package de.bigbull.marketblocks.network.packets.singleOfferShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.block.entity.SingleOfferShopBlockEntity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateOwnersPacket(BlockPos pos, List<UUID> owners) implements CustomPacketPayload {
    private static final int MAX_OWNERS_PER_UPDATE = 64;

    public static final CustomPacketPayload.Type<UpdateOwnersPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_owners"));

    public static final StreamCodec<ByteBuf, UpdateOwnersPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.pos());
                int cappedSize = Math.min(packet.owners().size(), MAX_OWNERS_PER_UPDATE);
                ByteBufCodecs.VAR_INT.encode(buf, cappedSize);
                int written = 0;
                for (UUID id : packet.owners()) {
                    if (written >= cappedSize) {
                        break;
                    }
                    UUIDUtil.STREAM_CODEC.encode(buf, id);
                    written++;
                }
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                int size = Math.max(0, ByteBufCodecs.VAR_INT.decode(buf));
                List<UUID> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    UUID decoded = UUIDUtil.STREAM_CODEC.decode(buf);
                    if (i < MAX_OWNERS_PER_UPDATE) {
                        list.add(decoded);
                    }
                }
                if (size > MAX_OWNERS_PER_UPDATE) {
                    MarketBlocks.LOGGER.warn("Received owner update packet with {} entries, capped to {}",
                            size, MAX_OWNERS_PER_UPDATE);
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
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SingleOfferShopBlockEntity blockEntity
                    && blockEntity.isPrimaryOwner(player)) {
                Map<UUID, String> map = new HashMap<>();
                int processed = 0;
                for (UUID id : packet.owners()) {
                    if (processed >= MAX_OWNERS_PER_UPDATE) {
                        MarketBlocks.LOGGER.warn("Owner update from {} exceeded {} entries, truncating",
                                player.getName().getString(), MAX_OWNERS_PER_UPDATE);
                        break;
                    }
                    ServerPlayer sp = level.getServer().getPlayerList().getPlayer(id);
                    String name = sp != null ? sp.getName().getString() : "";
                    map.put(id, name);
                    processed++;
                }
                // setAdditionalOwners() already calls sync() internally
                blockEntity.setAdditionalOwners(map);
            }
        });
    }
}
