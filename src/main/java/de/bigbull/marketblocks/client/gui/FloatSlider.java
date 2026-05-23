package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class FloatSlider extends AbstractSliderButton {
    private final float min;
    private final float max;
    private float currentValue;
    private final Consumer<Float> onValueChanged;
    private final Component prefix;

    public FloatSlider(int x, int y, int width, int height, Component prefix, float min, float max, float value, Consumer<Float> onValueChanged) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.min = min;
        this.max = max;
        this.currentValue = Mth.clamp(value, min, max);
        this.value = (this.currentValue - min) / (max - min);
        this.prefix = prefix;
        this.onValueChanged = onValueChanged;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.prefix.copy().append(": ").append(String.format(java.util.Locale.US, "%.2f", this.currentValue)));
    }

    @Override
    protected void applyValue() {
        this.currentValue = this.min + (float) this.value * (this.max - this.min);
        this.onValueChanged.accept(this.currentValue);
    }
}