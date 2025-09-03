package de.bigbull.marketblocks.util.custom.menu;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the different tabs or screens available in the Small Shop menu.
 */
public enum ShopTab {
    OFFERS,
    INVENTORY,
    SETTINGS;

    /**
     * Gets a ShopTab from its ordinal ID.
     * @param id The ordinal ID of the tab.
     * @return The corresponding ShopTab, or null if the ID is invalid.
     */
    @Nullable
    public static ShopTab fromId(int id) {
        if (id < 0 || id >= values().length) {
            return null;
        }
        return values()[id];
    }
}