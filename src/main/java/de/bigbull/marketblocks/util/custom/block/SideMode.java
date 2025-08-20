package de.bigbull.marketblocks.util.custom.block;

import net.minecraft.network.chat.Component;

public enum SideMode {
    DISABLED("gui.marketblocks.disabled"),
    INPUT("gui.marketblocks.input"),
    OUTPUT("gui.marketblocks.output");

    private final String translationKey;

    SideMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public SideMode next() {
        return switch (this) {
            case DISABLED -> INPUT;
            case INPUT -> OUTPUT;
            case OUTPUT -> DISABLED;
        };
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }
}