package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AutoFillPaymentPacket(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AutoFillPaymentPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "auto_fill_payment"));

    public static final StreamCodec<ByteBuf, AutoFillPaymentPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AutoFillPaymentPacket::pos,
            AutoFillPaymentPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AutoFillPaymentPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player.containerMenu instanceof SmallShopOffersMenu menu) {
                SmallShopBlockEntity blockEntity = menu.getBlockEntity();
                if (blockEntity.getBlockPos().equals(packet.pos())) {
                    ItemStack required1 = blockEntity.getOfferPayment1();
                    ItemStack required2 = blockEntity.getOfferPayment2();

                    if (required1.isEmpty() && !required2.isEmpty()) {
                        required1 = required2;
                        required2 = ItemStack.EMPTY;
                    }

                    menu.fillPaymentSlots(required1, required2);
                }
            }
        });
    }
}