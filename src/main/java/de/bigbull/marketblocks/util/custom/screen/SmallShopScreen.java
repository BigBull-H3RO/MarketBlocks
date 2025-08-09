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

        // Render Villager-style offer display nur im Offer Tab
        if (menu.getActiveTab() == 0) {
            renderOfferDisplay(guiGraphics);
        }
    }

    private void renderOfferDisplay(GuiGraphics guiGraphics) {
        ItemStack sale = menu.getSaleItem();
        ItemStack payA = menu.getPayItemA();
        ItemStack payB = menu.getPayItemB();

        // Nur anzeigen wenn ein Angebot existiert
        if (sale.isEmpty() || payA.isEmpty()) {
            return;
        }

        // Villager-style trade display
        int baseX = this.leftPos + 20;
        int baseY = this.topPos + 20;

        // Render background für das Angebot (optional)
        guiGraphics.fill(baseX - 2, baseY - 2, baseX + 138, baseY + 22, 0x88000000);

        // Erstes Payment Item
        guiGraphics.renderItem(payA, baseX, baseY);
        guiGraphics.renderItemDecorations(this.font, payA, baseX, baseY);

        // Plus Zeichen
        guiGraphics.drawString(this.font, "+", baseX + 25, baseY + 6, 0xFFFFFF);

        // Zweites Payment Item (falls vorhanden)
        int nextX = baseX + 35;
        if (!payB.isEmpty()) {
            guiGraphics.renderItem(payB, nextX, baseY);
            guiGraphics.renderItemDecorations(this.font, payB, nextX, baseY);
            nextX += 25;
            guiGraphics.drawString(this.font, "+", nextX, baseY + 6, 0xFFFFFF);
            nextX += 15;
        }

        // Pfeil
        guiGraphics.drawString(this.font, "->", nextX, baseY + 6, 0xFFFFFF);
        nextX += 25;

        // Sale Item
        guiGraphics.renderItem(sale, nextX, baseY);
        guiGraphics.renderItemDecorations(this.font, sale, nextX, baseY);

        // Stock-Anzeige
        if (menu.isOwnerView()) {
            String stockText = getStockText(sale);
            guiGraphics.drawString(this.font, stockText, nextX + 20, baseY + 6, 0xFFFFFF);
        }
    }

    private String getStockText(ItemStack sale) {
        // Diese Methode würde idealerweise den aktuellen Stock aus der BlockEntity abrufen
        return "Stock: ?"; // Placeholder - kann durch tatsächliche Stock-Berechnung ersetzt werden
    }

    private void renderOfferOverlay(GuiGraphics guiGraphics) {
        // Render Items in den Offer-Slots falls sie leer sind aber ein Angebot existiert
        ItemStack sale = menu.getSaleItem();
        if (!sale.isEmpty() && !menu.getSlot(24).hasItem()) {
            int centeredX = (176 - 3 * 18) / 2;
            guiGraphics.renderItem(sale, this.leftPos + centeredX + 59, this.topPos + 52);
            guiGraphics.renderItemDecorations(this.font, sale, this.leftPos + centeredX + 59, this.topPos + 52);
        }
        ItemStack payA = menu.getPayItemA();
        if (!payA.isEmpty() && !menu.getSlot(25).hasItem()) {
            int centeredX = (176 - 3 * 18) / 2;
            guiGraphics.renderItem(payA, this.leftPos + centeredX - 25, this.topPos + 52);
            guiGraphics.renderItemDecorations(this.font, payA, this.leftPos + centeredX - 25, this.topPos + 52);
        }
        ItemStack payB = menu.getPayItemB();
        if (!payB.isEmpty() && !menu.getSlot(26).hasItem()) {
            int centeredX = (176 - 3 * 18) / 2;
            guiGraphics.renderItem(payB, this.leftPos + centeredX + 1, this.topPos + 52);
            guiGraphics.renderItemDecorations(this.font, payB, this.leftPos + centeredX + 1, this.topPos + 52);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}