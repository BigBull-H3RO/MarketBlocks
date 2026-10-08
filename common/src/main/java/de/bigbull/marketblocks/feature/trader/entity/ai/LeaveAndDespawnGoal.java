package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LeaveAndDespawnGoal extends Goal {
    private final ShopBuyerEntity entity;
    private final double speedModifier;
    private int despawnTimer;
    private int repathCooldown;

    public LeaveAndDespawnGoal(ShopBuyerEntity entity, double speedModifier) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        // Only claim MOVE flag so head tracking (LookAtPlayer, RandomLookAround) remains natural and unblocked!
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return entity.getBudget() <= 0;
    }

    @Override
    public void start() {
        this.despawnTimer = 600; // 30 seconds max (gives plenty of time to walk naturally far away)
        this.repathCooldown = 0;
        entity.setTargetShop(null);

        // Try to walk away from the nearest player for a natural exit
        walkAwayFromPlayers();
    }

    @Override
    public void tick() {
        if (repathCooldown > 0) {
            repathCooldown--;
        }

        // Keep trying to walk away if navigation finishes early or gets interrupted
        if (entity.getNavigation().isDone() && repathCooldown <= 0) {
            walkAwayFromPlayers();
        }

        despawnTimer--;
        boolean canDespawn = false;

        double safeDistance = TraderConfig.DESPAWN_PLAYER_DISTANCE.get();

        if (despawnTimer <= 0) {
            // Absolute timeout reached: only despawn if not directly next to a player
            Player nearest = entity.level().getNearestPlayer(entity, 16.0D);
            if (nearest == null || despawnTimer < -200) {
                canDespawn = true;
            }
        } else if (despawnTimer < 500) { // After walking away for at least 5 seconds (100 ticks)
            Player nearest = entity.level().getNearestPlayer(entity, safeDistance);
            if (nearest == null) {
                canDespawn = true; // Safe to despawn when no player is within safeDistance (e.g. 36 blocks)
            } else if (entity.distanceToSqr(nearest) >= 20.0D * 20.0D && !nearest.hasLineOfSight(entity)) {
                // Out of line of sight (e.g. walked around a building/corner) and at least 20 blocks away
                canDespawn = true;
            }
        }

        if (canDespawn) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                // Poof particle effect and sound
                serverLevel.sendParticles(ParticleTypes.POOF,
                        entity.getX(), entity.getY() + 1.0D, entity.getZ(),
                        12, 0.4, 0.5, 0.4, 0.05);
                serverLevel.playSound(null, entity.blockPosition(), SoundEvents.WANDERING_TRADER_DISAPPEARED, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            entity.discard();
        }
    }

    /**
     * Attempts to walk away from the nearest player using collision-tested ground positions.
     * Falls back to a random direction if no player is nearby.
     */
    private void walkAwayFromPlayers() {
        this.repathCooldown = 30; // Repath at most once every 1.5 seconds

        double safeDistance = TraderConfig.DESPAWN_PLAYER_DISTANCE.get();
        double searchRadius = Math.max(48.0D, safeDistance + 16.0D);
        Player nearest = entity.level().getNearestPlayer(entity, searchRadius);
        Vec3 targetPos = null;

        if (nearest != null) {
            // Find a walkable ground position away from the player
            targetPos = DefaultRandomPos.getPosAway(entity, 16, 7, nearest.position());
        }

        if (targetPos == null) {
            // Fallback: random walkable position
            targetPos = DefaultRandomPos.getPos(entity, 12, 7);
        }

        if (targetPos != null) {
            entity.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speedModifier);
        }
    }
}
