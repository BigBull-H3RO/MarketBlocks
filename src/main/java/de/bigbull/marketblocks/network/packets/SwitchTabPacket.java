package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
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
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();
            BlockPos pos = packet.pos();
            ShopTab tab = packet.tab();
            if (tab == null) {
                MarketBlocks.LOGGER.warn("Received invalid tab from player {}", player.getName().getString());
                return;
            }

            if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity blockEntity) {
                if (tab == ShopTab.OFFERS) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopOffersMenu(id, inv, blockEntity),
                                    Component.translatable("container.marketblocks.small_shop_offers")
                            ),
                            pos
                    );
                } else if (tab == ShopTab.INVENTORY && blockEntity.isOwner(player)) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopInventoryMenu(id, inv, blockEntity),
                                    Component.translatable("container.marketblocks.small_shop_inventory")
                            ),
                            pos
                    );
                } else if (tab == ShopTab.SETTINGS && blockEntity.isOwner(player)) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopSettingsMenu(id, inv, blockEntity),
                                    Component.translatable("container.marketblocks.small_shop")
                            ),
                            pos
                    );
                }
            }
        });
    }
}