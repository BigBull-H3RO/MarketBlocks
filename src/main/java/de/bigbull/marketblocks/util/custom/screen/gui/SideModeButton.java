package de.bigbull.marketblocks.util.custom.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class SideModeButton extends Button {
    private static final ResourceLocation BUTTON =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/button.png");
    private static final ResourceLocation HIGHLIGHTED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/button_highlighted.png");
    private static final ResourceLocation SELECTED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/button_selected.png");
    private static final ResourceLocation DISABLED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/button_disabled.png");
    private static final ResourceLocation INPUT_ICON =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/input_icon.png");
    private static final ResourceLocation OUTPUT_ICON =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/output_icon.png");

    private SideMode mode;
    private final Consumer<SideMode> callback;
    private boolean selected;

    public SideModeButton(int x, int y, int width, int height, SideMode initialMode, Consumer<SideMode> callback) {
        super(x, y, width, height, Component.empty(), b -> {}, DEFAULT_NARRATION);
        this.mode = initialMode;
        this.callback = callback;
        this.selected = initialMode != SideMode.DISABLED;
    }

    public void setMode(SideMode mode) {
        this.mode = mode;
        this.selected = mode != SideMode.DISABLED;
    }

    @Override
    public void onPress() {
        mode = mode.next();
        selected = mode != SideMode.DISABLED;
        callback.accept(mode);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation background;
        if (!this.active) {
            background = DISABLED;
        } else if (selected) {
            background = SELECTED;
        } else if (isHoveredOrFocused()) {
            background = HIGHLIGHTED;
        } else {
            background = BUTTON;
        }

        RenderSystem.setShaderTexture(0, background);
        graphics.blit(background, getX(), getY(), 0, 0, getWidth(), getHeight(), 16, 16);

        ResourceLocation icon = null;
        if (mode == SideMode.INPUT) {
            icon = INPUT_ICON;
        } else if (mode == SideMode.OUTPUT) {
            icon = OUTPUT_ICON;
        }

        if (icon != null) {
            RenderSystem.setShaderTexture(0, icon);
            int iconX = getX() + (getWidth() - 16) / 2;
            int iconY = getY() + (getHeight() - 16) / 2;
            graphics.blit(icon, iconX, iconY, 0, 0, 16, 16, 16, 16);
        }
    }
}