package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopOffer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ServerShopAutoFillPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<ServerShopAutoFillPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_auto_fill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopAutoFillPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ServerShopAutoFillPacket::offerId,
            ServerShopAutoFillPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopAutoFillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ServerShopMenu menu) {
                // Nur im Normal-Modus (nicht Editor) erlauben, um Verwirrung zu vermeiden?
                // Oder beides? Hier erlauben wir es generell, das Menu regelt den Rest.

                ServerShopOffer offer = ServerShopManager.get().findOffer(packet.offerId());
                if (offer != null) {
                    menu.autoFillPayment(player, offer);
                }
            }
        });
    }
}