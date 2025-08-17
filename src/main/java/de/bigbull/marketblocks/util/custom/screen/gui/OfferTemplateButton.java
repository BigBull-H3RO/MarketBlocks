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
        super(x, y, 88, 20, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    public void update(ItemStack payment1, ItemStack payment2, ItemStack result, boolean arrowActive) {
        this.payment1 = payment1;
        this.payment2 = payment2;
        this.result = result;
        this.arrowActive = arrowActive;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        int itemX = getX() + 2;
        int itemY = getY() + 1;

        // Render Payment 1
        if (!payment1.isEmpty()) {
            graphics.renderItem(payment1, itemX + 2, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment1, itemX + 2, itemY);
        }

        // Render Payment 2
        if (!payment2.isEmpty()) {
            graphics.renderItem(payment2, itemX + 26, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment2, itemX + 26, itemY);
        }

        // Render Pfeil
        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, getX() + 52, getY() + 5, 0, 0, 10, 9, 10, 9);

        // Render Result
        if (!result.isEmpty()) {
            graphics.renderItem(result, getX() + 70, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, result, getX() + 70, itemY);
        }
    }
}