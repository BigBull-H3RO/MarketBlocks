package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * Compact numeric text box with gold border and dark interior.
 */
public class CompactNumberBox extends EditBox {
    private static final int BORDER_FOCUSED = 0xFFFFFF77;
    private static final int BORDER_NORMAL = 0xFFFFDD33;
    private static final int BORDER_DISABLED = 0xFF353535;
    private static final int BG_COLOR = 0xFF000000;
    private static final int BG_COLOR_DISABLED = 0xFF181818;
    private static final int TEXT_COLOR = 0xFFFFFF55;
    private static final int TEXT_COLOR_DISABLED = 0xFF707070;

    public CompactNumberBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.setBordered(false);
        this.setTextColor(TEXT_COLOR);
        this.setTextColorUneditable(TEXT_COLOR_DISABLED);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.isVisible()) {
            return;
        }

        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;

        // 1. Border & Background
        int borderColor;
        int bgColor;
        if (!this.isActive()) {
            borderColor = BORDER_DISABLED;
            bgColor = BG_COLOR_DISABLED;
            this.setTextColor(TEXT_COLOR_DISABLED);
        } else {
            borderColor = this.isFocused() ? BORDER_FOCUSED : BORDER_NORMAL;
            bgColor = BG_COLOR;
            this.setTextColor(TEXT_COLOR);
        }
        graphics.fill(x, y, x + w, y + h, borderColor);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);

        // 3. Fallback to default if blurred and empty
        if (!this.isFocused() && this.getValue().isEmpty()) {
            this.setValue("1");
        }

        // 4. Render text & blinking cursor with 4px left padding and vertical centering (vanilla EditBox formula: (h - 8) / 2)
        int textYOffset = (h - 8) / 2;
        int textXOffset = 4;
        graphics.pose().pushPose();
        graphics.pose().translate(textXOffset, textYOffset, 0);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
    }
}
