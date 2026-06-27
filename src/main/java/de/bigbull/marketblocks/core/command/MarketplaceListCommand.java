package de.bigbull.marketblocks.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles the {@code /marketblocks marketplace list [page]} command.
 * Displays all linked Marketplace blocks with pagination and action buttons.
 */
public final class MarketplaceListCommand {

        private MarketplaceListCommand() {
        }

        /**
         * Builds the Brigadier command node for {@code marketplace list}.
         */
        public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> build() {
                return Commands.literal("list")
                                .requires(source -> source.hasPermission(2))
                                .executes(c -> executeMarketplaceList(c, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(c -> executeMarketplaceList(c,
                                                                IntegerArgumentType.getInteger(c, "page"))));
        }

        private static int executeMarketplaceList(CommandContext<CommandSourceStack> context, int page) {
                CommandSourceStack source = context.getSource();
                MarketplaceLinkSavedData data = MarketplaceLinkSavedData.get(source.getLevel());
                List<MarketplaceLinkSavedData.LinkInfo> links = new ArrayList<>(data.getLinkedBlocks().values());

                if (links.isEmpty()) {
                        source.sendSuccess(
                                        () -> Component.translatable("command.marketblocks.marketplacelist.no_links"),
                                        false);
                        return 1;
                }

                displayPaginatedMarketplaces(source, links, page, "/marketblocks marketplace list ");

                return 1;
        }

        private static void displayPaginatedMarketplaces(CommandSourceStack source,
                        List<MarketplaceLinkSavedData.LinkInfo> links, int page, String baseCmd) {
                int totalPages = CommandUtils.totalPages(links.size());
                page = CommandUtils.clampPage(page, totalPages);

                final int currentPage = page;
                final int finalTotalPages = totalPages;
                source.sendSuccess(() -> Component
                                .translatable("command.marketblocks.marketplacelist.page_header", currentPage,
                                                finalTotalPages)
                                .withStyle(ChatFormatting.GOLD), false);

                int start = (page - 1) * CommandUtils.ITEMS_PER_PAGE;
                int end = Math.min(start + CommandUtils.ITEMS_PER_PAGE, links.size());

                for (int i = start; i < end; i++) {
                        MarketplaceLinkSavedData.LinkInfo info = links.get(i);
                        GlobalPos pos = info.blockPos;
                        String name = info.name != null && !info.name.isEmpty() ? info.name
                                        : pos.dimension().location().toString();

                        MutableComponent text = Component.translatable("command.marketblocks.marketplacelist.entry",
                                        (i + 1), name);

                        if (source.getEntity() instanceof ServerPlayer player
                                        && (player.hasPermissions(2) || Config.ALLOW_NON_OP_TELEPORT.get())) {
                                final double finalTpX = info.tpPos != null ? info.tpPos.x
                                                : CommandUtils.calculateTeleportPos(pos, source.getServer()).x;
                                final double finalTpY = info.tpPos != null ? info.tpPos.y
                                                : CommandUtils.calculateTeleportPos(pos, source.getServer()).y;
                                final double finalTpZ = info.tpPos != null ? info.tpPos.z
                                                : CommandUtils.calculateTeleportPos(pos, source.getServer()).z;

                                String tpCmd = (info.tpPos != null && info.tpYaw != null && info.tpPitch != null)
                                                ? String.format(Locale.US,
                                                                "/mb_internal_tp \"%s\" %.2f %.2f %.2f %.2f %.2f",
                                                                pos.dimension().location(), finalTpX, finalTpY,
                                                                finalTpZ, info.tpYaw, info.tpPitch)
                                                : String.format(Locale.US, "/mb_internal_tp \"%s\" %.2f %.2f %.2f",
                                                                pos.dimension().location(), finalTpX, finalTpY,
                                                                finalTpZ);

                                text.append(Component.literal(" ")
                                                .append(Component.translatable("command.marketblocks.list.tp")
                                                                .withStyle(ChatFormatting.GRAY))
                                                .withStyle(style -> style
                                                                .withClickEvent(new ClickEvent(
                                                                                ClickEvent.Action.RUN_COMMAND, tpCmd))
                                                                .withHoverEvent(new HoverEvent(
                                                                                HoverEvent.Action.SHOW_TEXT,
                                                                                Component.translatable(
                                                                                                "command.marketblocks.list.click_to_teleport")))));
                        }
                        if (source.getEntity() instanceof ServerPlayer player && player.hasPermissions(2)) {
                                String coordName = pos.pos().getX() + "_" + pos.pos().getY() + "_" + pos.pos().getZ();
                                text.append(Component.literal(" ")
                                                .append(Component.translatable("command.marketblocks.list.delete")
                                                                .withStyle(ChatFormatting.RED))
                                                .withStyle(style -> style.withClickEvent(new ClickEvent(
                                                                ClickEvent.Action.RUN_COMMAND,
                                                                String.format(Locale.US,
                                                                                "/execute in %s run marketblocks marketplace unlink %s",
                                                                                pos.dimension().location(), coordName)))
                                                                .withHoverEvent(new HoverEvent(
                                                                                HoverEvent.Action.SHOW_TEXT,
                                                                                Component.translatable(
                                                                                                "command.marketblocks.list.click_to_delete")))));
                        }

                        text.append(Component.literal(" ")
                                        .append(Component.translatable("command.marketblocks.list.waypoint")
                                                        .withStyle(ChatFormatting.AQUA))
                                        .withStyle(style -> style.withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        String.format(Locale.US,
                                                                        "/mb_internal_waypoint %d %d %d \"%s\" %s",
                                                                        pos.pos().getX(),
                                                                        pos.pos().getY(), pos.pos().getZ(),
                                                                        pos.dimension().location(),
                                                                        name.isEmpty() ? "Marketplace"
                                                                                        : name.replace(" ", "_"))))
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                        Component.translatable(
                                                                                        "command.marketblocks.list.click_to_waypoint")))));

                        source.sendSuccess(() -> text, false);
                }

                CommandUtils.sendPaginationFooter(source, page, totalPages, baseCmd);
        }
}
