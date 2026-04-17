package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.block.ShopBlockConfig;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TradeStandClassicBlock extends BaseShopBlock {
    public static final MapCodec<TradeStandClassicBlock> CODEC = simpleCodec(TradeStandClassicBlock::new);

    public TradeStandClassicBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.TRADE_STAND_DEFAULT_SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}