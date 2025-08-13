package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serverseitig werden die relevanten Payment- und Offer-Slots geleert und die
 * Items dem Spieler zurückgegeben oder vor dem Block gedroppt.
 */
public record CancelOfferPacket(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CancelOfferPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "cancel_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelOfferPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CancelOfferPacket::pos,
            CancelOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CancelOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity) {
                if (shopEntity.isOwner(player)) {
                    int[] slotIndices = {0, 1, 0};
                    for (int i = 0; i < slotIndices.length; i++) {
                        ItemStack stack;
                        if (i < 2) {
                            stack = shopEntity.getPaymentHandler().extractItem(slotIndices[i], Integer.MAX_VALUE, false);
                        } else {
                            stack = shopEntity.getOfferHandler().extractItem(slotIndices[i], Integer.MAX_VALUE, false);
                        }
                        returnOrDropItem(player, level, packet.pos(), stack);
                    }
                }
            }
        });
    }

    private static void returnOrDropItem(ServerPlayer player, Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
        }
    }
}