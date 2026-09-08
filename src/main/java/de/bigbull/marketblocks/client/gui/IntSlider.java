package de.bigbull.marketblocks.client.gui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An integer slider widget using the sleek modular CustomSlider design.
 */
public class IntSlider extends CustomSlider {
    public IntSlider(int x, int y, int width, int height, @Nullable Component prefix, int min, int max, int value,
                     @Nullable Consumer<Integer> onValueChanged) {
        super(x, y, width, height, prefix, min, max, value, val -> {
            if (onValueChanged != null) {
                onValueChanged.accept(Math.round(val));
            }
        });
        setStep(1.0f);
        setStringFormatter(val -> String.valueOf(Math.round(val)));
    }
}
