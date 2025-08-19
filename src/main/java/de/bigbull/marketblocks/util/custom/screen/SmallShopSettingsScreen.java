package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.UpdateRedstoneSettingPacket;
import de.bigbull.marketblocks.network.packets.UpdateShopNamePacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopSettingsMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SmallShopSettingsScreen extends AbstractSmallShopScreen<SmallShopSettingsMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_settings.png");

    private EditBox nameField;
    private Checkbox emitRedstoneCheckbox;

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

        emitRedstoneCheckbox = addRenderableWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.emit_redstone"), font)
                .pos(leftPos + 8, topPos + 50)
                .selected(blockEntity.isEmitRedstone())
                .tooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")))
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            String name = nameField.getValue();
            boolean emit = emitRedstoneCheckbox.selected();
            blockEntity.setShopNameClient(name);
            blockEntity.setEmitRedstoneClient(emit);
            NetworkHandler.sendToServer(new UpdateShopNamePacket(blockEntity.getBlockPos(), name));
            NetworkHandler.sendToServer(new UpdateRedstoneSettingPacket(blockEntity.getBlockPos(), emit));
        }).bounds(leftPos + imageWidth - 60 - 8, topPos + imageHeight - 20 - 8, 60, 20).build());
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