package de.bigbull.marketblocks.feature.visual.npc;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.Minecraft;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Client-only runtime state for BER-based visual NPC animations.
 */
public class ShopNpcAnimationState {
    private int lastAnimationNonce = -1;
    private boolean animationNonceInitialized = false;
    private long spawnAnimationStartTick = -1L;
    private long despawnAnimationStartTick = -1L;
    private int lastPurchaseCounter = 0;
    private boolean purchaseCounterInitialized = false;
    private int lastPaymentSuccessCounter = 0;
    private int lastPaymentFailCounter = 0;
    private boolean paymentFeedbackCountersInitialized = false;
    private boolean spawnLandSoundPlayed = false;
    private long lastSpawnWhooshTick = -1L;
    private long lastPurchaseFeedbackTick = -1L;
    private long lastPaymentFeedbackTick = -1L;
    private float smoothedYaw = 180.0F;
    private boolean yawInitialized = false;
    private float smoothedPitch = 0.0F;
    private boolean pitchInitialized = false;
    private Villager cachedRenderVillager;
    private RemotePlayer cachedRenderPlayer;
    private String lastPlayerSkinInput;

    public int getLastAnimationNonce() {
        return lastAnimationNonce;
    }

    public void setLastAnimationNonce(int lastAnimationNonce) {
        this.lastAnimationNonce = lastAnimationNonce;
        this.animationNonceInitialized = true;
    }

    public boolean isAnimationNonceInitialized() {
        return animationNonceInitialized;
    }

    public void primeAnimationNonce(int animationNonce) {
        this.lastAnimationNonce = animationNonce;
        this.animationNonceInitialized = true;
    }

    public long getSpawnAnimationStartTick() {
        return spawnAnimationStartTick;
    }

    public void startSpawn(long tick) {
        this.spawnAnimationStartTick = tick;
        this.spawnLandSoundPlayed = false;
        this.lastSpawnWhooshTick = -1L;
    }

    public long getDespawnAnimationStartTick() {
        return despawnAnimationStartTick;
    }

    public void startDespawn(long tick) {
        this.despawnAnimationStartTick = tick;
    }

    public int getLastPurchaseCounter() {
        return lastPurchaseCounter;
    }

    public void setLastPurchaseCounter(int lastPurchaseCounter) {
        this.lastPurchaseCounter = lastPurchaseCounter;
        this.purchaseCounterInitialized = true;
    }

    public boolean isPurchaseCounterInitialized() {
        return purchaseCounterInitialized;
    }

    public void primePurchaseCounter(int purchaseCounter) {
        this.lastPurchaseCounter = purchaseCounter;
        this.purchaseCounterInitialized = true;
    }

    public int getLastPaymentSuccessCounter() {
        return lastPaymentSuccessCounter;
    }

    public int getLastPaymentFailCounter() {
        return lastPaymentFailCounter;
    }

    public void setPaymentFeedbackCounters(int successCounter, int failCounter) {
        this.lastPaymentSuccessCounter = successCounter;
        this.lastPaymentFailCounter = failCounter;
        this.paymentFeedbackCountersInitialized = true;
    }

    public boolean isPaymentFeedbackCountersInitialized() {
        return paymentFeedbackCountersInitialized;
    }

    public long getLastPurchaseFeedbackTick() {
        return lastPurchaseFeedbackTick;
    }

    public void setLastPurchaseFeedbackTick(long lastPurchaseFeedbackTick) {
        this.lastPurchaseFeedbackTick = lastPurchaseFeedbackTick;
    }

    public long getLastPaymentFeedbackTick() {
        return lastPaymentFeedbackTick;
    }

    public void setLastPaymentFeedbackTick(long lastPaymentFeedbackTick) {
        this.lastPaymentFeedbackTick = lastPaymentFeedbackTick;
    }

    public boolean isSpawnLandSoundPlayed() {
        return spawnLandSoundPlayed;
    }

    public void setSpawnLandSoundPlayed(boolean spawnLandSoundPlayed) {
        this.spawnLandSoundPlayed = spawnLandSoundPlayed;
    }

    public long getLastSpawnWhooshTick() {
        return lastSpawnWhooshTick;
    }

    public void setLastSpawnWhooshTick(long lastSpawnWhooshTick) {
        this.lastSpawnWhooshTick = lastSpawnWhooshTick;
    }

    public float smoothRenderYaw(float targetYaw) {
        if (!yawInitialized) {
            smoothedYaw = targetYaw;
            yawInitialized = true;
            return smoothedYaw;
        }

        smoothedYaw = Mth.rotLerp(0.06F, smoothedYaw, targetYaw);
        return smoothedYaw;
    }

    public float smoothRenderPitch(float targetPitch) {
        if (!pitchInitialized) {
            smoothedPitch = targetPitch;
            pitchInitialized = true;
            return smoothedPitch;
        }

        smoothedPitch = Mth.lerp(0.05F, smoothedPitch, targetPitch);
        return smoothedPitch;
    }

    public Villager getOrCreateRenderVillager(Level level) {
        if (cachedRenderVillager == null || cachedRenderVillager.level() != level) {
            cachedRenderVillager = new Villager(EntityType.VILLAGER, level);
            cachedRenderVillager.noPhysics = true;
        }
        return cachedRenderVillager;
    }

    public RemotePlayer getOrCreateRenderPlayer(Level level, String input) {
        if (cachedRenderPlayer == null || cachedRenderPlayer.level() != level || !input.equals(lastPlayerSkinInput)) {
            lastPlayerSkinInput = input;

            GameProfile profile = null;
            UUID parsedUuid = null;

            try {
                if (input.length() == 36 || input.length() == 32) {
                    String uuidStr = input;
                    if (uuidStr.length() == 32) {
                        uuidStr = uuidStr.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5");
                    }
                    parsedUuid = UUID.fromString(uuidStr);
                }
            } catch (IllegalArgumentException e) {
            }

            if (Minecraft.getInstance().getConnection() != null) {
                for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
                    if (parsedUuid != null && info.getProfile().getId().equals(parsedUuid)) {
                        profile = info.getProfile();
                        break;
                    } else if (input.equalsIgnoreCase(info.getProfile().getName())) {
                        profile = info.getProfile();
                        break;
                    }
                }
            }

            if (profile == null) {
                if (parsedUuid != null) {
                    profile = new GameProfile(parsedUuid, "");
                    final UUID asyncUuid = parsedUuid;
                    CompletableFuture.supplyAsync(() -> {
                        var result = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(asyncUuid, false);
                        return result != null ? result.profile() : null;
                    }, Util.backgroundExecutor()).thenAcceptAsync(filledProfile -> {
                        if (filledProfile != null && input.equals(this.lastPlayerSkinInput) && this.cachedRenderPlayer != null && this.cachedRenderPlayer.level() == level) {
                            this.cachedRenderPlayer = createCustomSkinPlayer((ClientLevel) level, filledProfile);
                        }
                    }, Minecraft.getInstance());
                } else {
                    profile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(input), input);
                    SkullBlockEntity.fetchGameProfile(input).thenAcceptAsync(optProfile -> {
                        if (optProfile.isPresent() && input.equals(this.lastPlayerSkinInput) && this.cachedRenderPlayer != null && this.cachedRenderPlayer.level() == level) {
                            this.cachedRenderPlayer = createCustomSkinPlayer((ClientLevel) level, optProfile.get());
                        }
                    }, Minecraft.getInstance());
                }
            }

            this.cachedRenderPlayer = createCustomSkinPlayer((ClientLevel) level, profile);
        }
        return cachedRenderPlayer;
    }

    private RemotePlayer createCustomSkinPlayer(ClientLevel level, GameProfile profile) {
        RemotePlayer player = new RemotePlayer(level, profile) {
            @Override
            public PlayerSkin getSkin() {
                return Minecraft.getInstance().getSkinManager().getInsecureSkin(this.getGameProfile());
            }

            @Override
            public boolean isModelPartShown(PlayerModelPart part) {
                return true;
            }

            @Override
            public boolean isInvisibleTo(net.minecraft.world.entity.player.Player player) {
                return !this.isCustomNameVisible() || !super.hasCustomName();
            }

            @Override
            public boolean shouldShowName() {
                return super.hasCustomName() && this.isCustomNameVisible();
            }

            @Override
            public boolean hasCustomName() {
                return super.hasCustomName() && this.isCustomNameVisible();
            }

            @Override
            public Component getDisplayName() {
                return super.hasCustomName() ? this.getCustomName() : Component.empty();
            }
        };
        player.noPhysics = true;
        return player;
    }
}
