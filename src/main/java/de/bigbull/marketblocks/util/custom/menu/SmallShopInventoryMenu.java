package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menü für den Inventar-Modus des SmallShop
 */
public class SmallShopInventoryMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final IItemHandler inputHandler;
    private final IItemHandler outputHandler;

    private static final int INPUT_SLOTS = 12; // 3x4 Input Inventar
    private static final int OUTPUT_SLOTS = 12; // 3x4 Output Inventar
    private static final int PLAYER_INVENTORY_START = INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;

    private final ContainerData data;

    private SmallShopInventoryMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity, boolean init) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.inputHandler = blockEntity.getInputHandler();
        this.outputHandler = blockEntity.getOutputHandler();
        this.data = SmallShopMenuData.create(blockEntity, playerInventory.player);

        addDataSlots(this.data);
        setupSlots(playerInventory);
    }

    // Constructor für Server
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, true);

        if (blockEntity.getOwnerId() == null) {
            blockEntity.setOwner(playerInventory.player);
        }
    }

    // Constructor für Client
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buf), true);
    }

    private static SmallShopBlockEntity readBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);

        if (be instanceof SmallShopBlockEntity shopEntity) {
            return shopEntity;
        }

        return new SmallShopBlockEntity(pos, RegistriesInit.SMALL_SHOP_BLOCK.get().defaultBlockState());
    }

    private void setupSlots(Inventory playerInventory) {
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

        // Player Inventar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18,
                        GuiConstants.PLAYER_INV_Y_START + row * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, GuiConstants.HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        boolean isOwner = blockEntity.isOwner(player);
        return QuickMoveHelper.quickMoveStack(this, player, index,
                PLAYER_INVENTORY_START, HOTBAR_START,
                isOwner, 0, INPUT_SLOTS,
                this::moveItemStackTo);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity.stillValid(player);
    }

    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isOwner() {
        return data.get(2) == 1;
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