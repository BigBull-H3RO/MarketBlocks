package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;

/**
 * Interface für Small-Shop-Menüs.
 */
public interface ShopMenu {
    /**
     * Gibt die zugehörige Block-Entity des Shops zurück.
     */
    SmallShopBlockEntity getBlockEntity();

    /**
     * @return {@code true}, wenn der aktuelle Spieler Eigentümer des Shops ist.
     */
    boolean isOwner();
}