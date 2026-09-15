package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
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
        BlockPos standPos = getTargetStandingPos(entity.level(), entity.getTargetShop());
        return entity.distanceToSqr(Vec3.atCenterOf(standPos)) > (stopDistance * stopDistance);
    }

    public static BlockPos getTargetStandingPos(Level level, BlockPos shopPos) {
        BlockState state = level.getBlockState(shopPos);
        Direction facing = Direction.NORTH;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        BlockPos directFront = shopPos.relative(facing);
        if (isWalkable(level, directFront)) {
            return directFront;
        }

        // Counter / Obstacle handling:
        // Try positions in front of the counter or slightly to the sides
        BlockPos[] candidates = new BlockPos[] {
            directFront.relative(facing),                      // 2 blocks out (e.g. behind 1-block counter)
            directFront.relative(facing.getClockWise()),        // diagonal right
            directFront.relative(facing.getCounterClockWise()), // diagonal left
            directFront.relative(facing, 2),                    // 3 blocks out (for wider counter)
            shopPos.relative(facing.getClockWise()),           // adjacent right
            shopPos.relative(facing.getCounterClockWise())      // adjacent left
        };

        for (BlockPos cand : candidates) {
            for (int dy = 0; dy >= -1; dy--) {
                BlockPos testPos = cand.above(dy);
                if (isWalkable(level, testPos)) {
                    return testPos;
                }
            }
            if (isWalkable(level, cand.above(1))) {
                return cand.above(1);
            }
        }

        return directFront;
    }

    private static boolean isWalkable(Level level, BlockPos pos) {
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState floor = level.getBlockState(pos.below());

        boolean feetPassable = feet.isPathfindable(PathComputationType.LAND);
        boolean headPassable = head.isPathfindable(PathComputationType.LAND);
        boolean floorSolid = !floor.isAir() && floor.getFluidState().isEmpty();

        return feetPassable && headPassable && floorSolid;
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
        BlockPos standPos = getTargetStandingPos(entity.level(), entity.getTargetShop());
        entity.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D, speedModifier);
    }

    @Override
    public void tick() {
        pathfindTimer++;

        BlockPos pos = entity.getTargetShop();
        if (pos == null) return;

        BlockPos standPos = getTargetStandingPos(entity.level(), pos);
        entity.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10.0F,
                (float) entity.getMaxHeadXRot());

        // Re-path occasionally if needed, but the navigation handles basic pathfinding
        if (entity.getNavigation().isDone()
                && entity.distanceToSqr(Vec3.atCenterOf(standPos)) > (stopDistance * stopDistance)) {
            entity.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D,
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
