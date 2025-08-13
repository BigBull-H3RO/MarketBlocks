package de.bigbull.marketblocks.util.custom.menu;

import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/**
 * Hilfsklasse zur Erstellung der {@link ContainerData}-Instanz für SmallShop-Menüs.
 * <p>
 * Kapselt die Logik für die Menü-Flags und ermöglicht eine einheitliche Nutzung
 * in allen zukünftigen SmallShop-Menüs.
 * </p>
 */
public final class SmallShopMenuData {

    private SmallShopMenuData() {
    }

    /**
     * Erstellt eine standardisierte {@link ContainerData}-Implementierung für SmallShop-Menüs.
     * <p>
     * Auf dem Server werden die Werte dynamisch aus dem {@link SmallShopBlockEntity}
     * und dem übergebenen {@link Player} abgeleitet. Auf dem Client dient die Instanz
     * als Platzhalter für die synchronisierten Werte vom Server.
     * </p>
     *
     * @param blockEntity Der zugehörige SmallShop-Block
     * @param player      Der Spieler, für den das Menü geöffnet wird
     * @return Eine {@code ContainerData}-Instanz mit einheitlicher SmallShop-Logik
     */
    public static ContainerData create(SmallShopBlockEntity blockEntity, Player player) {
        if (blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide) {
            // Client: Werte werden vom Server synchronisiert
            return new SimpleContainerData(5);
        }

        // Server: Werte dynamisch berechnen
        return new SimpleContainerData(5) {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> blockEntity.hasOffer() ? 1 : 0;
                    case 1 -> blockEntity.isOfferAvailable() ? 1 : 0;
                    case 2 -> blockEntity.isOwner(player) ? 1 : 0;
                    case 3 -> 0; // Reserviert für weitere Flags
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Data wird vom Server gesteuert
            }
        };
    }
}