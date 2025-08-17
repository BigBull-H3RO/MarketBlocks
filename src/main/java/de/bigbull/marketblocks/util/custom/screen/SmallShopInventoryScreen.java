package de.bigbull.marketblocks.util.custom.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SmallShopInventoryScreen extends AbstractSmallShopScreen<SmallShopInventoryMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_inventory.png");

    private boolean lastIsOwner;

    public SmallShopInventoryScreen(SmallShopInventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        restoreMousePosition();
        clearWidgets();

        boolean isOwner = menu.isOwner();
        this.lastIsOwner = isOwner;

        if (isOwner) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, false, () -> switchTab(true), () -> {});
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Render Inventar-Labels mit Hintergrund
        Component inputLabel = Component.translatable("gui.marketblocks.input_inventory");
        Component outputLabel = Component.translatable("gui.marketblocks.output_inventory");

        // Input Label
        int inputWidth = font.width(inputLabel);
        graphics.fill(leftPos + 6, topPos + 4, leftPos + 8 + inputWidth, topPos + 14, 0x80000000);
        graphics.drawString(font, inputLabel, leftPos + 8, topPos + 6, 0xFFFFFF, false);

        // Output Label
        int outputWidth = font.width(outputLabel);
        graphics.fill(leftPos + 96, topPos + 4, leftPos + 98 + outputWidth, topPos + 14, 0x80000000);
        graphics.drawString(font, outputLabel, leftPos + 98, topPos + 6, 0xFFFFFF, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        // Inventar-Modus Titel
        Component title = Component.translatable("gui.marketblocks.inventory_title");
        graphics.drawString(font, title, 8, 6, 4210752, false);

        renderOwnerInfo(graphics, blockEntity, menu.isOwner(), imageWidth);

        // Info für Nicht-Owner
        if (!menu.isOwner()) {
            Component infoText = Component.translatable("gui.marketblocks.inventory_owner_only");
            int infoWidth = font.width(infoText);
            graphics.drawString(font, infoText, (imageWidth - infoWidth) / 2, 84, 0x808080, false);
        }

        // Spieler Inventar Label
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);

        // Tooltip für Transfer-Pfeil
        if (isMouseOver(x, y, leftPos + 58, topPos + 50, 24, 16)) {
            Component tooltip = Component.translatable("gui.marketblocks.inventory_flow_hint");
            graphics.renderTooltip(font, tooltip, x, y);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        boolean isOwner = menu.isOwner();
        if (isOwner != lastIsOwner) {
            lastIsOwner = isOwner;
            init();
        }
    }
}