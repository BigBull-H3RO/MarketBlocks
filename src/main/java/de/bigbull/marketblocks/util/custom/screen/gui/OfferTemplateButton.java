package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A custom widget that displays a trade offer, including payment items, a result item, and a trade arrow.
 * It functions as a button but its primary role is to visually represent the trade.
 */
public class OfferTemplateButton extends Button {
    private static final ResourceLocation TRADE_ARROW = MarketBlocks.id("textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED = MarketBlocks.id("textures/gui/icon/trade_arrow_disabled.png");

    private static final int WIDTH = 88;
    private static final int HEIGHT = 20;

    private static final int PAYMENT1_X = 4;
    private static final int PAYMENT2_X = 28;
    private static final int ARROW_X = 52;
    private static final int RESULT_X = 70;

    private static final int ITEM_Y = 2;
    private static final int ARROW_Y = 6;

    private ItemStack payment1 = ItemStack.EMPTY;
    private ItemStack payment2 = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private boolean arrowActive;

    public OfferTemplateButton(int x, int y, @NotNull OnPress onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    /**
     * Updates the items and state displayed by this button.
     * This should be called each frame before rendering.
     */
    public void update(@NotNull ItemStack payment1, @NotNull ItemStack payment2, @NotNull ItemStack result, boolean arrowActive) {
        this.payment1 = payment1;
        this.payment2 = payment2;
        this.result = result;
        this.arrowActive = arrowActive;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        final Font font = Minecraft.getInstance().font;
        final int x = getX();
        final int y = getY();

        // Render payment items
        renderItem(graphics, font, this.payment1, x + PAYMENT1_X, y + ITEM_Y);
        renderItem(graphics, font, this.payment2, x + PAYMENT2_X, y + ITEM_Y);

        // Render trade arrow
        ResourceLocation arrowTexture = this.arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, x + ARROW_X, y + ARROW_Y, 0, 0, 10, 9, 10, 9);

        // Render result item
        renderItem(graphics, font, this.result, x + RESULT_X, y + ITEM_Y);
    }

    private void renderItem(@NotNull GuiGraphics graphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        }
    }
}