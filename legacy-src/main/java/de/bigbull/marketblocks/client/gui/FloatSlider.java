package de.bigbull.marketblocks.client.gui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A floating-point slider widget using the sleek modular CustomSlider design.
 */
public class FloatSlider extends CustomSlider {
    public FloatSlider(int x, int y, int width, int height, @Nullable Component prefix,
                       float min, float max, float value, @Nullable Consumer<Float> onValueChanged) {
        super(x, y, width, height, prefix, min, max, value, onValueChanged);
    }
}
