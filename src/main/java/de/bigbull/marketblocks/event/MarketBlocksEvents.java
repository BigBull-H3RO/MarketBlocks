package de.bigbull.marketblocks.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceManager;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Binds the marketplace lifecycle to server events.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class MarketBlocksEvents {
    private static final String EDIT_MODE_ENABLED_KEY = "message.marketblocks.marketplace.edit_mode_enabled";
    private static final String EDIT_MODE_DISABLED_KEY = "message.marketblocks.marketplace.edit_mode_disabled";

    private MarketBlocksEvents() {
    }

    @SubscribeEvent
    public static void handleServerStart(ServerAboutToStartEvent event) {
        MarketplaceManager.get().initialize(event.getServer());
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
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("marketblocks")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("adminmode")
                                .executes(context -> {
                                    MarketplaceManager manager = MarketplaceManager.get();
                                    boolean enabled = !manager.isGlobalEditModeEnabled();
                                    setGlobalAdminModeAndNotify(enabled, context.getSource());
                                    return 1;
                                })
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            setGlobalAdminModeAndNotify(enabled, context.getSource());
                                            return 1;
                                        })))
                        .then(Commands.literal("marketplace")
                                .requires(source -> source.getEntity() instanceof ServerPlayer)
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    MarketplaceManager.get().openShop(player);
                                    return 1;
                                })
                                .then(Commands.literal("reload")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();
                                            MarketplaceManager.get().reload();
                                            source.sendSuccess(() -> Component.literal("Marketplace configuration reloaded from disk."), true);
                                            return 1;
                                }))
                                .then(Commands.literal("resetlimits")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    try {
                                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                        boolean changed = MarketplaceManager.get().resetLimitsForPlayer(player.getUUID());
                                                        if (changed) {
                                                            source.sendSuccess(() -> Component.literal("Reset daily limits for player " + player.getName().getString()), true);
                                                        } else {
                                                            source.sendSuccess(() -> Component.literal("No limits to reset for player " + player.getName().getString()), true);
                                                        }
                                                    } catch (Exception e) {
                                                        source.sendFailure(Component.literal("Player not found."));
                                                        return 0;
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static void setGlobalAdminModeAndNotify(boolean enabled, CommandSourceStack source) {
        MarketplaceManager.get().setGlobalEditModeEnabled(enabled);
        refreshOpenSingleOfferMenus(source);
        source.sendSuccess(
                () -> Component.translatable(enabled ? EDIT_MODE_ENABLED_KEY : EDIT_MODE_DISABLED_KEY),
                true);
    }

    private static void refreshOpenSingleOfferMenus(CommandSourceStack source) {
        if (source.getServer() == null) {
            return;
        }
        for (var player : source.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof SingleOfferShopMenu menu) {
                menu.broadcastChanges();
            }
        }
    }
}
