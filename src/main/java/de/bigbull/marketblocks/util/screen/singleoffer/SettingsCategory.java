package de.bigbull.marketblocks.util.screen.singleoffer;

import net.minecraft.network.chat.Component;

/**
 * Settings-Unterseiten fuer den Single-Offer-Shop.
 * Neue Kategorien koennen hier zentral erweitert werden.
 */
public enum SettingsCategory {
    GENERAL("gui.marketblocks.settings.category.general"),
    IO("gui.marketblocks.settings.category.io"),
    ACCESS("gui.marketblocks.settings.category.access");

    private final String translationKey;

    SettingsCategory(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component title() {
        return Component.translatable(translationKey);
    }
}



