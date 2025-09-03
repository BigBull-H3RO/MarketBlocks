package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A custom button that cycles through {@link SideMode} states when clicked.
 * It provides visual feedback by staying in a "pressed" state for a short duration.
 */
public class SideModeButton extends Button {
    private static final ResourceLocation BUTTON_NORMAL = MarketBlocks.id("textures/gui/button/custom_1/button.png");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = MarketBlocks.id("textures/gui/button/custom_1/button_highlighted.png");
    private static final ResourceLocation BUTTON_SELECTED = MarketBlocks.id("textures/gui/button/custom_1/button_selected.png");

    private static final ResourceLocation ICON_INPUT = MarketBlocks.id("textures/gui/button/custom_1/input_icon.png");
    private static final ResourceLocation ICON_OUTPUT = MarketBlocks.id("textures/gui/button/custom_1/output_icon.png");
    private static final ResourceLocation ICON_DISABLED = MarketBlocks.id("textures/gui/button/custom_1/disabled_icon.png");

    private static final int ICON_SIZE = 16;
    private static final int PRESS_TICKS_DURATION = 10;

    private SideMode mode;
    private final Consumer<SideMode> callback;
    private int pressTicks;

    /**
     * Constructs a new SideModeButton.
     * @param x The x-position of the button.
     * @param y The y-position of the button.
     * @param width The width of the button.
     * @param height The height of the button.
     * @param initialMode The starting {@link SideMode}.
     * @param callback A consumer that is called with the new mode when the button is clicked.
     */
    public SideModeButton(int x, int y, int width, int height, @NotNull SideMode initialMode, @NotNull Consumer<SideMode> callback) {
        super(x, y, width, height, Component.empty(), b -> {}, DEFAULT_NARRATION);
        this.mode = initialMode;
        this.callback = callback;
        this.pressTicks = 0;
    }

    /**
     * Sets the current mode of the button without triggering the callback.
     * Used to reset the button's state from the screen.
     */
    public void setMode(@NotNull SideMode mode) {
        this.mode = mode;
        this.pressTicks = 0;
    }

    /**
     * Called when the button is clicked. Cycles to the next {@link SideMode},
     * triggers the visual feedback, and calls the callback.
     */
    @Override
    public void onPress() {
        this.mode = this.mode.next();
        this.pressTicks = PRESS_TICKS_DURATION;
        this.callback.accept(this.mode);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation background;
        if (!this.active) {
            background = BUTTON_NORMAL;
        } else if (this.pressTicks > 0) {
            background = BUTTON_SELECTED;
        } else if (this.isHoveredOrFocused()) {
            background = BUTTON_HIGHLIGHTED;
        } else {
            background = BUTTON_NORMAL;
        }

        if (this.pressTicks > 0) {
            this.pressTicks--;
        }

        graphics.blit(background, getX(), getY(), 0, 0, getWidth(), getHeight(), ICON_SIZE, ICON_SIZE);

        ResourceLocation icon = switch (mode) {
            case DISABLED -> ICON_DISABLED;
            case INPUT -> ICON_INPUT;
            case OUTPUT -> ICON_OUTPUT;
        };

        int iconX = getX() + (getWidth() - ICON_SIZE) / 2;
        int iconY = getY() + (getHeight() - ICON_SIZE) / 2;
        graphics.blit(icon, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}