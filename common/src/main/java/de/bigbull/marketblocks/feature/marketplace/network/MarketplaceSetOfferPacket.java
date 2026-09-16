package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import de.bigbull.marketblocks.platform.network.PacketContext;

import java.util.UUID;

public record MarketplaceSetOfferPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<MarketplaceSetOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_set_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceSetOfferPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MarketplaceSetOfferPacket::offerId,
            MarketplaceSetOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceSetOfferPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MarketplaceMenu menu) {
                if (MarketplaceManager.get().isOfferOnPage(packet.offerId(), menu.selectedPage())) {
                    menu.setSelectedOffer(packet.offerId());
                } else {
                    menu.setCurrentTradingOffer(null);
                }
            }
        });
    }
}
