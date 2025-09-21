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

public record ServerShopDeleteCategoryPacket(String pageName, String categoryName) implements CustomPacketPayload {
    public static final Type<ServerShopDeleteCategoryPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_delete_category"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopDeleteCategoryPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerShopDeleteCategoryPacket::pageName,
            ByteBufCodecs.STRING_UTF8,
            ServerShopDeleteCategoryPacket::categoryName,
            ServerShopDeleteCategoryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopDeleteCategoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().canEdit(player) &&
                    ServerShopManager.get().deleteCategory(packet.pageName(), packet.categoryName())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}