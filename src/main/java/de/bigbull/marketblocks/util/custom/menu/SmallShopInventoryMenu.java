package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Menü für den Inventar-Modus des SmallShop
 */
public class SmallShopInventoryMenu extends AbstractContainerMenu {
    private final SmallShopBlockEntity blockEntity;
    private final Level level;
    private final Container container;

    // Slot-Indizes für Inventory-Modus
    private static final int INPUT_SLOTS = 12; // 3x4 Input Inventar
    private static final int OUTPUT_SLOTS = 12; // 3x4 Output Inventar
    private static final int PLAYER_INVENTORY_START = INPUT_SLOTS + OUTPUT_SLOTS;
    private static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;

    // Data Slots für Client-Server Sync
    private final ContainerData data;

    // Constructor für Server
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, SmallShopBlockEntity blockEntity) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.container = blockEntity;

        this.data = new SimpleContainerData(6) {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.hasOffer() ? 1 : 0;
                    case 1 -> blockEntity.isOfferAvailable() ? 1 : 0;
                    case 2 -> blockEntity.isOwner(playerInventory.player) ? 1 : 0;
                    case 3 -> blockEntity.getOwnerId() != null ? 1 : 0;
                    case 4 -> 0; // Reserviert für weitere Flags
                    case 5 -> 0; // Reserviert für weitere Flags
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Data wird vom Server gesteuert
            }
        };

        addDataSlots(this.data);

        // Setup der Slots
        setupSlots(playerInventory);

        // Owner setzen falls noch nicht gesetzt
        if (blockEntity.getOwnerId() == null) {
            blockEntity.setOwner(playerInventory.player);
        }
    }

    // Constructor für Client - FIXED VERSION
    public SmallShopInventoryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(RegistriesInit.SMALL_SHOP_INVENTORY_MENU.get(), containerId);

        // BlockPos aus Buffer lesen
        BlockPos pos = buf.readBlockPos();

        // Versuche BlockEntity aus der Welt zu bekommen
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof SmallShopBlockEntity shopEntity) {
            this.blockEntity = shopEntity;
        } else {
            // Fallback: Dummy BlockEntity erstellen (sollte nicht passieren)
            this.blockEntity = new SmallShopBlockEntity(pos, RegistriesInit.SMALL_SHOP_BLOCK.get().defaultBlockState());
        }

        this.level = playerInventory.player.level();
        this.container = this.blockEntity;

        this.data = new SimpleContainerData(6);
        addDataSlots(this.data);

        // Setup der Slots
        setupSlots(playerInventory);
    }

    // Alternative: Statische Factory-Methode für Client-Constructor
    public static SmallShopInventoryMenu createClientMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof SmallShopBlockEntity shopEntity) {
            return new SmallShopInventoryMenu(containerId, playerInventory, shopEntity);
        }
        // Fallback - erstelle Dummy Entity
        SmallShopBlockEntity dummy = new SmallShopBlockEntity(pos, RegistriesInit.SMALL_SHOP_BLOCK.get().defaultBlockState());
        return new SmallShopInventoryMenu(containerId, playerInventory, dummy);
    }

    private void setupSlots(Inventory playerInventory) {
        // Input Inventar (3x4 = 12 Slots) - Slots 0-11
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new InputSlot(container, row * 3 + col, 8 + col * 18, 18 + row * 18));
            }
        }

        // Output Inventar (3x4 = 12 Slots) - Slots 12-23
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new OutputSlot(container, INPUT_SLOTS + row * 3 + col,
                        116 + col * 18, 18 + row * 18));
            }
        }

        // Spieler Inventar - Slots 24-50
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18,
                        GuiConstants.PLAYER_INV_Y_START + row * 18));
            }
        }

        // Spieler Hotbar - Slots 51-59
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, GuiConstants.HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Von Container zu Spieler
            if (index < PLAYER_INVENTORY_START) {
                if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Von Spieler zu Container
            else if (index >= PLAYER_INVENTORY_START) {
                // Versuche in Input-Slots zu verschieben (Owner kann in Input, andere können nicht)
                boolean canMoveToInput = blockEntity.isOwner(player);
                if (canMoveToInput && !this.moveItemStackTo(itemstack1, 0, INPUT_SLOTS, false)) {
                    if (index < HOTBAR_START) {
                        // Von Inventar zu Hotbar
                        if (!this.moveItemStackTo(itemstack1, HOTBAR_START, HOTBAR_START + 9, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        // Von Hotbar zu Inventar
                        if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (!canMoveToInput) {
                    // Nicht-Owner können nur zwischen eigenem Inventar verschieben
                    if (index < HOTBAR_START) {
                        if (!this.moveItemStackTo(itemstack1, HOTBAR_START, HOTBAR_START + 9, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    // Getter für UI
    public SmallShopBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean hasOffer() {
        return data.get(0) == 1;
    }

    public boolean isOfferAvailable() {
        return data.get(1) == 1;
    }

    public boolean isOwner() {
        return data.get(2) == 1;
    }

    public boolean hasOwner() {
        return data.get(3) == 1;
    }

    // Custom Slot Klassen
    public static class InputSlot extends Slot {
        private final SmallShopBlockEntity blockEntity;

        public InputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.blockEntity = container instanceof SmallShopBlockEntity ? (SmallShopBlockEntity) container : null;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Nur Owner kann Items in Input-Slots platzieren
            if (blockEntity != null) {
                // Hier müssten wir den aktuellen Player ermitteln - das ist etwas tricky
                // Alternative: Überprüfung in der Screen-Klasse
                return true; // Vorerst erlauben, Überprüfung erfolgt anderswo
            }
            return false;
        }
    }

    public static class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; // Output-Slots akzeptieren keine Items vom Spieler
        }
    }
}