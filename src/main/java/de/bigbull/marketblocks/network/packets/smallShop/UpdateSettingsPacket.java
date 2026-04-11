package de.bigbull.marketblocks.network.packets.smallShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.block.entity.SmallShopBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateSettingsPacket(BlockPos pos, SideMode left, SideMode right, SideMode bottom, SideMode back, String name, boolean redstone) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_side_config"));

    public static final StreamCodec<ByteBuf, UpdateSettingsPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.pos());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.left().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.right().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.bottom().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.back().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.name());
                ByteBufCodecs.BOOL.encode(buf, packet.redstone());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                SideMode left = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                SideMode right = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                SideMode bottom = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                SideMode back = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                boolean redstone = ByteBufCodecs.BOOL.decode(buf);
                return new UpdateSettingsPacket(pos, left, right, bottom, back, name, redstone);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static SideMode parseSideMode(String value) {
        try {
            return SideMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            MarketBlocks.LOGGER.warn("Received invalid side mode '{}' in UpdateSettingsPacket, defaulting to DISABLED", value);
            return SideMode.DISABLED;
        }
    }

    public static void handle(UpdateSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                Direction facing = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);
                
                // Sanitize shop name
                String name = packet.name().strip().replaceAll("[^\\p{L}\\p{N} _-]", "");

                blockEntity.updateSettingsBatch(
                        facing.getCounterClockWise(), packet.left(),
                        facing.getClockWise(), packet.right(),
                        Direction.DOWN, packet.bottom(),
                        facing.getOpposite(), packet.back(),
                        name, packet.redstone()
                );
            }
        });
    }
}
