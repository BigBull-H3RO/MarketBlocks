package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
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
    private boolean outputAlmostFull = false;
    private boolean outputFull = false;

    // Side Configuration
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);

    // --- Inventories ---
    private class TrackedItemStackHandler extends ItemStackHandler {
        TrackedItemStackHandler(int size) {
            super(size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            needsOfferRefresh = true;
            updateOfferSlot();
            sync();
        }
    }

    private final ItemStackHandler inputHandler = new TrackedItemStackHandler(12);

    private final ItemStackHandler outputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            needsOfferRefresh = true;
            updateOfferSlot();
            updateOutputFullness();
            sync();
        }
    };

    private final ItemStackHandler paymentHandler = new TrackedItemStackHandler(2);

    private final ItemStackHandler offerHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // Fix Item Loss Bug: Ensure we only allow extraction of the FULL offer amount.
            // If the player tries to extract less (e.g. because inventory is full), we deny it.
            // This prevents paying full price for partial items.
            ItemStack currentOffer = getOfferResult();
            if (currentOffer.isEmpty()) return ItemStack.EMPTY;

            // If we are simulating, we just check if it's possible.
            // But we must respect the "all or nothing" rule even in simulation to signal the caller correctly.
            if (amount < currentOffer.getCount()) {
                return ItemStack.EMPTY;
            }

            if (simulate) {
                return super.extractItem(slot, amount, true);
            }

            // CRITICAL FIX: Check if purchase is allowed BEFORE giving the item
            if (!isReadyToPurchase()) {
                updateOfferSlot(); // Force visual refresh
                return ItemStack.EMPTY;
            }

            // Extract the item. We verified amount >= offer amount.
            ItemStack result = super.extractItem(slot, amount, false);

            // Double check: If we somehow extracted less than expected (shouldn't happen with ItemStackHandler if we checked count),
            // we have a problem. But super.extractItem() returns what was extracted.
            if (!result.isEmpty()) {
                // Since we verified amount >= count earlier, and ItemStackHandler respects amount,
                // and updateOfferSlot ensures the slot has the correct amount,
                // we can safely assume full extraction occurred or the slot was empty (handled by !result.isEmpty()).
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
    private void markDirty() {
        setChanged();
    }

    public void invalidateCaps() {
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public void sync() {
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
                worldPosition.getZ() + 0.5) <= MAX_PLAYER_DISTANCE_SQUARED;
    }

    // --- Owner System ---
    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public void addOwner(UUID id, String name) {
        additionalOwners.put(id, name);
        sync();
    }

    public void addOwnerClient(UUID id, String name) {
        additionalOwners.put(id, name);
    }

    public void removeOwner(UUID id) {
        if (additionalOwners.remove(id) != null) {
            sync();
        }
    }

    public Set<UUID> getOwners() {
        Set<UUID> owners = new HashSet<>(additionalOwners.keySet());
        if (ownerId != null) {
            owners.add(ownerId);
        }
        return owners;
    }

    public Map<UUID, String> getAdditionalOwners() {
        return additionalOwners;
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        additionalOwners.clear();
        additionalOwners.putAll(owners);
        sync();
    }

    public boolean isOwner(Player player) {
        UUID id = player.getUUID();
        return (ownerId != null && ownerId.equals(id)) || additionalOwners.containsKey(id);
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

    public void setShopNameClient(String name) {
        this.shopName = name;
        updateOfferSlot();
    }

    public boolean isEmitRedstone() {
        return emitRedstone;
    }

    public void setEmitRedstone(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
        sync();
    }

    public void setEmitRedstoneClient(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
        updateOfferSlot();
    }

    // --- Side Configuration ---
    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode) {
        SideMode oldMode = getMode(dir);
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

    public void setModeClient(Direction dir, SideMode mode) {
        sideModes.put(dir, mode);
        updateOfferSlot();
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

    private IItemHandler getValidNeighborHandler(Direction dir) {
        if (level == null) return null;
        BlockPos neighbourPos = worldPosition.relative(dir);
        IItemHandler neighbour = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, dir.getOpposite());
        if (neighbour == null) {
            neighbour = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbourPos, null);
        }
        if (neighbour instanceof LockedChestWrapper locked) {
            if (locked.getOwnerId() != null && getOwners().contains(locked.getOwnerId())) {
                return locked.getDelegate();
            } else {
                return null;
            }
        }
        return neighbour;
    }

    private void transferItems(IItemHandler from, IItemHandler to) {
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stackInSlot = from.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            ItemStack remainderSim = ItemHandlerHelper.insertItem(to, stackInSlot.copy(), true);
            int transferable = stackInSlot.getCount() - remainderSim.getCount();
            if (transferable > 0) {
                ItemStack extracted = from.extractItem(i, transferable, false);
                ItemStack leftover = ItemHandlerHelper.insertItem(to, extracted, false);
                if (!leftover.isEmpty()) {
                    from.insertItem(i, leftover, false);
                }
            }
        }
    }

    // --- Offer System ---
    public void createOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        this.offerPayment1 = payment1.copy();
        this.offerPayment2 = payment2.copy();
        this.offerResult = result.copy();
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

    public void setHasOfferClient(boolean hasOffer) {
        this.hasOffer = hasOffer;
        updateOfferSlot();
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

    private int countMatchingPayment(ItemStack target) {
        int total = 0;
        for (int i = 0; i < paymentHandler.getSlots(); i++) {
            ItemStack stack = paymentHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private boolean hasEnoughPayment(ItemStack required) {
        return required.isEmpty() || countMatchingPayment(required) >= required.getCount();
    }

    public void updateOfferSlot() {
        if (!hasOffer) {
            return;
        }

        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        if (canAfford() && hasResultItemInInput(false) && !isOutputFull() && hasOutputSpace(p1, p2)) {
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

    public boolean hasResultItemInInput() {
        return hasResultItemInInput(false);
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
        ItemStack result = getOfferResult();
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

    public void processPurchase() {
        pullFromInputChest();
        if (!isReadyToPurchase()) {
            return;
        }

        executeTrade();

        sync();
        triggerRedstonePulse();
        needsOfferRefresh = true;
    }

    private boolean isReadyToPurchase() {
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        return hasOffer && canAfford() && hasResultItemInInput(false) && !isOutputFull() && hasOutputSpace(p1, p2);
    }

    private void executeTrade() {
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        ItemStack result = getOfferResult();

        if (!p1.isEmpty()) removePayment(p1);
        if (!p2.isEmpty()) removePayment(p2);

        removeFromInput(result);

        if (!p1.isEmpty()) addToOutput(p1.copy());
        if (!p2.isEmpty()) addToOutput(p2.copy());
    }

    private void triggerRedstonePulse() {
        if (level == null || level.isClientSide || !emitRedstone) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof SmallShopBlock block) {
            level.setBlock(worldPosition, state.setValue(SmallShopBlock.POWERED, true), 3);
            level.updateNeighborsAt(worldPosition, block);
            level.scheduleTick(worldPosition, block, 20);
        }
    }

    private void removeFromInput(ItemStack toRemove) {
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

    private void removePayment(ItemStack required) {
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

    private boolean hasOutputSpace(ItemStack... stacks) {
        ItemStackHandler testHandler = new ItemStackHandler(outputHandler.getSlots());
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            testHandler.setStackInSlot(i, outputHandler.getStackInSlot(i).copy());
        }

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            if (!ItemHandlerHelper.insertItem(outputHandler, stack, true).isEmpty()) {
                return false;
            }
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

        for (Direction dir : Direction.values()) {
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
        ItemStack p1 = getOfferPayment1();
        ItemStack p2 = getOfferPayment2();
        return hasOffer && hasResultItemInInput(false) && !isOutputFull() && hasOutputSpace(p1, p2);
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
        handlerMap.values().forEach(handler -> dropItems(level, pos, handler));
    }

    public void unlockAdjacentChests() {
        if (level == null) return;
        for (Direction dir : Direction.values()) {
            BlockPos neighbour = worldPosition.relative(dir);
            level.invalidateCapabilities(neighbour);
        }
    }

    private void lockAdjacentChest(Direction dir) {
        if (level == null) return;
        BlockPos neighbour = worldPosition.relative(dir);
        level.invalidateCapabilities(neighbour);
    }

    public void lockAdjacentChests() {
        if (level == null) return;
        for (Direction dir : Direction.values()) {
            if (getModeForSide(dir) == SideMode.INPUT || getModeForSide(dir) == SideMode.OUTPUT) {
                lockAdjacentChest(dir);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SmallShopBlockEntity be) {
        if (level.isClientSide) {
            return;
        }

        be.tickCounter++;
        int offerInterval = Config.OFFER_UPDATE_INTERVAL.get();
        if (offerInterval > 0 && be.tickCounter % offerInterval == 0) {
            if (be.needsOfferRefresh) {
                be.updateOfferSlot();
                be.hasResultItemInInput(true); // Re-check with neighbors
                be.needsOfferRefresh = false;
            }
        }
        int chestInterval = Config.CHEST_IO_INTERVAL.get();
        if (chestInterval > 0 && be.tickCounter % chestInterval == 0) {
            be.pullFromInputChest();
            be.pushToOutputChest();
        }

        be.updateOutputFullness();
    }

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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadHandlers(tag, registries);
        loadOwner(tag);
        loadOffer(tag, registries);
        loadSettings(tag);
        loadSideModes(tag);
        outputAlmostFull = tag.getBoolean(NBT_OUTPUT_WARNING);
        outputFull = tag.getBoolean(NBT_OUTPUT_FULL);

        lockAdjacentChests();
        invalidateCaps();
        tickCounter = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveHandlers(tag, registries);
        saveOwner(tag);
        saveOffer(tag, registries);
        saveSettings(tag);
        saveSideModes(tag);
        tag.putBoolean(NBT_OUTPUT_WARNING, outputAlmostFull);
        tag.putBoolean(NBT_OUTPUT_FULL, outputFull);
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

    private void loadOwner(CompoundTag tag) {
        ownerId = tag.hasUUID(NBT_OWNER_ID) ? tag.getUUID(NBT_OWNER_ID) : null;
        ownerName = tag.getString(NBT_OWNER_NAME);
        additionalOwners.clear();
        if (tag.contains(NBT_ADDITIONAL_OWNERS, 9)) { // 9 = ListTag
            ListTag list = tag.getList(NBT_ADDITIONAL_OWNERS, 10); // 10 = CompoundTag
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID id = entry.getUUID(NBT_ADDITIONAL_OWNER_ID);
                String name = entry.getString(NBT_ADDITIONAL_OWNER_NAME);
                additionalOwners.put(id, name);
            }
        }
    }

    private void saveOwner(CompoundTag tag) {
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
        shopName = tag.getString(NBT_SHOP_NAME);
        emitRedstone = tag.getBoolean(NBT_EMIT_REDSTONE);
    }

    private void saveSettings(CompoundTag tag) {
        tag.putString(NBT_SHOP_NAME, shopName);
        tag.putBoolean(NBT_EMIT_REDSTONE, emitRedstone);
    }

    private void loadSideModes(CompoundTag tag) {
        sideModes.clear();
        for (Direction dir : Direction.values()) {
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
                // Log error for invalid enum value
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
        // The update tag should be a complete representation of the TE's data.
        return saveWithoutMetadata(registries);
    }
}