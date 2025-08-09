package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    // Zwei getrennte Lagerbereiche: Zahlungen (0..11) und Verkaufsware (0..11)
    private final ItemStackHandler payments = new ItemStackHandler(12);
    private final ItemStackHandler stock = new ItemStackHandler(12);
    private ItemStack saleItem = ItemStack.EMPTY;
    private ItemStack payItemA = ItemStack.EMPTY;
    private ItemStack payItemB = ItemStack.EMPTY;
    private ItemEntity displayItem;
    private ItemEntity payDisplayItemA;
    private ItemEntity payDisplayItemB;
    private UUID owner;

    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Zugriff auf das Zahlungs-Lager (rechte Seite).
     */
    public ItemStackHandler getPayments() {
        return payments;
    }

    /**
     * Zugriff auf das Verkaufs-Lager (linke Seite).
     */
    public ItemStackHandler getStock() {
        return stock;
    }

    public ItemStack getSaleItem() {
        return saleItem;
    }

    public void setSaleItem(ItemStack stack) {
        this.saleItem = stack;
        updateDisplayItems();
        sync();
    }

    public ItemStack getPayItemA() {
        return payItemA;
    }

    public void setPayItemA(ItemStack stack) {
        this.payItemA = stack;
        updateDisplayItems();
        sync();
    }

    public ItemStack getPayItemB() {
        return payItemB;
    }

    public void setPayItemB(ItemStack stack) {
        this.payItemB = stack;
        updateDisplayItems();
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

    public void setPayDisplayItemA(ItemEntity item) {
        this.payDisplayItemA = item;
    }

    public void discardPayDisplayItemA() {
        if (payDisplayItemA != null) {
            payDisplayItemA.discard();
            payDisplayItemA = null;
        }
    }

    public void setPayDisplayItemB(ItemEntity item) {
        this.payDisplayItemB = item;
    }

    public void discardPayDisplayItemB() {
        if (payDisplayItemB != null) {
            payDisplayItemB.discard();
            payDisplayItemB = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            updateDisplayItems();
        }
    }

    private void updateDisplayItems() {
        if (level == null || level.isClientSide) {
            return;
        }
        discardDisplayItem();
        discardPayDisplayItemA();
        discardPayDisplayItemB();
        if (saleItem.isEmpty()) {
            return;
        }
        BlockPos pos = getBlockPos();
        ItemEntity sale = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, saleItem.copy());
        sale.setNoGravity(true);
        sale.setNeverPickUp();
        sale.setUnlimitedLifetime();
        level.addFreshEntity(sale);
        setDisplayItem(sale);

        Direction facing = getBlockState().getValue(de.bigbull.marketblocks.util.custom.block.SmallShopBlock.FACING);
        double offX = pos.getX() + 0.5 + facing.getStepX() * 0.7;
        double offZ = pos.getZ() + 0.5 + facing.getStepZ() * 0.7;

        if (!payItemA.isEmpty()) {
            ItemEntity payA = new ItemEntity(level, offX, pos.getY() + 1.0, offZ, payItemA.copy());
            payA.setNoGravity(true);
            payA.setNeverPickUp();
            payA.setUnlimitedLifetime();
            level.addFreshEntity(payA);
            setPayDisplayItemA(payA);
        }
        if (!payItemB.isEmpty()) {
            Direction side = facing.getClockWise();
            double offXB = offX + side.getStepX() * 0.25;
            double offZB = offZ + side.getStepZ() * 0.25;
            ItemEntity payB = new ItemEntity(level, offXB, pos.getY() + 1.0, offZB, payItemB.copy());
            payB.setNoGravity(true);
            payB.setNeverPickUp();
            payB.setUnlimitedLifetime();
            level.addFreshEntity(payB);
            setPayDisplayItemB(payB);
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

        ItemStack slotA = paymentContainer.getItem(25);
        ItemStack slotB = paymentContainer.getItem(26);
        if (!ItemStack.isSameItemSameComponents(slotA, payItemA) || slotA.getCount() < payItemA.getCount()) {
            return false;
        }
        if (!payItemB.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(slotB, payItemB) || slotB.getCount() < payItemB.getCount()) {
                return false;
            }
        }

        // Simuliere das Einfügen der Bezahlung ins Zahlungs-Lager
        ItemStack simulateA = payItemA.copy();
        for (int i = 0; i < payments.getSlots() && !simulateA.isEmpty(); i++) {
            simulateA = payments.insertItem(i, simulateA, true);
        }
        if (!simulateA.isEmpty()) {
            return false;
        }
        if (!payItemB.isEmpty()) {
            ItemStack simulateB = payItemB.copy();
            for (int i = 0; i < payments.getSlots() && !simulateB.isEmpty(); i++) {
                simulateB = payments.insertItem(i, simulateB, true);
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
        for (int i = 0; i < payments.getSlots() && !simulateA.isEmpty(); i++) {
            simulateA = payments.insertItem(i, simulateA, true);
        }
        if (!simulateA.isEmpty()) {
            return; // Lager kann die Bezahlung nicht aufnehmen
        }

        if (!payItemB.isEmpty()) {
            ItemStack simulateB = payItemB.copy();
            for (int i = 0; i < payments.getSlots() && !simulateB.isEmpty(); i++) {
                simulateB = payments.insertItem(i, simulateB, true);
            }
            if (!simulateB.isEmpty()) {
                return; // Lager kann die Bezahlung nicht aufnehmen
            }
        }
        // Entferne Bezahlung aus den Slots
        ItemStack slotA = paymentContainer.getItem(25);
        slotA.shrink(payItemA.getCount());
        paymentContainer.setItem(25, slotA.isEmpty() ? ItemStack.EMPTY : slotA);
        ItemStack slotB = paymentContainer.getItem(26);
        if (!payItemB.isEmpty()) {
            slotB.shrink(payItemB.getCount());
            paymentContainer.setItem(26, slotB.isEmpty() ? ItemStack.EMPTY : slotB);
        }

        // Füge Zahlungen dem Lager hinzu
        ItemStack paymentA = payItemA.copy();
        for (int i = 0; i < payments.getSlots() && !paymentA.isEmpty(); i++) {
            paymentA = payments.insertItem(i, paymentA, false);
        }
        if (!paymentA.isEmpty()) {
            player.addItem(paymentA);
            return;
        }
        if (!payItemB.isEmpty()) {
            ItemStack paymentB = payItemB.copy();
            for (int i = 0; i < payments.getSlots() && !paymentB.isEmpty(); i++) {
                paymentB = payments.insertItem(i, paymentB, false);
            }
            if (!paymentB.isEmpty()) {
                player.addItem(paymentB);
                return;
            }
        }

        // Entferne Verkaufsware aus dem Lager
        for (int i = 0; i < stock.getSlots(); i++) {
            ItemStack stack = stock.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, saleItem)) {
                stack.shrink(saleItem.getCount());
                stock.setStackInSlot(i, stack);
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
        for (int i = 0; i < stock.getSlots(); i++) {
            ItemStack stack = stock.getStackInSlot(i);
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
        tag.put("Payments", payments.serializeNBT(provider));
        tag.put("Stock", stock.serializeNBT(provider));
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
        payments.deserializeNBT(registries, tag.getCompound("Payments"));
        stock.deserializeNBT(registries, tag.getCompound("Stock"));
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