package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.OfferManager;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S packet to create a new trade offer in a Small Shop.
 *
 * @param pos      The {@link BlockPos} of the shop.
 * @param payment1 The first item required for the trade (can be empty).
 * @param payment2 The second item required for the trade (can be empty).
 * @param result   The item given as a result of the trade.
 */
public record CreateOfferPacket(
        @NotNull BlockPos pos,
        @NotNull ItemStack payment1,
        @NotNull ItemStack payment2,
        @NotNull ItemStack result
) implements CustomPacketPayload {
    public static final Type<CreateOfferPacket> TYPE = new Type<>(MarketBlocks.id("create_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreateOfferPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CreateOfferPacket::pos,
            ItemStack.OPTIONAL_STREAM_CODEC, CreateOfferPacket::payment1,
            ItemStack.OPTIONAL_STREAM_CODEC, CreateOfferPacket::payment2,
            ItemStack.STREAM_CODEC, CreateOfferPacket::result,
            CreateOfferPacket::new
    );

    @Override
    public @NotNull Type<CreateOfferPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It verifies that the player is the owner of the shop and then creates the new offer.
     *
     * @param packet  The packet instance.
     * @param context The context of the packet handling.
     */
    public static void handle(final CreateOfferPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
                Level level = player.level();

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity) {
                if (shopEntity.isOwner(player)) {
                    OfferManager manager = shopEntity.getOfferManager();
                    boolean success = manager.applyOffer(player, packet.payment1(), packet.payment2(), packet.result());
                    if (!success) {
                        MarketBlocks.LOGGER.warn("Invalid offer creation attempt by player {}", player.getName().getString());
                    }
                } else {
                    MarketBlocks.LOGGER.warn("Player {} tried to create an offer in a shop they don't own at {}", player.getName().getString(), packet.pos());
                }
            }
        });
    }
}