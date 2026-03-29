package de.bigbull.marketblocks.util.custom.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Button with vanilla background and centered icon overlay.
 */
public class VanillaIconButton extends Button {
    private static final int ICON_TEXTURE_SIZE = 18;
    private final ResourceLocation icon;
    private final int iconSize;
    private final int sourceU;
    private final int sourceV;
    private final int sourceWidth;
    private final int sourceHeight;
    private final int iconYOffset;

    public VanillaIconButton(int x, int y, int width, int height, ResourceLocation icon, int iconSize,
                             Button.OnPress onPress, Component tooltip) {
        this(x, y, width, height, icon, iconSize,
                0, 0, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE,
                0,
                onPress, tooltip);
    }

    public VanillaIconButton(int x, int y, int width, int height, ResourceLocation icon, int iconSize,
                             int sourceU, int sourceV, int sourceWidth, int sourceHeight,
                             Button.OnPress onPress, Component tooltip) {
        this(x, y, width, height, icon, iconSize, sourceU, sourceV, sourceWidth, sourceHeight, 0, onPress, tooltip);
    }

    public VanillaIconButton(int x, int y, int width, int height, ResourceLocation icon, int iconSize,
                             int sourceU, int sourceV, int sourceWidth, int sourceHeight, int iconYOffset,
                             Button.OnPress onPress, Component tooltip) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.iconSize = iconSize;
        this.sourceU = sourceU;
        this.sourceV = sourceV;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.iconYOffset = iconYOffset;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        RenderSystem.setShaderTexture(0, icon);
        int maxTargetSize = Math.max(1, Math.min(iconSize, Math.min(getWidth(), getHeight())));
        float widthScale = (float) maxTargetSize / (float) sourceWidth;
        float heightScale = (float) maxTargetSize / (float) sourceHeight;
        float scale = Math.min(widthScale, heightScale);
        int iconRenderWidth = Math.max(1, Math.round(sourceWidth * scale));
        int iconRenderHeight = Math.max(1, Math.round(sourceHeight * scale));
        int iconX = getX() + (getWidth() - iconRenderWidth) / 2;
        int iconY = getY() + (getHeight() - iconRenderHeight) / 2 + iconYOffset;
        graphics.blit(icon, iconX, iconY, iconRenderWidth, iconRenderHeight,
                (float) sourceU, (float) sourceV,
                sourceWidth, sourceHeight,
                ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
    }
}

