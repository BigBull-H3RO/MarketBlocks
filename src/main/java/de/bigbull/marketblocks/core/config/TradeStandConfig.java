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
        BUILDER.push("General");
        TRADESTAND_DEFAULT_EMIT_REDSTONE = BUILDER
                .comment("Emit redstone signal when a trade occurs (Default: false)")
                .define("emitRedstone", false);
        TRADESTAND_DEFAULT_PURCHASE_XP_SOUND = BUILDER
                .comment("Play XP pickup sound upon successful trade (Default: false)")
                .define("purchaseXpSound", false);
        TRADESTAND_DEFAULT_IS_CLOSED = BUILDER
                .comment("Whether newly placed TradeStands start as closed (Default: false)")
                .define("isClosed", false);
        BUILDER.pop();

        BUILDER.push("Villager");
        TRADESTAND_DEFAULT_VILLAGER_NPC_ENABLED = BUILDER
                .comment("Show visual NPC by default (Default: false)")
                .define("enabled", false);
        TRADESTAND_DEFAULT_VILLAGER_PROFESSION = BUILDER
                .comment("Default profession of the visual NPC")
                .defineEnum("profession", VillagerVisualProfession.NONE);
        TRADESTAND_DEFAULT_PURCHASE_PARTICLES = BUILDER
                .comment("Spawn happy villager particles upon trade (Default: false)")
                .define("purchaseParticles", false);
        TRADESTAND_DEFAULT_PURCHASE_SOUNDS = BUILDER
                .comment("Play villager trade sound upon purchase (Default: false)")
                .define("purchaseSounds", false);
        TRADESTAND_DEFAULT_PAYMENT_SLOT_SOUNDS = BUILDER
                .comment("Play villager ambient sounds when payment slots change (Default: false)")
                .define("paymentSlotSounds", false);
        TRADESTAND_DEFAULT_USE_PLAYER_SKIN = BUILDER
                .comment("Use the owner's player skin for the visual NPC (Default: false)")
                .define("usePlayerSkin", false);
        BUILDER.pop();

        BUILDER.push("Visuals");
        TRADESTAND_DEFAULT_ITEM_VISIBLE = BUILDER
                .comment("Display floating offer item (Default: true)")
                .define("visible", true);
        TRADESTAND_DEFAULT_ITEM_FULLBRIGHT = BUILDER
                .comment("Render item with full brightness (Default: false)")
                .define("fullbright", false);
        TRADESTAND_DEFAULT_ITEM_SCALE = BUILDER
                .comment("Scale of the floating item (Default: 1.0)")
                .defineInRange("scale", 1.0, 0.5, 1.5);
        TRADESTAND_DEFAULT_ITEM_SPEED = BUILDER
                .comment("Rotation speed of the floating item (Default: 0.75)")
                .defineInRange("speed", 0.75, 0.0, 1.5);
        TRADESTAND_DEFAULT_ITEM_HEIGHT_OFFSET = BUILDER
                .comment("Vertical height offset of the floating item (Default: 0.0)")
                .defineInRange("heightOffset", 0.0, -0.25, 0.25);
        TRADESTAND_DEFAULT_ITEM_BOBBING = BUILDER
                .comment("Enable gentle up/down bobbing motion (Default: false)")
                .define("bobbing", false);
        BUILDER.pop();

        BUILDER.push("IO");
        TRADESTAND_DEFAULT_REDSTONE_CONTROL = BUILDER
                .comment("Redstone control mode for chest IO (IGNORED, LOW, HIGH)")
                .defineEnum("redstoneControl", IoRedstoneControl.IGNORED);
        TRADESTAND_DEFAULT_ALLOW_IO = BUILDER
                .comment("Allow chest IO interactions (Default: false)")
                .define("allowIo", false);
        TRADESTAND_DEFAULT_AUTO_IO = BUILDER
                .comment("Automatically pull/push items periodically (Default: false)")
                .define("autoIo", false);
        BUILDER.pop();

        BUILDER.push("Notifications");
        TRADESTAND_DEFAULT_NOTIFY_PURCHASE = BUILDER
                .comment("Notify owner upon purchase (Default: false)")
                .define("notifyPurchase", false);
        TRADESTAND_DEFAULT_NOTIFY_OUT_OF_STOCK = BUILDER
                .comment("Notify owner when stock is empty (Default: false)")
                .define("notifyOutOfStock", false);
        TRADESTAND_DEFAULT_NOTIFY_OUTPUT_FULL = BUILDER
                .comment("Notify owner when output inventory is full (Default: false)")
                .define("notifyOutputFull", false);
        TRADESTAND_DEFAULT_NOTIFY_CO_OWNERS = BUILDER
                .comment("Send notifications to co-owners as well (Default: false)")
                .define("notifyCoOwners", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
