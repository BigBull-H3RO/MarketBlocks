package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SmallShopScreen extends AbstractContainerScreen<SmallShopMenu> {
    public SmallShopScreen(SmallShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Hintergrund wird clientseitig gerendert; Platzhalter ohne Textur
    }
}