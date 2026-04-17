package de.bigbull.marketblocks.shop.marketplace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Clientseitiger Cache für die Marktplatz-Daten.
 */
public final class MarketplaceClientState {
    private static MarketplaceData data = MarketplaceData.empty();
    private static Map<UUID, MarketplaceOfferViewState> offerViewStates = Collections.emptyMap();
    private static long lastSyncTimeMillis;

    // Speichert den letzten Modus (true = Edit, false = Normal)
    private static boolean lastEditMode = false;

    private MarketplaceClientState() {
    }

    public static void apply(MarketplaceData newData, Map<UUID, MarketplaceOfferViewState> newOfferViewStates) {
        data = newData == null ? MarketplaceData.empty() : newData;
        offerViewStates = newOfferViewStates == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(newOfferViewStates));
        lastSyncTimeMillis = System.currentTimeMillis();
    }

    public static MarketplaceData data() {
        return data;
    }

    public static MarketplaceOfferViewState offerViewState(UUID offerId) {
        if (offerId == null) {
            return MarketplaceOfferViewState.empty();
        }
        return offerViewStates.getOrDefault(offerId, MarketplaceOfferViewState.empty());
    }


    public static boolean getLastEditMode() {
        return lastEditMode;
    }

    public static void setLastEditMode(boolean mode) {
        lastEditMode = mode;
    }

    public static long lastSyncTimeMillis() {
        return lastSyncTimeMillis;
    }
}