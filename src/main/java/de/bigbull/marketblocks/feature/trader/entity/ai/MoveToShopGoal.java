package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MoveToShopGoal extends Goal {
    private final ShopBuyerEntity entity;
    private final double speedModifier;
    private final float stopDistance;

    /** Maximum ticks to spend trying to reach a shop before giving up. */
    private static final int MAX_PATHFIND_TICKS = 600; // ~30 seconds
    private int pathfindTimer;

    public MoveToShopGoal(ShopBuyerEntity entity, double speedModifier, float stopDistance) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (entity.isInvisible()) return false;
        if (entity.getTargetShop() == null)
            return false;
        BlockPos frontPos = getFrontPos(entity.getTargetShop());
        return entity.distanceToSqr(Vec3.atCenterOf(frontPos)) > (stopDistance * stopDistance);
    }

    private BlockPos getFrontPos(BlockPos shopPos) {
        BlockState state = entity.level().getBlockState(shopPos);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return shopPos.relative(facing);
        }
        return shopPos;
    }

    @Override
    public boolean canContinueToUse() {
        if (entity.isInvisible()) return false;
        if (entity.getTargetShop() == null) return false;

        // Timeout: give up if pathfinding takes too long
        if (pathfindTimer >= MAX_PATHFIND_TICKS) {
            return false;
        }

        return canUse() && !entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.pathfindTimer = 0;
        BlockPos frontPos = getFrontPos(entity.getTargetShop());
        entity.getNavigation().moveTo(frontPos.getX() + 0.5D, frontPos.getY(), frontPos.getZ() + 0.5D, speedModifier);
    }

    @Override
    public void tick() {
        pathfindTimer++;

        BlockPos pos = entity.getTargetShop();
        if (pos == null) return;

        BlockPos frontPos = getFrontPos(pos);
        entity.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10.0F,
                (float) entity.getMaxHeadXRot());

        // Re-path occasionally if needed, but the navigation handles basic pathfinding
        if (entity.getNavigation().isDone()
                && entity.distanceToSqr(Vec3.atCenterOf(frontPos)) > (stopDistance * stopDistance)) {
            entity.getNavigation().moveTo(frontPos.getX() + 0.5D, frontPos.getY(), frontPos.getZ() + 0.5D,
                    speedModifier);
        }
    }

    @Override
    public void stop() {
        entity.getNavigation().stop();

        // If we timed out, abandon this shop target so the trader can find another
        if (pathfindTimer >= MAX_PATHFIND_TICKS && entity.getTargetShop() != null) {
            entity.addVisitedShop(entity.getTargetShop());
            entity.setTargetShop(null);
        }
    }
}
