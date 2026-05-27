package de.bigbull.marketblocks.feature.singleoffer.menu;

import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.client.gui.GuiConstants;
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

public abstract class AbstractSingleOfferShopMenu extends AbstractContainerMenu {
    protected static final int PLAYER_INV_SLOTS = 27;
    protected static final int HOTBAR_SLOTS = 9;

    protected AbstractSingleOfferShopMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    protected static @Nullable SingleOfferShopBlockEntity readBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);

        if (be instanceof SingleOfferShopBlockEntity shopEntity) {
            return shopEntity;
        }

        return null;
    }

    protected abstract void addPlayerInventory(Inventory playerInventory, int startY);

    protected final void initSlots(Inventory playerInventory) {
        addCustomSlots(playerInventory);
        if (showPlayerInventory()) {
            addPlayerInventory(playerInventory, GuiConstants.PLAYER_INV_Y_START);
        }
    }

    protected boolean showPlayerInventory() {
        return true;
    }

    protected abstract void addCustomSlots(Inventory playerInventory);
}

