package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menü für den Angebots-Modus des SmallShop
 */
public class SmallShopOffersMenu extends AbstractSmallShopMenu  {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler paymentHandler;
    private final IItemHandler offerHandler;

    private static final int PAYMENT_SLOTS = 2;
    private static final int OFFER_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = PAYMENT_SLOTS + OFFER_SLOT;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;

    private final ContainerData data;

    // Constructor für Server
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_OFFERS_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.paymentHandler = blockEntity.getPaymentHandler();
        this.offerHandler = blockEntity.getOfferHandler();
        this.data = blockEntity.createMenuFlags(playerInventory.player);

        addDataSlots(this.data);
        setupSlots(playerInventory);

        if (!playerInventory.player.level().isClientSide() && blockEntity.getOwnerId() == null) {
            blockEntity.setOwner(playerInventory.player);
        }
    }

    // Constructor für Client
    public SmallShopOffersMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buf));
    }

    private void setupSlots(Inventory playerInventory) {
        // Payment Slots
        addSlot(new PaymentSlot(paymentHandler, 0, 36, 52));
        addSlot(new PaymentSlot(paymentHandler, 1, 62, 52));

        // Offer Result Slot
        addSlot(new OfferSlot(offerHandler, 0, 120, 52, this));

        super.addPlayerInventory(playerInventory, GuiConstants.PLAYER_INV_Y_START);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == 2) {
            Slot slot = this.slots.get(index);
            if (!slot.hasItem()) {
                return ItemStack.EMPTY;
            }

            ItemStack stackInSlot = slot.getItem();
            ItemStack result = stackInSlot.copy();
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            blockEntity.performPurchase();
            blockEntity.updateOfferSlot();

            slot.onTake(player, stackInSlot);
            return result;
        }

        if (index >= PLAYER_INVENTORY_START && index < this.slots.size()) {
            Slot slot = this.slots.get(index);
            if (slot.hasItem()) {
                ItemStack stackInSlot = slot.getItem();
                ItemStack ret = stackInSlot.copy();
                if (this.moveItemStackTo(stackInSlot, 0, PAYMENT_SLOTS, false)) {
                    if (stackInSlot.isEmpty()) {
                        slot.setByPlayer(ItemStack.EMPTY);
                    } else {
                        slot.setChanged();
                    }
                    if (stackInSlot.getCount() == ret.getCount()) {
                        return ItemStack.EMPTY;
                    }

                    slot.onTake(player, stackInSlot);
                    return ret;
                }
            }
        }

        return transferStack(player, index, PLAYER_INVENTORY_START, HOTBAR_START);
    }

    public void autoFillPaymentSlots(ItemStack required1, ItemStack required2) {
        clearPaymentSlots();

        if (!required1.isEmpty()) {
            moveFromInventoryToPaymentSlot(0, required1);
        }
        if (!required2.isEmpty()) {
            moveFromInventoryToPaymentSlot(1, required2);
        }
    }

    private void clearPaymentSlots() {
        ItemStack stack0 = this.slots.get(0).getItem();
        if (!stack0.isEmpty()) {
            if (this.moveItemStackTo(stack0, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                this.slots.get(0).set(stack0.isEmpty() ? ItemStack.EMPTY : stack0);
            }
        }

        ItemStack stack1 = this.slots.get(1).getItem();
        if (!stack1.isEmpty()) {
            if (this.moveItemStackTo(stack1, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                this.slots.get(1).set(stack1.isEmpty() ? ItemStack.EMPTY : stack1);
            }
        }
    }

    private void moveFromInventoryToPaymentSlot(int paymentSlotIndex, ItemStack required) {
        for (int i = PLAYER_INVENTORY_START; i < this.slots.size(); i++) {
            ItemStack inventoryStack = this.slots.get(i).getItem();
            if (!inventoryStack.isEmpty() && ItemStack.isSameItemSameComponents(inventoryStack, required)) {
                ItemStack currentPaymentStack = this.slots.get(paymentSlotIndex).getItem();

                if (currentPaymentStack.isEmpty() || ItemStack.isSameItemSameComponents(inventoryStack, currentPaymentStack)) {
                    int maxStackSize = Math.min(inventoryStack.getMaxStackSize(), this.slots.get(paymentSlotIndex).getMaxStackSize());
                    int spaceInPaymentSlot = maxStackSize - currentPaymentStack.getCount();
                    int amountToTransfer = Math.min(spaceInPaymentSlot, inventoryStack.getCount());

                    if (amountToTransfer > 0) {
                        ItemStack newPaymentStack = inventoryStack.copyWithCount(currentPaymentStack.getCount() + amountToTransfer);
                        inventoryStack.shrink(amountToTransfer);
                        this.slots.get(i).set(inventoryStack.isEmpty() ? ItemStack.EMPTY : inventoryStack);
                        this.slots.get(paymentSlotIndex).set(newPaymentStack);

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

    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean hasOffer() {
        return (data.get(0) & 1) != 0;
    }

    public boolean isOfferAvailable() {
        return (data.get(0) & 2) != 0;
    }

    public boolean isOwner() {
        return (data.get(0) & 4) != 0;
    }

    public static class PaymentSlot extends SlotItemHandler {
        public PaymentSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return true;
        }
    }

    public static class OfferSlot extends SlotItemHandler {
        private final SmallShopOffersMenu menu;

        public OfferSlot(IItemHandler handler, int slot, int x, int y, SmallShopOffersMenu menu) {
            super(handler, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (menu.hasOffer()) return false;
            return menu.isOwner();
        }

        @Override
        public boolean mayPickup(Player player) {
            if (menu.isOwner()) return true;
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
            if (menu.hasOffer() && !stack.isEmpty()) {
                return;
            }
            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            menu.blockEntity.updateOfferSlot();
        }
    }
}