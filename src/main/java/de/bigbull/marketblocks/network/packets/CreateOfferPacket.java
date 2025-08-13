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
import net.minecraft.world.level.ChunkPos;
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
            ItemStack.OPTIONAL_STREAM_CODEC,
            CreateOfferPacket::payment1,
            ItemStack.OPTIONAL_STREAM_CODEC,
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

            if (level.getBlockEntity(packet.pos()) instanceof SmallShopBlockEntity shopEntity && shopEntity.isOwner(player)) {
                ItemStack[] slotCopies = copySlots(shopEntity);

                if (slotsAreValid(packet, slotCopies)) {
                    ItemStack[] extracted = extractItems(shopEntity, slotCopies);

                    shopEntity.createOffer(slotCopies[0], slotCopies[1], slotCopies[2]);
                    PacketDistributor.sendToPlayersTrackingChunk(player.serverLevel(), new ChunkPos(packet.pos()),
                            new OfferStatusPacket(packet.pos(), true));
                    level.sendBlockUpdated(packet.pos(), level.getBlockState(packet.pos()),
                            level.getBlockState(packet.pos()), 3);

                    returnStacksToPlayer(player, extracted);

                    MarketBlocks.LOGGER.info("Player {} created offer at {}", player.getName().getString(), packet.pos());
                } else {
                    MarketBlocks.LOGGER.warn("Invalid offer creation attempt by player {}", player.getName().getString());
                }
            }
        });
    }

    private static ItemStack[] copySlots(SmallShopBlockEntity shopEntity) {
        ItemStack[] slots = new ItemStack[3];
        for (int i = 0; i < 2; i++) {
            slots[i] = shopEntity.getPaymentHandler().getStackInSlot(i).copy();
        }
        slots[2] = shopEntity.getOfferHandler().getStackInSlot(0).copy();
        return slots;
    }

    private static boolean slotsAreValid(CreateOfferPacket packet, ItemStack[] slots) {
        ItemStack[] expected = {packet.payment1(), packet.payment2(), packet.result()};
        for (int i = 0; i < expected.length; i++) {
            if (!validatePaymentSlot(expected[i], slots[i])) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack[] extractItems(SmallShopBlockEntity shopEntity, ItemStack[] slots) {
        ItemStack[] extracted = new ItemStack[3];
        for (int i = 0; i < 2; i++) {
            extracted[i] = shopEntity.getPaymentHandler().extractItem(i, slots[i].getCount(), false);
        }
        extracted[2] = shopEntity.getOfferHandler().extractItem(0, slots[2].getCount(), false);
        return extracted;
    }

    private static boolean validatePaymentSlot(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty()) {
            return actual.isEmpty();
        }
        return !actual.isEmpty() &&
                ItemStack.isSameItemSameComponents(actual, expected) &&
                actual.getCount() == expected.getCount();
    }

    private static void returnStacksToPlayer(ServerPlayer player, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
                }
            }
        }
    }
}