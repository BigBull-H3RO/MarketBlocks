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

public record MarketplaceRenamePagePacket(String oldName, String newName) implements CustomPacketPayload {
    public static final Type<MarketplaceRenamePagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_rename_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceRenamePagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MarketplaceRenamePagePacket::oldName,
            ByteBufCodecs.STRING_UTF8,
            MarketplaceRenamePagePacket::newName,
            MarketplaceRenamePagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceRenamePagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !MarketplaceManager.get().canEdit(player)) {
                return;
            }
            MarketplaceManager.MutationResult<Void> result = MarketplaceManager.get().renamePage(packet.oldName(), packet.newName());
            if (result.isSuccess()) {
                MarketplaceManager.get().syncOpenViewers(player);
            } else {
                player.sendSystemMessage(result.errorMessage());
            }
        });
    }
}