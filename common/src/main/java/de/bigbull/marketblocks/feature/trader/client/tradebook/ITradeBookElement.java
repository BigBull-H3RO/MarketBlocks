package de.bigbull.marketblocks.feature.trader.client.tradebook;

import net.minecraft.client.gui.GuiGraphics;

public interface ITradeBookElement {

    /**
     * Checks if this element is responsible for handling the given insertion string.
     * @param insertion The insertion string from the text component.
     * @return true if this element can handle it.
     */
    boolean canHandle(String insertion);

    /**
     * Calculates the extra height required by this element.
     * @param insertion The insertion string.
     * @return The extra height in pixels.
     */
    int getExtraHeight(String insertion);

    /**
     * Checks if this element is rendered inline (at the character's exact X position)
     * or as a block (at the start of the line).
     * @return true if inline, false if block.
     */
    default boolean isInline() {
        return false;
    }

    /**
     * Renders this element.
     * @param graphics The GuiGraphics instance.
     * @param insertion The insertion string containing the data.
     * @param x The scaled X coordinate where rendering should start.
     * @param y The scaled Y coordinate where rendering should start.
     * @param mouseX The unscaled mouse X coordinate.
     * @param mouseY The unscaled mouse Y coordinate.
     * @param scale The current scale factor of the page.
     * @param context The render context providing access to font, offers, tooltips, etc.
     */
    void render(GuiGraphics graphics, String insertion, int x, int y, int mouseX, int mouseY, float scale, TradeBookRenderContext context);
}
