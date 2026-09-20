package de.bigbull.marketblocks.core.config.toml;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.network.ConfigSyncPacket;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.platform.Services;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central manager for registering, loading, and synchronizing TOML configurations.
 * 
 * Supports server-to-client configuration synchronization:
 * - Server configurations are sent to clients upon joining or when reloaded.
 * - On clients, received server values are applied strictly in-memory (zero disk overwriting).
 * - When disconnected from a server, local disk configurations are automatically restored.
 */
public class TomlConfigManager {

    public static class ConfigEntry {
        private final TomlConfigSpec spec;
        private final String relativePath;
        private final TomlConfigType type;

        public ConfigEntry(TomlConfigSpec spec, String relativePath, TomlConfigType type) {
            this.spec = spec;
            this.relativePath = relativePath;
            this.type = type;
        }

        public TomlConfigSpec getSpec() {
            return spec;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public TomlConfigType getType() {
            return type;
        }
    }

    private static final Map<String, ConfigEntry> REGISTERED_CONFIGS = new LinkedHashMap<>();

    public static void register(TomlConfigSpec spec, String relativePath, TomlConfigType type) {
        REGISTERED_CONFIGS.put(relativePath, new ConfigEntry(spec, relativePath, type));
    }

    public static void register(TomlConfigSpec spec, String relativePath) {
        register(spec, relativePath, TomlConfigType.SERVER);
    }

    public static Map<String, ConfigEntry> getRegisteredConfigs() {
        return Collections.unmodifiableMap(REGISTERED_CONFIGS);
    }

    /**
     * Loads all registered configurations from the local disk config directory.
     */
    public static void loadAll() {
        Path configDir = Services.PLATFORM.getConfigDirectory();
        for (ConfigEntry entry : REGISTERED_CONFIGS.values()) {
            Path filePath = configDir.resolve(entry.getRelativePath());
            Constants.LOG.info("Loading configuration file: {}", filePath);
            entry.getSpec().load(filePath);
        }
    }

    /**
     * Constructs a synchronization packet containing all SERVER configuration values.
     */
    public static ConfigSyncPacket createSyncPacket() {
        Map<String, Map<String, String>> data = new HashMap<>();
        for (ConfigEntry entry : REGISTERED_CONFIGS.values()) {
            if (entry.getType() == TomlConfigType.SERVER) {
                data.put(entry.getRelativePath(), entry.getSpec().exportValues());
            }
        }
        return new ConfigSyncPacket(data);
    }

    /**
     * Applies server-synchronized configuration values to active in-memory specs.
     * 
     * NOTE: Disk files are NOT modified.
     */
    public static void applySyncedConfigs(Map<String, Map<String, String>> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String path = entry.getKey();
            ConfigEntry registered = REGISTERED_CONFIGS.get(path);
            if (registered != null && registered.getType() == TomlConfigType.SERVER) {
                registered.getSpec().applyValues(entry.getValue());
                Constants.LOG.info("Applied synchronized server config for {}", path);
            }
        }
    }

    /**
     * Sends the current server configuration to a specific connected player.
     */
    public static void syncToPlayer(ServerPlayer player) {
        if (player != null) {
            Constants.LOG.info("Syncing server configs to player {}", player.getName().getString());
            NetworkHandler.sendToPlayer(player, createSyncPacket());
        }
    }

    /**
     * Broadcasts the current server configuration to all connected players on the server.
     */
    public static void syncToAll(MinecraftServer server) {
        if (server == null) {
            return;
        }
        ConfigSyncPacket packet = createSyncPacket();
        Constants.LOG.info("Broadcasting server config sync to {} connected players", server.getPlayerList().getPlayerCount());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(player, packet);
        }
    }

    /**
     * Called on the client when disconnecting from a server to restore the local disk configurations.
     */
    public static void onClientDisconnect() {
        Constants.LOG.info("Disconnected from server. Restoring local configuration files from disk...");
        loadAll();
    }
}
