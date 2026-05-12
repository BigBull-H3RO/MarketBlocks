package de.bigbull.marketblocks.feature.marketplace.data;

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

    /**
     * Calculates the price multiplier based on current demand.
     * Returns {@code 1.0} when demand pricing is disabled or {@code pricing} is {@code null}.
     *
     * @param pricing         the pricing configuration; may be {@code null}
     * @param demandPurchases the number of purchases tracked for demand purposes
     * @return the clamped price multiplier in the range [{@code minMultiplier}, {@code maxMultiplier}]
     */
    public static double computeDemandMultiplier(DemandPricing pricing, int demandPurchases) {
        if (pricing == null || !pricing.enabled()) {
            return 1.0d;
        }
        double raw = pricing.baseMultiplier() + (Math.max(0, demandPurchases) * pricing.demandStep());
        return Math.max(pricing.minMultiplier(), Math.min(pricing.maxMultiplier(), raw));
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
     * Applies daily decay to the demand purchase counter based on elapsed in-game days.
     *
     * @param currentDemandPurchases the current accumulated demand purchase count
     * @param lastDecayDay           the in-game day when decay was last applied (0 or negative means "now")
     * @param currentDay             the current in-game day
     * @param decayPerDay            purchases to subtract per elapsed day; 0 disables decay
     * @return the reduced demand purchase count, clamped to {@code ≥ 0}
     */
    public static int computeDemandPurchasesAfterDailyDecay(int currentDemandPurchases, long lastDecayDay, long currentDay, int decayPerDay) {
        int normalizedDemand = Math.max(0, currentDemandPurchases);
        long normalizedCurrentDay = Math.max(0L, currentDay);
        long normalizedLastDecayDay = lastDecayDay <= 0L ? normalizedCurrentDay : Math.min(lastDecayDay, normalizedCurrentDay);
        int normalizedDecayPerDay = Math.max(0, decayPerDay);
        if (normalizedDecayPerDay == 0) {
            return normalizedDemand;
        }

        long elapsedDays = normalizedCurrentDay - normalizedLastDecayDay;
        if (elapsedDays <= 0L) {
            return normalizedDemand;
        }
        long totalDecay = elapsedDays * (long) normalizedDecayPerDay;
        return (int) Math.max(0L, normalizedDemand - totalDecay);
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
     * Returns the seconds until the next restock tick, or {@link java.util.Optional#empty()} when
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
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
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
