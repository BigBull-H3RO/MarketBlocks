package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the common contract for all menus related to the Small Shop.
 * This ensures that any shop menu can provide access to its underlying block entity
 * and a set of flags for client-side state checking.
 */
public interface ShopMenu {
    /**
     * Gets the associated {@link SmallShopBlockEntity} for this menu.
     *
     * @return The block entity instance.
     */
    @NotNull
    SmallShopBlockEntity getBlockEntity();

    /**
     * Gets the bitmask of flags for this menu, synced from the server.
     * The flags are defined in {@link SmallShopBlockEntity}.
     *
     * @return The integer bitmask of flags.
     */
    int getFlags();

    /**
     * Checks if a specific flag is set in the menu's flag bitmask.
     *
     * @param flag The flag to check (e.g., {@link SmallShopBlockEntity#OWNER_FLAG}).
     * @return True if the flag is set, false otherwise.
     */
    default boolean hasFlag(int flag) {
        return (getFlags() & flag) != 0;
    }

    /**
     * A convenience method to check if the player viewing the menu is an owner of the shop.
     *
     * @return True if the player is an owner, false otherwise.
     */
    default boolean isOwner() {
        return hasFlag(SmallShopBlockEntity.OWNER_FLAG);
    }
}