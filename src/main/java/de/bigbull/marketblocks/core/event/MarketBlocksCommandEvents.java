package de.bigbull.marketblocks.core.event;

import java.util.Locale;
import java.util.Map;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.command.MarketplaceAdminCommand;
import de.bigbull.marketblocks.core.command.MarketplaceListCommand;
import de.bigbull.marketblocks.core.command.ShopListCommand;
import de.bigbull.marketblocks.core.command.ShopSearchCommand;
import de.bigbull.marketblocks.core.command.ShopStatsCommand;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Registers all MarketBlocks commands by delegating to dedicated command classes
 * under {@link de.bigbull.marketblocks.core.command}.
 *
 * <p>Also contains the two internal helper commands ({@code mb_internal_waypoint} and
 * {@code mb_internal_tp}) that are triggered via chat click-events and not intended
 * for direct player use.</p>
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class MarketBlocksCommandEvents {

    private MarketBlocksCommandEvents() {
    }

    private static final com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack> LINK_SUGGESTIONS = (
            context, builder) -> {
        MarketplaceLinkSavedData data = MarketplaceLinkSavedData.get(context.getSource().getLevel());
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<GlobalPos, MarketplaceLinkSavedData.LinkInfo> entry : data.getLinkedBlocks()
                .entrySet()) {
            String name = entry.getValue().name;
            if (name != null && !name.isEmpty()) {
                if (name.toLowerCase(Locale.ROOT).startsWith(input)) {
                    if (name.contains(" ")) {
                        builder.suggest("\"" + name + "\"");
                    } else {
                        builder.suggest(name);
                    }
                }
            } else {
                BlockPos pos = entry.getKey().pos();
                String coordName = pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
                if (coordName.startsWith(input)) {
                    builder.suggest(coordName);
                }
            }
        }
        return builder.buildFuture();
    };

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var buildContext = event.getBuildContext();

        event.getDispatcher().register(
                Commands.literal("marketblocks")
                        .then(ShopSearchCommand.build(buildContext))
                        .then(ShopListCommand.build())
                        .then(Commands.literal("marketplace")
                                .then(ShopStatsCommand.buildMarketplaceStats())
                                .then(Commands.literal("open")
                                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            MarketplaceManager.get().openShop(player);
                                            return 1;
                                        }))
                                .then(MarketplaceListCommand.build()))
                        .then(MarketplaceAdminCommand.build(LINK_SUGGESTIONS, buildContext)));

        event.getDispatcher().register(
                Commands.literal("mb_internal_waypoint")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .then(Commands.argument("dim", StringArgumentType.string())
                                                        .then(Commands
                                                                .argument("name", StringArgumentType.greedyString())
                                                                .executes(
                                                                        MarketBlocksCommandEvents::executeInternalWaypoint)))))));

        event.getDispatcher().register(
                Commands.literal("mb_internal_tp")
                        .then(Commands.argument("dim", StringArgumentType.string())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(context -> executeInternalTp(context, false))
                                                        .then(Commands.argument("yaw", DoubleArgumentType.doubleArg())
                                                                .then(Commands
                                                                        .argument("pitch",
                                                                                DoubleArgumentType.doubleArg())
                                                                        .executes(context -> executeInternalTp(context,
                                                                                true)))))))));
    }

    // ── Internal helper commands (triggered by chat click-events) ──

    private static int executeInternalWaypoint(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        String dim = StringArgumentType.getString(context, "dim");
        String name = StringArgumentType.getString(context, "name");

        String cleanName = name.replace(":", "");
        String label = cleanName.isEmpty() ? "S" : cleanName.substring(0, 1).toUpperCase();
        String xaeroDim = dim.replace("minecraft:", "Internal-") + "-waypoints";

        String xaeroWaypoint = String.format(Locale.US,
                "xaero-waypoint:%s:%s:%d:%d:%d:4:false:0:%s",
                cleanName, label, x, y, z, xaeroDim);
        String jmWaypoint = String.format(Locale.US,
                "[x:%d,y:%d,z:%d,dim:%s,name:%s]", x, y, z, dim, cleanName);

        player.sendSystemMessage(Component.translatable("command.marketblocks.waypoint.created")
                .withStyle(ChatFormatting.GREEN));

        if (Config.ENABLE_XAEROS_COMPAT.get() && ModList.get().isLoaded("xaerominimap")) {
            player.sendSystemMessage(Component.literal("Xaero's Minimap: ")
                    .append(Component.literal(xaeroWaypoint).withStyle(ChatFormatting.AQUA)));
        }
        if (Config.ENABLE_JOURNEYMAP_COMPAT.get() && ModList.get().isLoaded("journeymap")) {
            player.sendSystemMessage(Component.literal("JourneyMap: ")
                    .append(Component.literal(jmWaypoint).withStyle(ChatFormatting.AQUA)));
        }
        return 1;
    }

    private static int executeInternalTp(CommandContext<CommandSourceStack> context, boolean hasRot)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!Config.ALLOW_NON_OP_TELEPORT.get() && !player.hasPermissions(2)) {
            player.sendSystemMessage(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return 0;
        }
        String dimStr = StringArgumentType.getString(context, "dim");
        double x = DoubleArgumentType.getDouble(context, "x");
        double y = DoubleArgumentType.getDouble(context, "y");
        double z = DoubleArgumentType.getDouble(context, "z");

        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimStr));
        ServerLevel targetLevel = context.getSource().getServer().getLevel(dim);
        if (targetLevel != null) {
            float yaw = hasRot ? (float) DoubleArgumentType.getDouble(context, "yaw") : player.getYRot();
            float pitch = hasRot ? (float) DoubleArgumentType.getDouble(context, "pitch") : player.getXRot();
            player.teleportTo(targetLevel, x, y, z, yaw, pitch);
        }
        return 1;
    }
}
