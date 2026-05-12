package de.bigbull.marketblocks.core.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.SideMode;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public class ChestSecurityHandler {
    @SubscribeEvent
    public static void onChestPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.isClientSide()) return;
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) return;

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

        if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            Direction direction = ChestBlock.getConnectedDirection(state);
            BlockPos otherPos = event.getPos().relative(direction);
            if (isAdjacentToShop(level, event.getPos()) || isAdjacentToShop(level, otherPos)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity) {
            SingleOfferShopBlockEntity shop = findShop(level, pos);
            if (shop != null && (event.getEntity() == null || !shop.isOwner(event.getEntity()))) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isAdjacentToShop(Level level, BlockPos pos) {
        return findShopSingle(level, pos) != null;
    }

    private static SingleOfferShopBlockEntity findShop(Level level, BlockPos pos) {
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
            return null;
        }
        SingleOfferShopBlockEntity shop = findShopSingle(level, pos);
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

    private static SingleOfferShopBlockEntity findShopSingle(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof SingleOfferShopBlockEntity shop) {
                SideMode mode = shop.getModeForSide(dir.getOpposite());
                if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                    return shop;
                }
            }
        }
        return null;
    }
}
