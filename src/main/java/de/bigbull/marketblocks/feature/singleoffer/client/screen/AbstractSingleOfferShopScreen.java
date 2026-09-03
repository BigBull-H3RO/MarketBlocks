package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.singleoffer.network.SwitchTabPacket;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.menu.ShopMenu;
import de.bigbull.marketblocks.feature.singleoffer.menu.ShopTab;
import de.bigbull.marketblocks.client.gui.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base screen class for the single-offer shop UI.
 * Provides the shared tab navigation system and renders the shop owner's
 * information.
 *
 * @param <T> The container menu type.
 */
public abstract class AbstractSingleOfferShopScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T> {
    protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button_disabled"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button_highlighted"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button_selected"));

    protected static final WidgetSprites TAB_BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "tab_button/tab"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "tab_button/tab"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "tab_button/tab"), // Hover is identical to
                                                                                         // unselected
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "tab_button/tab_selected"));

    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID,
            "textures/gui/icon/singleoffer/home.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID,
            "textures/gui/icon/singleoffer/inventory.png");
    private static final ResourceLocation SETTINGS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID,
            "textures/gui/icon/settings.png");
    private static final ResourceLocation LOG_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID,
            "textures/gui/icon/log.png");

    // Layout configuration for the right side tabs
    protected static final int SIDE_TAB_WIDTH = 30;
    protected static final int SIDE_TAB_SELECTED_WIDTH_OFFSET = 5; // Unselected is 30, selected is 35 (35-30=5)
    protected static final int SIDE_TAB_HEIGHT = 26;
    protected static final int SIDE_TAB_X_OFFSET = -7;

    protected static final int SIDE_TAB_OFFERS_Y = -4;
    protected static final int SIDE_TAB_INVENTORY_Y = 26;
    protected static final int SIDE_TAB_SETTINGS_Y = 56;
    protected static final int SIDE_TAB_LOG_Y = 127;

    private boolean lastIsOwner;

    protected boolean isOwner() {
        if (menu instanceof ShopMenu shopMenu) {
            return shopMenu.isOwner();
        }
        return false;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        lastIsOwner = isOwner();
    }

    protected List<IconButton> sideTabs = new ArrayList<>();

    protected AbstractSingleOfferShopScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    protected void createTabButtons(int x, int y, ShopTab selectedTab, Runnable onOffers, Runnable onInventory,
            Runnable onSettings, Runnable onLog,
            boolean inventoryEnabled, boolean settingsEnabled, boolean logEnabled) {

        sideTabs.clear();
        int tabX = x + SIDE_TAB_X_OFFSET;

        int offersWidth = SIDE_TAB_WIDTH + (selectedTab == ShopTab.OFFERS ? SIDE_TAB_SELECTED_WIDTH_OFFSET : 0);
        IconButton offersButton = new IconButton(
                tabX, y + SIDE_TAB_OFFERS_Y, offersWidth, SIDE_TAB_HEIGHT,
                TAB_BUTTON_SPRITES, OFFERS_ICON,
                b -> {
                    if (selectedTab != ShopTab.OFFERS)
                        onOffers.run();
                },
                Component.translatable("gui.marketblocks.offers_tab"),
                () -> selectedTab == ShopTab.OFFERS).withCustomBackground(35, 27, -3, 0, 0).withIconOffset(0, 0).withSelectedIconOffset(0, 0);
        sideTabs.add(offersButton);

        int inventoryWidth = SIDE_TAB_WIDTH + (selectedTab == ShopTab.INVENTORY ? SIDE_TAB_SELECTED_WIDTH_OFFSET : 0);
        IconButton inventoryButton = new IconButton(
                tabX, y + SIDE_TAB_INVENTORY_Y, inventoryWidth, SIDE_TAB_HEIGHT,
                TAB_BUTTON_SPRITES, INVENTORY_ICON,
                b -> {
                    if (selectedTab != ShopTab.INVENTORY)
                        onInventory.run();
                },
                Component.translatable("gui.marketblocks.inventory_tab"),
                () -> selectedTab == ShopTab.INVENTORY).withCustomBackground(35, 27, -3, 0, 0).withIconOffset(0, 0).withSelectedIconOffset(0, 0);
        sideTabs.add(inventoryButton);
        inventoryButton.active = inventoryEnabled;

        int settingsWidth = SIDE_TAB_WIDTH + (selectedTab == ShopTab.SETTINGS ? SIDE_TAB_SELECTED_WIDTH_OFFSET : 0);
        IconButton settingsButton = new IconButton(
                tabX, y + SIDE_TAB_SETTINGS_Y, settingsWidth, SIDE_TAB_HEIGHT,
                TAB_BUTTON_SPRITES, SETTINGS_ICON,
                b -> {
                    if (selectedTab != ShopTab.SETTINGS)
                        onSettings.run();
                },
                Component.translatable("gui.marketblocks.settings_tab"),
                () -> selectedTab == ShopTab.SETTINGS).withCustomBackground(35, 27, -3, 0, 0).withIconOffset(0, 0).withSelectedIconOffset(0, 0);
        sideTabs.add(settingsButton);
        settingsButton.active = settingsEnabled;

        int logWidth = SIDE_TAB_WIDTH + (selectedTab == ShopTab.LOG ? SIDE_TAB_SELECTED_WIDTH_OFFSET : 0);
        IconButton logButton = new IconButton(
                tabX, y + SIDE_TAB_LOG_Y, logWidth, SIDE_TAB_HEIGHT,
                TAB_BUTTON_SPRITES, LOG_ICON,
                b -> {
                    if (selectedTab != ShopTab.LOG)
                        onLog.run();
                },
                Component.translatable("gui.marketblocks.log_tab"),
                () -> selectedTab == ShopTab.LOG).withCustomBackground(35, 27, -3, 0, 0).withIconOffset(0, 0).withSelectedIconOffset(0, 0);
        sideTabs.add(logButton);
        logButton.active = logEnabled;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (IconButton tab : sideTabs) {
            if (tab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Returns extra areas occupied by side tabs for JEI / REI / EMI exclusions.
     */
    public List<Rect2i> getExtraAreas() {
        if (sideTabs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Rect2i> areas = new ArrayList<>();
        for (IconButton tab : sideTabs) {
            if (tab.visible) {
                int width = Math.max(tab.getWidth(), 35);
                int height = Math.max(tab.getHeight(), 27);
                areas.add(new Rect2i(tab.getX(), tab.getY(), width, height));
            }
        }
        return areas;
    }

    /**
     * Switches the active tab and notifies the server.
     *
     * NOTE: Tab switching requires server notification for the following reasons:
     * 1. Container sync: The server needs to know which tab is active to properly
     * handle slot visibility and validation (e.g., OwnerGatedSlot checks)
     * 2. State consistency: If a player closes and reopens the menu, the server
     * remembers the last active tab
     * 3. Multi-player: Other players viewing the same shop see consistent state
     *
     * The client optimistically updates the tab immediately for responsiveness.
     * If the server rejects the tab switch (e.g., permission check fails),
     * the next container sync will revert the client state.
     */
    protected void switchTab(ShopTab tab) {
        if (menu instanceof ShopMenu shopMenu && (shopMenu.isOwner() || shopMenu.isOperator())) {
            SingleOfferShopBlockEntity blockEntity = shopMenu.getBlockEntity();

            NetworkHandler.sendToServer(new SwitchTabPacket(blockEntity.getBlockPos(), tab));
            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    protected void renderOwnerInfo(GuiGraphics guiGraphics, SingleOfferShopBlockEntity blockEntity, boolean isOwner,
            int imageWidth) {
        if (blockEntity.getOwnerId() == null) {
            return;
        }
        if (!isOwner && blockEntity.getOwnerName() != null && !blockEntity.getOwnerName().isBlank()) {
            String names = blockEntity.getOwnerName();
            if (!blockEntity.getAdditionalOwners().isEmpty()) {
                names += ", " + String.join(", ", blockEntity.getAdditionalOwners().values());
            }
            Component ownerText = Component.translatable("gui.marketblocks.owner", names);
            int ownerWidth = font.width(ownerText);
            guiGraphics.drawString(font, ownerText, imageWidth - ownerWidth - 8, 6, 0x404040, false);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        boolean owner = isOwner();
        if (owner != lastIsOwner) {
            init();
        }
    }

    protected void playSound(SoundEvent sound) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    protected void playSound(Holder<SoundEvent> sound) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }
}
