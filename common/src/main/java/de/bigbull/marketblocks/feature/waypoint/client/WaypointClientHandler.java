package de.bigbull.marketblocks.feature.waypoint.client;

import java.util.Locale;

import de.bigbull.marketblocks.compat.journeymap.JourneyMapCompat;
import de.bigbull.marketblocks.feature.waypoint.network.CreateWaypointPacket;
import de.bigbull.marketblocks.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class WaypointClientHandler {

    public static void handle(CreateWaypointPacket packet, Player player) {
        String name = packet.name().replace("_", " ");
        BlockPos pos = new BlockPos(packet.x(), packet.y(), packet.z());

        boolean hasJourneyMap = Services.PLATFORM.isModLoaded("journeymap");
        boolean hasXaero = Services.PLATFORM.isModLoaded("xaerominimap") || Services.PLATFORM.isModLoaded("xaeroworldmap");

        boolean handled = false;

        if (hasJourneyMap) {
            boolean created = JourneyMapCompat.createWaypoint(name, pos, packet.dim());
            if (created) {
                player.displayClientMessage(
                        Component.translatable("command.marketblocks.internal.waypoint.journeymap.success", name)
                                .withStyle(ChatFormatting.GREEN),
                        false);
                handled = true;
            }
        }

        if (hasXaero) {
            String cleanName = name.replace(":", "");
            String label = cleanName.isEmpty() ? "S" : cleanName.substring(0, 1).toUpperCase();
            String xaeroDim = packet.dim().replace("minecraft:", "Internal-") + "-waypoints";
            String xaeroWaypoint = String.format(Locale.US,
                    "xaero_waypoint:%s:%s:%d:%d:%d:1:false:0:Internal-dim%s",
                    name, label, packet.x(), packet.y(), packet.z(), xaeroDim);

            player.displayClientMessage(
                    Component.translatable("command.marketblocks.internal.waypoint.xaero")
                            .withStyle(ChatFormatting.YELLOW),
                    false);
            player.displayClientMessage(
                    Component.literal(xaeroWaypoint).withStyle(ChatFormatting.GRAY),
                    false);
            handled = true;
        }

        if (!handled) {
            player.displayClientMessage(
                    Component.translatable("command.marketblocks.internal.waypoint.coords",
                            name, packet.x(), packet.y(), packet.z(), packet.dim())
                            .withStyle(ChatFormatting.GOLD),
                    false);
        }
    }
}