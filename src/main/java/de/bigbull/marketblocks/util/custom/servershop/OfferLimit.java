package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * Konfigurierbare Limits für Server-Shop-Angebote.
 */
public final class OfferLimit {
    public static final Codec<OfferLimit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("unlimited").orElse(true).forGetter(OfferLimit::isUnlimited),
            Codec.INT.optionalFieldOf("daily_limit").forGetter(OfferLimit::dailyLimit),
            Codec.INT.optionalFieldOf("stock_limit").forGetter(OfferLimit::stockLimit),
            Codec.INT.optionalFieldOf("restock_seconds").forGetter(OfferLimit::restockSeconds)
    ).apply(instance, (unlimited, daily, stock, restock) -> new OfferLimit(
            unlimited,
            daily.orElse(null),
            stock.orElse(null),
            restock.orElse(null)
    )));

    private final boolean unlimited;
    private final Optional<Integer> dailyLimit;
    private final Optional<Integer> stockLimit;
    private final Optional<Integer> restockSeconds;

    public OfferLimit(boolean unlimited, Integer dailyLimit, Integer stockLimit, Integer restockSeconds) {
        this.unlimited = unlimited;
        this.dailyLimit = Optional.ofNullable(dailyLimit).filter(value -> value > 0);
        this.stockLimit = Optional.ofNullable(stockLimit).filter(value -> value > 0);
        this.restockSeconds = Optional.ofNullable(restockSeconds).filter(value -> value > 0);
    }

    public static OfferLimit unlimited() {
        return new OfferLimit(true, null, null, null);
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public Optional<Integer> dailyLimit() {
        return dailyLimit;
    }

    public Optional<Integer> stockLimit() {
        return stockLimit;
    }

    public Optional<Integer> restockSeconds() {
        return restockSeconds;
    }

    public OfferLimit withDailyLimit(Integer limit) {
        return new OfferLimit(unlimited && limit == null, limit, stockLimit.orElse(null), restockSeconds.orElse(null));
    }

    public OfferLimit withStockLimit(Integer limit) {
        return new OfferLimit(unlimited && limit == null, dailyLimit.orElse(null), limit, restockSeconds.orElse(null));
    }

    public OfferLimit withRestockSeconds(Integer seconds) {
        return new OfferLimit(unlimited && seconds == null, dailyLimit.orElse(null), stockLimit.orElse(null), seconds);
    }

    public OfferLimit asLimited() {
        if (!unlimited) {
            return this;
        }
        return new OfferLimit(false, dailyLimit.orElse(null), stockLimit.orElse(null), restockSeconds.orElse(null));
    }

    public OfferLimit asUnlimited() {
        if (unlimited && dailyLimit.isEmpty() && stockLimit.isEmpty() && restockSeconds.isEmpty()) {
            return this;
        }
        return unlimited();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OfferLimit other)) {
            return false;
        }
        return unlimited == other.unlimited
                && Objects.equals(dailyLimit, other.dailyLimit)
                && Objects.equals(stockLimit, other.stockLimit)
                && Objects.equals(restockSeconds, other.restockSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unlimited, dailyLimit, stockLimit, restockSeconds);
    }

    @Override
    public String toString() {
        return "OfferLimit{" +
                "unlimited=" + unlimited +
                ", dailyLimit=" + dailyLimit.orElse(null) +
                ", stockLimit=" + stockLimit.orElse(null) +
                ", restockSeconds=" + restockSeconds.orElse(null) +
                '}';
    }
}