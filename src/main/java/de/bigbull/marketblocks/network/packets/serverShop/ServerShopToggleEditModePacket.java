package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerShopToggleEditModePacket(boolean enable) implements CustomPacketPayload {
    public static final Type<ServerShopToggleEditModePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_toggle_edit_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopToggleEditModePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerShopToggleEditModePacket::enable,
            ServerShopToggleEditModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopToggleEditModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof ServerShopMenu menu) {
                // Nur wenn der Spieler die Berechtigung hat, darf er umschalten
                if (menu.hasEditPermission()) {
                    menu.setEditMode(packet.enable());
                }
            }
        });
    }
}