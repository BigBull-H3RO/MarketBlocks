package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Button with vanilla background and centered icon overlay.
 */
public class VanillaIconButton extends Button {
    private static final int ICON_TEXTURE_SIZE = 18;
    private final Identifier icon;
    private final int iconSize;
    private final int sourceU;
    private final int sourceV;
    private final int sourceWidth;
    private final int sourceHeight;
    private final int iconYOffset;

    public VanillaIconButton(int x, int y, int width, int height, Identifier icon, int iconSize,
                             Button.OnPress onPress, Component tooltip) {
        this(x, y, width, height, icon, iconSize,
                0, 0, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE,
                0,
                onPress, tooltip);
    }

    public VanillaIconButton(int x, int y, int width, int height, Identifier icon, int iconSize,
                             int sourceU, int sourceV, int sourceWidth, int sourceHeight,
                             Button.OnPress onPress, Component tooltip) {
        this(x, y, width, height, icon, iconSize, sourceU, sourceV, sourceWidth, sourceHeight, 0, onPress, tooltip);
    }

    public VanillaIconButton(int x, int y, int width, int height, Identifier icon, int iconSize,
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
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));

        int maxTargetSize = Math.max(1, Math.min(iconSize, Math.min(getWidth(), getHeight())));
        float widthScale = (float) maxTargetSize / (float) sourceWidth;
        float heightScale = (float) maxTargetSize / (float) sourceHeight;
        float scale = Math.min(widthScale, heightScale);
        int iconRenderWidth = Math.max(1, Math.round(sourceWidth * scale));
        int iconRenderHeight = Math.max(1, Math.round(sourceHeight * scale));
        int iconX = getX() + (getWidth() - iconRenderWidth) / 2;
        int iconY = getY() + (getHeight() - iconRenderHeight) / 2 + iconYOffset;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY,
                (float) sourceU, (float) sourceV,
                sourceWidth, sourceHeight,
                iconRenderWidth, iconRenderHeight,
                ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
    }
}
