#!/bin/bash
cat << 'INNER_EOF' > shop_screen.patch
--- src/main/java/de/bigbull/marketblocks/feature/singleoffer/client/screen/SingleOfferShopScreen.java
+++ src/main/java/de/bigbull/marketblocks/feature/singleoffer/client/screen/SingleOfferShopScreen.java
@@ -113,6 +113,18 @@
     private Boolean draftVisualPurchaseSounds;
     private Boolean draftVisualPaymentSlotSounds;
     private Boolean draftPurchaseXpFeedbackSound;
+
+    private Boolean draftOfferItemVisible;
+    private Boolean draftOfferItemFullbright;
+    private Float draftOfferItemScale;
+    private Float draftOfferItemSpeed;
+    private Float draftOfferItemHeightOffset;
+    private Boolean draftOfferItemBobbing;
+    private Integer draftOfferItemCount;
+    private Float draftOfferItemRotation;
+    private Boolean draftOfferItemChaos;
+    private Float draftOfferItemSpread;
+
     private VisualNpcPlacementResult visualPlacementResult = VisualNpcPlacementResult.OK;

     // Neue Variablen für Expandable UI & Pixel-Scroll
@@ -280,6 +292,17 @@
         if (draftVisualPaymentSlotSounds == null) draftVisualPaymentSlotSounds = visualSettings.paymentSlotSoundsEnabled();
         if (draftPurchaseXpFeedbackSound == null) draftPurchaseXpFeedbackSound = be.isPurchaseXpFeedbackSound();

+        if (draftOfferItemVisible == null) draftOfferItemVisible = visualSettings.offerItemVisible();
+        if (draftOfferItemFullbright == null) draftOfferItemFullbright = visualSettings.offerItemFullbright();
+        if (draftOfferItemScale == null) draftOfferItemScale = visualSettings.offerItemScale();
+        if (draftOfferItemSpeed == null) draftOfferItemSpeed = visualSettings.offerItemSpeed();
+        if (draftOfferItemHeightOffset == null) draftOfferItemHeightOffset = visualSettings.offerItemHeightOffset();
+        if (draftOfferItemBobbing == null) draftOfferItemBobbing = visualSettings.offerItemBobbing();
+        if (draftOfferItemCount == null) draftOfferItemCount = visualSettings.offerItemCount();
+        if (draftOfferItemRotation == null) draftOfferItemRotation = visualSettings.offerItemRotation();
+        if (draftOfferItemChaos == null) draftOfferItemChaos = visualSettings.offerItemChaos();
+        if (draftOfferItemSpread == null) draftOfferItemSpread = visualSettings.offerItemSpread();
+
         emitRedstoneEnabled = draftEmitRedstone;
         visualPlacementResult = resolveVisualPlacementResult(be);

@@ -361,6 +384,37 @@
             }
             case VISUALS -> {
                 npcNameField = null;
+                if (isOwner) {
+                    net.minecraft.world.level.block.Block block = be.getBlockState().getBlock();
+                    boolean isTradeStand = block instanceof de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
+                    boolean isMarketCrate = block instanceof de.bigbull.marketblocks.feature.singleoffer.block.MarketCrateBlock;
+
+                    SingleOfferSettingsSections.buildOfferItemSection(
+                        this,
+                        isTradeStand,
+                        isMarketCrate,
+                        draftOfferItemVisible,
+                        draftOfferItemFullbright,
+                        draftOfferItemScale,
+                        draftOfferItemSpeed,
+                        draftOfferItemHeightOffset,
+                        draftOfferItemBobbing,
+                        draftOfferItemCount,
+                        draftOfferItemRotation,
+                        draftOfferItemChaos,
+                        draftOfferItemSpread,
+                        val -> draftOfferItemVisible = val,
+                        val -> draftOfferItemFullbright = val,
+                        val -> draftOfferItemScale = val,
+                        val -> draftOfferItemSpeed = val,
+                        val -> draftOfferItemHeightOffset = val,
+                        val -> draftOfferItemBobbing = val,
+                        val -> draftOfferItemCount = val,
+                        val -> draftOfferItemRotation = val,
+                        val -> draftOfferItemChaos = val,
+                        val -> draftOfferItemSpread = val
+                    );
+                }
             }
             case ACCESS -> {
                 if (isOwner) {
@@ -377,11 +431,23 @@

             String name = draftShopName != null ? draftShopName : "";
             boolean emit = emitRedstoneEnabled;
+            ShopVisualSettings currentVisuals = be.getVisualSettings();
             ShopVisualSettings visuals = new ShopVisualSettings(
                     Boolean.TRUE.equals(draftVisualNpcEnabled), draftVisualNpcName,
                     draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession,
                     Boolean.TRUE.equals(draftVisualPurchaseParticles), Boolean.TRUE.equals(draftVisualPurchaseSounds),
-                    Boolean.TRUE.equals(draftVisualPaymentSlotSounds)
+                    Boolean.TRUE.equals(draftVisualPaymentSlotSounds),
+
+                    Boolean.TRUE.equals(draftOfferItemVisible),
+                    Boolean.TRUE.equals(draftOfferItemFullbright),
+                    draftOfferItemScale != null ? draftOfferItemScale : currentVisuals.offerItemScale(),
+                    draftOfferItemSpeed != null ? draftOfferItemSpeed : currentVisuals.offerItemSpeed(),
+                    draftOfferItemHeightOffset != null ? draftOfferItemHeightOffset : currentVisuals.offerItemHeightOffset(),
+                    Boolean.TRUE.equals(draftOfferItemBobbing),
+                    draftOfferItemCount != null ? draftOfferItemCount : currentVisuals.offerItemCount(),
+                    draftOfferItemRotation != null ? draftOfferItemRotation : currentVisuals.offerItemRotation(),
+                    Boolean.TRUE.equals(draftOfferItemChaos),
+                    draftOfferItemSpread != null ? draftOfferItemSpread : currentVisuals.offerItemSpread()
             );

             be.setShopNameClient(name);
@@ -408,8 +474,7 @@

             NetworkHandler.sendToServer(new UpdateSettingsPacket(
                     be.getBlockPos(), menu.getMode(leftDir), menu.getMode(rightDir), menu.getMode(bottomDir), menu.getMode(backDir),
-                    name, emit, visuals.npcEnabled(), visuals.npcName(), visuals.profession().serializedName(),
-                    visuals.purchaseParticlesEnabled(), visuals.purchaseSoundsEnabled(), visuals.paymentSlotSoundsEnabled(),
+                    name, emit, visuals,
                     Boolean.TRUE.equals(draftPurchaseXpFeedbackSound)
             ));
             if (menu.isPrimaryOwner()) {
@@ -910,9 +975,6 @@
                 graphics.drawString(font, Component.translatable(visualPlacementResult.translationKey()), 8, 84, 0xCC3333, false);
             }
         }
-        if (menu.isOwner() && activeSettingsCategory == SettingsCategory.VISUALS) {
-            graphics.drawString(font, Component.translatable("gui.marketblocks.settings.visual.placeholder"), 10, 24, 0x808080, false);
-        }
         if (!menu.isOwner() && !canToggleAdminShop()) {
             Component info = Component.translatable("gui.marketblocks.settings_owner_only");
             int w = font.width(info);
INNER_EOF
patch src/main/java/de/bigbull/marketblocks/feature/singleoffer/client/screen/SingleOfferShopScreen.java < shop_screen.patch
