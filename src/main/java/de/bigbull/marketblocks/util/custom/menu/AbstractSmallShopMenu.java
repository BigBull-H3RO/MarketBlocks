package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSmallShopMenu extends AbstractContainerMenu {
    protected static final int PLAYER_INV_SLOTS = 27;
    protected static final int HOTBAR_SLOTS = 9;

    protected AbstractSmallShopMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    protected static SmallShopBlockEntity readBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);

        if (be instanceof SmallShopBlockEntity shopEntity) {
            return shopEntity;
        }

        return new SmallShopBlockEntity(pos, RegistriesInit.SMALL_SHOP_BLOCK.get().defaultBlockState());
    }

    protected void addPlayerInventory(Inventory playerInventory, int startY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        int hotbarY = startY + 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    protected ItemStack quickMoveStack(Player player, int index, int containerSlots, int insertSlots) {
        int playerInvStart = containerSlots;
        int hotbarStart = playerInvStart + PLAYER_INV_SLOTS;

        if (index >= playerInvStart && index < this.slots.size()) {
            Slot slot = this.slots.get(index);
            if (slot.hasItem()) {
                ItemStack stack = slot.getItem();
                ItemStack ret = stack.copy();
                if (this.moveItemStackTo(stack, 0, insertSlots, false)) {
                    if (stack.isEmpty()) {
                        slot.setByPlayer(ItemStack.EMPTY);
                    } else {
                        slot.setChanged();
                    }

                    if (stack.getCount() != ret.getCount()) {
                        slot.onTake(player, stack);
                        return ret;
                    }

                    return ItemStack.EMPTY;
                }
            }
        }

        return transferStack(player, index, playerInvStart, hotbarStart);
    }

    protected ItemStack transferStack(Player player, int index, int containerEnd, int hotbarStart) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack ret = stack.copy();

        if (index < containerEnd) {
            if (!this.moveItemStackTo(stack, containerEnd, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (index < hotbarStart) {
                if (!this.moveItemStackTo(stack, hotbarStart, hotbarStart + 9, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, containerEnd, hotbarStart, false)) {
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
}