package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S packet to delete the current trade offer in a Small Shop.
 *
 * @param pos The {@link BlockPos} of the shop.
 */
public record DeleteOfferPacket(@NotNull BlockPos pos) implements CustomPacketPayload {
    public static final Type<DeleteOfferPacket> TYPE = new Type<>(MarketBlocks.id("delete_offer"));

    public static final StreamCodec<ByteBuf, DeleteOfferPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DeleteOfferPacket::pos,
            DeleteOfferPacket::new
    );

    @Override
    public @NotNull Type<DeleteOfferPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It verifies ownership, clears the offer, and returns the items from the
     * offer slots to the player's inventory or drops them in the world.
     */
    public static void handle(final DeleteOfferPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity) {
                if (shopEntity.isOwner(player)) {
                    // Clear the offer logic
                    shopEntity.clearOffer();

                    // Return items from payment and result slots to the owner
                    returnItemsToPlayer(level, packet.pos(), player, shopEntity.getPaymentHandler());
                    returnItemsToPlayer(level, packet.pos(), player, shopEntity.getOfferHandler());

                    // Mark the block entity as changed
                    shopEntity.updateOfferSlot();

                    // Send a status update to all players viewing this shop's menu.
                    // Note: Iterating all server players can be inefficient on large servers.
                    // A better implementation would involve the block entity tracking its viewers.
                    if (level.getServer() != null) {
                        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                            if (p.containerMenu instanceof SmallShopOffersMenu menu && menu.getBlockEntity() == shopEntity) {
                                PacketDistributor.sendToPlayer(p, new OfferStatusPacket(packet.pos(), false));
                            }
                        }
                    }

                    MarketBlocks.LOGGER.info("Player {} deleted offer at {}", player.getName().getString(), packet.pos());
                } else {
                    MarketBlocks.LOGGER.warn("Player {} tried to delete an offer in a shop they don't own at {}", player.getName().getString(), packet.pos());
                }
            }
        });
    }


    /**
     * Helper method to extract all items from an item handler and give them to a player.
     * If the player's inventory is full, items are dropped into the world.
     */
    private static void returnItemsToPlayer(Level level, BlockPos pos, ServerPlayer player, IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.extractItem(i, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    // Drop the item in the world above the shop block
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
                }
            }
        }
    }
}