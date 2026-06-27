package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.core.config.Config;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple rate limiter for incoming packets to prevent malicious clients from spamming the server.
 */
public class PacketRateLimiter {
    private static final Map<UUID, Long> lastPacketTimes = new ConcurrentHashMap<>();

    /**
     * Checks if the player is allowed to process a packet according to the rate limit configuration.
     * 
     * @param player The player to check
     * @return true if the packet should be processed, false if it should be rejected due to rate limiting
     */
    public static boolean canProcessPacket(Player player) {
        if (!Config.ENABLE_PACKET_RATE_LIMITING.get()) {
            return true;
        }

        long now = System.currentTimeMillis();
        long last = lastPacketTimes.getOrDefault(player.getUUID(), 0L);
        long cooldown = Config.PACKET_COOLDOWN_MS.get();

        if (now - last < cooldown) {
            return false;
        }

        lastPacketTimes.put(player.getUUID(), now);
        return true;
    }

    /**
     * Clears the rate limit history for a player.
     * This can be called when a player disconnects to prevent memory leaks.
     * 
     * @param playerId The UUID of the player to clear
     */
    public static void clearPlayer(UUID playerId) {
        lastPacketTimes.remove(playerId);
    }
}
