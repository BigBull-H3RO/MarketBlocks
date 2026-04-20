package de.bigbull.marketblocks.util.screen.singleoffer;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Settings-Unterseiten fuer den Single-Offer-Shop.
 * Neue Kategorien koennen hier zentral erweitert werden.
 */
public enum SettingsCategory {
    GENERAL("gui.marketblocks.settings.category.general", "textures/gui/icon/singleoffer/general.png"),
    IO("gui.marketblocks.settings.category.io", "textures/gui/icon/singleoffer/i_o.png"),
    ACCESS("gui.marketblocks.settings.category.access", "textures/gui/icon/singleoffer/padlock.png");

    private final String translationKey;
    private final ResourceLocation icon;

    SettingsCategory(String translationKey, String iconPath) {
        this.translationKey = translationKey;
        this.icon = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, iconPath);
    }

    public Component title() {
        return Component.translatable(translationKey);
    }

    public ResourceLocation icon() {
        return icon;
    }
}



