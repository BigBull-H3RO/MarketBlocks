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

    // Shapes: Kollision und Outline. Shape aus ShopBlockConfig wird für diese Klasse
    // NICHT verwendet – getShape/getCollisionShape/getInteractionShape werden komplett
    // überschrieben, weil sie vom HAS_SHOWCASE-Zustand abhängen.
    private static final VoxelShape SHAPE_NO_SHOWCASE   = Block.box(0, 0, 0, 16, 11, 16);

    public SmallShopBlockNeu(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HAS_SHOWCASE, false));
    }

    // -------------------------------------------------------------------------
    // ShopBlockConfig
    // -------------------------------------------------------------------------

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.SMALL_SHOP_NEU_SHAPE;
    }

    @Override
    public ShopRenderConfig getRenderConfig(BlockState state) {
        return ShopRenderConfig.SMALL_SHOP_NEU;
    }

    // -------------------------------------------------------------------------
    // BlockState-Helfer
    // -------------------------------------------------------------------------

    /** Gibt true zurück wenn der Block eine Glasvitrine hat. */
    public static boolean hasShowcase(BlockState state) {
        return state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())
                && state.hasProperty(HAS_SHOWCASE)
                && state.getValue(HAS_SHOWCASE);
    }

    // -------------------------------------------------------------------------
    // Shapes (state-abhängig)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // BlockState-Definition
    // -------------------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_SHOWCASE);
    }

    // -------------------------------------------------------------------------
    // Platzierung
    // -------------------------------------------------------------------------

    /**
     * FIX: Der frühere canBeReplaced-Check für pos.above() wurde entfernt.
     *
     * Früher platzierte dieser Block beim Setzen IMMER einen Top-Block → check war nötig.
     * Seit HAS_SHOWCASE eingeführt wurde, startet der Block ohne Vitrine (HAS_SHOWCASE=false),
     * daher braucht pos.above() beim Platzieren keinen freien Platz.
     * Der Top-Block wird erst bei tryEnableShowcase() per Shift+Klick mit Glasblock gesetzt.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(HAS_SHOWCASE, false);
    }

    // -------------------------------------------------------------------------
    // Vitrine aktivieren / deaktivieren (aufgerufen von ModGameEvents)
    // -------------------------------------------------------------------------

    /**
     * Vitrine hinzufügen: Spieler Shift+klickt mit Glasblock auf den Shop.
     * Prüft Owner-Berechtigung, setzt HAS_SHOWCASE=true und platziert den Top-Block.
     */
    public static InteractionResult tryEnableShowcase(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (!state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get()) || hasShowcase(state)) {
            return InteractionResult.PASS;
        }

        // Owner-Check nur server-seitig
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

        // Top-Block-Platz prüfen
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

    /**
     * Vitrine entfernen: Spieler Shift+klickt mit Axt auf den Shop.
     * Prüft Owner-Berechtigung, setzt HAS_SHOWCASE=false, entfernt Top-Block,
     * gibt einen Glasblock zurück.
     */
    public static InteractionResult tryDisableShowcase(Level level, BlockPos pos, BlockState state, Player player) {
        if (!state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get()) || !hasShowcase(state)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop && !shop.isOwner(player)) {
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

    // -------------------------------------------------------------------------
    // Block-Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Top-Block sicherstellen wenn der Block gesetzt wird.
     * Guard: nur wenn HAS_SHOWCASE bereits true ist (z.B. durch Welt-Migration oder /setblock).
     * Beim normalen Platzieren ist HAS_SHOWCASE=false → kein Top-Block nötig.
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock()) && hasShowcase(state)) {
            ensureTopBlock(level, pos);
        }
    }

    /**
     * NOTE: neighborChanged wird NICHT überschrieben.
     *
     * SmallShopBlockNeuTop überschreibt bereits neighborChanged + canSurvive.
     * Falls der Top-Block ohne den Base-Block existiert, entfernt er sich selbst.
     * Eine zusätzliche Überschreibung hier wäre redundant.
     */

    /**
     * Wenn der Base-Block entfernt wird → Top-Block mitentfernen.
     */
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

    // -------------------------------------------------------------------------
    // Top-Block-Verwaltung
    // -------------------------------------------------------------------------

    /**
     * Stellt sicher dass der Top-Block existiert wenn HAS_SHOWCASE=true.
     * Enthält auch einen Migrationspfad für Welten die vor Einführung von HAS_SHOWCASE
     * erstellt wurden (Top-Block vorhanden aber Property noch false).
     *
     * Aufrufstellen:
     * - onPlace (bei HAS_SHOWCASE=true)
     * - tryEnableShowcase (nach Setzen von HAS_SHOWCASE=true)
     * - SmallShopBlockEntity.onLoad (Chunk-Load-Sicherheit)
     *
     * NOT needed in: neighborChanged (→ canSurvive in Top), tick (→ canSurvive in Top)
     */
    public static void ensureTopBlock(Level level, BlockPos basePos) {
        if (level.isClientSide) return;

        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get()) || !baseState.hasProperty(HAS_SHOWCASE)) {
            return;
        }

        BlockPos topPos = basePos.above();
        Block topBlock = RegistriesInit.SMALL_SHOP_BLOCK_NEU_TOP.get();
        BlockState topState = level.getBlockState(topPos);

        // Migrationspfad: Top-Block existiert, aber HAS_SHOWCASE ist noch false
        // (Welt wurde vor Einführung von HAS_SHOWCASE erstellt)
        if (!baseState.getValue(HAS_SHOWCASE) && topState.is(topBlock)) {
            level.setBlock(basePos, baseState.setValue(HAS_SHOWCASE, true), 3);
            return;
        }

        if (!baseState.getValue(HAS_SHOWCASE)) return;
        if (topState.is(topBlock)) return;

        if (topState.isAir() || topState.canBeReplaced()) {
            level.setBlock(topPos, topBlock.defaultBlockState(), 3);
        }
    }

    // -------------------------------------------------------------------------

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}