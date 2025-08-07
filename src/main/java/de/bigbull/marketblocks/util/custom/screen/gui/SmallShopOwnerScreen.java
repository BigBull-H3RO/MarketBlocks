package de.bigbull.marketblocks.util.custom.screen.gui;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SmallShopOwnerScreen extends AbstractContainerScreen<SmallShopMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop.png");
    private Button saveButton;
    private Button removeButton;
    private int tab = 0;

    public SmallShopOwnerScreen(SmallShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;
        saveButton = addRenderableWidget(Button.builder(Component.translatable("screen.marketblocks.small_shop.save"), b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_CONFIRM);
        }).pos(x + 110, y + 90).size(60, 20).build());
        removeButton = addRenderableWidget(Button.builder(Component.translatable("screen.marketblocks.small_shop.remove"), b -> {
            menu.clickMenuButton(Minecraft.getInstance().player, SmallShopMenu.BUTTON_REMOVE);
        }).pos(x + 110, y + 115).size(60, 20).build());

        // Tab-Leiste rechts
        addRenderableWidget(Button.builder(Component.translatable("screen.marketblocks.small_shop.tab.offer"), b -> tab = 0)
                .pos(x + this.imageWidth + 4, y + 20).size(60, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.marketblocks.small_shop.tab.storage"), b -> tab = 1)
                .pos(x + this.imageWidth + 4, y + 44).size(60, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        saveButton.visible = tab == 0;
        removeButton.visible = tab == 0;
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}