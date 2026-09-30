package de.bigbull.marketblocks.platform.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class CommonItemStackHandler implements ICommonItemHandler {
    protected final NonNullList<ItemStack> stacks;

    public CommonItemStackHandler(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;

        validateSlotIndex(slot);
        ItemStack existing = stacks.get(slot);
        int limit = getStackLimit(slot, stack);

        if (existing.isEmpty()) {
            if (!simulate) {
                stacks.set(slot, stack.copyWithCount(Math.min(stack.getCount(), limit)));
                onContentsChanged(slot);
            }
            return stack.getCount() > limit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
        }

        if (!ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack;
        }

        int available = limit - existing.getCount();
        if (available <= 0) return stack;

        int toInsert = Math.min(stack.getCount(), available);
        if (!simulate) {
            existing.grow(toInsert);
            onContentsChanged(slot);
        }

        return stack.getCount() > toInsert ? stack.copyWithCount(stack.getCount() - toInsert) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);

        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copyWithCount(toExtract);

        if (!simulate) {
            if (existing.getCount() <= toExtract) {
                stacks.set(slot, ItemStack.EMPTY);
            } else {
                existing.shrink(toExtract);
            }
            onContentsChanged(slot);
        }

        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    protected void onContentsChanged(int slot) {
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                Tag itemTag = ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
                if (itemTag instanceof CompoundTag compoundTag) {
                    compoundTag.putInt("Slot", i);
                    nbtTagList.add(compoundTag);
                }
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        setSize(nbt.getIntOr("Size", stacks.size()));
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        ListTag tagList = nbt.getListOrEmpty("Items");
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompoundOrEmpty(i);
            int slot = itemTags.getIntOr("Slot", -1);
            if (slot >= 0 && slot < stacks.size()) {
                ItemStack stack = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), itemTags).result().orElse(ItemStack.EMPTY);
                stacks.set(slot, stack);
            }
        }
        onLoad();
    }

    public void setSize(int size) {
        while (stacks.size() < size) stacks.add(ItemStack.EMPTY);
        while (stacks.size() > size) stacks.remove(stacks.size() - 1);
    }

    protected void onLoad() {
    }
}