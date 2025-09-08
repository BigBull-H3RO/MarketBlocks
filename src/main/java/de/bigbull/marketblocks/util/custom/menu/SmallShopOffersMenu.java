package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * The menu for the "Offers" mode of the Small Shop.
 * This UI is used for creating and viewing trade offers.
 */
// deprecated – ersetzt durch SmallShopMenu
public class SmallShopOffersMenu extends AbstractSmallShopMenu implements ShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler paymentHandler;
    private final IItemHandler offerHandler;

    private static final int PAYMENT_SLOTS = 2;
    private static final int OFFER_SLOTS = 1;
    private static final int TOTAL_SLOTS = PAYMENT_SLOTS + OFFER_SLOTS;
    private static final int OFFER_SLOT_INDEX = PAYMENT_SLOTS;

    private final ContainerData data;

    // Server-side constructor
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.paymentHandler = blockEntity.getPaymentHandler();
        this.offerHandler = blockEntity.getOfferHandler();
        blockEntity.ensureOwner(playerInventory.player);
        this.data = blockEntity.createMenuFlags(playerInventory.player);

        addDataSlots(this.data);
        initSlots(playerInventory);
    }

    // Client-side constructor
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        SmallShopBlockEntity be = readBlockEntity(playerInventory, buf);
        if (be == null) {
            // If the block entity doesn't exist on the client, close the container.
            // This can happen with lag or if the block is broken while the menu is open.
            playerInventory.player.closeContainer();
        }
        this.blockEntity = be;
        this.paymentHandler = be != null ? be.getPaymentHandler() : new ItemStackHandler(PAYMENT_SLOTS);
        this.offerHandler = be != null ? be.getOfferHandler() : new ItemStackHandler(OFFER_SLOTS);
        this.data = be != null ? be.createMenuFlags(playerInventory.player) : new SimpleContainerData(1);

        addDataSlots(this.data);
        initSlots(playerInventory);
        if (be != null) {
            be.ensureOwner(playerInventory.player);
        }
    }

    @Override
    protected void addCustomSlots(Inventory playerInventory) {
        addSlot(new PaymentSlot(paymentHandler, 0, 36, 52));
        addSlot(new PaymentSlot(paymentHandler, 1, 62, 52));
        addSlot(new OfferSlot(offerHandler, 0, 120, 52, this));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Handle quick-moving the result of a trade
        if (index == OFFER_SLOT_INDEX) {
            Slot slot = this.slots.get(index);
            if (!slot.hasItem()) {
                return ItemStack.EMPTY;
            }

            ItemStack stackInSlot = slot.getItem();
            ItemStack result = stackInSlot.copy();
            // Try to move the item to the player's inventory
            if (!this.moveItemStackTo(stackInSlot, TOTAL_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            // This logic seems to be for when a player takes an item from the offer slot
            // It should trigger the purchase logic in the block entity
            blockEntity.performPurchase();
            blockEntity.updateOfferSlot(); // Refresh the offer slot state

            slot.onTake(player, stackInSlot);
            return result;
        }

        // Standard quick-move logic for other slots
        return super.quickMoveStack(player, index, TOTAL_SLOTS, PAYMENT_SLOTS);
    }

    /**
     * Fills the payment slots with the required items from the player's inventory.
     * This is a client-side helper for the "auto-fill" button.
     * @param required The item stacks required for the payment.
     */
    public void fillPaymentSlots(ItemStack... required) {
        clearPaymentSlots();

        for (int paymentSlotIndex = 0; paymentSlotIndex < PAYMENT_SLOTS; paymentSlotIndex++) {
            if (paymentSlotIndex >= required.length) {
                continue;
            }

            ItemStack requiredStack = required[paymentSlotIndex];
            if (requiredStack.isEmpty()) {
                continue;
            }

            transferRequiredItems(requiredStack, paymentSlotIndex);
        }
    }

    /**
     * Clears all items from the payment slots, returning them to the player's inventory.
     */
    private void clearPaymentSlots() {
        for (int paymentSlotIndex = 0; paymentSlotIndex < PAYMENT_SLOTS; paymentSlotIndex++) {
            ItemStack stackInSlot = this.slots.get(paymentSlotIndex).getItem();
            // Move the item back to the player inventory
            if (!stackInSlot.isEmpty() && this.moveItemStackTo(stackInSlot, TOTAL_SLOTS, this.slots.size(), true)) {
                // If the stack was fully moved, the slot will be empty. If not, it will have the remainder.
                this.slots.get(paymentSlotIndex).set(stackInSlot.isEmpty() ? ItemStack.EMPTY : stackInSlot);
            }
        }
    }

    /**
     * Transfers a single required item stack from the player's inventory into a specified payment slot.
     * @param required The item stack to transfer.
     * @param slotIndex The index of the payment slot to fill.
     */
    private void transferRequiredItems(ItemStack required, int slotIndex) {
        for (int i = TOTAL_SLOTS; i < this.slots.size(); i++) { // Iterate player inventory
            ItemStack inventoryStack = this.slots.get(i).getItem();
            if (!inventoryStack.isEmpty() && ItemStack.isSameItemSameComponents(inventoryStack, required)) {
                ItemStack currentPaymentStack = this.slots.get(slotIndex).getItem();

                if (currentPaymentStack.isEmpty() || ItemStack.isSameItemSameComponents(inventoryStack, currentPaymentStack)) {
                    int maxStackSize = Math.min(inventoryStack.getMaxStackSize(), this.slots.get(slotIndex).getMaxStackSize());
                    int spaceInPaymentSlot = maxStackSize - currentPaymentStack.getCount();
                    int amountToTransfer = Math.min(spaceInPaymentSlot, inventoryStack.getCount());

                    if (amountToTransfer > 0) {
                        ItemStack newPaymentStack = inventoryStack.copy();
                        newPaymentStack.setCount(currentPaymentStack.getCount() + amountToTransfer);
                        inventoryStack.shrink(amountToTransfer);
                        this.slots.get(i).set(inventoryStack.isEmpty() ? ItemStack.EMPTY : inventoryStack);
                        this.slots.get(slotIndex).set(newPaymentStack);

                        // If the payment slot is full, we can stop looking for this item.
                        if (newPaymentStack.getCount() >= maxStackSize) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    @Override
    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean hasOffer() {
        return hasFlag(SmallShopBlockEntity.HAS_OFFER_FLAG);
    }

    public boolean isOfferAvailable() {
        return hasFlag(SmallShopBlockEntity.OFFER_AVAILABLE_FLAG);
    }

    @Override
    public int getFlags() {
        return data.get(0);
    }

    /**
     * A custom slot for the payment items.
     */
    public static class PaymentSlot extends SlotItemHandler {
        public PaymentSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Players can place any item in the payment slots.
            return true;
        }
    }

    /**
     * A custom slot for the offer's result item.
     * This slot has special logic to handle offer creation and execution.
     */
    public static class OfferSlot extends SlotItemHandler {
        private final SmallShopOffersMenu menu;

        public OfferSlot(IItemHandler handler, int slot, int x, int y, SmallShopOffersMenu menu) {
            super(handler, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // An item can only be placed by the owner if no offer is currently set.
            if (menu.hasOffer()) return false;
            return menu.isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            // The owner can always pick up the item.
            if (menu.isOwner()) return true;
            // Other players can only pick it up if an offer is set and available for purchase.
            return menu.hasOffer() && menu.isOfferAvailable();
        }

        @Override
        public ItemStack remove(int amount) {
            if (menu.isOwner() || (menu.hasOffer() && menu.isOfferAvailable())) {
                return super.remove(amount);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void set(ItemStack stack) {
            // Allow server updates to display the result preview even when an offer exists.
            // Only block manual placement by the owner while an offer is active.
            if (menu.hasOffer() && menu.isOwner() && !stack.isEmpty()) {
                return;
            }
            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            // After taking the item, the offer slot needs to be refreshed on the server.
            menu.blockEntity.updateOfferSlot();
        }
    }
}