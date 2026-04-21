package de.bigbull.marketblocks.shop.visual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record VisualNpcPlacement(BlockPos standSurface, Vec3 spawnPos, VisualNpcPlacementResult result) {
    public boolean canSpawn() {
        return result.canSpawn();
    }
}

