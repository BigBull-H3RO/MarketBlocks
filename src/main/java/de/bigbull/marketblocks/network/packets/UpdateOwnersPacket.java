package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * C2S packet to update the list of additional owners for a shop.
 *
 * @param pos    The {@link BlockPos} of the shop.
 * @param owners A list of {@link UUID}s for the additional owners.
 */
public record UpdateOwnersPacket(@NotNull BlockPos pos, @NotNull List<UUID> owners) implements CustomPacketPayload {
    public static final Type<UpdateOwnersPacket> TYPE = new Type<>(MarketBlocks.id("update_owners"));

    public static final StreamCodec<ByteBuf, UpdateOwnersPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateOwnersPacket::pos,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateOwnersPacket::owners,
            UpdateOwnersPacket::new
    );

    @Override
    public @NotNull Type<UpdateOwnersPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It validates ownership, resolves the UUIDs to player names, and updates the shop's owner list.
     */
    public static void handle(final UpdateOwnersPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            MinecraftServer server = level.getServer();
            if (server == null) {
                return;
            }

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                Map<UUID, String> ownerMap = new HashMap<>();
                for (UUID id : packet.owners()) {
                    ServerPlayer ownerPlayer = server.getPlayerList().getPlayer(id);
                    // Name is stored as empty if the player is not online.
                    // The client will need to resolve this later if needed.
                    String name = ownerPlayer != null ? ownerPlayer.getName().getString() : "";
                    ownerMap.put(id, name);
                }
                blockEntity.setAdditionalOwners(ownerMap);
            }
        });
    }
}