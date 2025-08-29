package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class SmallShopSettingsMenu extends AbstractSmallShopMenu implements ShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final EnumMap<Direction, SideMode> sideModes;
    private final EnumMap<Direction, SideMode> initialModes;
    private final net.minecraft.world.inventory.ContainerData data;

    // Server constructor
    public SmallShopSettingsMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_CONFIG_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.sideModes = new EnumMap<>(Direction.class);
        this.initialModes = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.values()) {
            SideMode mode = blockEntity.getMode(dir);
            sideModes.put(dir, mode);
            initialModes.put(dir, mode);
        }
        this.data = blockEntity.createMenuFlags(playerInventory.player);
        addDataSlots(this.data);
        initSlots(playerInventory);
    }

    // Client constructor
    public SmallShopSettingsMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buf));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    public void setMode(Direction dir, SideMode mode) {
        sideModes.put(dir, mode);
    }

    public void resetModes() {
        sideModes.clear();
        sideModes.putAll(initialModes);
    }

    @Override
    public boolean isOwner() {
        return (data.get(0) & SmallShopBlockEntity.OWNER_FLAG) != 0;
    }

    public Map<UUID, String> getAdditionalOwners() {
        return blockEntity.getAdditionalOwners();
    }

    @Override
    protected void addCustomSlots(Inventory playerInventory) {}

    @Override
    protected boolean showPlayerInventory() {
        return false;
    }
}