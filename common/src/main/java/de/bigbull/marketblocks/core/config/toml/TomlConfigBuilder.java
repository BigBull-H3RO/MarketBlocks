package de.bigbull.marketblocks.core.config.toml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class TomlConfigBuilder {
    private final Deque<String> categoryStack = new ArrayDeque<>();
    private final List<TomlConfigValue<?>> values = new ArrayList<>();
    private final List<String> currentComments = new ArrayList<>();

    public TomlConfigBuilder push(String category) {
        categoryStack.push(category);
        return this;
    }

    public TomlConfigBuilder pop() {
        if (!categoryStack.isEmpty()) {
            categoryStack.pop();
        }
        return this;
    }

    public TomlConfigBuilder comment(String... lines) {
        if (lines != null) {
            for (String line : lines) {
                if (line != null) {
                    currentComments.add(line);
                }
            }
        }
        return this;
    }

    private String getCurrentCategory() {
        if (categoryStack.isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>(categoryStack);
        Collections.reverse(list);
        return String.join(".", list);
    }

    private List<String> pollComments() {
        List<String> result = new ArrayList<>(currentComments);
        currentComments.clear();
        return result;
    }

    public TomlConfigValue.BooleanValue define(String key, boolean defaultValue) {
        TomlConfigValue.BooleanValue val = new TomlConfigValue.BooleanValue(key, getCurrentCategory(), defaultValue, pollComments());
        values.add(val);
        return val;
    }

    public TomlConfigValue.StringValue define(String key, String defaultValue) {
        TomlConfigValue.StringValue val = new TomlConfigValue.StringValue(key, getCurrentCategory(), defaultValue, pollComments());
        values.add(val);
        return val;
    }

    public TomlConfigValue.IntValue defineInRange(String key, int defaultValue, int min, int max) {
        TomlConfigValue.IntValue val = new TomlConfigValue.IntValue(key, getCurrentCategory(), defaultValue, min, max, pollComments());
        values.add(val);
        return val;
    }

    public TomlConfigValue.DoubleValue defineInRange(String key, double defaultValue, double min, double max) {
        TomlConfigValue.DoubleValue val = new TomlConfigValue.DoubleValue(key, getCurrentCategory(), defaultValue, min, max, pollComments());
        values.add(val);
        return val;
    }

    public <E extends Enum<E>> TomlConfigValue.EnumValue<E> defineEnum(String key, E defaultValue) {
        TomlConfigValue.EnumValue<E> val = new TomlConfigValue.EnumValue<>(key, getCurrentCategory(), defaultValue, pollComments());
        values.add(val);
        return val;
    }

    public TomlConfigSpec build() {
        return new TomlConfigSpec(values);
    }
}
