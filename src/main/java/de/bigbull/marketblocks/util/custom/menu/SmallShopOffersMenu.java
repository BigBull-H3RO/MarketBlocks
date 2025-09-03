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
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * The menu for the "Offers" tab of the Small Shop.
 * This UI is used for creating and viewing trade offers, and for performing the trade itself.
 */
public class SmallShopOffersMenu extends AbstractSmallShopMenu implements ShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final ContainerData data;
    private final Player player;

    private static final int PAYMENT_SLOTS = 2;
    private static final int OFFER_SLOTS = 1;
    private static final int CONTAINER_SLOT_COUNT = PAYMENT_SLOTS + OFFER_SLOTS;

    /**
     * Server-side constructor.
     */
    public SmallShopOffersMenu(int containerId, @NotNull Inventory playerInventory, @NotNull SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        this.data = blockEntity.createMenuFlags(this.player);

        this.blockEntity.ensureOwner(this.player);
        this.addDataSlots(this.data);
        this.initSlots(playerInventory);
    }

    /**
     * Client-side constructor.
     */
    public SmallShopOffersMenu(int containerId, @NotNull Inventory playerInventory, @NotNull RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        this.blockEntity = readBlockEntity(playerInventory, buf); // Throws if BE is null
        this.player = playerInventory.player;
        this.data = new SimpleContainerData(1); // Client doesn't need the real data, just a placeholder

        this.blockEntity.ensureOwner(this.player);
        this.addDataSlots(this.data);
        this.initSlots(playerInventory);
    }

    @Override
    protected void addCustomSlots(final @NotNull Inventory playerInventory) {
        addSlot(new PaymentSlot(this.blockEntity.getPaymentHandler(), 0, 36, 52));
        addSlot(new PaymentSlot(this.blockEntity.getPaymentHandler(), 1, 62, 52));
        addSlot(new OfferSlot(this.blockEntity.getOfferHandler(), 0, 120, 52, this));
    }

    /**
     * Handles shift-clicking.
     * - From result slot -> Player inventory
     * - From payment slots -> Player inventory
     * - From player inventory -> Payment slots (if owner and no offer)
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyStack = sourceStack.copy();

        // --- Move from Container to Player ---
        if (index < CONTAINER_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, CONTAINER_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }
        // --- Move from Player to Container ---
        else {
            // Only owners can shift-click items into the payment slots when creating an offer.
            if (isOwner() && !hasFlag(SmallShopBlockEntity.HAS_OFFER_FLAG)) {
                if (!this.moveItemStackTo(sourceStack, 0, PAYMENT_SLOTS, false)) {
                    return ItemStack.EMPTY; // Failed to move to payment slots
                }
            } else {
                return ItemStack.EMPTY; // Non-owners or shops with offers cannot shift-click items in.
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == copyStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return copyStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.blockEntity.stillValid(player);
    }

    @Override
    public @NotNull SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getFlags() {
        return data.get(0);
    }

    public void fillPaymentSlots(ItemStack required1, ItemStack required2) {
        // Clear current payment slots to ensure a clean state
        this.slots.get(0).set(ItemStack.EMPTY);
        this.slots.get(1).set(ItemStack.EMPTY);

        // Fulfill the required items from the player's inventory
        fulfillRequirement(required1, this.slots.get(0));
        fulfillRequirement(required2, this.slots.get(1));

        // Tell the client that the container has changed.
        this.broadcastChanges();
    }

    private void fulfillRequirement(ItemStack required, Slot targetSlot) {
        if (required.isEmpty()) {
            return;
        }

        Inventory playerInventory = this.player.getInventory();
        int countNeeded = required.getCount();
        ItemStack collected = required.copyWithCount(0);

        // Find and remove the required items from player's inventory
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stackInPlayerInv = playerInventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(required, stackInPlayerInv)) {
                int canTake = Math.min(stackInPlayerInv.getCount(), countNeeded);
                stackInPlayerInv.shrink(canTake);
                collected.grow(canTake);
                countNeeded -= canTake;
                if (countNeeded <= 0) break;
            }
        }
        // Place collected items into the payment slot
        targetSlot.set(collected);
    }

    /**
     * A custom slot for the payment items.
     * Owners can place items here when creating an offer.
     * Customers can place items here to meet the payment requirements.
     */
    public static class PaymentSlot extends SlotItemHandler {
        public PaymentSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }
    }

    /**
     * A custom slot for the offer's result item.
     * This slot has special logic to handle offer creation and execution.
     * Taking from this slot triggers the trade.
     */
    public static class OfferSlot extends SlotItemHandler {
        private final SmallShopOffersMenu menu;

        public OfferSlot(IItemHandler handler, int slot, int x, int y, SmallShopOffersMenu menu) {
            super(handler, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            // An item can only be placed by the owner if no offer is currently set.
            return !menu.hasFlag(SmallShopBlockEntity.HAS_OFFER_FLAG) && menu.isOwner();
        }

        @Override
        public boolean mayPickup(@NotNull Player player) {
            // The owner can always pick up the item.
            if (menu.isOwner()) return true;
            // Other players can only pick it up if an offer is set and available for purchase.
            return menu.hasFlag(SmallShopBlockEntity.HAS_OFFER_FLAG) && menu.hasFlag(SmallShopBlockEntity.OFFER_AVAILABLE_FLAG);
        }

        @Override
        public @NotNull ItemStack remove(int amount) {
            if (mayPickup(menu.player)) {
                return super.remove(amount);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void set(@NotNull ItemStack stack) {
            // Prevent setting the slot if an offer already exists, to avoid visual glitches.
            if (menu.hasFlag(SmallShopBlockEntity.HAS_OFFER_FLAG) && !stack.isEmpty()) {
                return;
            }
            super.set(stack);
        }

        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            // The actual trade logic is triggered by the ItemStackHandler's extractItem override
            // in the BlockEntity, so we don't need to call performPurchase here.
            super.onTake(player, stack);
            menu.blockEntity.updateOfferSlot();
        }
    }
}