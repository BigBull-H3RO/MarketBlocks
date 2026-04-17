package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.block.entity.SingleOfferShopBlockEntity;

/**
 * Shared contract for single-offer shop menus.
 */
public interface ShopMenu {
    /**
     * Returns the backing shop block entity.
     */
    SingleOfferShopBlockEntity getBlockEntity();

    /**
     * Returns the bitmask of menu flags.
     */
    int getFlags();

    /**
     * Checks whether a specific flag is set.
     */
    default boolean hasFlag(int flag) {
        return (getFlags() & flag) != 0;
    }

    /**
     * @return true if the current player is an owner of the shop.
     */
    default boolean isOwner() {
        return hasFlag(SingleOfferShopBlockEntity.OWNER_FLAG);
    }

    /**
     * @return true if the current player is the primary owner of the shop.
     */
    default boolean isPrimaryOwner() {
        return hasFlag(SingleOfferShopBlockEntity.PRIMARY_OWNER_FLAG);
    }
}
