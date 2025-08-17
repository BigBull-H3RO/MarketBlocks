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
    private static final int OFFER_SLOTS = 1;
    private static final int TOTAL_SLOTS = PAYMENT_SLOTS + OFFER_SLOTS;
    private static final int OFFER_SLOT_INDEX = PAYMENT_SLOTS;

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
        blockEntity.ensureOwner(playerInventory.player);
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
        if (index == OFFER_SLOT_INDEX) {
            Slot slot = this.slots.get(index);
            if (!slot.hasItem()) {
                return ItemStack.EMPTY;
            }

            ItemStack stackInSlot = slot.getItem();
            ItemStack result = stackInSlot.copy();
            if (!this.moveItemStackTo(stackInSlot, TOTAL_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            blockEntity.performPurchase();
            blockEntity.updateOfferSlot();

            slot.onTake(player, stackInSlot);
            return result;
        }

        return super.quickMoveStack(player, index, TOTAL_SLOTS, PAYMENT_SLOTS);
    }

    public void fillPaymentSlots(ItemStack... required) {
        int playerInventoryStart = TOTAL_SLOTS;

        for (int paymentSlotIndex = 0; paymentSlotIndex < PAYMENT_SLOTS; paymentSlotIndex++) {
            ItemStack stackInSlot = this.slots.get(paymentSlotIndex).getItem();
            if (!stackInSlot.isEmpty()) {
                if (this.moveItemStackTo(stackInSlot, playerInventoryStart, this.slots.size(), true)) {
                    this.slots.get(paymentSlotIndex).set(stackInSlot.isEmpty() ? ItemStack.EMPTY : stackInSlot);
                }
            }

            if (paymentSlotIndex >= required.length) {
                continue;
            }

            ItemStack requiredStack = required[paymentSlotIndex];
            if (requiredStack.isEmpty()) {
                continue;
            }

            for (int i = playerInventoryStart; i < this.slots.size(); i++) {
                ItemStack inventoryStack = this.slots.get(i).getItem();
                if (!inventoryStack.isEmpty() && ItemStack.isSameItemSameComponents(inventoryStack, requiredStack)) {
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
            return net.minecraft.world.item.ItemStack.EMPTY;
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