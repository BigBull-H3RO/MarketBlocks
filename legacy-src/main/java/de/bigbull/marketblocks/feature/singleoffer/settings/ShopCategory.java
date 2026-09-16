package de.bigbull.marketblocks.feature.singleoffer.settings;

/**
 * Represents the classification category of a SingleOfferShop.
 * Used for filtering shops in the shop directory and search commands.
 */
public enum ShopCategory {
    NONE("none"),
    WEAPONS_ARMOR("weapons_armor"),
    TOOLS("tools"),
    BLOCKS("blocks"),
    FOOD_POTIONS("food_potions"),
    VALUABLES("valuables"),
    MISC("misc");

    private final String id;

    ShopCategory(String id) {
        this.id = id;
    }

    /**
     * @return The internal string identifier of the category.
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves a {@link ShopCategory} by its string identifier.
     *
     * @param id The identifier to match.
     * @return The matching category, or {@link #NONE} if not found.
     */
    public static ShopCategory fromId(String id) {
        for (ShopCategory category : values()) {
            if (category.getId().equalsIgnoreCase(id)) {
                return category;
            }
        }
        return NONE;
    }

    /**
     * Cycles to the next category in the enum sequence.
     *
     * @return The next {@link ShopCategory}.
     */
    public ShopCategory next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
