package de.bigbull.marketblocks.network.singleoffer;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
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
        boolean purchaseSounds,
        boolean paymentSlotSounds,
        boolean xpFeedbackSound,
        boolean offerItemVisualizationEnabled,
        float tradeStandOfferScaleMultiplier,
        float tradeStandOfferRotationSpeed,
        float tradeStandOfferHeightOffset,
        int marketCrateDisplayCount,
        float marketCrateOfferHeightOffset,
        float marketCrateOfferRotationSpeed,
        boolean marketCrateRandomPlacement,
        boolean marketCrateStableRandom
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
                ByteBufCodecs.BOOL.encode(buf, packet.paymentSlotSounds());
                ByteBufCodecs.BOOL.encode(buf, packet.xpFeedbackSound());
                ByteBufCodecs.BOOL.encode(buf, packet.offerItemVisualizationEnabled());
                ByteBufCodecs.FLOAT.encode(buf, packet.tradeStandOfferScaleMultiplier());
                ByteBufCodecs.FLOAT.encode(buf, packet.tradeStandOfferRotationSpeed());
                ByteBufCodecs.FLOAT.encode(buf, packet.tradeStandOfferHeightOffset());
                ByteBufCodecs.VAR_INT.encode(buf, packet.marketCrateDisplayCount());
                ByteBufCodecs.FLOAT.encode(buf, packet.marketCrateOfferHeightOffset());
                ByteBufCodecs.FLOAT.encode(buf, packet.marketCrateOfferRotationSpeed());
                ByteBufCodecs.BOOL.encode(buf, packet.marketCrateRandomPlacement());
                ByteBufCodecs.BOOL.encode(buf, packet.marketCrateStableRandom());
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
                boolean paymentSlotSounds = ByteBufCodecs.BOOL.decode(buf);
                boolean xpFeedbackSound = ByteBufCodecs.BOOL.decode(buf);
                boolean offerItemVisualizationEnabled = ByteBufCodecs.BOOL.decode(buf);
                float tradeStandOfferScaleMultiplier = ByteBufCodecs.FLOAT.decode(buf);
                float tradeStandOfferRotationSpeed = ByteBufCodecs.FLOAT.decode(buf);
                float tradeStandOfferHeightOffset = ByteBufCodecs.FLOAT.decode(buf);
                int marketCrateDisplayCount = ByteBufCodecs.VAR_INT.decode(buf);
                float marketCrateOfferHeightOffset = ByteBufCodecs.FLOAT.decode(buf);
                float marketCrateOfferRotationSpeed = ByteBufCodecs.FLOAT.decode(buf);
                boolean marketCrateRandomPlacement = ByteBufCodecs.BOOL.decode(buf);
                boolean marketCrateStableRandom = ByteBufCodecs.BOOL.decode(buf);
                return new UpdateSettingsPacket(pos, left, right, bottom, back, name, redstone,
                        npcEnabled, npcName, npcProfession, purchaseParticles, purchaseSounds, paymentSlotSounds, xpFeedbackSound,
                        offerItemVisualizationEnabled, tradeStandOfferScaleMultiplier, tradeStandOfferRotationSpeed, tradeStandOfferHeightOffset,
                        marketCrateDisplayCount, marketCrateOfferHeightOffset, marketCrateOfferRotationSpeed,
                        marketCrateRandomPlacement, marketCrateStableRandom);
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
                        packet.purchaseSounds(),
                        packet.paymentSlotSounds(),
                        packet.offerItemVisualizationEnabled(),
                        packet.tradeStandOfferScaleMultiplier(),
                        packet.tradeStandOfferRotationSpeed(),
                        packet.tradeStandOfferHeightOffset(),
                        packet.marketCrateDisplayCount(),
                        packet.marketCrateOfferHeightOffset(),
                        packet.marketCrateOfferRotationSpeed(),
                        packet.marketCrateRandomPlacement(),
                        packet.marketCrateStableRandom()
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
                        name, packet.redstone(), visuals, packet.xpFeedbackSound()
                );
            }
        });
    }
}
