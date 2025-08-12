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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Paket zum Abbrechen der Angebotserstellung.
 * Serverseitig werden die Slots 24-26 geleert und die Items dem Spieler
 * zurückgegeben oder vor dem Block gedroppt.
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
                    for (int slot = 24; slot <= 26; slot++) {
                        ItemStack stack = shopEntity.getItem(slot).copy();
                        if (!stack.isEmpty()) {
                            shopEntity.setItem(slot, ItemStack.EMPTY);
                            if (!player.getInventory().add(stack)) {
                                Containers.dropItemStack(level, packet.pos().getX() + 0.5, packet.pos().getY() + 1,
                                        packet.pos().getZ() + 0.5, stack);
                            }
                        }
                    }
                    PacketDistributor.sendToPlayer(player, new CancelOfferResponsePacket(packet.pos()));
                }
            }
        });
    }
}