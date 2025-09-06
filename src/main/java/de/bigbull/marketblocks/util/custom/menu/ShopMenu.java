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
     * Liefert die Bitmaske der Flags dieses Menüs.
     */
    int getFlags();

    /**
     * Prüft, ob ein bestimmtes Flag gesetzt ist.
     */
    default boolean hasFlag(int flag) {
        return (getFlags() & flag) != 0;
    }

    /**
     * @return {@code true}, wenn der aktuelle Spieler Eigentümer des Shops ist.
     */
    default boolean isOwner() {
        return hasFlag(SmallShopBlockEntity.OWNER_FLAG);
    }
}