package de.bigbull.marketblocks.util.custom.servershop;

/**
 * Clientseitiger Cache für die Server-Shop-Daten.
 */
public final class ServerShopClientState {
    private static ServerShopData data = ServerShopData.empty();
    private static boolean canEdit;

    // Speichert den letzten Modus (true = Edit, false = Normal)
    private static boolean lastEditMode = false;

    private ServerShopClientState() {
    }

    public static void apply(ServerShopData newData, boolean editable) {
        data = newData == null ? ServerShopData.empty() : newData;
        canEdit = editable;
    }

    public static ServerShopData data() {
        return data;
    }

    public static boolean canEdit() {
        return canEdit;
    }

    public static boolean getLastEditMode() {
        return lastEditMode;
    }

    public static void setLastEditMode(boolean mode) {
        lastEditMode = mode;
    }
}