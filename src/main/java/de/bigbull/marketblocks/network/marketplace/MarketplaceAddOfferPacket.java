package de.bigbull.marketblocks.network.marketplace;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.marketplace.menu.MarketplaceMenu;
import de.bigbull.marketblocks.feature.marketplace.data.DemandPricing;
import de.bigbull.marketblocks.feature.marketplace.data.OfferLimit;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record MarketplaceAddOfferPacket(String pageName) implements CustomPacketPayload {
    public static final Type<MarketplaceAddOfferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "marketplace_add_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketplaceAddOfferPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MarketplaceAddOfferPacket::pageName,
            MarketplaceAddOfferPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarketplaceAddOfferPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof MarketplaceMenu menu) || !menu.isEditor()) return;
            Container template = menu.templateContainer();
            ItemStack payment1 = template.getItem(0).copy();
            ItemStack payment2 = template.getItem(1).copy();
            ItemStack result = template.getItem(2).copy();

            if (result.isEmpty()) {
                player.sendSystemMessage(Component.translatable("gui.marketblocks.error.no_result_item"));
                return;
            }
            if (payment1.isEmpty() && payment2.isEmpty()) {
                player.sendSystemMessage(Component.translatable("gui.marketblocks.error.no_payment_items"));
                return;
            }

            MarketplaceOffer offer = new MarketplaceOffer(null, result, List.of(payment1, payment2),
                    OfferLimit.unlimited(), DemandPricing.disabled());
            MarketplaceManager.MutationResult<MarketplaceOffer> addResult = MarketplaceManager.get().addOffer(packet.pageName(), offer);
            if (addResult.isSuccess()) {
                returnTemplateItems(player, template);
                MarketplaceManager.get().syncOpenViewers(player);
            } else {
                player.sendSystemMessage(addResult.errorMessage());
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