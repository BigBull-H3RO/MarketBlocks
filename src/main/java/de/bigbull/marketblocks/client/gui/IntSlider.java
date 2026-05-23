package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class IntSlider extends AbstractSliderButton {
    private final int min;
    private final int max;
    private int currentValue;
    private final Consumer<Integer> onValueChanged;
    private final Component prefix;

    public IntSlider(int x, int y, int width, int height, Component prefix, int min, int max, int value, Consumer<Integer> onValueChanged) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.min = min;
        this.max = max;
        this.currentValue = Mth.clamp(value, min, max);
        this.value = (double)(this.currentValue - min) / (max - min);
        this.prefix = prefix;
        this.onValueChanged = onValueChanged;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.prefix.copy().append(": ").append(String.valueOf(this.currentValue)));
    }

    @Override
    protected void applyValue() {
        this.currentValue = (int) Math.round(this.min + this.value * (this.max - this.min));
        this.onValueChanged.accept(this.currentValue);
    }
}