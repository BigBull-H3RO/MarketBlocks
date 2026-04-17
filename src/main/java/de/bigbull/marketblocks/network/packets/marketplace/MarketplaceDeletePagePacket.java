package de.bigbull.marketblocks.network.packets.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarketplaceDeletePagePacket(String pageName) implements CustomPacketPayload {
    public static final Type<MarketplaceDeletePagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_delete_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceDeletePagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MarketplaceDeletePagePacket::pageName,
            MarketplaceDeletePagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceDeletePagePacket packet, IPayloadContext context) {
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