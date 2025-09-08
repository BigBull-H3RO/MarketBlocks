package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeleteOfferPacket(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DeleteOfferPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "delete_offer"));

    public static final StreamCodec<ByteBuf, DeleteOfferPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DeleteOfferPacket::pos,
            DeleteOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeleteOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity) {
                // Prüfe ob Spieler der Owner ist
                if (shopEntity.isOwner(player)) {
                    // Lösche das Angebot komplett
                    shopEntity.clearOffer();

                    // Leere auch die aktuellen Payment und Offer Slots
                    for (int i = 0; i < 2; i++) {
                        ItemStack stack = shopEntity.getPaymentHandler().extractItem(i, Integer.MAX_VALUE, false);
                        if (!stack.isEmpty()) {
                            if (!player.getInventory().add(stack)) {
                                Containers.dropItemStack(level, packet.pos().getX() + 0.5,
                                        packet.pos().getY() + 1,
                                        packet.pos().getZ() + 0.5, stack);
                            }
                        }
                    }

                    ItemStack resultStack = shopEntity.getOfferHandler().extractItem(0, Integer.MAX_VALUE, false);
                    if (!resultStack.isEmpty()) {
                        if (!player.getInventory().add(resultStack)) {
                            Containers.dropItemStack(level, packet.pos().getX() + 0.5,
                                    packet.pos().getY() + 1,
                                    packet.pos().getZ() + 0.5, resultStack);
                        }
                    }

                    shopEntity.updateOfferSlot();

                    // Sende Status-Update an alle Spieler mit geöffnetem Menü
                    ServerLevel serverLevel = (ServerLevel) level;
                    for (ServerPlayer p : serverLevel.players()) {
                        if (p.containerMenu instanceof SmallShopMenu menu && menu.getBlockEntity() == shopEntity) {
                            PacketDistributor.sendToPlayer(p, new OfferStatusPacket(packet.pos(), false));
                        }
                    }

                    MarketBlocks.LOGGER.info("Player {} deleted offer at {}", player.getName().getString(), packet.pos());
                }
            }
        });
    }
}