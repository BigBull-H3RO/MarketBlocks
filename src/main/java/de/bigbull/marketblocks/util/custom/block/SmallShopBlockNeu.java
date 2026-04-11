package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.block.ShopBlockConfig;
import de.bigbull.marketblocks.util.block.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SmallShopBlockNeu extends BaseShopBlock {
    public static final MapCodec<SmallShopBlockNeu> CODEC = simpleCodec(SmallShopBlockNeu::new);
    public static final BooleanProperty HAS_SHOWCASE = BooleanProperty.create("has_showcase");

    private static final VoxelShape BASE_SHAPE_NO_SHOWCASE = Block.box(0, 0, 0, 16, 11, 16);
    private static final VoxelShape BASE_SHAPE_WITH_SHOWCASE = Block.box(0, 0, 0, 16, 16, 16);

    public SmallShopBlockNeu(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HAS_SHOWCASE, false));
    }

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.SMALL_SHOP_TALL_SHOWCASE;
    }

    public static boolean hasShowcase(BlockState state) {
        return state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())
                && state.hasProperty(HAS_SHOWCASE)
                && state.getValue(HAS_SHOWCASE);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasShowcase(state) ? BASE_SHAPE_WITH_SHOWCASE : BASE_SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasShowcase(state) ? BASE_SHAPE_WITH_SHOWCASE : BASE_SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return hasShowcase(state) ? BASE_SHAPE_WITH_SHOWCASE : BASE_SHAPE_NO_SHOWCASE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_SHOWCASE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return state.setValue(HAS_SHOWCASE, false);
    }

    public static InteractionResult tryEnableShowcase(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (!state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get()) || hasShowcase(state)) {
            return InteractionResult.PASS;
        }

        // Owner-Check: Nur der Besitzer darf die Vitrine hinzufügen
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop && !shop.isOwner(player)) {
                return InteractionResult.FAIL;
            }
        }

        BlockPos topPos = pos.above();
        Block topBlock = RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get();
        BlockState topState = level.getBlockState(topPos);
        boolean topAlreadyPresent = topState.is(topBlock);
        if (!topAlreadyPresent && !(topState.isAir() || topState.canBeReplaced())) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(HAS_SHOWCASE, true), 3);
            ensureTopBlock(level, pos);
            if (!topAlreadyPresent && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static InteractionResult tryDisableShowcase(Level level, BlockPos pos, BlockState state, Player player) {
        if (!state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get()) || !hasShowcase(state)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SmallShopBlockEntity shop && !shop.isOwner(player)) {
                return InteractionResult.FAIL;
            }

            level.setBlock(pos, state.setValue(HAS_SHOWCASE, false), 3);

            BlockPos topPos = pos.above();
            if (level.getBlockState(topPos).is(RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }

            popResource(level, pos, new ItemStack(Items.GLASS));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * setPlacedBy entfernt – onPlace übernimmt die ensureTopBlock-Logik.
     * Beide gleichzeitig würden ensureTopBlock doppelt aufrufen (und den Migration-Pfad zweimal auslösen).
     * setPlacedBy wird nach onPlace aufgerufen, onPlace reicht vollständig aus.
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            ensureTopBlock(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && fromPos.equals(pos.above())) {
            ensureTopBlock(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos topPos = pos.above();
            if (level.getBlockState(topPos).is(RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void ensureTopBlock(Level level, BlockPos basePos) {
        if (level.isClientSide) {
            return;
        }

        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get()) || !baseState.hasProperty(HAS_SHOWCASE)) {
            return;
        }

        BlockPos topPos = basePos.above();
        Block topBlock = RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get();
        BlockState topState = level.getBlockState(topPos);

        // Migration path for existing worlds from pre-HAS_SHOWCASE versions.
        if (!baseState.getValue(HAS_SHOWCASE) && topState.is(topBlock)) {
            level.setBlock(basePos, baseState.setValue(HAS_SHOWCASE, true), 3);
            return;
        }

        if (!baseState.getValue(HAS_SHOWCASE)) {
            return;
        }

        if (topState.is(topBlock)) {
            return;
        }

        if (topState.isAir() || topState.canBeReplaced()) {
            level.setBlock(topPos, topBlock.defaultBlockState(), 3);
        }
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}