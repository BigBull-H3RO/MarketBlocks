package de.bigbull.marketblocks.feature.trader.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server packet sent when a player clicks the teleport button
 * in the Trade Book GUI. Replaces the previous approach of sending the
 * {@code mb_internal_tp} command from the client.
 */
public record TeleportRequestPacket(String shopId) implements CustomPacketPayload {
    public static final Type<TeleportRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "teleport_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRequestPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TeleportRequestPacket::shopId,
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

            ShopDirectorySavedData shopData = ShopDirectorySavedData.get(player.serverLevel());
            ShopDirectorySavedData.ShopEntry shop = shopData.getShopById(packet.shopId());
            
            if (shop == null) return;
            
            ServerLevel targetLevel = player.getServer().getLevel(shop.pos().dimension());
            if (targetLevel == null) return;

            BlockPos shopPos = shop.pos().pos();

            BlockEntity be = targetLevel.getBlockEntity(shopPos);
            if (!(be instanceof SingleOfferShopBlockEntity)) {
                shopData.unregisterShop(shop.pos());
                return;
            }

            if (shop.isClosed() && !player.getUUID().equals(shop.ownerUUID()) && !player.hasPermissions(2)) {
                return;
            }

            double tpX = shopPos.getX() + 0.5;
            double tpY = shopPos.getY();
            double tpZ = shopPos.getZ() + 0.5;
            float yaw = player.getYRot();
            float pitch = player.getXRot();

            BlockState state = targetLevel.getBlockState(shopPos);

            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                tpX += facing.getStepX();
                tpZ += facing.getStepZ();
                yaw = facing.getOpposite().toYRot();
                pitch = 0;
            }
            
            // Verify the position is safe/within world bounds
            if (!targetLevel.isInWorldBounds(shopPos) || !Double.isFinite(tpX) || !Double.isFinite(tpY) || !Double.isFinite(tpZ)) {
                return;
            }

            player.teleportTo(targetLevel, tpX, tpY, tpZ, yaw, pitch);
        });
    }
}
