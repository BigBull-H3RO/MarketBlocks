package de.bigbull.marketblocks.feature.marketplace.data;

import java.util.Locale;

import java.util.Optional;
import java.util.UUID;

/**
 * Pure utility methods for demand-pricing calculations, limit tracking, and stock restocking.
 * <p>All methods are stateless and operate only on their input parameters.</p>
 */
public final class MarketplaceRuntimeMath {
    private static final long TICKS_PER_SECOND = 20L;

    private MarketplaceRuntimeMath() {
    }

    public static final double MIN_TEMPERATURE = -1.0;
    public static final double MAX_TEMPERATURE = 1.0;
    public static final double NEUTRAL_ZONE_MAX = 0.2;
    public static final double NEUTRAL_ZONE_MIN = -0.2;

    /**
     * Calculates the price multiplier based on current market temperature.
     * Returns {@code 1.0} when demand pricing is disabled or {@code pricing} is {@code null}.
     *
     * @param pricing     the pricing configuration; may be {@code null}
     * @param temperature the current market temperature [-1.0, 1.0]
     * @return the clamped price multiplier in the range [{@code minMultiplier}, {@code maxMultiplier}]
     */
    public static double computeDemandMultiplier(DemandPricing pricing, double temperature) {
        if (pricing == null || !pricing.enabled()) {
            return 1.0d;
        }

        double clampedTemp = Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, temperature));

        if (clampedTemp > NEUTRAL_ZONE_MAX) {
            double ratio = (clampedTemp - NEUTRAL_ZONE_MAX) / (MAX_TEMPERATURE - NEUTRAL_ZONE_MAX);
            return 1.0 + ratio * (pricing.maxMultiplier() - 1.0);
        } else if (clampedTemp < NEUTRAL_ZONE_MIN) {
            double ratio = (NEUTRAL_ZONE_MIN - clampedTemp) / (NEUTRAL_ZONE_MIN - MIN_TEMPERATURE);
            return 1.0 - ratio * (1.0 - pricing.minMultiplier());
        }

        return 1.0d;
    }

    /**
     * Scales a base item count by the given demand multiplier, rounding up, with a minimum of 1.
     *
     * @param baseCount  the unscaled item count; if {@code ≤ 0} returns 0
     * @param multiplier the price multiplier; negative values are treated as 0
     * @return the scaled count, always {@code ≥ 1} when {@code baseCount > 0}
     */
    public static int scalePaymentCount(int baseCount, double multiplier) {
        if (baseCount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(baseCount * Math.max(0.0d, multiplier)));
    }

    /**
     * Applies exponential decay to the market temperature based on elapsed real-world time.
     *
     * @param currentTemperature the current temperature [-1.0, 1.0]
     * @param volatility         the volatility setting
     * @param lastUpdateMs       the real-world timestamp when it was last updated
     * @param currentTimeMs      the current real-world timestamp
     * @return the decayed temperature
     */
    public static double computeTemperatureAfterTimeDecay(double currentTemperature, Volatility volatility, long lastUpdateMs, long currentTimeMs) {
        if (volatility == null) volatility = Volatility.NORMAL;

        long elapsedMs = Math.max(0L, currentTimeMs - lastUpdateMs);
        if (elapsedMs == 0L) return currentTemperature;

        double elapsedHours = elapsedMs / (1000.0 * 60.0 * 60.0);

        // Decay factor per hour
        double k = switch (volatility) {
            case SLOW -> 0.0041; // Half-life ~7 days
            case NORMAL -> 0.0096; // Half-life ~3 days
            case FAST -> 0.0289; // Half-life ~1 day
        };

        // Decay towards MIN_TEMPERATURE (-1.0)
        double target = MIN_TEMPERATURE;
        double newTemp = target + (currentTemperature - target) * Math.exp(-k * elapsedHours);

        return Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, newTemp));
    }

    /**
     * Increases the market temperature based on purchase amount.
     *
     * @param currentTemperature the current temperature
     * @param volatility         the volatility setting
     * @param amount             number of items/trades purchased
     * @return the heated temperature
     */
    public static double addPurchaseHeat(double currentTemperature, Volatility volatility, int amount) {
        if (volatility == null) volatility = Volatility.NORMAL;

        double heatPerPurchase = switch (volatility) {
            case SLOW -> 0.02;
            case NORMAL -> 0.05;
            case FAST -> 0.10;
        };

        double newTemp = currentTemperature + (heatPerPurchase * amount);
        return Math.min(MAX_TEMPERATURE, newTemp);
    }

    /**
     * Returns the number of remaining purchases a player (or globally) may make for this offer,
     * taking both stock and daily limits into account.
     *
     * @param limit            the configured offer limits; {@code null} or unlimited → {@link Integer#MAX_VALUE}
     * @param state            the current runtime state of the offer
     * @param playerId         the UUID of the purchasing player (used for per-player daily limits)
     * @param globalDailyLimit {@code true} to use the global daily counter instead of the per-player counter
     * @return remaining purchase count, clamped to {@code ≥ 0}
     */
    public static int computeRemainingPurchases(OfferLimit limit, MarketplaceOfferRuntimeState state, UUID playerId, boolean globalDailyLimit) {
        if (limit == null || limit.isUnlimited()) {
            return Integer.MAX_VALUE;
        }

        int remaining = Integer.MAX_VALUE;
        if (limit.stockLimit().isPresent()) {
            remaining = state.stockRemaining().orElse(limit.stockLimit().get());
        }
        if (limit.dailyLimit().isPresent()) {
            remaining = Math.min(remaining, computeRemainingDailyPurchases(limit, state, playerId, globalDailyLimit));
        }
        return Math.max(0, remaining);
    }

    /**
     * Returns the number of daily purchases already exhausted for this offer,
     * considering either the global or per-player daily counter.
     *
     * @param limit            the configured offer limits; {@code null} or unlimited → {@link Integer#MAX_VALUE}
     * @param state            the current runtime state of the offer
     * @param playerId         the UUID of the purchasing player
     * @param globalDailyLimit {@code true} to use the global daily counter instead of the per-player counter
     * @return remaining daily purchases, clamped to {@code ≥ 0}
     */
    public static int computeRemainingDailyPurchases(OfferLimit limit, MarketplaceOfferRuntimeState state, UUID playerId, boolean globalDailyLimit) {
        if (limit == null || limit.isUnlimited() || limit.dailyLimit().isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int alreadyPurchased = globalDailyLimit
                ? state.purchasedTodayGlobal()
                : state.purchasedTodayForPlayer(playerId);
        return Math.max(0, limit.dailyLimit().get() - alreadyPurchased);
    }

    /**
     * Returns the seconds until the next restock tick, or {@link Optional#empty()} when
     * the offer is at full stock, unlimited, or has no restock configuration.
     *
     * @param limit           the configured offer limits
     * @param state           the current runtime state of the offer
     * @param currentGameTime the current server game time in ticks
     */
    public static Optional<Integer> computeSecondsUntilNextRestock(OfferLimit limit, MarketplaceOfferRuntimeState state, long currentGameTime) {
        if (limit == null || limit.isUnlimited() || limit.stockLimit().isEmpty() || limit.restockSeconds().isEmpty()) {
            return Optional.empty();
        }

        int maxStock = limit.stockLimit().get();
        int currentStock = state.stockRemaining().orElse(maxStock);
        if (currentStock >= maxStock) {
            return Optional.empty();
        }

        long intervalTicks = Math.max(1L, limit.restockSeconds().get()) * TICKS_PER_SECOND;
        long baseline = state.lastRestockGameTime() <= 0L ? Math.max(0L, currentGameTime) : state.lastRestockGameTime();
        long elapsedTicks = Math.max(0L, currentGameTime - baseline);
        long remainingTicks = Math.max(0L, intervalTicks - elapsedTicks);
        return Optional.of((int) Math.ceil(remainingTicks / (double) TICKS_PER_SECOND));
    }

    /**
     * Formats a duration in seconds as a {@code MM:SS} string (e.g., {@code "02:35"}).
     *
     * @param totalSeconds total seconds to format; negative values are treated as 0
     */
    public static String formatSecondsAsTimer(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    /**
     * Applies periodic stock replenishment and returns a {@link RestockResult} describing the new state.
     *
     * @param currentStock        the current stock level
     * @param maxStock            the maximum stock capacity
     * @param lastRestockGameTime game time (ticks) of the most recent restock; {@code ≤ 0} means "now"
     * @param currentGameTime     the current game time in ticks
     * @param restockSeconds      the restock interval in seconds
     * @return a {@link RestockResult} with the updated stock and last-restock time, plus a changed flag
     */
    public static RestockResult applyRestock(int currentStock, int maxStock, long lastRestockGameTime, long currentGameTime, int restockSeconds) {
        int normalizedCurrent = Math.max(0, Math.min(currentStock, maxStock));
        long normalizedCurrentTime = Math.max(0L, currentGameTime);
        long normalizedLastRestock = lastRestockGameTime <= 0L ? normalizedCurrentTime : Math.min(lastRestockGameTime, normalizedCurrentTime);
        long intervalTicks = Math.max(1L, restockSeconds) * TICKS_PER_SECOND;

        if (normalizedCurrent >= maxStock) {
            return new RestockResult(maxStock, normalizedCurrentTime, normalizedLastRestock != normalizedCurrentTime);
        }

        long elapsedTicks = Math.max(0L, normalizedCurrentTime - normalizedLastRestock);
        long refillSteps = elapsedTicks / intervalTicks;
        if (refillSteps <= 0L) {
            return new RestockResult(normalizedCurrent, normalizedLastRestock, normalizedLastRestock != lastRestockGameTime);
        }

        int replenishedStock = (int) Math.min(maxStock, normalizedCurrent + refillSteps);
        long newLastRestock = normalizedLastRestock + (refillSteps * intervalTicks);
        return new RestockResult(replenishedStock, newLastRestock, replenishedStock != currentStock || newLastRestock != lastRestockGameTime);
    }

    public record RestockResult(int stockRemaining, long lastRestockGameTime, boolean changed) {
    }
}

