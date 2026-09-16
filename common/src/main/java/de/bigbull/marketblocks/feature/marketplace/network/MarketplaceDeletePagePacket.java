package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import de.bigbull.marketblocks.platform.network.PacketContext;

public record MarketplaceDeletePagePacket(String pageName) implements CustomPacketPayload {
    public static final Type<MarketplaceDeletePagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_delete_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceDeletePagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MarketplaceManager.MAX_PAGE_NAME_LENGTH),
            MarketplaceDeletePagePacket::pageName,
            MarketplaceDeletePagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceDeletePagePacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !MarketplaceManager.get().canEdit(player)) {
                return;
            }
            MarketplaceManager.MutationResult<Void> result = MarketplaceManager.get().removePage(packet.pageName());
            if (result.isSuccess()) {
                MarketplaceManager.get().syncOpenViewers(player);
            } else {
                player.sendSystemMessage(result.errorMessage());
            }
        });
    }
}
