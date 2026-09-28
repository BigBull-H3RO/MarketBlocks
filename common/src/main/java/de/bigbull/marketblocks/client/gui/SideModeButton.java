package de.bigbull.marketblocks.client.gui;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * A multi-state button used in the I/O configuration UI.
 * Toggles between Disabled, Input, and Output states for a specific block face.
 */
public class SideModeButton extends Button {
    private static final ResourceLocation BUTTON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/button");
    private static final ResourceLocation HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/button_highlighted");
    private static final ResourceLocation SELECTED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/button_selected");
    public static final ResourceLocation INPUT_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/input_icon");
    public static final ResourceLocation OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/output_icon");
    public static final ResourceLocation DISABLED_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/disabled_icon");

    private final Component sideName;
    private SideMode mode;
    private final Consumer<SideMode> onModeChanged;
    private boolean isPressing = false;
    private int pressTicks = 0;

    public SideModeButton(int x, int y, int width, int height, Component sideName, SideMode initialMode, Consumer<SideMode> onModeChanged) {
        super(x, y, width, height, Component.empty(), b -> {
        }, DEFAULT_NARRATION);
        this.sideName = sideName;
        this.mode = initialMode;
        this.onModeChanged = onModeChanged;
        updateTooltip();
    }

    public SideModeButton(int x, int y, int width, int height, SideMode initialMode, Consumer<SideMode> onModeChanged) {
        this(x, y, width, height, Component.empty(), initialMode, onModeChanged);
    }

    public SideMode getMode() {
        return mode;
    }

    public void setMode(SideMode mode) {
        this.mode = mode;
        this.pressTicks = 0;
        this.isPressing = false;
        updateTooltip();
    }

    public void updateTooltip() {
        if (!this.active) {
            setTooltip(null);
            return;
        }
        if (sideName != null && !sideName.getString().isEmpty()) {
            setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    Component.empty().append(sideName).append(": ").append(mode.getDisplayName())));
        }
    }

    @Override
    public void onPress() {
        // Not used, handled in mouseClicked
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Handled in mouseClicked
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            isPressing = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            isPressing = true;
            pressTicks = 4;

            if (button == 0) { // Left click -> Next mode
                mode = mode.next();
                updateTooltip();
                if (onModeChanged != null) {
                    onModeChanged.accept(mode);
                }
                return true;
            } else if (button == 1) { // Right click -> Previous mode
                mode = mode.previous();
                updateTooltip();
                if (onModeChanged != null) {
                    onModeChanged.accept(mode);
                }
                return true;
            }
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

        graphics.blitSprite(RenderType::guiTextured, background, getX(), getY(), getWidth(), getHeight());

        ResourceLocation icon = switch (mode) {
            case DISABLED -> DISABLED_ICON;
            case INPUT -> INPUT_ICON;
            case OUTPUT -> OUTPUT_ICON;
        };

        int iconX = getX() + (getWidth() - 16) / 2;
        int iconY = getY() + (getHeight() - 16) / 2;
        graphics.blitSprite(RenderType::guiTextured, icon, iconX, iconY, 16, 16);
    }
}
