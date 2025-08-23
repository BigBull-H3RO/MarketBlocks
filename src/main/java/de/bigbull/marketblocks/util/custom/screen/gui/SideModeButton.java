package de.bigbull.marketblocks.util.custom.screen.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import net.minecraft.client.Minecraft;
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
    private static final ResourceLocation INPUT_ICON =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/input_icon.png");
    private static final ResourceLocation OUTPUT_ICON =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/output_icon.png");
    private static final ResourceLocation DISABLED_ICON =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/custom_1/disabled_icon.png");

    private SideMode mode;
    private final Consumer<SideMode> callback;
    private int pressTicks;
    private boolean isPressing;

    public SideModeButton(int x, int y, int width, int height, SideMode initialMode, Consumer<SideMode> callback) {
        super(x, y, width, height, Component.empty(), b -> {}, DEFAULT_NARRATION);
        this.mode = initialMode;
        this.callback = callback;
        this.pressTicks = 0;
        this.isPressing = false;
    }

    public void setMode(SideMode mode) {
        this.mode = mode;
        this.pressTicks = 0;
        this.isPressing = false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
            this.isPressing = true;
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isPressing && button == 0) {
            this.isPressing = false;
            if (this.isMouseOver(mouseX, mouseY)) {
                this.mode = this.mode.next();
                this.pressTicks = 10;
                this.callback.accept(this.mode);
            }
            return true;
        }
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation background;
        if (!this.active) {
            background = BUTTON;
        } else if (isPressing || pressTicks > 0) {
            background = SELECTED;
        } else if (isMouseOver(mouseX, mouseY)) {
            background = HIGHLIGHTED;
        } else {
            background = BUTTON;
        }

        if (!isPressing && pressTicks > 0) {
            pressTicks--;
        }

        RenderSystem.setShaderTexture(0, background);
        graphics.blit(background, getX(), getY(), 0, 0, getWidth(), getHeight(), 16, 16);

        ResourceLocation icon = switch (mode) {
            case DISABLED -> DISABLED_ICON;
            case INPUT -> INPUT_ICON;
            case OUTPUT -> OUTPUT_ICON;
        };

        RenderSystem.setShaderTexture(0, icon);
        int iconX = getX() + (getWidth() - 16) / 2;
        int iconY = getY() + (getHeight() - 16) / 2;
        graphics.blit(icon, iconX, iconY, 0, 0, 16, 16, 16, 16);
    }
}