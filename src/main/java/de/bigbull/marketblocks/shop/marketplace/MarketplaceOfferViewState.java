package de.bigbull.marketblocks.shop.marketplace;

import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

/**
 * Viewer-spezifischer Laufzeitstatus eines Marktplatz-Angebots.
 */
public record MarketplaceOfferViewState(
        int maxPurchasable,
        Optional<Integer> remainingDailyPurchases,
        Optional<Integer> remainingStock,
        Optional<Integer> restockSecondsRemaining,
        double priceMultiplier
) {
    private static final String MAX_PURCHASABLE_KEY = "max_purchasable";
    private static final String REMAINING_DAILY_KEY = "remaining_daily";
    private static final String REMAINING_STOCK_KEY = "remaining_stock";
    private static final String RESTOCK_SECONDS_KEY = "restock_seconds";
    private static final String PRICE_MULTIPLIER_KEY = "price_multiplier";

    public MarketplaceOfferViewState {
        maxPurchasable = Math.max(0, maxPurchasable);
        remainingDailyPurchases = java.util.Objects.requireNonNullElse(remainingDailyPurchases, Optional.<Integer>empty())
                .filter(candidate -> candidate >= 0);
        remainingStock = java.util.Objects.requireNonNullElse(remainingStock, Optional.<Integer>empty())
                .filter(candidate -> candidate >= 0);
        restockSecondsRemaining = java.util.Objects.requireNonNullElse(restockSecondsRemaining, Optional.<Integer>empty())
                .filter(candidate -> candidate >= 0);
        priceMultiplier = Math.max(0.0d, priceMultiplier);
    }

    public static MarketplaceOfferViewState empty() {
        return new MarketplaceOfferViewState(0, Optional.empty(), Optional.empty(), Optional.empty(), 1.0d);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(MAX_PURCHASABLE_KEY, maxPurchasable);
        remainingDailyPurchases.ifPresent(value -> tag.putInt(REMAINING_DAILY_KEY, value));
        remainingStock.ifPresent(value -> tag.putInt(REMAINING_STOCK_KEY, value));
        restockSecondsRemaining.ifPresent(value -> tag.putInt(RESTOCK_SECONDS_KEY, value));
        tag.putDouble(PRICE_MULTIPLIER_KEY, priceMultiplier);
        return tag;
    }

    public static MarketplaceOfferViewState fromTag(CompoundTag tag) {
        if (tag == null) {
            return empty();
        }
        return new MarketplaceOfferViewState(
                tag.getInt(MAX_PURCHASABLE_KEY),
                tag.contains(REMAINING_DAILY_KEY) ? Optional.of(tag.getInt(REMAINING_DAILY_KEY)) : Optional.empty(),
                tag.contains(REMAINING_STOCK_KEY) ? Optional.of(tag.getInt(REMAINING_STOCK_KEY)) : Optional.empty(),
                tag.contains(RESTOCK_SECONDS_KEY) ? Optional.of(tag.getInt(RESTOCK_SECONDS_KEY)) : Optional.empty(),
                tag.contains(PRICE_MULTIPLIER_KEY) ? tag.getDouble(PRICE_MULTIPLIER_KEY) : 1.0d
        );
    }
}
