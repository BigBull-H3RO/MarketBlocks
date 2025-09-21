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

/**
 * Wechselt serverseitig die ausgewählte Seite.
 */
public record ServerShopSelectPagePacket(int pageIndex) implements CustomPacketPayload {
    public static final Type<ServerShopSelectPagePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_select_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopSelectPagePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerShopSelectPagePacket::pageIndex,
            ServerShopSelectPagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopSelectPagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().selectPage(packet.pageIndex())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}