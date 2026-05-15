#!/bin/bash
# 1. Config
sed -i 's/VISUAL_NPC_RENDER_VIEW_DISTANCE;/VISUAL_NPC_RENDER_VIEW_DISTANCE;\n    public static final ModConfigSpec.BooleanValue ENABLE_GLOBAL_OFFER_ITEM_RENDERING;/g' src/main/java/de/bigbull/marketblocks/core/config/Config.java
sed -i 's/COMMON_BUILDER.pop();/VISUAL_NPC_RENDER_VIEW_DISTANCE = COMMON_BUILDER\n                .comment("Maximum distance in blocks for rendering visual shop NPCs.")\n                .defineInRange("visualNpcRenderViewDistance", 128, 16, 512);\n        ENABLE_GLOBAL_OFFER_ITEM_RENDERING = COMMON_BUILDER\n                .comment("Global master switch to enable\/disable offer item rendering for all shops. Disable to save performance.")\n                .define("enableGlobalOfferItemRendering", true);\n        COMMON_BUILDER.pop();/' src/main/java/de/bigbull/marketblocks/core/config/Config.java

# 2. Sliders
cat << 'INNER_EOF' > src/main/java/de/bigbull/marketblocks/client/gui/FloatSlider.java
package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class FloatSlider extends AbstractSliderButton {
    private final float min;
    private final float max;
    private final Consumer<Float> onValueChanged;
    private final String prefix;

    public FloatSlider(int x, int y, int width, int height, Component prefix, float min, float max, float currentValue, Consumer<Float> onValueChanged) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.prefix = prefix.getString() + ": ";
        this.min = min;
        this.max = max;
        this.onValueChanged = onValueChanged;
        float clampedVal = Mth.clamp(currentValue, min, max);
        this.value = (clampedVal - min) / (max - min);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        float val = min + (float) value * (max - min);
        this.setMessage(Component.literal(prefix + String.format("%.2f", val)));
    }

    @Override
    protected void applyValue() {
        float val = min + (float) value * (max - min);
        onValueChanged.accept(val);
    }
}
INNER_EOF

cat << 'INNER_EOF' > src/main/java/de/bigbull/marketblocks/client/gui/IntSlider.java
package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class IntSlider extends AbstractSliderButton {
    private final int min;
    private final int max;
    private final Consumer<Integer> onValueChanged;
    private final String prefix;

    public IntSlider(int x, int y, int width, int height, Component prefix, int min, int max, int currentValue, Consumer<Integer> onValueChanged) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.prefix = prefix.getString() + ": ";
        this.min = min;
        this.max = max;
        this.onValueChanged = onValueChanged;
        int clampedVal = Mth.clamp(currentValue, min, max);
        this.value = (double) (clampedVal - min) / (max - min);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        int val = min + (int) Math.round(value * (max - min));
        this.setMessage(Component.literal(prefix + val));
    }

    @Override
    protected void applyValue() {
        int val = min + (int) Math.round(value * (max - min));
        onValueChanged.accept(val);
    }
}
INNER_EOF
