package de.bigbull.marketblocks.util.custom.menu;

public enum ShopTab {
    OFFERS,
    INVENTORY,
    SETTINGS;

    public static ShopTab fromId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }
}