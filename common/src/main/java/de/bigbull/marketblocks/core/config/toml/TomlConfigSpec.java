package de.bigbull.marketblocks.core.config.toml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TomlConfigSpec {
    private final List<TomlConfigValue<?>> values;
    private final Map<String, List<TomlConfigValue<?>>> valuesByCategory;

    public TomlConfigSpec(List<TomlConfigValue<?>> values) {
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        Map<String, List<TomlConfigValue<?>>> map = new LinkedHashMap<>();
        for (TomlConfigValue<?> val : values) {
            map.computeIfAbsent(val.getCategory(), k -> new ArrayList<>()).add(val);
        }
        this.valuesByCategory = map;
    }

    public List<TomlConfigValue<?>> getValues() {
        return values;
    }

    public synchronized void load(Path file) {
        if (!Files.exists(file)) {
            save(file);
            return;
        }

        Map<String, String> parsedEntries = new HashMap<>();
        String currentSection = "";

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    String sec = line.substring(1, line.length() - 1).trim();
                    if (sec.startsWith("\"") && sec.endsWith("\"") && sec.length() >= 2) {
                        sec = sec.substring(1, sec.length() - 1);
                    }
                    currentSection = sec;
                    continue;
                }

                int eqIdx = line.indexOf('=');
                if (eqIdx != -1) {
                    String key = line.substring(0, eqIdx).trim();
                    String rawVal = line.substring(eqIdx + 1).trim();

                    // Strip trailing comments if any (not inside quotes)
                    rawVal = stripInlineComment(rawVal);

                    String fullKey = currentSection.isEmpty() ? key : currentSection + "." + key;
                    parsedEntries.put(fullKey, rawVal);
                }
            }
        } catch (IOException e) {
            System.err.println("[MarketBlocks] Failed to read config file " + file + ": " + e.getMessage());
        }

        // Apply parsed values
        for (TomlConfigValue<?> val : values) {
            String fullKey = val.getCategory().isEmpty() ? val.getKey() : val.getCategory() + "." + val.getKey();
            if (parsedEntries.containsKey(fullKey)) {
                val.deserialize(parsedEntries.get(fullKey));
            } else if ("maxShopsPerPlayer".equals(val.getKey())) {
                String legacyKey = (val.getCategory().isEmpty() ? "" : val.getCategory() + ".") + "maxShopsPerPlayerSurvival";
                if (parsedEntries.containsKey(legacyKey)) {
                    val.deserialize(parsedEntries.get(legacyKey));
                }
            }
        }

        // Always save back so that new keys or comments are preserved/updated
        save(file);
    }

    public synchronized void save(Path file) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                boolean firstCategory = true;
                for (Map.Entry<String, List<TomlConfigValue<?>>> entry : valuesByCategory.entrySet()) {
                    String category = entry.getKey();
                    List<TomlConfigValue<?>> categoryValues = entry.getValue();

                    if (!firstCategory) {
                        writer.newLine();
                    }
                    firstCategory = false;

                    if (!category.isEmpty()) {
                        // Quote section if it contains spaces
                        if (category.contains(" ")) {
                            writer.write("[\"" + category + "\"]");
                        } else {
                            writer.write("[" + category + "]");
                        }
                        writer.newLine();
                    }

                    for (TomlConfigValue<?> val : categoryValues) {
                        // Write comments
                        for (String comment : val.getComments()) {
                            writer.write("\t# " + comment);
                            writer.newLine();
                        }

                        // Write range/default hints if applicable
                        if (val instanceof TomlConfigValue.IntValue intVal) {
                            writer.write("\t# Range: " + intVal.getMin() + " ~ " + intVal.getMax() + ", Default: " + intVal.getDefaultValue());
                            writer.newLine();
                        } else if (val instanceof TomlConfigValue.DoubleValue doubleVal) {
                            writer.write("\t# Range: " + doubleVal.getMin() + " ~ " + doubleVal.getMax() + ", Default: " + doubleVal.getDefaultValue());
                            writer.newLine();
                        } else {
                            writer.write("\t# Default: " + val.getDefaultValue());
                            writer.newLine();
                        }

                        writer.write("\t" + val.getKey() + " = " + val.serialize());
                        writer.newLine();
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[MarketBlocks] Failed to save config file " + file + ": " + e.getMessage());
        }
    }

    private static String stripInlineComment(String value) {
        boolean inQuotes = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '#' && !inQuotes) {
                return value.substring(0, i).trim();
            }
        }
        return value.trim();
    }
}
