package de.bigbull.marketblocks.feature.marketplace.entity;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the MarketplaceBlock.
 * Currently serves as a simple marker entity without complex logic.
 */
public class MarketplaceBlockEntity extends BlockEntity {
    public MarketplaceBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.MARKETPLACE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide()) {
            de.bigbull.marketblocks.compat.journeymap.JourneyMapCompat.addMarketplaceMarker(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && level.isClientSide()) {
            de.bigbull.marketblocks.compat.journeymap.JourneyMapCompat.removeShopMarker(worldPosition);
        }
        super.setRemoved();
    }
}
