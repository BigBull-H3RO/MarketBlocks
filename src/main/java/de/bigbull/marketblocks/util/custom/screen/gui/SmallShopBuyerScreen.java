package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Käuferansicht für den Small Shop. Baut auf {@link SmallShopScreen} auf und
 * ergänzt nur die kaufspezifischen Bedienelemente.
 */
public class SmallShopBuyerScreen extends SmallShopScreen {
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/button"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/button_hover"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/button_selected"));

    private static final ResourceLocation ICON_CONFIRM = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/confirm");
    private static final ResourceLocation ICON_HOME = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "container/beacon/home");
    public SmallShopBuyerScreen(SmallShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        addRenderableWidget(new IconButton(x + 110, y + 90, 20, 20, BUTTON_SPRITES, ICON_CONFIRM, b -> {
            if (!menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_BUY) && this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.translatable("message.marketblocks.small_shop.buy_failed"), true);
            }
        }, Component.translatable("screen.marketblocks.small_shop.buy"), () -> false));

        // Nur Angebots-Tab sichtbar
        addRenderableWidget(new IconButton(x + this.imageWidth + 4, y + 20, 20, 20, BUTTON_SPRITES, ICON_HOME, b -> {
        }, Component.translatable("screen.marketblocks.small_shop.tab.offer"), () -> true));
    }
}