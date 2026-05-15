#!/bin/bash
cat << 'INNER_EOF' > network_packet.patch
--- src/main/java/de/bigbull/marketblocks/network/singleoffer/UpdateSettingsPacket.java
+++ src/main/java/de/bigbull/marketblocks/network/singleoffer/UpdateSettingsPacket.java
@@ -26,12 +26,7 @@
         SideMode back,
         String name,
         boolean redstone,
-        boolean npcEnabled,
-        String npcName,
-        String npcProfession,
-        boolean purchaseParticles,
-        boolean purchaseSounds,
-        boolean paymentSlotSounds,
+        ShopVisualSettings visualSettings,
         boolean xpFeedbackSound
 ) implements CustomPacketPayload {
     public static final CustomPacketPayload.Type<UpdateSettingsPacket> TYPE =
@@ -46,12 +41,7 @@
                 ByteBufCodecs.STRING_UTF8.encode(buf, packet.back().name());
                 ByteBufCodecs.STRING_UTF8.encode(buf, packet.name());
                 ByteBufCodecs.BOOL.encode(buf, packet.redstone());
-                ByteBufCodecs.BOOL.encode(buf, packet.npcEnabled());
-                ByteBufCodecs.STRING_UTF8.encode(buf, packet.npcName());
-                ByteBufCodecs.STRING_UTF8.encode(buf, packet.npcProfession());
-                ByteBufCodecs.BOOL.encode(buf, packet.purchaseParticles());
-                ByteBufCodecs.BOOL.encode(buf, packet.purchaseSounds());
-                ByteBufCodecs.BOOL.encode(buf, packet.paymentSlotSounds());
+                ShopVisualSettings.STREAM_CODEC.encode(buf, packet.visualSettings());
                 ByteBufCodecs.BOOL.encode(buf, packet.xpFeedbackSound());
             },
             buf -> {
@@ -62,15 +52,10 @@
                 SideMode back = parseSideMode(ByteBufCodecs.STRING_UTF8.decode(buf));
                 String name = ByteBufCodecs.STRING_UTF8.decode(buf);
                 boolean redstone = ByteBufCodecs.BOOL.decode(buf);
-                boolean npcEnabled = ByteBufCodecs.BOOL.decode(buf);
-                String npcName = ByteBufCodecs.STRING_UTF8.decode(buf);
-                String npcProfession = ByteBufCodecs.STRING_UTF8.decode(buf);
-                boolean purchaseParticles = ByteBufCodecs.BOOL.decode(buf);
-                boolean purchaseSounds = ByteBufCodecs.BOOL.decode(buf);
-                boolean paymentSlotSounds = ByteBufCodecs.BOOL.decode(buf);
+                ShopVisualSettings visualSettings = ShopVisualSettings.STREAM_CODEC.decode(buf);
                 boolean xpFeedbackSound = ByteBufCodecs.BOOL.decode(buf);
                 return new UpdateSettingsPacket(pos, left, right, bottom, back, name, redstone,
-                        npcEnabled, npcName, npcProfession, purchaseParticles, purchaseSounds, paymentSlotSounds, xpFeedbackSound);
+                        visualSettings, xpFeedbackSound);
             }
     );
     @Override
@@ -98,14 +83,7 @@

                 // Sanitize shop name
                 String name = packet.name().strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
-                ShopVisualSettings visuals = new ShopVisualSettings(
-                        packet.npcEnabled(),
-                        packet.npcName(),
-                        VillagerVisualProfession.fromSerialized(packet.npcProfession()),
-                        packet.purchaseParticles(),
-                        packet.purchaseSounds(),
-                        packet.paymentSlotSounds()
-                );
+                ShopVisualSettings visuals = packet.visualSettings();

                 if (visuals.npcEnabled() && !ShopVisualPlacementValidator.validate(level, packet.pos(), facing).canSpawn()) {
                     player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.marketblocks.visual_npc.space_blocked"));
INNER_EOF
patch src/main/java/de/bigbull/marketblocks/network/singleoffer/UpdateSettingsPacket.java < network_packet.patch
