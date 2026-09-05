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
        BUILDER.push("General");
        MARKETCRATE_DEFAULT_EMIT_REDSTONE = BUILDER
                .comment("Emit redstone signal when a trade occurs (Default: false)")
                .define("emitRedstone", false);
        MARKETCRATE_DEFAULT_PURCHASE_XP_SOUND = BUILDER
                .comment("Play XP pickup sound upon successful trade (Default: false)")
                .define("purchaseXpSound", false);
        MARKETCRATE_DEFAULT_IS_CLOSED = BUILDER
                .comment("Whether newly placed Market Crates start as closed (Default: false)")
                .define("isClosed", false);
        BUILDER.pop();

        BUILDER.push("Villager");
        MARKETCRATE_DEFAULT_VILLAGER_NPC_ENABLED = BUILDER
                .comment("Show visual NPC by default (Default: false)")
                .define("enabled", false);
        MARKETCRATE_DEFAULT_VILLAGER_PROFESSION = BUILDER
                .comment("Default profession of the visual NPC")
                .defineEnum("profession", VillagerVisualProfession.NONE);
        MARKETCRATE_DEFAULT_PURCHASE_PARTICLES = BUILDER
                .comment("Spawn happy villager particles upon trade (Default: false)")
                .define("purchaseParticles", false);
        MARKETCRATE_DEFAULT_PURCHASE_SOUNDS = BUILDER
                .comment("Play villager trade sound upon purchase (Default: false)")
                .define("purchaseSounds", false);
        MARKETCRATE_DEFAULT_PAYMENT_SLOT_SOUNDS = BUILDER
                .comment("Play villager ambient sounds when payment slots change (Default: false)")
                .define("paymentSlotSounds", false);
        MARKETCRATE_DEFAULT_USE_PLAYER_SKIN = BUILDER
                .comment("Use the owner's player skin for the visual NPC (Default: false)")
                .define("usePlayerSkin", false);
        BUILDER.pop();

        BUILDER.push("Visuals");
        MARKETCRATE_DEFAULT_ITEM_VISIBLE = BUILDER
                .comment("Display items in the crate (Default: true)")
                .define("visible", true);
        MARKETCRATE_DEFAULT_ITEM_FULLBRIGHT = BUILDER
                .comment("Render item with full brightness (Default: false)")
                .define("fullbright", false);
        MARKETCRATE_DEFAULT_ITEM_SCALE = BUILDER
                .comment("Scale of items inside the crate (Default: 1.0)")
                .defineInRange("scale", 1.0, 0.25, 1.5);
        MARKETCRATE_DEFAULT_ITEM_COUNT = BUILDER
                .comment("Number of rendered items inside the crate (Default: 1)")
                .defineInRange("itemCount", 1, 1, 96);
        MARKETCRATE_DEFAULT_ITEM_LAYOUT_MODE = BUILDER
                .comment("Layout mode for items inside the crate (STACKED, GRID, CIRCLE, RANDOM)")
                .defineEnum("layoutMode", CrateLayoutMode.STACKED);
        MARKETCRATE_DEFAULT_ITEM_DYNAMIC_FILL = BUILDER
                .comment("Dynamically scale rendered item count based on remaining stock (Default: false)")
                .define("dynamicFill", false);
        MARKETCRATE_DEFAULT_ITEM_ROTATION = BUILDER
                .comment("Base rotation angle in degrees (Default: 0.0)")
                .defineInRange("rotation", 0.0, 0.0, 360.0);
        MARKETCRATE_DEFAULT_ITEM_SPACING_XZ = BUILDER
                .comment("Horizontal spacing between items (Default: 0.0)")
                .defineInRange("spacingXZ", 0.0, -0.5, 0.5);
        MARKETCRATE_DEFAULT_ITEM_SPACING_Y = BUILDER
                .comment("Vertical spacing between item layers (Default: 0.0)")
                .defineInRange("spacingY", 0.0, 0.0, 2.0);
        MARKETCRATE_DEFAULT_ITEM_CHAOS_ROTATION = BUILDER
                .comment("Random jitter / chaos rotation amount (Default: 0.0)")
                .defineInRange("chaosRotation", 0.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("IO");
        MARKETCRATE_DEFAULT_REDSTONE_CONTROL = BUILDER
                .comment("Redstone control mode for chest IO (IGNORED, LOW, HIGH)")
                .defineEnum("redstoneControl", IoRedstoneControl.IGNORED);
        MARKETCRATE_DEFAULT_ALLOW_IO = BUILDER
                .comment("Allow chest IO interactions (Default: false)")
                .define("allowIo", false);
        MARKETCRATE_DEFAULT_AUTO_IO = BUILDER
                .comment("Automatically pull/push items periodically (Default: false)")
                .define("autoIo", false);
        BUILDER.pop();

        BUILDER.push("Notifications");
        MARKETCRATE_DEFAULT_NOTIFY_PURCHASE = BUILDER
                .comment("Notify owner upon purchase (Default: false)")
                .define("notifyPurchase", false);
        MARKETCRATE_DEFAULT_NOTIFY_OUT_OF_STOCK = BUILDER
                .comment("Notify owner when stock is empty (Default: false)")
                .define("notifyOutOfStock", false);
        MARKETCRATE_DEFAULT_NOTIFY_OUTPUT_FULL = BUILDER
                .comment("Notify owner when output inventory is full (Default: false)")
                .define("notifyOutputFull", false);
        MARKETCRATE_DEFAULT_NOTIFY_CO_OWNERS = BUILDER
                .comment("Send notifications to co-owners as well (Default: false)")
                .define("notifyCoOwners", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
