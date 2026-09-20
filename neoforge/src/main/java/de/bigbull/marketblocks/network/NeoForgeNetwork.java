package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.marketplace.network.*;
import de.bigbull.marketblocks.feature.singleoffer.network.*;
import de.bigbull.marketblocks.feature.trader.network.*;
import de.bigbull.marketblocks.core.config.network.ConfigSyncPacket;
import de.bigbull.marketblocks.platform.network.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;

public class NeoForgeNetwork {
    private static final String PROTOCOL_VERSION = "1.0.0";

    private static PacketContext wrap(IPayloadContext ctx) {
        return new PacketContext() {
            @Override
            public Player player() {
                return ctx.player();
            }

            @Override
            public void enqueueWork(Runnable runnable) {
                ctx.enqueueWork(runnable);
            }
        };
    }

    private static <T extends CustomPacketPayload> void registerServerPacket(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, PacketContext> handler) {
        registrar.playToServer(type, codec, (pkt, ctx) -> handler.accept(pkt, wrap(ctx)));
    }

    private static <T extends CustomPacketPayload> void registerClientPacket(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, PacketContext> handler) {
        registrar.playToClient(type, codec, (pkt, ctx) -> handler.accept(pkt, wrap(ctx)));
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).versioned(PROTOCOL_VERSION);

        // Server packets (client -> server)
        registerServerPacket(registrar, CreateOfferPacket.TYPE, CreateOfferPacket.CODEC, CreateOfferPacket::handle);
        registerServerPacket(registrar, DeleteOfferPacket.TYPE, DeleteOfferPacket.CODEC, DeleteOfferPacket::handle);
        registerServerPacket(registrar, SwitchTabPacket.TYPE, SwitchTabPacket.CODEC, SwitchTabPacket::handle);
        registerServerPacket(registrar, AutoFillPaymentPacket.TYPE, AutoFillPaymentPacket.CODEC, AutoFillPaymentPacket::handle);
        registerServerPacket(registrar, UpdateRedstoneSettingPacket.TYPE, UpdateRedstoneSettingPacket.CODEC, UpdateRedstoneSettingPacket::handle);
        registerServerPacket(registrar, UpdateSettingsPacket.TYPE, UpdateSettingsPacket.CODEC, UpdateSettingsPacket::handle);
        registerServerPacket(registrar, ToggleAdminShopModePacket.TYPE, ToggleAdminShopModePacket.CODEC, ToggleAdminShopModePacket::handle);
        registerServerPacket(registrar, ClearTransactionLogPacket.TYPE, ClearTransactionLogPacket.CODEC, ClearTransactionLogPacket::handle);

        registerServerPacket(registrar, MarketplaceOpenRequestPacket.TYPE, MarketplaceOpenRequestPacket.CODEC, MarketplaceOpenRequestPacket::handle);
        registerServerPacket(registrar, MarketplaceToggleEditModePacket.TYPE, MarketplaceToggleEditModePacket.CODEC, MarketplaceToggleEditModePacket::handle);
        registerServerPacket(registrar, MarketplaceSelectPagePacket.TYPE, MarketplaceSelectPagePacket.CODEC, MarketplaceSelectPagePacket::handle);
        registerServerPacket(registrar, MarketplaceCreatePagePacket.TYPE, MarketplaceCreatePagePacket.CODEC, MarketplaceCreatePagePacket::handle);
        registerServerPacket(registrar, MarketplaceRenamePagePacket.TYPE, MarketplaceRenamePagePacket.CODEC, MarketplaceRenamePagePacket::handle);
        registerServerPacket(registrar, MarketplaceDeletePagePacket.TYPE, MarketplaceDeletePagePacket.CODEC, MarketplaceDeletePagePacket::handle);
        registerServerPacket(registrar, MarketplaceAddOfferPacket.TYPE, MarketplaceAddOfferPacket.CODEC, MarketplaceAddOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceMoveOfferPacket.TYPE, MarketplaceMoveOfferPacket.CODEC, MarketplaceMoveOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceDeleteOfferPacket.TYPE, MarketplaceDeleteOfferPacket.CODEC, MarketplaceDeleteOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceUpdateOfferLimitsPacket.TYPE, MarketplaceUpdateOfferLimitsPacket.CODEC, MarketplaceUpdateOfferLimitsPacket::handle);
        registerServerPacket(registrar, MarketplaceUpdateOfferPricingPacket.TYPE, MarketplaceUpdateOfferPricingPacket.CODEC, MarketplaceUpdateOfferPricingPacket::handle);
        registerServerPacket(registrar, MarketplaceAutoFillPacket.TYPE, MarketplaceAutoFillPacket.CODEC, MarketplaceAutoFillPacket::handle);
        registerServerPacket(registrar, MarketplaceSetOfferPacket.TYPE, MarketplaceSetOfferPacket.CODEC, MarketplaceSetOfferPacket::handle);
        registerServerPacket(registrar, MarketplaceClearTemplatePacket.TYPE, MarketplaceClearTemplatePacket.CODEC, MarketplaceClearTemplatePacket::handle);

        registerServerPacket(registrar, TeleportRequestPacket.TYPE, TeleportRequestPacket.CODEC, TeleportRequestPacket::handle);

        // Client packets (server -> client)
        registerClientPacket(registrar, OfferStatusPacket.TYPE, OfferStatusPacket.CODEC, OfferStatusPacket::handle);
        registerClientPacket(registrar, TransactionLogSyncPacket.TYPE, TransactionLogSyncPacket.CODEC, TransactionLogSyncPacket::handle);
        registerClientPacket(registrar, MarketplaceSyncPacket.TYPE, MarketplaceSyncPacket.CODEC, MarketplaceSyncPacket::handle);
        registerClientPacket(registrar, LinkedBlocksSyncPacket.TYPE, LinkedBlocksSyncPacket.CODEC, LinkedBlocksSyncPacket::handle);
        registerClientPacket(registrar, TradeBookOpenPacket.TYPE, TradeBookOpenPacket.CODEC, TradeBookOpenPacket::handle);
        registerClientPacket(registrar, ConfigSyncPacket.TYPE, ConfigSyncPacket.CODEC, ConfigSyncPacket::handle);
    }
}
