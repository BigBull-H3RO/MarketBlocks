package de.bigbull.marketblocks.feature.singleoffer.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shape configuration for shop block designs.
 *
 * This class intentionally only contains geometry (VoxelShape) for
 * getShape/getCollisionShape/getInteractionShape.
 *
 * Render positions for Offer/Payment/Count-Text are defined in ShopRenderConfig.
 */
public record ShopBlockConfig(VoxelShape shape) {
    public VoxelShape getShape() {
        return shape;
    }

    /** TradeStandBlock (1 block tall, classic design). */
    public static final ShopBlockConfig TRADE_STAND_DEFAULT_SHAPE = new ShopBlockConfig(Block.box(0, 0, 0, 16, 13, 16));

    /**
     * TradeStandBlock – shared shape config for the base pedestal.
     *
     * Note: TradeStandBlock overrides getShape/getCollisionShape/getInteractionShape
     * state-dependently itself, so this config is only a consistent fallback there.
     */
    public static final ShopBlockConfig TRADE_STAND_SHAPE = new ShopBlockConfig(Block.box(0, 0, 0, 16, 11, 16));

    public static final ShopBlockConfig MARKET_CRATE_SHAPE = new ShopBlockConfig(Block.box(0, 0, 0, 16, 16, 16));
}
