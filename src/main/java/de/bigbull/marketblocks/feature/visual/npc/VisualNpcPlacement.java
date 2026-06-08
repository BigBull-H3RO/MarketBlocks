package de.bigbull.marketblocks.feature.visual.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Contains the result of an NPC placement validation, including the calculated spawn position.
 */
public record VisualNpcPlacement(BlockPos standSurface, Vec3 spawnPos, VisualNpcPlacementResult result) {
    public boolean canSpawn() {
        return result.canSpawn();
    }
}

