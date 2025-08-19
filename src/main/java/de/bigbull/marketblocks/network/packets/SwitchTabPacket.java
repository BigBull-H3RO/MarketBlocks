package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
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

public record SwitchTabPacket(BlockPos pos, int tab) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SwitchTabPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "switch_tab"));

    public static final StreamCodec<ByteBuf, SwitchTabPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SwitchTabPacket::pos,
            ByteBufCodecs.VAR_INT,
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
            int tab = packet.tab();

            if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity blockEntity) {
                if (tab == 0) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopOffersMenu(id, inv, blockEntity),
                                    Component.translatable("container.marketblocks.small_shop_offers")
                            ),
                            pos
                    );
                } else if (tab == 1 && blockEntity.isOwner(player)) {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (id, inv, p) -> new SmallShopInventoryMenu(id, inv, blockEntity),
                                    Component.translatable("container.marketblocks.small_shop_inventory")
                            ),
                            pos
                    );
                } else if (tab == 2 && blockEntity.isOwner(player)) {
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