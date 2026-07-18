package de.bigbull.marketblocks.feature.trader.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server packet sent when a player clicks the teleport button
 * in the Trade Book GUI. Replaces the previous approach of sending the
 * {@code mb_internal_tp} command from the client.
 */
public record TeleportRequestPacket(String dimension, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<TeleportRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "teleport_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRequestPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TeleportRequestPacket::dimension,
            ByteBufCodecs.DOUBLE, TeleportRequestPacket::x,
            ByteBufCodecs.DOUBLE, TeleportRequestPacket::y,
            ByteBufCodecs.DOUBLE, TeleportRequestPacket::z,
            TeleportRequestPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the teleport request on the server main thread.
     * Validates permissions and teleports the player in front of the target block,
     * accounting for horizontal facing direction.
     */
    public static void handle(TeleportRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Permission check: must be OP or config must allow non-OP teleport
            if (!Config.ALLOW_NON_OP_TELEPORT.get() && !player.hasPermissions(2)) {
                return;
            }

            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(packet.dimension()));
            ServerLevel targetLevel = player.getServer().getLevel(dim);
            if (targetLevel == null) return;

            double tpX = packet.x();
            double tpY = packet.y();
            double tpZ = packet.z();
            float yaw = player.getYRot();
            float pitch = player.getXRot();

            // Center X and Z if they are exactly integers (from the book)
            if (tpX == Math.floor(tpX)) tpX += 0.5;
            if (tpZ == Math.floor(tpZ)) tpZ += 0.5;

            BlockPos shopPos = new BlockPos((int) Math.floor(tpX), (int) Math.floor(tpY), (int) Math.floor(tpZ));
            BlockState state = targetLevel.getBlockState(shopPos);

            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                tpX += facing.getStepX();
                tpZ += facing.getStepZ();
                yaw = facing.getOpposite().toYRot();
                pitch = 0;
            }

            player.teleportTo(targetLevel, tpX, tpY, tpZ, yaw, pitch);
        });
    }
}
