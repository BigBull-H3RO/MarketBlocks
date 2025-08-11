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
                    // FIXED: Korrekte Slot-Indizes verwenden
                    // Payment Slots sind 24 und 25 in der BlockEntity
                    ItemStack payment1Slot = shopEntity.getItem(24).copy();
                    ItemStack payment2Slot = shopEntity.getItem(25).copy();

                    // Prüfe ob die Zahlungsslots mit den Paketdaten übereinstimmen
                    boolean payment1Valid = validatePaymentSlot(packet.payment1(), payment1Slot);
                    boolean payment2Valid = validatePaymentSlot(packet.payment2(), payment2Slot);

                    // Resultat im Spielerinventar suchen (NICHT im Block!)
                    int resultSlot = findResultItemInPlayerInventory(player, packet.result());

                    // Wenn alle Items vorhanden sind, entferne sie und erstelle das Angebot
                    if (payment1Valid && payment2Valid && resultSlot != -1) {
                        // Entferne Payment-Items aus Block
                        ItemStack payment1 = payment1Slot.isEmpty() ? ItemStack.EMPTY :
                                shopEntity.removeItem(24, payment1Slot.getCount());
                        ItemStack payment2 = payment2Slot.isEmpty() ? ItemStack.EMPTY :
                                shopEntity.removeItem(25, payment2Slot.getCount());

                        // Entferne Result-Item aus Spielerinventar
                        ItemStack result = player.getInventory().getItem(resultSlot).split(packet.result().getCount());

                        // Erstelle das Angebot
                        shopEntity.createOffer(payment1, payment2, result);

                        MarketBlocks.LOGGER.info("Player {} created offer at {}", player.getName().getString(), packet.pos());
                    } else {
                        MarketBlocks.LOGGER.warn("Invalid offer creation attempt by player {}", player.getName().getString());
                    }
                }
            }
        });
    }

    private static boolean validatePaymentSlot(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty()) {
            return actual.isEmpty();
        }
        return !actual.isEmpty() &&
                ItemStack.isSameItemSameComponents(actual, expected) &&
                actual.getCount() == expected.getCount();
    }

    private static int findResultItemInPlayerInventory(ServerPlayer player, ItemStack resultItem) {
        if (resultItem.isEmpty()) return -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, resultItem) &&
                    stack.getCount() >= resultItem.getCount()) {
                return i;
            }
        }
        return -1;
    }
}