package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.data.lang.ModLang;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // NBT Keys
    private static final String NBT_OWNER_ID = "OwnerId";
    private static final String NBT_OWNER_NAME = "OwnerName";
    private static final String NBT_ADDITIONAL_OWNERS = "AdditionalOwners";
    private static final String NBT_ADDITIONAL_OWNER_ID = "Id";
    private static final String NBT_ADDITIONAL_OWNER_NAME = "Name";
    private static final String NBT_HAS_OFFER = "HasOffer";
    private static final String NBT_SHOP_NAME = "ShopName";
    private static final String NBT_EMIT_REDSTONE = "EmitRedstone";
    private static final String NBT_SIDE_MODES = "SideModes";
    private static final String NBT_DIRECTION = "Direction";
    private static final String NBT_MODE = "Mode";
    private static final String KEY_PAYMENT1 = "OfferPayment1";
    private static final String KEY_PAYMENT2 = "OfferPayment2";
    private static final String KEY_RESULT = "OfferResult";

    // Inventory Handler Names
    private static final String HANDLER_INPUT = "InputInventory";
    private static final String HANDLER_OUTPUT = "OutputInventory";
    private static final String HANDLER_PAYMENT = "PaymentSlots";
    private static final String HANDLER_OFFER = "OfferSlot";

    // Menu Flags
    public static final int HAS_OFFER_FLAG = 1;
    public static final int OFFER_AVAILABLE_FLAG = 2;
    public static final int OWNER_FLAG = 4;

    // Offer System
    private ItemStack offerPayment1 = ItemStack.EMPTY;
    private ItemStack offerPayment2 = ItemStack.EMPTY;
    private ItemStack offerResult = ItemStack.EMPTY;
    private boolean hasOffer = false;

    // Owner System
    private UUID ownerId = null;
    private String ownerName = "";
    private final Map<UUID, String> additionalOwners = new HashMap<>();

    // Shop Settings
    private String shopName = "";
    private boolean emitRedstone = false;

    // Side Configuration
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);

    // --- Inventories ---
    private final ItemStackHandler inputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            needsOfferRefresh = true;
        }
    };

    private final ItemStackHandler outputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler paymentHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            needsOfferRefresh = true;
            updateOfferSlot();
            sync();
        }
    };

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            // The simulation part is tricky with this custom logic.
            // For now, we simulate by checking how many trades are possible and returning that.
            // This is not a perfect simulation but better than the default.
            if (simulate) {
                if (!isReadyToPurchase()) {
                    return ItemStack.EMPTY;
                }
                ItemStack resultPerTrade = getOfferResult();
                if (resultPerTrade.isEmpty()) {
                    return ItemStack.EMPTY;
                }

                int maxTrades = 0;
                // A full simulation would require cloning the inventories and running the logic,
                // which is too complex for this context. We'll simulate based on one check.
                if (isReadyToPurchase()) {
                    // This is a simplified check. A real simulation would be much more involved.
                    // We'll assume we can perform as many as requested, up to the stack size.
                    maxTrades = amount / resultPerTrade.getCount();
                }

                if (maxTrades == 0) return ItemStack.EMPTY;

                ItemStack simulatedStack = resultPerTrade.copy();
                simulatedStack.setCount(Math.min(amount, maxTrades * resultPerTrade.getCount()));
                return simulatedStack;
            }

            // --- Execution part (not simulating) ---
            int itemsPerTrade = getOfferResult().getCount();
            if (itemsPerTrade <= 0) {
                return ItemStack.EMPTY; // Avoid division by zero
            }

            int tradesToAttempt = amount / itemsPerTrade;
            if (tradesToAttempt == 0 && amount > 0) {
                tradesToAttempt = 1; // Allow taking single items even if result stack is > 1
            }

            int successfulTrades = 0;
            for (int i = 0; i < tradesToAttempt; i++) {
                // We must check readiness *inside* the loop, as each trade changes the state.
                if (isReadyToPurchase()) {
                    executeTrade(); // This deducts payment and stock
                    successfulTrades++;
                } else {
                    // Stop if we can no longer afford the trade or stock is out
                    break;
                }
            }

            if (successfulTrades > 0) {
                // Manually create the stack of resulting items
                ItemStack returnedStack = getOfferResult().copy();
                returnedStack.setCount(successfulTrades * itemsPerTrade);

                // Manually shrink the stack in the offer slot
                ItemStack stackInSlot = this.getStackInSlot(slot);
                stackInSlot.shrink(returnedStack.getCount());

                // Sync changes and update state
                sync();
                triggerRedstonePulse(); // Pulse once for the whole transaction
                needsOfferRefresh = true; // Refresh the offer slot display
                updateOfferSlot(); // Immediately update the slot

                return returnedStack;
            }

            return ItemStack.EMPTY;
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
    private boolean needsOfferRefresh = false;


    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
        for (Direction dir : Direction.values()) {
            sideModes.put(dir, SideMode.DISABLED);
        }
    }

    // --- Sided Wrapper for Item Handlers ---

    /**
     * A wrapper for IItemHandler that restricts insertion or extraction.
     * This is used to expose either insert-only or extract-only capabilities for the sides.
     *
     * @param backing     The backing item handler.
     * @param extractOnly If true, only extraction is allowed. If false, only insertion is allowed.
     */
    record SidedWrapper(IItemHandler backing, boolean extractOnly) implements IItemHandler {
        @Override
        public int getSlots() {
            return backing.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return backing.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (extractOnly) {
                return stack; // Cannot insert into an extract-only wrapper
            }
            return backing.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
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
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !extractOnly && backing.isItemValid(slot, stack);
        }
    }

    // --- Menu Provider ---
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(ModLang.CONTAINER_SMALL_SHOP);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new SmallShopOffersMenu(containerId, playerInventory, this);
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
    private void markDirty() {
        setChanged();
    }

    /**
     * Invalidates the capabilities of this block, forcing a re-evaluation by neighbors.
     */
    public void invalidateCaps() {
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    /**
     * Marks the block entity as changed and sends an update packet to the client.
     */
    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Checks if a player is close enough to interact with this block entity.
     * Used by the menu to determine if it should close.
     */
    public boolean stillValid(@NotNull Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= MAX_PLAYER_DISTANCE_SQUARED;
    }

    // --- Owner System ---

    /**
     * Sets the primary owner of this shop.
     */
    public void setOwner(final @NotNull Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    /**
     * Adds a player to the list of additional owners (server-side).
     */
    public void addOwner(final @NotNull UUID id, final @NotNull String name) {
        additionalOwners.put(id, name);
        sync();
    }

    /**
     * Adds a player to the list of additional owners (client-side prediction).
     */
    public void addOwnerClient(final @NotNull UUID id, final @NotNull String name) {
        additionalOwners.put(id, name);
    }

    /**
     * Removes a player from the list of additional owners.
     */
    public void removeOwner(final @NotNull UUID id) {
        if (additionalOwners.remove(id) != null) {
            sync();
        }
    }

    /**
     * Gets a set of all UUIDs that are owners of this shop (primary + additional).
     */
    public @NotNull Set<UUID> getOwners() {
        Set<UUID> owners = new HashSet<>(additionalOwners.keySet());
        if (ownerId != null) {
            owners.add(ownerId);
        }
        return owners;
    }

    /**
     * Gets the map of additional owners (UUID -> Name).
     */
    public @NotNull Map<UUID, String> getAdditionalOwners() {
        return Collections.unmodifiableMap(additionalOwners);
    }

    /**
     * Replaces the entire list of additional owners.
     */
    public void setAdditionalOwners(final @NotNull Map<UUID, String> owners) {
        additionalOwners.clear();
        additionalOwners.putAll(owners);
        sync();
    }

    /**
     * Checks if the given player is an owner of this shop.
     */
    public boolean isOwner(final @NotNull Player player) {
        final UUID id = player.getUUID();
        return (ownerId != null && ownerId.equals(id)) || additionalOwners.containsKey(id);
    }

    /**
     * Ensures that the block entity has an owner, setting it to the given player if not.
     * This is a fallback for cases where a shop might be placed by a non-player entity.
     */
    public void ensureOwner(final @NotNull Player player) {
        if (player.level().isClientSide() || ownerId != null) {
            return;
        }
            setOwner(player);
    }

    public @Nullable UUID getOwnerId() {
        return ownerId;
    }

    public @NotNull String getOwnerName() {
        return ownerName;
    }

    // --- Shop Settings ---
    public @NotNull String getShopName() {
        return shopName;
    }

    /**
     * Sets the custom name for the shop.
     */
    public void setShopName(final @NotNull String name) {
        if (name.length() > MAX_SHOP_NAME_LENGTH) {
            this.shopName = name.substring(0, MAX_SHOP_NAME_LENGTH);
        } else {
            this.shopName = name;
        }
        sync();
    }

    /**
     * Sets the shop name on the client for predictive updates.
     */
    public void setShopNameClient(final @NotNull String name) {
        this.shopName = name;
    }

    public boolean isEmitRedstone() {
        return emitRedstone;
    }

    /**
     * Sets whether the shop should emit a redstone pulse on purchase.
     */
    public void setEmitRedstone(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
        sync();
    }

    /**
     * Sets the redstone setting on the client for predictive updates.
     */
    public void setEmitRedstoneClient(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
    }

    // --- Side Configuration ---

    /**
     * Gets the I/O mode for a given side.
     */
    public @NotNull SideMode getMode(final @NotNull Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    /**
     * Sets the I/O mode for a given side and triggers capability invalidation.
     */
    public void setMode(final @NotNull Direction dir, final @NotNull SideMode mode) {
        final SideMode oldMode = getMode(dir);
        if (oldMode == mode) return;

        sideModes.put(dir, mode);
        markDirty();
        invalidateCaps();
        sync();
        invalidateNeighbor(dir);
        if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
            lockAdjacentChest(dir);
        } else if (oldMode != SideMode.DISABLED) {
            unlockAdjacentChests();
        }
    }

    /**
     * Sets the side mode on the client for predictive updates.
     */
    public void setModeClient(final @NotNull Direction dir, final @NotNull SideMode mode) {
        sideModes.put(dir, mode);
    }

    public @NotNull SideMode getModeForSide(final @NotNull Direction side) {
        return getMode(side);
    }

    /**
     * Invalidates the capabilities of the block adjacent to the given side.
     */
    private void invalidateNeighbor(final @NotNull Direction dir) {
        if (level != null) {
            level.invalidateCapabilities(worldPosition.relative(dir));
        }
    }

    /**
     * Gets the item handler of a valid, unlocked neighbor on a given side.
     *
     * @return The neighbor's IItemHandler, or null if no valid handler exists.
     */
    @Nullable
    private IItemHandler getValidNeighborHandler(final @NotNull Direction dir) {
        if (level == null) return null;
        final BlockPos neighbourPos = worldPosition.relative(dir);
        IItemHandler neighbour = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, dir.getOpposite());
        if (neighbour == null) {
            neighbour = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, null);
        }

        if (neighbour instanceof LockedChestWrapper locked) {
            // If the neighbor is a locked chest, only allow access if it belongs to one of this shop's owners.
            if (locked.getOwnerId() != null && getOwners().contains(locked.getOwnerId())) {
                return locked.getDelegate();
            } else {
                return null;
            }
        }
        return neighbour;
    }

    /**
     * Transfers all possible items from one item handler to another.
     */
    private void transferItems(final @NotNull IItemHandler from, final @NotNull IItemHandler to) {
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stackInSlot = from.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            ItemStack remainderSim = ItemHandlerHelper.insertItem(to, stackInSlot.copy(), true);
            int transferable = stackInSlot.getCount() - remainderSim.getCount();
            if (transferable > 0) {
                ItemStack extracted = from.extractItem(i, transferable, false);
                ItemStack leftover = ItemHandlerHelper.insertItem(to, extracted, false);
                if (!leftover.isEmpty()) {
                    // If some items couldn't be inserted, put them back.
                    from.insertItem(i, leftover, false);
                }
            }
        }
    }

    // --- Offer System ---

    /**
     * Sets the shop's trade offer.
     */
    public void createOffer(final @NotNull ItemStack payment1, final @NotNull ItemStack payment2, final @NotNull ItemStack result) {
        this.offerPayment1 = payment1.copy();
        this.offerPayment2 = payment2.copy();
        this.offerResult = result.copy();
        this.hasOffer = true;
        sync();
        needsOfferRefresh = true;
    }

    /**
     * Clears the shop's trade offer and empties the result slot.
     */
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
     * Sets the offer status on the client.
     */
    public void setHasOfferClient(boolean hasOffer) {
        this.hasOffer = hasOffer;
    }

    public @NotNull ItemStack getOfferPayment1() {
        return offerPayment1;
    }

    public @NotNull ItemStack getOfferPayment2() {
        return offerPayment2;
    }

    public @NotNull ItemStack getOfferResult() {
        return offerResult;
    }

    /**
     * Counts the total number of items matching the target in the payment slots.
     */
    private int countMatchingPayment(final @NotNull ItemStack target) {
        int total = 0;
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /**
     * Checks if the payment slots contain enough items for the given requirement.
     */
    private boolean hasEnoughPayment(final @NotNull ItemStack required) {
        return required.isEmpty() || countMatchingPayment(required) >= required.getCount();
    }

    /**
     * Updates the result slot based on whether the trade can be performed.
     * If the trade is possible, the result item is shown; otherwise, the slot is cleared.
     */
    public void updateOfferSlot() {
        if (!hasOffer) {
            return;
        }

        if (canAfford() && hasResultItemInInput(false)) {
            if (offerHandler.getStackInSlot(0).isEmpty()) {
                offerHandler.setStackInSlot(0, getOfferResult().copy());
            }
        } else {
            offerHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    /**
     * Checks if the items in the payment slots are sufficient for the current offer.
     */
    public boolean canAfford() {
        final ItemStack p1 = getOfferPayment1();
        final ItemStack p2 = getOfferPayment2();
        if (p1.isEmpty() && p2.isEmpty()) {
            return true; // Free trade
        }

        // Handle cases where both payments are the same item type
        if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
            int required = p1.getCount() + p2.getCount();
            return countMatchingPayment(p1) >= required;
        }

        return hasEnoughPayment(p1) && hasEnoughPayment(p2);
    }

    public boolean hasResultItemInInput() {
        return hasResultItemInInput(false);
    }

    /**
     * Checks if the shop has enough items in its input storage to provide the trade result.
     *
     * @param checkNeighbors If true, also checks connected external inventories.
     */
    public boolean hasResultItemInInput(boolean checkNeighbors) {
        final ItemStack result = getOfferResult();
        if (result.isEmpty()) return false;

        int found = 0;
        // Check internal inventory first
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, result)) {
                found += stack.getCount();
                if (found >= result.getCount()) {
                    return true;
                }
            }
        }

        // Check neighbor inventories if enabled
        if (checkNeighbors && level != null) {
            for (Direction dir : Direction.values()) {
                if (getModeForSide(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour == null) continue;

                    for (int i = 0; i < neighbour.getSlots(); i++) {
                        ItemStack stack = neighbour.getStackInSlot(i);
                        if (ItemStack.isSameItemSameComponents(stack, result)) {
                            found += stack.getCount();
                            if (found >= result.getCount()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return found >= result.getCount();
    }

    /**
     * The main logic for when a player purchases an item from the result slot.
     */
    private void processPurchase() {
        pullFromInputChest();
        if (!isReadyToPurchase()) {
            return;
        }

        executeTrade();

        sync();
        triggerRedstonePulse();
        needsOfferRefresh = true;
    }

    /**
     * Checks if all conditions for a trade are met.
     */
    private boolean isReadyToPurchase() {
        return hasOffer && canAfford() && hasResultItemInInput(false);
    }

    /**
     * Executes the item transfer for a trade: removes payment, removes result from input, and moves payment to output.
     */
    private void executeTrade() {
        final ItemStack p1 = getOfferPayment1();
        final ItemStack p2 = getOfferPayment2();
        final ItemStack result = getOfferResult();

        if (!p1.isEmpty()) removePayment(p1);
        if (!p2.isEmpty()) removePayment(p2);

        removeFromInput(result);

        if (!p1.isEmpty()) addToOutput(p1.copy());
        if (!p2.isEmpty()) addToOutput(p2.copy());
    }

    /**
     * Emits a short redstone pulse if the setting is enabled.
     */
    private void triggerRedstonePulse() {
        if (level == null || level.isClientSide || !emitRedstone) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof SmallShopBlock block) {
            level.setBlock(worldPosition, state.setValue(SmallShopBlock.POWERED, true), 3);
            level.updateNeighborsAt(worldPosition, block);
            level.scheduleTick(worldPosition, block, 2);
        }
    }

    /**
     * Removes the specified items from the input storage (internal and external).
     */
    private void removeFromInput(final @NotNull ItemStack toRemove) {
        int remaining = toRemove.getCount();
        if (remaining <= 0) return;

        // First, remove from internal inventory
        for (int i = 0; i < inputHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                int toTake = Math.min(remaining, stack.getCount());
                inputHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }

        // Then, remove from connected neighbor inventories
        if (remaining > 0 && level != null && !level.isClientSide) {
            for (Direction dir : Direction.values()) {
                if (getModeForSide(dir) != SideMode.INPUT) continue;

                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour == null) continue;

                for (int i = 0; i < neighbour.getSlots() && remaining > 0; i++) {
                    ItemStack stack = neighbour.getStackInSlot(i);
                    if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                        int toTake = Math.min(remaining, stack.getCount());
                        neighbour.extractItem(i, toTake, false);
                        remaining -= toTake;
                    }
                }
                if (remaining <= 0) break;
            }
        }
    }

    /**
     * Removes the specified items from the payment slots.
     */
    private void removePayment(final @NotNull ItemStack required) {
        int remaining = required.getCount();
        if (required.isEmpty() || remaining <= 0) {
            return;
        }

        // Iterate through both payment slots and remove the required amount
        for (int i = 0; i < paymentHandler.getSlots() && remaining > 0; i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toTake = Math.min(remaining, stack.getCount());
                paymentHandler.extractItem(i, toTake, false);
                remaining -= toTake;
            }
        }
    }

    /**
     * Adds an item stack to the output inventory, dropping it in the world if it doesn't fit.
     */
    private void addToOutput(final @NotNull ItemStack toAdd) {
        final ItemStack remaining = ItemHandlerHelper.insertItem(outputHandler, toAdd, false);

        if (!remaining.isEmpty() && level != null && !level.isClientSide) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1.0, worldPosition.getZ(), remaining);
        }
    }

    /**
     * Checks if the current offer is available for purchase (i.e., has stock).
     */
    public boolean isOfferAvailable() {
        return hasOffer && hasResultItemInInput(false);
    }

    /**
     * Creates a ContainerData object to sync boolean flags to the client menu.
     */
    public ContainerData createMenuFlags(final @NotNull Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int flags = 0;
                    if (hasOffer()) flags |= HAS_OFFER_FLAG;
                    if (isOfferAvailable()) flags |= OFFER_AVAILABLE_FLAG;
                    if (isOwner(player)) flags |= OWNER_FLAG;
                    return flags;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                // Not needed on client side, server is the source of truth.
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    // --- Item and Chest Handling ---

    /**
     * Helper to drop all items from a given item handler into the world.
     */
    private void dropItems(final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    /**
     * Drops all contents of all internal inventories. Called when the block is broken.
     */
    public void dropContents(final @NotNull Level level, final @NotNull BlockPos pos) {
        handlerMap.values().forEach(handler -> dropItems(level, pos, handler));
    }

    /**
     * Invalidates capabilities of all adjacent blocks to force them to re-evaluate this shop.
     * Used when unlocking chests after the shop is broken.
     */
    public void unlockAdjacentChests() {
        if (level == null) return;
        for (Direction dir : Direction.values()) {
            level.invalidateCapabilities(worldPosition.relative(dir));
        }
    }

    /**
     * Invalidates capabilities of a specific adjacent block. Used when a side mode is changed.
     */
    private void lockAdjacentChest(final @NotNull Direction dir) {
        if (level == null) return;
        level.invalidateCapabilities(worldPosition.relative(dir));
    }

    /**
     * Invalidates capabilities of all adjacent blocks that are configured for I/O.
     * Used when the owner is set.
     */
    public void lockAdjacentChests() {
        if (level == null) return;
        for (Direction dir : Direction.values()) {
            if (getModeForSide(dir) == SideMode.INPUT || getModeForSide(dir) == SideMode.OUTPUT) {
                lockAdjacentChest(dir);
            }
        }
    }

    /**
     * The server-side tick method for this block entity.
     */
    public static void tick(final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState state, final @NotNull SmallShopBlockEntity be) {
        if (level.isClientSide) {
            return;
        }

        be.tickCounter++;
        final int offerInterval = Config.OFFER_UPDATE_INTERVAL.get();
        if (offerInterval > 0 && be.tickCounter % offerInterval == 0) {
            if (be.needsOfferRefresh) {
                be.updateOfferSlot();
                be.hasResultItemInInput(true); // Re-check with neighbors
                be.needsOfferRefresh = false;
            }
        }
        final int chestInterval = Config.CHEST_IO_INTERVAL.get();
        if (chestInterval > 0 && be.tickCounter % chestInterval == 0) {
            be.pullFromInputChest();
            be.pushToOutputChest();
        }
    }

    /**
     * Pulls items from adjacent inventories configured as INPUT.
     */
    private void pullFromInputChest() {
        if (level == null || level.isClientSide) return;
        for (Direction dir : Direction.values()) {
            if (getModeForSide(dir) == SideMode.INPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(neighbour, inputHandler);
                }
            }
        }
    }

    /**
     * Pushes items to adjacent inventories configured as OUTPUT.
     */
    private void pushToOutputChest() {
        if (level == null || level.isClientSide) return;
        for (Direction dir : Direction.values()) {
            if (getModeForSide(dir) == SideMode.OUTPUT) {
                IItemHandler neighbour = getValidNeighborHandler(dir);
                if (neighbour != null) {
                    transferItems(outputHandler, neighbour);
                }
            }
        }
    }

    // --- NBT Serialization / Deserialization ---
    @Override
    protected void loadAdditional(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadHandlers(tag, registries);
        loadOwner(tag);
        loadOffer(tag, registries);
        loadSettings(tag);
        loadSideModes(tag);

        lockAdjacentChests();
        invalidateCaps();
        tickCounter = 0;
    }

    @Override
    protected void saveAdditional(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveHandlers(tag, registries);
        saveOwner(tag);
        saveOffer(tag, registries);
        saveSettings(tag);
        saveSideModes(tag);
    }

    private void loadHandlers(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> {
            if (tag.contains(name)) {
                handler.deserializeNBT(registries, tag.getCompound(name));
            }
        });
    }

    private void saveHandlers(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider registries) {
        handlerMap.forEach((name, handler) -> tag.put(name, handler.serializeNBT(registries)));
    }

    private void loadOwner(final @NotNull CompoundTag tag) {
        ownerId = tag.hasUUID(NBT_OWNER_ID) ? tag.getUUID(NBT_OWNER_ID) : null;
        ownerName = tag.getString(NBT_OWNER_NAME);
        additionalOwners.clear();
        if (tag.contains(NBT_ADDITIONAL_OWNERS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(NBT_ADDITIONAL_OWNERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID id = entry.getUUID(NBT_ADDITIONAL_OWNER_ID);
                String name = entry.getString(NBT_ADDITIONAL_OWNER_NAME);
                additionalOwners.put(id, name);
            }
        }
    }

    private void saveOwner(final @NotNull CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID(NBT_OWNER_ID, ownerId);
        }
        tag.putString(NBT_OWNER_NAME, ownerName);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, String> e : additionalOwners.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(NBT_ADDITIONAL_OWNER_ID, e.getKey());
            entry.putString(NBT_ADDITIONAL_OWNER_NAME, e.getValue());
            list.add(entry);
        }
        tag.put(NBT_ADDITIONAL_OWNERS, list);
    }

    private void loadOffer(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider registries) {
        offerPayment1 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT1));
        offerPayment2 = ItemStack.parseOptional(registries, tag.getCompound(KEY_PAYMENT2));
        offerResult = ItemStack.parseOptional(registries, tag.getCompound(KEY_RESULT));
        hasOffer = tag.getBoolean(NBT_HAS_OFFER);
    }

    private void saveOffer(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider registries) {
        if (!offerPayment1.isEmpty()) tag.put(KEY_PAYMENT1, offerPayment1.save(registries));
        if (!offerPayment2.isEmpty()) tag.put(KEY_PAYMENT2, offerPayment2.save(registries));
        if (!offerResult.isEmpty()) tag.put(KEY_RESULT, offerResult.save(registries));
        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
    }

    private void loadSettings(final @NotNull CompoundTag tag) {
        shopName = tag.getString(NBT_SHOP_NAME);
        emitRedstone = tag.getBoolean(NBT_EMIT_REDSTONE);
    }

    private void saveSettings(final @NotNull CompoundTag tag) {
        tag.putString(NBT_SHOP_NAME, shopName);
        tag.putBoolean(NBT_EMIT_REDSTONE, emitRedstone);
    }

    /**
     * Loads the I/O mode for each side from NBT.
     * Uses integer ordinals for efficiency.
     */
    private void loadSideModes(final @NotNull CompoundTag tag) {
        // Initialize with defaults
        sideModes.clear();
        for (Direction dir : Direction.values()) {
            sideModes.put(dir, SideMode.DISABLED);
        }

        ListTag sideList = tag.getList(NBT_SIDE_MODES, Tag.TAG_COMPOUND);
        for (int i = 0; i < sideList.size(); i++) {
            CompoundTag sideTag = sideList.getCompound(i);
            // Ordinal is used for efficiency. The values() array order must not change.
            int dirOrdinal = sideTag.getInt(NBT_DIRECTION);
            int modeOrdinal = sideTag.getInt(NBT_MODE);

            if (dirOrdinal >= 0 && dirOrdinal < Direction.values().length) {
                Direction dir = Direction.values()[dirOrdinal];
                SideMode mode = SideMode.fromId(modeOrdinal); // Use safe fromId method
                sideModes.put(dir, mode);
            }
        }
    }

    /**
     * Saves the I/O mode for each side to NBT.
     * Uses integer ordinals for efficiency.
     */
    private void saveSideModes(final @NotNull CompoundTag tag) {
        ListTag sideList = new ListTag();
        for (Map.Entry<Direction, SideMode> entry : sideModes.entrySet()) {
            CompoundTag sideTag = new CompoundTag();
            // Ordinal is used for efficiency. The values() array order must not change.
            sideTag.putInt(NBT_DIRECTION, entry.getKey().ordinal());
            sideTag.putInt(NBT_MODE, entry.getValue().ordinal());
            sideList.add(sideTag);
        }
        tag.put(NBT_SIDE_MODES, sideList);
    }


    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(final @NotNull HolderLookup.Provider registries) {
        // The update tag should be a complete representation of the TE's data.
        return saveWithoutMetadata(registries);
    }
}