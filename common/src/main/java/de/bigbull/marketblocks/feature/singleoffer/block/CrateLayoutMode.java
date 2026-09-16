package de.bigbull.marketblocks.feature.singleoffer.block;

/**
 * Defines the layout mode for items displayed in a MarketCrate.
 *
 * NOTE: This enum intentionally exposes only the two modes used by the MarketCrate
 * UI: LOSE (loose / scattered) and GESTAPELT (stacked / grid-based).
 */
public enum CrateLayoutMode {
    SCATTERED("lose"),
    STACKED("gestapelt");

    private static final CrateLayoutMode[] VALUES = values();

    private final String serializedName;

    CrateLayoutMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public String translationKey() {
        return "gui.marketblocks.visuals.layout_mode." + serializedName;
    }

    public CrateLayoutMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static CrateLayoutMode fromSerialized(String value) {
        if (value == null || value.isBlank()) {
            return SCATTERED;
        }
        for (CrateLayoutMode mode : VALUES) {
            if (mode.serializedName.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return SCATTERED;
    }
}
