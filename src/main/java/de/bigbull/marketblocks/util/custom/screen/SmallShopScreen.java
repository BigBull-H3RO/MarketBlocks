package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SmallShopScreen extends AbstractContainerScreen<SmallShopMenu> {
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop.png");

    public SmallShopScreen(SmallShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
    }

    /**
     * Kann von Unterklassen überschrieben werden, um dynamische Hintergründe zu
     * verwenden.
     */
    protected ResourceLocation getBackgroundTexture() {
        return DEFAULT_TEXTURE;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(getBackgroundTexture(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        renderOfferOverlay(guiGraphics);
    }

    private void renderOfferOverlay(GuiGraphics guiGraphics) {
        ItemStack sale = menu.getSaleItem();
        if (!sale.isEmpty() && !menu.getSlot(24).hasItem()) {
            guiGraphics.renderItem(sale, this.leftPos + 170, this.topPos + 54);
            guiGraphics.renderItemDecorations(this.font, sale, this.leftPos + 170, this.topPos + 54);
        }
        ItemStack payA = menu.getPayItemA();
        if (!payA.isEmpty() && !menu.getSlot(25).hasItem()) {
            guiGraphics.renderItem(payA, this.leftPos + 116, this.topPos + 54);
            guiGraphics.renderItemDecorations(this.font, payA, this.leftPos + 116, this.topPos + 54);
        }
        ItemStack payB = menu.getPayItemB();
        if (!payB.isEmpty() && !menu.getSlot(26).hasItem()) {
            guiGraphics.renderItem(payB, this.leftPos + 134, this.topPos + 54);
            guiGraphics.renderItemDecorations(this.font, payB, this.leftPos + 134, this.topPos + 54);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}