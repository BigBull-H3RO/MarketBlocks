package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Compact segmented button with distinct selected and unselected states.
 * Used for sub-tabs and mode switching in settings sections.
 */
public class SegmentButton extends AbstractButton {
    private static final int COLOR_SELECTED_BG = 0xFF3A3A3A;
    private static final int COLOR_SELECTED_BORDER = 0xFF1E1E1E;
    private static final int COLOR_SELECTED_TEXT = 0xFFFFFFFF;

    private static final int COLOR_NORMAL_BG = 0xFF6E6E6E;
    private static final int COLOR_NORMAL_BORDER = 0xFF373737;
    private static final int COLOR_NORMAL_TEXT = 0xFFD0D0D0;

    private static final int COLOR_HOVER_BG = 0xFF808080;
    private static final int COLOR_HOVER_BORDER = 0xFFFFFFFF;
    private static final int COLOR_HOVER_TEXT = 0xFFFFFFFF;

    private static final int COLOR_DISABLED_BG = 0xFF444444;
    private static final int COLOR_DISABLED_BORDER = 0xFF2A2A2A;
    private static final int COLOR_DISABLED_TEXT = 0xFF777777;

    private final Font font;
    private final OnPress onPress;
    private boolean selected;

    public interface OnPress {
        void onPress(SegmentButton button);
    }

    public SegmentButton(int x, int y, int width, int height, Component message, Font font, boolean selected, OnPress onPress) {
        super(x, y, width, height, message);
        this.font = font;
        this.selected = selected;
        this.onPress = onPress;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onPress() {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean hovered = isHoveredOrFocused();

        int bg;
        int border;
        int textColor;

        if (!this.active) {
            bg = COLOR_DISABLED_BG;
            border = COLOR_DISABLED_BORDER;
            textColor = COLOR_DISABLED_TEXT;
        } else if (this.selected) {
            bg = COLOR_SELECTED_BG;
            border = hovered ? 0xFF555555 : COLOR_SELECTED_BORDER;
            textColor = COLOR_SELECTED_TEXT;
        } else if (hovered) {
            bg = COLOR_HOVER_BG;
            border = COLOR_HOVER_BORDER;
            textColor = COLOR_HOVER_TEXT;
        } else {
            bg = COLOR_NORMAL_BG;
            border = COLOR_NORMAL_BORDER;
            textColor = COLOR_NORMAL_TEXT;
        }

        graphics.fill(x, y, x + w, y + h, border);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        Component msg = getMessage();
        if (msg != null && !msg.getString().isEmpty()) {
            int textW = this.font.width(msg);
            int textX = x + (w - textW) / 2;
            int textY = y + (h - this.font.lineHeight) / 2 + 1;
            graphics.drawString(this.font, msg, textX, textY, textColor, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
