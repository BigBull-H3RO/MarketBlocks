package de.bigbull.marketblocks.feature.singleoffer;

import net.minecraft.network.chat.Component;

/**
 * Represents the configuration modes for a side of a block in the I/O system.
 */
public enum SideMode {
    DISABLED("gui.marketblocks.disabled"),
    INPUT("gui.marketblocks.input"),
    OUTPUT("gui.marketblocks.output");

    private final String translationKey;

    SideMode(String translationKey) {
        this.translationKey = translationKey;
    }

    private static final SideMode[] VALUES = values();

    public SideMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }
}
