package de.bigbull.marketblocks.core.command;

import java.util.Locale;

import de.bigbull.marketblocks.core.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Shared utility methods for MarketBlocks commands.
 * Handles pagination footers, teleport position calculation, and waypoint/TP button building.
 */
public final class CommandUtils {

    public static final int ITEMS_PER_PAGE = 8;

    private CommandUtils() {
    }

    /**
     * Appends [TP] and [Waypoint] click-action buttons to a chat component.
     */
    public static void appendWaypointsAndTp(CommandSourceStack source, MutableComponent text, GlobalPos pos,
            String name) {
        if (source.getEntity() instanceof ServerPlayer player
                && (player.hasPermissions(2) || Config.ALLOW_NON_OP_TELEPORT.get())) {
            Vec3 tpPos = calculateTeleportPos(pos, source.getServer());
            final double finalTpX = tpPos.x;
            final double finalTpY = tpPos.y;
            final double finalTpZ = tpPos.z;

            text.append(Component.literal(" ")
                    .append(Component.translatable("command.marketblocks.list.tp").withStyle(ChatFormatting.GRAY))
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    String.format(Locale.US, "/mb_internal_tp \"%s\" %.2f %.2f %.2f",
                                            pos.dimension().location(), finalTpX, finalTpY, finalTpZ)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("command.marketblocks.list.click_to_teleport")))));
        }

        final String finalName = name;
        text.append(Component.literal(" ")
                .append(Component.translatable("command.marketblocks.list.waypoint").withStyle(ChatFormatting.AQUA))
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                String.format(Locale.US, "/mb_internal_waypoint %d %d %d \"%s\" %s", pos.pos().getX(),
                                        pos.pos().getY(), pos.pos().getZ(), pos.dimension().location(),
                                        finalName.isEmpty() ? "Waypoint" : finalName.replace(" ", "_"))))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("command.marketblocks.list.click_to_waypoint")))));
    }

    /**
     * Sends a pagination footer with [Prev] / [Next] buttons to the player.
     */
    public static void sendPaginationFooter(CommandSourceStack source, int page, int totalPages, String baseCmd) {
        if (totalPages <= 1)
            return;

        MutableComponent footer = Component.literal("");

        if (page > 1 && page < totalPages) {
            footer.append(Component.literal("======== ").withStyle(ChatFormatting.DARK_GRAY));
            footer.append(Component.translatable("command.marketblocks.list.prev").withStyle(
                    style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, baseCmd + (page - 1))))
                    .withStyle(ChatFormatting.YELLOW));
            footer.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
            footer.append(Component.translatable("command.marketblocks.list.next").withStyle(
                    style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, baseCmd + (page + 1))))
                    .withStyle(ChatFormatting.YELLOW));
            footer.append(Component.literal(" ========").withStyle(ChatFormatting.DARK_GRAY));
        } else if (page > 1) {
            footer.append(Component.literal("============= ").withStyle(ChatFormatting.DARK_GRAY));
            footer.append(Component.translatable("command.marketblocks.list.prev").withStyle(
                    style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, baseCmd + (page - 1))))
                    .withStyle(ChatFormatting.YELLOW));
            footer.append(Component.literal(" =============").withStyle(ChatFormatting.DARK_GRAY));
        } else if (page < totalPages) {
            footer.append(Component.literal("============= ").withStyle(ChatFormatting.DARK_GRAY));
            footer.append(Component.translatable("command.marketblocks.list.next").withStyle(
                    style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, baseCmd + (page + 1))))
                    .withStyle(ChatFormatting.YELLOW));
            footer.append(Component.literal(" =============").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (!footer.getString().isEmpty()) {
            source.sendSuccess(() -> footer, false);
        }
    }

    /**
     * Calculates the teleport position in front of a block, accounting for horizontal facing.
     */
    public static Vec3 calculateTeleportPos(GlobalPos pos, MinecraftServer server) {
        double tpX = pos.pos().getX() + 0.5;
        double tpY = pos.pos().getY();
        double tpZ = pos.pos().getZ() + 0.5;

        ServerLevel level = server.getLevel(pos.dimension());
        if (level != null && level.isLoaded(pos.pos())) {
            BlockState state = level.getBlockState(pos.pos());
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                tpX += facing.getStepX();
                tpZ += facing.getStepZ();
            } else {
                tpZ += 1.0;
            }
        } else {
            tpZ += 1.0;
        }
        return new Vec3(tpX, tpY, tpZ);
    }

    /**
     * Clamps the page number and returns the clamped value.
     */
    public static int clampPage(int page, int totalPages) {
        if (page < 1) return 1;
        if (page > totalPages) return totalPages;
        return page;
    }

    /**
     * Calculates total pages from item count.
     */
    public static int totalPages(int itemCount) {
        int pages = (int) Math.ceil((double) itemCount / ITEMS_PER_PAGE);
        return pages == 0 ? 1 : pages;
    }
}
