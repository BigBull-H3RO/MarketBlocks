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
