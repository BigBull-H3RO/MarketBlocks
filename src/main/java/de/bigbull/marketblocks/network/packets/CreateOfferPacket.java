package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.OfferManager;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A packet sent from the client to the server to create a new trade offer in a shop block.
 *
 * @param pos       The position of the shop block.
 * @param payment1  The first item required for the trade.
 * @param payment2  The second item required for the trade (can be empty).
 * @param result    The item given as a result of the trade.
 */
public record CreateOfferPacket(BlockPos pos, ItemStack payment1, ItemStack payment2, ItemStack result)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateOfferPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "create_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreateOfferPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CreateOfferPacket::pos,
            ItemStack.OPTIONAL_STREAM_CODEC,
            CreateOfferPacket::payment1,
            ItemStack.OPTIONAL_STREAM_CODEC,
            CreateOfferPacket::payment2,
            ItemStack.STREAM_CODEC,
            CreateOfferPacket::result,
            CreateOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It verifies that the player is the owner of the shop and then applies the new offer.
     *
     * @param packet  The packet instance.
     * @param context The context of the packet handling.
     */
    public static void handle(CreateOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();

                if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity && shopEntity.isOwner(player)) {
                    OfferManager manager = shopEntity.getOfferManager();
                    if (!manager.applyOffer(player, packet.payment1(), packet.payment2(), packet.result())) {
                        MarketBlocks.LOGGER.warn("Invalid offer creation attempt by player {}", player.getName().getString());
                    }
                }
            }
        });
    }
}