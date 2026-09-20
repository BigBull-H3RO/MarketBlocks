package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.feature.marketplace.network.*;
import de.bigbull.marketblocks.feature.singleoffer.network.*;
import de.bigbull.marketblocks.feature.trader.network.*;
import de.bigbull.marketblocks.core.config.network.ConfigSyncPacket;
import de.bigbull.marketblocks.platform.network.PacketContext;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class FabricNetwork {

    private static <T extends CustomPacketPayload> void registerServer(CustomPacketPayload.Type<T> type,
                                                                       net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec,
                                                                       BiConsumer<T, PacketContext> handler) {
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            PacketContext packetCtx = new PacketContext() {
                @Override
                public Player player() {
                    return context.player();
                }

                @Override
                public void enqueueWork(Runnable runnable) {
                    context.server().execute(runnable);
                }
            };
            handler.accept(payload, packetCtx);
        });
    }

    private static <T extends CustomPacketPayload> void registerClient(CustomPacketPayload.Type<T> type,
                                                                       net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(type, codec);
    }

    public static void init() {
        // Server packets
        registerServer(CreateOfferPacket.TYPE, CreateOfferPacket.CODEC, CreateOfferPacket::handle);
        registerServer(DeleteOfferPacket.TYPE, DeleteOfferPacket.CODEC, DeleteOfferPacket::handle);
        registerServer(SwitchTabPacket.TYPE, SwitchTabPacket.CODEC, SwitchTabPacket::handle);
        registerServer(AutoFillPaymentPacket.TYPE, AutoFillPaymentPacket.CODEC, AutoFillPaymentPacket::handle);
        registerServer(UpdateRedstoneSettingPacket.TYPE, UpdateRedstoneSettingPacket.CODEC, UpdateRedstoneSettingPacket::handle);
        registerServer(UpdateSettingsPacket.TYPE, UpdateSettingsPacket.CODEC, UpdateSettingsPacket::handle);
        registerServer(ToggleAdminShopModePacket.TYPE, ToggleAdminShopModePacket.CODEC, ToggleAdminShopModePacket::handle);
        registerServer(ClearTransactionLogPacket.TYPE, ClearTransactionLogPacket.CODEC, ClearTransactionLogPacket::handle);

        registerServer(MarketplaceOpenRequestPacket.TYPE, MarketplaceOpenRequestPacket.CODEC, MarketplaceOpenRequestPacket::handle);
        registerServer(MarketplaceToggleEditModePacket.TYPE, MarketplaceToggleEditModePacket.CODEC, MarketplaceToggleEditModePacket::handle);
        registerServer(MarketplaceSelectPagePacket.TYPE, MarketplaceSelectPagePacket.CODEC, MarketplaceSelectPagePacket::handle);
        registerServer(MarketplaceCreatePagePacket.TYPE, MarketplaceCreatePagePacket.CODEC, MarketplaceCreatePagePacket::handle);
        registerServer(MarketplaceRenamePagePacket.TYPE, MarketplaceRenamePagePacket.CODEC, MarketplaceRenamePagePacket::handle);
        registerServer(MarketplaceDeletePagePacket.TYPE, MarketplaceDeletePagePacket.CODEC, MarketplaceDeletePagePacket::handle);
        registerServer(MarketplaceAddOfferPacket.TYPE, MarketplaceAddOfferPacket.CODEC, MarketplaceAddOfferPacket::handle);
        registerServer(MarketplaceMoveOfferPacket.TYPE, MarketplaceMoveOfferPacket.CODEC, MarketplaceMoveOfferPacket::handle);
        registerServer(MarketplaceDeleteOfferPacket.TYPE, MarketplaceDeleteOfferPacket.CODEC, MarketplaceDeleteOfferPacket::handle);
        registerServer(MarketplaceUpdateOfferLimitsPacket.TYPE, MarketplaceUpdateOfferLimitsPacket.CODEC, MarketplaceUpdateOfferLimitsPacket::handle);
        registerServer(MarketplaceUpdateOfferPricingPacket.TYPE, MarketplaceUpdateOfferPricingPacket.CODEC, MarketplaceUpdateOfferPricingPacket::handle);
        registerServer(MarketplaceAutoFillPacket.TYPE, MarketplaceAutoFillPacket.CODEC, MarketplaceAutoFillPacket::handle);
        registerServer(MarketplaceSetOfferPacket.TYPE, MarketplaceSetOfferPacket.CODEC, MarketplaceSetOfferPacket::handle);
        registerServer(MarketplaceClearTemplatePacket.TYPE, MarketplaceClearTemplatePacket.CODEC, MarketplaceClearTemplatePacket::handle);

        registerServer(TeleportRequestPacket.TYPE, TeleportRequestPacket.CODEC, TeleportRequestPacket::handle);

        // Client packets
        registerClient(OfferStatusPacket.TYPE, OfferStatusPacket.CODEC);
        registerClient(TransactionLogSyncPacket.TYPE, TransactionLogSyncPacket.CODEC);
        registerClient(MarketplaceSyncPacket.TYPE, MarketplaceSyncPacket.CODEC);
        registerClient(LinkedBlocksSyncPacket.TYPE, LinkedBlocksSyncPacket.CODEC);
        registerClient(TradeBookOpenPacket.TYPE, TradeBookOpenPacket.CODEC);
        registerClient(ConfigSyncPacket.TYPE, ConfigSyncPacket.CODEC);
    }
}
