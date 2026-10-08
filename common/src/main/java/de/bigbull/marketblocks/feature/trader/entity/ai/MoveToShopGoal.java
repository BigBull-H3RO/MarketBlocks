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
    private static final int MAX_PATHFIND_TICKS = 160; // ~8 seconds
    private int pathfindTimer;

    public MoveToShopGoal(ShopBuyerEntity entity, double speedModifier, float stopDistance) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (entity.isScared()) return false;
        if (entity.isInvisible()) return false;
        if (entity.getTargetShop() == null)
            return false;
        Vec3 standVec = getTargetStandingVec(entity.level(), entity.getTargetShop(), entity);
        // If elevated on the shop block or counter, always activate to step down to ground level
        if (entity.getY() >= entity.getTargetShop().getY() + 0.30D && entity.distanceToSqr(Vec3.atCenterOf(entity.getTargetShop())) < 3.0 * 3.0) {
            return true;
        }
        return entity.distanceToSqr(standVec) > (stopDistance * stopDistance);
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
        // Try positions in front of the counter or slightly to the sides (always strictly on the front side)
        BlockPos[] candidates = new BlockPos[] {
            directFront.relative(facing),                                         // 2 blocks out (e.g. customer side of 1-block counter)
            directFront.relative(facing.getClockWise()),                           // diagonal front-right
            directFront.relative(facing.getCounterClockWise()),                    // diagonal front-left
            directFront.relative(facing).relative(facing.getClockWise()),          // 2 blocks out, diagonal right
            directFront.relative(facing).relative(facing.getCounterClockWise()),   // 2 blocks out, diagonal left
            directFront.relative(facing, 2)                                       // 3 blocks out (for wider counter)
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

    /**
     * Calculates an organic, randomized standing position in front of the shop.
     * Uses a deterministic seed based on entity ID and shop position so the customer's
     * chosen spot stays consistent during navigation while varying naturally between traders.
     */
    public static Vec3 getTargetStandingVec(Level level, BlockPos shopPos, ShopBuyerEntity entity) {
        BlockState state = level.getBlockState(shopPos);
        Direction facing = Direction.NORTH;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        BlockPos directFront = shopPos.relative(facing);
        BlockPos standBlock = getTargetStandingPos(level, shopPos);

        double fx = facing.getStepX();
        double fz = facing.getStepZ();
        double px = -fz; // lateral perpendicular
        double pz = fx;

        long seed = ((long) entity.getId() << 32) ^ shopPos.asLong();
        java.util.Random rnd = new java.util.Random(seed);

        if (standBlock.getX() == directFront.getX() && standBlock.getZ() == directFront.getZ()) {
            // Front block directly adjacent: randomize distance between 1.40m and 1.85m (never on the block!)
            double dist = 1.40D + rnd.nextDouble() * 0.45D;
            double lateral = (rnd.nextDouble() - 0.5D) * 0.40D; // +/- 0.20m lateral shift
            return new Vec3(
                    shopPos.getX() + 0.5D + fx * dist + px * lateral,
                    standBlock.getY(),
                    shopPos.getZ() + 0.5D + fz * dist + pz * lateral
            );
        }

        BlockPos twoBlocksFront = directFront.relative(facing);
        if (standBlock.getX() == twoBlocksFront.getX() && standBlock.getZ() == twoBlocksFront.getZ()) {
            // 2 blocks out (e.g. over a counter): randomize distance between 2.05m and 2.45m
            double dist = 2.05D + rnd.nextDouble() * 0.40D;
            double lateral = (rnd.nextDouble() - 0.5D) * 0.40D;
            return new Vec3(
                    shopPos.getX() + 0.5D + fx * dist + px * lateral,
                    standBlock.getY(),
                    shopPos.getZ() + 0.5D + fz * dist + pz * lateral
            );
        }

        // For diagonal positions, slight lateral jitter
        double jx = (rnd.nextDouble() - 0.5D) * 0.30D;
        double jz = (rnd.nextDouble() - 0.5D) * 0.30D;
        return new Vec3(standBlock.getX() + 0.5D + jx, standBlock.getY(), standBlock.getZ() + 0.5D + jz);
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

    /**
     * Checks whether an entity position is strictly in front of the shop (within approximately +/- 65 degrees
     * of the forward facing direction) at natural customer distance.
     * Prevents standing on top of the counter/shop or interacting from flanks/rear.
     */
    public static boolean isInFrontOfShop(Level level, BlockPos shopPos, Vec3 entityPos) {
        BlockState state = level.getBlockState(shopPos);
        Direction facing = Direction.NORTH;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        double dx = entityPos.x - (shopPos.getX() + 0.5D);
        double dz = entityPos.z - (shopPos.getZ() + 0.5D);

        // 1. Must NOT be on top of or elevated on the shop block/counter!
        // Feet higher than floor level (+0.30) within 2.0 blocks of shop center:
        if (entityPos.y >= shopPos.getY() + 0.30D && (dx * dx + dz * dz) < 4.0D) {
            return false;
        }

        double fx = facing.getStepX();
        double fz = facing.getStepZ();

        // 2. Dot product along horizontal facing vector:
        // Shop front side starts at dot >= 0.20D and extends to max trade reach (dot <= 3.5D)
        double dot = dx * fx + dz * fz;
        if (dot < 0.20D || dot > 3.5D) {
            return false;
        }

        // 3. Entity must be within frontal arc (~81 degrees of the forward axis: cos(81°) ≈ 0.156)
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist > 0.001D) {
            double cosAngle = dot / horizDist;
            return cosAngle >= 0.15D;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (entity.isScared()) return false;
        if (entity.isInvisible()) return false;
        if (entity.getTargetShop() == null) return false;

        // Timeout: give up if pathfinding takes too long
        if (pathfindTimer >= MAX_PATHFIND_TICKS) {
            return false;
        }

        // If elevated on the shop block or counter, do not abort on navigation done
        boolean elevated = entity.getY() >= entity.getTargetShop().getY() + 0.30D
                && entity.distanceToSqr(Vec3.atCenterOf(entity.getTargetShop())) < 3.0 * 3.0;
        if (elevated) {
            return true;
        }

        return canUse() && !entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.pathfindTimer = 0;
        Vec3 standVec = getTargetStandingVec(entity.level(), entity.getTargetShop(), entity);
        entity.getNavigation().moveTo(standVec.x, standVec.y, standVec.z, speedModifier);
    }

    @Override
    public void tick() {
        pathfindTimer++;

        BlockPos pos = entity.getTargetShop();
        if (pos == null) return;

        Vec3 standVec = getTargetStandingVec(entity.level(), pos, entity);
        double distSq = entity.distanceToSqr(standVec);

        boolean elevated = entity.getY() >= pos.getY() + 0.30D
                && entity.distanceToSqr(Vec3.atCenterOf(pos)) < 3.0 * 3.0;

        if (elevated) {
            // Direct physical movement push to step down towards target standing position
            entity.getMoveControl().setWantedPosition(standVec.x, standVec.y, standVec.z, speedModifier);
            entity.getLookControl().setLookAt(standVec.x, standVec.y + 0.5D, standVec.z, 20.0F, 20.0F);
            return;
        }

        // Only focus head on the shop when close to the counter (last ~2.5 blocks),
        // strictly on ground level (not elevated on the counter), and positioned in front of the shop.
        boolean onFloor = entity.getY() < pos.getY() + 0.30D;
        if (distSq <= 2.5 * 2.5 && onFloor && isInFrontOfShop(entity.level(), pos, entity.position())) {
            entity.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10.0F,
                    (float) entity.getMaxHeadXRot());
        }

        // Re-path occasionally if needed, but the navigation handles basic pathfinding
        if (entity.getNavigation().isDone()
                && distSq > (stopDistance * stopDistance)) {
            entity.getNavigation().moveTo(standVec.x, standVec.y, standVec.z,
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
