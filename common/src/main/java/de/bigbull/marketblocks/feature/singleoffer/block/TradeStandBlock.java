package de.bigbull.marketblocks.feature.singleoffer.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.SoundType;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
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

/**
 * Represents the trade stand shop block variant.
 * Supports a togglable "showcase" mode which places an invisible secondary
 * block on top for interaction logic.
 */
public class TradeStandBlock extends BaseShopBlock {
    public static final MapCodec<TradeStandBlock> CODEC = simpleCodec(TradeStandBlock::new);
    public static final BooleanProperty HAS_SHOWCASE = BooleanProperty.create("has_showcase");

    private static final VoxelShape SHAPE_NO_SHOWCASE = Block.box(0, 0, 0, 16, 11, 16);

    private static final VoxelShape SHAPE_WITH_SHOWCASE = Shapes.or(
            SHAPE_NO_SHOWCASE,
            Block.box(1, 11, 1, 15, 16, 15));

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

    /**
     * Checks whether the given BlockState is this shop and has a showcase.
     */
    public static boolean hasShowcase(BlockState state) {
        return state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) && state.getValue(HAS_SHOWCASE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_SHOWCASE) ? SHAPE_WITH_SHOWCASE : SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return state.getValue(HAS_SHOWCASE) ? SHAPE_WITH_SHOWCASE : SHAPE_NO_SHOWCASE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(HAS_SHOWCASE) ? SHAPE_WITH_SHOWCASE : SHAPE_NO_SHOWCASE;
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

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return createCloneStack(state);
    }

    public static ItemStack createCloneStack(BlockState state) {
        ItemStack stack = new ItemStack(RegistriesInit.TRADE_STAND_BLOCK.get());
        if (state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) && state.getValue(HAS_SHOWCASE)) {
            stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HAS_SHOWCASE, true));
        }
        return stack;
    }

    public static boolean canPlayerModifyShowcase(Level level, BlockPos pos, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SingleOfferShopBlockEntity shop && shop.getOwnerId() != null) {
            return shop.isOwner(player);
        }
        return true;
    }

    /**
     * Checks whether the hit result on this base block specifically struck the showcase
     * (box from 1/16 to 15/16 in X/Z and 11/16 to 16/16 in Y), rather than the 1-pixel
     * counter margin or front/side table surface.
     */
    public static boolean isHitOnShowcase(BlockPos pos, BlockHitResult hitResult) {
        // If the hit face is UP, it's hitting the counter tabletop (Y=11/16) that surrounds the showcase
        if (hitResult.getDirection() == Direction.UP) {
            return false;
        }

        double hitX = hitResult.getLocation().x - pos.getX();
        double hitY = hitResult.getLocation().y - pos.getY();
        double hitZ = hitResult.getLocation().z - pos.getZ();

        double minX = 1.0 / 16.0 - 0.001;
        double maxX = 15.0 / 16.0 + 0.001;
        double minY = 11.0 / 16.0 + 0.001;
        double maxY = 1.0 + 0.001;
        double minZ = 1.0 / 16.0 - 0.001;
        double maxZ = 15.0 / 16.0 + 0.001;

        return hitY >= minY && hitY <= maxY
                && hitX >= minX && hitX <= maxX
                && hitZ >= minZ && hitZ <= maxZ;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        boolean hasShowcase = state.getValue(HAS_SHOWCASE);

        // 1. Holding Glass and clicking top face of counter without showcase places the showcase (no shift needed)
        if (stack.is(Items.GLASS) && hitResult.getDirection() == Direction.UP && !hasShowcase) {
            if (canPlayerModifyShowcase(level, pos, player)) {
                InteractionResult res = tryEnableShowcase(level, pos, state, player, stack);
                if (res.consumesAction()) {
                    return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
                }
            }
            // If the player is not an owner/co-owner, do NOT return FAIL.
            // Delegate to super.useItemOn so it opens the shop GUI normally without placing a glass block in the world.
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        // 2. Clicking the showcase with an axe knocks it down (no shift needed).
        // The showcase extends from Y = 11/16 to 16/16 and X/Z = 1/16 to 15/16 in this base block.
        if (stack.getItem() instanceof net.minecraft.world.item.AxeItem && hasShowcase
                && isHitOnShowcase(pos, hitResult)) {
            if (canPlayerModifyShowcase(level, pos, player)) {
                InteractionResult res = tryDisableShowcase(level, pos, state, player);
                if (res.consumesAction()) {
                    return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
                }
            }
            // If the player is not authorized to knock down the showcase, let them open the shop GUI normally with their axe
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        // 3. Side/front clicks, clicks on the counter (including surrounding 1-pixel margin), or other items open the shop menu normally
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    public static InteractionResult tryEnableShowcase(Level level, BlockPos pos, BlockState state, Player player,
            ItemStack stack) {
        if (!state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || state.getValue(HAS_SHOWCASE)) {
            return InteractionResult.PASS;
        }

        BlockPos topPos = pos.above();
        BlockState topState = level.getBlockState(topPos);
        Block topBlock = RegistriesInit.TRADE_STAND_BLOCK_TOP.get();
        boolean topAlreadyPresent = topState.is(topBlock);

        if (!topAlreadyPresent && !topState.canBeReplaced()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SingleOfferShopBlockEntity shop && shop.getOwnerId() != null && !shop.isOwner(player)) {
                return InteractionResult.FAIL;
            }

            level.setBlock(pos, state.setValue(HAS_SHOWCASE, true), 3);
            ensureTopBlock(level, pos);

            if (!topAlreadyPresent && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundType.GLASS.getPlaceSound(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public static InteractionResult tryDisableShowcase(Level level, BlockPos pos, BlockState state, Player player) {
        if (!state.is(RegistriesInit.TRADE_STAND_BLOCK.get()) || !state.getValue(HAS_SHOWCASE)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SingleOfferShopBlockEntity shop && shop.getOwnerId() != null && !shop.isOwner(player)) {
                return InteractionResult.FAIL;
            }

            level.setBlock(pos, state.setValue(HAS_SHOWCASE, false), 3);

            BlockPos topPos = pos.above();
            if (level.getBlockState(topPos).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            }

            ItemEntity glassItem = getItemEntity(level, pos);
            level.addFreshEntity(glassItem);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    private static @NotNull ItemEntity getItemEntity(Level level, BlockPos pos) {
        double spawnX = pos.getX() + 0.5D;
        double spawnY = pos.getY() + (11.0D / 16.0D) + 0.1D;
        double spawnZ = pos.getZ() + 0.5D;

        ItemEntity glassItem = new ItemEntity(
                level, spawnX, spawnY, spawnZ, new ItemStack(Items.GLASS));
        glassItem.setDefaultPickUpDelay();
        return glassItem;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock()) && state.getValue(HAS_SHOWCASE)) {
            ensureTopBlock(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockPos topPos = pos.above();
        if (level.getBlockState(topPos).is(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())) {
            level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
            level.destroyBlockProgress(0, topPos, -1);
            level.destroyBlockProgress(500000, topPos, -1);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    public static void ensureTopBlock(Level level, BlockPos basePos) {
        if (level.isClientSide())
            return;

        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(RegistriesInit.TRADE_STAND_BLOCK.get()))
            return;

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
                level.destroyBlockProgress(0, topPos, -1);
                level.destroyBlockProgress(500000, topPos, -1);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!canPlayerDestroy(level, pos, player, false)) {
            return state;
        }
        if (!level.isClientSide() && state.getValue(HAS_SHOWCASE)) {
            var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var silkTouchHolder = registry.get(Enchantments.SILK_TOUCH);

            boolean hasSilkTouch = silkTouchHolder.isPresent()
                    && net.minecraft.world.item.enchantment.EnchantmentHelper
                            .getEnchantmentLevel(silkTouchHolder.get(), player) > 0;

            if (!hasSilkTouch && !player.isCreative()) {
                level.playSound(null, pos, SoundEvents.GLASS_BREAK,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseShopBlock> codec() {
        return CODEC;
    }
}
