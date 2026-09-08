package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Pixel-precise miniature arrow button for number spinners.
 */
public class MiniArrowButton extends AbstractWidget {
    private final boolean up;
    private final Runnable onPress;

    public MiniArrowButton(int x, int y, int width, int height, boolean up, Runnable onPress) {
        super(x, y, width, height, Component.empty());
        this.up = up;
        this.onPress = onPress;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active && visible) {
            onPress.run();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHoveredOrFocused();
        int borderColor = hovered ? 0xFFFFFFFF : 0xFF373737;
        int bgColor = hovered ? 0xFF8B8B8B : 0xFF555555;
        int arrowColor = !active ? 0xFFA0A0A0 : (hovered ? 0xFFFFFFFF : 0xFFE0E0E0);

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Border & Background
        graphics.fill(x, y, x + w, y + h, borderColor);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);

        // Centered arrow pixel icon
        int cx = x + (w / 2);
        int cy = y + (h / 2);

        if (up) {
            graphics.fill(cx, cy - 1, cx + 1, cy, arrowColor);
            graphics.fill(cx - 1, cy, cx + 2, cy + 1, arrowColor);
            graphics.fill(cx - 2, cy + 1, cx + 3, cy + 2, arrowColor);
        } else {
            graphics.fill(cx - 2, cy - 1, cx + 3, cy, arrowColor);
            graphics.fill(cx - 1, cy, cx + 2, cy + 1, arrowColor);
            graphics.fill(cx, cy + 1, cx + 1, cy + 2, arrowColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
