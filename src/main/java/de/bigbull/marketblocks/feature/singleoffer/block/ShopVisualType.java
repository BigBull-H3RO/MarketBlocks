package de.bigbull.marketblocks.feature.singleoffer.block;

import net.minecraft.world.level.block.Block;

/**
 * Defines the type of visual block design for a shop.
 */
public enum ShopVisualType {
    TRADE_STAND,
    MARKET_CRATE,
    UNKNOWN;

    public static ShopVisualType from(Block block) {
        if (block instanceof MarketCrateBlock) {
            return MARKET_CRATE;
        }
        if (block instanceof TradeStandBlock) {
            return TRADE_STAND;
        }
        return UNKNOWN;
    }

    public boolean isMarketCrate() {
        return this == MARKET_CRATE;
    }

    public boolean isTradeStand() {
        return this == TRADE_STAND;
    }
}
