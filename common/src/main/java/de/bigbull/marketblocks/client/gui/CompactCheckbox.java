package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Modern compact checkbox widget with dark bezel frame and vivid green
 * checkmark.
 */
public class CompactCheckbox extends AbstractButton {
    private static final int DEFAULT_BOX_SIZE = 12;

    // Checkbox colors
    private static final int COLOR_BORDER_NORMAL = 0xFF555555;
    private static final int COLOR_BORDER_HOVERED = 0xFFFFFFFF;
    private static final int COLOR_BORDER_DISABLED = 0xFF303030;
    private static final int COLOR_BG = 0xFF282828;
    private static final int COLOR_BG_DISABLED = 0xFF1C1C1C;
    private static final int COLOR_GREEN_BRIGHT = 0xFF3FE33F;
    private static final int COLOR_GREEN_DARK = 0xFF1F8B1F;
    private static final int COLOR_GREEN_DISABLED_BRIGHT = 0xFF1E6E1E;
    private static final int COLOR_GREEN_DISABLED_DARK = 0xFF103A10;
    private static final int COLOR_LABEL = 0x404040;
    private static final int COLOR_LABEL_DISABLED = 0x808080;

    private boolean selected;
    private final OnValueChange onValueChange;
    private final Font font;
    private final int boxSize;
    private boolean textOnLeft = false;
    private boolean compactText = false;
    private boolean boxOnRightEdge = false;

    public interface OnValueChange {
        void onValueChange(CompactCheckbox checkbox, boolean value);
    }

    public CompactCheckbox(int x, int y, Component message, Font font, boolean initialSelected,
            OnValueChange onValueChange) {
        this(x, y, message, font, initialSelected, DEFAULT_BOX_SIZE, onValueChange);
    }

    public CompactCheckbox(int x, int y, Component message, Font font, boolean initialSelected, int boxSize,
            OnValueChange onValueChange) {
        super(x, y, boxSize + 4 + font.width(message), Math.max(boxSize, font.lineHeight), message);
        this.font = font;
        this.selected = initialSelected;
        this.boxSize = boxSize;
        this.onValueChange = onValueChange;
    }

    public CompactCheckbox setTextOnLeft(boolean textOnLeft) {
        this.textOnLeft = textOnLeft;
        recalculateWidth();
        return this;
    }

    public CompactCheckbox setCompactText(boolean compactText) {
        this.compactText = compactText;
        recalculateWidth();
        return this;
    }

    public CompactCheckbox setBoxOnRightEdge(boolean boxOnRightEdge) {
        this.boxOnRightEdge = boxOnRightEdge;
        return this;
    }

    public CompactCheckbox setCustomWidth(int width) {
        this.width = width;
        return this;
    }

    private void recalculateWidth() {
        if (this.boxOnRightEdge) {
            return;
        }
        Component msg = getEffectiveMessage();
        int textWidth = msg != null ? this.font.width(msg) : 0;
        this.width = (textWidth > 0) ? (textWidth + 4 + this.boxSize) : this.boxSize;
    }

    private Component getEffectiveMessage() {
        Component msg = getMessage();
        if (msg == null || msg.getString().isEmpty()) {
            return Component.empty();
        }
        if (this.compactText) {
            return GuiConstants.compact(Component.literal(msg.getString().toUpperCase(java.util.Locale.ROOT)));
        }
        return msg;
    }

    public int getBoxX() {
        if (this.boxOnRightEdge) {
            return getX() + getWidth() - this.boxSize;
        } else if (this.textOnLeft) {
            Component msg = getEffectiveMessage();
            int textWidth = (msg != null && !msg.getString().isEmpty()) ? this.font.width(msg) : 0;
            return getX() + textWidth + 4;
        } else {
            return getX();
        }
    }

    public int getBoxY() {
        return getY() + (getHeight() - this.boxSize) / 2;
    }

    public boolean isBoxHovered(double mouseX, double mouseY) {
        if (!this.active || !this.visible) {
            return false;
        }
        int bx = getBoxX();
        int by = getBoxY();
        return mouseX >= bx && mouseX < bx + this.boxSize
                && mouseY >= by && mouseY < by + this.boxSize;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isBoxHovered(mouseX, mouseY);
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        if (this.onValueChange != null) {
            this.onValueChange.onValueChange(this, this.selected);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Component msg = getEffectiveMessage();
        int bx = getBoxX();
        int by = getBoxY();
        int textX = (this.textOnLeft || this.boxOnRightEdge) ? getX() : bx + this.boxSize + 4;
        boolean boxHovered = isBoxHovered(mouseX, mouseY);

        // 1. Outer border & background (dimmed when disabled)
        int borderColor;
        int bgColor;
        if (!this.active) {
            borderColor = COLOR_BORDER_DISABLED;
            bgColor = COLOR_BG_DISABLED;
        } else {
            borderColor = boxHovered ? COLOR_BORDER_HOVERED : COLOR_BORDER_NORMAL;
            bgColor = COLOR_BG;
        }
        graphics.fill(bx, by, bx + this.boxSize, by + this.boxSize, borderColor);
        graphics.fill(bx + 1, by + 1, bx + this.boxSize - 1, by + this.boxSize - 1, bgColor);

        // 2. Checkmark when selected
        if (this.selected) {
            renderCheckmark(graphics, bx, by, this.active);
        }

        // 3. Label text
        if (msg != null && !msg.getString().isEmpty()) {
            int textY = this.compactText
                    ? by + 2
                    : getY() + (getHeight() - this.font.lineHeight) / 2 + 1;
            int textColor = this.active ? COLOR_LABEL : COLOR_LABEL_DISABLED;
            graphics.drawString(this.font, msg, textX, textY, textColor, false);
        }
    }

    private void renderCheckmark(GuiGraphics graphics, int bx, int by, boolean active) {
        int brightColor = active ? COLOR_GREEN_BRIGHT : COLOR_GREEN_DISABLED_BRIGHT;
        int shadowColor = active ? COLOR_GREEN_DARK : COLOR_GREEN_DISABLED_DARK;

        // Bright green main stroke
        int[][] bright = {
                { 2, 5 }, { 2, 6 },
                { 3, 6 }, { 3, 7 },
                { 4, 7 }, { 4, 8 },
                { 5, 6 }, { 5, 7 },
                { 6, 5 }, { 6, 6 },
                { 7, 4 }, { 7, 5 },
                { 8, 3 }, { 8, 4 },
                { 9, 2 }, { 9, 3 }
        };
        for (int[] p : bright) {
            graphics.fill(bx + p[0], by + p[1], bx + p[0] + 1, by + p[1] + 1, brightColor);
        }

        // Dark green shadow stroke underneath
        int[][] shadow = {
                { 2, 7 },
                { 3, 8 },
                { 4, 9 },
                { 5, 8 },
                { 6, 7 },
                { 7, 6 },
                { 8, 5 },
                { 9, 4 }
        };
        for (int[] p : shadow) {
            graphics.fill(bx + p[0], by + p[1], bx + p[0] + 1, by + p[1] + 1, shadowColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narration.add(NarratedElementType.USAGE,
                        Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narration.add(NarratedElementType.USAGE,
                        Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }
}
