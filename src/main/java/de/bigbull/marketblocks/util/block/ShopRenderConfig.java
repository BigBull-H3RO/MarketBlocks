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

    // Legacy-Bridge fuer bestehende Aufrufer
    @Deprecated public double getOfferItemX() { return offerItem.x(); }
    @Deprecated public double getOfferItemY() { return offerItem.y(); }
    @Deprecated public double getOfferItemZ() { return offerItem.z(); }
    @Deprecated public float getOfferItemScale() { return offerItem.scale(); }
    @Deprecated public double getPaymentItemX() { return payment1Item.x(); }
    @Deprecated public double getPaymentItemY() { return payment1Item.y(); }
    @Deprecated public double getPaymentItemZ() { return payment1Item.z(); }
    @Deprecated public float getPaymentItemScale() { return payment1Item.scale(); }
    /**
     * @deprecated Gibt payment1Item.y - payment2Item.y zurück.
     * Hinweis: Der Wert kann je nach Design-Config variieren und stimmt nicht
     * zwangsläufig mit dem alten "spacing=0.4" überein (z.B. SMALL_SHOP_DEFAULT: 0.36).
     * Direkt payment1Item().y() und payment2Item().y() verwenden.
     */
    @Deprecated public double getPaymentItemSpacing() { return payment1Item.y() - payment2Item.y(); }
    @Deprecated public double getCountTextOffsetX() { return payment1CountText.x() - payment1Item.x(); }
    @Deprecated public double getCountTextOffsetY() { return payment1Item.y() - payment1CountText.y(); }
    @Deprecated public float getCountTextScale() { return payment1CountText.scale(); }

    /**
     * Gemeinsames Tuning-Profil fuer Front-Payment-Layout.
     *
     * x: seitlicher Slot-Offset im lokalen Front-Koordinatensystem
     * z: Abstand vor dem Block
     * yTop/yBottom: vertikale Slot-Position fuer Payment1/Payment2
     *
     * Schnellhilfe fuers Feintuning:
     * - Item weiter nach links/rechts: X aendern
     * - Item weiter nach vorne/hinten: Z aendern
     * - Item hoeher/tiefer: Y aendern
     * - Groesse: Scale aendern
     */
    private static final RenderTuningProfile COMPACT_FRONT_LAYOUT = new RenderTuningProfile(
            0.35D,
            0.1D,
            0.6D,
            0.2D,
            0.4F,
            0.5D,
            0.1D,
            0.45D,
            0.05D,
            0.015F
    );

    public static final ShopRenderConfig SMALL_SHOP_DEFAULT = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            // Preset-spezifisches Payment-Layout (klassischer SmallShop)
            // X = seitlich, Y = Hoehe, Z = Abstand nach vorne
            .payment1ItemX(0.30D).payment1ItemY(0.62D).payment1ItemZ(0.08D).payment1ItemScale(0.4F)
            .payment2ItemX(0.30D).payment2ItemY(0.26D).payment2ItemZ(0.08D).payment2ItemScale(0.4F)
            // Count-Text sitzt i.d.R. leicht rechts/unter dem jeweiligen Item
            .payment1CountTextX(0.46D).payment1CountTextY(0.47D).payment1CountTextZ(0.08D).payment1CountTextScale(0.015F)
            .payment2CountTextX(0.46D).payment2CountTextY(0.11D).payment2CountTextZ(0.08D).payment2CountTextScale(0.015F)
            // Offer-Item zentral ueber dem Block
            .offerItemX(0.5D).offerItemY(1.3D).offerItemZ(0.5D).offerItemScale(0.8F)
            // Offer-Count-Text unter/nahe Offer-Item
            .offerCountTextX(0.5D).offerCountTextY(1.15D).offerCountTextZ(0.5D).offerCountTextScale(0.015F)
            .build();

    public static final ShopRenderConfig SMALL_SHOP_NEU = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            // Preset-spezifisches Payment-Layout (SmallShopNeu)
            // X = seitlich, Y = Hoehe, Z = Abstand nach vorne
            .payment1ItemX(0.35D).payment1ItemY(0.60D).payment1ItemZ(0.10D).payment1ItemScale(0.4F)
            .payment1ItemYaw(12.0F).payment1ItemPitch(0.0F).payment1ItemRoll(0.0F)
            .payment2ItemX(0.35D).payment2ItemY(0.20D).payment2ItemZ(0.10D).payment2ItemScale(0.4F)
            .payment2ItemYaw(12.0F).payment2ItemPitch(0.0F).payment2ItemRoll(0.0F)
            // Count-Text sitzt i.d.R. leicht rechts/unter dem jeweiligen Item
            .payment1CountTextX(0.50D).payment1CountTextY(0.45D).payment1CountTextZ(0.10D).payment1CountTextScale(0.015F)
            .payment1CountTextYaw(8.0F).payment1CountTextPitch(0.0F).payment1CountTextRoll(0.0F)
            .payment2CountTextX(0.50D).payment2CountTextY(0.05D).payment2CountTextZ(0.10D).payment2CountTextScale(0.015F)
            .payment2CountTextYaw(8.0F).payment2CountTextPitch(0.0F).payment2CountTextRoll(0.0F)
            // Offer-Item zentral ueber dem Block
            .offerItemX(0.5D).offerItemY(1.1D).offerItemZ(0.5D).offerItemScale(0.8F)
            // Offer-Count-Text unter/nahe Offer-Item
            .offerCountTextX(0.5D).offerCountTextY(0.95D).offerCountTextZ(0.5D).offerCountTextScale(0.015F)
            .build();

    public static Builder builder() { return new Builder(); }

    private record RenderTuningProfile(
            double paymentItemX,
            double paymentItemZ,
            double payment1ItemY,
            double payment2ItemY,
            float paymentItemScale,
            double countTextX,
            double countTextZ,
            double payment1CountTextY,
            double payment2CountTextY,
            float countTextScale
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

        /**
         * Setzt ein komplettes Basis-Layout fuer Payment1/Payment2 plus Count-Texte.
         * Danach koennen einzelne Werte gezielt ueberschrieben werden.
         *
         * Feintuning-Template (Reihenfolge):
         * 1) applyProfile(...)
         * 2) payment1Item* / payment2Item* anpassen
         * 3) payment1CountText* / payment2CountText* anpassen
         * 4) offerItem* / offerCountText* anpassen
         */
        /**
         * Setzt ein komplettes Basis-Layout fuer Payment1/Payment2 plus Count-Texte
         * und setzt dabei alle Rotationswerte (yaw/pitch/roll) defensiv auf 0 zurück.
         *
         * FIX: Rotationen werden zurückgesetzt damit keine versehentlich gesetzten
         * Rotationswerte aus einem früheren Builder-Call erhalten bleiben.
         * Danach können Rotationen gezielt pro Slot überschrieben werden.
         */
        public Builder applyProfile(RenderTuningProfile profile) {
            // Rotationen explizit zurücksetzen bevor das Profil angewendet wird
            this.payment1ItemYaw = 0.0F; this.payment1ItemPitch = 0.0F; this.payment1ItemRoll = 0.0F;
            this.payment2ItemYaw = 0.0F; this.payment2ItemPitch = 0.0F; this.payment2ItemRoll = 0.0F;
            this.payment1CountTextYaw = 0.0F; this.payment1CountTextPitch = 0.0F; this.payment1CountTextRoll = 0.0F;
            this.payment2CountTextYaw = 0.0F; this.payment2CountTextPitch = 0.0F; this.payment2CountTextRoll = 0.0F;
            return this
                    .payment1ItemX(profile.paymentItemX)
                    .payment1ItemY(profile.payment1ItemY)
                    .payment1ItemZ(profile.paymentItemZ)
                    .payment1ItemScale(profile.paymentItemScale)
                    .payment2ItemX(profile.paymentItemX)
                    .payment2ItemY(profile.payment2ItemY)
                    .payment2ItemZ(profile.paymentItemZ)
                    .payment2ItemScale(profile.paymentItemScale)
                    .payment1CountTextX(profile.countTextX)
                    .payment1CountTextY(profile.payment1CountTextY)
                    .payment1CountTextZ(profile.countTextZ)
                    .payment1CountTextScale(profile.countTextScale)
                    .payment2CountTextX(profile.countTextX)
                    .payment2CountTextY(profile.payment2CountTextY)
                    .payment2CountTextZ(profile.countTextZ)
                    .payment2CountTextScale(profile.countTextScale);
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

        // Legacy-Bridge fuer bestehende Builder-Aufrufe
        @Deprecated public Builder paymentItemX(double v) { this.payment1ItemX = v; this.payment2ItemX = v; return this; }
        @Deprecated public Builder paymentItemY(double v) { this.payment1ItemY = v; return this; }
        @Deprecated public Builder paymentItemZ(double v) { this.payment1ItemZ = v; this.payment2ItemZ = v; return this; }
        @Deprecated public Builder paymentItemScale(float v) { this.payment1ItemScale = v; this.payment2ItemScale = v; return this; }
        /**
         * @deprecated Reihenfolge-sensitiv: Muss NACH payment1ItemY() bzw. paymentItemY()
         * aufgerufen werden, da payment2ItemY relativ zu this.payment1ItemY berechnet wird.
         * Falsche Reihenfolge führt zu falscher payment2-Position.
         * Verwende stattdessen payment2ItemY(...) direkt.
         */
        @Deprecated public Builder paymentItemSpacing(double v) { this.payment2ItemY = this.payment1ItemY - v; return this; }
        @Deprecated public Builder countTextOffsetX(double v) {
            this.payment1CountTextX = this.payment1ItemX + v;
            this.payment2CountTextX = this.payment2ItemX + v;
            return this;
        }
        @Deprecated public Builder countTextOffsetY(double v) {
            this.payment1CountTextY = this.payment1ItemY - v;
            this.payment2CountTextY = this.payment2ItemY - v;
            return this;
        }
        @Deprecated public Builder countTextScale(float v) {
            this.payment1CountTextScale = v;
            this.payment2CountTextScale = v;
            return this;
        }

        public ShopRenderConfig build() { return new ShopRenderConfig(this); }
    }
}