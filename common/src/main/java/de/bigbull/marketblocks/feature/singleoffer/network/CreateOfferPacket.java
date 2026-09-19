package de.bigbull.marketblocks.feature.singleoffer.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.SingleOfferConfig;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.singleoffer.entity.OfferManager;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import de.bigbull.marketblocks.platform.network.PacketContext;

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
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "create_offer"));

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
     * It verifies that the player is allowed to create an offer (either unowned shop or owner) and then applies the new offer.
     *
     * @param packet  The packet instance.
     * @param context The context of the packet handling.
     */
    public static void handle(CreateOfferPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();

                if (player.containerMenu instanceof de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu menu
                        && menu.getBlockEntity() == level.getBlockEntity(packet.pos())
                        && menu.stillValid(player)
                        && menu.getBlockEntity() instanceof SingleOfferShopBlockEntity shopEntity
                        && (shopEntity.getOwnerId() == null || shopEntity.isOwner(player))) {
                    // Validate shop limit on server before creating offer
                    if (!shopEntity.hasOffer() && !player.isCreative()
                            && !(player.hasPermissions(2) && MarketplaceManager.get().isEditModeEnabled(player))) {
                        int maxShops = SingleOfferConfig.MAX_SHOPS_PER_PLAYER.get();
                        if (maxShops >= 0 && level instanceof ServerLevel serverLevel) {
                            long activeShops = ShopDirectorySavedData.get(serverLevel).getShops().stream()
                                    .filter(s -> player.getUUID().equals(s.ownerUUID()) && !s.result().isEmpty())
                                    .count();
                            if (activeShops >= maxShops) {
                                player.displayClientMessage(
                                        Component.translatable("gui.marketblocks.error.shop_limit_reached", maxShops),
                                        true);
                                return;
                            }
                        }
                    }
                    OfferManager manager = shopEntity.getOfferManager();
                    if (!manager.applyOffer(player, packet.payment1(), packet.payment2(), packet.result())) {
                        Constants.LOG.warn("Invalid offer creation attempt by player {}", player.getName().getString());
                    }
                }
            }
        });
    }
}
