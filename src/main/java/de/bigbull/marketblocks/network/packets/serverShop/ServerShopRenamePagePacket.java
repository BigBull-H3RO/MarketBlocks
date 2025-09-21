package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerShopRenamePagePacket(String oldName, String newName) implements CustomPacketPayload {
    public static final Type<ServerShopRenamePagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_rename_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopRenamePagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerShopRenamePagePacket::oldName,
            ByteBufCodecs.STRING_UTF8,
            ServerShopRenamePagePacket::newName,
            ServerShopRenamePagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopRenamePagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().canEdit(player) &&
                    ServerShopManager.get().renamePage(packet.oldName(), packet.newName())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}