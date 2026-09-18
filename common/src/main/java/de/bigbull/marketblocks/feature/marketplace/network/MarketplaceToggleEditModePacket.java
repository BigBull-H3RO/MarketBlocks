package de.bigbull.marketblocks.feature.marketplace.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import de.bigbull.marketblocks.platform.network.PacketContext;

public record MarketplaceToggleEditModePacket(boolean enable) implements CustomPacketPayload {
    public static final Type<MarketplaceToggleEditModePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "marketplace_toggle_edit_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceToggleEditModePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            MarketplaceToggleEditModePacket::enable,
            MarketplaceToggleEditModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceToggleEditModePacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MarketplaceMenu menu) {
                if (player.hasPermissions(2)) {
                    de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager.get().setEditMode(player, packet.enable());
                    menu.setEditMode(packet.enable());
                }
            }
        });
    }
}
