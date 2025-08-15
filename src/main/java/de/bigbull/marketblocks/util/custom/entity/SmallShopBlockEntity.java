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

import java.util.Map;
import java.util.UUID;

public class SmallShopBlockEntity extends BlockEntity implements MenuProvider {
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

    // Inventare
    private final ItemStackHandler inputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            updateOfferSlot();
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private final ItemStackHandler paymentHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            updateOfferSlot();
        }
    };

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
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

    private final Map<String, ItemStackHandler> handlerMap = Map.of(
            "InputInventory", inputHandler,
            "OutputInventory", outputHandler,
            "PaymentSlots", paymentHandler,
            "OfferSlot", offerHandler
    );

    private final IItemHandler inputOnly  = new SidedWrapper(inputHandler, false);
    private final IItemHandler outputOnly = new SidedWrapper(outputHandler, true);

    record SidedWrapper(IItemHandler backing, boolean extractOnly) implements IItemHandler {
        public int getSlots() {
            return backing.getSlots();
        }

        public ItemStack getStackInSlot(int slot) {
            return backing.getStackInSlot(slot);
        }

        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return extractOnly ? stack : backing.insertItem(slot, stack, simulate);
            }

        public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return extractOnly ? backing.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
            }

        public int getSlotLimit(int slot) {
            return backing.getSlotLimit(slot);
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            return !extractOnly && backing.isItemValid(slot, stack);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.marketblocks.small_shop");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SmallShopOffersMenu(containerId, playerInventory, this);
    }

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

    public IItemHandler getInputOnly()  { return inputOnly; }
    public IItemHandler getOutputOnly() { return outputOnly; }

    private void markDirty() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
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

    public boolean canAfford() {
        ItemStack payment1 = paymentHandler.getStackInSlot(0);
        ItemStack payment2 = paymentHandler.getStackInSlot(1);

        // Fall 1: Beide Payments sind das gleiche Item (stackable)
        if (!offerPayment1.isEmpty() && !offerPayment2.isEmpty() &&
                ItemStack.isSameItemSameComponents(offerPayment1, offerPayment2)) {

            int total = 0;
            if (ItemStack.isSameItemSameComponents(payment1, offerPayment1)) {
                total += payment1.getCount();
            }
            if (ItemStack.isSameItemSameComponents(payment2, offerPayment1)) {
                total += payment2.getCount();
            }
            return total >= offerPayment1.getCount() + offerPayment2.getCount();
        }

        // Fall 2: Zwei verschiedene Payment-Items
        if (!offerPayment1.isEmpty() && !offerPayment2.isEmpty()) {
            // Prüfe beide möglichen Slot-Kombinationen
            boolean case1 = ItemStack.isSameItemSameComponents(payment1, offerPayment1) &&
                    payment1.getCount() >= offerPayment1.getCount() &&
                    ItemStack.isSameItemSameComponents(payment2, offerPayment2) &&
                    payment2.getCount() >= offerPayment2.getCount();

            boolean case2 = ItemStack.isSameItemSameComponents(payment1, offerPayment2) &&
                    payment1.getCount() >= offerPayment2.getCount() &&
                    ItemStack.isSameItemSameComponents(payment2, offerPayment1) &&
                    payment2.getCount() >= offerPayment1.getCount();

            return case1 || case2;
        }

        // Fall 3: Nur ein Payment-Item (offerPayment2 ist leer)
        if (!offerPayment1.isEmpty() && offerPayment2.isEmpty()) {
            return (ItemStack.isSameItemSameComponents(payment1, offerPayment1) &&
                    payment1.getCount() >= offerPayment1.getCount()) ||
                    (ItemStack.isSameItemSameComponents(payment2, offerPayment1) &&
                            payment2.getCount() >= offerPayment1.getCount());
        }

        // Fall 4: Kein Payment erforderlich (sollte nicht vorkommen, aber sicherheitshalber)
        return offerPayment1.isEmpty() && offerPayment2.isEmpty();
    }

    public boolean hasResultItemInInput() {
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
            removePayment(offerPayment1, offerPayment1.getCount());
        }
        if (!offerPayment2.isEmpty()) {
            removePayment(offerPayment2, offerPayment2.getCount());
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
        updateOfferSlot();
    }

    public void performPurchase() {
        processPurchase();
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

    private void removePayment(ItemStack required, int amount) {
        if (required.isEmpty() || amount <= 0) {
            return;
        }

        int remaining = amount;

        // Durchlaufe beide Payment-Slots und entferne die erforderliche Menge
        for (int i = 0; i < 2 && remaining > 0; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toTake = Math.min(remaining, stack.getCount());
                paymentHandler.extractItem(i, toTake, false);
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
        handlerMap.values().forEach(handler -> dropItems(level, pos, handler));
    }

    private void saveHandlers(CompoundTag tag, HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> tag.put(name, handler.serializeNBT(registries)));
    }

    private void loadHandlers(CompoundTag tag, HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> {
            if (tag.contains(name)) {
                handler.deserializeNBT(registries, tag.getCompound(name));
            }
        });
    }

    public int calculateMaxPurchasable(ItemStack resultTemplate) {
        if (resultTemplate.isEmpty() || !hasOffer()) return 0;

        // Wie viele volle Result-Batches sind im Input vorhanden?
        int batchesFromInput = countResultBatchesInInput(resultTemplate);

        // Wie viele Batches kann man sich leisten (auf Basis beider Payment-Slots, inkl. Slottausch & same-item-Case)?
        int batchesFromPayment = countAffordableBatches();

        return Math.min(batchesFromInput, batchesFromPayment);
    }

    /** Zählt, wie viele volle resultTemplate-Batches im Input liegen. */
    private int countResultBatchesInInput(ItemStack template) {
        int total = 0;
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack s = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(s, template)) {
                total += s.getCount();
            }
        }
        int perBatch = Math.max(1, template.getCount());
        return total / perBatch;
    }

    /** Ermittelt, wie viele “Angebots-Sets” vollständig bezahlt werden können. */
    private int countAffordableBatches() {
        // Keine Payments → theoretisch unendlich; real limitiert dann nur durch Input
        boolean p1Empty = offerPayment1.isEmpty();
        boolean p2Empty = offerPayment2.isEmpty();
        if (p1Empty && p2Empty) return Integer.MAX_VALUE;

        // Hole Inhalte beider Payment-Slots
        ItemStack slot0 = paymentHandler.getStackInSlot(0);
        ItemStack slot1 = paymentHandler.getStackInSlot(1);

        // Helper: zähle, wie viele Items eines Typs in beiden Slots liegen
        java.util.function.Function<ItemStack, Integer> totalOf = (tmpl) -> {
            int n = 0;
            if (!tmpl.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(slot0, tmpl)) n += slot0.getCount();
                if (ItemStack.isSameItemSameComponents(slot1, tmpl)) n += slot1.getCount();
            }
            return n;
        };

        if (!p1Empty && !p2Empty) {
            // Zwei Payments vorhanden
            boolean sameItem = ItemStack.isSameItemSameComponents(offerPayment1, offerPayment2);
            if (sameItem) {
                // beide Payments sind dasselbe Item → Summiere beide Slot-Bestände und teile durch Summe der Anforderungen
                int totalAvailable = totalOf.apply(offerPayment1);
                int need = offerPayment1.getCount() + offerPayment2.getCount();
                if (need <= 0) return 0;
                return totalAvailable / need;
            } else {
                // zwei verschiedene Items → min(availableA/needA, availableB/needB)
                int haveA = totalOf.apply(offerPayment1);
                int haveB = totalOf.apply(offerPayment2);
                int needA = Math.max(1, offerPayment1.getCount());
                int needB = Math.max(1, offerPayment2.getCount());
                return Math.min(haveA / needA, haveB / needB);
            }
        }

        // Nur ein Payment-Item
        ItemStack required = p1Empty ? offerPayment2 : offerPayment1;
        int need = Math.max(1, required.getCount());
        int have = totalOf.apply(required);
        return have / need;
    }


    // NBT Speicherung
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        loadHandlers(tag, registries);

        if (tag.contains("OfferPayment1")) {
            offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound("OfferPayment1"));
        } else {
            offerPayment1 = ItemStack.EMPTY;
        }
        if (tag.contains("OfferPayment2")) {
            offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound("OfferPayment2"));
        } else {
            offerPayment2 = ItemStack.EMPTY;
        }
        if (tag.contains("OfferResult")) {
            offerResult = ItemStack.parseOptional(registries, tag.getCompound("OfferResult"));
        } else {
            offerResult = ItemStack.EMPTY;
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

        saveHandlers(tag, registries);

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