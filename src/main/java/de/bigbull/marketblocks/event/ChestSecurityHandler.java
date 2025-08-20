package de.bigbull.marketblocks.event;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
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
                (chest, side) -> isLocked(chest.getLevel(), chest.getBlockPos(), null) ? new LockedItemHandler(chest) : null
        );
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            if (isLocked(level, pos, event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isLocked(Level level, BlockPos pos, Player player) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof SmallShopBlockEntity shop) {
                SideMode mode = shop.getModeForSide(dir.getOpposite());
                if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
                    if (player == null || !shop.isOwner(player)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static class LockedItemHandler implements IItemHandler {
        private final ChestBlockEntity chest;
        private final IItemHandler delegate;

        LockedItemHandler(ChestBlockEntity chest) {
            this.chest = chest;
            this.delegate = new InvWrapper(chest);
        }

        private boolean locked() {
            Level level = chest.getLevel();
            return level != null && isLocked(level, chest.getBlockPos(), null);
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
            if (locked()) return net.minecraft.world.item.ItemStack.EMPTY;
            return delegate.getStackInSlot(slot);
        }

        @Override
        public net.minecraft.world.item.ItemStack insertItem(int slot, net.minecraft.world.item.ItemStack stack, boolean simulate) {
            if (locked()) return stack;
            return delegate.insertItem(slot, stack, simulate);
        }

        @Override
        public net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (locked()) return net.minecraft.world.item.ItemStack.EMPTY;
            return delegate.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, net.minecraft.world.item.ItemStack stack) {
            if (locked()) return false;
            return delegate.isItemValid(slot, stack);
        }
    }
}