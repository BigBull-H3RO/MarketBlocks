package de.bigbull.marketblocks.util.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bigbull.marketblocks.init.RegistriesInit;
import de.bigbull.marketblocks.shop.visual.IVisualShopNPC;
import de.bigbull.marketblocks.shop.visual.ShopNpcAnimationState;
import de.bigbull.marketblocks.shop.visual.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.shop.visual.ShopVisualSettings;
import de.bigbull.marketblocks.shop.visual.VisualNpcAnimationEvent;
import de.bigbull.marketblocks.shop.visual.VisualNpcPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public final class VisualShopNpcRenderer {
    private static final float SPAWN_DROP_START_Y = 56.0F;
    private static final int SPAWN_FALL_SOUND_START_OFFSET_TICKS = 0;
    private static final float SPAWN_DROP_DURATION = 52.0F;
    private static final float SPAWN_BOUNCE_DURATION = 8.0F;
    private static final float DESPAWN_DURATION = 10.0F;
    private static final float HEAD_YAW_LIMIT = 58.0F;
    private static final float PLAYER_TRACK_RANGE = 8.0F;
    private static final float SPAWN_FALL_SOUND_VOLUME = 0.40F;
    private static final float SPAWN_FALL_SOUND_PITCH = 1.00F;
    private static final float SPAWN_BOUNCE_SOUND_VOLUME = 0.72F;
    private static final float SPAWN_BOUNCE_SOUND_PITCH = 1.18F;

    private VisualShopNpcRenderer() {
    }

    public static void render(IVisualShopNPC host, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Level level = host.getVisualLevel();
        if (level == null) {
            return;
        }

        ShopVisualSettings settings = host.getVisualSettings();
        ShopNpcAnimationState state = host.getVisualAnimationState();
        long now = level.getGameTime();

        processAnimationEvents(host, settings, state, level, now);
        processPurchaseEffects(host, settings, state, level);

        boolean spawnActive = state.getSpawnAnimationStartTick() >= 0 && now - state.getSpawnAnimationStartTick() <= (long) (SPAWN_DROP_DURATION + SPAWN_BOUNCE_DURATION);
        boolean despawnActive = state.getDespawnAnimationStartTick() >= 0 && now - state.getDespawnAnimationStartTick() <= (long) DESPAWN_DURATION;
        if (!settings.npcEnabled() && !spawnActive && !despawnActive) {
            return;
        }

        VisualNpcPlacement placement = ShopVisualPlacementValidator.validate(level, host.getVisualShopPos(), host.getVisualFacing());
        if (!placement.canSpawn()) {
            return;
        }

        float animationYOffset = calculateAnimationYOffset(state, now, partialTick, spawnActive, despawnActive);
        Vec3 spawnPos = placement.spawnPos();
        if (spawnActive) {
            float age = (now - state.getSpawnAnimationStartTick()) + partialTick;
            if (age < SPAWN_DROP_DURATION && age >= SPAWN_FALL_SOUND_START_OFFSET_TICKS && state.getLastSpawnWhooshTick() < 0) {
                level.playLocalSound(spawnPos.x, spawnPos.y + Math.max(animationYOffset, 2.0F), spawnPos.z,
                        RegistriesInit.VISUAL_NPC_FALL_SOUND.get(), SoundSource.BLOCKS, SPAWN_FALL_SOUND_VOLUME, SPAWN_FALL_SOUND_PITCH, false);
                state.setLastSpawnWhooshTick(now);
            }
        }
        if (spawnActive && !state.isSpawnLandSoundPlayed() && now - state.getSpawnAnimationStartTick() >= (long) SPAWN_DROP_DURATION) {
            level.playLocalSound(spawnPos.x, spawnPos.y, spawnPos.z,
                    SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, SPAWN_BOUNCE_SOUND_VOLUME, SPAWN_BOUNCE_SOUND_PITCH, false);
            state.setSpawnLandSoundPlayed(true);
        }
        float bodyYaw = host.getVisualFacing().toYRot();
        LookTarget lookTarget = calculateLookTarget(level, spawnPos, now, partialTick, bodyYaw);
        float clampedHeadYaw = clampRelativeYaw(bodyYaw, lookTarget.yaw(), HEAD_YAW_LIMIT);
        float headYaw = state.smoothRenderYaw(clampedHeadYaw);
        float headPitch = state.smoothRenderPitch(lookTarget.pitch());

        Villager villager = state.getOrCreateRenderVillager(level);
        villager.noCulling = true;
        VillagerData data = villager.getVillagerData().setProfession(settings.profession().toVillagerProfession());
        villager.setVillagerData(data);
        villager.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        villager.setYRot(bodyYaw);
        villager.yBodyRot = bodyYaw;
        villager.yBodyRotO = bodyYaw;
        villager.setYHeadRot(headYaw);
        villager.yHeadRot = headYaw;
        villager.yHeadRotO = headYaw;
        villager.setXRot(headPitch);
        villager.xRotO = headPitch;
        villager.tickCount = (int) now;
        if (!settings.npcName().isBlank()) {
            villager.setCustomNameVisible(true);
            villager.setCustomName(net.minecraft.network.chat.Component.literal(settings.npcName()));
        } else {
            villager.setCustomNameVisible(false);
            villager.setCustomName(null);
        }

        BlockPos shopPos = host.getVisualShopPos();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.render(
                villager,
                spawnPos.x - shopPos.getX(),
                spawnPos.y - shopPos.getY() + animationYOffset,
                spawnPos.z - shopPos.getZ(),
                bodyYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight
        );
    }

    private static void processAnimationEvents(IVisualShopNPC host, ShopVisualSettings settings, ShopNpcAnimationState state, Level level, long now) {
        int currentNonce = host.getVisualAnimationNonce();
        if (!state.isAnimationNonceInitialized()) {
            // First client sync: adopt nonce without replaying stale animation events.
            state.primeAnimationNonce(currentNonce);
            return;
        }

        if (state.getLastAnimationNonce() == currentNonce) {
            return;
        }

        byte event = host.getVisualAnimationEvent();
        if (event == VisualNpcAnimationEvent.NONE) {
            state.setLastAnimationNonce(currentNonce);
            return;
        }

        VisualNpcPlacement placement = ShopVisualPlacementValidator.validate(level, host.getVisualShopPos(), host.getVisualFacing());
        if (!placement.canSpawn()) {
            return;
        }

        // Consume nonce only after this event can really be processed.
        state.setLastAnimationNonce(currentNonce);

        if (event == VisualNpcAnimationEvent.SPAWN && settings.npcEnabled()) {
            state.startSpawn(now);
            return;
        }

        if (event == VisualNpcAnimationEvent.DESPAWN) {
            state.startDespawn(now);
            spawnPuff(level, placement.spawnPos());
            level.playLocalSound(placement.spawnPos().x, placement.spawnPos().y + 0.6D, placement.spawnPos().z,
                    SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.BLOCKS, 0.8F, 1.15F, false);
        }
    }

    private static void processPurchaseEffects(IVisualShopNPC host, ShopVisualSettings settings, ShopNpcAnimationState state, Level level) {
        int currentCounter = host.getVisualPurchaseCounter();
        if (currentCounter <= state.getLastPurchaseCounter()) {
            return;
        }

        VisualNpcPlacement placement = ShopVisualPlacementValidator.validate(level, host.getVisualShopPos(), host.getVisualFacing());
        if (placement.canSpawn() && settings.npcEnabled()) {
            if (settings.purchaseParticlesEnabled()) {
                for (int i = 0; i < 10; i++) {
                    double angle = (Math.PI * 2.0D * i / 10.0D) + level.random.nextDouble() * 0.45D;
                    double radius = 0.62D + level.random.nextDouble() * 0.24D;
                    double px = placement.spawnPos().x + Math.cos(angle) * radius;
                    double pz = placement.spawnPos().z + Math.sin(angle) * radius;
                    double py = placement.spawnPos().y + 0.25D + level.random.nextDouble() * 1.4D;
                    level.addParticle(
                            ParticleTypes.HAPPY_VILLAGER,
                            px,
                            py,
                            pz,
                            (px - placement.spawnPos().x) * 0.02D,
                            0.03D,
                            (pz - placement.spawnPos().z) * 0.02D
                    );
                }
            }
            if (settings.purchaseSoundsEnabled()) {
                level.playLocalSound(placement.spawnPos().x, placement.spawnPos().y + 1.0D, placement.spawnPos().z,
                        SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 0.6F, 1.0F, false);
            }
        }

        state.setLastPurchaseCounter(currentCounter);
    }

    private static float calculateAnimationYOffset(ShopNpcAnimationState state, long now, float partialTick, boolean spawnActive, boolean despawnActive) {
        if (spawnActive) {
            float age = (now - state.getSpawnAnimationStartTick()) + partialTick;
            if (age <= SPAWN_DROP_DURATION) {
                float progress = Mth.clamp(age / SPAWN_DROP_DURATION, 0.0F, 1.0F);
                float eased = 1.0F - progress * progress * progress;
                return SPAWN_DROP_START_Y * eased;
            }
            float bounceProgress = Mth.clamp((age - SPAWN_DROP_DURATION) / SPAWN_BOUNCE_DURATION, 0.0F, 1.0F);
            return Mth.sin((1.0F - bounceProgress) * (float) Math.PI) * 0.16F;
        }

        if (despawnActive) {
            float age = (now - state.getDespawnAnimationStartTick()) + partialTick;
            float progress = Mth.clamp(age / DESPAWN_DURATION, 0.0F, 1.0F);
            return progress * 0.5F;
        }

        return 0.0F;
    }

    private static LookTarget calculateLookTarget(Level level, Vec3 spawnPos, long now, float partialTick, float fallbackYaw) {
        Player nearest = level.getNearestPlayer(spawnPos.x, spawnPos.y, spawnPos.z, PLAYER_TRACK_RANGE, false);
        if (nearest == null) {
            float idlePitch = calculateIdleNodPitch(now, partialTick);
            return new LookTarget(fallbackYaw, idlePitch);
        }

        double dx = nearest.getX() - spawnPos.x;
        double dz = nearest.getZ() - spawnPos.z;
        double dy = nearest.getEyeY() - (spawnPos.y + 1.42D);
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
        float pitch = Mth.clamp((float) (-(Mth.atan2(dy, horizontal) * (180F / Math.PI))), -18.0F, 14.0F);
        return new LookTarget(yaw, pitch);
    }

    private static float clampRelativeYaw(float bodyYaw, float targetYaw, float maxDelta) {
        float delta = Mth.wrapDegrees(targetYaw - bodyYaw);
        float clamped = Mth.clamp(delta, -maxDelta, maxDelta);
        return bodyYaw + clamped;
    }

    private static float calculateIdleNodPitch(long now, float partialTick) {
        float time = now + partialTick;
        float primary = Mth.sin(time * 0.11F) * 2.35F;
        float secondary = Mth.sin(time * 0.037F + 1.3F) * 0.85F;
        return primary + secondary;
    }

    private record LookTarget(float yaw, float pitch) {
    }

    private static void spawnPuff(Level level, Vec3 spawnPos) {
        for (int i = 0; i < 16; i++) {
            level.addParticle(
                    ParticleTypes.CLOUD,
                    spawnPos.x + (level.random.nextDouble() - 0.5D) * 0.8D,
                    spawnPos.y + 0.9D + level.random.nextDouble() * 0.5D,
                    spawnPos.z + (level.random.nextDouble() - 0.5D) * 0.8D,
                    (level.random.nextDouble() - 0.5D) * 0.04D,
                    0.05D,
                    (level.random.nextDouble() - 0.5D) * 0.04D
            );
        }
    }
}





