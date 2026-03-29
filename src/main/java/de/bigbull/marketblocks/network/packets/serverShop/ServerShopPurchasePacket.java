package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Legacy-Paket des alten inventarbasierten Kaufpfads.
 * Wird nicht mehr registriert, da ServerShop-Käufe nur noch slotbasiert über das Menü laufen.
 */
@Deprecated(forRemoval = true)
public record ServerShopPurchasePacket(UUID offerId, int amount) implements CustomPacketPayload {
    public static final Type<ServerShopPurchasePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_purchase"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopPurchasePacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ServerShopPurchasePacket::offerId,
            ByteBufCodecs.VAR_INT,
            ServerShopPurchasePacket::amount,
            ServerShopPurchasePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopPurchasePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            packet.offerId();
            packet.amount();
            // Legacy no-op: aktive Käufe werden ausschließlich über ServerShopMenu abgewickelt.
        });
    }
}