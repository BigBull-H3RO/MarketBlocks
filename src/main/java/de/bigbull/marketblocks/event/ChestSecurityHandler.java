package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.LockedChestWrapper;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
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
                        IItemHandler handler = new InvWrapper(chest);
                        return new LockedChestWrapper(handler, shop.getOwnerId());
                    }
                    return null;
                }
        );
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

    private static SmallShopBlockEntity findShop(Level level, BlockPos pos) {
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