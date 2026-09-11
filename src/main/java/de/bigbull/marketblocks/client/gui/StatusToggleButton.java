package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Modern toggle button displaying shop open/pause state with custom pixel-art
 * play and pause icons and distinct vivid state colors.
 */
public class StatusToggleButton extends AbstractButton {
    private static final int COLOR_ACTIVE_BG = 0xFF223822;
    private static final int COLOR_ACTIVE_BORDER = 0xFF388038;
    private static final int COLOR_ACTIVE_BORDER_HOVER = 0xFF60E060;
    private static final int COLOR_ACTIVE_ICON = 0xFF40E040;
    private static final int COLOR_ACTIVE_TEXT = 0xFF50FF50;

    private static final int COLOR_PAUSED_BG = 0xFF442816;
    private static final int COLOR_PAUSED_BORDER = 0xFFD07020;
    private static final int COLOR_PAUSED_BORDER_HOVER = 0xFFFFB030;
    private static final int COLOR_PAUSED_ICON = 0xFFFFA030;
    private static final int COLOR_PAUSED_TEXT = 0xFFFFB840;

    private final Font font;
    private final OnStatusChange onStatusChange;
    private boolean paused;

    public interface OnStatusChange {
        void onStatusChange(StatusToggleButton button, boolean paused);
    }

    public StatusToggleButton(int x, int y, int width, int height, Font font, boolean initialPaused,
            OnStatusChange onStatusChange) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.paused = initialPaused;
        this.onStatusChange = onStatusChange;
        updateTooltip();
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        updateTooltip();
    }

    public void updateTooltip() {
        setTooltip(Tooltip.create(Component.translatable(this.paused
                ? "gui.marketblocks.general.status.paused.tooltip"
                : "gui.marketblocks.general.status.active.tooltip")));
    }

    @Override
    public void onPress() {
        this.paused = !this.paused;
        updateTooltip();
        if (this.onStatusChange != null) {
            this.onStatusChange.onStatusChange(this, this.paused);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean hovered = isHoveredOrFocused();

        if (!this.paused) {
            // --- ACTIVE / OPEN STATE ---
            int border = hovered ? COLOR_ACTIVE_BORDER_HOVER : COLOR_ACTIVE_BORDER;
            graphics.fill(x, y, x + w, y + h, border);
            graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_ACTIVE_BG);

            // Centered 4x7 Pixel Play Triangle
            int iconX = x + (w - 4) / 2;
            int iconY = y + (h - 7) / 2;
            graphics.fill(iconX, iconY, iconX + 1, iconY + 7, COLOR_ACTIVE_ICON);
            graphics.fill(iconX + 1, iconY + 1, iconX + 2, iconY + 6, COLOR_ACTIVE_ICON);
            graphics.fill(iconX + 2, iconY + 2, iconX + 3, iconY + 5, COLOR_ACTIVE_ICON);
            graphics.fill(iconX + 3, iconY + 3, iconX + 4, iconY + 4, COLOR_ACTIVE_ICON);
        } else {
            // --- PAUSED STATE ---
            int border = hovered ? COLOR_PAUSED_BORDER_HOVER : COLOR_PAUSED_BORDER;
            graphics.fill(x, y, x + w, y + h, border);
            graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_PAUSED_BG);

            // Centered 5x7 Pixel Pause Bars
            int iconX = x + (w - 5) / 2;
            int iconY = y + (h - 7) / 2;
            graphics.fill(iconX, iconY, iconX + 2, iconY + 7, COLOR_PAUSED_ICON);
            graphics.fill(iconX + 3, iconY, iconX + 5, iconY + 7, COLOR_PAUSED_ICON);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
