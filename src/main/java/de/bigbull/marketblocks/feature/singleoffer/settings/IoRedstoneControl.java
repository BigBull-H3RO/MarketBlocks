package de.bigbull.marketblocks.feature.singleoffer.settings;

/**
 * Defines how redstone signals affect the automated I/O operations of a shop.
 */
public enum IoRedstoneControl {
    IGNORED("gui.marketblocks.io.redstone_control.ignored"),
    REQUIRE_SIGNAL("gui.marketblocks.io.redstone_control.require_signal"),
    REQUIRE_NO_SIGNAL("gui.marketblocks.io.redstone_control.require_no_signal");

    private static final IoRedstoneControl[] VALUES = values();

    private final String translationKey;

    IoRedstoneControl(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public IoRedstoneControl next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}
