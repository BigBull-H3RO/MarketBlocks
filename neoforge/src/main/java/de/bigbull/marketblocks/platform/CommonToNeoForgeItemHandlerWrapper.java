package de.bigbull.marketblocks.platform;

import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public class CommonToNeoForgeItemHandlerWrapper implements ResourceHandler<ItemResource> {
    private final ICommonItemHandler common;
    private final List<SlotWrapper> slotWrappers;

    public CommonToNeoForgeItemHandlerWrapper(ICommonItemHandler common) {
        this.common = common;
        int slots = common.getSlots();
        this.slotWrappers = new ArrayList<>(slots);
        for (int i = 0; i < slots; i++) {
            this.slotWrappers.add(new SlotWrapper(i));
        }
    }

    private class SlotWrapper extends ItemStackResourceHandler {
        private final int slot;

        SlotWrapper(int slot) {
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return common.getStackInSlot(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            common.setStackInSlot(slot, stack);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return common.isItemValid(slot, resource.toStack());
        }

        @Override
        protected int getCapacity(ItemResource resource) {
            return common.getSlotLimit(slot);
        }
    }

    @Override
    public int size() {
        return common.getSlots();
    }

    @Override
    public ItemResource getResource(int slot) {
        if (slot < 0 || slot >= slotWrappers.size()) return ItemResource.EMPTY;
        return slotWrappers.get(slot).getResource(0);
    }

    @Override
    public long getAmountAsLong(int slot) {
        if (slot < 0 || slot >= slotWrappers.size()) return 0;
        return slotWrappers.get(slot).getAmountAsLong(0);
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        if (slot < 0 || slot >= slotWrappers.size()) return 0;
        return slotWrappers.get(slot).getCapacityAsLong(0, resource);
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        if (slot < 0 || slot >= slotWrappers.size()) return false;
        return slotWrappers.get(slot).isValid(0, resource);
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transactionContext) {
        if (slot < 0 || slot >= slotWrappers.size()) return 0;
        return slotWrappers.get(slot).insert(0, resource, amount, transactionContext);
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transactionContext) {
        if (slot < 0 || slot >= slotWrappers.size()) return 0;
        return slotWrappers.get(slot).extract(0, resource, amount, transactionContext);
    }
}
