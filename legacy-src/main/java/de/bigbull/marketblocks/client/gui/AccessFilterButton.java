package de.bigbull.marketblocks.client.gui;

import de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Vivid filter mode button for Whitelist / Blacklist toggle in access settings.
 */
public class AccessFilterButton extends AbstractButton {
    private static final int COLOR_WL_BG = 0xFF1B381B;
    private static final int COLOR_WL_BORDER = 0xFF2E6E2E;
    private static final int COLOR_WL_BG_HOVER = 0xFF254C25;
    private static final int COLOR_WL_BORDER_HOVER = 0xFF44AA44;
    private static final int COLOR_WL_TEXT = 0xFF55FF55;
    private static final int COLOR_WL_TEXT_HOVER = 0xFF88FF88;

    private static final int COLOR_BL_BG = 0xFF3D1818;
    private static final int COLOR_BL_BORDER = 0xFF732626;
    private static final int COLOR_BL_BG_HOVER = 0xFF542222;
    private static final int COLOR_BL_BORDER_HOVER = 0xFFAA3333;
    private static final int COLOR_BL_TEXT = 0xFFFF5555;
    private static final int COLOR_BL_TEXT_HOVER = 0xFFFFAAAA;

    private final Font font;
    private final OnPress onPress;
    private AccessMode accessMode;

    public interface OnPress {
        void onPress(AccessFilterButton button);
    }

    public AccessFilterButton(int x, int y, int width, int height, Font font, AccessMode initialMode, OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.accessMode = initialMode;
        this.onPress = onPress;
        updateTooltip();
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode mode) {
        this.accessMode = mode;
        updateTooltip();
    }

    public void updateTooltip() {
        setTooltip(Tooltip.create(Component.translatable(this.accessMode == AccessMode.WHITELIST
                ? "gui.marketblocks.access.filter_whitelist.tooltip"
                : "gui.marketblocks.access.filter_blacklist.tooltip")));
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

        boolean isWhitelist = this.accessMode == AccessMode.WHITELIST;
        int bg = isWhitelist
                ? (hovered ? COLOR_WL_BG_HOVER : COLOR_WL_BG)
                : (hovered ? COLOR_BL_BG_HOVER : COLOR_BL_BG);
        int border = isWhitelist
                ? (hovered ? COLOR_WL_BORDER_HOVER : COLOR_WL_BORDER)
                : (hovered ? COLOR_BL_BORDER_HOVER : COLOR_BL_BORDER);
        int textColor = isWhitelist
                ? (hovered ? COLOR_WL_TEXT_HOVER : COLOR_WL_TEXT)
                : (hovered ? COLOR_BL_TEXT_HOVER : COLOR_BL_TEXT);

        graphics.fill(x, y, x + w, y + h, border);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        Component label = Component.translatable(isWhitelist
                ? "gui.marketblocks.access.filter_whitelist"
                : "gui.marketblocks.access.filter_blacklist");
        int textW = this.font.width(label);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - this.font.lineHeight) / 2 + 1;
        graphics.drawString(this.font, label, textX, textY, textColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
