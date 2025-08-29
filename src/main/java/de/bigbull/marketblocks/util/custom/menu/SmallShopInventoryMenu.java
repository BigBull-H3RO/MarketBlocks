package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menü für den Inventar-Modus des SmallShop
 */
public class SmallShopInventoryMenu extends AbstractSmallShopMenu implements ShopMenu  {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;

    private static final int INPUT_SLOTS = 12;
    private static final int OUTPUT_SLOTS = 12;

    private final ContainerData data;

    // Constructor für Server
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.inputHandler = blockEntity.getInputHandler();
        this.outputHandler = blockEntity.getOutputHandler();
        this.data = blockEntity.createMenuFlags(playerInventory.player);

        addDataSlots(this.data);
        initSlots(playerInventory);
        blockEntity.ensureOwner(playerInventory.player);
    }

    // Constructor für Client
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buf));
    }

    @Override
    protected void addCustomSlots(Inventory playerInventory) {
        // Input Inventar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new InputSlot(blockEntity, inputHandler, row * 4 + col, 8 + col * 18, 18 + row * 18, playerInventory.player));
            }
        }

        // Output Inventar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new OutputSlot(outputHandler, row * 4 + col,
                        98 + col * 18, 18 + row * 18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSlots = INPUT_SLOTS + OUTPUT_SLOTS;
        return super.quickMoveStack(player, index, containerSlots, isOwner() ? INPUT_SLOTS : 0);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    @Override
    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean isOwner() {
        return (data.get(0) & SmallShopBlockEntity.OWNER_FLAG) != 0;
    }

    public static class InputSlot extends SlotItemHandler {
        private final SmallShopBlockEntity blockEntity;
        private final Player player;

        public InputSlot(SmallShopBlockEntity blockEntity, IItemHandler handler, int slot, int x, int y, Player player) {
            super(handler, slot, x, y);
            this.blockEntity = blockEntity;
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return blockEntity.isOwner(player);
        }

        @Override
        public boolean mayPickup(Player player) {
            return blockEntity.isOwner(player);
        }
    }

    public static class OutputSlot extends SlotItemHandler {
        public OutputSlot(IItemHandler handler, int slot, int x, int y) {
            super(handler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}