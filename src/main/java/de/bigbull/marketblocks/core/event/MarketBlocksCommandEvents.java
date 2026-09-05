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
import de.bigbull.marketblocks.core.command.ShopSearchCommand;
import de.bigbull.marketblocks.core.command.ShopStatsCommand;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
 * Registers all MarketBlocks commands and handles internal click-action
 * subcommands.
 */
@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class MarketBlocksCommandEvents {

    private MarketBlocksCommandEvents() {
    }

    private static final com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack> LINK_SUGGESTIONS = (
            context, builder) -> {
        String input = builder.getRemainingLowerCase();
        for (Map.Entry<GlobalPos, MarketplaceLinkSavedData.LinkInfo> entry : MarketplaceLinkSavedData
                .get(context.getSource().getLevel()).getLinkedBlocks()
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

        var rootNode = event.getDispatcher().register(
                Commands.literal("marketblocks")
                        .then(ShopSearchCommand.build(buildContext))
                        .then(ShopStatsCommand.build())
                        .then(Commands.literal("marketplace")
                                .then(Commands.literal("open")
                                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            MarketplaceManager.get().openShop(player);
                                            return 1;
                                        })))
                        .then(MarketplaceAdminCommand.build(LINK_SUGGESTIONS))
                        .then(Commands.literal("internal")
                                .requires(source -> source.getEntity() instanceof ServerPlayer)
                                .then(Commands.literal("waypoint")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                                .then(Commands.argument("dim", StringArgumentType.string())
                                                                        .then(Commands
                                                                                .argument("name", StringArgumentType.greedyString())
                                                                                .executes(
                                                                                        MarketBlocksCommandEvents::executeInternalWaypoint)))))))
                                .then(Commands.literal("tp")
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
                                                                                                true)))))))))));

        event.getDispatcher().register(Commands.literal("mb").redirect(rootNode));
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
                "xaero_waypoint:%s:%s:%d:%d:%d:1:false:0:Internal-dim%s",
                name, label, x, y, z, xaeroDim);

        boolean hasJourneyMap = ModList.get().isLoaded("journeymap");
        boolean hasXaero = ModList.get().isLoaded("xaerominimap")
                || ModList.get().isLoaded("xaeroworldmap");

        if (!hasJourneyMap && !hasXaero) {
            player.sendSystemMessage(Component.translatable("command.marketblocks.internal.waypoint.coords",
                    name, x, y, z, dim).withStyle(ChatFormatting.GOLD));
            return 1;
        }

        if (hasJourneyMap) {
            player.sendSystemMessage(Component.translatable("command.marketblocks.internal.waypoint.journeymap")
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (hasXaero) {
            player.sendSystemMessage(Component.translatable("command.marketblocks.internal.waypoint.xaero")
                    .withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal(xaeroWaypoint).withStyle(ChatFormatting.GRAY));
        }

        return 1;
    }

    private static int executeInternalTp(CommandContext<CommandSourceStack> context, boolean withRotation)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!Config.ALLOW_NON_OP_TELEPORT.get() && !player.hasPermissions(2)) {
            player.sendSystemMessage(
                    Component.translatable("command.marketblocks.internal.tp.no_permission")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        String dim = StringArgumentType.getString(context, "dim");
        double x = DoubleArgumentType.getDouble(context, "x");
        double y = DoubleArgumentType.getDouble(context, "y");
        double z = DoubleArgumentType.getDouble(context, "z");

        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse(dim));
        ServerLevel targetLevel = player.getServer().getLevel(worldKey);

        if (targetLevel == null) {
            player.sendSystemMessage(
                    Component.translatable("command.marketblocks.internal.tp.invalid_dimension")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        float yaw = player.getYRot();
        float pitch = player.getXRot();

        if (withRotation) {
            yaw = (float) DoubleArgumentType.getDouble(context, "yaw");
            pitch = (float) DoubleArgumentType.getDouble(context, "pitch");
        } else {
            BlockPos targetBlock = BlockPos.containing(x, y, z);
            BlockState state = targetLevel.getBlockState(targetBlock);
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                yaw = facing.toYRot();
            }
        }

        player.teleportTo(targetLevel, x, y, z, yaw, pitch);
        player.sendSystemMessage(
                Component.translatable("command.marketblocks.internal.tp.success")
                        .withStyle(ChatFormatting.GREEN));
        return 1;
    }
}
