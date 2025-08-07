package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity {
    // Lager verwaltet nun zwei 3x3-Blöcke
    private final ItemStackHandler inventory = new ItemStackHandler(18);
    private ItemStack saleItem = ItemStack.EMPTY;
    private ItemStack payItemA = ItemStack.EMPTY;
    private ItemStack payItemB = ItemStack.EMPTY;
    private ItemEntity displayItem;
    private ItemEntity payDisplayItem;
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

    public ItemStack getPayItemA() {
        return payItemA;
    }

    public void setPayItemA(ItemStack stack) {
        this.payItemA = stack;
        sync();
    }

    public ItemStack getPayItemB() {
        return payItemB;
    }

    public void setPayItemB(ItemStack stack) {
        this.payItemB = stack;
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

    public void setPayDisplayItem(ItemEntity item) {
        this.payDisplayItem = item;
    }

    public void discardPayDisplayItem() {
        if (payDisplayItem != null) {
            payDisplayItem.discard();
            payDisplayItem = null;
        }
    }

    /**
     * Prüft, ob genügend Lagerbestand und passende Bezahlung in den angegebenen Slots vorhanden sind.
     */
    public boolean canTrade(Container paymentContainer) {
        if (saleItem.isEmpty() || payItemA.isEmpty()) {
            return false;
        }
        if (!hasStock()) {
            return false;
        }

        ItemStack slotA = paymentContainer.getItem(19);
        ItemStack slotB = paymentContainer.getItem(20);
        if (!ItemStack.isSameItemSameComponents(slotA, payItemA) || slotA.getCount() < payItemA.getCount()) {
            return false;
        }
        if (!payItemB.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(slotB, payItemB) || slotB.getCount() < payItemB.getCount()) {
                return false;
            }
        }

        // Simuliere das Einfügen der Bezahlung ins Lager
        ItemStack simulateA = payItemA.copy();
        for (int i = 0; i < inventory.getSlots() && !simulateA.isEmpty(); i++) {
            simulateA = inventory.insertItem(i, simulateA, true);
        }
        if (!simulateA.isEmpty()) {
            return false;
        }
        if (!payItemB.isEmpty()) {
            ItemStack simulateB = payItemB.copy();
            for (int i = 0; i < inventory.getSlots() && !simulateB.isEmpty(); i++) {
                simulateB = inventory.insertItem(i, simulateB, true);
            }
            if (!simulateB.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Führt den Kauf aus und regelt Übergabe und Abzug der Items aus den Bezahl-Slots.
     */
    public void performTrade(Player player, Container paymentContainer) {
        if (!canTrade(paymentContainer)) {
            return;
        }

        // Simuliere erneut das Einfügen, um Race-Conditions zu vermeiden
        ItemStack simulateA = payItemA.copy();
        for (int i = 0; i < inventory.getSlots() && !simulateA.isEmpty(); i++) {
            simulateA = inventory.insertItem(i, simulateA, true);
        }
        if (!simulateA.isEmpty()) {
            return; // Lager kann die Bezahlung nicht aufnehmen
        }

        if (!payItemB.isEmpty()) {
            ItemStack simulateB = payItemB.copy();
            for (int i = 0; i < inventory.getSlots() && !simulateB.isEmpty(); i++) {
                simulateB = inventory.insertItem(i, simulateB, true);
            }
            if (!simulateB.isEmpty()) {
                return; // Lager kann die Bezahlung nicht aufnehmen
            }
        }
        // Entferne Bezahlung aus den Slots
        ItemStack slotA = paymentContainer.getItem(19);
        slotA.shrink(payItemA.getCount());
        paymentContainer.setItem(19, slotA.isEmpty() ? ItemStack.EMPTY : slotA);
        ItemStack slotB = paymentContainer.getItem(20);
        if (!payItemB.isEmpty()) {
            slotB.shrink(payItemB.getCount());
            paymentContainer.setItem(20, slotB.isEmpty() ? ItemStack.EMPTY : slotB);
        }

        // Füge Zahlungen dem Lager hinzu
        ItemStack paymentA = payItemA.copy();
        for (int i = 0; i < inventory.getSlots() && !paymentA.isEmpty(); i++) {
            paymentA = inventory.insertItem(i, paymentA, false);
        }
        if (!paymentA.isEmpty()) {
            player.addItem(paymentA);
            return;
        }
        if (!payItemB.isEmpty()) {
            ItemStack paymentB = payItemB.copy();
            for (int i = 0; i < inventory.getSlots() && !paymentB.isEmpty(); i++) {
                paymentB = inventory.insertItem(i, paymentB, false);
            }
            if (!paymentB.isEmpty()) {
                player.addItem(paymentB);
                return;
            }
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
        if (!payItemA.isEmpty()) {
            tag.put("PayItemA", payItemA.save(provider));
        }
        if (!payItemB.isEmpty()) {
            tag.put("PayItemB", payItemB.save(provider));
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
        payItemA = ItemStack.parseOptional(registries, tag.getCompound("PayItemA"));
        payItemB = ItemStack.parseOptional(registries, tag.getCompound("PayItemB"));
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