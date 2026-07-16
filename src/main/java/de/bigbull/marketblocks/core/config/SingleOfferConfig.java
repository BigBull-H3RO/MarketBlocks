package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SingleOfferConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue SHOP_TAB_GENERAL_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_IO_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_VILLAGER_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_VISUALS_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_NOTIFICATIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOP_TAB_ACCESS_ENABLED;

    static {
        BUILDER.push("SingleOfferShop - Tab Visibility");
        SHOP_TAB_GENERAL_ENABLED = BUILDER.define("shopTabGeneralEnabled", true);
        SHOP_TAB_IO_ENABLED = BUILDER.define("shopTabIoEnabled", true);
        SHOP_TAB_VILLAGER_ENABLED = BUILDER.define("shopTabVillagerEnabled", true);
        SHOP_TAB_VISUALS_ENABLED = BUILDER.define("shopTabVisualsEnabled", true);
        SHOP_TAB_NOTIFICATIONS_ENABLED = BUILDER.define("shopTabNotificationsEnabled", true);
        SHOP_TAB_ACCESS_ENABLED = BUILDER.define("shopTabAccessEnabled", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
