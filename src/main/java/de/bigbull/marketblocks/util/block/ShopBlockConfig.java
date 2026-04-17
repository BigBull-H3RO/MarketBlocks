package de.bigbull.marketblocks.util.block;

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
public class ShopBlockConfig {
    private final VoxelShape shape;

    private ShopBlockConfig(Builder builder) {
        this.shape = builder.shape;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public VoxelShape getShape() { return shape; }

    // -------------------------------------------------------------------------
    // Vordefinierte Konfigurationen
    // -------------------------------------------------------------------------

    /** TradeStandBlock (1 Block hoch, klassisches Design). */
    public static final ShopBlockConfig TRADE_STAND_DEFAULT_SHAPE = builder()
            .shape(Block.box(0, 0, 0, 16, 13, 16))
            .build();

    /**
     * TradeStandBlockNeu – gemeinsame Shape-Config fuer den Basissockel.
     *
     * Hinweis: TradeStandBlockNeu ueberschreibt getShape/getCollisionShape/getInteractionShape
     * state-abhaengig selbst, daher ist diese Config dort nur ein konsistenter Fallback.
     */
    public static final ShopBlockConfig TRADE_STAND_SHAPE = builder()
            .shape(Block.box(0, 0, 0, 16, 11, 16))
            .build();

    /**
     * @deprecated Verwende stattdessen TRADE_STAND_NEU_SHAPE.
     *
     * Altname bleibt nur fuer Kompatibilitaet mit aelteren Referenzen erhalten.
     */
    @Deprecated
    public static final ShopBlockConfig TRADE_STAND_NO_SHOWCASE_SHAPE = TRADE_STAND_SHAPE;

    /**
     * @deprecated Verwende stattdessen TRADE_STAND_NEU_SHAPE.
     *
     * Altname bleibt nur fuer Kompatibilitaet mit aelteren Referenzen erhalten.
     */
    @Deprecated
    public static final ShopBlockConfig TRADE_STAND_WITH_SHOWCASE_SHAPE = TRADE_STAND_SHAPE;

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private VoxelShape shape = Block.box(0, 0, 0, 16, 16, 16);

        public Builder shape(VoxelShape v) { this.shape = v; return this; }


        public ShopBlockConfig build() { return new ShopBlockConfig(this); }
    }
}