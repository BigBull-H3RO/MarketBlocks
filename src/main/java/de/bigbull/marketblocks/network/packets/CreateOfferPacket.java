package de.bigbull.marketblocks.network.packets;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.OfferManager;
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
                OfferManager manager = shopEntity.getOfferManager();
                if (manager.validateOffer(packet.payment1(), packet.payment2(), packet.result())) {
                    manager.applyOffer(player, packet.payment1(), packet.payment2(), packet.result());
                } else {
                    MarketBlocks.LOGGER.warn("Invalid offer creation attempt by player {}", player.getName().getString());
                }
            }
        });
    }
}