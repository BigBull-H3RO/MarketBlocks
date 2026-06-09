package de.bigbull.marketblocks.core.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.notification.PendingNotificationsSavedData;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.InteractionResult;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Central event handler for server lifecycle events and command registration on
 * the FORGE bus.
 * Manages the initialization of global systems (like the MarketplaceManager),
 * processes player logins
 * for offline notifications, and registers all `/marketblocks` administrative
 * and utility commands.
 * Also handles block interactions for linking physical blocks to the virtual
 * Marketplace.
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
    public static void handlePlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PendingNotificationsSavedData data = PendingNotificationsSavedData.get(player.serverLevel());
            Set<BlockPos> emptyShops = data.getAndClearOutOfStock(player.getUUID());
            Set<BlockPos> fullShops = data.getAndClearOutputFull(player.getUUID());

            if (!emptyShops.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("gui.marketblocks.notifications.login.out_of_stock", emptyShops.size()));
                for (BlockPos pos : emptyShops) {
                    player.sendSystemMessage(Component
                            .literal(" - [X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ() + "]"));
                }
            }
            if (!fullShops.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("gui.marketblocks.notifications.login.output_full", fullShops.size()));
                for (BlockPos pos : fullShops) {
                    player.sendSystemMessage(Component
                            .literal(" - [X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ() + "]"));
                }
            }
        }
    }

    private static final com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack> LINK_SUGGESTIONS = (
            context, builder) -> {
        MarketplaceLinkSavedData data = MarketplaceLinkSavedData.get(context.getSource().getLevel());
        String input = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
        for (java.util.Map.Entry<GlobalPos, MarketplaceLinkSavedData.LinkInfo> entry : data.getLinkedBlocks()
                .entrySet()) {
            String name = entry.getValue().name;
            if (name != null && !name.isEmpty()) {
                if (name.toLowerCase(java.util.Locale.ROOT).startsWith(input)) {
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
        event.getDispatcher().register(
                Commands.literal("marketblocks")
                        .then(Commands.literal("list")
                                .executes(context -> {
                                    MarketBlocksEvents.executeList(context);
                                    if (context.getSource().hasPermission(2)) {
                                        MarketBlocksEvents.executeMarketplaceList(context);
                                    }
                                    return 1;
                                })
                                .then(Commands.literal("shops")
                                        .executes(MarketBlocksEvents::executeList))
                                .then(Commands.literal("marketplaces")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(MarketBlocksEvents::executeMarketplaceList)))
                        .then(Commands.literal("adminmode")
                                .requires(source -> source.hasPermission(2))
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
                                            source.sendSuccess(
                                                    () -> Component
                                                            .translatable("command.marketblocks.reload.success"),
                                                    true);
                                            return 1;
                                        }))
                                .then(Commands.literal("resetlimits")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    try {
                                                        ServerPlayer player = EntityArgument.getPlayer(context,
                                                                "player");
                                                        boolean changed = MarketplaceManager.get()
                                                                .resetLimitsForPlayer(player.getUUID());
                                                        if (changed) {
                                                            source.sendSuccess(
                                                                    () -> Component
                                                                            .translatable(
                                                                                    "command.marketblocks.resetlimits.success",
                                                                                    player.getName().getString()),
                                                                    true);
                                                        } else {
                                                            source.sendSuccess(
                                                                    () -> Component
                                                                            .translatable(
                                                                                    "command.marketblocks.resetlimits.no_changes",
                                                                                    player.getName().getString()),
                                                                    true);
                                                        }
                                                    } catch (Exception e) {
                                                        source.sendFailure(Component
                                                                .translatable("command.marketblocks.player_not_found"));
                                                        return 0;
                                                    }
                                                    return 1;
                                                })))
                                .then(Commands.literal("link")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> linkBlock(context, null, null))
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(context -> linkBlock(context,
                                                        StringArgumentType.getString(context, "name"), null))
                                                .then(Commands.argument("tp_pos", Vec3Argument.vec3())
                                                        .executes(context -> linkBlock(context,
                                                                StringArgumentType.getString(context, "name"),
                                                                Vec3Argument.getVec3(context, "tp_pos"))))))
                                .then(Commands.literal("unlink")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(MarketBlocksEvents::executeMarketplaceUnlink)
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .suggests(LINK_SUGGESTIONS)
                                                .executes(MarketBlocksEvents::executeMarketplaceUnlinkByName)))));

        event.getDispatcher().register(
                Commands.literal("mb_internal_waypoint")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .then(Commands.argument("dim", StringArgumentType.string())
                                                        .then(Commands
                                                                .argument("name", StringArgumentType.greedyString())
                                                                .executes(context -> {
                                                                    ServerPlayer player = context.getSource()
                                                                            .getPlayerOrException();
                                                                    int x = IntegerArgumentType.getInteger(context,
                                                                            "x");
                                                                    int y = IntegerArgumentType.getInteger(context,
                                                                            "y");
                                                                    int z = IntegerArgumentType.getInteger(context,
                                                                            "z");
                                                                    String dim = StringArgumentType.getString(context,
                                                                            "dim");
                                                                    String name = StringArgumentType.getString(context,
                                                                            "name");

                                                                    String cleanName = name.replace(":", "");
                                                                    String label = cleanName.isEmpty() ? "S"
                                                                            : cleanName.substring(0, 1).toUpperCase();
                                                                    String xaeroDim = dim.replace("minecraft:",
                                                                            "Internal-") + "-waypoints";

                                                                    String xaeroWaypoint = String.format(Locale.US,
                                                                            "xaero-waypoint:%s:%s:%d:%d:%d:4:false:0:%s",
                                                                            cleanName, label, x, y, z, xaeroDim);
                                                                    String jmWaypoint = String.format(Locale.US,
                                                                            "[x:%d,y:%d,z:%d,dim:%s,name:%s]", x, y, z,
                                                                            dim, cleanName);

                                                                    player.sendSystemMessage(Component.translatable(
                                                                            "command.marketblocks.waypoint.created")
                                                                            .withStyle(ChatFormatting.GREEN));

                                                                    if (Config.ENABLE_XAEROS_COMPAT.get()
                                                                            && ModList.get().isLoaded("xaerominimap")) {
                                                                        player.sendSystemMessage(Component
                                                                                .literal("Xaero's Minimap: ")
                                                                                .append(Component.literal(xaeroWaypoint)
                                                                                        .withStyle(
                                                                                                ChatFormatting.AQUA)));
                                                                    }
                                                                    if (Config.ENABLE_JOURNEYMAP_COMPAT.get()
                                                                            && ModList.get().isLoaded("journeymap")) {
                                                                        player.sendSystemMessage(Component
                                                                                .literal("JourneyMap: ")
                                                                                .append(Component.literal(jmWaypoint)
                                                                                        .withStyle(
                                                                                                ChatFormatting.AQUA)));
                                                                    }
                                                                    return 1;
                                                                })))))));
    }

    private static int linkBlock(CommandContext<CommandSourceStack> context, String name, Vec3 tpPos)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);

            Vec3 finalTpPos = tpPos;
            Float finalTpYaw = null;
            Float finalTpPitch = null;
            if (finalTpPos == null) {
                finalTpPos = net.minecraft.world.phys.Vec3.atBottomCenterOf(player.blockPosition());
                finalTpYaw = net.minecraft.core.Direction.fromYRot(player.getYRot()).toYRot();
                finalTpPitch = 0.0f;
            }

            if (MarketplaceLinkSavedData.get(player.serverLevel()).addLink(globalPos, name, finalTpPos, finalTpYaw,
                    finalTpPitch)) {
                player.sendSystemMessage(Component.translatable("command.marketblocks.link.success"));
            } else {
                player.sendSystemMessage(Component.translatable("command.marketblocks.link.already_linked"));
            }
        } else {
            player.sendSystemMessage(Component.translatable("command.marketblocks.link.not_looking_at_block"));
        }
        return 1;
    }

    private static int executeMarketplaceUnlink(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);
            if (MarketplaceLinkSavedData.get(player.serverLevel()).removeLink(globalPos)) {
                player.sendSystemMessage(Component.translatable("command.marketblocks.unlink.success"));
            } else {
                player.sendSystemMessage(Component.translatable("command.marketblocks.unlink.not_linked"));
            }
        } else {
            player.sendSystemMessage(Component.translatable("command.marketblocks.unlink.not_looking_at_block"));
        }
        return 1;
    }

    private static int executeMarketplaceUnlinkByName(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        net.minecraft.server.level.ServerLevel level = context.getSource().getLevel();
        int removed = MarketplaceLinkSavedData.get(level).removeLinkByName(name);
        if (removed > 0) {
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.marketblocks.unlink.success_name", removed, name), true);
        } else {
            context.getSource().sendFailure(Component.translatable("command.marketblocks.unlink.not_found", name));
        }
        return 1;
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

    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ShopDirectorySavedData data = ShopDirectorySavedData.get(source.getLevel());
        List<ShopDirectorySavedData.ShopEntry> shops = data.getShops();

        if (shops.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.marketblocks.shoplist.no_shops"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("command.marketblocks.shoplist.header"), false);
        for (ShopDirectorySavedData.ShopEntry shop : shops) {
            String shopName = shop.shopName();
            if (shopName == null || shopName.isEmpty()) {
                shopName = "Unnamed Shop";
            }

            String owner = shop.ownerName() != null ? shop.ownerName() : "Unknown";
            Component status = shop.isClosed()
                    ? Component.translatable("command.marketblocks.shoplist.closed")
                            .withStyle(net.minecraft.ChatFormatting.RED)
                    : Component.translatable("command.marketblocks.shoplist.open")
                            .withStyle(net.minecraft.ChatFormatting.GREEN);
            GlobalPos pos = shop.pos();

            MutableComponent text = Component.translatable("command.marketblocks.shoplist.entry", status, shopName,
                    owner);

            if (source.getEntity() instanceof ServerPlayer player && player.hasPermissions(2)) {
                double tpX = pos.pos().getX() + 0.5;
                double tpY = pos.pos().getY();
                double tpZ = pos.pos().getZ() + 0.5;

                net.minecraft.server.level.ServerLevel shopLevel = source.getServer().getLevel(pos.dimension());
                if (shopLevel != null && shopLevel.isLoaded(pos.pos())) {
                    net.minecraft.world.level.block.state.BlockState state = shopLevel.getBlockState(pos.pos());
                    if (state.hasProperty(
                            net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                        net.minecraft.core.Direction facing = state.getValue(
                                net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                        tpX += facing.getStepX();
                        tpZ += facing.getStepZ();
                    } else {
                        tpZ += 1.0;
                    }
                } else {
                    tpZ += 1.0;
                }

                final double finalTpX = tpX;
                final double finalTpY = tpY;
                final double finalTpZ = tpZ;

                text.append(Component.literal(" ")
                        .append(Component.translatable(
                                "command.marketblocks.list.tp").withStyle(net.minecraft.ChatFormatting.GRAY))
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        String.format(java.util.Locale.US, "/execute in %s run tp @s %.2f %.2f %.2f",
                                                pos.dimension().location(), finalTpX, finalTpY, finalTpZ)))
                                .withHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component
                                                .translatable("command.marketblocks.list.click_to_teleport")))));
            }

            final String finalShopName = shopName;

            text.append(
                    Component.literal(" ")
                            .append(Component.translatable(
                                    "command.marketblocks.list.waypoint").withStyle(net.minecraft.ChatFormatting.AQUA))
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            String.format(java.util.Locale.US,
                                                    "/mb_internal_waypoint %d %d %d \"%s\" %s", pos.pos().getX(),
                                                    pos.pos().getY(), pos.pos().getZ(), pos.dimension().location(),
                                                    finalShopName.isEmpty() ? "Shop"
                                                            : finalShopName.replace(" ", "_"))))
                                    .withHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component
                                                    .translatable("command.marketblocks.list.click_to_waypoint")))));

            source.sendSuccess(() -> text, false);
        }

        return 1;
    }

    private static int executeMarketplaceList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MarketplaceLinkSavedData data = MarketplaceLinkSavedData.get(source.getLevel());
        java.util.Map<GlobalPos, MarketplaceLinkSavedData.LinkInfo> links = data.getLinkedBlocks();

        if (links.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.marketblocks.marketplacelist.no_links"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("command.marketblocks.marketplacelist.header"), false);
        int index = 1;
        for (MarketplaceLinkSavedData.LinkInfo info : links.values()) {
            GlobalPos pos = info.blockPos;
            String name = info.name != null && !info.name.isEmpty() ? info.name : pos.dimension().location().toString();
            MutableComponent text = Component.translatable("command.marketblocks.marketplacelist.entry", index++, name);

            if (source.getEntity() instanceof ServerPlayer player && player.hasPermissions(2)) {
                double tpX = pos.pos().getX() + 0.5;
                double tpY = pos.pos().getY();
                double tpZ = pos.pos().getZ() + 0.5;

                if (info.tpPos != null) {
                    tpX = info.tpPos.x;
                    tpY = info.tpPos.y;
                    tpZ = info.tpPos.z;
                } else {
                    net.minecraft.server.level.ServerLevel linkLevel = source.getServer().getLevel(pos.dimension());
                    if (linkLevel != null && linkLevel.isLoaded(pos.pos())) {
                        net.minecraft.world.level.block.state.BlockState state = linkLevel.getBlockState(pos.pos());
                        if (state.hasProperty(
                                net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                            net.minecraft.core.Direction facing = state.getValue(
                                    net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                            tpX += facing.getStepX();
                            tpZ += facing.getStepZ();
                        } else {
                            tpZ += 1.0;
                        }
                    } else {
                        tpZ += 1.0;
                    }
                }

                final double finalTpX = tpX;
                final double finalTpY = tpY;
                final double finalTpZ = tpZ;

                if (info.tpPos != null && info.tpYaw != null && info.tpPitch != null) {
                    final float finalTpYaw = info.tpYaw;
                    final float finalTpPitch = info.tpPitch;
                    text.append(Component.literal(" ")
                            .append(Component.translatable(
                                    "command.marketblocks.list.tp").withStyle(net.minecraft.ChatFormatting.GRAY))
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            String.format(java.util.Locale.US,
                                                    "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
                                                    pos.dimension().location(), finalTpX, finalTpY, finalTpZ,
                                                    finalTpYaw, finalTpPitch)))
                                    .withHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component
                                                    .translatable("command.marketblocks.list.click_to_teleport")))));
                } else {
                    text.append(Component.literal(" ")
                            .append(Component.translatable(
                                    "command.marketblocks.list.tp").withStyle(net.minecraft.ChatFormatting.GRAY))
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            String.format(java.util.Locale.US,
                                                    "/execute in %s run tp @s %.2f %.2f %.2f",
                                                    pos.dimension().location(), finalTpX, finalTpY, finalTpZ)))
                                    .withHoverEvent(
                                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component
                                                    .translatable("command.marketblocks.list.click_to_teleport")))));
                }

                String coordName = pos.pos().getX() + "_" + pos.pos().getY() + "_" + pos.pos().getZ();
                text.append(Component.literal(" ")
                        .append(Component.translatable("command.marketblocks.list.delete")
                                .withStyle(net.minecraft.ChatFormatting.RED))
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        String.format(java.util.Locale.US,
                                                "/execute in %s run marketblocks marketplace unlink %s",
                                                pos.dimension().location(), coordName)))
                                .withHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.translatable("command.marketblocks.list.click_to_delete")))));
            }

            final String finalMarketplaceName = name;

            text.append(Component.literal(" ")
                    .append(Component.translatable(
                            "command.marketblocks.list.waypoint").withStyle(net.minecraft.ChatFormatting.AQUA))
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format(java.util.Locale.US, "/mb_internal_waypoint %d %d %d \"%s\" %s",
                                            pos.pos().getX(), pos.pos().getY(), pos.pos().getZ(),
                                            pos.dimension().location(),
                                            finalMarketplaceName.isEmpty() ? "Marketplace"
                                                    : finalMarketplaceName.replace(" ", "_"))))
                            .withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.translatable("command.marketblocks.list.click_to_waypoint")))));

            source.sendSuccess(() -> text, false);
        }

        return 1;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide())
            return;
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        BlockPos pos = event.getPos();
        GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);

        boolean isLinked = MarketplaceLinkSavedData.get(player.serverLevel()).isLinked(globalPos);

        if (isLinked) {
            MarketplaceManager.get().openShop(player);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide())
            return;
        if (!(event.getPlayer() instanceof ServerPlayer player))
            return;

        BlockPos pos = event.getPos();
        GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);

        boolean isLinked = MarketplaceLinkSavedData.get(player.serverLevel()).isLinked(globalPos);

        if (isLinked) {
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("command.marketblocks.break.denied"));
                event.setCanceled(true);
            } else {
                MarketplaceLinkSavedData.get(player.serverLevel()).removeLink(globalPos);
                player.sendSystemMessage(Component.translatable("command.marketblocks.break.unlinked"));
            }
        }
    }
}
