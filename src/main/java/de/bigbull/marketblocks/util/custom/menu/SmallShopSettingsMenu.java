package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SmallShopSettingsMenu extends AbstractSmallShopMenu {
    private final SmallShopBlockEntity blockEntity;

    // Server constructor
    public SmallShopSettingsMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_CONFIG_MENU.get(), containerId);
        this.blockEntity = blockEntity;
    }

    // Client constructor
    public SmallShopSettingsMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buf));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isOwner() {
        return true;
    }
}