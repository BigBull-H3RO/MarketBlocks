package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SideMode;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * The menu for the "Settings" tab of the Small Shop.
 * This menu is unique as it contains no item slots. Its primary purpose is to hold
 * the client-side state of the settings while the user is editing them, before they are
 * sent to the server via a packet.
 */
public class SmallShopSettingsMenu extends AbstractSmallShopMenu implements ShopMenu {
    private final SmallShopBlockEntity blockEntity;
    private final EnumMap<Direction, SideMode> sideModes;
    private final EnumMap<Direction, SideMode> initialModes;
    private final ContainerData data;

    /**
     * Server-side constructor.
     */
    public SmallShopSettingsMenu(int containerId, @NotNull Inventory playerInventory, @NotNull SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_SETTINGS_MENU.get(), containerId);
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

    /**
     * Client-side constructor.
     */
    public SmallShopSettingsMenu(int containerId, @NotNull Inventory playerInventory, @NotNull RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_SETTINGS_MENU.get(), containerId);
        this.blockEntity = readBlockEntity(playerInventory, buf);
        this.data = new SimpleContainerData(1);
        this.sideModes = new EnumMap<>(Direction.class);
        this.initialModes = new EnumMap<>(Direction.class);

        for (Direction dir : Direction.values()) {
            SideMode mode = this.blockEntity.getMode(dir);
            sideModes.put(dir, mode);
            initialModes.put(dir, mode);
        }

        addDataSlots(this.data);
        initSlots(playerInventory);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY; // No slots, no quick-moving.
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public @NotNull SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * Gets the current, potentially unsaved, mode for a given side.
     */
    public SideMode getMode(Direction dir) {
        return sideModes.getOrDefault(dir, SideMode.DISABLED);
    }

    /**
     * Sets the mode for a given side in the local menu state.
     */
    public void setMode(Direction dir, SideMode mode) {
        sideModes.put(dir, mode);
    }

    /**
     * Resets any changes made to the side modes back to their initial state.
     */
    public void resetModes() {
        sideModes.clear();
        sideModes.putAll(initialModes);
    }

    @Override
    public int getFlags() {
        return data.get(0);
    }

    /**
     * Gets the map of additional owners directly from the block entity.
     */
    public Map<UUID, String> getAdditionalOwners() {
        return blockEntity.getAdditionalOwners();
    }

    @Override
    protected void addCustomSlots(@NotNull Inventory playerInventory) {
        // No custom slots in the settings menu.
    }

    @Override
    protected boolean showPlayerInventory() {
        return false; // No player inventory shown in the settings menu.
    }
}