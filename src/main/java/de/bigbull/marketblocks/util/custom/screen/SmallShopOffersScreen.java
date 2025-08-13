package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.CancelOfferPacket;
import de.bigbull.marketblocks.network.packets.CreateOfferPacket;
import de.bigbull.marketblocks.network.packets.DeleteOfferPacket;
import de.bigbull.marketblocks.network.packets.SwitchTabPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopOffersMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class SmallShopOffersScreen extends AbstractSmallShopScreen<SmallShopOffersMenu> implements ContainerListener {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_offers.png");
    private static final ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    // Button Sprites für weitere Aktionen
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png")
    );

    // Icons
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");

    private boolean creatingOffer = false;
    private boolean lastIsOwner;
    private OfferTemplateButton offerButton;

    public SmallShopOffersScreen(SmallShopOffersMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();

        // Listener erneut registrieren, um doppelte Einträge zu vermeiden
        menu.removeSlotListener(this);
        menu.addSlotListener(this);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();
        this.lastIsOwner = isOwner;
        this.offerButton = new OfferTemplateButton(leftPos + 46, topPos + 20);

        // Clear existing buttons
        clearWidgets();

        // Tab-Buttons (nur für Owner sichtbar)
        if (isOwner) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, true, () -> {}, this::switchToInventory);
        }

        // Angebots-Buttons
        if (isOwner) {
            if (creatingOffer) {
                // Bestätigen/Abbrechen Buttons während Erstellung
                IconButton confirmButton = addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 6, 20, 20,
                        BUTTON_SPRITES, CREATE_ICON,
                        button -> confirmOffer(),
                        Component.translatable("gui.marketblocks.confirm_offer"),
                        () -> false
                ));

                IconButton cancelButton = addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 28, 20, 20,
                        BUTTON_SPRITES, DELETE_ICON,
                        button -> cancelOfferCreation(),
                        Component.translatable("gui.marketblocks.cancel_offer"),
                        () -> false
                ));
            } else if (!blockEntity.hasOffer()) {
                // Erstellen Button wenn kein Angebot existiert
                IconButton createOfferButton = addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 6, 20, 20,
                        BUTTON_SPRITES, CREATE_ICON,
                        button -> startOfferCreation(),
                        Component.translatable("gui.marketblocks.create_offer"),
                        () -> false
                ));
            } else {
                // Löschen Button wenn Angebot existiert
                IconButton deleteOfferButton = addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 28, 20, 20,
                        BUTTON_SPRITES, DELETE_ICON,
                        button -> deleteOffer(),
                        Component.translatable("gui.marketblocks.delete_offer"),
                        () -> false
                ));
            }
        }
    }

    private void switchToInventory() {
        // Sende nur ein Paket an den Server, der anschließend das Menü öffnet
        if (menu.isOwner()) {
            SmallShopBlockEntity blockEntity = menu.getBlockEntity();
            NetworkHandler.sendToServer(new SwitchTabPacket(blockEntity.getBlockPos(), false));
            playClickSound();
        }
    }

    private void startOfferCreation() {
        creatingOffer = true;
        menu.setCreatingOffer(true);
        playClickSound();
        init();
    }

    private void confirmOffer() {
        try {
            // Hole Items aus Payment Slots
            ItemStack payment1 = menu.slots.get(0).getItem().copy();
            ItemStack payment2 = menu.slots.get(1).getItem().copy();
            ItemStack result = menu.slots.get(2).getItem().copy();

            // IMPROVED: Bessere Validation
            if (result.isEmpty()) {
                // Zeige Fehlermeldung
                minecraft.gui.getChat().addMessage(
                        Component.translatable("gui.marketblocks.error.no_result_item")
                                .withStyle(ChatFormatting.RED)
                );
                playErrorSound();
                return;
            }

            if (payment1.isEmpty() && payment2.isEmpty()) {
                // Zeige Fehlermeldung
                minecraft.gui.getChat().addMessage(
                        Component.translatable("gui.marketblocks.error.no_payment_items")
                                .withStyle(ChatFormatting.RED)
                );
                playErrorSound();
                return;
            }

            // Sende Netzwerk-Paket
            NetworkHandler.sendToServer(new CreateOfferPacket(
                    menu.getBlockEntity().getBlockPos(),
                    payment1,
                    payment2,
                    result
            ));

            creatingOffer = false;
            menu.setCreatingOffer(false);
            menu.getBlockEntity().setHasOfferClient(true);
            playSuccessSound();
            init();

        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error confirming offer", e);
            playErrorSound();
        }
    }

    private void cancelOfferCreation() {
        NetworkHandler.sendToServer(new CancelOfferPacket(menu.getBlockEntity().getBlockPos()));
        onOfferCreationCancelled();
        playClickSound();
    }

    public void onOfferCreationCancelled() {
        creatingOffer = false;
        menu.setCreatingOffer(false);
        init();
    }

    private void deleteOffer() {
        NetworkHandler.sendToServer(new DeleteOfferPacket(menu.getBlockEntity().getBlockPos()));
        playClickSound();
    }

    public void onOfferDeleted() {
        creatingOffer = false;
        menu.setCreatingOffer(false);
        menu.getBlockEntity().setHasOfferClient(false);
        init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(BACKGROUND, i, j, 0, 0, imageWidth, imageHeight);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        if (creatingOffer) {
            renderOfferPreview(graphics);
        } else if (blockEntity.hasOffer()) {
            renderExistingOffer(graphics, blockEntity);
        }

        // Render Handels-Pfeil
        boolean arrowActive = blockEntity.hasOffer() && blockEntity.isOfferAvailable();
        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, leftPos + 88, topPos + 35, 0, 0, 24, 16);

        // Render Erstellungs-Hinweise
        if (creatingOffer) {
            renderCreationHints(graphics);
        }
    }

    private void renderExistingOffer(GuiGraphics graphics, SmallShopBlockEntity blockEntity) {
        offerButton.update(
                blockEntity.getOfferPayment1(),
                blockEntity.getOfferPayment2(),
                blockEntity.getOfferResult(),
                blockEntity.isOfferAvailable()
        );
        offerButton.renderWidget(graphics, 0, 0, 0);
    }

    private void renderOfferPreview(GuiGraphics graphics) {
        offerButton.update(
                menu.slots.get(0).getItem(),
                menu.slots.get(1).getItem(),
                menu.slots.get(2).getItem(),
                true
        );
        offerButton.renderWidget(graphics, 0, 0, 0);
    }

    private void renderCreationHints(GuiGraphics graphics) {
        Component hint = Component.translatable("gui.marketblocks.create_hint");
        int hintWidth = font.width(hint);
        graphics.drawString(font, hint, leftPos + (imageWidth - hintWidth) / 2, topPos + 90, 0x808080, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        // Titel
        Component title = Component.translatable("gui.marketblocks.shop_title");
        graphics.drawString(font, title, 8, 6, 4210752, false);

        // Owner Info für Nicht-Owner
        if (!menu.isOwner() && blockEntity.getOwnerName() != null) {
            Component ownerText = Component.translatable("gui.marketblocks.owner", blockEntity.getOwnerName());
            int ownerWidth = font.width(ownerText);
            graphics.drawString(font, ownerText, imageWidth - ownerWidth - 8, 6, 0x404040, false);
        }

        // Status-Meldungen
        if (blockEntity.hasOffer()) {
            if (!blockEntity.isOfferAvailable()) {
                Component noStockText = Component.translatable("gui.marketblocks.out_of_stock");
                graphics.drawString(font, noStockText, 8, 84, 0xFF0000, false);
            } else if (!menu.isOwner()) {
                Component availableText = Component.translatable("gui.marketblocks.available");
                graphics.drawString(font, availableText, 8, 84, 0x00FF00, false);
            }
        } else if (!menu.isOwner()) {
            Component noOfferText = Component.translatable("gui.marketblocks.no_offers");
            graphics.drawString(font, noOfferText, 8, 84, 0x808080, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity.hasOffer() && !creatingOffer) {
            renderOfferTooltips(graphics, x, y, blockEntity);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (creatingOffer) {
            offerButton.update(
                    this.menu.slots.get(0).getItem(),
                    this.menu.slots.get(1).getItem(),
                    this.menu.slots.get(2).getItem(),
                    true
            );
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // keine zusätzlichen Aktionen nötig
    }

    private void renderOfferTooltips(GuiGraphics graphics, int mouseX, int mouseY, SmallShopBlockEntity blockEntity) {
        int offerX = leftPos + 20;
        int offerY = topPos + 8;

        // Tooltip für Payment 1
        if (isMouseOver(mouseX, mouseY, offerX, offerY, 16, 16)) {
            ItemStack payment1 = blockEntity.getOfferPayment1();
            if (!payment1.isEmpty()) {
                graphics.renderTooltip(font, payment1, mouseX, mouseY);
                return;
            }
        }

        // Tooltip für Payment 2
        if (isMouseOver(mouseX, mouseY, offerX + 18, offerY, 16, 16)) {
            ItemStack payment2 = blockEntity.getOfferPayment2();
            if (!payment2.isEmpty()) {
                graphics.renderTooltip(font, payment2, mouseX, mouseY);
                return;
            }
        }

        // Tooltip für Result
        if (isMouseOver(mouseX, mouseY, offerX + 64, offerY, 16, 16)) {
            ItemStack result = blockEntity.getOfferResult();
            if (!result.isEmpty()) {
                graphics.renderTooltip(font, result, mouseX, mouseY);
                return;
            }
        }

        // Tooltip für Handels-Pfeil
        if (isMouseOver(mouseX, mouseY, leftPos + 88, topPos + 35, 24, 16)) {
            if (blockEntity.hasOffer()) {
                Component tooltip = blockEntity.isOfferAvailable()
                        ? Component.translatable("gui.marketblocks.trade_available")
                        : Component.translatable("gui.marketblocks.trade_unavailable");
                graphics.renderTooltip(font, tooltip, mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
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

    @Override
    public void removed() {
        super.removed();
        menu.removeSlotListener(this);
    }

    // Sound-Hilfsmethoden
    private void playClickSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void playSuccessSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
    }

    private void playErrorSound() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_BREAK, 1.0F));
    }
}