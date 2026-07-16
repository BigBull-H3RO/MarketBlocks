package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.feature.singleoffer.settings.IoRedstoneControl;
import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
import net.neoforged.neoforge.common.ModConfigSpec;

public class TradeStandConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_EMIT_REDSTONE;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PURCHASE_XP_SOUND;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_IS_CLOSED;

    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_VILLAGER_NPC_ENABLED;
    public static final ModConfigSpec.EnumValue<VillagerVisualProfession> TRADESTAND_DEFAULT_VILLAGER_PROFESSION;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PURCHASE_PARTICLES;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PURCHASE_SOUNDS;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_PAYMENT_SLOT_SOUNDS;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_USE_PLAYER_SKIN;

    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ITEM_VISIBLE;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ITEM_FULLBRIGHT;
    public static final ModConfigSpec.DoubleValue TRADESTAND_DEFAULT_ITEM_SCALE;
    public static final ModConfigSpec.DoubleValue TRADESTAND_DEFAULT_ITEM_SPEED;
    public static final ModConfigSpec.DoubleValue TRADESTAND_DEFAULT_ITEM_HEIGHT_OFFSET;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ITEM_BOBBING;

    public static final ModConfigSpec.EnumValue<IoRedstoneControl> TRADESTAND_DEFAULT_REDSTONE_CONTROL;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_ALLOW_IO;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_AUTO_IO;

    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_PURCHASE;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_OUT_OF_STOCK;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_OUTPUT_FULL;
    public static final ModConfigSpec.BooleanValue TRADESTAND_DEFAULT_NOTIFY_CO_OWNERS;

    static {
        BUILDER.push("TradeStand - Default Values");
        BUILDER.comment("These values are used as starting values for new TradeStands. If a tab is disabled above, these serve as fixed forced values.");
        
        BUILDER.push("General Defaults");
        TRADESTAND_DEFAULT_EMIT_REDSTONE = BUILDER.define("tradestandDefaultEmitRedstone", false);
        TRADESTAND_DEFAULT_PURCHASE_XP_SOUND = BUILDER.define("tradestandDefaultPurchaseXpSound", false);
        TRADESTAND_DEFAULT_IS_CLOSED = BUILDER.define("tradestandDefaultIsClosed", false);
        BUILDER.pop();
        
        BUILDER.push("Villager Defaults");
        TRADESTAND_DEFAULT_VILLAGER_NPC_ENABLED = BUILDER.define("tradestandDefaultVillagerNpcEnabled", false);
        TRADESTAND_DEFAULT_VILLAGER_PROFESSION = BUILDER.defineEnum("tradestandDefaultVillagerProfession", VillagerVisualProfession.NONE);
        TRADESTAND_DEFAULT_PURCHASE_PARTICLES = BUILDER.define("tradestandDefaultPurchaseParticles", false);
        TRADESTAND_DEFAULT_PURCHASE_SOUNDS = BUILDER.define("tradestandDefaultPurchaseSounds", false);
        TRADESTAND_DEFAULT_PAYMENT_SLOT_SOUNDS = BUILDER.define("tradestandDefaultPaymentSlotSounds", false);
        TRADESTAND_DEFAULT_USE_PLAYER_SKIN = BUILDER.define("tradestandDefaultUsePlayerSkin", false);
        BUILDER.pop();
        
        BUILDER.push("Visuals Defaults");
        TRADESTAND_DEFAULT_ITEM_VISIBLE = BUILDER.define("tradestandDefaultItemVisible", true);
        TRADESTAND_DEFAULT_ITEM_FULLBRIGHT = BUILDER.define("tradestandDefaultItemFullbright", false);
        TRADESTAND_DEFAULT_ITEM_SCALE = BUILDER.defineInRange("tradestandDefaultItemScale", 1.0, 0.5, 1.5);
        TRADESTAND_DEFAULT_ITEM_SPEED = BUILDER.defineInRange("tradestandDefaultItemSpeed", 0.75, 0.0, 1.5);
        TRADESTAND_DEFAULT_ITEM_HEIGHT_OFFSET = BUILDER.defineInRange("tradestandDefaultItemHeightOffset", 0.0, -0.25, 0.25);
        TRADESTAND_DEFAULT_ITEM_BOBBING = BUILDER.define("tradestandDefaultItemBobbing", false);
        BUILDER.pop();
        
        BUILDER.push("I/O Defaults");
        TRADESTAND_DEFAULT_REDSTONE_CONTROL = BUILDER.defineEnum("tradestandDefaultRedstoneControl", IoRedstoneControl.IGNORED);
        TRADESTAND_DEFAULT_ALLOW_IO = BUILDER.define("tradestandDefaultAllowIo", false);
        TRADESTAND_DEFAULT_AUTO_IO = BUILDER.define("tradestandDefaultAutoIo", false);
        BUILDER.pop();
        
        BUILDER.push("Notification Defaults");
        TRADESTAND_DEFAULT_NOTIFY_PURCHASE = BUILDER.define("tradestandDefaultNotifyPurchase", false);
        TRADESTAND_DEFAULT_NOTIFY_OUT_OF_STOCK = BUILDER.define("tradestandDefaultNotifyOutOfStock", false);
        TRADESTAND_DEFAULT_NOTIFY_OUTPUT_FULL = BUILDER.define("tradestandDefaultNotifyOutputFull", false);
        TRADESTAND_DEFAULT_NOTIFY_CO_OWNERS = BUILDER.define("tradestandDefaultNotifyCoOwners", false);
        BUILDER.pop();
        
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
