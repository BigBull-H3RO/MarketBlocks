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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Unsichtbarer Hilfsblock der die obere Hälfte der Glasvitrine (Y=16..25) abdeckt.
 *
 * Dieser Block:
 * - Ist nicht direkt platzierbar (kein BlockItem, kein Crafting-Rezept)
 * - Wird automatisch von SmallShopBlockNeu gesetzt wenn HAS_SHOWCASE=true
 * - Ist unsichtbar (RenderShape.INVISIBLE) – das Modell wird vom Base-Block gerendert
 * - Leitet alle Interaktionen an den darunterliegenden SmallShopBlockNeu weiter
 * - Entfernt sich selbst wenn der Base-Block nicht mehr vorhanden ist (canSurvive)
 *
 * Shapes:
 * - getShape() → für Raycast (Anvisieren) und Outline – volle Breite (0..16), Y=0..9
 * - getCollisionShape() → Kollision – identisch mit getShape()
 *
 * NOTE: Die Outline-Darstellung wird von BlockOutlineHandler übernommen, der eine
 * durchgehende Box (Y=0..25) vom Base-Block aus zeichnet und so die Nahtlinie
 * bei der Blockgrenze Y=16 eliminiert.
 */
public class SmallShopBlockNeuTop extends Block {
    public static final MapCodec<SmallShopBlockNeuTop> CODEC = simpleCodec(SmallShopBlockNeuTop::new);

    // Volle Breite (0..16), Y=0..9 entspricht Welt-Y+16 bis Welt-Y+25.
    // Keine negativen Y-Werte – Minecraft-VoxelShapes sind zuverlässig nur in [0,16].
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 9, 16);

    public SmallShopBlockNeuTop(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    // -------------------------------------------------------------------------
    // Shapes
    // -------------------------------------------------------------------------

    /**
     * Muss eine echte Shape zurückgeben – Minecraft nutzt getShape() für den Raycast
     * (Anvisieren). Shapes.empty() würde die Interaktion im Y=16..25-Bereich deaktivieren.
     * Die Outline-Darstellung übernimmt BlockOutlineHandler separat.
     */
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

    // -------------------------------------------------------------------------
    // Überlebensbedingung
    // -------------------------------------------------------------------------

    /**
     * Dieser Block kann nur überleben wenn direkt darunter ein SmallShopBlockNeu liegt.
     * Zusammen mit neighborChanged entfernt sich der Top-Block automatisch wenn der
     * Base-Block entfernt wird – ohne dass SmallShopBlockNeu neighborChanged überschreiben muss.
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.canSurvive(level, pos)) {
            level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), 3);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && !state.canSurvive(level, pos)) {
            level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), 3);
        }
    }

    // -------------------------------------------------------------------------
    // Interaktion → Weiterleitung an Base-Block
    // -------------------------------------------------------------------------

    /** Klick auf den Top-Block → Interaktion wird an den Base-Block weitergeleitet. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())) {
            return InteractionResult.PASS;
        }
        BlockHitResult redirectedHit = new BlockHitResult(
                hitResult.getLocation(), hitResult.getDirection(), basePos, hitResult.isInside());
        return baseState.useWithoutItem(level, player, redirectedHit);
    }

    /** Abbau des Top-Blocks → Base-Block wird abgebaut (mit Owner-Check im Base-Block). */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockPos basePos = pos.below();
        BlockState baseState = level.getBlockState(basePos);
        if (baseState.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())) {
            return baseState.onDestroyedByPlayer(level, basePos, player, willHarvest, level.getFluidState(basePos));
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    // onRemove: KEIN Override nötig.
    // Der Base-Block (SmallShopBlockNeu.onRemove) entfernt den Top-Block wenn er selbst
    // abgebaut wird. Und falls der Top-Block direkt entfernt wird (z.B. /setblock),
    // reicht super.onRemove() – kein zusätzliches Verhalten nötig.

    // -------------------------------------------------------------------------

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}