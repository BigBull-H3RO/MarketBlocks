package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarketplaceToggleEditModePacket(boolean enable) implements CustomPacketPayload {
    public static final Type<MarketplaceToggleEditModePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_toggle_edit_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceToggleEditModePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            MarketplaceToggleEditModePacket::enable,
            MarketplaceToggleEditModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceToggleEditModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MarketplaceMenu menu) {
                if (menu.canUseEditMode()) {
                    menu.setEditMode(packet.enable());
                }
            }
        });
    }
}