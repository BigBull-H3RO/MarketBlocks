package de.bigbull.marketblocks.network.packets.singleOfferShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.singleoffer.SideMode;
import de.bigbull.marketblocks.shop.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.visual.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.shop.visual.ShopVisualSettings;
import de.bigbull.marketblocks.shop.visual.VillagerVisualProfession;
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
        boolean npcEnabled,
        String npcName,
        String npcProfession,
        boolean purchaseParticles,
        boolean purchaseSounds
) implements CustomPacketPayload {
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
                ByteBufCodecs.BOOL.encode(buf, packet.npcEnabled());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.npcName());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.npcProfession());
                ByteBufCodecs.BOOL.encode(buf, packet.purchaseParticles());
                ByteBufCodecs.BOOL.encode(buf, packet.purchaseSounds());
            },
            buf -> {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                SideMode left = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                SideMode right = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                SideMode bottom = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                SideMode back = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                boolean redstone = ByteBufCodecs.BOOL.decode(buf);
                boolean npcEnabled = ByteBufCodecs.BOOL.decode(buf);
                String npcName = ByteBufCodecs.STRING_UTF8.decode(buf);
                String npcProfession = ByteBufCodecs.STRING_UTF8.decode(buf);
                boolean purchaseParticles = ByteBufCodecs.BOOL.decode(buf);
                boolean purchaseSounds = ByteBufCodecs.BOOL.decode(buf);
                return new UpdateSettingsPacket(pos, left, right, bottom, back, name, redstone,
                        npcEnabled, npcName, npcProfession, purchaseParticles, purchaseSounds);
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
            if (level.getBlockEntity(packet.pos()) instanceof SingleOfferShopBlockEntity blockEntity && blockEntity.isOwner(player)) {
                Direction facing = blockEntity.getBlockState().getValue(BaseShopBlock.FACING);
                
                // Sanitize shop name
                String name = packet.name().strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
                ShopVisualSettings visuals = new ShopVisualSettings(
                        packet.npcEnabled(),
                        packet.npcName(),
                        VillagerVisualProfession.fromSerialized(packet.npcProfession()),
                        packet.purchaseParticles(),
                        packet.purchaseSounds()
                );

                if (visuals.npcEnabled() && !ShopVisualPlacementValidator.validate(level, packet.pos(), facing).canSpawn()) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.marketblocks.visual_npc.space_blocked"));
                    visuals = visuals.withNpcEnabled(false);
                }

                blockEntity.updateSettingsBatch(
                        facing.getCounterClockWise(), packet.left(),
                        facing.getClockWise(), packet.right(),
                        Direction.DOWN, packet.bottom(),
                        facing.getOpposite(), packet.back(),
                        name, packet.redstone(), visuals
                );
            }
        });
    }
}
