package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Basisklasse für kleine Shop-Screens mit Tab-Unterstützung.
 */
public abstract class AbstractSmallShopScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/offers.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/inventory.png");

    protected AbstractSmallShopScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    /**
     * Erstellt die Tab-Buttons für Angebot und Inventar.
     *
     * @param x             x-Position der Buttons
     * @param y             y-Position des ersten Buttons
     * @param offersSelected {@code true}, wenn die Angebots-Ansicht aktiv ist
     * @param onOffers      Aktion beim Wechsel zur Angebots-Ansicht
     * @param onInventory   Aktion beim Wechsel zur Inventar-Ansicht
     */
    protected void createTabButtons(int x, int y, boolean offersSelected, Runnable onOffers, Runnable onInventory) {
        IconButton offersButton = new IconButton(
                x, y, 24, 24,
                BUTTON_SPRITES, OFFERS_ICON,
                b -> { if (!offersSelected) onOffers.run(); },
                Component.translatable("gui.marketblocks.offers_tab"),
                () -> offersSelected
        );

        IconButton inventoryButton = new IconButton(
                x, y + 28, 24, 24,
                BUTTON_SPRITES, INVENTORY_ICON,
                b -> { if (offersSelected) onInventory.run(); },
                Component.translatable("gui.marketblocks.inventory_tab"),
                () -> !offersSelected
        );

        addRenderableWidget(offersButton);
        addRenderableWidget(inventoryButton);
    }
}