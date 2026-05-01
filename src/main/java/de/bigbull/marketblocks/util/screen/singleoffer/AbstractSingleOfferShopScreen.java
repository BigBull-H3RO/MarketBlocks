package de.bigbull.marketblocks.util.screen.singleoffer;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.singleOfferShop.SwitchTabPacket;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.singleoffer.menu.ShopMenu;
import de.bigbull.marketblocks.shop.singleoffer.menu.ShopTab;
import de.bigbull.marketblocks.util.screen.gui.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractSingleOfferShopScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/singleoffer/home.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/singleoffer/inventory.png");
    private static final ResourceLocation SETTINGS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/settings.png");
    private static final ResourceLocation LOG_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/log.png");

    private boolean lastIsOwner;

    protected abstract boolean isOwner();

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        lastIsOwner = isOwner();
    }

    protected AbstractSingleOfferShopScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    protected void createTabButtons(int x, int y, ShopTab selectedTab, Runnable onOffers, Runnable onInventory, Runnable onSettings, Runnable onLog,
                                    boolean inventoryEnabled, boolean settingsEnabled, boolean logEnabled) {
        addRenderableWidget(new IconButton(
                x - 2, y - 4, 22, 22,
                BUTTON_SPRITES, OFFERS_ICON,
                b -> { if (selectedTab != ShopTab.OFFERS) onOffers.run(); },
                Component.translatable("gui.marketblocks.offers_tab"),
                () -> selectedTab == ShopTab.OFFERS
        ));

        IconButton inventoryButton = addRenderableWidget(new IconButton(
                x - 2, y + 22, 22, 22,
                BUTTON_SPRITES, INVENTORY_ICON,
                b -> { if (selectedTab != ShopTab.INVENTORY) onInventory.run(); },
                Component.translatable("gui.marketblocks.inventory_tab"),
                () -> selectedTab == ShopTab.INVENTORY
        ));
        inventoryButton.active = inventoryEnabled;

        IconButton settingsButton = addRenderableWidget(new IconButton(
                x - 2, y + 48, 22, 22,
                BUTTON_SPRITES, SETTINGS_ICON,
                b -> { if (selectedTab != ShopTab.SETTINGS) onSettings.run(); },
                Component.translatable("gui.marketblocks.settings_tab"),
                () -> selectedTab == ShopTab.SETTINGS
        ));
        settingsButton.active = settingsEnabled;

        IconButton logButton = addRenderableWidget(new IconButton(
                x - 2, y + 132, 22, 22,
                BUTTON_SPRITES, LOG_ICON,
                b -> { if (selectedTab != ShopTab.LOG) onLog.run(); },
                Component.translatable("gui.marketblocks.log_tab"),
                () -> selectedTab == ShopTab.LOG
        ));
        logButton.active = logEnabled;
    }

    /**
     * Switches the active tab and notifies the server.
     *
     * NOTE: Tab switching requires server notification for the following reasons:
     * 1. Container sync: The server needs to know which tab is active to properly
     *    handle slot visibility and validation (e.g., OwnerGatedSlot checks)
     * 2. State consistency: If a player closes and reopens the menu, the server
     *    remembers the last active tab
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

    protected void renderOwnerInfo(GuiGraphics guiGraphics, SingleOfferShopBlockEntity blockEntity, boolean isOwner, int imageWidth) {
        if (!isOwner && blockEntity.getOwnerName() != null) {
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

