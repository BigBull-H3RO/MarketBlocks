package de.bigbull.marketblocks.network.packets.serverShop;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.servershop.DemandPricing;
import de.bigbull.marketblocks.util.custom.servershop.OfferLimit;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ServerShopAddOfferPacket(String pageName) implements CustomPacketPayload {
    public static final Type<ServerShopAddOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "server_shop_add_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerShopAddOfferPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerShopAddOfferPacket::pageName,
            ServerShopAddOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerShopAddOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof ServerShopMenu menu) || !menu.isEditor()) return;
            Container template = menu.templateContainer();
            ItemStack payment1 = template.getItem(0).copy();
            ItemStack payment2 = template.getItem(1).copy();
            ItemStack result = template.getItem(2).copy();
            if (result.isEmpty()) return;
            ServerShopOffer offer = new ServerShopOffer(null, result, List.of(payment1, payment2),
                    OfferLimit.unlimited(), DemandPricing.disabled());
            if (ServerShopManager.get().addOffer(packet.pageName(), offer).isPresent()) {
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