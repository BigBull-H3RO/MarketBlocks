package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.block.entity.LockedChestWrapper;
import de.bigbull.marketblocks.block.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = MarketBlocks.MODID)
public final class ChestSecurityCapabilities {
    private ChestSecurityCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BlockEntityType.CHEST,
                (chest, side) -> {
                    if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
                        return null;
                    }
                    SmallShopBlockEntity shop = findShop(chest.getLevel(), chest.getBlockPos());
                    if (shop == null) {
                        return null;
                    }

                    IItemHandler handler;
                    BlockState state = chest.getBlockState();
                    if (Config.ENABLE_DOUBLE_CHEST_SUPPORT.get()
                            && state.getBlock() instanceof ChestBlock
                            && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
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
        );
    }

    private static SmallShopBlockEntity findShop(Level level, BlockPos pos) {
        if (level == null) {
            return null;
        }
        if (!Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get()) {
            return null;
        }
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
