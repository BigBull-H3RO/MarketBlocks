package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Unsichtbarer Hilfsblock für die Glasvitrine (Y=16..25).
 * Leitet alle Klicks/Abbau-Events an den darunterliegenden Hauptblock weiter.
 */
public class SmallShopBlockNeuTop extends Block {
    public static final MapCodec<SmallShopBlockNeuTop> CODEC = simpleCodec(SmallShopBlockNeuTop::new);

    // Hitbox für die Vitrine
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 9, 16);

    public SmallShopBlockNeuTop(BlockBehaviour.Properties properties) {
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** * Überlebensbedingung: Muss auf einem SmallShopBlockNeu stehen.
     */
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

    /** * Leitet Rechtsklick an den Base-Block weiter.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);

        if (!baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) {
            return InteractionResult.PASS;
        }

        BlockHitResult redirectedHit = new BlockHitResult(
                hitResult.getLocation(), hitResult.getDirection(), basePos, hitResult.isInside()
        );
        return baseState.useWithoutItem(level, player, redirectedHit);
    }

    /** * Leitet Linksklick/Abbauen an den Base-Block weiter.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);

        if (baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) {
            return baseState.onDestroyedByPlayer(level, basePos, player, willHarvest, level.getFluidState(basePos));
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}