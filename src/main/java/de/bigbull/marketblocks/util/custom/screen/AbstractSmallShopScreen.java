package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public abstract class AbstractSmallShopScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/offers.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/inventory.png");

    protected static double savedMouseX = -1;
    protected static double savedMouseY = -1;

    protected AbstractSmallShopScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

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
}