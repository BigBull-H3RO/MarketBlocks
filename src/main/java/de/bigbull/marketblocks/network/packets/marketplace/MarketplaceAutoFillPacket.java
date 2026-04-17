package de.bigbull.marketblocks.network.packets.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.MarketplaceMenu;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceManager;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceOffer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MarketplaceAutoFillPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<MarketplaceAutoFillPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_auto_fill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceAutoFillPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MarketplaceAutoFillPacket::offerId,
            MarketplaceAutoFillPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceAutoFillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MarketplaceMenu menu) {
                MarketplaceOffer offer = MarketplaceManager.get().findOffer(packet.offerId());
                if (offer != null) {
                    // WICHTIG: Erst Angebot setzen (damit slotsChanged weiß was zu tun ist)
                    menu.setCurrentTradingOffer(offer);
                    // Dann Items einfüllen -> triggert slotsChanged -> Result Slot erscheint
                    menu.autoFillPayment(player, offer);
                }
            }
        });
    }
}