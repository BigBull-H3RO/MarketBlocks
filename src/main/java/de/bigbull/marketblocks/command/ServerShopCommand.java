package de.bigbull.marketblocks.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.bigbull.marketblocks.shop.server.ServerShopManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ServerShopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("servershop")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ServerShopCommand::executeReload)
                )
                .then(Commands.literal("resetlimits")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ServerShopCommand::executeResetLimits)
                        )
                )
        );
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerShopManager.get().loadFromDisk();
        source.sendSuccess(() -> Component.literal("Server Shop configuration reloaded from disk."), true);
        return 1;
    }

    private static int executeResetLimits(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            boolean changed = ServerShopManager.get().resetLimitsForPlayer(player.getUUID());
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
    }
}
