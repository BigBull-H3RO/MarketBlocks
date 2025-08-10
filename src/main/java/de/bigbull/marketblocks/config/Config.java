package de.bigbull.marketblocks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Shop Settings
    private static final ModConfigSpec.IntValue MAX_INPUT_STACKS = BUILDER
            .comment("Maximum number of item stacks in input inventory (default: 12)")
            .defineInRange("maxInputStacks", 12, 1, 64);

    private static final ModConfigSpec.IntValue MAX_OUTPUT_STACKS = BUILDER
            .comment("Maximum number of item stacks in output inventory (default: 12)")
            .defineInRange("maxOutputStacks", 12, 1, 64);

    private static final ModConfigSpec.BooleanValue ALLOW_SELF_TRADING = BUILDER
            .comment("Allow shop owners to trade with their own shops (default: true)")
            .define("allowSelfTrading", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_EXACT_PAYMENT = BUILDER
            .comment("Require exact payment amounts, no overpayment allowed (default: false)")
            .define("requireExactPayment", false);

    // UI Settings
    private static final ModConfigSpec.BooleanValue SHOW_OWNER_NAME = BUILDER
            .comment("Show shop owner name in GUI for non-owners (default: true)")
            .define("showOwnerName", true);

    private static final ModConfigSpec.BooleanValue PLAY_TRADE_SOUNDS = BUILDER
            .comment("Play sounds when trading (default: true)")
            .define("playTradeSounds", true);

    // Security Settings
    private static final ModConfigSpec.BooleanValue PROTECT_FROM_EXPLOSIONS = BUILDER
            .comment("Protect shops from explosions (default: true)")
            .define("protectFromExplosions", true);

    private static final ModConfigSpec.BooleanValue ALLOW_HOPPERS = BUILDER
            .comment("Allow hoppers to interact with shops (default: false)")
            .define("allowHoppers", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Getter methods
    public static int getMaxInputStacks() {
        return MAX_INPUT_STACKS.get();
    }

    public static int getMaxOutputStacks() {
        return MAX_OUTPUT_STACKS.get();
    }

    public static boolean allowSelfTrading() {
        return ALLOW_SELF_TRADING.get();
    }

    public static boolean requireExactPayment() {
        return REQUIRE_EXACT_PAYMENT.get();
    }

    public static boolean showOwnerName() {
        return SHOW_OWNER_NAME.get();
    }

    public static boolean playTradeSounds() {
        return PLAY_TRADE_SOUNDS.get();
    }

    public static boolean protectFromExplosions() {
        return PROTECT_FROM_EXPLOSIONS.get();
    }

    public static boolean allowHoppers() {
        return ALLOW_HOPPERS.get();
    }
}
