package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S packet to update all settings of a shop from the settings screen.
 *
 * @param pos      The {@link BlockPos} of the shop.
 * @param left     The {@link SideMode} for the left side of the shop.
 * @param right    The {@link SideMode} for the right side of the shop.
 * @param bottom   The {@link SideMode} for the bottom side of the shop.
 * @param back     The {@link SideMode} for the back side of the shop.
 * @param name     The new name for the shop.
 * @param redstone The new value for the redstone setting.
 */
public record UpdateSettingsPacket(
        @NotNull BlockPos pos,
        @NotNull SideMode left,
        @NotNull SideMode right,
        @NotNull SideMode bottom,
        @NotNull SideMode back,
        @NotNull String name,
        boolean redstone
) implements CustomPacketPayload {

    public static final Type<UpdateSettingsPacket> TYPE = new Type<>(MarketBlocks.id("update_settings"));

    private static final StreamCodec<ByteBuf, SideMode> SIDE_MODE_CODEC = ByteBufCodecs.idMapper(SideMode::fromId, SideMode::ordinal);

    public static final StreamCodec<ByteBuf, UpdateSettingsPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateSettingsPacket::pos,
            SIDE_MODE_CODEC, UpdateSettingsPacket::left,
            SIDE_MODE_CODEC, UpdateSettingsPacket::right,
            SIDE_MODE_CODEC, UpdateSettingsPacket::bottom,
            SIDE_MODE_CODEC, UpdateSettingsPacket::back,
            ByteBufCodecs.STRING_UTF8, UpdateSettingsPacket::name,
            ByteBufCodecs.BOOL, UpdateSettingsPacket::redstone,
            UpdateSettingsPacket::new
    );

    @Override
    public @NotNull Type<UpdateSettingsPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * Validates ownership and applies all the new settings to the shop.
     */
    public static void handle(final UpdateSettingsPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                Direction facing = blockEntity.getBlockState().getValue(SmallShopBlock.FACING);
                blockEntity.setMode(facing.getCounterClockWise(), packet.left());
                blockEntity.setMode(facing.getClockWise(), packet.right());
                blockEntity.setMode(Direction.DOWN, packet.bottom());
                blockEntity.setMode(facing.getOpposite(), packet.back());

                // Sanitize the shop name to prevent issues
                String sanitizedName = packet.name().strip().replaceAll("[^A-Za-z0-9 _-]", "");
                blockEntity.setShopName(sanitizedName);
                blockEntity.setEmitRedstone(packet.redstone());
            }
        });
    }
}