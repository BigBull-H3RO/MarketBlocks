package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SmallShopSettingsMenu extends AbstractSmallShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final SideMode initialLeft, initialRight, initialBottom, initialBack;
    private SideMode left, right, bottom, back;
    private final net.minecraft.world.inventory.ContainerData data;

    // Server constructor
    public SmallShopSettingsMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_CONFIG_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.left = blockEntity.getLeftMode();
        this.right = blockEntity.getRightMode();
        this.bottom = blockEntity.getBottomMode();
        this.back = blockEntity.getBackMode();
        this.initialLeft = this.left;
        this.initialRight = this.right;
        this.initialBottom = this.bottom;
        this.initialBack = this.back;
        this.data = blockEntity.createMenuFlags(playerInventory.player);
        addDataSlots(this.data);
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

    public SideMode getLeft() { return left; }
    public SideMode getRight() { return right; }
    public SideMode getBottom() { return bottom; }
    public SideMode getBack() { return back; }

    public void setLeft(SideMode mode) { left = mode; }
    public void setRight(SideMode mode){ right = mode; }
    public void setBottom(SideMode mode){ bottom = mode; }
    public void setBack(SideMode mode){ back = mode; }

    public void resetModes() {
        left = initialLeft;
        right = initialRight;
        bottom = initialBottom;
        back = initialBack;
    }

    public boolean isOwner() {
        return (data.get(0) & 4) != 0;
    }
}