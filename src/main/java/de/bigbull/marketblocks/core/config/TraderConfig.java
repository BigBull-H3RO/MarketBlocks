package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TraderConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_TRADER_SPAWNING;
    public static final ModConfigSpec.IntValue TRADER_SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue TRADER_SPAWN_NEAR_PLAYER_CHANCE_PERCENT;
    public static final ModConfigSpec.IntValue TRADER_MIN_BUDGET;
    public static final ModConfigSpec.IntValue TRADER_MAX_BUDGET;
    public static final ModConfigSpec.IntValue TRADER_DESPAWN_TICKS;
    public static final ModConfigSpec.IntValue TRADER_MAX_PER_DIMENSION;
    public static final ModConfigSpec.IntValue TRADER_MAX_SHOPS_PER_VISIT;
    public static final ModConfigSpec.BooleanValue TRADER_NAMES_ENABLED;
    public static final ModConfigSpec.BooleanValue TRADER_PREFER_DAYTIME_SPAWN;
    public static final ModConfigSpec.BooleanValue TRADER_ALLOW_ADMIN_SHOPS;
    public static final ModConfigSpec.BooleanValue TRADER_DYNAMIC_PRICING_ENABLED;
    public static final ModConfigSpec.DoubleValue TRADER_DYNAMIC_PRICING_DECAY_RATE;
    public static final ModConfigSpec.DoubleValue TRADER_DYNAMIC_PRICING_MIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue TRADER_DYNAMIC_PRICING_MAX_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue TRADER_DYNAMIC_PRICING_SATURATION_PER_UNIT;
    public static final ModConfigSpec.DoubleValue TRADER_DYNAMIC_PRICING_CRAFTING_BONUS;

    static {
        BUILDER.push("Wandering Trader NPC");
        ENABLE_TRADER_SPAWNING = BUILDER
                .comment("Enable spawning of Wandering Trader NPCs that buy items from SingleOfferShops")
                .define("enableTraderSpawning", true);
        TRADER_SPAWN_CHANCE = BUILDER
                .comment("Chance to spawn a trader per tick (1 in X). Default: 24000 (roughly once per Minecraft day)")
                .defineInRange("traderSpawnChance", 24000, 100, Integer.MAX_VALUE);
        TRADER_SPAWN_NEAR_PLAYER_CHANCE_PERCENT = BUILDER
                .comment("Chance in percent that a spawning trader spawns near a random player instead of a shop. Default: 10")
                .defineInRange("traderSpawnNearPlayerChancePercent", 10, 0, 100);
        TRADER_MIN_BUDGET = BUILDER
                .comment("Minimum budget value a spawned trader has")
                .defineInRange("traderMinBudget", 32, 1, 1000000);
        TRADER_MAX_BUDGET = BUILDER
                .comment("Maximum budget value a spawned trader has")
                .defineInRange("traderMaxBudget", 8192, 1, 1000000);
        TRADER_DESPAWN_TICKS = BUILDER
                .comment("Time in ticks before a trader despawns. Default: 48000 (~40 minutes, same as vanilla Wandering Trader)")
                .defineInRange("traderDespawnTicks", 48000, 1200, 240000);
        TRADER_MAX_PER_DIMENSION = BUILDER
                .comment("Maximum number of Shop Buyer NPCs that can exist simultaneously per dimension. Default: 3")
                .defineInRange("traderMaxPerDimension", 3, 1, 20);
        TRADER_MAX_SHOPS_PER_VISIT = BUILDER
                .comment("Maximum number of shops a trader visits before leaving. Actual number is random between 1 and this value. Default: 4")
                .defineInRange("traderMaxShopsPerVisit", 4, 1, 10);
        TRADER_NAMES_ENABLED = BUILDER
                .comment("Enable random names for Shop Buyer NPCs displayed above their heads.",
                        "Names can be customized in config/marketblocks/trader_names.json")
                .define("traderNamesEnabled", true);
        TRADER_PREFER_DAYTIME_SPAWN = BUILDER
                .comment("If true, traders will only spawn during daytime (like the vanilla Wandering Trader)")
                .define("traderPreferDaytimeSpawn", true);
        TRADER_ALLOW_ADMIN_SHOPS = BUILDER
                .comment("If true, traders can spawn near and purchase from SingleOfferShops that have Admin Mode enabled. Default: true")
                .define("traderAllowAdminShops", true);
        TRADER_DYNAMIC_PRICING_ENABLED = BUILDER
                .comment("Enable dynamic pricing based on supply and demand for Shop Buyer NPC transactions.")
                .define("traderDynamicPricingEnabled", true);
        TRADER_DYNAMIC_PRICING_DECAY_RATE = BUILDER
                .comment("Rate at which NPC market saturation decays per tick (e.g. 0.000005 reduces saturation by 0.000005 per tick).")
                .defineInRange("traderDynamicPricingDecayRate", 0.000005, 0.0, 1.0);
        TRADER_DYNAMIC_PRICING_MIN_MULTIPLIER = BUILDER
                .comment("Minimum multiplier for dynamic NPC pricing (e.g. 0.1 = 10% of base value).")
                .defineInRange("traderDynamicPricingMinMultiplier", 0.1, 0.01, 10.0);
        TRADER_DYNAMIC_PRICING_MAX_MULTIPLIER = BUILDER
                .comment("Maximum multiplier for dynamic NPC pricing (e.g. 1.0 = 100% of base value).")
                .defineInRange("traderDynamicPricingMaxMultiplier", 1.0, 0.1, 100.0);
        TRADER_DYNAMIC_PRICING_SATURATION_PER_UNIT = BUILDER
                .comment("Saturation added to the item pool per unit sold to an NPC.")
                .defineInRange("traderDynamicPricingSaturationPerUnit", 0.005, 0.0, 10.0);
        TRADER_DYNAMIC_PRICING_CRAFTING_BONUS = BUILDER
                .comment("Value multiplier bonus added per crafting step to reward processing (e.g. 0.1 = +10% value per step).")
                .defineInRange("traderDynamicPricingCraftingBonus", 0.1, 0.0, 1.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
