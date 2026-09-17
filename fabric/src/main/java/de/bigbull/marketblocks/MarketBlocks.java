package de.bigbull.marketblocks;

import de.bigbull.marketblocks.command.FabricMarketBlocksCommands;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.notification.PendingNotificationsSavedData;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.trader.ShopBuyerSpawner;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import de.bigbull.marketblocks.init.FabricRegistries;
import de.bigbull.marketblocks.network.FabricNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.Set;

public class MarketBlocks implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
        FabricRegistries.init();
        FabricNetwork.init();

        // Entity attributes
        FabricDefaultAttributeRegistry.register(RegistriesInit.SHOP_BUYER.get(), ShopBuyerEntity.createAttributes());

        // Lifecycle Events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            MarketplaceManager.get().initialize(server);
            TraderEconomyManager.get().load();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            MarketplaceManager.get().shutdown();
            ShopBuyerSpawner.clearAll();
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                MarketplaceManager.get().reload();
                TraderEconomyManager.get().load();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MarketplaceManager.get().tick();
            for (var level : server.getAllLevels()) {
                ShopBuyerSpawner.tick(level);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            MarketplaceLinkSavedData.get(player.serverLevel()).syncToPlayer(player);
            PendingNotificationsSavedData data = PendingNotificationsSavedData.get(player.serverLevel());
            Set<BlockPos> emptyShops = data.getAndClearOutOfStock(player.getUUID());
            Set<BlockPos> fullShops = data.getAndClearOutputFull(player.getUUID());

            if (!emptyShops.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("gui.marketblocks.notifications.login.out_of_stock", emptyShops.size()));
                for (BlockPos pos : emptyShops) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.notifications.login.coordinate", pos.getX(), pos.getY(), pos.getZ()));
                }
            }
            if (!fullShops.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("gui.marketblocks.notifications.login.output_full", fullShops.size()));
                for (BlockPos pos : fullShops) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.notifications.login.coordinate", pos.getX(), pos.getY(), pos.getZ()));
                }
            }
        });

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            FabricMarketBlocksCommands.register(dispatcher, registryAccess);
        });

        // Block interaction & chest security
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            GlobalPos globalPos = GlobalPos.of(serverPlayer.serverLevel().dimension(), pos);

            if (MarketplaceLinkSavedData.get(serverPlayer.serverLevel()).isLinked(globalPos)) {
                MarketplaceManager.get().openShop(serverPlayer);
                return InteractionResult.SUCCESS;
            }

            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity) {
                for (Direction dir : Direction.values()) {
                    BlockPos shopPos = pos.relative(dir);
                    if (world.getBlockEntity(shopPos) instanceof SingleOfferShopBlockEntity shop) {
                        if (!shop.getOwners().contains(player.getUUID()) && !player.hasPermissions(2)) {
                            player.displayClientMessage(Component.translatable("message.marketblocks.chest.locked"), true);
                            return InteractionResult.FAIL;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide) return true;
            if (!(player instanceof ServerPlayer serverPlayer)) return true;

            GlobalPos globalPos = GlobalPos.of(serverPlayer.serverLevel().dimension(), pos);
            MarketplaceLinkSavedData linkData = MarketplaceLinkSavedData.get(serverPlayer.serverLevel());
            if (linkData.isLinked(globalPos)) {
                if (!player.hasPermissions(2)) {
                    player.sendSystemMessage(Component.translatable("command.marketblocks.break.denied"));
                    return false;
                } else {
                    linkData.removeLink(globalPos);
                    linkData.syncToAll(serverPlayer.getServer());
                    player.sendSystemMessage(Component.translatable("command.marketblocks.break.unlinked"));
                }
            }

            if (state.getBlock() instanceof ChestBlock) {
                for (Direction dir : Direction.values()) {
                    BlockPos shopPos = pos.relative(dir);
                    if (world.getBlockEntity(shopPos) instanceof SingleOfferShopBlockEntity shop) {
                        if (!shop.getOwners().contains(player.getUUID()) && !player.hasPermissions(2)) {
                            player.displayClientMessage(Component.translatable("message.marketblocks.chest.locked"), true);
                            return false;
                        }
                    }
                }
            }
            return true;
        });

        Constants.LOG.info("MarketBlocks Fabric initialized successfully.");
    }
}
