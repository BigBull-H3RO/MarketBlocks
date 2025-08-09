package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Käuferansicht für den Small Shop mit automatischer Kauflogik
 */
public class SmallShopBuyerScreen extends SmallShopScreen {
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/button"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/button_hover"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/button_selected"));

    private static final ResourceLocation ICON_HOME = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/home");

    // Pfeil-Sprites für Trade-Status
    private static final ResourceLocation ARROW_ENABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/arrow_enabled");
    private static final ResourceLocation ARROW_DISABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/arrow_disabled");

    // Zusätzliche Anzeige-Elemente
    private static final ResourceLocation TRADE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/trade_background");

    public SmallShopBuyerScreen(SmallShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        // Nur Angebots-Tab für Käufer (deaktiviert, da nur ein Tab)
        addRenderableWidget(new IconButton(x + this.imageWidth + 4, y + 20, 20, 20, BUTTON_SPRITES, ICON_HOME, b -> {
            // Kein Action nötig, da Käufer nur Offer-Tab sieht
        }, Component.translatable("screen.marketblocks.small_shop.tab.offer"), () -> true));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // Render Trade-Bereich
        renderTradeArea(guiGraphics);
        renderTradeArrow(guiGraphics);
    }

    private void renderTradeArea(GuiGraphics guiGraphics) {
        if (!menu.hasActiveOffer()) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;

        // Hintergrund für Trade-Bereich (optional)
        int tradeX = x + 40;
        int tradeY = y + 45;
        int tradeWidth = 96;
        int tradeHeight = 26;

        // Leicht dunkler Hintergrund für Trade-Bereich
        guiGraphics.fill(tradeX, tradeY, tradeX + tradeWidth, tradeY + tradeHeight, 0x44000000);

        // Rahmen um Trade-Bereich
        guiGraphics.hLine(tradeX, tradeX + tradeWidth, tradeY, 0xFF666666);
        guiGraphics.hLine(tradeX, tradeX + tradeWidth, tradeY + tradeHeight, 0xFF666666);
        guiGraphics.vLine(tradeX, tradeY, tradeY + tradeHeight, 0xFF666666);
        guiGraphics.vLine(tradeX + tradeWidth, tradeY, tradeY + tradeHeight, 0xFF666666);
    }

    private void renderTradeArrow(GuiGraphics guiGraphics) {
        if (!menu.hasActiveOffer()) {
            return;
        }

        int x = this.leftPos;
        int y = this.topPos;
        int arrowX = x + 70; // Position zwischen Bezahl- und Kaufslots
        int arrowY = y + 52;

        // Bestimme Pfeil-Status basierend auf Verfügbarkeit und Bezahlung
        boolean hasStock = menu.hasStock();
        boolean hasValidPayment = hasValidPaymentInSlots();
        boolean canTrade = hasStock && hasValidPayment;

        ResourceLocation arrowTexture = canTrade ? ARROW_ENABLED : ARROW_DISABLED;

        // Render Pfeil
        guiGraphics.blitSprite(arrowTexture, arrowX, arrowY, 22, 15);

        // Zusätzliche Status-Indikatoren
        if (!hasStock) {
            // Kleines "X" über dem Pfeil bei fehlendem Stock
            guiGraphics.drawString(this.font, "✗", arrowX + 8, arrowY - 8, 0xFF0000);
        }
    }

    private boolean hasValidPaymentInSlots() {
        if (!menu.hasActiveOffer()) {
            return false;
        }

        // Prüfe Bezahlslot A
        if (!menu.getPayItemA().isEmpty()) {
            if (menu.getSlot(25).getItem().getCount() < menu.getPayItemA().getCount()) {
                return false;
            }
        }

        // Prüfe Bezahlslot B (falls erforderlich)
        if (!menu.getPayItemB().isEmpty()) {
            if (menu.getSlot(26).getItem().getCount() < menu.getPayItemB().getCount()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        renderTradeStatus(graphics, mouseX, mouseY);
        renderHoverTooltips(graphics, mouseX, mouseY);
    }

    private void renderTradeStatus(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.hasActiveOffer()) {
            // Zeige "Kein Angebot verfügbar" Message
            int x = this.leftPos + 45;
            int y = this.topPos + 30;
            Component noOffer = Component.translatable("message.marketblocks.shop.no_offer_available");
            graphics.drawCenteredString(this.font, noOffer, x + 43, y, 0x888888);
            return;
        }

        // Trade-Informationen anzeigen
        int x = this.leftPos + 8;
        int y = this.topPos + 75;

        if (!menu.hasStock()) {
            Component noStock = Component.translatable("message.marketblocks.small_shop.no_stock");
            graphics.drawString(this.font, noStock, x, y, 0xFF0000);
        } else if (hasValidPaymentInSlots()) {
            Component readyTrade = Component.translatable("message.marketblocks.shop.ready_to_trade");
            graphics.drawString(this.font, readyTrade, x, y, 0x00FF00);
        } else {
            Component needPayment = Component.translatable("message.marketblocks.shop.place_payment");
            graphics.drawString(this.font, needPayment, x, y, 0xFFAA00);
        }
    }

    private void renderHoverTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        // Tooltip für Pfeil-Bereich
        int arrowX = this.leftPos + 70;
        int arrowY = this.topPos + 52;

        if (mouseX >= arrowX && mouseX <= arrowX + 22 && mouseY >= arrowY && mouseY <= arrowY + 15) {
            if (!menu.hasActiveOffer()) {
                graphics.renderTooltip(this.font, Component.translatable("tooltip.marketblocks.shop.no_offer"), mouseX, mouseY);
            } else if (!menu.hasStock()) {
                graphics.renderTooltip(this.font, Component.translatable("tooltip.marketblocks.shop.no_stock"), mouseX, mouseY);
            } else if (!hasValidPaymentInSlots()) {
                graphics.renderTooltip(this.font, Component.translatable("tooltip.marketblocks.shop.insufficient_payment"), mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font, Component.translatable("tooltip.marketblocks.shop.trade_ready"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Render Titel
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        // Render Inventar-Label
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // Zusätzliche Labels für Käufer
        if (menu.hasActiveOffer()) {
            int x = 8;
            int y = 32;

            graphics.drawString(this.font, Component.translatable("label.marketblocks.shop.payment"), x + 20, y, 4210752, false);
            graphics.drawString(this.font, Component.translatable("label.marketblocks.shop.receive"), x + 100, y, 4210752, false);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Regelmäßige Updates für dynamische Käufer-Elemente
    }
}