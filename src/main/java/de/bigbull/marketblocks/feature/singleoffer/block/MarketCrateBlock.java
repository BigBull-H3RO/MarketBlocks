package de.bigbull.marketblocks.feature.singleoffer.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopBlockConfig;
import de.bigbull.marketblocks.feature.singleoffer.client.render.ShopRenderConfig;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;

public class MarketCrateBlock extends BaseShopBlock {
    public static final MapCodec<MarketCrateBlock> CODEC = simpleCodec(MarketCrateBlock::new);

    public MarketCrateBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.MARKET_CRATE_SHAPE;
    }

    @Override
    public ShopRenderConfig getRenderConfig(BlockState state) {
        return ShopRenderConfig.MARKET_CRATE;
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}

