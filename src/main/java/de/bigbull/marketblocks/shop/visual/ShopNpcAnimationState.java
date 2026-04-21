package de.bigbull.marketblocks.shop.visual;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

/**
 * Client-only runtime state for BER-based visual NPC animations.
 */
public class ShopNpcAnimationState {
    private int lastAnimationNonce = -1;
    private long spawnAnimationStartTick = -1L;
    private long despawnAnimationStartTick = -1L;
    private int lastPurchaseCounter = 0;
    private boolean spawnFallSoundPlayed = false;
    private boolean spawnLandSoundPlayed = false;
    private long lastSpawnWhooshTick = -1L;
    private float smoothedYaw = 180.0F;
    private boolean yawInitialized = false;
    private float smoothedPitch = 0.0F;
    private boolean pitchInitialized = false;
    private Villager cachedRenderVillager;

    public int getLastAnimationNonce() {
        return lastAnimationNonce;
    }

    public void setLastAnimationNonce(int lastAnimationNonce) {
        this.lastAnimationNonce = lastAnimationNonce;
    }

    public long getSpawnAnimationStartTick() {
        return spawnAnimationStartTick;
    }

    public void startSpawn(long tick) {
        this.spawnAnimationStartTick = tick;
        this.spawnFallSoundPlayed = false;
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
    }

    public boolean isSpawnFallSoundPlayed() {
        return spawnFallSoundPlayed;
    }

    public void setSpawnFallSoundPlayed(boolean spawnFallSoundPlayed) {
        this.spawnFallSoundPlayed = spawnFallSoundPlayed;
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
        smoothedYaw = Mth.rotLerp(0.22F, smoothedYaw, targetYaw);
        return smoothedYaw;
    }

    public float smoothRenderPitch(float targetPitch) {
        if (!pitchInitialized) {
            smoothedPitch = targetPitch;
            pitchInitialized = true;
            return smoothedPitch;
        }
        smoothedPitch = Mth.lerp(0.18F, smoothedPitch, targetPitch);
        return smoothedPitch;
    }

    public Villager getOrCreateRenderVillager(Level level) {
        if (cachedRenderVillager == null || cachedRenderVillager.level() != level) {
            cachedRenderVillager = new Villager(EntityType.VILLAGER, level);
            cachedRenderVillager.noPhysics = true;
        }
        return cachedRenderVillager;
    }
}



