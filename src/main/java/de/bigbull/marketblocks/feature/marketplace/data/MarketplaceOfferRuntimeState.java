package de.bigbull.marketblocks.feature.marketplace.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.*;

/**
 * Persistent runtime state of a marketplace offer.
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
            Codec.DOUBLE.fieldOf("temperature").orElse(0.0).forGetter(MarketplaceOfferRuntimeState::temperature),
            Codec.LONG.fieldOf("last_temperature_update_game_time").orElse(0L).forGetter(MarketplaceOfferRuntimeState::lastTemperatureUpdateGameTime),
            Codec.INT.fieldOf("lifetime_purchases").orElse(0).forGetter(MarketplaceOfferRuntimeState::lifetimePurchases),
            PLAYER_DAILY_PURCHASE_CODEC.listOf().optionalFieldOf("player_purchases", Collections.emptyList()).forGetter(MarketplaceOfferRuntimeState::playerPurchasesForCodec),
            Codec.DOUBLE.optionalFieldOf("sale_percent").forGetter(MarketplaceOfferRuntimeState::salePercent),
            Codec.LONG.optionalFieldOf("sale_end_timestamp", 0L).forGetter(MarketplaceOfferRuntimeState::saleEndTimestamp)
    ).apply(instance, (stockRemaining, purchasedToday, lastDailyResetDay, lastRestockGameTime, temperature, lastTemperatureUpdateGameTime, lifetimePurchases, playerPurchases, salePercent, saleEndTimestamp) ->
            new MarketplaceOfferRuntimeState(
                    stockRemaining.orElse(null),
                    purchasedToday,
                    lastDailyResetDay,
                    lastRestockGameTime,
                    temperature,
                    lastTemperatureUpdateGameTime,
                    lifetimePurchases,
                    toPlayerPurchaseMap(playerPurchases),
                    salePercent.orElse(null),
                    saleEndTimestamp)));

    private final Optional<Integer> stockRemaining;
    private final int purchasedTodayGlobal;
    private final long lastDailyResetDay;
    private final long lastRestockGameTime;
    private final double temperature;
    private final long lastTemperatureUpdateGameTime;
    private final int lifetimePurchases;
    private final Map<UUID, Integer> purchasedTodayByPlayer;
    private final Optional<Double> salePercent;
    private final long saleEndTimestamp;

    public MarketplaceOfferRuntimeState(Integer stockRemaining, int purchasedTodayGlobal, long lastDailyResetDay, long lastRestockGameTime,
                                       double temperature) {
        this(stockRemaining, purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, temperature, 0L, 0, Collections.emptyMap(), null, 0L);
    }

    public MarketplaceOfferRuntimeState(Integer stockRemaining, int purchasedTodayGlobal, long lastDailyResetDay, long lastRestockGameTime,
                                       double temperature, long lastTemperatureUpdateGameTime, int lifetimePurchases, Map<UUID, Integer> purchasedTodayByPlayer) {
        this(stockRemaining, purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, temperature, lastTemperatureUpdateGameTime, lifetimePurchases, purchasedTodayByPlayer, null, 0L);
    }

    public MarketplaceOfferRuntimeState(Integer stockRemaining, int purchasedTodayGlobal, long lastDailyResetDay, long lastRestockGameTime,
                                       double temperature, long lastTemperatureUpdateGameTime, int lifetimePurchases, Map<UUID, Integer> purchasedTodayByPlayer, Double salePercent, long saleEndTimestamp) {
        this.stockRemaining = Optional.ofNullable(stockRemaining).filter(value -> value >= 0);
        this.purchasedTodayGlobal = Math.max(0, purchasedTodayGlobal);
        this.lastDailyResetDay = Math.max(0L, lastDailyResetDay);
        this.lastRestockGameTime = Math.max(0L, lastRestockGameTime);
        this.temperature = Math.max(-1.0, Math.min(1.0, temperature));
        this.lastTemperatureUpdateGameTime = Math.max(0L, lastTemperatureUpdateGameTime);
        this.lifetimePurchases = Math.max(0, lifetimePurchases);
        this.purchasedTodayByPlayer = sanitizePlayerPurchases(purchasedTodayByPlayer);
        this.salePercent = Optional.ofNullable(salePercent);
        this.saleEndTimestamp = Math.max(0L, saleEndTimestamp);
    }

    public static MarketplaceOfferRuntimeState empty() {
        return new MarketplaceOfferRuntimeState(null, 0, 0L, 0L, 0.0, 0L, 0, Collections.emptyMap(), null, 0L);
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

    public double temperature() {
        return temperature;
    }

    public long lastTemperatureUpdateGameTime() {
        return lastTemperatureUpdateGameTime;
    }

    public int lifetimePurchases() {
        return lifetimePurchases;
    }

    public Optional<Double> salePercent() {
        return salePercent;
    }

    public long saleEndTimestamp() {
        return saleEndTimestamp;
    }

    public boolean isSaleActive() {
        if (salePercent.isEmpty()) {
            return false;
        }
        if (saleEndTimestamp <= 0L) {
            return true;
        }
        return System.currentTimeMillis() < saleEndTimestamp;
    }

    public MarketplaceOfferRuntimeState withStockRemaining(Integer stockRemaining) {
        return toBuilder().stockRemaining(stockRemaining).build();
    }

    public MarketplaceOfferRuntimeState withPurchasedTodayGlobal(int purchasedTodayGlobal) {
        return toBuilder().purchasedTodayGlobal(purchasedTodayGlobal).build();
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
        return toBuilder().purchasedTodayByPlayer(updated).build();
    }

    public MarketplaceOfferRuntimeState withClearedPlayerPurchases() {
        return toBuilder().purchasedTodayByPlayer(Collections.emptyMap()).build();
    }

    public MarketplaceOfferRuntimeState withLastDailyResetDay(long lastDailyResetDay) {
        return toBuilder().lastDailyResetDay(lastDailyResetDay).build();
    }

    public MarketplaceOfferRuntimeState withLastRestockGameTime(long lastRestockGameTime) {
        return toBuilder().lastRestockGameTime(lastRestockGameTime).build();
    }

    public MarketplaceOfferRuntimeState withTemperature(double temperature) {
        return toBuilder().temperature(temperature).build();
    }

    public MarketplaceOfferRuntimeState withLastTemperatureUpdateGameTime(long lastTemperatureUpdateGameTime) {
        return toBuilder().lastTemperatureUpdateGameTime(lastTemperatureUpdateGameTime).build();
    }

    public MarketplaceOfferRuntimeState withLifetimePurchases(int lifetimePurchases) {
        return toBuilder().lifetimePurchases(lifetimePurchases).build();
    }

    public MarketplaceOfferRuntimeState withSale(Double salePercent, long saleEndTimestamp) {
        return toBuilder().salePercent(salePercent).saleEndTimestamp(saleEndTimestamp).build();
    }

    /**
     * Creates a mutable builder initialized with the current state.
     * Use this for batch updates to avoid intermediate object allocation.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Mutable builder for {@link MarketplaceOfferRuntimeState}.
     * Allows chaining multiple mutations before creating a single new immutable instance.
     */
    public static final class Builder {
        private Integer stockRemaining;
        private int purchasedTodayGlobal;
        private long lastDailyResetDay;
        private long lastRestockGameTime;
        private double temperature;
        private long lastTemperatureUpdateGameTime;
        private int lifetimePurchases;
        private Map<UUID, Integer> purchasedTodayByPlayer;
        private Double salePercent;
        private long saleEndTimestamp;

        private Builder(MarketplaceOfferRuntimeState source) {
            this.stockRemaining = source.stockRemaining.orElse(null);
            this.purchasedTodayGlobal = source.purchasedTodayGlobal;
            this.lastDailyResetDay = source.lastDailyResetDay;
            this.lastRestockGameTime = source.lastRestockGameTime;
            this.temperature = source.temperature;
            this.lastTemperatureUpdateGameTime = source.lastTemperatureUpdateGameTime;
            this.lifetimePurchases = source.lifetimePurchases;
            this.purchasedTodayByPlayer = source.purchasedTodayByPlayer;
            this.salePercent = source.salePercent.orElse(null);
            this.saleEndTimestamp = source.saleEndTimestamp;
        }

        public Builder stockRemaining(Integer stockRemaining) { this.stockRemaining = stockRemaining; return this; }
        public Builder purchasedTodayGlobal(int v) { this.purchasedTodayGlobal = v; return this; }
        public Builder lastDailyResetDay(long v) { this.lastDailyResetDay = v; return this; }
        public Builder lastRestockGameTime(long v) { this.lastRestockGameTime = v; return this; }
        public Builder temperature(double v) { this.temperature = v; return this; }
        public Builder lastTemperatureUpdateGameTime(long v) { this.lastTemperatureUpdateGameTime = v; return this; }
        public Builder lifetimePurchases(int v) { this.lifetimePurchases = v; return this; }
        public Builder purchasedTodayByPlayer(Map<UUID, Integer> v) { this.purchasedTodayByPlayer = v; return this; }
        public Builder salePercent(Double v) { this.salePercent = v; return this; }
        public Builder saleEndTimestamp(long v) { this.saleEndTimestamp = v; return this; }

        public MarketplaceOfferRuntimeState build() {
            return new MarketplaceOfferRuntimeState(stockRemaining, purchasedTodayGlobal, lastDailyResetDay,
                    lastRestockGameTime, temperature, lastTemperatureUpdateGameTime, lifetimePurchases,
                    purchasedTodayByPlayer, salePercent, saleEndTimestamp);
        }
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
                && Double.compare(temperature, other.temperature) == 0
                && lastTemperatureUpdateGameTime == other.lastTemperatureUpdateGameTime
                && lifetimePurchases == other.lifetimePurchases
                && saleEndTimestamp == other.saleEndTimestamp
                && Objects.equals(stockRemaining, other.stockRemaining)
                && Objects.equals(purchasedTodayByPlayer, other.purchasedTodayByPlayer)
                && Objects.equals(salePercent, other.salePercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockRemaining, purchasedTodayGlobal, lastDailyResetDay, lastRestockGameTime, temperature, lastTemperatureUpdateGameTime, lifetimePurchases, purchasedTodayByPlayer, salePercent, saleEndTimestamp);
    }

    private record PlayerDailyPurchase(UUID playerId, int purchasedToday) {
    }
}

