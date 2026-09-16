package de.bigbull.marketblocks.platform.services;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IItemTransferHelper {
    @Nullable
    ICommonItemHandler getNeighborItemHandler(Level level, BlockPos pos, Direction side);
}