package de.bigbull.marketblocks.util.custom.screen.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * A custom button widget that displays a background sprite and a separate icon texture.
 * It supports being in a "selected" state, controlled by a BooleanSupplier.
 */
public class IconButton extends Button {
    private static final int ICON_SIZE = 18;

    private final WidgetSprites sprites;
    private final ResourceLocation icon;
    private final BooleanSupplier selectedSupplier;

    /**
     * Constructs a new IconButton.
     * @param x The x-position of the button.
     * @param y The y-position of the button.
     * @param width The width of the button.
     * @param height The height of the button.
     * @param sprites The sprites for the button's background states.
     * @param icon The resource location of the icon to render on top.
     * @param onPress The action to perform when the button is pressed.
     * @param tooltip The tooltip to display on hover.
     * @param selectedSupplier A supplier to determine if the button is in a "selected" state.
     */
    public IconButton(int x, int y, int width, int height, @NotNull WidgetSprites sprites, @NotNull ResourceLocation icon, @NotNull Button.OnPress onPress,
                      @NotNull Component tooltip, @NotNull BooleanSupplier selectedSupplier) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.sprites = sprites;
        this.icon = icon;
        this.selectedSupplier = selectedSupplier;
        this.setTooltip(Tooltip.create(tooltip));
    }

    /**
     * Renders the button, choosing the correct background sprite based on its current state
     * (active, hovered, selected) and then renders the icon on top.
     */
    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        final boolean isSelected = this.selectedSupplier.getAsBoolean();
        final ResourceLocation background = this.sprites.get(this.active, isSelected || this.isHovered());

        graphics.blit(background, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());

        final int iconX = this.getX() + (this.getWidth() - ICON_SIZE) / 2;
        final int iconY = this.getY() + (this.getHeight() - ICON_SIZE) / 2;
        graphics.blit(this.icon, iconX, iconY, 0, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}