package de.bigbull.marketblocks.util.custom.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(9);
    private ItemStack saleItem = ItemStack.EMPTY;
    private ItemStack payItem = ItemStack.EMPTY;
    private UUID owner;

    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getSaleItem() {
        return saleItem;
    }

    public void setSaleItem(ItemStack stack) {
        this.saleItem = stack;
    }

    public ItemStack getPayItem() {
        return payItem;
    }

    public void setPayItem(ItemStack stack) {
        this.payItem = stack;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Inventory", inventory.serializeNBT(provider));
        if (!saleItem.isEmpty()) {
            tag.put("SaleItem", saleItem.save(provider));
        }
        if (!payItem.isEmpty()) {
            tag.put("PayItem", payItem.save(provider));
        }
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        saleItem = ItemStack.parseOptional(registries, tag.getCompound("SaleItem"));
        payItem = ItemStack.parseOptional(registries, tag.getCompound("PayItem"));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}