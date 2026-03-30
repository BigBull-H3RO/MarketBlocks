package de.bigbull.marketblocks.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopManager;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bindet den Server-Shop an den Lebenszyklus des Servers.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class ServerShopEvents {
    private static final String EDIT_MODE_ENABLED_KEY = "message.marketblocks.server_shop.edit_mode_enabled";
    private static final String EDIT_MODE_DISABLED_KEY = "message.marketblocks.server_shop.edit_mode_disabled";

    private ServerShopEvents() {
    }

    @SubscribeEvent
    public static void handleServerStart(ServerAboutToStartEvent event) {
        ServerShopManager.get().initialize(event.getServer());
    }

    @SubscribeEvent
    public static void handleServerStop(ServerStoppingEvent event) {
        ServerShopManager.get().shutdown();
    }

    @SubscribeEvent
    public static void handleServerTick(ServerTickEvent.Post event) {
        ServerShopManager.get().tick();
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("servershop")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerShopManager.get().openShop(player);
                            return 1;
                        })
                        .then(Commands.literal("editmode")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ServerShopManager manager = ServerShopManager.get();
                                    boolean enabled = !manager.isGlobalEditModeEnabled();
                                    setGlobalEditModeAndNotify(enabled, context.getSource());
                                    return 1;
                                })
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            setGlobalEditModeAndNotify(enabled, context.getSource());
                                            return 1;
                                        }))));
    }

    private static void setGlobalEditModeAndNotify(boolean enabled, net.minecraft.commands.CommandSourceStack source) {
        ServerShopManager.get().setGlobalEditModeEnabled(enabled);
        source.sendSuccess(
                () -> Component.translatable(enabled ? EDIT_MODE_ENABLED_KEY : EDIT_MODE_DISABLED_KEY),
                true);
    }
}
