package de.bigbull.marketblocks.util.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Configuration class for shop block designs.
 * Contains design-specific properties like shape, renderer offsets, and display positions.
 *
 * This config controls how items are rendered on the shop block:
 * - Offer Item: The item being sold (hovers above the block)
 * - Payment Items: Up to 2 items required for purchase (displayed at front of block)
 *
 * HINWEIS zur Shape bei Multi-Block-Designs (z.B. SMALL_SHOP_TALL_SHOWCASE):
 * getShape() (= Outline-Highlight) darf über Y=16 hinausgehen → eine durchgehende Outline
 * für den gesamten Block ohne sichtbare Naht.
 * getCollisionShape() muss separat in der Subklasse auf Y=16 begrenzt werden,
 * damit Spieler nicht in der Luft stehen (siehe SmallShopBlockNeu).
 */
public class ShopBlockConfig {
    private final VoxelShape shape;

    // Offer Item (floating sale item above block)
    private final double offerItemX;
    private final double offerItemY;
    private final double offerItemZ;
    private final float offerItemScale;

    // Payment Items (required items displayed at front)
    private final double paymentItemX;
    private final double paymentItemY;
    private final double paymentItemZ;
    private final float paymentItemScale;
    private final double paymentItemSpacing;

    // Count text (displays item quantity)
    private final double countTextOffsetX;
    private final double countTextOffsetY;
    private final float countTextScale;

    private ShopBlockConfig(Builder builder) {
        this.shape = builder.shape;
        this.offerItemX = builder.offerItemX;
        this.offerItemY = builder.offerItemY;
        this.offerItemZ = builder.offerItemZ;
        this.offerItemScale = builder.offerItemScale;
        this.paymentItemX = builder.paymentItemX;
        this.paymentItemY = builder.paymentItemY;
        this.paymentItemZ = builder.paymentItemZ;
        this.paymentItemScale = builder.paymentItemScale;
        this.paymentItemSpacing = builder.paymentItemSpacing;
        this.countTextOffsetX = builder.countTextOffsetX;
        this.countTextOffsetY = builder.countTextOffsetY;
        this.countTextScale = builder.countTextScale;
    }

    public VoxelShape getShape() {
        return shape;
    }

    public double getOfferItemX() { return offerItemX; }
    public double getOfferItemY() { return offerItemY; }
    public double getOfferItemZ() { return offerItemZ; }
    public float getOfferItemScale() { return offerItemScale; }

    public double getPaymentItemX() { return paymentItemX; }
    public double getPaymentItemY() { return paymentItemY; }
    public double getPaymentItemZ() { return paymentItemZ; }
    public float getPaymentItemScale() { return paymentItemScale; }
    public double getPaymentItemSpacing() { return paymentItemSpacing; }

    public double getCountTextOffsetX() { return countTextOffsetX; }
    public double getCountTextOffsetY() { return countTextOffsetY; }
    public float getCountTextScale() { return countTextScale; }

    // -------------------------------------------------------------------------
    // Vordefinierte Konfigurationen
    // -------------------------------------------------------------------------

    public static final ShopBlockConfig SMALL_SHOP_DEFAULT = builder()
            .shape(Block.box(0, 0, 0, 16, 13, 16))
            .offerItemX(0.5D).offerItemY(1.3D).offerItemZ(0.5D).offerItemScale(0.8F)
            .paymentItemX(0.35D).paymentItemY(0.6D).paymentItemZ(0.1D).paymentItemScale(0.4F)
            .paymentItemSpacing(0.4D)
            .countTextOffsetX(0.15D).countTextOffsetY(0.15D).countTextScale(0.015F)
            .build();

    /**
     * Tall Showcase (2-Block-Design: SmallShopBlockNeu + SmallShopBlockNeuTop)
     *
     * shape geht bis Y=25 → Outline umrahmt die GESAMTE Vitrine ohne Naht.
     * Die Kollision wird in SmallShopBlockNeu separat auf Y=16 begrenzt.
     */
    public static final ShopBlockConfig SMALL_SHOP_TALL_SHOWCASE = builder()
            // Y=25 = Gesamthöhe des Blocks (11er Sockel + 14er Vitrine).
            // Deckt beide Block-Räume visuell ab → eine durchgehende Outline, kein Strich.
            .shape(Block.box(0, 0, 0, 16, 16, 16))
            .offerItemX(0.5D).offerItemY(1.6D).offerItemZ(0.5D).offerItemScale(0.8F)
            .paymentItemX(0.35D).paymentItemY(0.6D).paymentItemZ(0.1D).paymentItemScale(0.4F)
            .paymentItemSpacing(0.4D)
            .countTextOffsetX(0.15D).countTextOffsetY(0.15D).countTextScale(0.015F)
            .build();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VoxelShape shape = Block.box(0, 0, 0, 16, 16, 16);

        private double offerItemX = 0.5D;
        private double offerItemY = 1.3D;
        private double offerItemZ = 0.5D;
        private float offerItemScale = 0.8F;

        private double paymentItemX = 0.35D;
        private double paymentItemY = 0.6D;
        private double paymentItemZ = 0.1D;
        private float paymentItemScale = 0.4F;
        private double paymentItemSpacing = 0.4D;

        private double countTextOffsetX = 0.15D;
        private double countTextOffsetY = 0.15D;
        private float countTextScale = 0.015F;

        public Builder shape(VoxelShape shape) { this.shape = shape; return this; }

        public Builder offerItemX(double x) { this.offerItemX = x; return this; }
        public Builder offerItemY(double y) { this.offerItemY = y; return this; }
        public Builder offerItemZ(double z) { this.offerItemZ = z; return this; }
        public Builder offerItemScale(float scale) { this.offerItemScale = scale; return this; }

        public Builder paymentItemX(double x) { this.paymentItemX = x; return this; }
        public Builder paymentItemY(double y) { this.paymentItemY = y; return this; }
        public Builder paymentItemZ(double z) { this.paymentItemZ = z; return this; }
        public Builder paymentItemScale(float scale) { this.paymentItemScale = scale; return this; }
        public Builder paymentItemSpacing(double spacing) { this.paymentItemSpacing = spacing; return this; }

        public Builder countTextOffsetX(double offset) { this.countTextOffsetX = offset; return this; }
        public Builder countTextOffsetY(double offset) { this.countTextOffsetY = offset; return this; }
        public Builder countTextScale(float scale) { this.countTextScale = scale; return this; }

        public ShopBlockConfig build() { return new ShopBlockConfig(this); }
    }
}