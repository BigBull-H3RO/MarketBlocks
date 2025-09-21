package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ServerShopReplaceOfferStacksPacket(UUID offerId) implements CustomPacketPayload {
    public static final Type<ServerShopReplaceOfferStacksPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_replace_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopReplaceOfferStacksPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ServerShopReplaceOfferStacksPacket::offerId,
            ServerShopReplaceOfferStacksPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopReplaceOfferStacksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof ServerShopMenu menu) || !menu.isEditor()) {
                return;
            }
            Container template = menu.templateContainer();
            ItemStack result = template.getItem(2).copy();
            if (result.isEmpty()) {
                return;
            }
            List<ItemStack> payments = new ArrayList<>();
            payments.add(template.getItem(0).copy());
            payments.add(template.getItem(1).copy());
            if (ServerShopManager.get().replaceOfferStacks(packet.offerId(), result, payments)) {
                returnTemplateItems(player, template);
                ServerShopManager.get().syncOpenViewers(player);
            }
        });
    }

    private static void returnTemplateItems(ServerPlayer player, Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}