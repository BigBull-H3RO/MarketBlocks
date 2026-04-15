package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.block.ShopBlockConfig;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SmallShopBlock extends BaseShopBlock {
    public static final MapCodec<SmallShopBlock> CODEC = simpleCodec(SmallShopBlock::new);

    public SmallShopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.SMALL_SHOP_DEFAULT_SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}