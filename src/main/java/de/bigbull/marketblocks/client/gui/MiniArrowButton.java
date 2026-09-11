package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

/**
 * Pixel-precise miniature arrow stepper button using vanilla Minecraft button sprites.
 * Supports dual stepper mode (up and down in a single widget with 1px divider)
 * or legacy single button mode.
 */
public class MiniArrowButton extends AbstractWidget {
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/button"),
            ResourceLocation.withDefaultNamespace("widget/button_disabled"),
            ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
    private static final int ARROW_COLOR = 0xFFFFFFFF;
    private static final int ARROW_COLOR_DISABLED = 0xFFA0A0A0;
    private static final int ARROW_SHADOW = 0xFF181818;

    private final boolean stepperMode;
    private final boolean up;
    private final Runnable onPress;
    private final Runnable onUp;
    private final Runnable onDown;

    /**
     * Dual stepper constructor (up and down combined in a single widget).
     */
    public MiniArrowButton(int x, int y, int width, int height, Runnable onUp, Runnable onDown) {
        super(x, y, width, height, Component.empty());
        this.stepperMode = true;
        this.up = false;
        this.onPress = null;
        this.onUp = onUp;
        this.onDown = onDown;
    }

    /**
     * Single arrow button constructor (legacy).
     */
    public MiniArrowButton(int x, int y, int width, int height, boolean up, Runnable onPress) {
        super(x, y, width, height, Component.empty());
        this.stepperMode = false;
        this.up = up;
        this.onPress = onPress;
        this.onUp = null;
        this.onDown = null;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active && this.visible) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (this.stepperMode) {
                if (mouseY < getY() + (getHeight() / 2.0)) {
                    if (this.onUp != null) {
                        this.onUp.run();
                    }
                } else {
                    if (this.onDown != null) {
                        this.onDown.run();
                    }
                }
            } else if (this.onPress != null) {
                this.onPress.run();
            }
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        if (this.stepperMode) {
            renderStepper(graphics, x, y, w, h, mouseX, mouseY);
        } else {
            renderSingle(graphics, x, y, w, h);
        }
    }

    private void renderStepper(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean mouseInWidget = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        double midY = y + (h / 2.0);
        boolean upHovered = this.active && mouseInWidget && mouseY < midY;
        boolean downHovered = this.active && mouseInWidget && mouseY >= midY;

        int arrow = !this.active ? ARROW_COLOR_DISABLED : ARROW_COLOR;

        // Each button is 8px tall and they overlap by 1px at the shared black border (no gap)
        int btnH = (h + 1) / 2;
        int btn2Y = y + h - btnH;

        ResourceLocation upSprite = BUTTON_SPRITES.get(this.active, upHovered);
        ResourceLocation downSprite = BUTTON_SPRITES.get(this.active, downHovered);

        // Draw non-hovered button first so the hovered button's highlight renders cleanly on top
        if (downHovered) {
            graphics.blitSprite(upSprite, x, y, w, btnH);
            graphics.blitSprite(downSprite, x, btn2Y, w, btnH);
        } else {
            graphics.blitSprite(downSprite, x, btn2Y, w, btnH);
            graphics.blitSprite(upSprite, x, y, w, btnH);
        }

        // Arrows
        int cx = x + (w / 2);
        int cy1 = y + 3;
        // Up Arrow
        graphics.fill(cx - 1, cy1 - 1, cx + 1, cy1, arrow);
        graphics.fill(cx - 2, cy1, cx + 2, cy1 + 1, arrow);
        graphics.fill(cx - 3, cy1 + 1, cx + 3, cy1 + 2, arrow);
        graphics.fill(cx - 3, cy1 + 2, cx + 3, cy1 + 3, ARROW_SHADOW);

        // Down Arrow
        int cy2 = btn2Y + 4;
        graphics.fill(cx - 3, cy2 - 1, cx + 3, cy2, arrow);
        graphics.fill(cx - 2, cy2, cx + 2, cy2 + 1, arrow);
        graphics.fill(cx - 1, cy2 + 1, cx + 1, cy2 + 2, arrow);
        graphics.fill(cx - 1, cy2 + 2, cx + 1, cy2 + 3, ARROW_SHADOW);
    }

    private void renderSingle(GuiGraphics graphics, int x, int y, int w, int h) {
        boolean hovered = this.active && this.isHovered();
        ResourceLocation sprite = BUTTON_SPRITES.get(this.active, hovered);
        graphics.blitSprite(sprite, x, y, w, h);

        int arrow = !this.active ? ARROW_COLOR_DISABLED : ARROW_COLOR;
        int cx = x + (w / 2);
        int cy = y + (h / 2);
        if (this.up) {
            graphics.fill(cx - 1, cy - 2, cx + 1, cy - 1, arrow);
            graphics.fill(cx - 2, cy - 1, cx + 2, cy, arrow);
            graphics.fill(cx - 3, cy, cx + 3, cy + 1, arrow);
            graphics.fill(cx - 3, cy + 1, cx + 3, cy + 2, ARROW_SHADOW);
        } else {
            graphics.fill(cx - 3, cy - 1, cx + 3, cy, arrow);
            graphics.fill(cx - 2, cy, cx + 2, cy + 1, arrow);
            graphics.fill(cx - 1, cy + 1, cx + 1, cy + 2, arrow);
            graphics.fill(cx - 1, cy + 2, cx + 1, cy + 3, ARROW_SHADOW);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, createNarrationMessage());
    }
}
