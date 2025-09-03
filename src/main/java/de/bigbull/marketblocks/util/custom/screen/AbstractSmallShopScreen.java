package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModLang;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.SwitchTabPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.ShopMenu;
import de.bigbull.marketblocks.util.custom.menu.ShopTab;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * An abstract base class for all screens related to the Small Shop.
 * It provides shared functionality for tab navigation, owner info rendering,
 * and a non-standard (but safe) mouse position restoration when switching between tabs.
 *
 * @param <T> The type of the menu associated with this screen.
 */
public abstract class AbstractSmallShopScreen<T extends AbstractContainerMenu & ShopMenu> extends AbstractContainerScreen<T> {
    protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            MarketBlocks.id("textures/gui/button/button.png"),
            MarketBlocks.id("textures/gui/button/button_disabled.png"),
            MarketBlocks.id("textures/gui/button/button_highlighted.png"),
            MarketBlocks.id("textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation OFFERS_ICON = MarketBlocks.id("textures/gui/icon/offers.png");
    private static final ResourceLocation INVENTORY_ICON = MarketBlocks.id("textures/gui/icon/inventory.png");
    private static final ResourceLocation SETTINGS_ICON = MarketBlocks.id("textures/gui/icon/settings.png");

    private static final int OWNER_INFO_COLOR = 0x404040;

    private boolean lastIsOwner;

    protected AbstractSmallShopScreen(@NotNull T menu, @NotNull Inventory inv, @NotNull Component title) {
        super(menu, inv, title);
    }

    /**
     * A required check for the screen to know if the player is an owner.
     * This is used to dynamically re-render the screen if ownership status changes.
     */
    protected boolean isOwner() {
        return this.menu.isOwner();
    }

    @Override
    protected void init() {
        super.init();
        this.lastIsOwner = isOwner();
    }

    /**
     * Creates the three navigation tab buttons on the right side of the screen.
     */
    protected void createTabButtons(int x, int y, @NotNull ShopTab selectedTab) {
        addRenderableWidget(new IconButton(
                x, y, 22, 22,
                BUTTON_SPRITES, OFFERS_ICON,
                b -> this.switchTab(ShopTab.OFFERS),
                Component.translatable(ModLang.GUI_OFFERS_TAB),
                () -> selectedTab == ShopTab.OFFERS
        ));

        addRenderableWidget(new IconButton(
                x, y + 26, 22, 22,
                BUTTON_SPRITES, INVENTORY_ICON,
                b -> this.switchTab(ShopTab.INVENTORY),
                Component.translatable(ModLang.GUI_INVENTORY_TAB),
                () -> selectedTab == ShopTab.INVENTORY
        ));

        addRenderableWidget(new IconButton(
                x, y + 52, 22, 22,
                BUTTON_SPRITES, SETTINGS_ICON,
                b -> this.switchTab(ShopTab.SETTINGS),
                Component.translatable(ModLang.GUI_SETTINGS_TAB),
                () -> selectedTab == ShopTab.SETTINGS
        ));
    }

    /**
     * Sends a packet to the server to switch to a different menu/tab.
     */
    protected void switchTab(final @NotNull ShopTab tab) {
        if (this.menu.isOwner()) {
            final SmallShopBlockEntity blockEntity = this.menu.getBlockEntity();

            NetworkHandler.sendToServer(new SwitchTabPacket(blockEntity.getBlockPos(), tab));
        }
    }

    /**
     * Renders the "Owner: ..." text in the top right corner if the player is not an owner.
     */
    protected void renderOwnerInfo(@NotNull GuiGraphics guiGraphics, @NotNull SmallShopBlockEntity blockEntity) {
        if (!isOwner() && blockEntity.getOwnerName() != null) {
            String ownerNames = blockEntity.getOwnerName();
            String additionalOwnerNames = blockEntity.getAdditionalOwners().values().stream()
                    .filter(name -> name != null && !name.isEmpty())
                    .collect(Collectors.joining(", "));

            if (!additionalOwnerNames.isEmpty()) {
                ownerNames += ", " + additionalOwnerNames;
            }

            Component ownerText = Component.translatable(ModLang.GUI_OWNER, ownerNames);
            int ownerWidth = this.font.width(ownerText);
            guiGraphics.drawString(this.font, ownerText, this.imageWidth - ownerWidth - 8, 6, OWNER_INFO_COLOR, false);
        }
    }

    /**
     * Re-initializes the screen if the player's ownership status changes.
     */
    @Override
    public void containerTick() {
        super.containerTick();
        if (isOwner() != this.lastIsOwner) {
            this.init();
        }
    }

    protected void playSound(SoundEvent sound) {
        if (this.minecraft != null && this.minecraft.getSoundManager() != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
        }
    }
}