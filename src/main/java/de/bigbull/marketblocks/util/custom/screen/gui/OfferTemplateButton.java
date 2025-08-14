package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Button zum Anzeigen eines bestehenden Angebots.
 */
public class OfferTemplateButton extends Button {
    private static final ResourceLocation TRADE_ARROW =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    private ItemStack payment1 = ItemStack.EMPTY;
    private ItemStack payment2 = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private boolean arrowActive;
    private final Consumer<OfferTemplateButton> onPress;

    public OfferTemplateButton(int x, int y, Consumer<OfferTemplateButton> onPress) {
        super(x, y, 86, 20, Component.empty(), b -> {}, DEFAULT_NARRATION);
        this.onPress = onPress;
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
        int itemY = getY() + 2;

        if (!payment1.isEmpty()) {
            graphics.renderItem(payment1, itemX, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment1, itemX, itemY);
        }

        if (!payment2.isEmpty()) {
            graphics.renderItem(payment2, itemX + 18, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, payment2, itemX + 18, itemY);
        }

        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, getX() + 46, getY() + 3, 0, 0, 12, 8, 24, 16);

        if (!result.isEmpty()) {
            graphics.renderItem(result, getX() + 66, itemY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, result, getX() + 66, itemY);
        }
    }

    public void onPress() {
        if (this.active && this.visible) {
            this.onPress.accept(this);
        }
    }
}