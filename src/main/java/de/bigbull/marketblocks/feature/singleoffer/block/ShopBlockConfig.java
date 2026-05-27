package de.bigbull.marketblocks.feature.singleoffer.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shape-Konfiguration fuer Shop-Block-Designs.
 *
 * Diese Klasse enthaelt bewusst nur Geometrie (VoxelShape) fuer
 * getShape/getCollisionShape/getInteractionShape.
 *
 * Render-Positionen fuer Offer/Payment/Count-Text sind in ShopRenderConfig ausgelagert.
 */
public record ShopBlockConfig(VoxelShape shape) {
    public VoxelShape getShape() {
        return shape;
    }

    /** TradeStandBlock (1 Block hoch, klassisches Design). */
    public static final ShopBlockConfig TRADE_STAND_DEFAULT_SHAPE = new ShopBlockConfig(Block.box(0, 0, 0, 16, 13, 16));

    /**
     * TradeStandBlockNeu – gemeinsame Shape-Config fuer den Basissockel.
     *
     * Hinweis: TradeStandBlockNeu ueberschreibt getShape/getCollisionShape/getInteractionShape
     * state-abhaengig selbst, daher ist diese Config dort nur ein konsistenter Fallback.
     */
    public static final ShopBlockConfig TRADE_STAND_SHAPE = new ShopBlockConfig(Block.box(0, 0, 0, 16, 11, 16));

    public static final ShopBlockConfig MARKET_CRATE_SHAPE = new ShopBlockConfig(Block.box(0, 0, 0, 16, 16, 16));
}
