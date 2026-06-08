package de.bigbull.marketblocks.feature.singleoffer.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Represents the market crate shop variant block.
 * Uses a unique, slightly slanted VoxelShape to match its 3D model.
 */
public class MarketCrateBlock extends BaseShopBlock {
    public static final MapCodec<MarketCrateBlock> CODEC = simpleCodec(MarketCrateBlock::new);

    private static final VoxelShape BASE_SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 8, 16),
            Block.box(2, 8, 1, 14, 10, 2),
            Block.box(2, 8, 14, 14, 15, 15),
            Block.box(1, 8, 11, 2, 15, 15),
            Block.box(1, 8, 8, 2, 14, 11),
            Block.box(1, 8, 4, 2, 12, 8),
            Block.box(1, 8, 1, 2, 11, 4),
            Block.box(14, 8, 11, 15, 15, 15),
            Block.box(14, 8, 8, 15, 14, 11),
            Block.box(14, 8, 4, 15, 12, 8),
            Block.box(14, 8, 1, 15, 11, 4),

            Block.box(0, 9.5, 0, 16, 11.5, 1.5),
            Block.box(0, 15, 13.5, 16, 16, 16),
            Block.box(0, 10.5, 1.5, 2.5, 12.5, 4.5),
            Block.box(0, 11.5, 4.5, 2.5, 13.5, 7.5),
            Block.box(0, 12.5, 7.5, 2.5, 14.5, 10.5),
            Block.box(0, 14, 10.5, 2.5, 16, 13.5),
            Block.box(13.5, 10.5, 1.5, 16, 12.5, 4.5),
            Block.box(13.5, 11.5, 4.5, 16, 13.5, 7.5),
            Block.box(13.5, 12.5, 7.5, 16, 14.5, 10.5),
            Block.box(13.5, 14, 10.5, 16, 16, 13.5));

    private static final VoxelShape SHAPE_EAST = rotateShape(Direction.NORTH, Direction.EAST, BASE_SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, BASE_SHAPE_NORTH);
    private static final VoxelShape SHAPE_WEST = rotateShape(Direction.NORTH, Direction.WEST, BASE_SHAPE_NORTH);

    public MarketCrateBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
                    Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    private VoxelShape getRotatedShape(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return switch (facing) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> BASE_SHAPE_NORTH;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getRotatedShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getRotatedShape(state);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getRotatedShape(state);
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
