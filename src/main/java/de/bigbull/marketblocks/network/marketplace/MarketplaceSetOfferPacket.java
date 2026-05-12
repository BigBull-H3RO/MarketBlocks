package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MarketplaceSetOfferPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<MarketplaceSetOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_set_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceSetOfferPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MarketplaceSetOfferPacket::offerId,
            MarketplaceSetOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceSetOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MarketplaceMenu menu) {
                if (MarketplaceManager.get().isOfferOnPage(packet.offerId(), menu.selectedPage())) {
                    // Sets the offer server-side so slotsChanged can compute result availability correctly.
                    menu.setSelectedOffer(packet.offerId());
                } else {
                    menu.setCurrentTradingOffer(null);
                }
            }
        });
    }
}
