package de.bigbull.marketblocks.util.custom.block;

import com.mojang.serialization.MapCodec;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SmallShopBlockEntity shop)) {
            return InteractionResult.FAIL;
        }

        // Bestimme Ansichtstyp
        boolean ownerView = player.getUUID().equals(shop.getOwner());

        // Erstelle erweiterten Container (28 Slots statt 27)
        SimpleContainer container = new SimpleContainer(28);

        // Synchronisiere Container mit Block-Entity
        syncContainerWithBlockEntity(container, shop);

        MenuProvider provider = new SimpleMenuProvider((id, inv, ply) ->
                new SmallShopMenu(id, inv, container, shop, ownerView),
                Component.translatable("container.small_shop"));

        player.openMenu(provider);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Synchronisiert Container mit Block-Entity Daten
     */
    private void syncContainerWithBlockEntity(SimpleContainer container, SmallShopBlockEntity shop) {
        // Kopiere Zahlungs-Lager (Slots 0-11)
        for (int i = 0; i < shop.getPayments().getSlots(); i++) {
            container.setItem(i, shop.getPayments().getStackInSlot(i).copy());
        }

        // Kopiere Verkaufs-Lager (Slots 12-23)
        for (int i = 0; i < shop.getStock().getSlots(); i++) {
            container.setItem(12 + i, shop.getStock().getStackInSlot(i).copy());
        }

        // Angebots-Templates (Slots 24-26)
        container.setItem(24, shop.getSaleItem().copy());
        container.setItem(25, shop.getPayItemA().copy());
        container.setItem(26, shop.getPayItemB().copy());

        // Kaufslot (Slot 27) bleibt leer - wird automatisch befüllt
        container.setItem(27, ItemStack.EMPTY);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmallShopBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }

        return createTickerHelper(blockEntityType, de.bigbull.marketblocks.util.RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(),
                (lvl, pos, st, be) -> {
                    if (be instanceof SmallShopBlockEntity shop) {
                        shop.tick();
                    }
                });
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (placer instanceof Player player && !level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop) {
                shop.setOwner(player.getUUID());

                // Sende willkommens-nachricht
                player.sendSystemMessage(Component.translatable("message.marketblocks.shop.placed",
                        player.getDisplayName()));
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmallShopBlockEntity shop) {

                // Entferne Display-Entities
                shop.discardDisplayItem();
                shop.discardPayDisplayItemA();
                shop.discardPayDisplayItemB();

                // Droppe alle Items aus beiden Lagern
                dropInventoryContents(level, pos, shop);

                // Droppe Template-Items falls vorhanden
                dropTemplateItems(level, pos, shop);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void dropInventoryContents(Level level, BlockPos pos, SmallShopBlockEntity shop) {
        // Erstelle Container mit allen Items
        SimpleContainer dropContainer = new SimpleContainer(
                shop.getPayments().getSlots() + shop.getStock().getSlots());

        // Zahlungs-Lager
        for (int i = 0; i < shop.getPayments().getSlots(); i++) {
            dropContainer.setItem(i, shop.getPayments().getStackInSlot(i));
        }

        // Verkaufs-Lager
        for (int i = 0; i < shop.getStock().getSlots(); i++) {
            dropContainer.setItem(shop.getPayments().getSlots() + i,
                    shop.getStock().getStackInSlot(i));
        }

        Containers.dropContents(level, pos, dropContainer);
    }

    private void dropTemplateItems(Level level, BlockPos pos, SmallShopBlockEntity shop) {
        if (!shop.getSaleItem().isEmpty()) {
            Block.popResource(level, pos, shop.getSaleItem());
        }
        if (!shop.getPayItemA().isEmpty()) {
            Block.popResource(level, pos, shop.getPayItemA());
        }
        if (!shop.getPayItemB().isEmpty()) {
            Block.popResource(level, pos, shop.getPayItemB());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SmallShopBlockEntity shop) {
            shop.tick();
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SmallShopBlockEntity shop) {
            // Redstone-Signal basierend auf Stock-Status
            if (shop.getSaleItem().isEmpty()) {
                return 0; // Kein Angebot
            }

            int maxTrades = shop.getMaxTradesFromStock();
            if (maxTrades == 0) {
                return 1; // Angebot vorhanden, aber kein Stock
            }

            // Signal basierend auf Stock-Level (2-15)
            int stockPercentage = Math.min(maxTrades * 100 / 64, 100); // Normalisiert auf 64 Items max
            return 2 + (stockPercentage * 13 / 100);
        }
        return 0;
    }
}