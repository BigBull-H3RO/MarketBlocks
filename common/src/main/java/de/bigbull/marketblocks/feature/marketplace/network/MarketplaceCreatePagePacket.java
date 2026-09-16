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
import de.bigbull.marketblocks.util.NameValidator;

public record MarketplaceCreatePagePacket(String pageName) implements CustomPacketPayload {
    public static final Type<MarketplaceCreatePagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_create_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceCreatePagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MarketplaceManager.MAX_PAGE_NAME_LENGTH),
            MarketplaceCreatePagePacket::pageName,
            MarketplaceCreatePagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceCreatePagePacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && MarketplaceManager.get().canEdit(player)) {
                String sanitizedName = NameValidator.sanitizePageName(packet.pageName());
                MarketplaceManager.MutationResult<?> result = MarketplaceManager.get().createPage(sanitizedName);
                if (result.isSuccess()) {
                    MarketplaceManager.get().syncOpenViewers(player);
                } else {
                    player.sendSystemMessage(result.errorMessage());
                }
            }
        });
    }
}
