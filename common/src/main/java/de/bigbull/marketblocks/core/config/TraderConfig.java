package de.bigbull.marketblocks.core.config;

import de.bigbull.marketblocks.core.config.toml.TomlConfigBuilder;
import de.bigbull.marketblocks.core.config.toml.TomlConfigSpec;
import de.bigbull.marketblocks.core.config.toml.TomlConfigValue;

public class TraderConfig {
    public static final TomlConfigBuilder BUILDER = new TomlConfigBuilder();
    public static final TomlConfigSpec SPEC;

    // --- Spawning ---
    public static final TomlConfigValue.BooleanValue SPAWN_ENABLED;
    public static final TomlConfigValue.IntValue SPAWN_COOLDOWN_TICKS;
    public static final TomlConfigValue.IntValue SPAWN_CHANCE_PERCENT;
    public static final TomlConfigValue.BooleanValue PREFER_DAYTIME_SPAWN;
    public static final TomlConfigValue.IntValue MAX_PER_DIMENSION;
    public static final TomlConfigValue.IntValue SHOP_DETECTION_RADIUS;

    // --- Behavior ---
    public static final TomlConfigValue.IntValue DESPAWN_TICKS;
    public static final TomlConfigValue.IntValue MAX_SHOPS_PER_VISIT;
    public static final TomlConfigValue.IntValue MIN_BUDGET;
    public static final TomlConfigValue.IntValue MAX_BUDGET;
    public static final TomlConfigValue.BooleanValue ALLOW_ADMIN_SHOPS;
    public static final TomlConfigValue.BooleanValue NAMES_ENABLED;

    // --- Dynamic Pricing ---
    public static final TomlConfigValue.BooleanValue DYNAMIC_PRICING_ENABLED;
    public static final TomlConfigValue.DoubleValue DYNAMIC_PRICING_DECAY_RATE;
    public static final TomlConfigValue.DoubleValue DYNAMIC_PRICING_MIN_MULTIPLIER;
    public static final TomlConfigValue.DoubleValue DYNAMIC_PRICING_MAX_MULTIPLIER;
    public static final TomlConfigValue.DoubleValue DYNAMIC_PRICING_SATURATION_PER_UNIT;
    public static final TomlConfigValue.DoubleValue DYNAMIC_PRICING_CRAFTING_BONUS;

    // --- Easter Egg: Rage Mode ---
    public static final TomlConfigValue.BooleanValue RAGE_MODE_ENABLED;

    static {
        BUILDER.push("Spawning");
        SPAWN_ENABLED = BUILDER
                .comment("Enable spawning of Wandering Trader NPCs that buy items from SingleOfferShops")
                .define("enabled", true);
        SPAWN_COOLDOWN_TICKS = BUILDER
                .comment("Cooldown in ticks per player before another trader can visit their shops (24000 = 1 full Minecraft day). Default: 24000")
                .defineInRange("spawnCooldownTicks", 24000, 1200, 240000);
        SPAWN_CHANCE_PERCENT = BUILDER
                .comment("Chance in percent every minute (1200 ticks) to spawn a trader for an eligible player after cooldown. Default: 25")
                .defineInRange("spawnChancePercent", 25, 1, 100);
        PREFER_DAYTIME_SPAWN = BUILDER
                .comment("If true, traders will only spawn during daytime (like the vanilla Wandering Trader)")
                .define("preferDaytime", true);
        MAX_PER_DIMENSION = BUILDER
                .comment("Maximum number of Shop Buyer NPCs that can exist simultaneously per dimension. Default: 4")
                .defineInRange("maxPerDimension", 4, 1, 20);
        SHOP_DETECTION_RADIUS = BUILDER
                .comment("Radius in blocks around a player to search for active shops with offers before spawning. Default: 64")
                .defineInRange("shopDetectionRadius", 64, 16, 256);
        BUILDER.pop();

        BUILDER.push("Behavior");
        DESPAWN_TICKS = BUILDER
                .comment("Time in ticks before a trader despawns. Default: 48000 (~40 minutes, same as vanilla Wandering Trader)")
                .defineInRange("despawnTicks", 48000, 6000, 120000);
        MAX_SHOPS_PER_VISIT = BUILDER
                .comment("Maximum number of distinct shops a trader will visit before despawning. Default: 5")
                .defineInRange("maxShopsPerVisit", 5, 1, 20);
        MIN_BUDGET = BUILDER
                .comment("Minimum emerald budget a trader spawns with. Default: 16")
                .defineInRange("minBudget", 16, 1, 1024);
        MAX_BUDGET = BUILDER
                .comment("Maximum emerald budget a trader spawns with. Default: 128")
                .defineInRange("maxBudget", 128, 16, 4096);
        ALLOW_ADMIN_SHOPS = BUILDER
                .comment("Whether traders are allowed to buy from Admin Shops (infinite supply). Default: true")
                .define("allowAdminShops", true);
        NAMES_ENABLED = BUILDER
                .comment("Give traders randomized medieval trader names with custom nametags. Default: true")
                .define("namesEnabled", true);
        BUILDER.pop();

        BUILDER.push("DynamicPricing");
        DYNAMIC_PRICING_ENABLED = BUILDER
                .comment("Enable dynamic NPC pricing based on item market saturation (supply/demand). Default: true")
                .define("enabled", true);
        DYNAMIC_PRICING_DECAY_RATE = BUILDER
                .comment("Decay rate per hour (reduces saturation over time, 0.05 = 5% decay/hour). Default: 0.05")
                .defineInRange("decayRatePerHour", 0.05, 0.0, 1.0);
        DYNAMIC_PRICING_MIN_MULTIPLIER = BUILDER
                .comment("Minimum price multiplier when market is completely saturated (0.25 = 25% of base price). Default: 0.25")
                .defineInRange("minMultiplier", 0.25, 0.05, 1.0);
        DYNAMIC_PRICING_MAX_MULTIPLIER = BUILDER
                .comment("Maximum price multiplier when item is in high demand (1.5 = 150% of base price). Default: 1.5")
                .defineInRange("maxMultiplier", 1.5, 1.0, 5.0);
        DYNAMIC_PRICING_SATURATION_PER_UNIT = BUILDER
                .comment("Saturation increase per unit sold (0.005 = 200 items sold gives 1.0 saturation). Default: 0.005")
                .defineInRange("saturationPerUnit", 0.005, 0.0001, 0.1);
        DYNAMIC_PRICING_CRAFTING_BONUS = BUILDER
                .comment("Price bonus per crafting step for processed goods (0.10 = +10% per step). Default: 0.10")
                .defineInRange("craftingBonusPerStep", 0.10, 0.0, 0.50);
        BUILDER.pop();

        BUILDER.push("EasterEggs");
        RAGE_MODE_ENABLED = BUILDER
                .comment("If true, wandering traders get angry when provoked by players and fight back! Default: true")
                .define("rageModeEnabled", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}