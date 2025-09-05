package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopOffersScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * S2C packet to sync the offer status (if one exists) of a shop to the client.
 * Also triggers a screen update if the offer was deleted.
 *
 * @param pos      The {@link BlockPos} of the shop.
 * @param hasOffer True if the shop has an offer, false otherwise.
 */
public record OfferStatusPacket(@NotNull BlockPos pos, boolean hasOffer) implements CustomPacketPayload {
    public static final Type<OfferStatusPacket> TYPE = new Type<>(MarketBlocks.id("offer_status"));

    public static final StreamCodec<ByteBuf, OfferStatusPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OfferStatusPacket::pos,
            ByteBufCodecs.BOOL, OfferStatusPacket::hasOffer,
            OfferStatusPacket::new
    );

    @Override
    public @NotNull Type<OfferStatusPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the client side.
     */
    public static void handle(final OfferStatusPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> handleClient(packet));
    }

    /**
     * Client-side logic for handling the packet.
     * Updates the block entity's client-side state and refreshes the screen if necessary.
     */
    private static void handleClient(final OfferStatusPacket packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null || !(level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity)) {
            return;
        }

        shopEntity.setHasOfferClient(packet.hasOffer());

        if (Minecraft.getInstance().player.containerMenu instanceof SmallShopOffersMenu m)
            m.refreshFlags();

        // If the player is viewing the correct screen, trigger a refresh.
        if (Minecraft.getInstance().screen instanceof SmallShopOffersScreen screen && screen.isFor(packet.pos())) {
            if (packet.hasOffer()) {
                screen.onOfferCreated();
            } else {
                screen.onOfferDeleted();
            }
        }
    }
}