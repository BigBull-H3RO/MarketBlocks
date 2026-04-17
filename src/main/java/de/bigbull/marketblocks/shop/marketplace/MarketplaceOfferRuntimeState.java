package de.bigbull.marketblocks.shop.marketplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.*;

/**
 * Persistenter Laufzeitstatus eines Marktplatz-Angebots.
 */
public final class MarketplaceOfferRuntimeState {
    private static final Codec<PlayerDailyPurchase> PLAYER_DAILY_PURCHASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player_id").forGetter(PlayerDailyPurchase::playerId),
            Codec.INT.fieldOf("purchased_today").forGetter(PlayerDailyPurchase::purchasedToday)
    ).apply(instance, PlayerDailyPurchase::new));

    public static final Codec<MarketplaceOfferRuntimeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("stock_remaining").forGetter(MarketplaceOfferRuntimeState::stockRemaining),
            Codec.INT.fieldOf("purchased_today").orElse(0).forGetter(MarketplaceOfferRuntimeState::purchasedTodayGlobal),
            Codec.LONG.fieldOf("last_daily_reset_day").orElse(0L).forGetter(MarketplaceOfferRuntimeState::lastDailyResetDay),
            Codec.LONG.fieldOf("last_restock_game_time").orElse(0L).forGetter(MarketplaceOfferRuntimeState::lastRestockGameTime),
            Codec.INT.fieldOf("demand_purchases").orElse(0).forGetter(MarketplaceOfferRuntimeState::demandPurchases),
            Codec.LONG.fieldOf("last_demand_decay_day").orElse(0L).forGetter(MarketplaceOfferRuntimeState::lastDemandDecayDay),
            PLAYER_DAILY_PURCHASE_CODEC.listOf().optionalFieldOf("player_purchases", Collections.emptyList()).forGetter(MarketplaceOfferRuntimeState::playerPurchasesForCodec)
    ).apply(instance, (stockRemaining, purchasedToday, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, playerPurchases) ->
            new MarketplaceOfferRuntimeState(
                    stockRemaining.orElse(null),
                    purchasedToday,
                    lastDailyResetDay,
                    lastRestockGameTime,
                    demandPurchases,
                    lastDemandDecayDay,
                    toPlayerPurchaseMap(playerPurchases))));

    private final Optional<Integer> stockRemaining;
    private final int purchasedTodayGlobal;
    private final long lastDailyResetDay;
    private final long lastRestockGameTime;
    private final int demandPurchases;
    private final long lastDemandDecayDay;
    private final Map<UUID, Integer> purchasedTodayByPlayer;

    public MarketplaceOfferRuntimeState(Integer stockRemaining, int purchasedTodayGlobal, long lastDailyResetDay, long lastRestockGameTime,
                                       int demandPurchases) {
        this(stockRemaining, purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, 0L, Collections.emptyMap());
    }

    public MarketplaceOfferRuntimeState(Integer stockRemaining, int purchasedTodayGlobal, long lastDailyResetDay, long lastRestockGameTime,
                                       int demandPurchases, long lastDemandDecayDay, Map<UUID, Integer> purchasedTodayByPlayer) {
        this.stockRemaining = Optional.ofNullable(stockRemaining).filter(value -> value >= 0);
        this.purchasedTodayGlobal = Math.max(0, purchasedTodayGlobal);
        this.lastDailyResetDay = Math.max(0L, lastDailyResetDay);
        this.lastRestockGameTime = Math.max(0L, lastRestockGameTime);
        this.demandPurchases = Math.max(0, demandPurchases);
        this.lastDemandDecayDay = Math.max(0L, lastDemandDecayDay);
        this.purchasedTodayByPlayer = sanitizePlayerPurchases(purchasedTodayByPlayer);
    }

    public static MarketplaceOfferRuntimeState empty() {
        return new MarketplaceOfferRuntimeState(null, 0, 0L, 0L, 0, 0L, Collections.emptyMap());
    }

    public Optional<Integer> stockRemaining() {
        return stockRemaining;
    }

    public int purchasedTodayGlobal() {
        return purchasedTodayGlobal;
    }

    public int purchasedTodayForPlayer(UUID playerId) {
        if (playerId == null) {
            return 0;
        }
        return purchasedTodayByPlayer.getOrDefault(playerId, 0);
    }

    public Map<UUID, Integer> purchasedTodayByPlayer() {
        return Collections.unmodifiableMap(purchasedTodayByPlayer);
    }

    public long lastDailyResetDay() {
        return lastDailyResetDay;
    }

    public long lastRestockGameTime() {
        return lastRestockGameTime;
    }

    public int demandPurchases() {
        return demandPurchases;
    }

    public long lastDemandDecayDay() {
        return lastDemandDecayDay;
    }

    public MarketplaceOfferRuntimeState withStockRemaining(Integer stockRemaining) {
        return new MarketplaceOfferRuntimeState(stockRemaining, purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    public MarketplaceOfferRuntimeState withPurchasedTodayGlobal(int purchasedTodayGlobal) {
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    public MarketplaceOfferRuntimeState withPurchasedTodayForPlayer(UUID playerId, int purchasedToday) {
        Map<UUID, Integer> updated = new HashMap<>(purchasedTodayByPlayer);
        if (playerId != null) {
            if (purchasedToday <= 0) {
                updated.remove(playerId);
            } else {
                updated.put(playerId, purchasedToday);
            }
        }
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, updated);
    }

    public MarketplaceOfferRuntimeState withClearedPlayerPurchases() {
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, Collections.emptyMap());
    }

    public MarketplaceOfferRuntimeState withLastDailyResetDay(long lastDailyResetDay) {
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    public MarketplaceOfferRuntimeState withLastRestockGameTime(long lastRestockGameTime) {
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    public MarketplaceOfferRuntimeState withDemandPurchases(int demandPurchases) {
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    public MarketplaceOfferRuntimeState withLastDemandDecayDay(long lastDemandDecayDay) {
        return new MarketplaceOfferRuntimeState(stockRemaining.orElse(null), purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    private List<PlayerDailyPurchase> playerPurchasesForCodec() {
        return purchasedTodayByPlayer.entrySet().stream()
                .map(entry -> new PlayerDailyPurchase(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PlayerDailyPurchase::playerId))
                .toList();
    }

    private static Map<UUID, Integer> toPlayerPurchaseMap(List<PlayerDailyPurchase> entries) {
        Map<UUID, Integer> result = new HashMap<>();
        if (entries != null) {
            for (PlayerDailyPurchase entry : entries) {
                if (entry != null && entry.playerId() != null && entry.purchasedToday() > 0) {
                    result.put(entry.playerId(), entry.purchasedToday());
                }
            }
        }
        return result;
    }

    private static Map<UUID, Integer> sanitizePlayerPurchases(Map<UUID, Integer> purchasedTodayByPlayer) {
        Map<UUID, Integer> sanitized = new HashMap<>();
        if (purchasedTodayByPlayer != null) {
            purchasedTodayByPlayer.forEach((playerId, purchasedToday) -> {
                if (playerId != null && purchasedToday != null && purchasedToday > 0) {
                    sanitized.put(playerId, purchasedToday);
                }
            });
        }
        return sanitized;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MarketplaceOfferRuntimeState other)) {
            return false;
        }
        return purchasedTodayGlobal == other.purchasedTodayGlobal
                && lastDailyResetDay == other.lastDailyResetDay
                && lastRestockGameTime == other.lastRestockGameTime
                && demandPurchases == other.demandPurchases
                && lastDemandDecayDay == other.lastDemandDecayDay
                && Objects.equals(stockRemaining, other.stockRemaining)
                && Objects.equals(purchasedTodayByPlayer, other.purchasedTodayByPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockRemaining, purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, demandPurchases, lastDemandDecayDay, purchasedTodayByPlayer);
    }

    private record PlayerDailyPurchase(UUID playerId, int purchasedToday) {
    }
}

