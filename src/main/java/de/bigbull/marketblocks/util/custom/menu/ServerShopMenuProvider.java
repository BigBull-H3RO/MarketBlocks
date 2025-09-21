package de.bigbull.marketblocks.util.custom.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * MenuProvider für den blocklosen Server-Shop.
 */
public record ServerShopMenuProvider(boolean canEdit, int selectedPage) implements MenuProvider {
    private static final Component TITLE = Component.translatable("menu.marketblocks.server_shop");

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ServerShopMenu(containerId, playerInventory, canEdit, selectedPage);
    }
}