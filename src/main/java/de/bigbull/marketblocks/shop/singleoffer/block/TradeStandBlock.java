package de.bigbull.marketblocks.shop.singleoffer.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.block.ShopBlockConfig;
import de.bigbull.marketblocks.util.block.ShopRenderConfig;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeStandBlock extends BaseShopBlock {
    public static final MapCodec<TradeStandBlock> CODEC = simpleCodec(TradeStandBlock::new);
    public static final BooleanProperty HAS_SHOWCASE = BooleanProperty.create("has_showcase");

    // Feste Shape ohne Vitrine (Y geht nur bis 11)
    private static final VoxelShape SHAPE_NO_SHOWCASE = Block.box(0, 0, 0, 16, 11, 16);

    // Zusammengesetzte Shape: Basis-Block + schmälere Vitrine oben drauf
    private static final VoxelShape SHAPE_WITH_SHOWCASE = Shapes.or(
            SHAPE_NO_SHOWCASE,                                          // Der untere Basis-Teil (Y=0 bis 11)
            Block.box(1, 11, 1, 15, 16, 15)     // Die Vitrine (1 Pixel nach innen versetzt, Y=11 bis 16)
    );

    public TradeStandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HAS_SHOWCASE, false));
    }

    @Override
    public ShopBlockConfig getShopConfig() {
        return ShopBlockConfig.TRADE_STAND_SHAPE;
    }

    @Override
    public ShopRenderConfig getRenderConfig(BlockState state) {
        return ShopRenderConfig.TRADE_STAND;
    }

    /** * Hilfsmethode: Prüft, ob der übergebene BlockState dieser Shop ist und eine Vitrine hat.
     */
    public static boolean hasShowcase(BlockState state) {
        return state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) && state.getValue(HAS_SHOWCASE);
    }

    // --- HIER IST DER FIX ---
    // Wir fragen ab, ob die Vitrine aktiv ist. Wenn ja, geben wir die volle 16er Höhe zurück!
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_SHOWCASE) ? SHAPE_WITH_SHOWCASE : SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_SHOWCASE) ? SHAPE_WITH_SHOWCASE : SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(HAS_SHOWCASE) ? SHAPE_WITH_SHOWCASE : SHAPE_NO_SHOWCASE;
    }
    // ------------------------

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

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return createCloneStack(state);
    }

    public static ItemStack createCloneStack(BlockState state) {
        ItemStack stack = new ItemStack(RegistriesInit.TRADE_STAND_BLOCK.get());
        if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) && state.getValue(HAS_SHOWCASE)) {
            stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HAS_SHOWCASE, true));
        }
        return stack;
    }

    public static InteractionResult tryEnableShowcase(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (!state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || state.getValue(HAS_SHOWCASE)) {
            return InteractionResult.PASS;
        }

        BlockPos topPos = pos.above();
        BlockState topState = level.getBlockState(topPos);
        Block topBlock = RegistriesInit.TRADE_STAND_BLOCK_TOP.get();
        boolean topAlreadyPresent = topState.is(topBlock);

        if (!topAlreadyPresent && !topState.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SingleOfferShopBlockEntity shop && !shop.isOwner(player)) {
                return InteractionResult.FAIL;
            }

            level.setBlock(pos, state.setValue(HAS_SHOWCASE, true), 3);
            ensureTopBlock(level, pos);

            if (!topAlreadyPresent && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static InteractionResult tryDisableShowcase(Level level, BlockPos pos, BlockState state, Player player) {
        if (!state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || !state.getValue(HAS_SHOWCASE)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SingleOfferShopBlockEntity shop && !shop.isOwner(player)) {
                return InteractionResult.FAIL;
            }

            level.setBlock(pos, state.setValue(HAS_SHOWCASE, false), 3);

            BlockPos topPos = pos.above();
            if (level.getBlockState(topPos).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }

            ItemEntity glassItem = getItemEntity(level, pos);
            level.addFreshEntity(glassItem);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static @NotNull ItemEntity getItemEntity(Level level, BlockPos pos) {
        double spawnX = pos.getX() + 0.5D;
        // Y auf exakt 11 Pixel Höhe (11/16) + 0.1 Puffer, damit es schön aufploppt
        double spawnY = pos.getY() + (11.0D / 16.0D) + 0.1D;
        double spawnZ = pos.getZ() + 0.5D;

        // Das Item manuell in die Welt setzen
        ItemEntity glassItem = new ItemEntity(
                level, spawnX, spawnY, spawnZ, new ItemStack(Items.GLASS)
        );
        glassItem.setDefaultPickUpDelay(); // Wichtig: Damit der Spieler es nicht in der exakt selben Millisekunde einsaugt
        return glassItem;
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
            if (level.getBlockState(topPos).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void ensureTopBlock(Level level, BlockPos basePos) {
        if (level.isClientSide) return;

        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(RegistriesInit.TRADE_STAND_BLOCK.get())) return;

        BlockPos topPos = basePos.above();
        BlockState topState = level.getBlockState(topPos);
        Block topBlock = RegistriesInit.TRADE_STAND_BLOCK_TOP.get();

        if (!baseState.getValue(HAS_SHOWCASE) && topState.is(topBlock)) {
            level.setBlock(basePos, baseState.setValue(HAS_SHOWCASE, true), 3);
            return;
        }

        if (baseState.getValue(HAS_SHOWCASE) && !topState.is(topBlock)) {
            if (topState.canBeReplaced()) {
                level.setBlock(topPos, topBlock.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.getValue(HAS_SHOWCASE)) {
            // Check auf Behutsamkeit (Silk Touch) in 1.21.1
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            var silkTouch = registry.getHolder(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH);

            boolean hasSilkTouch = silkTouch.isPresent() &&
                    player.getMainHandItem().getEnchantments().getLevel(silkTouch.get()) > 0;

            if (!hasSilkTouch && !player.isCreative()) {
                // Nur wenn KEINE Behutsamkeit und NICHT im Creative Mode -> Glas Sound abspielen
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}
