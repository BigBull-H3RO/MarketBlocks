package de.bigbull.marketblocks.network.singleoffer;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.menu.ShopTab;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleAdminShopModePacket(BlockPos pos, boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleAdminShopModePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "toggle_admin_shop_mode"));

    public static final StreamCodec<ByteBuf, ToggleAdminShopModePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ToggleAdminShopModePacket::pos,
            ByteBufCodecs.BOOL,
            ToggleAdminShopModePacket::enabled,
            ToggleAdminShopModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleAdminShopModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.hasPermissions(2)) {
                return;
            }
            if (!Config.MARKETBLOCKS_ADMIN_MODE_ENABLED.get()) {
                return;
            }

            Level level = player.level();
            if (!(level.getBlockEntity(packet.pos()) instanceof SingleOfferShopBlockEntity blockEntity)) {
                return;
            }

            blockEntity.setAdminShopEnabled(packet.enabled());
            if (packet.enabled() && player.getServer() != null) {
                for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                    if (onlinePlayer.containerMenu instanceof SingleOfferShopMenu menu
                            && menu.getBlockEntity() == blockEntity
                            && menu.getActiveTab() == ShopTab.INVENTORY) {
                        menu.setActiveTabServer(ShopTab.OFFERS);
                    }
                }
            }
        });
    }
}
