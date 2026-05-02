package de.bigbull.marketblocks.feature.marketplace.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Configurable dynamic pricing model that scales payment costs based on purchase demand.
 * <p>All field values are clamped to valid ranges in the constructor, ensuring
 * {@code minMultiplier ≤ maxMultiplier} and all multipliers are non-negative.</p>
 */
public record DemandPricing(
        boolean enabled,
        double baseMultiplier,
        double demandStep,
        double minMultiplier,
        double maxMultiplier
) {
    public static final Codec<DemandPricing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").orElse(false).forGetter(DemandPricing::enabled),
            Codec.DOUBLE.fieldOf("base_multiplier").orElse(1.0d).forGetter(DemandPricing::baseMultiplier),
            Codec.DOUBLE.fieldOf("demand_step").orElse(0.05d).forGetter(DemandPricing::demandStep),
            Codec.DOUBLE.fieldOf("min_multiplier").orElse(0.25d).forGetter(DemandPricing::minMultiplier),
            Codec.DOUBLE.fieldOf("max_multiplier").orElse(4.0d).forGetter(DemandPricing::maxMultiplier)
    ).apply(instance, DemandPricing::new));

    public DemandPricing {
        baseMultiplier = Math.max(0.0d, baseMultiplier);
        demandStep = Math.max(0.0d, demandStep);
        minMultiplier = Math.max(0.0d, Math.min(minMultiplier, maxMultiplier));
        maxMultiplier = Math.max(minMultiplier, maxMultiplier);
    }

    public static DemandPricing disabled() {
        return new DemandPricing(false, 1.0d, 0.05d, 0.25d, 4.0d);
    }

    /** Returns a copy of this configuration with the {@code enabled} flag set to the given value. */
    public DemandPricing withEnabled(boolean enabled) {
        return new DemandPricing(enabled, baseMultiplier, demandStep, minMultiplier, maxMultiplier);
    }

    /** Returns a copy of this configuration with the demand step per purchase set to {@code step}. */
    public DemandPricing withDemandStep(double step) {
        return new DemandPricing(enabled, baseMultiplier, step, minMultiplier, maxMultiplier);
    }

    /** Returns a copy of this configuration with the multiplier bounds set to {@code min} and {@code max}. */
    public DemandPricing withMinMax(double min, double max) {
        return new DemandPricing(enabled, baseMultiplier, demandStep, min, max);
    }
}