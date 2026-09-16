package de.bigbull.marketblocks.core.config.toml;

import java.util.List;
import java.util.function.Supplier;

public abstract class TomlConfigValue<T> implements Supplier<T> {
    private final String key;
    private final String category;
    private final T defaultValue;
    private final List<String> comments;
    protected T value;

    public TomlConfigValue(String key, String category, T defaultValue, List<String> comments) {
        this.key = key;
        this.category = category;
        this.defaultValue = defaultValue;
        this.comments = comments;
        this.value = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getCategory() {
        return category;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public List<String> getComments() {
        return comments;
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public abstract String serialize();

    public abstract void deserialize(String rawValue);

    public static class BooleanValue extends TomlConfigValue<Boolean> {
        public BooleanValue(String key, String category, Boolean defaultValue, List<String> comments) {
            super(key, category, defaultValue, comments);
        }

        @Override
        public String serialize() {
            return String.valueOf(value);
        }

        @Override
        public void deserialize(String raw) {
            if (raw != null) {
                value = Boolean.parseBoolean(raw.trim());
            }
        }
    }

    public static class IntValue extends TomlConfigValue<Integer> {
        private final int min;
        private final int max;

        public IntValue(String key, String category, Integer defaultValue, int min, int max, List<String> comments) {
            super(key, category, defaultValue, comments);
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String serialize() {
            return String.valueOf(value);
        }

        @Override
        public void deserialize(String raw) {
            if (raw != null) {
                try {
                    int parsed = Integer.parseInt(raw.trim());
                    value = Math.max(min, Math.min(max, parsed));
                } catch (NumberFormatException ignored) {
                    value = getDefaultValue();
                }
            }
        }
    }

    public static class DoubleValue extends TomlConfigValue<Double> {
        private final double min;
        private final double max;

        public DoubleValue(String key, String category, Double defaultValue, double min, double max, List<String> comments) {
            super(key, category, defaultValue, comments);
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        @Override
        public String serialize() {
            return String.valueOf(value);
        }

        @Override
        public void deserialize(String raw) {
            if (raw != null) {
                try {
                    double parsed = Double.parseDouble(raw.trim());
                    value = Math.max(min, Math.min(max, parsed));
                } catch (NumberFormatException ignored) {
                    value = getDefaultValue();
                }
            }
        }
    }

    public static class StringValue extends TomlConfigValue<String> {
        public StringValue(String key, String category, String defaultValue, List<String> comments) {
            super(key, category, defaultValue, comments);
        }

        @Override
        public String serialize() {
            return "\"" + (value != null ? value.replace("\"", "\\\"") : "") + "\"";
        }

        @Override
        public void deserialize(String raw) {
            if (raw != null) {
                String trimmed = raw.trim();
                if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                    value = trimmed.substring(1, trimmed.length() - 1).replace("\\\"", "\"");
                } else {
                    value = trimmed;
                }
            }
        }
    }

    public static class EnumValue<E extends Enum<E>> extends TomlConfigValue<E> {
        private final Class<E> enumClass;

        @SuppressWarnings("unchecked")
        public EnumValue(String key, String category, E defaultValue, List<String> comments) {
            super(key, category, defaultValue, comments);
            this.enumClass = (Class<E>) defaultValue.getClass();
        }

        @Override
        public String serialize() {
            return "\"" + value.name() + "\"";
        }

        @Override
        public void deserialize(String raw) {
            if (raw != null) {
                String cleaned = raw.trim();
                if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
                    cleaned = cleaned.substring(1, cleaned.length() - 1);
                }
                for (E constant : enumClass.getEnumConstants()) {
                    if (constant.name().equalsIgnoreCase(cleaned)) {
                        value = constant;
                        return;
                    }
                }
                value = getDefaultValue();
            }
        }
    }
}
