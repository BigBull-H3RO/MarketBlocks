package de.bigbull.marketblocks.util.block;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.block.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Basisklasse für alle Shop-Blöcke.
 * Beinhaltet allgemeine Logik für Besitzer, Redstone-Signale und Interaktionen.
 * Shape- und Render-Konfiguration sind getrennt, damit Anzeige-Offsets je Variante
 * unabhängig von Collision/Interaction-Shape gepflegt werden können.
 */
public abstract class BaseShopBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BaseShopBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    /**
     * Gibt die Konfiguration für dieses Shop-Design zurück.
     * Muss von Subklassen implementiert werden.
     */
    public abstract ShopBlockConfig getShopConfig();

    /**
     * Render-Konfiguration für Offer/Payment-Item und Mengen-Text.
     */
    public ShopRenderConfig getRenderConfig(BlockState state) {
        return ShopRenderConfig.SMALL_SHOP_DEFAULT;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShopConfig().getShape();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShopConfig().getShape();
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShopConfig().getShape();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, false);
    }

    // --- Redstone & Komparator Logik ---

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity shop) {
            Direction side = direction.getOpposite();
            if (hasComparatorReadPath(level, pos, side)) {
                return shop.getAnalogSignal(side);
            }
        }
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return false;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity shop) {
            int maxSignal = 0;
            boolean foundComparator = false;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (hasComparatorReadPath(level, pos, dir)) {
                    maxSignal = Math.max(maxSignal, shop.getAnalogSignal(dir));
                    foundComparator = true;
                }
            }

            // Fallback: Gebe Signal für die Rückseite, wenn kein dedizierter Komparator erkannt wird
            if (!foundComparator) {
                Direction back = state.getValue(FACING).getOpposite();
                return shop.getAnalogSignal(back);
            }
            return maxSignal;
        }
        return 0;
    }

    private static boolean hasComparatorReadPath(BlockGetter level, BlockPos sourcePos, Direction sourceToComparator) {
        if (sourceToComparator.getAxis().isVertical()) return false;

        BlockPos neighborPos = sourcePos.relative(sourceToComparator);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (isComparatorReadingFromBlock(neighborState, sourceToComparator)) return true;
        if (!neighborState.isRedstoneConductor(level, neighborPos)) return false;

        BlockPos comparatorPos = neighborPos.relative(sourceToComparator);
        return isComparatorReadingFromBlock(level.getBlockState(comparatorPos), sourceToComparator);
    }

    private static boolean isComparatorReadingFromBlock(BlockState state, Direction sourceToComparator) {
        return state.getBlock() instanceof ComparatorBlock &&
                state.getValue(ComparatorBlock.FACING) == sourceToComparator.getOpposite();
    }

    // --- Block Updates & Interaktion ---

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmallShopBlockEntity shopEntity) {
            shopEntity.updateNeighborCache();
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            level.updateNeighborsAt(pos, this); // Stellt sicher, dass das Abschalten erkannt wird
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity shopEntity) {
                shopEntity.setOwner(player);
                shopEntity.lockAdjacentChests();
            }
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof SmallShopBlockEntity shopEntity)) {
            return InteractionResult.FAIL;
        }

        // Owner-Recovery nur mit administrativen Rechten erlauben
        if (shopEntity.getOwnerId() == null) {
            if (canRepairMissingOwner(level, player)) {
                shopEntity.setOwner(player);
                MarketBlocks.LOGGER.warn("Recovered missing shop owner at {} in {} by {}",
                        pos, level.dimension().location(), player.getGameProfile().getName());
            } else {
                MarketBlocks.LOGGER.warn("Blocked missing-owner recovery attempt at {} in {} by {}",
                        pos, level.dimension().location(), player.getGameProfile().getName());
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (shopEntity.hasOffer() || shopEntity.isOwner(player)) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, p) -> new SmallShopMenu(id, inv, shopEntity),
                                Component.translatable("container.marketblocks.small_shop")
                        ), pos
                );
            } else {
                serverPlayer.displayClientMessage(Component.translatable("message.marketblocks.small_shop.no_offer"), true);
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity shop) {
            return new SimpleMenuProvider(
                    (id, inv, p) -> new SmallShopMenu(id, inv, shop),
                    Component.translatable("container.marketblocks.small_shop")
            );
        }
        return null;
    }

    private static boolean canRepairMissingOwner(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        return serverPlayer.hasPermissions(2) ||
                serverLevel.getServer().isSingleplayerOwner(serverPlayer.getGameProfile());
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmallShopBlockEntity shop) {
            if (!shop.isOwner(player)) {
                if (player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("message.marketblocks.small_shop.not_owner"), true);
                }
                level.sendBlockUpdated(pos, state, state, 3); // Resync für den Client
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SmallShopBlockEntity shopEntity) {
                shopEntity.dropContents(level, pos);
                shopEntity.unlockAdjacentChests();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallShopBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), SmallShopBlockEntity::tick);
    }
}
