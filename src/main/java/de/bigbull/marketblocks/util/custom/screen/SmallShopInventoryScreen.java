package de.bigbull.marketblocks.util.custom.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.SwitchTabPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopInventoryMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class SmallShopInventoryScreen extends AbstractContainerScreen<SmallShopInventoryMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_inventory.png");
    private static final ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");

    // Button Sprites
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    // Icons
    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/offers.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/inventory.png");

    // Buttons
    private IconButton offersButton;
    private IconButton inventoryButton;
    private boolean lastIsOwner;

    public SmallShopInventoryScreen(SmallShopInventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();

        boolean isOwner = menu.isOwner();
        this.lastIsOwner = isOwner;

        // Clear existing buttons
        clearWidgets();

        // Tab-Buttons (nur für Owner sichtbar)
        if (isOwner) {
            this.offersButton = addRenderableWidget(new IconButton(
                    leftPos + imageWidth + 4, topPos + 8, 24, 24,
                    BUTTON_SPRITES, OFFERS_ICON,
                    button -> switchToOffers(),
                    Component.translatable("gui.marketblocks.offers_tab"),
                    () -> false
            ));

            this.inventoryButton = addRenderableWidget(new IconButton(
                    leftPos + imageWidth + 4, topPos + 36, 24, 24,
                    BUTTON_SPRITES, INVENTORY_ICON,
                    button -> {}, // Bereits im Inventory-Modus
                    Component.translatable("gui.marketblocks.inventory_tab"),
                    () -> true // Immer selected da wir im Inventory-Modus sind
            ));
        }
    }

    private void switchToOffers() {
        // Sende nur ein Paket an den Server, der anschließend das Menü öffnet
        if (menu.isOwner()) {
            SmallShopBlockEntity blockEntity = menu.getBlockEntity();
            NetworkHandler.sendToServer(new SwitchTabPacket(blockEntity.getBlockPos(), true));
            playClickSound();
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

        // Render Transfer-Pfeil zwischen Input und Output
        graphics.blit(TRADE_ARROW, leftPos + 58, topPos + 50, 0, 0, 24, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        // Inventar-Modus Titel
        Component title = Component.translatable("gui.marketblocks.inventory_title");
        graphics.drawString(font, title, 8, 6, 4210752, false);

        // Owner Info für Nicht-Owner
        if (!menu.isOwner() && blockEntity.getOwnerName() != null) {
            Component ownerText = Component.translatable("gui.marketblocks.owner", blockEntity.getOwnerName());
            int ownerWidth = font.width(ownerText);
            graphics.drawString(font, ownerText, imageWidth - ownerWidth - 8, 6, 0x404040, false);
        }

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

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Tab-Wechsel mit E für Owner
        if (menu.isOwner() && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (hasShiftDown()) {
                // Shift+E schließt GUI
                minecraft.player.closeContainer();
                return true;
            } else {
                // E wechselt zu Offers
                switchToOffers();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        boolean isOwner = menu.isOwner();
        if (isOwner != lastIsOwner) {
            lastIsOwner = isOwner;
            init();
            return;
        }

        // Aktualisiere UI falls nötig
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            // Könnte hier Live-Updates für Inventar-Änderungen implementieren
        }
    }

    // Sound-Hilfsmethoden
    private void playClickSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}