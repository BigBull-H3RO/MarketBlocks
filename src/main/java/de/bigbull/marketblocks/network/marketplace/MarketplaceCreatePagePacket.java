package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarketplaceCreatePagePacket(String pageName) implements CustomPacketPayload {
    public static final Type<MarketplaceCreatePagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_create_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceCreatePagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MarketplaceCreatePagePacket::pageName,
            MarketplaceCreatePagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceCreatePagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && MarketplaceManager.get().canEdit(player)) {
                MarketplaceManager.MutationResult<?> result = MarketplaceManager.get().createPage(packet.pageName());
                if (result.isSuccess()) {
                    MarketplaceManager.get().syncOpenViewers(player);
                } else {
                    player.sendSystemMessage(result.errorMessage());
                }
            }
        });
    }
}