package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity implements MenuProvider, Container {
    private static final int INPUT_SLOTS = 12; // 3x4 Input Inventar
    private static final int OUTPUT_SLOTS = 12; // 3x4 Output Inventar
    private static final int PAYMENT_SLOTS = 2; // 2 Bezahlslots
    private static final int OFFER_SLOT = 1; // 1 Angebots-Slot

    // Öffentliche Slot-Indizes für Zugriff in Menüs und Paketen
    public static final int PAYMENT_SLOT_1 = INPUT_SLOTS + OUTPUT_SLOTS;
    public static final int PAYMENT_SLOT_2 = PAYMENT_SLOT_1 + 1;
    public static final int OFFER_RESULT_SLOT = INPUT_SLOTS + OUTPUT_SLOTS + PAYMENT_SLOTS;

    // Inventare
    private final NonNullList<ItemStack> inputInventory = NonNullList.withSize(INPUT_SLOTS, ItemStack.EMPTY);
    private final NonNullList<ItemStack> outputInventory = NonNullList.withSize(OUTPUT_SLOTS, ItemStack.EMPTY);
    private final NonNullList<ItemStack> paymentSlots = NonNullList.withSize(PAYMENT_SLOTS, ItemStack.EMPTY);
    private ItemStack offerSlot = ItemStack.EMPTY;

    // Angebots-System
    private ItemStack offerPayment1 = ItemStack.EMPTY;
    private ItemStack offerPayment2 = ItemStack.EMPTY;
    private ItemStack offerResult = ItemStack.EMPTY;
    private boolean hasOffer = false;

    // Owner System
    private UUID ownerId = null;
    private String ownerName = "";

    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.marketblocks.small_shop");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        // Fallback: Verwende Offers Menu als Standard
        return new SmallShopOffersMenu(containerId, playerInventory, this);
    }

    // Container Implementation
    @Override
    public int getContainerSize() {
        // FIXED: Total = INPUT + OUTPUT + PAYMENT + OFFER = 12 + 12 + 2 + 1 = 27
        return INPUT_SLOTS + OUTPUT_SLOTS + PAYMENT_SLOTS + OFFER_SLOT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inputInventory) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : outputInventory) {
            if (!stack.isEmpty()) return false;
        }
        for (ItemStack stack : paymentSlots) {
            if (!stack.isEmpty()) return false;
        }
        return offerSlot.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < INPUT_SLOTS) {
            return inputInventory.get(slot);
        } else if (slot < INPUT_SLOTS + OUTPUT_SLOTS) {
            return outputInventory.get(slot - INPUT_SLOTS);
        } else if (slot < OFFER_RESULT_SLOT) {
            // Payment-Slots
            return paymentSlots.get(slot - INPUT_SLOTS - OUTPUT_SLOTS);
        } else if (slot == OFFER_RESULT_SLOT) {
            // Slot für das Ergebnis des Angebots
            return offerSlot;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        NonNullList<ItemStack> list = getInventoryList(slot);
        int localSlot = getLocalSlot(slot);

        if (localSlot >= 0 && localSlot < list.size()) {
            ItemStack result = ContainerHelper.removeItem(list, localSlot, amount);
            if (!result.isEmpty()) {
                setChanged();
                updateOfferSlot();
            }
            return result;
        }

        if (slot == OFFER_RESULT_SLOT) {
            ItemStack result = offerSlot.split(amount);
            if (!result.isEmpty()) {
                setChanged();
                processPurchase();
            }
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> list = getInventoryList(slot);
        int localSlot = getLocalSlot(slot);

        if (localSlot >= 0 && localSlot < list.size()) {
            return ContainerHelper.takeItem(list, localSlot);
        }

        if (slot == OFFER_RESULT_SLOT) {
            ItemStack result = offerSlot;
            offerSlot = ItemStack.EMPTY;
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        NonNullList<ItemStack> list = getInventoryList(slot);
        int localSlot = getLocalSlot(slot);

        if (localSlot >= 0 && localSlot < list.size()) {
            list.set(localSlot, stack);
            setChanged();
            updateOfferSlot();
            return;
        }

        if (slot == OFFER_RESULT_SLOT) {
            offerSlot = stack;
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        // Löscht Inventar und Angebotszustand
        inputInventory.clear();
        outputInventory.clear();
        paymentSlots.clear();
        offerSlot = ItemStack.EMPTY;
        offerPayment1 = ItemStack.EMPTY;
        offerPayment2 = ItemStack.EMPTY;
        offerResult = ItemStack.EMPTY;
        hasOffer = false;
        setChanged();
        updateOfferSlot();
    }

    // Hilfsmethoden für Slot-Management
    private NonNullList<ItemStack> getInventoryList(int slot) {
        if (slot < INPUT_SLOTS) {
            return inputInventory;
        } else if (slot < INPUT_SLOTS + OUTPUT_SLOTS) {
            return outputInventory;
        } else if (slot < OFFER_RESULT_SLOT) {
            return paymentSlots;
        }
        return NonNullList.withSize(0, ItemStack.EMPTY);
    }

    private int getLocalSlot(int slot) {
        if (slot < INPUT_SLOTS) {
            return slot;
        } else if (slot < INPUT_SLOTS + OUTPUT_SLOTS) {
            return slot - INPUT_SLOTS;
        } else if (slot < OFFER_RESULT_SLOT) {
            return slot - INPUT_SLOTS - OUTPUT_SLOTS;
        }
        return -1;
    }

    // Owner-System
    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public boolean isOwner(Player player) {
        return ownerId != null && ownerId.equals(player.getUUID());
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    // Angebots-System
    public void createOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        this.offerPayment1 = payment1.copy();
        this.offerPayment2 = payment2.copy();
        this.offerResult = result.copy();
        this.hasOffer = true;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        updateOfferSlot();
    }

    public void clearOffer() {
        this.offerPayment1 = ItemStack.EMPTY;
        this.offerPayment2 = ItemStack.EMPTY;
        this.offerResult = ItemStack.EMPTY;
        this.hasOffer = false;
        this.offerSlot = ItemStack.EMPTY;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasOffer() {
        return hasOffer;
    }

    public void setHasOfferClient(boolean hasOffer) {
        this.hasOffer = hasOffer;
    }

    public ItemStack getOfferPayment1() {
        return offerPayment1;
    }

    public ItemStack getOfferPayment2() {
        return offerPayment2;
    }

    public ItemStack getOfferResult() {
        return offerResult;
    }

    // Angebots-Logik
    public void updateOfferSlot() {
        if (!hasOffer) {
            // Während der Angebotserstellung sollen die Slots unverändert bleiben
            return;
        }

        // Prüfe ob genügend Bezahlitems vorhanden sind
        if (canAfford() && hasResultItemInInput()) {
            if (offerSlot.isEmpty()) {
                offerSlot = offerResult.copy();
            }
        } else {
            offerSlot = ItemStack.EMPTY;
        }
    }

    private boolean canAfford() {
        ItemStack payment1 = paymentSlots.get(0);
        ItemStack payment2 = paymentSlots.get(1);

        // Prüfe erste Bezahlung
        if (!offerPayment1.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(payment1, offerPayment1) ||
                    payment1.getCount() < offerPayment1.getCount()) {
                return false;
            }
        }

        // Prüfe zweite Bezahlung
        if (!offerPayment2.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(payment2, offerPayment2) ||
                    payment2.getCount() < offerPayment2.getCount()) {
                return false;
            }
        }

        return true;
    }

    private boolean hasResultItemInInput() {
        if (offerResult.isEmpty()) return false;

        for (ItemStack stack : inputInventory) {
            if (ItemStack.isSameItemSameComponents(stack, offerResult) &&
                    stack.getCount() >= offerResult.getCount()) {
                return true;
            }
        }
        return false;
    }

    private void processPurchase() {
        if (!hasOffer || !canAfford() || !hasResultItemInInput()) {
            return;
        }

        // Entferne Bezahlung aus Payment-Slots
        if (!offerPayment1.isEmpty()) {
            paymentSlots.get(0).shrink(offerPayment1.getCount());
        }
        if (!offerPayment2.isEmpty()) {
            paymentSlots.get(1).shrink(offerPayment2.getCount());
        }

        // Entferne Result-Item aus Input-Inventar
        removeFromInput(offerResult);

        // Füge Bezahlung zu Output-Inventar hinzu
        if (!offerPayment1.isEmpty()) {
            addToOutput(offerPayment1.copy());
        }
        if (!offerPayment2.isEmpty()) {
            addToOutput(offerPayment2.copy());
        }

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        updateOfferSlot(); // Prüfe ob noch ein Kauf möglich ist
    }

    private void removeFromInput(ItemStack toRemove) {
        int remaining = toRemove.getCount();
        for (int i = 0; i < inputInventory.size() && remaining > 0; i++) {
            ItemStack stack = inputInventory.get(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                remaining -= toTake;
            }
        }
    }

    private void addToOutput(ItemStack toAdd) {
        for (int i = 0; i < outputInventory.size(); i++) {
            ItemStack stack = outputInventory.get(i);
            if (stack.isEmpty()) {
                outputInventory.set(i, toAdd);
                return;
            } else if (ItemStack.isSameItemSameComponents(stack, toAdd)) {
                int canAdd = Math.min(toAdd.getCount(), stack.getMaxStackSize() - stack.getCount());
                if (canAdd > 0) {
                    stack.grow(canAdd);
                    toAdd.shrink(canAdd);
                    if (toAdd.isEmpty()) return;
                }
            }
        }

        // Falls nach dem Durchlauf noch Items übrig sind, dürfen sie nicht verloren gehen
        if (!toAdd.isEmpty() && level != null && !level.isClientSide) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), toAdd);
        }
    }

    public boolean isOfferAvailable() {
        return hasOffer && hasResultItemInInput();
    }

    public void dropContents(Level level, BlockPos pos) {
        // Droppe Input Inventar
        for (ItemStack stack : inputInventory) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        // Droppe Output Inventar
        for (ItemStack stack : outputInventory) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        // Droppe Payment Slots
        for (ItemStack stack : paymentSlots) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        // Droppe Offer Slot
        if (!offerSlot.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), offerSlot);
        }
    }

    // NBT Speicherung
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        ContainerHelper.loadAllItems(tag.getCompound("InputInventory"), inputInventory, registries);
        ContainerHelper.loadAllItems(tag.getCompound("OutputInventory"), outputInventory, registries);
        ContainerHelper.loadAllItems(tag.getCompound("PaymentSlots"), paymentSlots, registries);

        if (tag.contains("OfferSlot")) {
            offerSlot = ItemStack.parseOptional(registries, tag.getCompound("OfferSlot"));
        }

        if (tag.contains("OfferPayment1")) {
            offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound("OfferPayment1"));
        }
        if (tag.contains("OfferPayment2")) {
            offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound("OfferPayment2"));
        }
        if (tag.contains("OfferResult")) {
            offerResult = ItemStack.parseOptional(registries, tag.getCompound("OfferResult"));
        }

        hasOffer = tag.getBoolean("HasOffer");

        if (tag.hasUUID("OwnerId")) {
            ownerId = tag.getUUID("OwnerId");
        }
        if (tag.contains("OwnerName")) {
            ownerName = tag.getString("OwnerName");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag inputTag = new CompoundTag();
        ContainerHelper.saveAllItems(inputTag, inputInventory, registries);
        tag.put("InputInventory", inputTag);

        CompoundTag outputTag = new CompoundTag();
        ContainerHelper.saveAllItems(outputTag, outputInventory, registries);
        tag.put("OutputInventory", outputTag);

        CompoundTag paymentTag = new CompoundTag();
        ContainerHelper.saveAllItems(paymentTag, paymentSlots, registries);
        tag.put("PaymentSlots", paymentTag);

        if (!offerSlot.isEmpty()) {
            tag.put("OfferSlot", offerSlot.save(registries));
        }

        if (!offerPayment1.isEmpty()) {
            tag.put("OfferPayment1", offerPayment1.save(registries));
        }
        if (!offerPayment2.isEmpty()) {
            tag.put("OfferPayment2", offerPayment2.save(registries));
        }
        if (!offerResult.isEmpty()) {
            tag.put("OfferResult", offerResult.save(registries));
        }

        tag.putBoolean("HasOffer", hasOffer);

        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }
        tag.putString("OwnerName", ownerName);
    }

    // Synchronisation
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    // Inventar-Zugriff für UI
    public NonNullList<ItemStack> getInputInventory() {
        return inputInventory;
    }

    public NonNullList<ItemStack> getOutputInventory() {
        return outputInventory;
    }

    public NonNullList<ItemStack> getPaymentSlots() {
        return paymentSlots;
    }

    public ItemStack getOfferSlot() {
        return offerSlot;
    }

    // FIXED: Getter-Methoden für Slot-Zugriff
    public ItemStack getPaymentSlot1() {
        return getItem(PAYMENT_SLOT_1);

    }

    public ItemStack getPaymentSlot2() {
        return getItem(PAYMENT_SLOT_2);

    }

    public ItemStack getOfferResultSlot() {
        return getItem(OFFER_RESULT_SLOT);

    }
}