package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Modern toggle button displaying the admin shop mode state in a distinct
 * amethyst / purple command theme with custom pixel-art icon and toggle switch.
 */
public class AdminModeToggleButton extends AbstractButton {
    private static final int COLOR_ACTIVE_BG = 0xFF2E1840;
    private static final int COLOR_ACTIVE_BORDER = 0xFF8A38D0;
    private static final int COLOR_ACTIVE_BORDER_HOVER = 0xFFB050FF;
    private static final int COLOR_ACTIVE_ACCENT = 0xFFE0A0FF;

    private static final int COLOR_INACTIVE_BG = 0xFF242424;
    private static final int COLOR_INACTIVE_BORDER = 0xFF505050;
    private static final int COLOR_INACTIVE_BORDER_HOVER = 0xFF707070;
    private static final int COLOR_INACTIVE_TEXT = 0xFF909090;

    private final Font font;
    private final OnToggle onToggle;
    private boolean adminMode;

    public interface OnToggle {
        void onToggle(AdminModeToggleButton button, boolean adminMode);
    }

    public AdminModeToggleButton(int x, int y, int width, int height, Font font, boolean initialAdminMode,
            OnToggle onToggle) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.adminMode = initialAdminMode;
        this.onToggle = onToggle;
        updateTooltip();
    }

    public boolean isAdminMode() {
        return this.adminMode;
    }

    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
        updateTooltip();
    }

    public void updateTooltip() {
        setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.admin_shop.button.tooltip")));
    }

    @Override
    public void onPress() {
        this.adminMode = !this.adminMode;
        if (this.onToggle != null) {
            this.onToggle.onToggle(this, this.adminMode);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean hovered = isHoveredOrFocused();

        int border = this.adminMode
                ? (hovered ? COLOR_ACTIVE_BORDER_HOVER : COLOR_ACTIVE_BORDER)
                : (hovered ? COLOR_INACTIVE_BORDER_HOVER : COLOR_INACTIVE_BORDER);
        int bg = this.adminMode ? COLOR_ACTIVE_BG : COLOR_INACTIVE_BG;

        // Background and border
        graphics.fill(x, y, x + w, y + h, border);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        // Label (centered horizontally and vertically)
        Component label = Component.translatable(this.adminMode
                ? "gui.marketblocks.admin_shop.button.active"
                : "gui.marketblocks.admin_shop.button.inactive");
        int textColor = this.adminMode ? COLOR_ACTIVE_ACCENT : COLOR_INACTIVE_TEXT;
        int textX = x + (w - font.width(label)) / 2;
        int textY = y + (h - font.lineHeight) / 2;
        graphics.drawString(font, label, textX, textY, textColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
