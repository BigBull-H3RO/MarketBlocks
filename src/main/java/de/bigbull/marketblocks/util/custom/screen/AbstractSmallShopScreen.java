package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.SwitchTabPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
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
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

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

    protected static double savedMouseX = -1;
    protected static double savedMouseY = -1;

    private boolean lastIsOwner;

    protected abstract boolean isOwner();

    @Override
    protected void init() {
        super.init();
        restoreMousePosition();
        clearWidgets();
        lastIsOwner = isOwner();
    }

    protected AbstractSmallShopScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    protected void createTabButtons(int x, int y, int selectedTab, Runnable onOffers, Runnable onInventory, Runnable onSettings) {
        addRenderableWidget(new IconButton(
                x - 2, y - 4, 22, 22,
                BUTTON_SPRITES, OFFERS_ICON,
                b -> { if (selectedTab != 0) onOffers.run(); },
                Component.translatable("gui.marketblocks.offers_tab"),
                () -> selectedTab == 0
        ));

        addRenderableWidget(new IconButton(
                x - 2, y + 22, 22, 22,
                BUTTON_SPRITES, INVENTORY_ICON,
                b -> { if (selectedTab != 1) onInventory.run(); },
                Component.translatable("gui.marketblocks.inventory_tab"),
                () -> selectedTab == 1
        ));

        addRenderableWidget(new IconButton(
                x - 2, y + 48, 22, 22,
                BUTTON_SPRITES, SETTINGS_ICON,
                b -> { if (selectedTab != 2) onSettings.run(); },
                Component.translatable("gui.marketblocks.settings_tab"),
                () -> selectedTab == 2
        ));
    }

    protected void switchTab(int tab) {
        SmallShopBlockEntity blockEntity = null;
        boolean isOwner = false;

        if (menu instanceof SmallShopOffersMenu offersMenu) {
            blockEntity = offersMenu.getBlockEntity();
            isOwner = offersMenu.isOwner();
        } else if (menu instanceof SmallShopInventoryMenu inventoryMenu) {
            blockEntity = inventoryMenu.getBlockEntity();
            isOwner = inventoryMenu.isOwner();
        } else if (menu instanceof SmallShopSettingsMenu configMenu) {
            blockEntity = configMenu.getBlockEntity();
            isOwner = configMenu.isOwner();
            if (!isOwner) {
                MarketBlocks.LOGGER.warn("Non-owner attempted to switch tab via settings menu");
            }
        }

        if (blockEntity != null && isOwner) {
            Minecraft mc = Minecraft.getInstance();
            savedMouseX = mc.mouseHandler.xpos();
            savedMouseY = mc.mouseHandler.ypos();

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

    protected void restoreMousePosition() {
        if (savedMouseX >= 0 && savedMouseY >= 0) {
            Minecraft mc = Minecraft.getInstance();
            mc.mouseHandler.setIgnoreFirstMove();
            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), savedMouseX, savedMouseY);

            try {
                MouseHandler handler = mc.mouseHandler;
                Field xField = MouseHandler.class.getDeclaredField("xpos");
                Field yField = MouseHandler.class.getDeclaredField("ypos");
                xField.setAccessible(true);
                yField.setAccessible(true);
                xField.setDouble(handler, savedMouseX);
                yField.setDouble(handler, savedMouseY);
            } catch (ReflectiveOperationException ignored) {
            }

            double scaledX = savedMouseX * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
            double scaledY = savedMouseY * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
            this.mouseMoved(scaledX, scaledY);

            savedMouseX = -1;
            savedMouseY = -1;
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