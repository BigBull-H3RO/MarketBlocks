package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class OfferTemplateButton extends Button {
    private static final ResourceLocation TRADE_ARROW =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    private ItemStack payment1 = ItemStack.EMPTY;
    private ItemStack payment2 = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private boolean arrowActive;

    public OfferTemplateButton(int x, int y, OnPress onPress) {
        super(x, y, 86, 20, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    public void update(ItemStack payment1, ItemStack payment2, ItemStack result, boolean arrowActive) {
        this.payment1 = payment1;
        this.payment2 = payment2;
        this.result = result;
        this.arrowActive = arrowActive;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        // Immer einen sichtbaren Hintergrund rendern
        int backgroundColor;
        if (!this.active) {
            backgroundColor = 0x40000000; // Dunkelgrau für inaktiv
        } else if (this.isHoveredOrFocused()) {
            backgroundColor = 0x80FFFFFF; // Hell für Hover
        } else {
            backgroundColor = 0x60000000; // Mittelgrau für normal
        }

        // Render Hintergrund
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);

        // Render Rahmen
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + 1, 0xFF000000); // Top
        graphics.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), 0xFF000000); // Bottom
        graphics.fill(getX(), getY(), getX() + 1, getY() + getHeight(), 0xFF000000); // Left
        graphics.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), 0xFF000000); // Right

        int itemX = getX() + 2;
        int itemY = getY() + 2;

        // Render Payment 1
        if (!payment1.isEmpty()) {
            graphics.renderItem(payment1, itemX, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment1, itemX, itemY);
        }

        // Render Payment 2
        if (!payment2.isEmpty()) {
            graphics.renderItem(payment2, itemX + 18, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment2, itemX + 18, itemY);
        }

        // Render Pfeil
        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, getX() + 40, getY() + 2, 0, 0, 12, 8, 24, 16);

        // Render Result
        if (!result.isEmpty()) {
            graphics.renderItem(result, getX() + 66, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, result, getX() + 66, itemY);
        }
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }
}