package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
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
                    // Payment Slots sind 24 und 25, Offer Slot ist 26
                    ItemStack payment1Slot = shopEntity.getItem(24).copy();
                    ItemStack payment2Slot = shopEntity.getItem(25).copy();
                    ItemStack offerSlot = shopEntity.getItem(26).copy();

                    // Prüfe ob die Slots mit den Paketdaten übereinstimmen
                    boolean payment1Valid = validatePaymentSlot(packet.payment1(), payment1Slot);
                    boolean payment2Valid = validatePaymentSlot(packet.payment2(), payment2Slot);
                    boolean resultValid = validatePaymentSlot(packet.result(), offerSlot);

                    // Wenn alle Items vorhanden sind, erstelle das Angebot
                    if (payment1Valid && payment2Valid && resultValid) {
                        // Entferne Items aus den Slots und gib sie dem Spieler zurück
                        ItemStack payment1 = shopEntity.removeItemNoUpdate(24);
                        ItemStack payment2 = shopEntity.removeItemNoUpdate(25);
                        ItemStack result = shopEntity.removeItemNoUpdate(26);

                        // Erstelle das Angebot mit Kopien
                        shopEntity.createOffer(payment1Slot, payment2Slot, offerSlot);
                        PacketDistributor.sendToPlayer(player, new OfferStatusPacket(packet.pos(), true));

                        returnStackToPlayer(player, payment1);
                        returnStackToPlayer(player, payment2);
                        returnStackToPlayer(player, result);

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

    private static void returnStackToPlayer(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty()) {
            player.getInventory().placeItemBackInInventory(stack);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            }
        }
    }
}