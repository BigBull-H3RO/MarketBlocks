package de.bigbull.marketblocks.shop.visual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;

public final class ShopVisualPlacementValidator {
    private ShopVisualPlacementValidator() {
    }

    public static VisualNpcPlacement validate(Level level, BlockPos shopPos, Direction facing) {
        BlockPos behind = shopPos.relative(facing.getOpposite());
        BlockPos[] candidates = new BlockPos[]{behind, behind.below()};

        boolean hasAnySurface = false;
        for (BlockPos standSurface : candidates) {
            if (!hasStandSurface(level, standSurface)) {
                continue;
            }
            hasAnySurface = true;

            Vec3 spawnPos = computeSpawnPos(level, standSurface);
            if (hasClearance(level, spawnPos)) {
                return new VisualNpcPlacement(standSurface, spawnPos, VisualNpcPlacementResult.OK);
            }
        }

        BlockPos fallback = hasStandSurface(level, behind) ? behind : behind.below();
        Vec3 fallbackSpawn = computeSpawnPos(level, fallback);
        if (!hasAnySurface) {
            return new VisualNpcPlacement(fallback, fallbackSpawn, VisualNpcPlacementResult.NO_STAND_SURFACE);
        }
        return new VisualNpcPlacement(fallback, fallbackSpawn, VisualNpcPlacementResult.BLOCKED_ABOVE);
    }

    private static boolean hasStandSurface(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    private static Vec3 computeSpawnPos(Level level, BlockPos standSurface) {
        VoxelShape shape = level.getBlockState(standSurface).getCollisionShape(level, standSurface);
        double topOffset = shape.isEmpty() ? 1.0D : shape.max(Direction.Axis.Y);
        return new Vec3(standSurface.getX() + 0.5D, standSurface.getY() + topOffset, standSurface.getZ() + 0.5D);
    }

    private static boolean hasClearance(Level level, Vec3 spawnPos) {
        AABB villagerSpace = new AABB(
                spawnPos.x - 0.30D,
                spawnPos.y,
                spawnPos.z - 0.30D,
                spawnPos.x + 0.30D,
                spawnPos.y + 1.95D,
                spawnPos.z + 0.30D
        );
        return level.noCollision(villagerSpace);
    }
}


