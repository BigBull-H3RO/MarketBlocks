package de.bigbull.marketblocks.util.custom.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

/**
 * Einfacher Button mit Hintergrund-Sprites und separatem Icon.
 */
public class IconButton extends Button {
    private final WidgetSprites sprites;
    private final ResourceLocation icon;
    private final BooleanSupplier selectedSupplier;

    public IconButton(int x, int y, int width, int height, WidgetSprites sprites, ResourceLocation icon, Button.OnPress onPress,
                      Component tooltip, BooleanSupplier selectedSupplier) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.sprites = sprites;
        this.icon = icon;
        this.selectedSupplier = selectedSupplier;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = selectedSupplier != null && selectedSupplier.getAsBoolean();
        ResourceLocation background = sprites.get(this.active, isHoveredOrFocused() || selected);
        RenderSystem.setShaderTexture(0, background);
        graphics.blit(background, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        RenderSystem.setShaderTexture(0, icon);
        int iconX = getX() + (getWidth() - 16) / 2;
        int iconY = getY() + (getHeight() - 16) / 2;
        graphics.blit(icon, iconX, iconY, 0, 0, 0, 16, 16, 16, 16);
    }
}