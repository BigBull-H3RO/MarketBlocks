package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractSmallShopScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/offers.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/inventory.png");
    private static final ResourceLocation SETTINGS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/settings.png");

    private boolean lastIsOwner;

    protected abstract boolean isOwner();

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        lastIsOwner = isOwner();
    }

    protected AbstractSmallShopScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    protected void createTabButtons(int x, int y, ShopTab selectedTab, Runnable onOffers, Runnable onInventory, Runnable onSettings) {
        addRenderableWidget(new IconButton(
                x - 2, y - 4, 22, 22,
                BUTTON_SPRITES, OFFERS_ICON,
                b -> { if (selectedTab != ShopTab.OFFERS) onOffers.run(); },
                Component.translatable("gui.marketblocks.offers_tab"),
                () -> selectedTab == ShopTab.OFFERS
        ));

        addRenderableWidget(new IconButton(
                x - 2, y + 22, 22, 22,
                BUTTON_SPRITES, INVENTORY_ICON,
                b -> { if (selectedTab != ShopTab.INVENTORY) onInventory.run(); },
                Component.translatable("gui.marketblocks.inventory_tab"),
                () -> selectedTab == ShopTab.INVENTORY
        ));

        addRenderableWidget(new IconButton(
                x - 2, y + 48, 22, 22,
                BUTTON_SPRITES, SETTINGS_ICON,
                b -> { if (selectedTab != ShopTab.SETTINGS) onSettings.run(); },
                Component.translatable("gui.marketblocks.settings_tab"),
                () -> selectedTab == ShopTab.SETTINGS
        ));
    }

    protected void switchTab(ShopTab tab) {
        if (menu instanceof ShopMenu shopMenu && shopMenu.isOwner()) {
            SmallShopBlockEntity blockEntity = shopMenu.getBlockEntity();

            NetworkHandler.sendToServer(new SwitchTabPacket(blockEntity.getBlockPos(), tab));
            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    protected void renderOwnerInfo(GuiGraphics guiGraphics, SmallShopBlockEntity blockEntity, boolean isOwner, int imageWidth) {
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