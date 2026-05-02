package de.bigbull.marketblocks.feature.singleoffer.menu;

public enum ShopTab {
    OFFERS,
    INVENTORY,
    SETTINGS,
    LOG;

    public static ShopTab fromId(int id) {
        return id >= 0 && id < values().length ? values()[id] : OFFERS;
    }
}
