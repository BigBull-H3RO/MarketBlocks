package de.bigbull.marketblocks.client.gui;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/**
 * A multi-state button used in the I/O configuration UI.
 * Toggles between Disabled, Input, and Output states for a specific block face.
 */
public class SideModeButton extends Button {
    private static final Identifier BUTTON = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/button");
    private static final Identifier HIGHLIGHTED = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/button_highlighted");
    private static final Identifier SELECTED = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/button_selected");
    public static final Identifier INPUT_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/input_icon");
    public static final Identifier OUTPUT_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "sidemode/output_icon");
    public static final Identifier DISABLED_ICON = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
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
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 || event.button() == 1) {
            isPressing = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!this.active || !this.visible) {
            return false;
        }

        if (this.isMouseOver(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            isPressing = true;
            pressTicks = 4;

            if (event.button() == 0) { // Left click -> Next mode
                mode = mode.next();
                updateTooltip();
                if (onModeChanged != null) {
                    onModeChanged.accept(mode);
                }
                return true;
            } else if (event.button() == 1) { // Right click -> Previous mode
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
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Identifier background;
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

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, background, getX(), getY(), getWidth(), getHeight());

        Identifier icon = switch (mode) {
            case DISABLED -> DISABLED_ICON;
            case INPUT -> INPUT_ICON;
            case OUTPUT -> OUTPUT_ICON;
        };

        int iconX = getX() + (getWidth() - 16) / 2;
        int iconY = getY() + (getHeight() - 16) / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 16, 16);
    }
}
