package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallShopMenu extends AbstractContainerMenu {
    private final Container container;

    public SmallShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, true);
    }

    public SmallShopMenu(int id, Inventory playerInventory, boolean ownerView) {
        this(id, playerInventory, new SimpleContainer(11), ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, Container container, boolean ownerView) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), id);
        checkContainerSize(container, 11);
        this.container = container;
        container.startOpen(playerInventory.player);

        // 3x3 inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                int index = col + row * 3;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new Slot(container, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return ownerView;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return ownerView;
                    }
                });
            }
        }

        // Sale item slot
        this.addSlot(new Slot(container, 9, 134, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ownerView;
            }

            @Override
            public boolean mayPickup(Player player) {
                return ownerView;
            }
        });

        // Payment item slot
        this.addSlot(new Slot(container, 10, 134, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ownerView;
            }

            @Override
            public boolean mayPickup(Player player) {
                return ownerView;
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

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 11) {
                if (!this.moveItemStackTo(stack, 11, 47, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 11, false)) {
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
}