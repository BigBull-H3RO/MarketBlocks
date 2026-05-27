package de.bigbull.marketblocks.feature.marketplace.block;

import de.bigbull.marketblocks.feature.marketplace.entity.MarketplaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

public class MarketplaceBlock extends BaseEntityBlock {

    public static final MapCodec<MarketplaceBlock> CODEC = simpleCodec(MarketplaceBlock::new);

    public MarketplaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarketplaceBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
