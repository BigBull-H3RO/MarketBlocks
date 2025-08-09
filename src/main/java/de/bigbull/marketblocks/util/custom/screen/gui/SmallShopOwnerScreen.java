package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Besitzeransicht für den Small Shop mit Tab-System und vollständiger Funktionalität
 */
public class SmallShopOwnerScreen extends SmallShopScreen {
    private static final ResourceLocation TEXTURE_OFFER = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop.png");
    private static final ResourceLocation TEXTURE_STORAGE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/shop_storage.png");

    // Button-Sprites
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/button"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/button_highlighted"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/button_selected"));

    // Icons
    private static final ResourceLocation ICON_CONFIRM = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/confirm");
    private static final ResourceLocation ICON_CANCEL = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/cancel");
    private static final ResourceLocation ICON_HOME = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/home");
    private static final ResourceLocation ICON_INVENTORY = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/inventory");

    // Pfeil-Sprites für Trade-Visualisierung
    private static final ResourceLocation ARROW_ENABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/arrow_enabled");
    private static final ResourceLocation ARROW_DISABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/arrow_disabled");

    private IconButton saveButton;
    private IconButton removeButton;
    private IconButton offerTabButton;
    private IconButton storageTabButton;

    public SmallShopOwnerScreen(SmallShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        int rightX = x + this.imageWidth - 24;

        // Haupt-Buttons (nur im Offer-Tab sichtbar)
        saveButton = addRenderableWidget(new IconButton(rightX, y + 4, 20, 20, BUTTON_SPRITES, ICON_CONFIRM, b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_CONFIRM);
        }, Component.translatable("screen.marketblocks.small_shop.save"), () -> false));

        removeButton = addRenderableWidget(new IconButton(rightX, y + 28, 20, 20, BUTTON_SPRITES, ICON_CANCEL, b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_REMOVE);
        }, Component.translatable("screen.marketblocks.small_shop.remove"), () -> false));

        // Tab-Buttons
        offerTabButton = addRenderableWidget(new IconButton(x + this.imageWidth + 4, y + 20, 20, 20, BUTTON_SPRITES, ICON_HOME, b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_TAB_OFFER);
            updateButtonVisibility();
        }, Component.translatable("screen.marketblocks.small_shop.tab.offer"), () -> menu.getActiveTab() == 0));

        storageTabButton = addRenderableWidget(new IconButton(x + this.imageWidth + 4, y + 44, 20, 20, BUTTON_SPRITES, ICON_INVENTORY, b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_TAB_STORAGE);
            updateButtonVisibility();
        }, Component.translatable("screen.marketblocks.small_shop.tab.storage"), () -> menu.getActiveTab() == 1));

        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        boolean offerTab = menu.getActiveTab() == 0;
        saveButton.visible = offerTab && !menu.hasActiveOffer();
        removeButton.visible = offerTab && menu.hasActiveOffer();
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return menu.getActiveTab() == 0 ? TEXTURE_OFFER : TEXTURE_STORAGE;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // Render Trade-Pfeil nur im Offer-Tab
        if (menu.getActiveTab() == 0) {
            renderTradeArrow(guiGraphics);
        }
    }

    private void renderTradeArrow(GuiGraphics guiGraphics) {
        int x = this.leftPos;
        int y = this.topPos;
        int arrowX = x + 80; // Position zwischen Bezahl- und Kaufslots
        int arrowY = y + 55;

        // Bestimme Pfeil-Status
        boolean canTrade = menu.hasActiveOffer() && menu.hasStock();
        ResourceLocation arrowTexture = canTrade ? ARROW_ENABLED : ARROW_DISABLED;

        // Render Pfeil (16x16 Sprite)
        guiGraphics.blitSprite(arrowTexture, arrowX, arrowY, 16, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateButtonVisibility();
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render zusätzliche UI-Elemente
        renderTabIndicators(graphics);
        renderOfferStatus(graphics, mouseX, mouseY);
    }

    private void renderTabIndicators(GuiGraphics graphics) {
        // Kleine visuelle Indikatoren für aktiven Tab
        int x = this.leftPos + this.imageWidth + 2;
        int y = this.topPos + 18;

        if (menu.getActiveTab() == 0) {
            graphics.fill(x, y + 2, x + 2, y + 22, 0xFF4CAF50); // Grün für Offer-Tab
        } else {
            graphics.fill(x, y + 26, x + 2, y + 46, 0xFF2196F3); // Blau für Storage-Tab
        }
    }

    private void renderOfferStatus(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getActiveTab() != 0) return;

        // Status-Text über dem Angebot
        int x = this.leftPos + 8;
        int y = this.topPos + 8;

        if (menu.hasActiveOffer()) {
            Component status = menu.hasStock() ?
                    Component.translatable("status.marketblocks.shop.active") :
                    Component.translatable("status.marketblocks.shop.no_stock");
            graphics.drawString(this.font, status, x, y, menu.hasStock() ? 0x00FF00 : 0xFF0000);
        } else {
            Component status = Component.translatable("status.marketblocks.shop.no_offer");
            graphics.drawString(this.font, status, x, y, 0x888888);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Regelmäßige Updates für dynamische Elemente
        updateButtonVisibility();
    }
}