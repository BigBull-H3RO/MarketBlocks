package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity implements MenuProvider {
    private static final int INPUT_SLOTS = 12; // 3x4 Input Inventar
    private static final int OUTPUT_SLOTS = 12; // 3x4 Output Inventar
    private static final int PAYMENT_SLOTS = 2; // 2 Bezahlslots
    private static final int OFFER_SLOT = 1; // 1 Angebots-Slot

    // Öffentliche Slot-Indizes für Zugriff in Menüs und Paketen
    public static final int PAYMENT_SLOT_1 = INPUT_SLOTS + OUTPUT_SLOTS;
    public static final int PAYMENT_SLOT_2 = PAYMENT_SLOT_1 + 1;
    public static final int OFFER_RESULT_SLOT = INPUT_SLOTS + OUTPUT_SLOTS + PAYMENT_SLOTS;

    // Inventare
    private final ItemStackHandler inputHandler = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            updateOfferSlot();
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private final ItemStackHandler paymentHandler = new ItemStackHandler(PAYMENT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            updateOfferSlot();
        }
    };

    private final ItemStackHandler offerHandler = new ItemStackHandler(OFFER_SLOT) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack result = super.extractItem(slot, amount, simulate);
            if (!simulate && !result.isEmpty()) {
                processPurchase();
            }
            return result;
        }
    };

    private final CombinedInvWrapper combinedHandler =
            new CombinedInvWrapper(inputHandler, outputHandler, paymentHandler, offerHandler);

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

    // ItemStackHandler Getter
    public ItemStackHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getOutputHandler() {
        return outputHandler;
    }

    public ItemStackHandler getPaymentHandler() {
        return paymentHandler;
    }

    public ItemStackHandler getOfferHandler() {
        return offerHandler;
    }

    public IItemHandler getCombinedHandler() {
        return combinedHandler;
    }

    private void markDirty() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    // Inventar-Utils
    public int getContainerSize() {
        return INPUT_SLOTS + OUTPUT_SLOTS + PAYMENT_SLOTS + OFFER_SLOT;
    }

    private static boolean allEmpty(ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return allEmpty(inputHandler) && allEmpty(outputHandler) &&
                allEmpty(paymentHandler) && offerHandler.getStackInSlot(0).isEmpty();
    }

    public ItemStack getItem(int slot) {
        ItemStackHandler handler = getHandler(slot);
        int local = getLocalSlot(slot);
        return handler != null ? handler.getStackInSlot(local) : ItemStack.EMPTY;
    }

    public ItemStack removeItem(int slot, int amount) {
        ItemStackHandler handler = getHandler(slot);
        int local = getLocalSlot(slot);
        if (handler != null) {
            ItemStack result = handler.extractItem(local, amount, false);
            if (handler != offerHandler && !result.isEmpty()) {
                updateOfferSlot();
            }
            return result;
        }

        return ItemStack.EMPTY;
    }

    public ItemStack removeItemNoUpdate(int slot) {
        ItemStackHandler handler = getHandler(slot);
        int local = getLocalSlot(slot);
        if (handler != null) {
            ItemStack stack = handler.getStackInSlot(local);
            handler.setStackInSlot(local, ItemStack.EMPTY);
            return stack;
        }

        return ItemStack.EMPTY;
    }

    public void setItem(int slot, ItemStack stack) {
        ItemStackHandler handler = getHandler(slot);
        int local = getLocalSlot(slot);
        if (handler != null) {
            handler.setStackInSlot(local, stack);
        }
    }

    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    public void clearContent() {
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            inputHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            outputHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            paymentHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        offerPayment1 = ItemStack.EMPTY;
        offerPayment2 = ItemStack.EMPTY;
        offerResult = ItemStack.EMPTY;
        hasOffer = false;
        setChanged();
        updateOfferSlot();
    }

    private ItemStackHandler getHandler(int slot) {
        if (slot < INPUT_SLOTS) {
            return inputHandler;
        } else if (slot < INPUT_SLOTS + OUTPUT_SLOTS) {
            return outputHandler;
        } else if (slot < OFFER_RESULT_SLOT) {
            return paymentHandler;
        } else if (slot == OFFER_RESULT_SLOT) {
            return offerHandler;
        }
        return null;
    }

    private int getLocalSlot(int slot) {
        if (slot < INPUT_SLOTS) {
            return slot;
        } else if (slot < INPUT_SLOTS + OUTPUT_SLOTS) {
            return slot - INPUT_SLOTS;
        } else if (slot < OFFER_RESULT_SLOT) {
            return slot - INPUT_SLOTS - OUTPUT_SLOTS;
        } else if (slot == OFFER_RESULT_SLOT) {
            return 0;
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
        this.offerHandler.setStackInSlot(0, ItemStack.EMPTY);
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
            return;
        }

        if (canAfford() && hasResultItemInInput()) {
            if (offerHandler.getStackInSlot(0).isEmpty()) {
                offerHandler.setStackInSlot(0, offerResult.copy());
            }
        } else {
            offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    private boolean canAfford() {
        ItemStack payment1 = paymentHandler.getStackInSlot(0);
        ItemStack payment2 = paymentHandler.getStackInSlot(1);

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

        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
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
            paymentHandler.extractItem(0, offerPayment1.getCount(), false);
        }
        if (!offerPayment2.isEmpty()) {
            paymentHandler.extractItem(1, offerPayment2.getCount(), false);
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
        for (int i = 0; i < inputHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                inputHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }
    }

    private void addToOutput(ItemStack toAdd) {
        ItemStack remaining = toAdd;
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            remaining = outputHandler.insertItem(i, remaining, false);
            if (remaining.isEmpty()) {
                return;
            }
        }

        if (!remaining.isEmpty() && level != null && !level.isClientSide) {
            net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), remaining);
        }
    }

    public boolean isOfferAvailable() {
        return hasOffer && hasResultItemInInput();
    }

    private void dropItems(Level level, BlockPos pos, ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        dropItems(level, pos, inputHandler);
        dropItems(level, pos, outputHandler);
        dropItems(level, pos, paymentHandler);
        ItemStack offer = offerHandler.getStackInSlot(0);
        if (!offer.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), offer);
        }
    }

    // NBT Speicherung
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("InputInventory")) {
            inputHandler.deserializeNBT(registries, tag.getCompound("InputInventory"));
        }
        if (tag.contains("OutputInventory")) {
            outputHandler.deserializeNBT(registries, tag.getCompound("OutputInventory"));
        }
        if (tag.contains("PaymentSlots")) {
            paymentHandler.deserializeNBT(registries, tag.getCompound("PaymentSlots"));
        }
        if (tag.contains("OfferSlot")) {
            offerHandler.setStackInSlot(0, ItemStack.parseOptional(registries, tag.getCompound("OfferSlot")));
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

        tag.put("InputInventory", inputHandler.serializeNBT(registries));
        tag.put("OutputInventory", outputHandler.serializeNBT(registries));
        tag.put("PaymentSlots", paymentHandler.serializeNBT(registries));
        ItemStack offer = offerHandler.getStackInSlot(0);
        if (!offer.isEmpty()) {
            tag.put("OfferSlot", offer.save(registries));
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
}