package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LeaveAndDespawnGoal extends Goal {
    private final ShopBuyerEntity entity;
    private final double speedModifier;
    private int despawnTimer;

    /** How far away the trader should try to walk before vanishing. */
    private static final double WALK_AWAY_DISTANCE = 16.0D;

    public LeaveAndDespawnGoal(ShopBuyerEntity entity, double speedModifier) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return entity.getBudget() <= 0;
    }

    @Override
    public void start() {
        this.despawnTimer = 200; // 10 seconds – more gradual than the original 7 seconds
        entity.setTargetShop(null);

        // Try to walk away from the nearest player for a natural exit
        walkAwayFromPlayers();
    }

    @Override
    public void tick() {
        // Keep trying to walk away if navigation finishes early
        if (entity.getNavigation().isDone()) {
            walkAwayFromPlayers();
        }
        
        despawnTimer--;
        if (despawnTimer <= 0) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                // Poof effect
                serverLevel.sendParticles(ParticleTypes.POOF, 
                        entity.getX(), entity.getY() + 1.0D, entity.getZ(), 
                        10, 0.5, 0.5, 0.5, 0.05);
                serverLevel.playSound(null, entity.blockPosition(), SoundEvents.WANDERING_TRADER_DISAPPEARED, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            entity.discard();
        }
    }

    /**
     * Attempts to walk away from the nearest player.
     * Falls back to a random direction if no player is nearby.
     */
    private void walkAwayFromPlayers() {
        Player nearest = entity.level().getNearestPlayer(entity, 32.0D);
        Vec3 targetPos = null;

        if (nearest != null) {
            // Calculate direction away from the nearest player
            Vec3 awayDir = entity.position().subtract(nearest.position()).normalize();
            double distance = 10.0D + entity.getRandom().nextInt(6); // 10-15 blocks away

            targetPos = entity.position().add(
                    awayDir.x * distance,
                    0,
                    awayDir.z * distance
            );
        }

        if (targetPos == null) {
            // Fallback: random position
            Vec3 pos = net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(entity, 10, 7);
            if (pos != null) {
                targetPos = pos;
            }
        }

        if (targetPos != null) {
            entity.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speedModifier);
        }
    }
}
