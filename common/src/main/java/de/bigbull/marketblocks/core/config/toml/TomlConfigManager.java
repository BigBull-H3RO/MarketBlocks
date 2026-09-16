package de.bigbull.marketblocks.core.config.toml;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.platform.Services;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class TomlConfigManager {
    private static final Map<TomlConfigSpec, String> REGISTERED_CONFIGS = new LinkedHashMap<>();

    public static void register(TomlConfigSpec spec, String relativePath) {
        REGISTERED_CONFIGS.put(spec, relativePath);
    }

    public static void loadAll() {
        Path configDir = Services.PLATFORM.getConfigDirectory();
        for (Map.Entry<TomlConfigSpec, String> entry : REGISTERED_CONFIGS.entrySet()) {
            TomlConfigSpec spec = entry.getKey();
            String relativePath = entry.getValue();
            Path filePath = configDir.resolve(relativePath);
            Constants.LOG.info("Loading configuration file: {}", filePath);
            spec.load(filePath);
        }
    }
}
