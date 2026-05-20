package de.bigbull.marketblocks.feature.singleoffer.block;

/**
 * Defines the layout mode for items displayed in a MarketCrate.
 *
 * NOTE: This enum intentionally exposes only the two modes used by the MarketCrate
 * UI: LOSE (loose / scattered) and GESTAPELT (stacked / grid-based).
 */
public enum CrateLayoutMode {
    LOSE("lose"),
    GESTAPELT("gestapelt");

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
        CrateLayoutMode[] values = values();
        int next = (ordinal() + 1) % values.length;
        return values[next];
    }

    public static CrateLayoutMode fromSerialized(String value) {
        if (value == null || value.isBlank()) {
            return LOSE;
        }
        for (CrateLayoutMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return LOSE;
    }
}

