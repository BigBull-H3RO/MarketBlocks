package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TraderConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // --- Spawning ---
    public static final ModConfigSpec.BooleanValue SPAWN_ENABLED;
    public static final ModConfigSpec.IntValue SPAWN_CHANCE;
    public static final ModConfigSpec.BooleanValue PREFER_DAYTIME_SPAWN;
    public static final ModConfigSpec.IntValue SPAWN_NEAR_PLAYER_CHANCE_PERCENT;
    public static final ModConfigSpec.IntValue MAX_PER_DIMENSION;

    // --- Behavior ---
    public static final ModConfigSpec.IntValue DESPAWN_TICKS;
    public static final ModConfigSpec.IntValue MAX_SHOPS_PER_VISIT;
    public static final ModConfigSpec.IntValue MIN_BUDGET;
    public static final ModConfigSpec.IntValue MAX_BUDGET;
    public static final ModConfigSpec.BooleanValue ALLOW_ADMIN_SHOPS;
    public static final ModConfigSpec.BooleanValue NAMES_ENABLED;

    // --- Dynamic Pricing ---
    public static final ModConfigSpec.BooleanValue DYNAMIC_PRICING_ENABLED;
    public static final ModConfigSpec.DoubleValue DYNAMIC_PRICING_DECAY_RATE;
    public static final ModConfigSpec.DoubleValue DYNAMIC_PRICING_MIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue DYNAMIC_PRICING_MAX_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue DYNAMIC_PRICING_SATURATION_PER_UNIT;
    public static final ModConfigSpec.DoubleValue DYNAMIC_PRICING_CRAFTING_BONUS;

    // --- Easter Egg: Rage Mode ---
    public static final ModConfigSpec.BooleanValue RAGE_MODE_ENABLED;

    static {
        BUILDER.push("Spawning");
        SPAWN_ENABLED = BUILDER
                .comment("Enable spawning of Wandering Trader NPCs that buy items from SingleOfferShops")
                .define("enabled", true);
        SPAWN_CHANCE = BUILDER
                .comment("Chance to spawn a trader per tick (1 in X). Default: 24000 (roughly once per Minecraft day)")
                .defineInRange("spawnChance", 24000, 100, Integer.MAX_VALUE);
        PREFER_DAYTIME_SPAWN = BUILDER
                .comment("If true, traders will only spawn during daytime (like the vanilla Wandering Trader)")
                .define("preferDaytime", true);
        SPAWN_NEAR_PLAYER_CHANCE_PERCENT = BUILDER
                .comment("Chance in percent that a spawning trader spawns near a random player instead of a shop. Default: 10")
                .defineInRange("spawnNearPlayerChancePercent", 10, 0, 100);
        MAX_PER_DIMENSION = BUILDER
                .comment("Maximum number of Shop Buyer NPCs that can exist simultaneously per dimension. Default: 3")
                .defineInRange("maxPerDimension", 3, 1, 20);
        BUILDER.pop();

        BUILDER.push("Behavior");
        DESPAWN_TICKS = BUILDER
                .comment("Time in ticks before a trader despawns. Default: 48000 (~40 minutes, same as vanilla Wandering Trader)")
                .defineInRange("despawnTicks", 48000, 1200, 240000);
        MAX_SHOPS_PER_VISIT = BUILDER
                .comment("Maximum number of shops a trader visits before leaving. Actual number is random between 1 and this value. Default: 4")
                .defineInRange("maxShopsPerVisit", 4, 1, 10);
        MIN_BUDGET = BUILDER
                .comment("Minimum budget value a spawned trader has. Default: 32")
                .defineInRange("minBudget", 32, 1, 1000000);
        MAX_BUDGET = BUILDER
                .comment("Maximum budget value a spawned trader has. Default: 8192")
                .defineInRange("maxBudget", 8192, 1, 1000000);
        ALLOW_ADMIN_SHOPS = BUILDER
                .comment("If true, traders can spawn near and purchase from SingleOfferShops that have Admin Mode enabled. Default: true")
                .define("allowAdminShops", true);
        NAMES_ENABLED = BUILDER
                .comment("Enable random names for Shop Buyer NPCs displayed above their heads.",
                        "Names can be customized in config/marketblocks/trader/trader_names.json")
                .define("customNames", true);
        BUILDER.pop();

        BUILDER.push("DynamicPricing");
        DYNAMIC_PRICING_ENABLED = BUILDER
                .comment("Enable dynamic pricing based on supply and demand for Shop Buyer NPC transactions.")
                .define("enabled", true);
        DYNAMIC_PRICING_DECAY_RATE = BUILDER
                .comment("Rate at which NPC market saturation decays per tick (e.g. 0.000005 reduces saturation by 0.000005 per tick).")
                .defineInRange("decayRate", 0.000005, 0.0, 1.0);
        DYNAMIC_PRICING_MIN_MULTIPLIER = BUILDER
                .comment("Minimum multiplier for dynamic NPC pricing (e.g. 0.1 = 10% of base value).")
                .defineInRange("minMultiplier", 0.1, 0.01, 10.0);
        DYNAMIC_PRICING_MAX_MULTIPLIER = BUILDER
                .comment("Maximum multiplier for dynamic NPC pricing (e.g. 1.0 = 100% of base value).")
                .defineInRange("maxMultiplier", 1.0, 0.1, 100.0);
        DYNAMIC_PRICING_SATURATION_PER_UNIT = BUILDER
                .comment("Saturation added to the item pool per unit sold to an NPC.")
                .defineInRange("saturationPerUnit", 0.005, 0.0, 10.0);
        DYNAMIC_PRICING_CRAFTING_BONUS = BUILDER
                .comment("Value multiplier bonus added per crafting step to reward processing (e.g. 0.1 = +10% value per step).")
                .defineInRange("craftingBonus", 0.1, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("RageMode");
        RAGE_MODE_ENABLED = BUILDER
                .comment("Enable the rage mode Easter Egg where the Shop Buyer attacks players who spam-click it.")
                .define("enabled", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
