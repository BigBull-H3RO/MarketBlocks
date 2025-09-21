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

public record ServerShopToggleCategoryPacket(String pageName, String categoryName) implements CustomPacketPayload {
    public static final Type<ServerShopToggleCategoryPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_toggle_category"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopToggleCategoryPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerShopToggleCategoryPacket::pageName,
            ByteBufCodecs.STRING_UTF8,
            ServerShopToggleCategoryPacket::categoryName,
            ServerShopToggleCategoryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopToggleCategoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player &&
                    ServerShopManager.get().toggleCategory(packet.pageName(), packet.categoryName())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}