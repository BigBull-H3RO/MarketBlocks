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
