package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.GenericMenuProvider;
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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SwitchTabPacket(BlockPos pos, boolean showOffers) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SwitchTabPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "switch_tab"));

    public static final StreamCodec<ByteBuf, SwitchTabPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SwitchTabPacket::pos,
            ByteBufCodecs.BOOL,
            SwitchTabPacket::showOffers,
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

            if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity blockEntity) {
                if (packet.showOffers()) {
                    player.openMenu(
                            new GenericMenuProvider(
                                    Component.translatable("container.marketblocks.small_shop_offers"),
                                    (id, inv, p) -> new SmallShopOffersMenu(id, inv, blockEntity)
                            ),
                            pos
                    );
                } else if (blockEntity.isOwner(player)) {
                    player.openMenu(
                            new GenericMenuProvider(
                                    Component.translatable("container.marketblocks.small_shop_inventory"),
                                    (id, inv, p) -> new SmallShopInventoryMenu(id, inv, blockEntity)
                            ),
                            pos
                    );
                }
            }
        });
    }
}