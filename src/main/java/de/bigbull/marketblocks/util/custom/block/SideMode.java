package de.bigbull.marketblocks.util.custom.block;

import de.bigbull.marketblocks.data.lang.ModLang;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the possible I/O modes for a side of the Small Shop block.
 */
public enum SideMode {
    DISABLED(ModLang.GUI_DISABLED),
    INPUT(ModLang.GUI_INPUT),
    OUTPUT(ModLang.GUI_OUTPUT);

    private final String translationKey;

    SideMode(String translationKey) {
        this.translationKey = translationKey;
    }

    /**
     * Cycles to the next mode in the sequence (DISABLED -> INPUT -> OUTPUT -> DISABLED).
     * @return The next SideMode.
     */
    public SideMode next() {
        return switch (this) {
            case DISABLED -> INPUT;
            case INPUT -> OUTPUT;
            case OUTPUT -> DISABLED;
        };
    }

    /**
     * Gets the translatable display name for this mode.
     * @return A translatable component representing the mode's name.
     */
    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    /**
     * Gets a SideMode from its ordinal ID.
     * Provides a safe fallback to DISABLED for invalid IDs.
     * @param id The ordinal ID of the mode.
     * @return The corresponding SideMode, or DISABLED if not found.
     */
    @NotNull
    public static SideMode fromId(int id) {
        SideMode[] values = values();
        if (id < 0 || id >= values.length) {
            return DISABLED;
        }
        return values[id];
    }
}