package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.entity.LockedChestWrapper;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public class ChestSecurityHandler {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityType.CHEST,
                (chest, side) -> {
                    SmallShopBlockEntity shop = findShop(chest.getLevel(), chest.getBlockPos());
                    if (shop != null) {
                        IItemHandler handler;
                        BlockState state = chest.getBlockState();
                        if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get() &&
                                state.getBlock() instanceof ChestBlock &&
                                state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                            Direction dir = ChestBlock.getConnectedDirection(state);
                            BlockPos otherPos = chest.getBlockPos().relative(dir);
                            BlockEntity otherBe = chest.getLevel().getBlockEntity(otherPos);
                            if (otherBe instanceof ChestBlockEntity otherChest) {
                                handler = new CombinedInvWrapper(new InvWrapper(chest), new InvWrapper(otherChest));
                            } else {
                                handler = new InvWrapper(chest);
                            }
                        } else {
                            handler = new InvWrapper(chest);
                        }
                        return new LockedChestWrapper(handler, shop.getOwnerId());
                    }
                    return null;
                }
        );
    }

    @SubscribeEvent
    public static void onChestPlaced(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockState state = event.getPlacedBlock();
        if (!(state.getBlock() instanceof ChestBlock)) return;

        if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get()) {
            if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                Direction dir = ChestBlock.getConnectedDirection(state);
                BlockPos otherPos = event.getPos().relative(dir);
                if (isAdjacentToShop(level, event.getPos()) || isAdjacentToShop(level, otherPos)) {
                    level.invalidateCapabilities(event.getPos());
                    level.invalidateCapabilities(otherPos);
                }
            }
            return;
        }

        if (state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) return;

        Direction direction = ChestBlock.getConnectedDirection(state);
        BlockPos otherPos = event.getPos().relative(direction);
        BlockState otherState = level.getBlockState(otherPos);

        if (isAdjacentToShop(level, event.getPos()) || isAdjacentToShop(level, otherPos)) {
            level.setBlock(event.getPos(), state.setValue(ChestBlock.TYPE, ChestType.SINGLE), 3);
            level.setBlock(otherPos, otherState.setValue(ChestBlock.TYPE, ChestType.SINGLE), 3);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            SmallShopBlockEntity shop = findShop(level, pos);
            if (shop != null && (event.getEntity() == null || !shop.isOwner(event.getEntity()))) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isAdjacentToShop(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (level.getBlockState(neighbor).getBlock() instanceof SmallShopBlock) {
                return true;
            }
        }
        return false;
    }

    private static SmallShopBlockEntity findShop(Level level, BlockPos pos) {
        SmallShopBlockEntity shop = findShopSingle(level, pos);
        if (shop != null) {
            return shop;
        }

        if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get()) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                Direction direction = ChestBlock.getConnectedDirection(state);
                BlockPos otherPos = pos.relative(direction);
                return findShopSingle(level, otherPos);
            }
        }

        return null;
    }

    private static SmallShopBlockEntity findShopSingle(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof SmallShopBlockEntity shop) {
                SideMode mode = shop.getModeForSide(dir.getOpposite());
                if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                    return shop;
                }
            }
        }
        return null;
    }
}