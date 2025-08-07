package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class SmallShopBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<SmallShopBlock> CODEC = simpleCodec(SmallShopBlock::new);

    public SmallShopBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends SmallShopBlock> codec() {
        return CODEC;
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
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop) {
                boolean ownerView = player.getUUID().equals(shop.getOwner());

                // Erstelle einen Container mit den aktuellen Inhalten des Blocks
                SimpleContainer container = new SimpleContainer(11);
                for (int i = 0; i < shop.getInventory().getSlots(); i++) {
                    container.setItem(i, shop.getInventory().getStackInSlot(i).copy());
                }
                container.setItem(9, shop.getSaleItem().copy());
                container.setItem(10, shop.getPayItem().copy());

                MenuProvider provider = new SimpleMenuProvider((id, inv, ply) ->
                        new SmallShopMenu(id, inv, container, shop, ownerView),
                        Component.translatable("container.small_shop"));
                player.openMenu(provider);

                ItemStack sale = shop.getSaleItem();
                if (!sale.isEmpty()) {
                    // Entferne vorhandene ItemEntities an dieser Position
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(pos)).forEach(Entity::discard);

                    shop.discardDisplayItem();
                    ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, sale.copy());
                    item.setNoGravity(true);
                    item.setNeverPickUp();
                    item.setUnlimitedLifetime();
                    level.addFreshEntity(item);
                    shop.setDisplayItem(item);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallShopBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop) {
                shop.setOwner(player.getUUID());
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop) {
                shop.discardDisplayItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}