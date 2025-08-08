package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.SmallShopScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Käuferansicht für den Small Shop. Baut auf {@link SmallShopScreen} auf und
 * ergänzt nur die kaufspezifischen Bedienelemente.
 */
public class SmallShopBuyerScreen extends SmallShopScreen {
    public SmallShopBuyerScreen(SmallShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.marketblocks.small_shop.buy"), b -> {
            if (!menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_BUY) && this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.translatable("message.marketblocks.small_shop.buy_failed"), true);
            }
        }).pos(x + 110, y + 90).size(60, 20).build());

        // Nur Angebots-Tab sichtbar
        this.addRenderableWidget(Button.builder(Component.translatable("screen.marketblocks.small_shop.tab.offer"), b -> {})
                .pos(x + this.imageWidth + 4, y + 20).size(60, 20).build());
    }
}