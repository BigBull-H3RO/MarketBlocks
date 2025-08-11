package de.bigbull.marketblocks.util.custom.screen.gui;

/**
 * Gemeinsame GUI-Konstanten für SmallShop-Menüs und -Screens.
 */
public final class GuiConstants {
    /** Gesamthöhe des GUI-Hintergrundbildes. */
    public static final int IMAGE_HEIGHT = 256;
    /** Y-Startposition des Spielerinventars. */
    public static final int PLAYER_INV_Y_START = IMAGE_HEIGHT - 94;
    /** Y-Position der Spieler-Hotbar. */
    public static final int HOTBAR_Y = PLAYER_INV_Y_START + 58;
    /** Y-Position des Inventar-Titels. */
    public static final int PLAYER_INV_LABEL_Y = PLAYER_INV_Y_START - 12;

    private GuiConstants() {
    }
}