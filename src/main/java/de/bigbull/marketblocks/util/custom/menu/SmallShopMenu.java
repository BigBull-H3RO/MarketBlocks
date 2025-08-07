package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallShopMenu extends AbstractContainerMenu {
    private final Container container;
    private final SmallShopBlockEntity blockEntity;
    private final boolean ownerView;

    public static final int BUTTON_CONFIRM = 0;
    public static final int BUTTON_BUY = 1;

    public SmallShopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, true);
    }

    public SmallShopMenu(int id, Inventory playerInventory, boolean ownerView) {
        this(id, playerInventory, null, ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, SmallShopBlockEntity blockEntity, boolean ownerView) {
        this(id, playerInventory, new SimpleContainer(11), blockEntity, ownerView);
    }

    public SmallShopMenu(int id, Inventory playerInventory, Container container, SmallShopBlockEntity blockEntity, boolean ownerView) {
        super(RegistriesInit.SMALL_SHOP_MENU.get(), id);
        checkContainerSize(container, 11);
        this.container = container;
        this.blockEntity = blockEntity;
        this.ownerView = ownerView;
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
        return false;
    }

    private void confirmOffer(Player player) {
        if (!ownerView || blockEntity == null) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            blockEntity.getInventory().setStackInSlot(i, container.getItem(i));
        }
        blockEntity.setSaleItem(container.getItem(9));
        blockEntity.setPayItem(container.getItem(10));
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    private void buyItem(Player player) {
        if (blockEntity == null) {
            return;
        }
        if (!blockEntity.hasStock()) {
            player.sendSystemMessage(Component.literal("Lager ist leer"));
            return;
        }
        if (!blockEntity.canTrade(player)) {
            player.sendSystemMessage(Component.literal("Bezahlung passt nicht"));
            return;
        }
        blockEntity.performTrade(player);
    }

    public boolean isOwnerView() {
        return ownerView;
    }
}