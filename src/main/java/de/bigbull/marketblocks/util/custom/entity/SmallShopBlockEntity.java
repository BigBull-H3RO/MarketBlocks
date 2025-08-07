package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(9);
    private ItemStack saleItem = ItemStack.EMPTY;
    private ItemStack payItem = ItemStack.EMPTY;
    private ItemEntity displayItem;
    private UUID owner;

    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getSaleItem() {
        return saleItem;
    }

    public void setSaleItem(ItemStack stack) {
        discardDisplayItem();
        this.saleItem = stack;
        sync();
    }

    public ItemStack getPayItem() {
        return payItem;
    }

    public void setPayItem(ItemStack stack) {
        this.payItem = stack;
        sync();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setDisplayItem(ItemEntity item) {
        this.displayItem = item;
    }

    public ItemEntity getDisplayItem() {
        return displayItem;
    }

    public void discardDisplayItem() {
        if (displayItem != null) {
            displayItem.discard();
            displayItem = null;
        }
    }

    /**
     * Prüft, ob genügend Lagerbestand und passende Bezahlung vorhanden sind.
     */
    public boolean canTrade(Player player) {
        if (saleItem.isEmpty() || payItem.isEmpty()) {
            return false;
        }
        if (!hasStock()) {
            return false;
        }

        // Prüfe, ob der Spieler die geforderte Bezahlung inklusive NBT besitzt
        int remaining = payItem.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, payItem)) {
                remaining -= stack.getCount();
            }
        }
        if (remaining > 0) {
            return false;
        }

        // Simuliere das Einfügen der Bezahlung ins Lager
        ItemStack simulate = payItem.copy();
        for (int i = 0; i < inventory.getSlots() && !simulate.isEmpty(); i++) {
            simulate = inventory.insertItem(i, simulate, true);
        }
        return simulate.isEmpty();
    }

    /**
     * Führt den Kauf aus und regelt Übergabe und Abzug der Items.
     */
    public void performTrade(Player player) {
        if (!canTrade(player)) {
            return;
        }

        // Simuliere erneut das Einfügen, um Race-Conditions zu vermeiden
        ItemStack simulate = payItem.copy();
        for (int i = 0; i < inventory.getSlots() && !simulate.isEmpty(); i++) {
            simulate = inventory.insertItem(i, simulate, true);
        }
        if (!simulate.isEmpty()) {
            return; // Lager kann die Bezahlung nicht aufnehmen
        }

        // Entferne Bezahlung aus dem Spielerinventar, dabei NBT berücksichtigen
        ItemStack payment = payItem.copy();
        ItemStack toRemove = payItem.copy();
        for (int i = 0; i < player.getInventory().getContainerSize() && !toRemove.isEmpty(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int remove = Math.min(stack.getCount(), toRemove.getCount());
                stack.shrink(remove);
                player.getInventory().setItem(i, stack);
                toRemove.shrink(remove);
            }
        }
        if (!toRemove.isEmpty()) {
            return; // Spieler hatte nicht genug passende Items
        }

        // Füge die Bezahlung dem Lager hinzu
        for (int i = 0; i < inventory.getSlots() && !payment.isEmpty(); i++) {
            payment = inventory.insertItem(i, payment, false);
        }
        if (!payment.isEmpty()) {
            // Einfügen fehlgeschlagen, gib dem Spieler die Items zurück
            player.addItem(payment);
            return;
        }

        // Entferne Verkaufsware aus dem Lager
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                stack.shrink(saleItem.getCount());
                inventory.setStackInSlot(i, stack);
                break;
            }
        }

        // Übergib Verkaufsware an Spieler
        ItemStack result = saleItem.copy();
        if (!player.addItem(result)) {
            player.drop(result, false);
        }
        setChanged();
    }

    /**
     * Prüft, ob Lagerbestand für das Angebot vorhanden ist.
     */
    public boolean hasStock() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem) && stack.getCount() >= saleItem.getCount()) {
                return true;
            }
        }
        return false;
    }

    private void sync() {
        setChanged();
        Level level = getLevel();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
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

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }
}