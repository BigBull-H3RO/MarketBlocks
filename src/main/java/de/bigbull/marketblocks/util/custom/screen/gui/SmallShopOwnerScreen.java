package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Besitzeransicht für den Small Shop. Erweitert {@link SmallShopScreen} um
 * Speicher- und Angebots-Tab sowie entsprechende Buttons.
 */
public class SmallShopOwnerScreen extends SmallShopScreen {
    private static final ResourceLocation TEXTURE_OFFER = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop.png");
    private static final ResourceLocation TEXTURE_STORAGE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/shop_storage.png");

    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/button_highlighted"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/button"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/button_selected"));

    private static final ResourceLocation ICON_CONFIRM = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/confirm");
    private static final ResourceLocation ICON_CANCEL = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/cancel");
    private static final ResourceLocation ICON_HOME = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/home");
    private static final ResourceLocation ICON_INVENTORY = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/shop/inventory");

    private IconButton saveButton;
    private IconButton removeButton;

    private int tab = 0;

    public SmallShopOwnerScreen(SmallShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        saveButton = addRenderableWidget(new IconButton(x + 110, y + 90, 20, 20, BUTTON_SPRITES, ICON_CONFIRM, b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_CONFIRM);
        }, Component.translatable("screen.marketblocks.small_shop.save"), () -> false));

        removeButton = addRenderableWidget(new IconButton(x + 110, y + 115, 20, 20, BUTTON_SPRITES, ICON_CANCEL, b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_REMOVE);
        }, Component.translatable("screen.marketblocks.small_shop.remove"), () -> false));
        removeButton.setTooltip(Tooltip.create(Component.translatable("screen.marketblocks.small_shop.remove")));

        // Tab-Leiste rechts
        addRenderableWidget(new IconButton(x + this.imageWidth + 4, y + 20, 20, 20, BUTTON_SPRITES, ICON_HOME, b -> {
            tab = 0;
            menu.setActiveTab(0);
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_TAB_OFFER);
            }, Component.translatable("screen.marketblocks.small_shop.tab.offer"), () -> tab == 0));

        addRenderableWidget(new IconButton(x + this.imageWidth + 4, y + 44, 20, 20, BUTTON_SPRITES, ICON_INVENTORY, b -> {
            tab = 1;
            menu.setActiveTab(1);
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_TAB_STORAGE);
            }, Component.translatable("screen.marketblocks.small_shop.tab.storage"), () -> tab == 1));
        menu.setActiveTab(tab);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return tab == 0 ? TEXTURE_OFFER : TEXTURE_STORAGE;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        saveButton.visible = tab == 0;
        removeButton.visible = tab == 0;
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}