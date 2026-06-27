package de.bigbull.marketblocks.core.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.notification.PendingNotificationsSavedData;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Set;

/**
 * Handles server lifecycle events like start, stop, tick, and player login.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class MarketBlocksLifecycleEvents {

    private MarketBlocksLifecycleEvents() {
    }

    @SubscribeEvent
    public static void handleServerStart(ServerAboutToStartEvent event) {
        MarketplaceManager.get().initialize(event.getServer());
        TraderEconomyManager.get().load();
    }

    @SubscribeEvent
    public static void handleServerStop(ServerStoppingEvent event) {
        MarketplaceManager.get().shutdown();
    }

    @SubscribeEvent
    public static void handleServerTick(ServerTickEvent.Post event) {
        MarketplaceManager.get().tick();
    }

    @SubscribeEvent
    public static void handleLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            de.bigbull.marketblocks.feature.trader.ShopBuyerSpawner.tick(serverLevel);
        }
    }

    @SubscribeEvent
    public static void handlePlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
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
        }
    }
}
