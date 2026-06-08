package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Settings subcategories for the single-offer shop.
 * New categories can be centrally expanded here.
 */
public enum SettingsCategory {
    GENERAL("gui.marketblocks.settings.category.general", "textures/gui/icon/singleoffer/general.png", Config.SHOP_TAB_GENERAL_ENABLED),
    IO("gui.marketblocks.settings.category.io", "textures/gui/icon/singleoffer/i_o.png", Config.SHOP_TAB_IO_ENABLED),
    VILLAGER("gui.marketblocks.settings.category.villager", "textures/gui/icon/singleoffer/villager.png", Config.SHOP_TAB_VILLAGER_ENABLED),
    VISUALS("gui.marketblocks.settings.category.visual", "textures/gui/icon/singleoffer/visuals.png", Config.SHOP_TAB_VISUALS_ENABLED),
    NOTIFICATIONS("gui.marketblocks.settings.category.notifications", "textures/gui/icon/singleoffer/notification.png", Config.SHOP_TAB_NOTIFICATIONS_ENABLED),
    ACCESS("gui.marketblocks.settings.category.access", "textures/gui/icon/singleoffer/padlock.png", Config.SHOP_TAB_ACCESS_ENABLED);

    private final String translationKey;
    private final ResourceLocation icon;
    private final net.neoforged.neoforge.common.ModConfigSpec.BooleanValue configToggle;

    SettingsCategory(String translationKey, String iconPath, net.neoforged.neoforge.common.ModConfigSpec.BooleanValue configToggle) {
        this.translationKey = translationKey;
        this.icon = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, iconPath);
        this.configToggle = configToggle;
    }

    public boolean isEnabled() {
        return configToggle == null || configToggle.get();
    }

    public Component title() {
        return Component.translatable(translationKey);
    }

    public ResourceLocation icon() {
        return icon;
    }
}


