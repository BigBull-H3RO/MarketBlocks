package de.bigbull.marketblocks.network.packets.singleOfferShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SingleOfferShopMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SwitchTabPacket(BlockPos pos, ShopTab tab) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SwitchTabPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "switch_tab"));

    private static final StreamCodec<ByteBuf, ShopTab> TAB_CODEC = ByteBufCodecs.VAR_INT.map(
            ShopTab::fromId,
            ShopTab::ordinal
    );

    public static final StreamCodec<ByteBuf, SwitchTabPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SwitchTabPacket::pos,
            TAB_CODEC,
            SwitchTabPacket::tab,
            SwitchTabPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SwitchTabPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            BlockPos pos = packet.pos();
            ShopTab tab = packet.tab();
            if (tab == null) {
                MarketBlocks.LOGGER.warn("Received invalid tab from player {}", player.getName().getString());
                return;
            }

            if (level.getBlockEntity(pos) instanceof SingleOfferShopBlockEntity blockEntity) {
                if (player.containerMenu instanceof SingleOfferShopMenu menu && menu.getBlockEntity() == blockEntity) {
                    if (tab == ShopTab.OFFERS || blockEntity.isOwner(player)) {
                        menu.setActiveTabServer(tab);
                    }
                }
            }
        });
    }
}
