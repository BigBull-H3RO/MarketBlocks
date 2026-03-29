package de.bigbull.marketblocks.util.custom.servershop;

import java.util.Optional;
import java.util.UUID;

/**
 * Pure Hilfsmethoden für Server-Shop-Limits, Restock und Preisberechnung.
 */
public final class ServerShopRuntimeMath {
    private static final long TICKS_PER_SECOND = 20L;

    private ServerShopRuntimeMath() {
    }

    public static double computeDemandMultiplier(DemandPricing pricing, int demandPurchases) {
        if (pricing == null || !pricing.enabled()) {
            return 1.0d;
        }
        double raw = pricing.baseMultiplier() + (Math.max(0, demandPurchases) * pricing.demandStep());
        return Math.max(pricing.minMultiplier(), Math.min(pricing.maxMultiplier(), raw));
    }

    public static int scalePaymentCount(int baseCount, double multiplier) {
        if (baseCount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(baseCount * Math.max(0.0d, multiplier)));
    }

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

    public static int computeRemainingPurchases(OfferLimit limit, ServerShopOfferRuntimeState state, UUID playerId, boolean globalDailyLimit) {
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

    public static int computeRemainingDailyPurchases(OfferLimit limit, ServerShopOfferRuntimeState state, UUID playerId, boolean globalDailyLimit) {
        if (limit == null || limit.isUnlimited() || limit.dailyLimit().isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int alreadyPurchased = globalDailyLimit
                ? state.purchasedTodayGlobal()
                : state.purchasedTodayForPlayer(playerId);
        return Math.max(0, limit.dailyLimit().get() - alreadyPurchased);
    }

    public static Optional<Integer> computeSecondsUntilNextRestock(OfferLimit limit, ServerShopOfferRuntimeState state, long currentGameTime) {
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

    public static String formatSecondsAsTimer(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int minutes = safeSeconds / 60;
        int seconds = safeSeconds % 60;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

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
