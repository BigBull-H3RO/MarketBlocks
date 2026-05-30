package de.bigbull.marketblocks.feature.singleoffer.block;

/**
 * Render configuration for shop block designs.
 * Contains separate slot configurations for:
 * - Offer Item + Offer Count-Text
 * - Payment1 Item + Payment1 Count-Text
 * - Payment2 Item + Payment2 Count-Text
 */
public class ShopRenderConfig {
    private final SlotRenderConfig offerItem;
    private final SlotRenderConfig offerCountText;
    private final SlotRenderConfig frontOfferItem;
    private final SlotRenderConfig payment1Item;
    private final SlotRenderConfig payment1CountText;
    private final SlotRenderConfig payment2Item;
    private final SlotRenderConfig payment2CountText;

    private final boolean offerItemFloating;
    private final int offerItemDisplayCount;
    private final boolean showFrontOffer;
    private final boolean showTradeArrow;
    private final SlotRenderConfig tradeArrow;

    private ShopRenderConfig(Builder builder) {
        this.offerItem = builder.offerItem;
        this.offerCountText = builder.offerCountText;
        this.frontOfferItem = builder.frontOfferItem;
        this.payment1Item = builder.payment1Item;
        this.payment1CountText = builder.payment1CountText;
        this.payment2Item = builder.payment2Item;
        this.payment2CountText = builder.payment2CountText;
        this.tradeArrow = builder.tradeArrow;
        this.offerItemFloating = builder.offerItemFloating;
        this.offerItemDisplayCount = builder.offerItemDisplayCount;
        this.showFrontOffer = builder.showFrontOffer;
        this.showTradeArrow = builder.showTradeArrow;
    }

    public SlotRenderConfig getOfferItem() { return offerItem; }
    public SlotRenderConfig getOfferCountText() { return offerCountText; }
    public SlotRenderConfig getFrontOfferItem() { return frontOfferItem; }
    public SlotRenderConfig getPayment1Item() { return payment1Item; }
    public SlotRenderConfig getPayment1CountText() { return payment1CountText; }
    public SlotRenderConfig getPayment2Item() { return payment2Item; }
    public SlotRenderConfig getPayment2CountText() { return payment2CountText; }
    public SlotRenderConfig getTradeArrow() { return tradeArrow; }

    public boolean isOfferItemFloating() { return offerItemFloating; }
    public int getOfferItemDisplayCount() { return offerItemDisplayCount; }
    public boolean isShowFrontOffer() { return showFrontOffer; }
    public boolean isShowTradeArrow() { return showTradeArrow; }

    private static final RenderTuningProfile COMPACT_FRONT_LAYOUT = new RenderTuningProfile(
            0.35D, 0.1D, 0.6D, 0.2D, 0.4F, 0.5D, 0.1D, 0.45D, 0.05D, 0.015F
    );

    public static final ShopRenderConfig TRADE_STAND_DEFAULT = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            .payment1Item(new SlotRenderConfig(0.30D, 0.62D, 0.08D, 0.4F))
            .payment2Item(new SlotRenderConfig(0.30D, 0.26D, 0.08D, 0.4F))
            .payment1CountText(new SlotRenderConfig(0.46D, 0.47D, 0.08D, 0.015F))
            .payment2CountText(new SlotRenderConfig(0.46D, 0.11D, 0.08D, 0.015F))
            .offerItem(new SlotRenderConfig(0.5D, 1.3D, 0.5D, 0.8F))
            .offerCountText(new SlotRenderConfig(0.5D, 1.15D, 0.5D, 0.015F))
            .build();

    public static final ShopRenderConfig TRADE_STAND = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            .payment1Item(new SlotRenderConfig(0.657D, 0.465D, 0.568D, 0.26F, 0.0F, -22.5F, 0.0F))
            .payment2Item(new SlotRenderConfig(0.657D, 0.153D, 0.568D, 0.26F, 0.0F, -22.5F, 0.0F))
            .payment1CountText(new SlotRenderConfig(0.36D, 0.525D, 0.54D, 0.020F, 0.0F, -22.5F, 0.0F))
            .payment2CountText(new SlotRenderConfig(0.36D, 0.21D, 0.54D, 0.020F, 0.0F, -22.5F, 0.0F))
            .offerItem(new SlotRenderConfig(0.5D, 1.1D, 0.5D, 0.70F))
            .offerCountText(new SlotRenderConfig(0.492D, 0.794D, 0.278D, 0.016F, 0.0F, -45.0F, 0.0F))
            .build();

    public static final ShopRenderConfig MARKET_CRATE = builder()
            .applyProfile(COMPACT_FRONT_LAYOUT)
            .payment1Item(new SlotRenderConfig(0.78D, 0.25D, 0.536D, 0.20F))
            .payment2Item(new SlotRenderConfig(0.58D, 0.25D, 0.536D, 0.20F))
            .frontOfferItem(new SlotRenderConfig(0.22D, 0.25D, 0.536D, 0.20F))
            .payment1CountText(new SlotRenderConfig(0.70D, 0.175D, 0.5315D, 0.0085F))
            .payment2CountText(new SlotRenderConfig(0.50D, 0.175D, 0.5315D, 0.0085F))
            .offerCountText(new SlotRenderConfig(0.14D, 0.175D, 0.5315D, 0.0085F))
            .tradeArrow(new SlotRenderConfig(0.40D, 0.25D, 0.5315D, 0.125F))
            .offerItem(new SlotRenderConfig(0.50D, 0.65D, 0.50D, 0.60F, 0.0F, 67.2F, 0.0F))
            .offerItemFloating(false)
            .offerItemDisplayCount(6)
            .showFrontOffer(true)
            .showTradeArrow(true)
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
        private SlotRenderConfig offerItem = new SlotRenderConfig(0.5D, 1.3D, 0.5D, 0.8F);
        private SlotRenderConfig offerCountText = new SlotRenderConfig(0.5D, 1.15D, 0.5D, 0.015F);
        private SlotRenderConfig frontOfferItem = new SlotRenderConfig(0.35D, 0.6D, 0.1D, 0.4F);
        private SlotRenderConfig tradeArrow = new SlotRenderConfig(0.5D, 0.5D, 0.1D, 0.2F);
        private SlotRenderConfig payment1Item = new SlotRenderConfig(0.35D, 0.6D, 0.1D, 0.4F);
        private SlotRenderConfig payment1CountText = new SlotRenderConfig(0.5D, 0.45D, 0.1D, 0.015F);
        private SlotRenderConfig payment2Item = new SlotRenderConfig(0.35D, 0.2D, 0.1D, 0.4F);
        private SlotRenderConfig payment2CountText = new SlotRenderConfig(0.5D, 0.05D, 0.1D, 0.015F);

        private boolean offerItemFloating = true;
        private int offerItemDisplayCount = 1;
        private boolean showFrontOffer = false;
        private boolean showTradeArrow = false;

        public Builder applyProfile(RenderTuningProfile profile) {
            this.payment1Item = new SlotRenderConfig(profile.paymentItemX, profile.payment1ItemY, profile.paymentItemZ, profile.paymentItemScale);
            this.payment2Item = new SlotRenderConfig(profile.paymentItemX, profile.payment2ItemY, profile.paymentItemZ, profile.paymentItemScale);
            this.payment1CountText = new SlotRenderConfig(profile.countTextX, profile.payment1CountTextY, profile.countTextZ, profile.countTextScale);
            this.payment2CountText = new SlotRenderConfig(profile.countTextX, profile.payment2CountTextY, profile.countTextZ, profile.countTextScale);
            return this;
        }

        public Builder offerItem(SlotRenderConfig cfg) { this.offerItem = cfg; return this; }
        public Builder offerCountText(SlotRenderConfig cfg) { this.offerCountText = cfg; return this; }
        public Builder frontOfferItem(SlotRenderConfig cfg) { this.frontOfferItem = cfg; return this; }
        public Builder tradeArrow(SlotRenderConfig cfg) { this.tradeArrow = cfg; return this; }
        public Builder payment1Item(SlotRenderConfig cfg) { this.payment1Item = cfg; return this; }
        public Builder payment1CountText(SlotRenderConfig cfg) { this.payment1CountText = cfg; return this; }
        public Builder payment2Item(SlotRenderConfig cfg) { this.payment2Item = cfg; return this; }
        public Builder payment2CountText(SlotRenderConfig cfg) { this.payment2CountText = cfg; return this; }

        public Builder offerItemFloating(boolean v) { this.offerItemFloating = v; return this; }
        public Builder offerItemDisplayCount(int v) { this.offerItemDisplayCount = v; return this; }
        public Builder showFrontOffer(boolean v) { this.showFrontOffer = v; return this; }
        public Builder showTradeArrow(boolean v) { this.showTradeArrow = v; return this; }

        public ShopRenderConfig build() {
            ShopRenderConfig config = new ShopRenderConfig(this);
            return config;
        }
    }
}
