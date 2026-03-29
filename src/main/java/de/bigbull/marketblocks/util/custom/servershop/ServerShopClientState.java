package de.bigbull.marketblocks.util.custom.servershop;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Clientseitiger Cache für die Server-Shop-Daten.
 */
public final class ServerShopClientState {
    private static ServerShopData data = ServerShopData.empty();
    private static Map<UUID, ServerShopOfferViewState> offerViewStates = Collections.emptyMap();
    private static long lastSyncTimeMillis;

    // Speichert den letzten Modus (true = Edit, false = Normal)
    private static boolean lastEditMode = false;

    private ServerShopClientState() {
    }

    public static void apply(ServerShopData newData, Map<UUID, ServerShopOfferViewState> newOfferViewStates) {
        data = newData == null ? ServerShopData.empty() : newData;
        offerViewStates = newOfferViewStates == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(newOfferViewStates));
        lastSyncTimeMillis = System.currentTimeMillis();
    }

    public static ServerShopData data() {
        return data;
    }

    public static ServerShopOfferViewState offerViewState(UUID offerId) {
        if (offerId == null) {
            return ServerShopOfferViewState.empty();
        }
        return offerViewStates.getOrDefault(offerId, ServerShopOfferViewState.empty());
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