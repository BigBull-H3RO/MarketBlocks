package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoRedstoneControl;
import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MarketCrateConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_EMIT_REDSTONE;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PURCHASE_XP_SOUND;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_IS_CLOSED;

    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_VILLAGER_NPC_ENABLED;
    public static final ModConfigSpec.EnumValue<VillagerVisualProfession> MARKETCRATE_DEFAULT_VILLAGER_PROFESSION;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PURCHASE_PARTICLES;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PURCHASE_SOUNDS;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_PAYMENT_SLOT_SOUNDS;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_USE_PLAYER_SKIN;

    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ITEM_VISIBLE;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ITEM_FULLBRIGHT;
    public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_SCALE;
    public static final ModConfigSpec.IntValue MARKETCRATE_DEFAULT_ITEM_COUNT;
    public static final ModConfigSpec.EnumValue<CrateLayoutMode> MARKETCRATE_DEFAULT_ITEM_LAYOUT_MODE;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ITEM_DYNAMIC_FILL;
    public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_ROTATION;
    public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_SPACING_XZ;
    public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_SPACING_Y;
    public static final ModConfigSpec.DoubleValue MARKETCRATE_DEFAULT_ITEM_CHAOS_ROTATION;

    public static final ModConfigSpec.EnumValue<IoRedstoneControl> MARKETCRATE_DEFAULT_REDSTONE_CONTROL;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_ALLOW_IO;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_AUTO_IO;

    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_PURCHASE;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_OUT_OF_STOCK;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_OUTPUT_FULL;
    public static final ModConfigSpec.BooleanValue MARKETCRATE_DEFAULT_NOTIFY_CO_OWNERS;

    static {
        BUILDER.push("MarketCrate - Default Values");
        BUILDER.comment("These values are used as starting values for new MarketCrates. If a tab is disabled above, these serve as fixed forced values.");
        
        BUILDER.push("General Defaults");
        MARKETCRATE_DEFAULT_EMIT_REDSTONE = BUILDER.define("marketcrateDefaultEmitRedstone", false);
        MARKETCRATE_DEFAULT_PURCHASE_XP_SOUND = BUILDER.define("marketcrateDefaultPurchaseXpSound", false);
        MARKETCRATE_DEFAULT_IS_CLOSED = BUILDER.define("marketcrateDefaultIsClosed", false);
        BUILDER.pop();
        
        BUILDER.push("Villager Defaults");
        MARKETCRATE_DEFAULT_VILLAGER_NPC_ENABLED = BUILDER.define("marketcrateDefaultVillagerNpcEnabled", false);
        MARKETCRATE_DEFAULT_VILLAGER_PROFESSION = BUILDER.defineEnum("marketcrateDefaultVillagerProfession", VillagerVisualProfession.NONE);
        MARKETCRATE_DEFAULT_PURCHASE_PARTICLES = BUILDER.define("marketcrateDefaultPurchaseParticles", false);
        MARKETCRATE_DEFAULT_PURCHASE_SOUNDS = BUILDER.define("marketcrateDefaultPurchaseSounds", false);
        MARKETCRATE_DEFAULT_PAYMENT_SLOT_SOUNDS = BUILDER.define("marketcrateDefaultPaymentSlotSounds", false);
        MARKETCRATE_DEFAULT_USE_PLAYER_SKIN = BUILDER.define("marketcrateDefaultUsePlayerSkin", false);
        BUILDER.pop();
        
        BUILDER.push("Visuals Defaults");
        MARKETCRATE_DEFAULT_ITEM_VISIBLE = BUILDER.define("marketcrateDefaultItemVisible", true);
        MARKETCRATE_DEFAULT_ITEM_FULLBRIGHT = BUILDER.define("marketcrateDefaultItemFullbright", false);
        MARKETCRATE_DEFAULT_ITEM_SCALE = BUILDER.defineInRange("marketcrateDefaultItemScale", 1.0, 0.25, 1.5);
        MARKETCRATE_DEFAULT_ITEM_COUNT = BUILDER.defineInRange("marketcrateDefaultItemCount", 1, 1, 96);
        MARKETCRATE_DEFAULT_ITEM_LAYOUT_MODE = BUILDER.defineEnum("marketcrateDefaultItemLayoutMode", CrateLayoutMode.STACKED);
        MARKETCRATE_DEFAULT_ITEM_DYNAMIC_FILL = BUILDER.define("marketcrateDefaultItemDynamicFill", false);
        MARKETCRATE_DEFAULT_ITEM_ROTATION = BUILDER.defineInRange("marketcrateDefaultItemRotation", 0.0, 0.0, 360.0);
        MARKETCRATE_DEFAULT_ITEM_SPACING_XZ = BUILDER.defineInRange("marketcrateDefaultItemSpacingXZ", 0.0, -0.5, 0.5);
        MARKETCRATE_DEFAULT_ITEM_SPACING_Y = BUILDER.defineInRange("marketcrateDefaultItemSpacingY", 0.0, 0.0, 2.0);
        MARKETCRATE_DEFAULT_ITEM_CHAOS_ROTATION = BUILDER.defineInRange("marketcrateDefaultItemChaosRotation", 0.0, 0.0, 1.0);
        BUILDER.pop();
        
        BUILDER.push("I/O Defaults");
        MARKETCRATE_DEFAULT_REDSTONE_CONTROL = BUILDER.defineEnum("marketcrateDefaultRedstoneControl", IoRedstoneControl.IGNORED);
        MARKETCRATE_DEFAULT_ALLOW_IO = BUILDER.define("marketcrateDefaultAllowIo", false);
        MARKETCRATE_DEFAULT_AUTO_IO = BUILDER.define("marketcrateDefaultAutoIo", false);
        BUILDER.pop();
        
        BUILDER.push("Notification Defaults");
        MARKETCRATE_DEFAULT_NOTIFY_PURCHASE = BUILDER.define("marketcrateDefaultNotifyPurchase", false);
        MARKETCRATE_DEFAULT_NOTIFY_OUT_OF_STOCK = BUILDER.define("marketcrateDefaultNotifyOutOfStock", false);
        MARKETCRATE_DEFAULT_NOTIFY_OUTPUT_FULL = BUILDER.define("marketcrateDefaultNotifyOutputFull", false);
        MARKETCRATE_DEFAULT_NOTIFY_CO_OWNERS = BUILDER.define("marketcrateDefaultNotifyCoOwners", false);
        BUILDER.pop();
        
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
