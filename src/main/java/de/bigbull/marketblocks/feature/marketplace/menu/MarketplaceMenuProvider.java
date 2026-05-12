package de.bigbull.marketblocks.feature.marketplace.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * MenuProvider für den blocklosen Marktplatz.
 */
public record MarketplaceMenuProvider(boolean canEdit, boolean globalEditModeEnabled) implements MenuProvider {
    private static final Component TITLE = Component.translatable("menu.marketblocks.marketplace");

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MarketplaceMenu(containerId, playerInventory, canEdit, globalEditModeEnabled);
    }
}
