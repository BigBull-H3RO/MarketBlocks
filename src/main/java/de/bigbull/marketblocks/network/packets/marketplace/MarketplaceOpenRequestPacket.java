package de.bigbull.marketblocks.network.packets.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Fordert das Öffnen des Marktplatz-Menüs an.
 */
public record MarketplaceOpenRequestPacket() implements CustomPacketPayload {
    public static final Type<MarketplaceOpenRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_open_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceOpenRequestPacket> CODEC = StreamCodec.unit(new MarketplaceOpenRequestPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceOpenRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                MarketplaceManager.get().openShop(player);
            }
        });
    }
}