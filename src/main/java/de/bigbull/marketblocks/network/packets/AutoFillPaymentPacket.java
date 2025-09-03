package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S packet to request the server to auto-fill the payment slots from the player's inventory.
 * This is triggered when a player clicks on an existing offer.
 *
 * @param pos The {@link BlockPos} of the shop.
 */
public record AutoFillPaymentPacket(@NotNull BlockPos pos) implements CustomPacketPayload {
    public static final Type<AutoFillPaymentPacket> TYPE = new Type<>(MarketBlocks.id("auto_fill_payment"));

    public static final StreamCodec<ByteBuf, AutoFillPaymentPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AutoFillPaymentPacket::pos,
            AutoFillPaymentPacket::new
    );

    @Override
    public @NotNull Type<AutoFillPaymentPacket> type() {
        return TYPE;
    }

    /**
     * Handles the packet on the server side.
     * It validates that the player has the correct menu open and then attempts to fill the payment slots.
     */
    public static void handle(final AutoFillPaymentPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            AbstractContainerMenu openMenu = player.containerMenu;
            if (openMenu instanceof SmallShopOffersMenu offersMenu) {
                SmallShopBlockEntity blockEntity = offersMenu.getBlockEntity();

                // Ensure the player has the menu for the correct block entity open
                if (blockEntity.getBlockPos().equals(packet.pos())) {
                    ItemStack required1 = blockEntity.getOfferPayment1();
                    ItemStack required2 = blockEntity.getOfferPayment2();

                    // Normalize payments (if slot 1 is empty but slot 2 has an item, move item from slot 2 to 1)
                    if (required1.isEmpty() && !required2.isEmpty()) {
                        required1 = required2;
                        required2 = ItemStack.EMPTY;
                    }

                    offersMenu.fillPaymentSlots(required1, required2);
                }
            }
        });
    }
}