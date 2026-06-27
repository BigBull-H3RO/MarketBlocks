package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server payload sent when a player requests to open the Marketplace GUI,
 * typically triggered via the dedicated keybind (default: 'O').
 */
public record MarketplaceOpenRequestPacket() implements CustomPacketPayload {
    public static final Type<MarketplaceOpenRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_open_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceOpenRequestPacket> CODEC = StreamCodec.unit(new MarketplaceOpenRequestPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the incoming request on the server main thread.
     * Opens the Marketplace menu for the sender and triggers the associated advancement.
     *
     * @param packet  The received packet instance.
     * @param context The network payload context containing the sending player.
     */
    public static void handle(MarketplaceOpenRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                MarketplaceManager.get().openShop(player);
                RegistriesInit.MARKETPLACE_OPEN_TRIGGER.get().trigger(player);
            }
        });
    }
}
