package de.bigbull.marketblocks.util.custom.entity;

import de.bigbull.marketblocks.config.Config;
import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
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

public class SmallShopBlockEntity extends BlockEntity implements MenuProvider {
    // NBT-Keys
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

    // Inventarhandler-Namen
    private static final String HANDLER_INPUT = "InputInventory";
    private static final String HANDLER_OUTPUT = "OutputInventory";
    private static final String HANDLER_PAYMENT = "PaymentSlots";
    private static final String HANDLER_OFFER = "OfferSlot";

    // Menü-Flags
    public static final int HAS_OFFER = 1;
    public static final int OFFER_AVAILABLE = 2;
    public static final int OWNER_FLAG = 4;

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
    private final Map<UUID, String> additionalOwners = new HashMap<>();

    // Shop Name
    private String shopName = "";

    // Redstone
    private boolean emitRedstone = false;

    // Seitenkonfiguration
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);

    public SmallShopBlockEntity(BlockPos pos, BlockState state) {
        super(RegistriesInit.SMALL_SHOP_BLOCK_ENTITY.get(), pos, state);
        for (Direction dir : Direction.values()) {
            sideModes.put(dir, SideMode.DISABLED);
        }
    }

    // Inventare
    private final ItemStackHandler inputHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            needsOfferRefresh = true;
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
            needsOfferRefresh = true;
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
            HANDLER_INPUT, inputHandler,
            HANDLER_OUTPUT, outputHandler,
            HANDLER_PAYMENT, paymentHandler,
            HANDLER_OFFER, offerHandler
    );

    private final IItemHandler inputOnly  = new SidedWrapper(inputHandler, false);
    private final IItemHandler outputOnly = new SidedWrapper(outputHandler, true);

    private final OfferManager offerManager = new OfferManager(this);

    private int tickCounter = 0;
    private boolean needsOfferRefresh = false;

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

    public  void sync() {
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

    public void addOwner(UUID id, String name) {
        additionalOwners.put(id, name);
        sync();
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

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String name) {
        if (name.length() > 32) {
            name = name.substring(0, 32);
        }
        this.shopName = name;
        sync();
    }

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

    public void setEmitRedstoneClient(boolean emitRedstone) {
        this.emitRedstone = emitRedstone;
    }

    // Seitenkonfiguration
    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode) {
        SideMode oldMode = getMode(dir);
        sideModes.put(dir, mode);
        markDirty();
        sync();
        invalidateNeighbor(dir);
        if (mode == SideMode.INPUT || mode == SideMode.OUTPUT) {
            lockAdjacentChest(dir);
        } else if (oldMode != SideMode.DISABLED) {
            unlockAdjacentChests();
        }
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

    // Angebots-System
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

        if (canAfford() && hasResultItemInInput(false)) {
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
        return hasResultItemInInput(false);
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
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

        if (checkNeighbors && level != null && total < result.getCount()) {
            for (Direction dir : Direction.values()) {
                if (getModeForSide(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour == null) {
                        continue;
                    }
                    for (int i = 0; i < neighbour.getSlots(); i++) {
                        ItemStack stack = neighbour.getStackInSlot(i);
                        if (ItemStack.isSameItemSameComponents(stack, result)) {
                            total += stack.getCount();
                            if (total >= result.getCount()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return total >= result.getCount();
    }

    private void processPurchase() {
        pullFromInputChest();
        if (!hasOffer || !canAfford() || !hasResultItemInInput(false)) {
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
            if (emitRedstone) {
                BlockState state = level.getBlockState(worldPosition);
                if (state.getBlock() instanceof SmallShopBlock block) {
                    level.setBlock(worldPosition, state.setValue(SmallShopBlock.POWERED, true), 3);
                    level.updateNeighborsAt(worldPosition, block);
                    level.scheduleTick(worldPosition, block, 2);
                }
            }
        }
        needsOfferRefresh = true;
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

        if (remaining > 0 && level != null && !level.isClientSide) {
            for (Direction dir : Direction.values()) {
                if (getModeForSide(dir) == SideMode.INPUT) {
                    IItemHandler neighbour = getValidNeighborHandler(dir);
                    if (neighbour == null) {
                        continue;
                    }
                    for (int i = 0; i < neighbour.getSlots() && remaining > 0; i++) {
                        ItemStack stack = neighbour.getStackInSlot(i);
                        if (ItemStack.isSameItemSameComponents(stack, toRemove)) {
                            int toTake = Math.min(remaining, stack.getCount());
                            neighbour.extractItem(i, toTake, false);
                            remaining -= toTake;
                        }
                    }
                    if (remaining <= 0) {
                        break;
                    }
                }
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
        return hasOffer && hasResultItemInInput(false);
    }

    public ContainerData createMenuFlags(Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int flags = 0;
                    if (hasOffer()) flags |= HAS_OFFER;
                    if (isOfferAvailable()) flags |= OFFER_AVAILABLE;
                    if (isOwner(player)) flags |= OWNER_FLAG;
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
        if (!level.isClientSide) {
            be.tickCounter++;
            int offerInterval = Config.OFFER_UPDATE_INTERVAL.get();
            if (offerInterval > 0 && be.tickCounter % offerInterval == 0) {
                if (be.needsOfferRefresh) {
                    be.updateOfferSlot();
                    be.hasResultItemInInput(true);
                    be.needsOfferRefresh = false;
                }
            }
            int chestInterval = Config.CHEST_IO_INTERVAL.get();
            if (chestInterval > 0 && be.tickCounter % chestInterval == 0) {
                be.pullFromInputChest();
                be.pushToOutputChest();
            }
        }
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
        ownerId = tag.hasUUID(NBT_OWNER_ID) ? tag.getUUID(NBT_OWNER_ID) : null;
        ownerName = tag.contains(NBT_OWNER_NAME) ? tag.getString(NBT_OWNER_NAME) : "";
        additionalOwners.clear();
        if (tag.contains(NBT_ADDITIONAL_OWNERS)) {
            ListTag list = tag.getList(NBT_ADDITIONAL_OWNERS, 10);
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

        hasOffer = tag.getBoolean(NBT_HAS_OFFER);
        loadOwner(tag);
        shopName = tag.contains(NBT_SHOP_NAME) ? tag.getString(NBT_SHOP_NAME) : "";
        emitRedstone = tag.getBoolean(NBT_EMIT_REDSTONE);

        ListTag sideList = tag.getList(NBT_SIDE_MODES, 10);
        sideModes.clear();
        for (Direction dir : Direction.values()) {
            sideModes.put(dir, SideMode.DISABLED);
        }
        for (int i = 0; i < sideList.size(); i++) {
            CompoundTag sideTag = sideList.getCompound(i);
            Direction dir = Direction.valueOf(sideTag.getString(NBT_DIRECTION));
            SideMode mode = SideMode.valueOf(sideTag.getString(NBT_MODE));
            sideModes.put(dir, mode);
        }
        lockAdjacentChests();
        tickCounter = 0;
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

        tag.putBoolean(NBT_HAS_OFFER, hasOffer);
        saveOwner(tag);
        tag.putString(NBT_SHOP_NAME, shopName);
        tag.putBoolean(NBT_EMIT_REDSTONE, emitRedstone);

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
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}