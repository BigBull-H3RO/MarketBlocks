package de.bigbull.marketblocks.feature.trader.network;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.trader.client.TradeBookClientHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/**
 * Packet sent from server to client to open the custom Trade Book screen with dynamic pages.
 */
public record TradeBookOpenPacket(List<Component> pages, Map<String, ShopOfferData> offers) implements CustomPacketPayload {

    public record ShopOfferData(ItemStack payment1, ItemStack payment2, ItemStack result) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ShopOfferData> CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, ShopOfferData::payment1,
                ItemStack.OPTIONAL_STREAM_CODEC, ShopOfferData::payment2,
                ItemStack.OPTIONAL_STREAM_CODEC, ShopOfferData::result,
                ShopOfferData::new
        );
    }

    public static final CustomPacketPayload.Type<TradeBookOpenPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "trade_book_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TradeBookOpenPacket> CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list()), TradeBookOpenPacket::pages,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ShopOfferData.CODEC), TradeBookOpenPacket::offers,
            TradeBookOpenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TradeBookOpenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> TradeBookClientHandler.openScreen(packet.pages(), packet.offers()));
    }
}
