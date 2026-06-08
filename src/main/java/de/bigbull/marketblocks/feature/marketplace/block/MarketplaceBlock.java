package de.bigbull.marketblocks.feature.marketplace.block;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.marketplace.entity.MarketplaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

/**
 * A physical block representation for the marketplace.
 * While the marketplace can be opened globally via keybind, this block provides
 * a physical access point in the world.
 */
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

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public float getExplosionResistance() {
        return Config.SHOP_BLAST_RESISTANCE.get().floatValue();
    }
}
