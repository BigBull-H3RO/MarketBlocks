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

public record ServerShopRenameCategoryPacket(String pageName, String oldName, String newName)
        implements CustomPacketPayload {
    public static final Type<ServerShopRenameCategoryPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_rename_category"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopRenameCategoryPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerShopRenameCategoryPacket::pageName,
            ByteBufCodecs.STRING_UTF8,
            ServerShopRenameCategoryPacket::oldName,
            ByteBufCodecs.STRING_UTF8,
            ServerShopRenameCategoryPacket::newName,
            ServerShopRenameCategoryPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopRenameCategoryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && ServerShopManager.get().canEdit(player) &&
                    ServerShopManager.get().renameCategory(packet.pageName(), packet.oldName(), packet.newName())) {
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }
}