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
 * A simple button widget that uses custom background sprites and overlays a
 * separate icon.
 * Supports different sprite states for normal, hovered, and selected modes.
 */
public class IconButton extends Button {
    private final WidgetSprites sprites;
    private final ResourceLocation icon;
    private final ResourceLocation activeIcon;
    private final BooleanSupplier selectedSupplier;
    private final Component tooltipMessage;
    private boolean flipBackgroundHorizontal = false;

    private int customBgWidth = -1;
    private int customBgHeight = -1;
    private int customBgOffsetX = 0;
    private int customBgOffsetY = 0;
    private int customSelectedBgOffsetX = 0;

    private int iconOffsetX = 0;
    private int iconOffsetY = 0;
    private int selectedIconOffsetX = 0;
    private int selectedIconOffsetY = 0;

    public IconButton(int x, int y, int width, int height, WidgetSprites sprites, ResourceLocation icon,
            Button.OnPress onPress,
            Component tooltip, BooleanSupplier selectedSupplier) {
        this(x, y, width, height, sprites, icon, null, onPress, tooltip, selectedSupplier);
    }

    public IconButton(int x, int y, int width, int height, WidgetSprites sprites, ResourceLocation icon,
            ResourceLocation activeIcon,
            Button.OnPress onPress, Component tooltip, BooleanSupplier selectedSupplier) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.sprites = sprites;
        this.icon = icon;
        this.activeIcon = activeIcon;
        this.selectedSupplier = selectedSupplier;
        this.tooltipMessage = tooltip;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
    }

    public Component getTooltipMessage() {
        return tooltipMessage;
    }

    public IconButton withFlippedBackground() {
        this.flipBackgroundHorizontal = true;
        return this;
    }

    public IconButton withCustomBackground(int width, int height, int unselectedOffsetX, int unselectedOffsetY,
            int selectedOffsetX) {
        this.customBgWidth = width;
        this.customBgHeight = height;
        this.customBgOffsetX = unselectedOffsetX;
        this.customBgOffsetY = unselectedOffsetY;
        this.customSelectedBgOffsetX = selectedOffsetX;
        return this;
    }

    public IconButton withSelectedIconOffset(int x, int y) {
        this.selectedIconOffsetX = x;
        this.selectedIconOffsetY = y;
        return this;
    }

    public boolean isSelected() {
        return selectedSupplier != null && selectedSupplier.getAsBoolean();
    }

    public IconButton withIconOffset(int x, int y) {
        this.iconOffsetX = x;
        this.iconOffsetY = y;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = isSelected();
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

        int renderWidth = customBgWidth > 0 ? customBgWidth : getWidth();
        int renderHeight = customBgHeight > 0 ? customBgHeight : getHeight();
        int offX = selected ? customSelectedBgOffsetX : customBgOffsetX;
        int offY = customBgOffsetY;

        if (this.flipBackgroundHorizontal) {
            graphics.pose().pushPose();
            graphics.pose().translate(getX() + getWidth() / 2.0F, 0, 0);
            graphics.pose().scale(-1.0F, 1.0F, 1.0F);
            graphics.pose().translate(-(getX() + getWidth() / 2.0F), 0, 0);

            RenderSystem.disableCull();
            graphics.blitSprite(background, getX() + offX, getY() + offY, renderWidth, renderHeight);
            RenderSystem.enableCull();

            graphics.pose().popPose();
        } else {
            graphics.blitSprite(background, getX() + offX, getY() + offY, renderWidth, renderHeight);
        }

        ResourceLocation iconToRender = (selected && activeIcon != null) ? activeIcon : icon;
        RenderSystem.setShaderTexture(0, iconToRender);
        int iconSize = Math.min(18, Math.min(getWidth(), getHeight()));
        int currentIconOffsetX = selected ? selectedIconOffsetX : iconOffsetX;
        int currentIconOffsetY = selected ? selectedIconOffsetY : iconOffsetY;
        int iconX = getX() + (getWidth() - iconSize) / 2 + currentIconOffsetX;
        int iconY = getY() + (getHeight() - iconSize) / 2 + currentIconOffsetY;

        graphics.blit(iconToRender, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }
}
