package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModLang;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S packet to request the server to open a different shop menu (tab).
 * This is used for navigating between the Offers, Inventory, and Settings screens.
 *
 * @param pos The {@link BlockPos} of the shop.
 * @param tab The {@link ShopTab} to switch to.
 */
public record SwitchTabPacket(@NotNull BlockPos pos, @NotNull ShopTab tab) implements CustomPacketPayload {
    public static final Type<SwitchTabPacket> TYPE = new Type<>(MarketBlocks.id("switch_tab"));

    private static final StreamCodec<ByteBuf, ShopTab> TAB_CODEC = ByteBufCodecs.idMapper(ShopTab::fromId, ShopTab::ordinal);

    public static final StreamCodec<ByteBuf, SwitchTabPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SwitchTabPacket::pos,
            TAB_CODEC, SwitchTabPacket::tab,
            SwitchTabPacket::new
    );

    @Override
    public @NotNull Type<SwitchTabPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It validates the request and opens the corresponding menu for the player.
     * Access to inventory and settings tabs is restricted to the shop owner.
     */
    public static void handle(final SwitchTabPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Level level = player.level();
            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity blockEntity) {
                switch (packet.tab()) {
                    case OFFERS -> player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopOffersMenu(id, inv, blockEntity),
                                    Component.translatable(ModLang.CONTAINER_SMALL_SHOP_OFFERS)
                            ),
                            packet.pos()
                    );
                    case INVENTORY -> {
                        if (blockEntity.isOwner(player)) {
                            player.openMenu(
                                new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopInventoryMenu(id, inv, blockEntity),
                                    Component.translatable(ModLang.CONTAINER_SMALL_SHOP_INVENTORY)
                                ),
                                packet.pos()
                            );
                        }
                    }
                    case SETTINGS -> {
                        if (blockEntity.isOwner(player)) {
                            player.openMenu(
                                new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopSettingsMenu(id, inv, blockEntity),
                                    Component.translatable(ModLang.GUI_SETTINGS_TITLE)
                                ),
                                packet.pos()
                            );
                        }
                    }
                }
            }
        });
    }
}