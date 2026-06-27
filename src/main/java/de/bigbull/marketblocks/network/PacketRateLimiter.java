package de.bigbull.marketblocks.network;

import de.bigbull.marketblocks.core.config.Config;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple rate limiter for incoming packets to prevent malicious clients from spamming the server.
 */
public class PacketRateLimiter {
    private record PlayerPacketKey(UUID playerId, CustomPacketPayload.Type<?> packetType) {}
    private static final Map<PlayerPacketKey, Long> lastPacketTimes = new ConcurrentHashMap<>();

    /**
     * Checks if the player is allowed to process a packet according to the rate limit configuration.
     * 
     * @param player The player to check
     * @param packetType The type of the packet being processed
     * @return true if the packet should be processed, false if it should be rejected due to rate limiting
     */
    public static boolean canProcessPacket(Player player, CustomPacketPayload.Type<?> packetType) {
        if (!Config.ENABLE_PACKET_RATE_LIMITING.get()) {
            return true;
        }

        long now = System.currentTimeMillis();
        PlayerPacketKey key = new PlayerPacketKey(player.getUUID(), packetType);
        long last = lastPacketTimes.getOrDefault(key, 0L);
        long cooldown = Config.PACKET_COOLDOWN_MS.get();

        if (packetType.equals(de.bigbull.marketblocks.feature.singleoffer.network.SwitchTabPacket.TYPE) ||
            packetType.equals(de.bigbull.marketblocks.feature.marketplace.network.MarketplaceSelectPagePacket.TYPE) ||
            packetType.equals(de.bigbull.marketblocks.feature.marketplace.network.MarketplaceOpenRequestPacket.TYPE)) {
            cooldown = Math.min(cooldown, 25L);
        }

        if (now - last < cooldown) {
            return false;
        }

        lastPacketTimes.put(key, now);
        return true;
    }

    /**
     * Clears the rate limit history for a player.
     * This can be called when a player disconnects to prevent memory leaks.
     * 
     * @param playerId The UUID of the player to clear
     */
    public static void clearPlayer(UUID playerId) {
        lastPacketTimes.keySet().removeIf(key -> key.playerId().equals(playerId));
    }
}
