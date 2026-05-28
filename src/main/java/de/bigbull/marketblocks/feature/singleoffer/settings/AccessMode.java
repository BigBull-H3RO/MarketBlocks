package de.bigbull.marketblocks.feature.singleoffer.settings;

import net.minecraft.network.chat.Component;

public enum AccessMode {
    EVERYONE("gui.marketblocks.access.mode.everyone"),
    WHITELIST("gui.marketblocks.access.mode.whitelist"),
    BLACKLIST("gui.marketblocks.access.mode.blacklist");

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
        return values()[(this.ordinal() + 1) % values().length];
    }
}
