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
        this(id, playerInventory, new SimpleContainer(27), blockEntity, ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, Container container, SmallShopBlockEntity blockEntity, boolean ownerView) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), id);
        checkContainerSize(container, 27);
        this.container = container;
        this.blockEntity = blockEntity;
        this.ownerView = ownerView;
        container.startOpen(playerInventory.player);

        // 3x4 Output slots (0-11)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 4; ++col) {
                int index = col + row * 4;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
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

        // 3x4 Input slots (12-23)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 4; ++col) {
                int index = 12 + col + row * 4;
                int x = 98 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return ownerView && activeTab == 1 && (!hasSaleItem() || isSaleItem(stack));
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

        // Zentrierte Verkaufs- und Bezahl-Slots
        int centeredX = (176 - 3 * 18) / 2;

        // Sale item slot (rechts)
        this.addSlot(new Slot(container, 24, centeredX + 59, 52) {
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
                return !ownerView || activeTab == 0;
            }
        });

        // Payment item slots (links)
        this.addSlot(new Slot(container, 25, centeredX - 25, 52) {
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
                return !ownerView || activeTab == 0;
            }
        });

        this.addSlot(new Slot(container, 26, centeredX + 1, 52) {
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
                return !ownerView || activeTab == 0;
            }
        });

        // Player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = 84 + row * 18;
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Hotbar slots
        for (int col = 0; col < 9; ++col) {
            int x = 8 + col * 18;
            int y = 142;
            this.addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    public void setActiveTab(int tab) {
        this.activeTab = tab;
        broadcastChanges();
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
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(stack, 27, 63, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 27, false)) {
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
        if (!ownerView) {
            // Shift-Klick aus Spielerinventar in Bezahl-Slots
            if (clickType == ClickType.QUICK_MOVE && slotId >= 27 && slotId < this.slots.size()) {
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

            // Klick auf Angebotsslot zum Kauf
            if (slotId == 24) {
                if (clickType == ClickType.QUICK_MOVE) {
                    int trades = calculateMaxTrades(player);
                    if (trades > 0) {
                        buyItem(player, trades);
                    }
                } else if (clickType == ClickType.PICKUP) {
                    buyItem(player);
                }
                return;
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
            moved |= fillPaySlot(stack, 25, blockEntity.getPayItemA());
        }
        if (matchesPayItem(stack, blockEntity.getPayItemB())) {
            moved |= fillPaySlot(stack, 26, blockEntity.getPayItemB());
        }
        return moved;
    }

    private boolean matchesPayItem(ItemStack stack, ItemStack required) {
        return !required.isEmpty() && ItemStack.isSameItemSameComponents(stack, required);
    }

    private boolean hasSaleItem() {
        return blockEntity != null && !blockEntity.getSaleItem().isEmpty();
    }

    private boolean isSaleItem(ItemStack stack) {
        if (!hasSaleItem()) {
            return false;
        }
        ItemStack saleItem = blockEntity.getSaleItem();
        return ItemStack.isSameItemSameComponents(stack, saleItem);
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
        for (int i = 0; i < 24; i++) {
            blockEntity.getInventory().setStackInSlot(i, container.getItem(i));
        }
        blockEntity.setSaleItem(container.getItem(24));
        blockEntity.setPayItemA(container.getItem(25));
        blockEntity.setPayItemB(container.getItem(26));
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
        container.setItem(24, ItemStack.EMPTY);
        container.setItem(25, ItemStack.EMPTY);
        container.setItem(26, ItemStack.EMPTY);
        broadcastChanges();
    }

    private void removeOffer(Player player) {
        if (!ownerView || blockEntity == null) {
            return;
        }
        blockEntity.setSaleItem(ItemStack.EMPTY);
        blockEntity.setPayItemA(ItemStack.EMPTY);
        blockEntity.setPayItemB(ItemStack.EMPTY);
        container.setItem(24, ItemStack.EMPTY);
        container.setItem(25, ItemStack.EMPTY);
        container.setItem(26, ItemStack.EMPTY);
    }

    private void buyItem(Player player) {
        buyItem(player, 1);
    }

    private void buyItem(Player player, int trades) {
        if (blockEntity == null || trades <= 0) {
            return;
        }
        if (!blockEntity.hasStock()) {
            player.sendSystemMessage(Component.translatable("message.marketblocks.small_shop.no_stock"));
            return;
        }
        autoFillPayment(player, trades);
        int executed = 0;
        while (executed < trades) {
            if (!blockEntity.canTrade(container)) {
                if (executed == 0) {
                    player.sendSystemMessage(Component.translatable("message.marketblocks.small_shop.payment_mismatch"));
                }
                break;
            }
            blockEntity.performTrade(player, container);
            executed++;
            broadcastChanges();
        }
    }

    private void autoFillPayment(Player player, int trades) {
        if (blockEntity == null) {
            return;
        }
        moveFromPlayer(player, blockEntity.getPayItemA(), 25, trades);
        moveFromPlayer(player, blockEntity.getPayItemB(), 26, trades);
    }

    private void moveFromPlayer(Player player, ItemStack required, int slotIndex, int trades) {
        if (required.isEmpty()) {
            return;
        }
        ItemStack slotStack = container.getItem(slotIndex);
        if (!slotStack.isEmpty() && !ItemStack.isSameItemSameComponents(slotStack, required)) {
            return;
        }
        int needed = required.getCount() * trades - slotStack.getCount();
        if (needed <= 0) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize() && needed > 0; i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(invStack, required)) {
                int toMove = Math.min(invStack.getCount(), needed);
                if (slotStack.isEmpty()) {
                    slotStack = invStack.split(toMove);
                } else {
                    invStack.shrink(toMove);
                    slotStack.grow(toMove);
                }
                needed -= toMove;
                player.getInventory().setItem(i, invStack);
            }
        }
        container.setItem(slotIndex, slotStack);
    }

    private int calculateMaxTrades(Player player) {
        if (blockEntity == null) {
            return 0;
        }
        ItemStack sale = blockEntity.getSaleItem();
        ItemStack payA = blockEntity.getPayItemA();
        if (sale.isEmpty() || payA.isEmpty()) {
            return 0;
        }

        int stock = 0;
        int perTrade = sale.getCount();
        for (int i = 0; i < blockEntity.getInventory().getSlots(); i++) {
            ItemStack stack = blockEntity.getInventory().getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(stack, sale)) {
                stock += stack.getCount() / perTrade;
            }
        }

        int availableA = container.getItem(25).getCount() + countItem(player, payA);
        int trades = Math.min(stock, availableA / payA.getCount());

        ItemStack payB = blockEntity.getPayItemB();
        if (!payB.isEmpty()) {
            int availableB = container.getItem(26).getCount() + countItem(player, payB);
            trades = Math.min(trades, availableB / payB.getCount());
        }

        int capA = 64 / payA.getCount();
        trades = Math.min(trades, capA);
        if (!blockEntity.getPayItemB().isEmpty()) {
            int capB = 64 / blockEntity.getPayItemB().getCount();
            trades = Math.min(trades, capB);
        }
        return trades;
    }

    private int countItem(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(invStack, stack)) {
                total += invStack.getCount();
            }
        }
        return total;
    }

    public ItemStack getSaleItem() {
        return blockEntity != null ? blockEntity.getSaleItem() : ItemStack.EMPTY;
    }

    public ItemStack getPayItemA() {
        return blockEntity != null ? blockEntity.getPayItemA() : ItemStack.EMPTY;
    }

    public ItemStack getPayItemB() {
        return blockEntity != null ? blockEntity.getPayItemB() : ItemStack.EMPTY;
    }


    public boolean isOwnerView() {
        return ownerView;
    }
}