package de.bigbull.marketblocks.feature.singleoffer.menu;

/**
 * Represents the available tabs in a shop's UI.
 */
public enum ShopTab {
    OFFERS,
    INVENTORY,
    SETTINGS,
    LOG;

    public static ShopTab fromId(int id) {
        return id >= 0 && id < values().length ? values()[id] : OFFERS;
    }
}
