package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unified menu for the Small Shop. Handles all tabs without reopening the
 * container on tab changes.
 */
public class SmallShopMenu extends AbstractSmallShopMenu implements ShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final Player player;

    private final IItemHandler paymentHandler;
    private final IItemHandler offerHandler;
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;

    private final ContainerData flags;
    private final ContainerData tabData = new SimpleContainerData(1);

    // Side mode handling for settings tab
    private final EnumMap<Direction, SideMode> sideModes = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, SideMode> initialModes = new EnumMap<>(Direction.class);

    private static final int PAYMENT_SLOTS = 2;
    private static final int OFFER_SLOTS = 1;
    private static final int INPUT_SLOTS = 12;
    private static final int OUTPUT_SLOTS = 12;
    private static final int TOTAL_SLOTS = PAYMENT_SLOTS + OFFER_SLOTS + INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int OFFER_SLOT_INDEX = PAYMENT_SLOTS; // after payment slots

    // Server ctor
    public SmallShopMenu(int containerId, Inventory inv, SmallShopBlockEntity be) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), containerId);
        this.blockEntity = be;
        this.player = inv.player;
        this.paymentHandler = be.getPaymentHandler();
        this.offerHandler = be.getOfferHandler();
        this.inputHandler = be.getInputHandler();
        this.outputHandler = be.getOutputHandler();
        this.flags = be.createMenuFlags(player);

        for (Direction dir : Direction.values()) {
            SideMode mode = be.getMode(dir);
            sideModes.put(dir, mode);
            initialModes.put(dir, mode);
        }

        addDataSlots(this.flags);
        addDataSlots(this.tabData);
        blockEntity.ensureOwner(player);
        initSlots(inv);
    }

    // Client ctor
    public SmallShopMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), containerId);
        SmallShopBlockEntity be = readBlockEntity(inv, buf);
        if (be == null) {
            inv.player.closeContainer();
        }
        this.blockEntity = be;
        this.player = inv.player;
        this.paymentHandler = be != null ? be.getPaymentHandler() : new ItemStackHandler(PAYMENT_SLOTS);
        this.offerHandler = be != null ? be.getOfferHandler() : new ItemStackHandler(OFFER_SLOTS);
        this.inputHandler = be != null ? be.getInputHandler() : new ItemStackHandler(INPUT_SLOTS);
        this.outputHandler = be != null ? be.getOutputHandler() : new ItemStackHandler(OUTPUT_SLOTS);
        this.flags = be != null ? be.createMenuFlags(player) : new SimpleContainerData(1);

        if (be != null) {
            for (Direction dir : Direction.values()) {
                SideMode mode = be.getMode(dir);
                sideModes.put(dir, mode);
                initialModes.put(dir, mode);
            }
        }

        addDataSlots(this.flags);
        addDataSlots(this.tabData);
        if (be != null) {
            be.ensureOwner(player);
        }
        initSlots(inv);
    }

    @Override
    protected void addCustomSlots(Inventory playerInventory) {
        // Payment slots
        addSlot(new GatedSlot(paymentHandler, 0, 36, 52, ShopTab.OFFERS));
        addSlot(new GatedSlot(paymentHandler, 1, 62, 52, ShopTab.OFFERS));

        // Offer slot
        addSlot(new OfferSlot(offerHandler, 0, 120, 52));

        // Input inventory (4x3 grid starting at 8,18)
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OwnerGatedSlot(inputHandler, index++, 8 + col * 18, 18 + row * 18, ShopTab.INVENTORY));
            }
        }

        // Output inventory (4x3 grid starting at 98,18)
        index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OutputGatedSlot(outputHandler, index++, 98 + col * 18, 18 + row * 18, ShopTab.INVENTORY));
            }
        }
    }

    @Override
    protected void addPlayerInventory(Inventory playerInventory, int startY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new PlayerSlot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        int hotbarY = startY + 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new PlayerSlot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    // --- Tab API ---
    public ShopTab getActiveTab() {
        return ShopTab.fromId(tabData.get(0));
    }

    public boolean isTab(ShopTab tab) {
        return getActiveTab() == tab;
    }

    /** Sets the active tab on the server side and broadcasts changes. */
    public void setActiveTabServer(ShopTab tab) {
        tabData.set(0, tab.ordinal());
        broadcastChanges();
    }

    /** Sets the active tab on the client side without notifying the server. */
    public void setActiveTabClient(ShopTab tab) {
        tabData.set(0, tab.ordinal());
    }

    // --- Settings helpers ---
    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode) {
        sideModes.put(dir, mode);
    }

    public void resetModes() {
        sideModes.clear();
        sideModes.putAll(initialModes);
    }

    public Map<UUID, String> getAdditionalOwners() {
        return blockEntity.getAdditionalOwners();
    }

    /**
     * Füllt die Zahlungsslots mit den benötigten Items aus dem Spielerinventar.
     */
    public void fillPaymentSlots(ItemStack... required) {
        clearPaymentSlots();
        for (int i = 0; i < PAYMENT_SLOTS && i < required.length; i++) {
            ItemStack req = required[i];
            if (req.isEmpty()) {
                continue;
            }
            transferRequiredItems(req, i);
        }
    }

    private void clearPaymentSlots() {
        for (int i = 0; i < PAYMENT_SLOTS; i++) {
            Slot s = this.slots.get(i);
            ItemStack stack = s.getItem();
            if (!stack.isEmpty() && this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                s.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                s.setChanged(); // <--
            }
        }
    }


    private void transferRequiredItems(ItemStack required, int slotIndex) {
        for (int i = TOTAL_SLOTS; i < this.slots.size(); i++) {
            ItemStack invStack = this.slots.get(i).getItem();
            if (!invStack.isEmpty() && ItemStack.isSameItemSameComponents(invStack, required)) {
                ItemStack cur = this.slots.get(slotIndex).getItem();
                if (cur.isEmpty() || ItemStack.isSameItemSameComponents(invStack, cur)) {
                    int max = Math.min(invStack.getMaxStackSize(), this.slots.get(slotIndex).getMaxStackSize());
                    int space = max - cur.getCount();
                    int move = Math.min(space, invStack.getCount());
                    if (move > 0) {
                        ItemStack newStack = invStack.copy();
                        newStack.setCount(cur.getCount() + move);
                        invStack.shrink(move);
                        this.slots.get(i).set(invStack.isEmpty() ? ItemStack.EMPTY : invStack);
                        this.slots.get(slotIndex).set(newStack);
                        if (newStack.getCount() >= max) {
                            break;
                        }
                    }
                }
            }
        }
    }

    // --- Quick move / shift click ---
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isTab(ShopTab.SETTINGS)) return ItemStack.EMPTY;

        if (index == OFFER_SLOT_INDEX && isTab(ShopTab.OFFERS)) {
            Slot slot = this.slots.get(index);

            if (!blockEntity.hasOffer()) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack ret = stack.copy();
                if (!this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                if (stack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
                blockEntity.updateOfferSlot();
                return ret;
            }

            if (blockEntity.isOutputFull()) {
                if (!player.level().isClientSide) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.output_full"));
                }
                return ItemStack.EMPTY;
            }

            ItemStack result = ItemStack.EMPTY;

            while (blockEntity.isOfferAvailable() && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (result.isEmpty()) {
                    result = stack.copy();
                }

                if (!this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                    ItemStack drop = stack.copy();
                    slot.set(ItemStack.EMPTY);
                    blockEntity.processPurchase();
                    player.drop(drop, false);
                    blockEntity.updateOfferSlot();
                    break;
                }

                slot.set(stack);
                blockEntity.processPurchase();
                blockEntity.updateOfferSlot();
            }

            blockEntity.updateOfferSlot();
            return result;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack ret = stack.copy();

        if (index < TOTAL_SLOTS) {
            if (!this.moveItemStackTo(stack, TOTAL_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (isTab(ShopTab.OFFERS)) {
                if (!blockEntity.hasOffer()) {
                    this.moveItemStackTo(stack, 0, PAYMENT_SLOTS, false);
                    if (!stack.isEmpty()) {
                        Slot offerSlot = this.slots.get(OFFER_SLOT_INDEX);
                        if (offerSlot.getItem().isEmpty()) {
                            this.moveItemStackTo(stack, OFFER_SLOT_INDEX, OFFER_SLOT_INDEX + 1, false);
                        }
                    }
                    if (!stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stack, 0, PAYMENT_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (isTab(ShopTab.INVENTORY) && isOwner()) {
                int start = PAYMENT_SLOTS + OFFER_SLOTS;
                int end = start + INPUT_SLOTS;
                if (!this.moveItemStackTo(stack, start, end, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == ret.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return ret;
    }

    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (type == ClickType.PICKUP_ALL) {
            if (slotId >= 0 && slotId < PAYMENT_SLOTS) {
                return;
            }
            if (slotId == OFFER_SLOT_INDEX && !blockEntity.hasOffer()) {
                return;
            }
        }
        super.clicked(slotId, button, type, player);
    }

    // --- ShopMenu impl ---
    @Override
    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getFlags() {
        return flags.get(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    // --- Slot wrappers ---
    private abstract class BaseSlot extends SlotItemHandler {
        private final ShopTab tab;
        BaseSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y);
            this.tab = tab;
        }

        @Override
        public boolean isActive() {
            return isTab(tab);
        }
    }

    private class GatedSlot extends BaseSlot {
        GatedSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y, tab);
        }
    }

    private class OwnerGatedSlot extends BaseSlot {
        OwnerGatedSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y, tab);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            return isOwner();
        }
    }

    private class OfferSlot extends BaseSlot {
        OfferSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y, ShopTab.OFFERS);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Angebot existiert -> nie überschreiben
            if (blockEntity.hasOffer()) return false;
            // Template-Modus: nur Owner dürfen vorlegen
            return isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            if (!blockEntity.hasOffer()) {
                // Template-Modus: Owner dürfen falsch gelegte Items wieder rausnehmen
                return isOwner();
            }

            if (blockEntity.isOutputFull()) {
                if (!player.level().isClientSide) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.output_full"));
                }
                return false;
            }
            // Angebots-Modus: Kauf erlaubt, wenn verfügbar (Owner darf auch kaufen)
            return blockEntity.isOfferAvailable();
        }

        @Override
        public ItemStack remove(int amount) {
            if (!blockEntity.hasOffer()) {
                // Template-Modus: normal entfernen (Owner-Check übernimmt mayPickup)
                return super.remove(amount);
            }
            // Angebots-Modus: nur wenn verfügbar
            return blockEntity.isOfferAvailable() ? super.remove(amount) : ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            // Bestehendes Angebot nie per Set überschreiben
            if (blockEntity.hasOffer()) return;
            // Template-Modus: nur Owner dürfen setzen
            if (!isOwner()) return;
            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            blockEntity.updateOfferSlot();
        }

    }

    private class OutputGatedSlot extends BaseSlot {
        OutputGatedSlot(IItemHandler handler, int slot, int x, int y, ShopTab tab) {
            super(handler, slot, x, y, tab);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private class PlayerSlot extends Slot {
        PlayerSlot(Inventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isActive() {
            return !isTab(ShopTab.SETTINGS);
        }
    }
}