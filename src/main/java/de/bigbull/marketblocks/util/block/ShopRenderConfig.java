package de.bigbull.marketblocks.util.block;

/**
 * Render-Konfiguration fuer Shop-Block-Designs.
 * Enthaelt getrennte Slot-Konfigurationen fuer:
 * - Offer Item + Offer Count-Text
 * - Payment1 Item + Payment1 Count-Text
 * - Payment2 Item + Payment2 Count-Text
 */
public class ShopRenderConfig {
    private final SlotRenderConfig offerItem;
    private final SlotRenderConfig offerCountText;
    private final SlotRenderConfig payment1Item;
    private final SlotRenderConfig payment1CountText;
    private final SlotRenderConfig payment2Item;
    private final SlotRenderConfig payment2CountText;

    private ShopRenderConfig(Builder builder) {
        this.offerItem = new SlotRenderConfig(builder.offerItemX, builder.offerItemY, builder.offerItemZ, builder.offerItemScale,
                builder.offerItemYaw, builder.offerItemPitch, builder.offerItemRoll);
        this.offerCountText = new SlotRenderConfig(builder.offerCountTextX, builder.offerCountTextY, builder.offerCountTextZ, builder.offerCountTextScale,
                builder.offerCountTextYaw, builder.offerCountTextPitch, builder.offerCountTextRoll);
        this.payment1Item = new SlotRenderConfig(builder.payment1ItemX, builder.payment1ItemY, builder.payment1ItemZ, builder.payment1ItemScale,
                builder.payment1ItemYaw, builder.payment1ItemPitch, builder.payment1ItemRoll);
        this.payment1CountText = new SlotRenderConfig(builder.payment1CountTextX, builder.payment1CountTextY, builder.payment1CountTextZ, builder.payment1CountTextScale,
                builder.payment1CountTextYaw, builder.payment1CountTextPitch, builder.payment1CountTextRoll);
        this.payment2Item = new SlotRenderConfig(builder.payment2ItemX, builder.payment2ItemY, builder.payment2ItemZ, builder.payment2ItemScale,
                builder.payment2ItemYaw, builder.payment2ItemPitch, builder.payment2ItemRoll);
        this.payment2CountText = new SlotRenderConfig(builder.payment2CountTextX, builder.payment2CountTextY, builder.payment2CountTextZ, builder.payment2CountTextScale,
                builder.payment2CountTextYaw, builder.payment2CountTextPitch, builder.payment2CountTextRoll);
    }

    public SlotRenderConfig getOfferItem() { return offerItem; }
    public SlotRenderConfig getOfferCountText() { return offerCountText; }
    public SlotRenderConfig getPayment1Item() { return payment1Item; }
    public SlotRenderConfig getPayment1CountText() { return payment1CountText; }
    public SlotRenderConfig getPayment2Item() { return payment2Item; }
    public SlotRenderConfig getPayment2CountText() { return payment2CountText; }

    /**
     * Gemeinsames Tuning-Profil fuer Front-Payment-Layout.
     * x: seitlicher Slot-Offset im lokalen Front-Koordinatensystem
     * z: Abstand vor dem Block
     * yTop/yBottom: vertikale Slot-Position fuer Payment1/Payment2
     */
    private static final RenderTuningProfile COMPACT_FRONT_LAYOUT = new RenderTuningProfile(
            0.35D, 0.1D, 0.6D, 0.2D, 0.4F, 0.5D, 0.1D, 0.45D, 0.05D, 0.015F
    );

    public static final ShopRenderConfig SMALL_SHOP_DEFAULT = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            .payment1ItemX(0.30D).payment1ItemY(0.62D).payment1ItemZ(0.08D).payment1ItemScale(0.4F)
            .payment2ItemX(0.30D).payment2ItemY(0.26D).payment2ItemZ(0.08D).payment2ItemScale(0.4F)
            .payment1CountTextX(0.46D).payment1CountTextY(0.47D).payment1CountTextZ(0.08D).payment1CountTextScale(0.015F)
            .payment2CountTextX(0.46D).payment2CountTextY(0.11D).payment2CountTextZ(0.08D).payment2CountTextScale(0.015F)
            .offerItemX(0.5D).offerItemY(1.3D).offerItemZ(0.5D).offerItemScale(0.8F)
            .offerCountTextX(0.5D).offerCountTextY(1.15D).offerCountTextZ(0.5D).offerCountTextScale(0.015F)
            .build();

    public static final ShopRenderConfig SMALL_SHOP = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            .payment1ItemX(0.657D).payment1ItemY(0.465D).payment1ItemZ(0.568D).payment1ItemScale(0.26F)
            .payment1ItemYaw(0.0F).payment1ItemPitch(-22.5F).payment1ItemRoll(0.0F)
            .payment2ItemX(0.657D).payment2ItemY(0.153D).payment2ItemZ(0.568D).payment2ItemScale(0.26F)
            .payment2ItemYaw(0.0F).payment2ItemPitch(-22.5F).payment2ItemRoll(0.0F)
            .payment1CountTextX(0.36D).payment1CountTextY(0.525D).payment1CountTextZ(0.54D).payment1CountTextScale(0.020F)
            .payment1CountTextYaw(0.0F).payment1CountTextPitch(-22.5F).payment1CountTextRoll(0.0F)
            .payment2CountTextX(0.36D).payment2CountTextY(0.21D).payment2CountTextZ(0.54D).payment2CountTextScale(0.020F)
            .payment2CountTextYaw(0.0F).payment2CountTextPitch(-22.5F).payment2CountTextRoll(0.0F)
            .offerItemX(0.5D).offerItemY(1.1D).offerItemZ(0.5D).offerItemScale(0.70F)
            .offerItemYaw(0.0F).offerItemPitch(0.0F).offerItemRoll(0.0F)
            .offerCountTextX(0.5D).offerCountTextY(0.80D).offerCountTextZ(0.28D).offerCountTextScale(0.016F)
            .offerCountTextYaw(0.0F).offerCountTextPitch(-45.0F).offerCountTextRoll(0.0F)
            .build();

    public static Builder builder() { return new Builder(); }

    private record RenderTuningProfile(
            double paymentItemX, double paymentItemZ, double payment1ItemY, double payment2ItemY,
            float paymentItemScale, double countTextX, double countTextZ,
            double payment1CountTextY, double payment2CountTextY, float countTextScale
    ) {}

    public record SlotRenderConfig(double x, double y, double z, float scale, float yaw, float pitch, float roll) {
        public SlotRenderConfig(double x, double y, double z, float scale) {
            this(x, y, z, scale, 0.0F, 0.0F, 0.0F);
        }
    }

    public static class Builder {
        private double offerItemX = 0.5D, offerItemY = 1.3D, offerItemZ = 0.5D;
        private float offerItemScale = 0.8F;
        private float offerItemYaw = 0.0F, offerItemPitch = 0.0F, offerItemRoll = 0.0F;

        private double offerCountTextX = 0.5D, offerCountTextY = 1.15D, offerCountTextZ = 0.5D;
        private float offerCountTextScale = 0.015F;
        private float offerCountTextYaw = 0.0F, offerCountTextPitch = 0.0F, offerCountTextRoll = 0.0F;

        private double payment1ItemX = 0.35D, payment1ItemY = 0.6D, payment1ItemZ = 0.1D;
        private float payment1ItemScale = 0.4F;
        private float payment1ItemYaw = 0.0F, payment1ItemPitch = 0.0F, payment1ItemRoll = 0.0F;

        private double payment1CountTextX = 0.5D, payment1CountTextY = 0.45D, payment1CountTextZ = 0.1D;
        private float payment1CountTextScale = 0.015F;
        private float payment1CountTextYaw = 0.0F, payment1CountTextPitch = 0.0F, payment1CountTextRoll = 0.0F;

        private double payment2ItemX = 0.35D, payment2ItemY = 0.2D, payment2ItemZ = 0.1D;
        private float payment2ItemScale = 0.4F;
        private float payment2ItemYaw = 0.0F, payment2ItemPitch = 0.0F, payment2ItemRoll = 0.0F;

        private double payment2CountTextX = 0.5D, payment2CountTextY = 0.05D, payment2CountTextZ = 0.1D;
        private float payment2CountTextScale = 0.015F;
        private float payment2CountTextYaw = 0.0F, payment2CountTextPitch = 0.0F, payment2CountTextRoll = 0.0F;

        public Builder applyProfile(RenderTuningProfile profile) {
            this.payment1ItemYaw = 0.0F; this.payment1ItemPitch = 0.0F; this.payment1ItemRoll = 0.0F;
            this.payment2ItemYaw = 0.0F; this.payment2ItemPitch = 0.0F; this.payment2ItemRoll = 0.0F;
            this.payment1CountTextYaw = 0.0F; this.payment1CountTextPitch = 0.0F; this.payment1CountTextRoll = 0.0F;
            this.payment2CountTextYaw = 0.0F; this.payment2CountTextPitch = 0.0F; this.payment2CountTextRoll = 0.0F;

            return this
                    .payment1ItemX(profile.paymentItemX).payment1ItemY(profile.payment1ItemY).payment1ItemZ(profile.paymentItemZ).payment1ItemScale(profile.paymentItemScale)
                    .payment2ItemX(profile.paymentItemX).payment2ItemY(profile.payment2ItemY).payment2ItemZ(profile.paymentItemZ).payment2ItemScale(profile.paymentItemScale)
                    .payment1CountTextX(profile.countTextX).payment1CountTextY(profile.payment1CountTextY).payment1CountTextZ(profile.countTextZ).payment1CountTextScale(profile.countTextScale)
                    .payment2CountTextX(profile.countTextX).payment2CountTextY(profile.payment2CountTextY).payment2CountTextZ(profile.countTextZ).payment2CountTextScale(profile.countTextScale);
        }

        public Builder offerItemX(double v) { this.offerItemX = v; return this; }
        public Builder offerItemY(double v) { this.offerItemY = v; return this; }
        public Builder offerItemZ(double v) { this.offerItemZ = v; return this; }
        public Builder offerItemScale(float v) { this.offerItemScale = v; return this; }
        public Builder offerItemYaw(float v) { this.offerItemYaw = v; return this; }
        public Builder offerItemPitch(float v) { this.offerItemPitch = v; return this; }
        public Builder offerItemRoll(float v) { this.offerItemRoll = v; return this; }

        public Builder offerCountTextX(double v) { this.offerCountTextX = v; return this; }
        public Builder offerCountTextY(double v) { this.offerCountTextY = v; return this; }
        public Builder offerCountTextZ(double v) { this.offerCountTextZ = v; return this; }
        public Builder offerCountTextScale(float v) { this.offerCountTextScale = v; return this; }
        public Builder offerCountTextYaw(float v) { this.offerCountTextYaw = v; return this; }
        public Builder offerCountTextPitch(float v) { this.offerCountTextPitch = v; return this; }
        public Builder offerCountTextRoll(float v) { this.offerCountTextRoll = v; return this; }

        public Builder payment1ItemX(double v) { this.payment1ItemX = v; return this; }
        public Builder payment1ItemY(double v) { this.payment1ItemY = v; return this; }
        public Builder payment1ItemZ(double v) { this.payment1ItemZ = v; return this; }
        public Builder payment1ItemScale(float v) { this.payment1ItemScale = v; return this; }
        public Builder payment1ItemYaw(float v) { this.payment1ItemYaw = v; return this; }
        public Builder payment1ItemPitch(float v) { this.payment1ItemPitch = v; return this; }
        public Builder payment1ItemRoll(float v) { this.payment1ItemRoll = v; return this; }

        public Builder payment1CountTextX(double v) { this.payment1CountTextX = v; return this; }
        public Builder payment1CountTextY(double v) { this.payment1CountTextY = v; return this; }
        public Builder payment1CountTextZ(double v) { this.payment1CountTextZ = v; return this; }
        public Builder payment1CountTextScale(float v) { this.payment1CountTextScale = v; return this; }
        public Builder payment1CountTextYaw(float v) { this.payment1CountTextYaw = v; return this; }
        public Builder payment1CountTextPitch(float v) { this.payment1CountTextPitch = v; return this; }
        public Builder payment1CountTextRoll(float v) { this.payment1CountTextRoll = v; return this; }

        public Builder payment2ItemX(double v) { this.payment2ItemX = v; return this; }
        public Builder payment2ItemY(double v) { this.payment2ItemY = v; return this; }
        public Builder payment2ItemZ(double v) { this.payment2ItemZ = v; return this; }
        public Builder payment2ItemScale(float v) { this.payment2ItemScale = v; return this; }
        public Builder payment2ItemYaw(float v) { this.payment2ItemYaw = v; return this; }
        public Builder payment2ItemPitch(float v) { this.payment2ItemPitch = v; return this; }
        public Builder payment2ItemRoll(float v) { this.payment2ItemRoll = v; return this; }

        public Builder payment2CountTextX(double v) { this.payment2CountTextX = v; return this; }
        public Builder payment2CountTextY(double v) { this.payment2CountTextY = v; return this; }
        public Builder payment2CountTextZ(double v) { this.payment2CountTextZ = v; return this; }
        public Builder payment2CountTextScale(float v) { this.payment2CountTextScale = v; return this; }
        public Builder payment2CountTextYaw(float v) { this.payment2CountTextYaw = v; return this; }
        public Builder payment2CountTextPitch(float v) { this.payment2CountTextPitch = v; return this; }
        public Builder payment2CountTextRoll(float v) { this.payment2CountTextRoll = v; return this; }

        public ShopRenderConfig build() { return new ShopRenderConfig(this); }
    }
}