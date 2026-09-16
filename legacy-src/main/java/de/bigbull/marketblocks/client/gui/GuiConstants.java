package de.bigbull.marketblocks.client.gui;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared GUI constants for single-offer shop menus and screens.
 */
public final class GuiConstants {
    /** Total height of the GUI background. */
    public static final int IMAGE_HEIGHT = 166;
    /** Total width of the GUI background. */
    public static final int IMAGE_WIDTH = 176;
    /** Y start position of the player inventory. */
    public static final int PLAYER_INV_Y_START = 84;
    /** Y position of the inventory label. */
    public static final int PLAYER_INV_LABEL_Y = PLAYER_INV_Y_START - 11;

    /** Compact pixel-art micro font for subheadings, group boxes, and slider labels. */
    public static final ResourceLocation COMPACT_FONT = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "compact");

    private GuiConstants() {
    }

    /**
     * Styles the given Component to use the compact micro font.
     */
    public static MutableComponent compact(Component component) {
        if (component == null) {
            return Component.empty();
        }
        return Component.empty().append(component).withStyle(style -> style.withFont(COMPACT_FONT));
    }

    /**
     * Creates a literal Component with the compact micro font.
     */
    public static MutableComponent compact(String text) {
        if (text == null) {
            return Component.empty();
        }
        return Component.literal(text).withStyle(style -> style.withFont(COMPACT_FONT));
    }
}

