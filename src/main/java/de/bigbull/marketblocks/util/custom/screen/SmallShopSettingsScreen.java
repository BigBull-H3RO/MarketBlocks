package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.UpdateShopNamePacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SmallShopSettingsScreen extends AbstractSmallShopScreen<SmallShopSettingsMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_settings.png");

    private EditBox nameField;

    public SmallShopSettingsScreen(SmallShopSettingsMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        createTabButtons(leftPos + imageWidth + 4, topPos + 8, 2,
                () -> switchTab(0),
                () -> switchTab(1),
                () -> {});

        nameField = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 20, 120, 20,
                Component.translatable("gui.marketblocks.shop_name")));
        nameField.setValue(blockEntity.getShopName());

        addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            String name = nameField.getValue();
            blockEntity.setShopNameClient(name);
            NetworkHandler.sendToServer(new UpdateShopNamePacket(blockEntity.getBlockPos(), name));
        }).bounds(leftPos + 8, topPos + 50, 60, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("gui.marketblocks.settings_title"), 8, 6, 4210752, false);
    }

    @Override
    protected boolean isOwner() {
        return true;
    }
}