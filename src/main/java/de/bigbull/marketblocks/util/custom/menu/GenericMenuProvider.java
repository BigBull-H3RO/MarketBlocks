package de.bigbull.marketblocks.util.custom.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.function.Supplier;

/**
 * Eine einfache generische MenuProvider-Implementierung, die über einen {@link Supplier}
 * ein neues {@link AbstractContainerMenu} erzeugt und einen anpassbaren Anzeigenamen besitzt.
 */
public record GenericMenuProvider(Component displayName, MenuSupplier menuSupplier) implements MenuProvider {

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return menuSupplier.create(containerId, playerInventory, player);
    }

    @FunctionalInterface
    public interface MenuSupplier extends Supplier<AbstractContainerMenu> {
        AbstractContainerMenu create(int containerId, Inventory playerInventory, Player player);

        @Override
        default AbstractContainerMenu get() {
            return create(0, null, null);
        }
    }
}