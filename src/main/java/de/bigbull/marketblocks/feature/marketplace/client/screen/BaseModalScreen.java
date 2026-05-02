package de.bigbull.marketblocks.feature.marketplace.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Common base class for modal dialogs rendered over an existing screen.
 * Suppresses background blur and prevents hover/tooltip interactions from the parent screen.
 */
public abstract class BaseModalScreen extends Screen {
    private static final int EDGE_PADDING = 8;
    private static final int BACKGROUND_MOUSE_OFFSCREEN = -10000;

    /** Vertical distance in pixels between consecutive input-field rows. */
    protected static final int ROW_SPACING = 24;
    /** Standard width of a numeric/text input field. */
    protected static final int INPUT_WIDTH = 66;
    /** Standard height of a numeric/text input field. */
    protected static final int INPUT_HEIGHT = 18;
    /** Horizontal offset from the panel's left edge to the row labels. */
    protected static final int LABEL_X_OFFSET = 14;
    /** Vertical offset from the panel's top edge to the first row label. */
    protected static final int LABEL_START_Y_OFFSET = 35;
    /** Vertical offset from the panel's top edge to the first input field. */
    protected static final int INPUT_START_Y_OFFSET = 30;

    protected final Screen parent;
    protected final int panelWidth;
    protected final int panelHeight;
    private final int preferredLeft;
    private final int preferredCenterY;

    protected int panelLeft;
    protected int panelTop;

    protected BaseModalScreen(Component title, Screen parent, int panelWidth, int panelHeight, int preferredLeft, int preferredCenterY) {
        super(title);
        this.parent = parent;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.preferredLeft = preferredLeft;
        this.preferredCenterY = preferredCenterY;
    }

    protected final void initModalBounds() {
        if (this.preferredLeft >= 0 && this.preferredCenterY >= 0) {
            int preferredTop = this.preferredCenterY - (this.panelHeight / 2);
            this.panelLeft = Math.min(Math.max(EDGE_PADDING, this.preferredLeft), this.width - this.panelWidth - EDGE_PADDING);
            this.panelTop = Math.min(Math.max(EDGE_PADDING, preferredTop), this.height - this.panelHeight - EDGE_PADDING);
        } else {
            this.panelLeft = (this.width - this.panelWidth) / 2;
            this.panelTop = (this.height - this.panelHeight) / 2;
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderParentBackdrop(guiGraphics, partialTick);
        renderDimLayer(guiGraphics);
        renderModalPanel(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderDimLayer(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // Ensure the dim layer is above all parent content regardless of previous depth state.
        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        guiGraphics.pose().popPose();
    }

    private void renderModalPanel(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 500.0F);

        renderPanelBackground(guiGraphics);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        renderPanelForeground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
    }

    private void renderParentBackdrop(GuiGraphics guiGraphics, float partialTick) {
        if (this.parent instanceof MarketplaceScreen marketplaceScreen) {
            marketplaceScreen.renderModalBackdrop(guiGraphics, partialTick);
        } else if (this.parent != null) {
            this.parent.render(guiGraphics, BACKGROUND_MOUSE_OFFSCREEN, BACKGROUND_MOUSE_OFFSCREEN, partialTick);
        } else {
            this.renderTransparentBackground(guiGraphics);
        }
    }

    protected void renderPanelBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight, 0xFF151515);
        guiGraphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + 20, 0xFF2B2B2B);
        guiGraphics.renderOutline(this.panelLeft, this.panelTop, this.panelWidth, this.panelHeight, 0xFF555555);
        guiGraphics.drawString(this.font, this.title, this.panelLeft + (this.panelWidth - this.font.width(this.title)) / 2, this.panelTop + 6, 0xFFFFFF, false);
    }

    protected abstract void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    /**
     * Displays a short action-bar message on the local client player.
     *
     * @param message the message component to show above the hotbar
     */
    protected void notifyClient(Component message) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(message, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}



