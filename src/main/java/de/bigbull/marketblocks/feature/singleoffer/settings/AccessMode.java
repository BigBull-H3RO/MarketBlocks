package de.bigbull.marketblocks.feature.singleoffer.settings;

import net.minecraft.network.chat.Component;

/**
 * Defines the access control mode for a shop.
 */
public enum AccessMode {
    EVERYONE("gui.marketblocks.access.mode.everyone"),
    WHITELIST("gui.marketblocks.access.mode.whitelist"),
    BLACKLIST("gui.marketblocks.access.mode.blacklist");

    private static final AccessMode[] VALUES = values();

    private final String translationKey;

    AccessMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public Component title() {
        return Component.translatable(translationKey);
    }

    public AccessMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}
