package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CreateOfferPacket(BlockPos pos, ItemStack payment1, ItemStack payment2, ItemStack result)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateOfferPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "create_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreateOfferPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CreateOfferPacket::pos,
            ItemStack.STREAM_CODEC,
            CreateOfferPacket::payment1,
            ItemStack.STREAM_CODEC,
            CreateOfferPacket::payment2,
            ItemStack.STREAM_CODEC,
            CreateOfferPacket::result,
            CreateOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CreateOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Level level = player.level();

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity) {
                // Prüfe ob Spieler der Owner ist
                if (shopEntity.isOwner(player)) {
                    // Tatsächliche Items aus Block und Spielerinventar ermitteln
                    ItemStack payment1Slot = shopEntity.getItem(24).copy();
                    ItemStack payment2Slot = shopEntity.getItem(25).copy();

                    // Prüfe ob die Zahlungsslots mit den Paketdaten übereinstimmen
                    boolean payment1Valid = packet.payment1().isEmpty() ? payment1Slot.isEmpty() :
                            (!payment1Slot.isEmpty() && ItemStack.isSameItemSameComponents(payment1Slot, packet.payment1())
                                    && payment1Slot.getCount() == packet.payment1().getCount());
                    boolean payment2Valid = packet.payment2().isEmpty() ? payment2Slot.isEmpty() :
                            (!payment2Slot.isEmpty() && ItemStack.isSameItemSameComponents(payment2Slot, packet.payment2())
                                    && payment2Slot.getCount() == packet.payment2().getCount());

                    // Resultat im Spielerinventar suchen
                    int resultSlot = -1;

                    if (!packet.result().isEmpty()) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (ItemStack.isSameItemSameComponents(stack, packet.result())
                                    && stack.getCount() >= packet.result().getCount()) {
                                resultSlot = i;
                                break;
                            }
                        }
                    }

                    // Wenn alle Items vorhanden sind, entferne sie und erstelle das Angebot
                    if (payment1Valid && payment2Valid && resultSlot != -1) {
                        ItemStack payment1 = payment1Slot.isEmpty() ? ItemStack.EMPTY : shopEntity.removeItem(24, payment1Slot.getCount());
                        ItemStack payment2 = payment2Slot.isEmpty() ? ItemStack.EMPTY : shopEntity.removeItem(25, payment2Slot.getCount());
                        ItemStack result = player.getInventory().getItem(resultSlot).split(packet.result().getCount());

                        shopEntity.createOffer(payment1, payment2, result);

                        MarketBlocks.LOGGER.info("Player {} created offer at {}", player.getName().getString(), packet.pos());
                    }
                }
            }
        });
    }
}