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
import net.minecraft.world.inventory.ContainerData;
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
    private static final String KEY_PAYMENT1 = "OfferPayment1";
    private static final String KEY_PAYMENT2 = "OfferPayment2";
    private static final String KEY_RESULT = "OfferResult";
    private ItemStack offerPayment1 = ItemStack.EMPTY;
    private ItemStack offerPayment2 = ItemStack.EMPTY;
    private ItemStack offerResult   = ItemStack.EMPTY;
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

    private final OfferManager offerManager = new OfferManager(this);

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

    public OfferManager getOfferManager() {
        return offerManager;
    }

    public IItemHandler getInputOnly()  { return inputOnly; }
    public IItemHandler getOutputOnly() { return outputOnly; }

    private void markDirty() {
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

    public void ensureOwner(Player player) {
        if (!player.level().isClientSide() && ownerId == null) {
            setOwner(player);
        }
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
        sync();
        updateOfferSlot();
    }

    public void clearOffer() {
        offerPayment1 = ItemStack.EMPTY;
        offerPayment2 = ItemStack.EMPTY;
        offerResult = ItemStack.EMPTY;
        this.hasOffer = false;
        this.offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        sync();
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

    private int countMatching(ItemStack target) {
        int total = 0;
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private boolean hasEnough(ItemStack required) {
        return required.isEmpty() || countMatching(required) >= required.getCount();
    }

    public void updateOfferSlot() {
        if (!hasOffer) {
            return;
        }

        if (canAfford() && hasResultItemInInput()) {
            if (offerHandler.getStackInSlot(0).isEmpty()) {
                offerHandler.setStackInSlot(0, getOfferResult().copy());
            }
        } else {
            offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public boolean canAfford() {
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        if (p1.isEmpty() && p2.isEmpty()) {
            return true;
        }

        if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
            int required = p1.getCount() + p2.getCount();
            return countMatching(p1) >= required;
        }

        return hasEnough(p1) && hasEnough(p2);
    }

    public boolean hasResultItemInInput() {
        ItemStack result = getOfferResult();
        if (result.isEmpty()) return false;

        int total = 0;
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, result)) {
                total += stack.getCount();
                if (total >= result.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void processPurchase() {
        if (!hasOffer || !canAfford() || !hasResultItemInInput()) {
            return;
        }

        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        ItemStack result = getOfferResult();
        if (!p1.isEmpty()) {
            removePayment(p1, p1.getCount());
        }
        if (!p2.isEmpty()) {
                removePayment(p2, p2.getCount());
        }

        removeFromInput(result);

        if (!p1.isEmpty()) {
            addToOutput(p1.copy());
        }
        if (!p2.isEmpty()) {
            addToOutput(p2.copy());
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

    public ContainerData createMenuFlags(Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int flags = 0;
                    if (hasOffer()) flags |= 1;
                    if (isOfferAvailable()) flags |= 2;
                    if (isOwner(player)) flags |= 4;
                    return flags;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                // nicht benötigt
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
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

    private void loadOwner(CompoundTag tag) {
        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : "";
    }

    private void saveOwner(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }
        tag.putString("OwnerName", ownerName);
    }

    // NBT Speicherung
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadHandlers(tag, registries);

        offerPayment1 = tag.contains(KEY_PAYMENT1)
                ? ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1))
                : ItemStack.EMPTY;
        offerPayment2 = tag.contains(KEY_PAYMENT2)
                ? ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2))
                : ItemStack.EMPTY;
        offerResult = tag.contains(KEY_RESULT)
                ? ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT))
                : ItemStack.EMPTY;

        hasOffer = tag.getBoolean("HasOffer");
        loadOwner(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveHandlers(tag, registries);

        if (!offerPayment1.isEmpty()) {
            tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
        }
        if (!offerPayment2.isEmpty()) {
            tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
        }
        if (!offerResult.isEmpty()) {
            tag.put(KEY_RESULT, offerResult.save(registries));
        }

        tag.putBoolean("HasOffer", hasOffer);
        saveOwner(tag);
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