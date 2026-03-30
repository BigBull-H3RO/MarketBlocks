package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

/**
 * Configurable dynamic pricing model that scales payment costs based on purchase demand.
 * <p>All field values are clamped to valid ranges in the constructor, ensuring
 * {@code minMultiplier ≤ maxMultiplier} and all multipliers are non-negative.</p>
 */
public final class DemandPricing {
    public static final Codec<DemandPricing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").orElse(false).forGetter(DemandPricing::enabled),
            Codec.DOUBLE.fieldOf("base_multiplier").orElse(1.0d).forGetter(DemandPricing::baseMultiplier),
            Codec.DOUBLE.fieldOf("demand_step").orElse(0.05d).forGetter(DemandPricing::demandStep),
            Codec.DOUBLE.fieldOf("min_multiplier").orElse(0.25d).forGetter(DemandPricing::minMultiplier),
            Codec.DOUBLE.fieldOf("max_multiplier").orElse(4.0d).forGetter(DemandPricing::maxMultiplier)
    ).apply(instance, DemandPricing::new));

    private final boolean enabled;
    private final double baseMultiplier;
    private final double demandStep;
    private final double minMultiplier;
    private final double maxMultiplier;

    public DemandPricing(boolean enabled, double baseMultiplier, double demandStep, double minMultiplier, double maxMultiplier) {
        this.enabled = enabled;
        this.baseMultiplier = Math.max(0.0d, baseMultiplier);
        this.demandStep = Math.max(0.0d, demandStep);
        this.minMultiplier = Math.max(0.0d, Math.min(minMultiplier, maxMultiplier));
        this.maxMultiplier = Math.max(this.minMultiplier, maxMultiplier);
    }

    public static DemandPricing disabled() {
        return new DemandPricing(false, 1.0d, 0.05d, 0.25d, 4.0d);
    }

    public boolean enabled() {
        return enabled;
    }

    public double baseMultiplier() {
        return baseMultiplier;
    }

    public double demandStep() {
        return demandStep;
    }

    public double minMultiplier() {
        return minMultiplier;
    }

    public double maxMultiplier() {
        return maxMultiplier;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DemandPricing other)) {
            return false;
        }
        return enabled == other.enabled
                && Double.compare(other.baseMultiplier, baseMultiplier) == 0
                && Double.compare(other.demandStep, demandStep) == 0
                && Double.compare(other.minMultiplier, minMultiplier) == 0
                && Double.compare(other.maxMultiplier, maxMultiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, baseMultiplier, demandStep, minMultiplier, maxMultiplier);
    }

    @Override
    public String toString() {
        return "DemandPricing{" +
                "enabled=" + enabled +
                ", baseMultiplier=" + baseMultiplier +
                ", demandStep=" + demandStep +
                ", minMultiplier=" + minMultiplier +
                ", maxMultiplier=" + maxMultiplier +
                '}';
    }
}