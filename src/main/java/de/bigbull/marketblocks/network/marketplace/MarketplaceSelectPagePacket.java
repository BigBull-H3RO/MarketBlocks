package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Wechselt serverseitig die ausgewählte Seite.
 */
public record MarketplaceSelectPagePacket(int pageIndex) implements CustomPacketPayload {
    public static final Type<MarketplaceSelectPagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_select_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceSelectPagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MarketplaceSelectPagePacket::pageIndex,
            MarketplaceSelectPagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceSelectPagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !(player.containerMenu instanceof MarketplaceMenu menu)) {
                return;
            }
            int pageCount = MarketplaceManager.get().snapshot().size();
            if (packet.pageIndex() < 0 || packet.pageIndex() >= pageCount) {
                return;
            }
            menu.setSelectedPageServer(packet.pageIndex());
            menu.setCurrentTradingOffer(null);
        });
    }
}