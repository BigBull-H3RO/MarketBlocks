package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallShopBlockTop extends Block {
    public static final MapCodec<SmallShopBlockTop> CODEC = simpleCodec(SmallShopBlockTop::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 9, 15);

    public SmallShopBlockTop(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(RegistriesInit.SMALL_SHOP_BLOCK.get());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.canSurvive(level, pos)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && !state.canSurvive(level, pos)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);

        if (!baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) {
            return InteractionResult.PASS;
        }

        BlockHitResult redirectedHit = new BlockHitResult(hitResult.getLocation(), hitResult.getDirection(), basePos, hitResult.isInside());
        return baseState.useWithoutItem(level, player, redirectedHit);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);

        if (baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) {
            return SmallShopBlock.createCloneStack(baseState);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);

        if (baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) {
            return baseState.onDestroyedByPlayer(level, basePos, player, willHarvest, fluid);
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    // Leitet den eigentlichen Abbau inklusive Loot/Enchantment-Verhalten auf den Basisblock um.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);

        if (baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) {
            baseState.getBlock().playerWillDestroy(level, basePos, baseState, player);

            // Zeigt Partikel/Sound des Shop-Basisblocks statt des Top-Helferblocks.
            level.levelEvent(player, 2001, basePos, Block.getId(baseState));

            if (!level.isClientSide()) {
                if (player.isCreative()) {
                    level.setBlock(basePos, Blocks.AIR.defaultBlockState(), 35);
                } else {
                    BlockEntity blockEntity = level.getBlockEntity(basePos);
                    baseState.getBlock().playerDestroy(level, player, basePos, baseState, blockEntity, player.getMainHandItem());
                    level.setBlock(basePos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}