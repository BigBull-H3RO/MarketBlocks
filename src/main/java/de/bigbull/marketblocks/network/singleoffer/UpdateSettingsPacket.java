package de.bigbull.marketblocks.network.singleoffer;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
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

public record UpdateSettingsPacket(
        BlockPos pos,
        SideMode left,
        SideMode right,
        SideMode bottom,
        SideMode back,
        String name,
        boolean redstone,
        ShopVisualSettings visualSettings,
        boolean xpFeedbackSound
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "update_side_config"));

    private static final SideMode[] SIDE_MODE_VALUES = SideMode.values();
    private static final StreamCodec<ByteBuf, SideMode> SIDE_MODE_CODEC = ByteBufCodecs.VAR_INT.map(
            value -> value >= 0 && value < SIDE_MODE_VALUES.length ? SIDE_MODE_VALUES[value] : SideMode.DISABLED,
            SideMode::ordinal
    );

    public static final StreamCodec<ByteBuf, UpdateSettingsPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.pos());
                SIDE_MODE_CODEC.encode(buf, packet.left());
                SIDE_MODE_CODEC.encode(buf, packet.right());
                SIDE_MODE_CODEC.encode(buf, packet.bottom());
                SIDE_MODE_CODEC.encode(buf, packet.back());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.name());
                ByteBufCodecs.BOOL.encode(buf, packet.redstone());
                ShopVisualSettings.STREAM_CODEC.encode(buf, packet.visualSettings());
                ByteBufCodecs.BOOL.encode(buf, packet.xpFeedbackSound());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                SideMode left = SIDE_MODE_CODEC.decode(buf);
                SideMode right = SIDE_MODE_CODEC.decode(buf);
                SideMode bottom = SIDE_MODE_CODEC.decode(buf);
                SideMode back = SIDE_MODE_CODEC.decode(buf);
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                boolean redstone = ByteBufCodecs.BOOL.decode(buf);
                ShopVisualSettings visualSettings = ShopVisualSettings.STREAM_CODEC.decode(buf);
                boolean xpFeedbackSound = ByteBufCodecs.BOOL.decode(buf);
                return new UpdateSettingsPacket(pos, left, right, bottom, back, name, redstone,
                        visualSettings, xpFeedbackSound);
            }
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSettingsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SingleOfferShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                Direction facing = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);
                
                // Sanitize shop name
                String name = packet.name().strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
                ShopVisualSettings visuals = packet.visualSettings();

                if (visuals.npcEnabled() && !ShopVisualPlacementValidator.validate(level, packet.pos(), facing).canSpawn()) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.marketblocks.visual_npc.space_blocked"));
                    visuals = visuals.withNpcEnabled(false);
                }

                blockEntity.updateSettingsBatch(
                        facing.getCounterClockWise(), packet.left(),
                        facing.getClockWise(), packet.right(),
                        Direction.DOWN, packet.bottom(),
                        facing.getOpposite(), packet.back(),
                        name, packet.redstone(), visuals, packet.xpFeedbackSound()
                );
            }
        });
    }
}
