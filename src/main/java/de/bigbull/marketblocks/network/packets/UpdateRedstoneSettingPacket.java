package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S packet to update the "Emit Redstone" setting for a shop.
 *
 * @param pos     The {@link BlockPos} of the shop.
 * @param enabled The new value for the redstone setting.
 */
public record UpdateRedstoneSettingPacket(@NotNull BlockPos pos, boolean enabled) implements CustomPacketPayload {

    public static final Type<UpdateRedstoneSettingPacket> TYPE = new Type<>(MarketBlocks.id("update_redstone_setting"));


    public static final StreamCodec<ByteBuf, UpdateRedstoneSettingPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateRedstoneSettingPacket::pos,
            ByteBufCodecs.BOOL, UpdateRedstoneSettingPacket::enabled,
            UpdateRedstoneSettingPacket::new
    );

    @Override
    public @NotNull Type<UpdateRedstoneSettingPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It validates that the player is the owner of the shop and then updates the setting.
     */
    public static void handle(final UpdateRedstoneSettingPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                blockEntity.setEmitRedstone(packet.enabled());
            }
        });
    }
}