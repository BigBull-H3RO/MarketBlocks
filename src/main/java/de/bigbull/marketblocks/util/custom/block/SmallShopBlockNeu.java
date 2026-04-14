package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.block.ShopBlockConfig;
import de.bigbull.marketblocks.util.block.ShopRenderConfig;
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

    // Feste Shape ohne Vitrine (Y geht nur bis 11)
    private static final VoxelShape SHAPE_NO_SHOWCASE = Block.box(0, 0, 0, 16, 11, 16);

    public SmallShopBlockNeu(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HAS_SHOWCASE, false));
    }

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.SMALL_SHOP_NEU_SHAPE;
    }

    @Override
    public ShopRenderConfig getRenderConfig(BlockState state) {
        return ShopRenderConfig.SMALL_SHOP_NEU;
    }

    /** * Hilfsmethode: Prüft, ob der übergebene BlockState dieser Shop ist und eine Vitrine hat.
     */
    public static boolean hasShowcase(BlockState state) {
        return state.is(RegistriesInit.SMALL_SHOP_BLOCK.get()) && state.getValue(HAS_SHOWCASE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE_NO_SHOWCASE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_SHOWCASE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.setValue(HAS_SHOWCASE, false) : null;
    }

    /**
     * Versucht, dem Shop eine Vitrine hinzuzufügen.
     * Aufgerufen durch ein Event (Shift-Rechtsklick mit Glas).
     */
    public static InteractionResult tryEnableShowcase(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (!state.is(RegistriesInit.SMALL_SHOP_BLOCK.get()) || state.getValue(HAS_SHOWCASE)) {
            return InteractionResult.PASS;
        }

        BlockPos topPos = pos.above();
        BlockState topState = level.getBlockState(topPos);
        Block topBlock = RegistriesInit.SMALL_SHOP_BLOCK_TOP.get();
        boolean topAlreadyPresent = topState.is(topBlock);

        // Prüfen, ob Platz für den Top-Block ist
        if (!topAlreadyPresent && !topState.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop && !shop.isOwner(player)) {
                return InteractionResult.FAIL; // Nur der Besitzer darf das
            }

            level.setBlock(pos, state.setValue(HAS_SHOWCASE, true), 3);
            ensureTopBlock(level, pos);

            if (!topAlreadyPresent && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Versucht, die Vitrine des Shops zu entfernen.
     * Aufgerufen durch ein Event (Shift-Rechtsklick mit Axt).
     */
    public static InteractionResult tryDisableShowcase(Level level, BlockPos pos, BlockState state, Player player) {
        if (!state.is(RegistriesInit.SMALL_SHOP_BLOCK.get()) || !state.getValue(HAS_SHOWCASE)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop && !shop.isOwner(player)) {
                return InteractionResult.FAIL; // Nur der Besitzer darf das
            }

            // Status zurücksetzen und Glas droppen
            level.setBlock(pos, state.setValue(HAS_SHOWCASE, false), 3);

            BlockPos topPos = pos.above();
            if (level.getBlockState(topPos).is(RegistriesInit.SMALL_SHOP_BLOCK_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }

            popResource(level, pos, new ItemStack(Items.GLASS));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock()) && state.getValue(HAS_SHOWCASE)) {
            ensureTopBlock(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos topPos = pos.above();
            if (level.getBlockState(topPos).is(RegistriesInit.SMALL_SHOP_BLOCK_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Garantiert, dass der Top-Block existiert, sofern HAS_SHOWCASE aktiv ist.
     * Repariert auch Migrationsprobleme (Block existiert, aber State ist falsch).
     */
    public static void ensureTopBlock(Level level, BlockPos basePos) {
        if (level.isClientSide) return;

        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(RegistriesInit.SMALL_SHOP_BLOCK.get())) return;

        BlockPos topPos = basePos.above();
        BlockState topState = level.getBlockState(topPos);
        Block topBlock = RegistriesInit.SMALL_SHOP_BLOCK_TOP.get();

        // Migrationspfad: Top-Block ist da, aber der Base-Block weiß nichts davon
        if (!baseState.getValue(HAS_SHOWCASE) && topState.is(topBlock)) {
            level.setBlock(basePos, baseState.setValue(HAS_SHOWCASE, true), 3);
            return;
        }

        // Reguläres Verhalten: Top-Block platzieren, falls noch nicht vorhanden
        if (baseState.getValue(HAS_SHOWCASE) && !topState.is(topBlock)) {
            if (topState.canBeReplaced()) {
                level.setBlock(topPos, topBlock.defaultBlockState(), 3);
            }
        }
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}