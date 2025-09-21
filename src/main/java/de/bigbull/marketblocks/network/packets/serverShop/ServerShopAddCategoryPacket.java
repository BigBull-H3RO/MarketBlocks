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

public record ServerShopAddCategoryPacket(String pageName, String categoryName) implements CustomPacketPayload {
    public static final Type<ServerShopAddCategoryPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_add_category"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopAddCategoryPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerShopAddCategoryPacket::pageName,
            ByteBufCodecs.STRING_UTF8,
            ServerShopAddCategoryPacket::categoryName,
            ServerShopAddCategoryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopAddCategoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().canEdit(player) &&
                    ServerShopManager.get().addCategory(packet.pageName(), packet.categoryName()).isPresent()) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}