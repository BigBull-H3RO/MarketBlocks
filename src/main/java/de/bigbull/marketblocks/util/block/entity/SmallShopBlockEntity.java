package de.bigbull.marketblocks.util.block.entity;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.block.BaseShopBlock;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlockNeu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
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
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * Represents the core logic for the Small Shop block. This BlockEntity handles
 * everything from inventory management, offer creation, player ownership,
 * and interaction with adjacent blocks.
 */
public class SmallShopBlockEntity extends BlockEntity implements MenuProvider {
    // Constants
    private static final int MAX_SHOP_NAME_LENGTH = 32;
    private static final double MAX_PLAYER_DISTANCE_SQUARED = 64.0;
    private static final Direction[] DIRECTIONS = Direction.values();

    // NBT Keys
    private static final String NBT_HAS_OFFER = "HasOffer";
    private static final String NBT_SHOP_NAME = "ShopName";
    private static final String NBT_EMIT_REDSTONE = "EmitRedstone";
    private static final String NBT_SIDE_MODES = "SideModes";
    private static final String NBT_DIRECTION = "Direction";
    private static final String NBT_MODE = "Mode";
    private static final String KEY_PAYMENT1 = "OfferPayment1";
    private static final String KEY_PAYMENT2 = "OfferPayment2";
    private static final String KEY_RESULT = "OfferResult";
    private static final String NBT_OUTPUT_WARNING = "OutputWarning";
    private static final String NBT_OUTPUT_FULL = "OutputFull";

    // Inventory Handler Names
    private static final String HANDLER_INPUT = "InputInventory";
    private static final String HANDLER_OUTPUT = "OutputInventory";
    private static final String HANDLER_PAYMENT = "PaymentSlots";
    private static final String HANDLER_OFFER = "OfferSlot";

    // Menu Flags
    public static final int HAS_OFFER_FLAG = 1;
    public static final int OFFER_AVAILABLE_FLAG = 2;
    public static final int OWNER_FLAG = 4;
    public static final int PRIMARY_OWNER_FLAG = 8;

    // Offer System
    private ItemStack offerPayment1 = ItemStack.EMPTY;
    private ItemStack offerPayment2 = ItemStack.EMPTY;
    private ItemStack offerResult = ItemStack.EMPTY;
    private boolean hasOffer = false;

    // Transaction Log
    private final LinkedList<String> transactionLog = new LinkedList<>();

    // Owner System
    private final ShopOwnerManager ownerManager = new ShopOwnerManager(this);
    private final ShopInventoryManager inventoryManager = new ShopInventoryManager(this);

    // Shop Settings
    private String shopName = "";
    private boolean emitRedstone = false;
    private boolean outputAlmostFull = false;
    private boolean outputFull = false;

    // Side Configuration
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);

    // --- CORE HANDLERS ---

    // --- Inventories ---
    private class TrackedItemStackHandler extends ItemStackHandler {
        TrackedItemStackHandler(int size) {
            super(size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateOfferSlot(); // Immediate update for UI responsiveness
            needsOfferRefresh = true; // Flag for tick-based neighbor check
            // Note: sync() is batched in tick() to reduce network spam
        }
    }

    private final ItemStackHandler inputHandler = new TrackedItemStackHandler(12);

    private final ItemStackHandler outputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateOfferSlot(); // Immediate update for UI responsiveness
            needsOfferRefresh = true; // Flag for tick-based checks
            // Note: updateOutputFullness() and sync() are batched in tick()
        }
    };

    private final ItemStackHandler paymentHandler = new TrackedItemStackHandler(2);

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // Fix Item Loss Bug: Ensure we only allow extraction of the FULL offer amount
            // ONLY when an offer is active. In creation mode, normal rules apply.
            if (hasOffer()) {
                ItemStack currentOffer = getOfferResult();
                if (currentOffer.isEmpty()) return ItemStack.EMPTY;

                // Enforce all-or-nothing extraction to prevent partial payment
                if (amount < currentOffer.getCount()) {
                    return ItemStack.EMPTY;
                }
            }

            if (simulate) {
                return super.extractItem(slot, amount, true);
            }

            // Purchase logic only applies if an offer exists
            if (hasOffer()) {
                if (!isReadyToPurchase()) {
                    updateOfferSlot();
                    return ItemStack.EMPTY;
                }
            }

            ItemStack result = super.extractItem(slot, amount, false);

            if (hasOffer() && !result.isEmpty()) {
                processPurchase();
            }

            return result;
        }
    };

    private final Map<String, ItemStackHandler> handlerMap = Map.of(
            HANDLER_INPUT, inputHandler,
            HANDLER_OUTPUT, outputHandler,
            HANDLER_PAYMENT, paymentHandler,
            HANDLER_OFFER, offerHandler
    );

    private final IItemHandler inputOnly = new SidedWrapper(inputHandler, false);
    private final IItemHandler outputOnly = new SidedWrapper(outputHandler, true);

    private final OfferManager offerManager = new OfferManager(this);

    private int tickCounter = 0;
    private volatile boolean needsOfferRefresh = false;


    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
        for (Direction dir : DIRECTIONS) {
            sideModes.put(dir, SideMode.DISABLED);
        }
    }

    // --- Sided Wrapper for Item Handlers ---
    /**
     * A wrapper for IItemHandler that restricts insertion or extraction.
     * @param backing The backing item handler.
     * @param extractOnly If true, only extraction is allowed. If false, only insertion is allowed.
     */
    record SidedWrapper(IItemHandler backing, boolean extractOnly) implements IItemHandler {
        @Override
        public int getSlots() {
            return backing.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return backing.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (extractOnly) {
                return stack; // Cannot insert into an extract-only wrapper
            }
            return backing.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!extractOnly) {
                return ItemStack.EMPTY; // Cannot extract from an insert-only wrapper
            }
            return backing.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return backing.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !extractOnly && backing.isItemValid(slot, stack);
        }
    }

    // --- Menu Provider ---
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.marketblocks.small_shop");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SmallShopMenu(containerId, playerInventory, this);
    }

    // --- Getters for Handlers ---
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

    public IItemHandler getInputOnly() {
        return inputOnly;
    }

    public IItemHandler getOutputOnly() {
        return outputOnly;
    }


    // --- Core BlockEntity Methods ---
    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
            level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        }
    }

    public void updateNeighborCache() {
        inventoryManager.updateNeighborCache();
    }

    private boolean isChestIoExtensionEnabled() {
        return Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get();
    }

    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= MAX_PLAYER_DISTANCE_SQUARED;
    }

    // --- Owner System ---
    public void setOwner(Player player) {
        ownerManager.setOwner(player);
    }

    @SuppressWarnings("unused")
    public void addOwner(UUID id, String name) {
        ownerManager.addOwner(id, name);
    }

    @ApiStatus.Internal
    public void addOwnerClient(UUID id, String name) {
        ownerManager.addOwnerClient(id, name);
    }

    @SuppressWarnings("unused")
    public void removeOwner(UUID id) {
        ownerManager.removeOwner(id);
    }

    public Set<UUID> getOwners() {
        return ownerManager.getOwners();
    }

    public Map<UUID, String> getAdditionalOwners() {
        return ownerManager.getAdditionalOwners();
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        ownerManager.setAdditionalOwners(owners);
    }

    public boolean isOwner(Player player) {
        return ownerManager.isOwner(player);
    }

    public boolean isPrimaryOwner(Player player) {
        return ownerManager.isPrimaryOwner(player);
    }

    public void ensureOwner(Player player) {
        ownerManager.ensureOwner(player);
    }

    public UUID getOwnerId() {
        return ownerManager.getOwnerId();
    }

    public String getOwnerName() {
        return ownerManager.getOwnerName();
    }

    // --- Shop Settings ---
    public String getShopName() {
        return shopName;
    }

    public void setShopName(String name) {
        if (name.length() > MAX_SHOP_NAME_LENGTH) {
            name = name.substring(0, MAX_SHOP_NAME_LENGTH);
        }
        this.shopName = name;
        sync();
    }

    /**
     * Sets the shop name on the client side.
     * Client setters should ONLY update state, no side effects.
     */
    @ApiStatus.Internal
    public void setShopNameClient(String name) {
        this.shopName = name;
    }

    public boolean isEmitRedstone() {
        return emitRedstone;
    }

    public void setEmitRedstone(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
        sync();
    }

    /**
     * Sets the redstone emit flag on the client side.
     * Client setters should ONLY update state, no side effects.
     */
    @ApiStatus.Internal
    public void setEmitRedstoneClient(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
    }

    // --- Side Configuration ---
    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode) {
        SideMode oldMode = getMode(dir);
        sideModes.put(dir, mode);
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        sync();
        invalidateNeighbor(dir);
        if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
            lockAdjacentChest(dir);
        } else if (oldMode != SideMode.DISABLED) {
            unlockAdjacentChests();
        }
        updateNeighborCache();
    }

    /**
     * Sets the mode for a direction without triggering sync().
     * Used internally for batch updates to reduce network traffic.
     */
    public void setModeNoSync(Direction dir, SideMode mode) {
        SideMode oldMode = getMode(dir);
        sideModes.put(dir, mode);
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        invalidateNeighbor(dir);
        if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
            lockAdjacentChest(dir);
        } else if (oldMode != SideMode.DISABLED) {
            unlockAdjacentChests();
        }
    }

    /**
     * Batch update for shop settings to avoid multiple sync() calls.
     * Used by UpdateSettingsPacket to update all settings at once.
     */
    public void updateSettingsBatch(Direction left, SideMode leftMode,
                                    Direction right, SideMode rightMode,
                                    Direction bottom, SideMode bottomMode,
                                    Direction back, SideMode backMode,
                                    String name, boolean redstone) {
        setModeNoSync(left, leftMode);
        setModeNoSync(right, rightMode);
        setModeNoSync(bottom, bottomMode);
        setModeNoSync(back, backMode);
        setShopNameNoSync(name);
        setEmitRedstoneNoSync(redstone);
        updateNeighborCache();
        sync();
    }
    
    /**
     * Sets the shop name without triggering sync().
     * Used internally for batch updates.
     */
    public void setShopNameNoSync(String name) {
        if (name.length() > MAX_SHOP_NAME_LENGTH) {
            name = name.substring(0, MAX_SHOP_NAME_LENGTH);
        }
        this.shopName = name;
    }
    
    /**
     * Sets the redstone emit flag without triggering sync().
     * Used internally for batch updates.
     */
    public void setEmitRedstoneNoSync(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
    }

    /**
     * Sets the side mode on the client side.
     * Client setters should ONLY update state, no side effects.
     */
    @ApiStatus.Internal
    public void setModeClient(Direction dir, SideMode mode) {
        sideModes.put(dir, mode);
    }

    public SideMode getModeForSide(Direction side) {
        return getMode(side);
    }

    private void invalidateNeighbor(Direction dir) {
        if (level != null) {
            BlockPos neighbour = worldPosition.relative(dir);
            level.invalidateCapabilities(neighbour);
        }
    }

    public IItemHandler getValidNeighborHandler(Direction dir) {
        return inventoryManager.getValidNeighborHandler(dir);
    }

    // --- Offer System ---
    public void createOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        ItemStack normalizedPayment1 = payment1 == null ? ItemStack.EMPTY : payment1.copy();
        ItemStack normalizedPayment2 = payment2 == null ? ItemStack.EMPTY : payment2.copy();
        if (normalizedPayment1.isEmpty() && !normalizedPayment2.isEmpty()) {
            normalizedPayment1 = normalizedPayment2;
            normalizedPayment2 = ItemStack.EMPTY;
        }

        this.offerPayment1 = normalizedPayment1;
        this.offerPayment2 = normalizedPayment2;
        this.offerResult = result == null ? ItemStack.EMPTY : result.copy();
        this.hasOffer = true;
        sync();
        needsOfferRefresh = true;
    }

    public void clearOffer() {
        offerPayment1 = ItemStack.EMPTY;
        offerPayment2 = ItemStack.EMPTY;
        offerResult = ItemStack.EMPTY;
        this.hasOffer = false;
        this.offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        sync();
        needsOfferRefresh = true;
    }

    public boolean hasOffer() {
        return hasOffer;
    }

    /**
     * Sets the offer status on the client side.
     * Client setters should ONLY update state, no side effects.
     */
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

    /**
     * Counts matching payment items in payment slots.
     * 
     * @param target The target ItemStack to match (must not be null)
     * @return Total count of matching items, or 0 if target is null/empty
     */
    private int countMatchingPayment(ItemStack target) {
        if (target == null || target.isEmpty()) return 0;
        
        int total = 0;
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (stack != null && ItemStack.isSameItemSameComponents(stack, target)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private boolean hasEnoughPayment(ItemStack required) {
        return required.isEmpty() || countMatchingPayment(required) >= required.getCount();
    }

    public void updateOfferSlot() {
        updateOfferSlot(false);
    }

    private void updateOfferSlot(boolean checkNeighbors) {
        if (!hasOffer) {
            return;
        }

        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        if (canAfford() && hasResultItemInInput(checkNeighbors) && !isOutputFull() && hasOutputSpace(p1, p2)) {
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
            return true; // Free trade
        }

        if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
            int required = p1.getCount() + p2.getCount();
            return countMatchingPayment(p1) >= required;
        }

        return hasEnoughPayment(p1) && hasEnoughPayment(p2);
    }

    @SuppressWarnings("unused")
    public boolean hasResultItemInInput() {
        return hasResultItemInInput(false);
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
        ItemStack result = getOfferResult();
        if (result == null || result.isEmpty()) return false;

        int found = 0;
        // Check internal inventory first
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (stack != null && ItemStack.isSameItemSameComponents(stack, result)) {
                found += stack.getCount();
                if (found >= result.getCount()) {
                    return true;
                }
            }
        }

        // Check neighbor inventories if enabled
        if (checkNeighbors && isChestIoExtensionEnabled() && level != null) {
            for (Direction dir : DIRECTIONS) {
                if (getModeForSide(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour == null) continue;

                    for (int i = 0; i < neighbour.getSlots(); i++) {
                        ItemStack stack = neighbour.getStackInSlot(i);
                        if (stack != null && ItemStack.isSameItemSameComponents(stack, result)) {
                            found += stack.getCount();
                            if (found >= result.getCount()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public List<String> getTransactionLog() {
        return Collections.unmodifiableList(transactionLog);
    }

    private void addLogEntry(String entry) {
        transactionLog.addFirst(entry);
        if (transactionLog.size() > 10) {
            transactionLog.removeLast();
        }
    }

    public int getAnalogSignal(Direction readSide) {
        SideMode mode = getModeForSide(readSide);
        if (mode == SideMode.OUTPUT) {
            return calculateComparatorSignal(outputHandler);
        } else if (mode == SideMode.INPUT) {
            return calculateComparatorSignal(inputHandler);
        }
        return 0;
    }

    private int calculateComparatorSignal(IItemHandler handler) {
        if (handler == null) return 0;

        int totalSlots = handler.getSlots();
        if (totalSlots == 0) return 0;

        float fullness = 0.0F;
        boolean hasItem = false;

        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                fullness += (float) stack.getCount() / (float) Math.min(handler.getSlotLimit(i), stack.getMaxStackSize());
                hasItem = true;
            }
        }

        fullness /= (float) totalSlots;
        return Mth.floor(fullness * 14.0F) + (hasItem ? 1 : 0);
    }

    public void processPurchase() {
        processBulkPurchase(1);
    }

    /**
     * Executes the purchase logic for a specified maximum amount of trades.
     * This logic relies on pre-calculating the exact number of possible trades using three constraints:
     * 1) Affordability (based on matching items in the payment slots)
     * 2) In-stock limits (based on the output stack count in the input handler)
     * 3) Output space limits (based on space availability in the output handler)
     * Output space is simulated iteratively to ensure accurate evaluation of max stacking.
     */
    public int processBulkPurchase(int maxAmount) {
        if (maxAmount <= 0) return 0;

        if (isChestIoExtensionEnabled()) {
            updateNeighborCache();
            inventoryManager.pullFromInputChest(inputHandler);
        }

        if (!hasOffer) return 0;
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        ItemStack result = getOfferResult();
        if (result.isEmpty()) return 0;

        // 1. Calculate how many we can afford
        int affordable = Integer.MAX_VALUE;
        if (!p1.isEmpty()) {
            affordable = Math.min(affordable, countMatchingPayment(p1) / p1.getCount());
        }
        if (!p2.isEmpty()) {
            // Special handling if p1 and p2 are same item type
            if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
                int totalReqPerUnit = p1.getCount() + p2.getCount();
                affordable = countMatchingPayment(p1) / totalReqPerUnit;
            } else {
                affordable = Math.min(affordable, countMatchingPayment(p2) / p2.getCount());
            }
        }
        if (p1.isEmpty() && p2.isEmpty()) affordable = maxAmount; // Free

        // 2. Calculate how many we have in stock (Input)
        int inStock = countMatchingInput(result) / result.getCount();

        // 3. Calculate output space
        int actualAmount = Math.min(maxAmount, Math.min(affordable, inStock));
        if (actualAmount <= 0) return 0;

        // Verify Output Space for actualAmount
        // NOTE: We simulate insertions on a cloned handler to ensure accurate space calculation
        // when payment1 and payment2 might share slots. The testHandler is method-local and
        // will be garbage-collected automatically after this method exits.
        int validAmount = simulateOutputSpace(p1, p2, actualAmount);
        actualAmount = validAmount;

        if (actualAmount <= 0) {
            // Output full
            updateOutputFullness();
            return 0;
        }

        executeTrades(p1, p2, result, actualAmount);

        StringBuilder tradeLog = new StringBuilder();
        tradeLog.append("Sold ").append(actualAmount * result.getCount()).append("x ").append(result.getHoverName().getString());
        if (!p1.isEmpty() || !p2.isEmpty()) {
            tradeLog.append(" for ");
            if (!p1.isEmpty()) {
                tradeLog.append(actualAmount * p1.getCount()).append("x ").append(p1.getHoverName().getString());
            }
            if (!p2.isEmpty()) {
                if (!p1.isEmpty()) tradeLog.append(" and ");
                tradeLog.append(actualAmount * p2.getCount()).append("x ").append(p2.getHoverName().getString());
            }
        } else {
            tradeLog.append(" for free");
        }
        addLogEntry(tradeLog.toString());

        sync();
        triggerRedstonePulse();
        needsOfferRefresh = true;
        return actualAmount;
    }

    /**
     * Simulates how many transactions can fit in output inventory.
     * 
     * @param p1 First payment item (can be empty)
     * @param p2 Second payment item (can be empty)
     * @param maxTransactions Maximum number of transactions to simulate
     * @return Number of transactions that actually fit (may be less than maxTransactions)
     */
    private int simulateOutputSpace(ItemStack p1, ItemStack p2, int maxTransactions) {
        // Create temporary handler for simulation
        ItemStackHandler testHandler = new ItemStackHandler(outputHandler.getSlots());
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            testHandler.setStackInSlot(i, outputHandler.getStackInSlot(i).copy());
        }

        int validAmount = 0;
        for (int i = 0; i < maxTransactions; i++) {
            boolean fits = true;
            
            if (!p1.isEmpty()) {
                if (!ItemHandlerHelper.insertItem(testHandler, p1.copy(), false).isEmpty()) {
                    fits = false;
                }
            }
            
            if (fits && !p2.isEmpty()) {
                if (!ItemHandlerHelper.insertItem(testHandler, p2.copy(), false).isEmpty()) {
                    fits = false;
                }
            }
            
            if (fits) {
                validAmount++;
            } else {
                break; // No more space
            }
        }
        
        return validAmount;
    }

    /**
     * Counts how many items of the target type exist in input inventory and neighbor chests.
     * 
     * SAFETY: Creates defensive copies when reading from neighbor inventories to prevent
     * ConcurrentModificationException if neighbor inventory is modified during iteration.
     */
    private int countMatchingInput(ItemStack target) {
        if (target == null || target.isEmpty()) return 0;
        
        int found = 0;
        // Check internal inventory
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (stack != null && ItemStack.isSameItemSameComponents(stack, target)) {
                found += stack.getCount();
            }
        }
        // Check neighbor inventories (with defensive copies)
        if (isChestIoExtensionEnabled() && level != null) {
            for (Direction dir : DIRECTIONS) {
                if (getModeForSide(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour != null) {
                        for (int i = 0; i < neighbour.getSlots(); i++) {
                            ItemStack stack = neighbour.getStackInSlot(i);
                            if (stack != null && !stack.isEmpty()) {
                                // Defensive copy to prevent concurrent modification issues
                                ItemStack safeCopy = stack.copy();
                                if (ItemStack.isSameItemSameComponents(safeCopy, target)) {
                                    found += safeCopy.getCount();
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    private boolean isReadyToPurchase() {
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        return hasOffer && canAfford() && hasResultItemInInput(false) && !isOutputFull() && hasOutputSpace(p1, p2);
    }

    private void executeTrades(ItemStack p1, ItemStack p2, ItemStack result, int tradeCount) {
        if (tradeCount <= 0) {
            return;
        }

        ItemStack totalP1 = multiplyStackForTrades(p1, tradeCount);
        ItemStack totalP2 = multiplyStackForTrades(p2, tradeCount);
        ItemStack totalResult = multiplyStackForTrades(result, tradeCount);

        if (!totalP1.isEmpty()) {
            removePayment(totalP1);
        }
        if (!totalP2.isEmpty()) {
            removePayment(totalP2);
        }
        if (!totalResult.isEmpty()) {
            removeFromInput(totalResult);
        }

        addToOutputBatched(p1, tradeCount);
        addToOutputBatched(p2, tradeCount);
    }

    private ItemStack multiplyStackForTrades(ItemStack stack, int tradeCount) {
        if (stack == null || stack.isEmpty() || tradeCount <= 0) {
            return ItemStack.EMPTY;
        }
        long total = (long) stack.getCount() * tradeCount;
        if (total <= 0L) {
            return ItemStack.EMPTY;
        }
        ItemStack multiplied = stack.copy();
        multiplied.setCount((int) Math.min(Integer.MAX_VALUE, total));
        return multiplied;
    }

    private void addToOutputBatched(ItemStack stack, int times) {
        if (stack == null || stack.isEmpty() || times <= 0) {
            return;
        }

        long total = (long) stack.getCount() * times;
        if (total <= 0L) {
            return;
        }

        int maxStack = stack.getMaxStackSize();
        while (total > 0L) {
            ItemStack chunk = stack.copy();
            chunk.setCount((int) Math.min(total, maxStack));
            addToOutput(chunk);
            total -= chunk.getCount();
        }
    }

    private void triggerRedstonePulse() {
        if (level == null || level.isClientSide || !emitRedstone) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof BaseShopBlock block) {
            level.setBlock(worldPosition, state.setValue(BaseShopBlock.POWERED, true), 3);
            level.updateNeighborsAt(worldPosition, block);
            level.scheduleTick(worldPosition, block, 20);
        }
    }

    private void removeFromInput(ItemStack toRemove) {
        if (toRemove == null || toRemove.isEmpty()) return;
        
        int remaining = toRemove.getCount();
        if (remaining <= 0) return;

        // First, remove from internal inventory
        for (int i = 0; i < inputHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (stack != null && ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                inputHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }

        // Then, remove from connected neighbor inventories
        if (isChestIoExtensionEnabled() && remaining > 0 && level != null && !level.isClientSide) {
            for (Direction dir : DIRECTIONS) {
                if (getModeForSide(dir) != SideMode.INPUT) continue;

                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;

                for (int i = 0; i < neighbour.getSlots() && remaining > 0; i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    if (stack != null && ItemStack.isSameItemSameComponents(stack, toRemove)) {
                        int toTake = Math.min(remaining, stack.getCount());
                        neighbour.extractItem(i, toTake, false);
                        remaining -= toTake;
                    }
                }
                if (remaining <= 0) break;
            }
        }
    }

    private void removePayment(ItemStack required) {
        if (required == null || required.isEmpty()) return;
        
        int remaining = required.getCount();
        if (remaining <= 0) {
            return;
        }

        // Iterate through both payment slots and remove the required amount
        for (int i = 0; i < paymentHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (stack != null && ItemStack.isSameItemSameComponents(stack, required)) {
                int toTake = Math.min(remaining, stack.getCount());
                paymentHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }
    }

    private boolean hasOutputSpace(ItemStack... stacks) {
        ItemStackHandler testHandler = new ItemStackHandler(outputHandler.getSlots());
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            testHandler.setStackInSlot(i, outputHandler.getStackInSlot(i).copy());
        }

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            ItemStack remaining = ItemHandlerHelper.insertItem(testHandler, stack.copy(), false);
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void addToOutput(ItemStack toAdd) {
        ItemHandlerHelper.insertItem(outputHandler, toAdd, false);
    }

    public boolean isOutputAlmostFull() {
        return outputAlmostFull;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOutputFull() {
        return outputFull;
    }

    public boolean isOutputSpaceMissing() {
        if (!hasResultItemInInput(false)) {
            return false;
        }
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        return !hasOutputSpace(p1, p2);
    }

    private void updateOutputFullness() {
        if (level == null || level.isClientSide) {
            return;
        }

        int total = 0;
        int filled = 0;

        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemStack stack = outputHandler.getStackInSlot(i);
            int limit = outputHandler.getSlotLimit(i);
            if (!stack.isEmpty()) {
                limit = Math.min(limit, stack.getMaxStackSize());
            }
            total += limit;
            filled += stack.getCount();
        }

        if (isChestIoExtensionEnabled()) {
            for (Direction dir : DIRECTIONS) {
                if (getModeForSide(dir) != SideMode.OUTPUT) continue;
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;
                for (int i = 0; i < neighbour.getSlots(); i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    int limit = neighbour.getSlotLimit(i);
                    if (!stack.isEmpty()) {
                        limit = Math.min(limit, stack.getMaxStackSize());
                    }
                    total += limit;
                    filled += stack.getCount();
                }
            }
        }

        boolean newOutputFull = total > 0 && filled >= total;

        boolean newOutputAlmostFull = false;
        if (Config.ENABLE_OUTPUT_WARNING.get()) {
            int threshold = Config.OUTPUT_WARNING_PERCENT.get();
            newOutputAlmostFull = total > 0 && (filled * 100 >= total * threshold);
        }

        if (newOutputFull != outputFull || newOutputAlmostFull != outputAlmostFull) {
            outputFull = newOutputFull;
            outputAlmostFull = newOutputAlmostFull;
            sync();
        }
    }

    public boolean isOfferAvailable() {
        return hasOffer && !offerHandler.getStackInSlot(0).isEmpty();
    }

    public ContainerData createMenuFlags(Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int flags = 0;
                    if (hasOffer()) flags |= HAS_OFFER_FLAG;
                    if (isOfferAvailable()) flags |= OFFER_AVAILABLE_FLAG;
                    if (isOwner(player)) flags |= OWNER_FLAG;
                    if (isPrimaryOwner(player)) flags |= PRIMARY_OWNER_FLAG;
                    return flags;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                // Not needed on client side
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    // --- Item and Chest Handling ---
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
        if (!hasOffer) {
            dropItems(level, pos, offerHandler);
        }
    }

    public void unlockAdjacentChests() {
        if (level == null || !isChestIoExtensionEnabled()) return;
        for (Direction dir : DIRECTIONS) {
            BlockPos neighbour = worldPosition.relative(dir);
            level.invalidateCapabilities(neighbour);
        }
    }

    private void lockAdjacentChest(Direction dir) {
        if (level == null || !isChestIoExtensionEnabled()) return;
        BlockPos neighbour = worldPosition.relative(dir);
        level.invalidateCapabilities(neighbour);
    }

    public void lockAdjacentChests() {
        if (level == null || !isChestIoExtensionEnabled()) return;
        for (Direction dir : DIRECTIONS) {
            if (getModeForSide(dir) == SideMode.INPUT || getModeForSide(dir) == SideMode.OUTPUT) {
                lockAdjacentChest(dir);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos pos, BlockState state, SmallShopBlockEntity be) {
        if (level.isClientSide) {
            return;
        }

        be.tickCounter++;
        if (be.tickCounter % 20 == 0 && state.is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())) {
            SmallShopBlockNeu.ensureTopBlock(level, pos);
        }
        boolean chestExtensionEnabled = Config.ENABLE_CHEST_IO_EXTENSION_EXPERIMENTAL.get();

        int offerInterval = Config.OFFER_UPDATE_INTERVAL.get();
        if (offerInterval > 0 && be.tickCounter % offerInterval == 0) {
            if (chestExtensionEnabled) {
                be.updateNeighborCache();
            }
            if (be.needsOfferRefresh) {
                be.updateOfferSlot(true);
                be.needsOfferRefresh = false;
                be.sync(); // Sync after offer refresh
            }
            be.updateOutputFullness(); // Moved inside interval
        }

        int chestInterval = Config.CHEST_IO_INTERVAL.get();
        if (chestExtensionEnabled && chestInterval > 0 && be.tickCounter % chestInterval == 0) {
            be.updateNeighborCache();
            be.inventoryManager.pullFromInputChest(be.inputHandler);
            be.inventoryManager.pushToOutputChest(be.outputHandler);
        }
    }

    // --- NBT HANDLING ---

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unlockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        unlockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && getBlockState().is(RegistriesInit.SMALL_SHOP_BLOCK_NEU.get())) {
            SmallShopBlockNeu.ensureTopBlock(level, worldPosition);
        }
        updateNeighborCache();
        
        // Perform chunk-dependent operations here instead of in loadAdditional()
        // At this point, the level is guaranteed to be set and neighbors are accessible
        lockAdjacentChests();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadHandlers(tag, registries);
        ownerManager.load(tag);
        loadOffer(tag, registries);
        loadSettings(tag);
        loadSideModes(tag);
        outputAlmostFull = tag.getBoolean(NBT_OUTPUT_WARNING);
        outputFull = tag.getBoolean(NBT_OUTPUT_FULL);

        transactionLog.clear();
        if (tag.contains("TransactionLog", 9)) {
            ListTag list = tag.getList("TransactionLog", 8); // 8 is StringTag
            // Validate transaction log size (max 10 entries)
            int maxEntries = Math.min(list.size(), 10);
            if (list.size() > 10) {
                MarketBlocks.LOGGER.warn("Transaction log exceeds max size at {}, truncating from {} to 10 entries", 
                        worldPosition, list.size());
            }
            for (int i = 0; i < maxEntries; i++) {
                String entry = list.getString(i);
                // Validate individual entry length (max 256 chars)
                if (entry.length() > 256) {
                    entry = entry.substring(0, 256);
                }
                transactionLog.add(entry);
            }
        }

        // Moved to onLoad(): lockAdjacentChests() and invalidateCapabilities()
        // This ensures level is not null and neighbor chunks are loaded
        tickCounter = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveHandlers(tag, registries);
        ownerManager.save(tag);
        saveOffer(tag, registries);
        saveSettings(tag);
        saveSideModes(tag);
        tag.putBoolean(NBT_OUTPUT_WARNING, outputAlmostFull);
        tag.putBoolean(NBT_OUTPUT_FULL, outputFull);

        ListTag logList = new ListTag();
        for (String logEntry : transactionLog) {
            logList.add(net.minecraft.nbt.StringTag.valueOf(logEntry));
        }
        tag.put("TransactionLog", logList);
    }

    private void loadHandlers(CompoundTag tag, HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> {
            if (tag.contains(name)) {
                handler.deserializeNBT(registries, tag.getCompound(name));
            }
        });
    }

    private void saveHandlers(CompoundTag tag, HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> tag.put(name, handler.serializeNBT(registries)));
    }

    private void loadOffer(CompoundTag tag, HolderLookup.Provider registries) {
        offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1));
        offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2));
        offerResult = ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT));
        hasOffer = tag.getBoolean(NBT_HAS_OFFER);
    }

    private void saveOffer(CompoundTag tag, HolderLookup.Provider registries) {
        if (!offerPayment1.isEmpty()) tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
        if (!offerPayment2.isEmpty()) tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
        if (!offerResult.isEmpty()) tag.put(KEY_RESULT, offerResult.save(registries));
        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
    }

    private void loadSettings(CompoundTag tag) {
        String name = tag.getString(NBT_SHOP_NAME);
        // Validate shop name length
        if (name.length() > MAX_SHOP_NAME_LENGTH) {
            MarketBlocks.LOGGER.warn("Shop name exceeds max length at {}, truncating from {} to {} chars", 
                    worldPosition, name.length(), MAX_SHOP_NAME_LENGTH);
            name = name.substring(0, MAX_SHOP_NAME_LENGTH);
        }
        this.shopName = name;
        this.emitRedstone = tag.getBoolean(NBT_EMIT_REDSTONE);
    }

    private void saveSettings(CompoundTag tag) {
        tag.putString(NBT_SHOP_NAME, shopName);
        tag.putBoolean(NBT_EMIT_REDSTONE, emitRedstone);
    }

    private void loadSideModes(CompoundTag tag) {
        sideModes.clear();
        for (Direction dir : DIRECTIONS) {
            sideModes.put(dir, SideMode.DISABLED);
        }
        ListTag sideList = tag.getList(NBT_SIDE_MODES, 10);
        for (int i = 0; i < sideList.size(); i++) {
            CompoundTag sideTag = sideList.getCompound(i);
            try {
                Direction dir = Direction.valueOf(sideTag.getString(NBT_DIRECTION));
                SideMode mode = SideMode.valueOf(sideTag.getString(NBT_MODE));
                sideModes.put(dir, mode);
            } catch (IllegalArgumentException e) {
                MarketBlocks.LOGGER.warn("Invalid side mode data for direction/mode at position {}: {}",
                        worldPosition, e.getMessage());
                // Fallback: Direction already set to DISABLED above
            }
        }
    }

    private void saveSideModes(CompoundTag tag) {
        ListTag sideList = new ListTag();
        for (Map.Entry<Direction, SideMode> entry : sideModes.entrySet()) {
            CompoundTag sideTag = new CompoundTag();
            sideTag.putString(NBT_DIRECTION, entry.getKey().name());
            sideTag.putString(NBT_MODE, entry.getValue().name());
            sideList.add(sideTag);
        }
        tag.put(NBT_SIDE_MODES, sideList);
    }


    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // SECURITY: Only send client-relevant data, not full inventory contents
        CompoundTag tag = new CompoundTag();
        
        // Offer data (needed for rendering)
        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
        if (hasOffer) {
            if (!offerPayment1.isEmpty()) tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
            if (!offerPayment2.isEmpty()) tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
            if (!offerResult.isEmpty()) tag.put(KEY_RESULT, offerResult.save(registries));
        }
        
        // Shop settings (needed for UI)
        tag.putString(NBT_SHOP_NAME, shopName);
        tag.putBoolean(NBT_EMIT_REDSTONE, emitRedstone);
        
        // Owner info (needed for permissions check)
        ownerManager.save(tag);
        
        // Side modes (needed for rendering/capabilities)
        saveSideModes(tag);
        
        // Output status flags (needed for UI indicators)
        tag.putBoolean(NBT_OUTPUT_WARNING, outputAlmostFull);
        tag.putBoolean(NBT_OUTPUT_FULL, outputFull);
        
        // Note: Inventory handlers (input/output/payment) are NOT sent to clients
        // for security reasons - they contain owner's items
        
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        // CRITICAL: Only load the data that was sent via getUpdateTag()
        // Do NOT call loadAdditional() as it expects full NBT data!
        
        // Offer data
        offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1));
        offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2));
        offerResult = ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT));
        hasOffer = tag.getBoolean(NBT_HAS_OFFER);
        
        // Shop settings
        shopName = tag.getString(NBT_SHOP_NAME);
        emitRedstone = tag.getBoolean(NBT_EMIT_REDSTONE);
        
        // Owner info
        ownerManager.load(tag);
        
        // Side modes
        loadSideModes(tag);
        
        // Output status
        outputAlmostFull = tag.getBoolean(NBT_OUTPUT_WARNING);
        outputFull = tag.getBoolean(NBT_OUTPUT_FULL);
        
        // Update client-side state
        updateOfferSlot();
    }
}
