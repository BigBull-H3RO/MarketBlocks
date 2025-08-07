package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallShopMenu extends AbstractContainerMenu {
    private final Container container;
    private final SmallShopBlockEntity blockEntity;
    private final boolean ownerView;
    private int activeTab = 0;

    public static final int BUTTON_CONFIRM = 0;
    public static final int BUTTON_BUY = 1;
    public static final int BUTTON_REMOVE = 2;
    public static final int BUTTON_TAB_OFFER = 3;
    public static final int BUTTON_TAB_STORAGE = 4;

    public SmallShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, true);
    }

    public SmallShopMenu(int id, Inventory playerInventory, boolean ownerView) {
        this(id, playerInventory, null, ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, SmallShopBlockEntity blockEntity, boolean ownerView) {
        this(id, playerInventory, new SimpleContainer(21), blockEntity, ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, Container container, SmallShopBlockEntity blockEntity, boolean ownerView) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), id);
        checkContainerSize(container, 21);
        this.container = container;
        this.blockEntity = blockEntity;
        this.ownerView = ownerView;
        container.startOpen(playerInventory.player);

        // 3x3 Output slots (0-8)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                int index = col + row * 3;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return ownerView && activeTab == 1;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return ownerView && activeTab == 1;
                    }

                    @Override
                    public boolean isActive() {
                        return ownerView && activeTab == 1;
                    }
                });
            }
        }

        // 3x3 Input slots (9-17)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                int index = 9 + col + row * 3;
                int x = 80 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return ownerView && activeTab == 1;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return ownerView && activeTab == 1;
                    }

                    @Override
                    public boolean isActive() {
                        return ownerView && activeTab == 1;
                    }
                });
            }
        }

        // Sale item slot
        this.addSlot(new Slot(container, 18, 134, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ownerView && activeTab == 0;
            }

            @Override
            public boolean mayPickup(Player player) {
                return ownerView && activeTab == 0;
            }

            @Override
            public boolean isActive() {
                return ownerView ? activeTab == 0 : true;
            }
        });

        // Payment item slots
        this.addSlot(new Slot(container, 19, 134, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true;
            }

            @Override
            public boolean isActive() {
                return ownerView ? activeTab == 0 : true;
            }
        });

        this.addSlot(new Slot(container, 20, 134, 72) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true;
            }

            @Override
            public boolean isActive() {
                return ownerView ? activeTab == 0 : true;
            }
        });

        // Player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = 86 + row * 18;
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Hotbar slots
        for (int col = 0; col < 9; ++col) {
            int x = 8 + col * 18;
            int y = 144;
            this.addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    public void setActiveTab(int tab) {
        this.activeTab = tab;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null) {
            return player.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= 64;
        }
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 21) {
                if (!this.moveItemStackTo(stack, 21, 57, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 21, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (!ownerView && clickType == ClickType.QUICK_MOVE && slotId >= 21 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (moveToPaySlots(stack)) {
                    slot.set(stack);
                    slot.setChanged();
                    broadcastChanges();
                    return;
                }
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    private boolean moveToPaySlots(ItemStack stack) {
        if (blockEntity == null) {
            return false;
        }
        boolean moved = false;
        if (matchesPayItem(stack, blockEntity.getPayItemA())) {
            moved |= fillPaySlot(stack, 19, blockEntity.getPayItemA());
        }
        if (matchesPayItem(stack, blockEntity.getPayItemB())) {
            moved |= fillPaySlot(stack, 20, blockEntity.getPayItemB());
        }
        return moved;
    }

    private boolean matchesPayItem(ItemStack stack, ItemStack required) {
        return !required.isEmpty() && ItemStack.isSameItemSameComponents(stack, required);
    }

    private boolean fillPaySlot(ItemStack from, int slotIndex, ItemStack required) {
        ItemStack slotStack = container.getItem(slotIndex);
        if (!slotStack.isEmpty() && !ItemStack.isSameItemSameComponents(slotStack, required)) {
            return false;
        }
        int needed = required.getCount() - slotStack.getCount();
        if (needed <= 0) {
            return false;
        }
        int toMove = Math.min(from.getCount(), needed);
        if (slotStack.isEmpty()) {
            container.setItem(slotIndex, from.split(toMove));
        } else {
            from.shrink(toMove);
            slotStack.grow(toMove);
            container.setItem(slotIndex, slotStack);
        }
        return toMove > 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_CONFIRM) {
            confirmOffer(player);
            return true;
        }
        if (id == BUTTON_BUY) {
            buyItem(player);
            return true;
        }
        if (id == BUTTON_REMOVE) {
            removeOffer(player);
            return true;
        }
        if (id == BUTTON_TAB_OFFER) {
            setActiveTab(0);
            return true;
        }
        if (id == BUTTON_TAB_STORAGE) {
            setActiveTab(1);
            return true;
        }
        return false;
    }

    private void confirmOffer(Player player) {
        if (!ownerView || blockEntity == null) {
            return;
        }
        for (int i = 0; i < 18; i++) {
            blockEntity.getInventory().setStackInSlot(i, container.getItem(i));
        }
        blockEntity.setSaleItem(container.getItem(18));
        blockEntity.setPayItemA(container.getItem(19));
        blockEntity.setPayItemB(container.getItem(20));
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    private void removeOffer(Player player) {
        if (!ownerView || blockEntity == null) {
            return;
        }
        blockEntity.setSaleItem(ItemStack.EMPTY);
        blockEntity.setPayItemA(ItemStack.EMPTY);
        blockEntity.setPayItemB(ItemStack.EMPTY);
        container.setItem(18, ItemStack.EMPTY);
        container.setItem(19, ItemStack.EMPTY);
        container.setItem(20, ItemStack.EMPTY);
    }

    private void buyItem(Player player) {
        if (blockEntity == null) {
            return;
        }
        if (!blockEntity.hasStock()) {
            player.sendSystemMessage(Component.translatable("message.marketblocks.small_shop.no_stock"));
            return;
        }
        autoFillPayment(player);
        if (!blockEntity.canTrade(container)) {
            player.sendSystemMessage(Component.translatable("message.marketblocks.small_shop.payment_mismatch"));
            return;
        }
        blockEntity.performTrade(player, container);
        broadcastChanges();
    }

    private void autoFillPayment(Player player) {
        if (blockEntity == null) {
            return;
        }
        moveFromPlayer(player, blockEntity.getPayItemA(), 19);
        moveFromPlayer(player, blockEntity.getPayItemB(), 20);
    }

    private void moveFromPlayer(Player player, ItemStack required, int slotIndex) {
        if (required.isEmpty()) {
            return;
        }
        ItemStack slotStack = container.getItem(slotIndex);
        if (!slotStack.isEmpty() && !ItemStack.isSameItemSameComponents(slotStack, required)) {
            return;
        }
        int needed = required.getCount() - slotStack.getCount();
        if (needed <= 0) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize() && needed > 0; i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(invStack, required)) {
                int toMove = Math.min(invStack.getCount(), needed);
                if (slotStack.isEmpty()) {
                    slotStack = invStack.split(toMove);
                    container.setItem(slotIndex, slotStack);
                } else {
                    invStack.shrink(toMove);
                    slotStack.grow(toMove);
                    container.setItem(slotIndex, slotStack);
                }
                needed -= toMove;
                player.getInventory().setItem(i, invStack);
            }
        }
    }

    public boolean isOwnerView() {
        return ownerView;
    }
}