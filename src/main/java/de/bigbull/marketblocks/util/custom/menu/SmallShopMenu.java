package de.bigbull.marketblocks.util.custom.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

public class SmallShopMenu extends AbstractContainerMenu {
    protected SmallShopMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public MenuType<?> getType() {
        return super.getType();
    }

    @Override
    public boolean isValidSlotIndex(int slotIndex) {
        return super.isValidSlotIndex(slotIndex);
    }

    @Override
    protected Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    @Override
    protected DataSlot addDataSlot(DataSlot intValue) {
        return super.addDataSlot(intValue);
    }

    @Override
    protected void addDataSlots(ContainerData array) {
        super.addDataSlots(array);
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
    }

    @Override
    public void setSynchronizer(ContainerSynchronizer synchronizer) {
        super.setSynchronizer(synchronizer);
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
    }

    @Override
    public void removeSlotListener(ContainerListener listener) {
        super.removeSlotListener(listener);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return super.getItems();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }

    @Override
    public void broadcastFullState() {
        super.broadcastFullState();
    }

    @Override
    public void setRemoteSlot(int slot, ItemStack stack) {
        super.setRemoteSlot(slot, stack);
    }

    @Override
    public void setRemoteSlotNoCopy(int slot, ItemStack stack) {
        super.setRemoteSlotNoCopy(slot, stack);
    }

    @Override
    public void setRemoteCarried(ItemStack remoteCarried) {
        super.setRemoteCarried(remoteCarried);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return super.clickMenuButton(player, id);
    }

    @Override
    public Slot getSlot(int slotId) {
        return super.getSlot(slotId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    @Override
    protected void clearContainer(Player player, Container container) {
        super.clearContainer(player, container);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        super.setItem(slotId, stateId, stack);
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        super.initializeContents(stateId, items, carried);
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    protected void resetQuickCraft() {
        super.resetQuickCraft();
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return super.canDragTo(slot);
    }

    @Override
    public void setCarried(ItemStack stack) {
        super.setCarried(stack);
    }

    @Override
    public ItemStack getCarried() {
        return super.getCarried();
    }

    @Override
    public void suppressRemoteUpdates() {
        super.suppressRemoteUpdates();
    }

    @Override
    public void resumeRemoteUpdates() {
        super.resumeRemoteUpdates();
    }

    @Override
    public void transferState(AbstractContainerMenu menu) {
        super.transferState(menu);
    }

    @Override
    public OptionalInt findSlot(Container container, int slotIndex) {
        return super.findSlot(container, slotIndex);
    }

    @Override
    public int getStateId() {
        return super.getStateId();
    }

    @Override
    public int incrementStateId() {
        return super.incrementStateId();
    }
}