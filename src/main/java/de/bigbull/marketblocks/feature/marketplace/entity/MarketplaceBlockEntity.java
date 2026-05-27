package de.bigbull.marketblocks.feature.marketplace.entity;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MarketplaceBlockEntity extends BlockEntity {
    public MarketplaceBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.MARKETPLACE_BLOCK_ENTITY.get(), pos, state);
    }
}
