package de.bigbull.marketblocks.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

/**
 * A simple button widget that uses custom background sprites and overlays a separate icon.
 * Supports different sprite states for normal, hovered, and selected modes.
 */
public class IconButton extends Button {
    private final WidgetSprites sprites;
    private final ResourceLocation icon;
    private final ResourceLocation activeIcon;
    private final BooleanSupplier selectedSupplier;

    public IconButton(int x, int y, int width, int height, WidgetSprites sprites, ResourceLocation icon, Button.OnPress onPress,
                      Component tooltip, BooleanSupplier selectedSupplier) {
        this(x, y, width, height, sprites, icon, null, onPress, tooltip, selectedSupplier);
    }

    public IconButton(int x, int y, int width, int height, WidgetSprites sprites, ResourceLocation icon, ResourceLocation activeIcon,
                      Button.OnPress onPress, Component tooltip, BooleanSupplier selectedSupplier) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.sprites = sprites;
        this.icon = icon;
        this.activeIcon = activeIcon;
        this.selectedSupplier = selectedSupplier;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = selectedSupplier != null && selectedSupplier.getAsBoolean();
        ResourceLocation background;
        if (!this.active) {
            background = sprites.get(false, false);
        } else if (selected) {
            background = sprites.get(false, true);
        } else if (isHoveredOrFocused()) {
            background = sprites.get(true, true);
        } else {
            background = sprites.get(true, false);
        }

        graphics.blitSprite(background, getX(), getY(), getWidth(), getHeight());
        ResourceLocation iconToRender = (selected && activeIcon != null) ? activeIcon : icon;
        RenderSystem.setShaderTexture(0, iconToRender);
        int iconSize = Math.min(18, Math.min(getWidth(), getHeight()));
        int iconX = getX() + (getWidth() - iconSize) / 2;
        int iconY = getY() + (getHeight() - iconSize) / 2;
        graphics.blit(iconToRender, iconX, iconY, 0, 0, iconSize, iconSize, 18, 18);
    }
}
