package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import de.bigbull.marketblocks.platform.services.IItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class NeoForgeItemTransferHelper implements IItemTransferHelper {

    @Nullable
    @Override
    public ICommonItemHandler getNeighborItemHandler(Level level, BlockPos pos, Direction side) {
        if (level == null || !level.isLoaded(pos)) {
            return null;
        }

        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (handler == null) {
            handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        }

        if (handler == null) {
            Container container = null;
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock chestBlock) {
                container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof Container c) {
                    container = c;
                }
            }
            if (container != null) {
                handler = VanillaContainerWrapper.of(container);
            }
        }

        if (handler == null) return null;

        final ResourceHandler<ItemResource> target = handler;
        return new ICommonItemHandler() {
            @Override
            public int getSlots() {
                return target.size();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemUtil.getStack(target, slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return ItemUtil.insertItemReturnRemaining(target, slot, stack, simulate, null);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount <= 0 || slot >= target.size()) return ItemStack.EMPTY;
                try (Transaction tx = Transaction.open(null)) {
                    ItemResource res = target.getResource(slot);
                    if (res.isEmpty()) return ItemStack.EMPTY;
                    int extracted = target.extract(slot, res, amount, tx);
                    if (!simulate) {
                        tx.commit();
                    }
                    return res.toStack(extracted);
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                if (slot < 0 || slot >= target.size()) return 64;
                ItemResource res = target.getResource(slot);
                return (int) target.getCapacityAsLong(slot, res);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (stack.isEmpty()) return false;
                return target.isValid(slot, ItemResource.of(stack));
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
            }
        };
    }
}