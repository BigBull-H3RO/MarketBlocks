package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.GenericMenuProvider;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SmallShopBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<SmallShopBlock> CODEC = simpleCodec(SmallShopBlock::new);

    public SmallShopBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SmallShopBlockEntity shopEntity)) {
            return InteractionResult.FAIL;
        }

        if (shopEntity.getOwnerId() == null) {
            shopEntity.setOwner(player);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (shopEntity.hasOffer() || shopEntity.isOwner(player)) {
                // Besitzer sehen standardmäßig ebenfalls das Offers-Menü
                serverPlayer.openMenu(
                        new GenericMenuProvider(
                                Component.translatable("container.marketblocks.small_shop_offers"),
                                (id, inv, p) -> new SmallShopOffersMenu(id, inv, shopEntity)
                        ),
                        pos
                );
            } else {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SmallShopBlockEntity shopEntity) {
            // Standardmäßig Offers-Menu zurückgeben
            return new GenericMenuProvider(
                    Component.translatable("container.marketblocks.small_shop_offers"),
                    (id, inv, player) -> new SmallShopOffersMenu(id, inv, shopEntity)
            );
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SmallShopBlockEntity shopEntity) {
                // Droppe alle Items beim Abbauen
                shopEntity.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected MapCodec<? extends SmallShopBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallShopBlockEntity(pos, state);
    }
}