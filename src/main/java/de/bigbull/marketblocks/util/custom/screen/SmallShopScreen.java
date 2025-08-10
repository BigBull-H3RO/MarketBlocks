package de.bigbull.marketblocks.util.custom.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.CreateOfferPacket;
import de.bigbull.marketblocks.network.packets.DeleteOfferPacket;
import de.bigbull.marketblocks.util.custom.entity.SmallShopBlockEntity;
import de.bigbull.marketblocks.util.custom.menu.SmallShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallShopScreen extends AbstractContainerScreen<SmallShopMenu> {
    private static final ResourceLocation BACKGROUND_OFFERS = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_offers.png");
    private static final ResourceLocation BACKGROUND_INVENTORY = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_inventory.png");
    private static final ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_arrow_disabled.png");

    // Button Sprites
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/button"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/button_highlighted"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/button_selected")
    );

    private static final WidgetSprites ACTION_BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/action"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/action_highlighted"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/action_pressed"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "button/action_disabled")
    );

    // Icons
    private static final ResourceLocation OFFERS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icons/offers.png");
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icons/inventory.png");
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icons/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icons/delete.png");

    private boolean showOffers = true;
    private boolean creatingOffer = false;

    // Temporäre Slots für Angebots-Erstellung
    private ItemStack tempPayment1 = ItemStack.EMPTY;
    private ItemStack tempPayment2 = ItemStack.EMPTY;
    private ItemStack tempResult = ItemStack.EMPTY;

    // Buttons
    private IconButton offersButton;
    private IconButton inventoryButton;
    private IconButton createOfferButton;
    private IconButton deleteOfferButton;
    private IconButton confirmButton;
    private IconButton cancelButton;

    public SmallShopScreen(SmallShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();

        // Clear existing buttons
        clearWidgets();

        // Tab-Buttons (nur für Owner sichtbar)
        if (isOwner) {
            this.offersButton = addRenderableWidget(new IconButton(
                    leftPos - 28, topPos + 8, 24, 24,
                    BUTTON_SPRITES, OFFERS_ICON,
                    button -> switchToOffers(),
                    Component.translatable("gui.marketblocks.offers_tab"),
                    () -> showOffers
            ));

            this.inventoryButton = addRenderableWidget(new IconButton(
                    leftPos - 28, topPos + 36, 24, 24,
                    BUTTON_SPRITES, INVENTORY_ICON,
                    button -> switchToInventory(),
                    Component.translatable("gui.marketblocks.inventory_tab"),
                    () -> !showOffers
            ));
        }

        // Angebots-Buttons (nur auf Angebots-Seite)
        if (showOffers) {
            if (isOwner) {
                if (creatingOffer) {
                    // Bestätigen/Abbrechen Buttons während Erstellung
                    this.confirmButton = addRenderableWidget(new IconButton(
                            leftPos + 80, topPos + 60, 20, 20,
                            ACTION_BUTTON_SPRITES, CREATE_ICON,
                            button -> confirmOffer(),
                            Component.translatable("gui.marketblocks.confirm_offer"),
                            () -> false
                    ));

                    this.cancelButton = addRenderableWidget(new IconButton(
                            leftPos + 104, topPos + 60, 20, 20,
                            ACTION_BUTTON_SPRITES, DELETE_ICON,
                            button -> cancelOfferCreation(),
                            Component.translatable("gui.marketblocks.cancel_offer"),
                            () -> false
                    ));
                } else if (!blockEntity.hasOffer()) {
                    // Erstellen Button wenn kein Angebot existiert
                    this.createOfferButton = addRenderableWidget(new IconButton(
                            leftPos + 80, topPos + 60, 20, 20,
                            ACTION_BUTTON_SPRITES, CREATE_ICON,
                            button -> startOfferCreation(),
                            Component.translatable("gui.marketblocks.create_offer"),
                            () -> false
                    ));
                } else {
                    // Löschen Button wenn Angebot existiert
                    this.deleteOfferButton = addRenderableWidget(new IconButton(
                            leftPos + 104, topPos + 60, 20, 20,
                            ACTION_BUTTON_SPRITES, DELETE_ICON,
                            button -> deleteOffer(),
                            Component.translatable("gui.marketblocks.delete_offer"),
                            () -> false
                    ));
                }
            }
        }

        updateSlotVisibility();
    }

    private void switchToOffers() {
        if (!showOffers) {
            showOffers = true;
            creatingOffer = false;
            playClickSound();
            init();
        }
    }

    private void switchToInventory() {
        if (showOffers) {
            showOffers = false;
            creatingOffer = false;
            playClickSound();
            init();
        }
    }

    private void startOfferCreation() {
        creatingOffer = true;
        tempPayment1 = ItemStack.EMPTY;
        tempPayment2 = ItemStack.EMPTY;
        tempResult = ItemStack.EMPTY;
        playClickSound();
        init();
    }

    private void confirmOffer() {
        // Hole Items aus Payment Slots
        ItemStack payment1 = menu.slots.get(24).getItem().copy();
        ItemStack payment2 = menu.slots.get(25).getItem().copy();

        // Für das Result Item müsste hier ein spezieller Slot sein
        // Aktuell nehmen wir an, dass es manuell gesetzt wurde
        ItemStack result = tempResult.copy();

        if (!payment1.isEmpty() || !payment2.isEmpty() || !result.isEmpty()) {
            // Sende Netzwerk-Paket
            NetworkHandler.sendToServer(new CreateOfferPacket(
                    menu.getBlockEntity().getBlockPos(),
                    payment1,
                    payment2,
                    result
            ));

            // Leere die Slots
            menu.slots.get(24).set(ItemStack.EMPTY);
            menu.slots.get(25).set(ItemStack.EMPTY);

            creatingOffer = false;
            playSuccessSound();
            init();
        }
    }

    private void cancelOfferCreation() {
        // Gebe Items zurück
        if (!menu.slots.get(24).getItem().isEmpty()) {
            minecraft.player.getInventory().add(menu.slots.get(24).getItem());
            menu.slots.get(24).set(ItemStack.EMPTY);
        }
        if (!menu.slots.get(25).getItem().isEmpty()) {
            minecraft.player.getInventory().add(menu.slots.get(25).getItem());
            menu.slots.get(25).set(ItemStack.EMPTY);
        }

        creatingOffer = false;
        tempPayment1 = ItemStack.EMPTY;
        tempPayment2 = ItemStack.EMPTY;
        tempResult = ItemStack.EMPTY;
        playClickSound();
        init();
    }

    private void deleteOffer() {
        NetworkHandler.sendToServer(new DeleteOfferPacket(menu.getBlockEntity().getBlockPos()));
        playClickSound();
        init();
    }

    private void updateSlotVisibility() {
        for (Slot slot : menu.slots) {
            int index = slot.index;

            // Player inventory slots sind immer sichtbar (ab Index 27)
            if (index >= 27) {
                setPlayerSlotPositions(slot, index);
                continue;
            }

            if (showOffers) {
                // Angebots-Modus: Nur Payment und Offer Slots sichtbar
                if (index < 24) { // Input/Output Slots verstecken
                    hideSlot(slot);
                } else { // Payment/Offer Slots positionieren
                    repositionOfferSlots(slot, index);
                }
            } else {
                // Inventar-Modus: Nur Input/Output Slots sichtbar
                if (index < 24) { // Input/Output Slots
                    repositionInventorySlots(slot, index);
                } else if (index < 27) { // Payment/Offer Slots verstecken
                    hideSlot(slot);
                }
            }
        }
    }

    private void hideSlot(Slot slot) {
        slot.x = -1000;
        slot.y = -1000;
    }

    private void setPlayerSlotPositions(Slot slot, int index) {
        if (index < 54) { // Main inventory (27-53)
            int localIndex = index - 27;
            int row = localIndex / 9;
            int col = localIndex % 9;
            slot.x = leftPos + 8 + col * 18;
            slot.y = leftPos + 140 + row * 18;
        } else { // Hotbar (54-62)
            int localIndex = index - 54;
            slot.x = leftPos + 8 + localIndex * 18;
            slot.y = leftPos + 198;
        }
    }

    private void repositionOfferSlots(Slot slot, int index) {
        switch (index) {
            case 24 -> { // Payment Slot 1
                slot.x = leftPos + 44;
                slot.y = topPos + 35;
            }
            case 25 -> { // Payment Slot 2
                slot.x = leftPos + 62;
                slot.y = topPos + 35;
            }
            case 26 -> { // Offer Result Slot
                slot.x = leftPos + 120;
                slot.y = topPos + 35;
            }
        }
    }

    private void repositionInventorySlots(Slot slot, int index) {
        if (index < 12) { // Input Slots (0-11)
            int row = index / 3;
            int col = index % 3;
            slot.x = leftPos + 8 + col * 18;
            slot.y = topPos + 18 + row * 18;
        } else if (index < 24) { // Output Slots (12-23)
            int localIndex = index - 12;
            int row = localIndex / 3;
            int col = localIndex % 3;
            slot.x = leftPos + 116 + col * 18;
            slot.y = topPos + 18 + row * 18;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ResourceLocation background = showOffers ? BACKGROUND_OFFERS : BACKGROUND_INVENTORY;
        graphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (showOffers) {
            renderOfferBackground(graphics, partialTick, mouseX, mouseY);
        } else {
            renderInventoryBackground(graphics, partialTick, mouseX, mouseY);
        }
    }

    private void renderOfferBackground(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        // Render Handels-Pfeil
        boolean arrowActive = blockEntity.hasOffer() && blockEntity.isOfferAvailable();
        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, leftPos + 88, topPos + 35, 0, 0, 24, 16);

        // Render bestehendes Angebot (falls vorhanden und nicht gerade erstellt wird)
        if (blockEntity.hasOffer() && !creatingOffer) {
            renderExistingOffer(graphics, blockEntity);
        }

        // Render Erstellungs-Hinweise
        if (creatingOffer) {
            renderCreationHints(graphics);
        }
    }

    private void renderExistingOffer(GuiGraphics graphics, SmallShopBlockEntity blockEntity) {
        // Render Angebots-Vorschau oben
        int offerX = leftPos + 20;
        int offerY = topPos + 8;

        // Background für Angebots-Anzeige
        graphics.fill(offerX - 2, offerY - 2, offerX + 100, offerY + 20, 0x80000000);

        // Render Payment Items
        ItemStack payment1 = blockEntity.getOfferPayment1();
        ItemStack payment2 = blockEntity.getOfferPayment2();
        ItemStack result = blockEntity.getOfferResult();

        if (!payment1.isEmpty()) {
            graphics.renderItem(payment1, offerX, offerY);
            graphics.renderItemDecorations(font, payment1, offerX, offerY);
        }

        if (!payment2.isEmpty()) {
            graphics.renderItem(payment2, offerX + 18, offerY);
            graphics.renderItemDecorations(font, payment2, offerX + 18, offerY);
        }

        // Render Pfeil für Angebots-Vorschau (kleiner)
        ResourceLocation arrowTexture = blockEntity.isOfferAvailable() ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, offerX + 44, offerY + 1, 0, 0, 12, 8, 24, 16);

        if (!result.isEmpty()) {
            graphics.renderItem(result, offerX + 64, offerY);
            graphics.renderItemDecorations(font, result, offerX + 64, offerY);
        }
    }

    private void renderCreationHints(GuiGraphics graphics) {
        Component hint = Component.translatable("gui.marketblocks.create_hint");
        int hintWidth = font.width(hint);
        graphics.drawString(font, hint, leftPos + (imageWidth - hintWidth) / 2, topPos + 90, 0x808080, false);
    }

    private void renderInventoryBackground(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Render Inventar-Labels mit Hintergrund
        Component inputLabel = Component.translatable("gui.marketblocks.input_inventory");
        Component outputLabel = Component.translatable("gui.marketblocks.output_inventory");

        // Input Label
        int inputWidth = font.width(inputLabel);
        graphics.fill(leftPos + 6, topPos + 4, leftPos + 8 + inputWidth, topPos + 14, 0x80000000);
        graphics.drawString(font, inputLabel, leftPos + 8, topPos + 6, 0xFFFFFF, false);

        // Output Label
        int outputWidth = font.width(outputLabel);
        graphics.fill(leftPos + 114, topPos + 4, leftPos + 116 + outputWidth, topPos + 14, 0x80000000);
        graphics.drawString(font, outputLabel, leftPos + 116, topPos + 6, 0xFFFFFF, false);

        // Render Transfer-Pfeil zwischen Input und Output
        graphics.blit(TRADE_ARROW, leftPos + 76, topPos + 50, 0, 0, 24, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        if (showOffers) {
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
        } else {
            // Inventar-Modus Titel
            Component title = Component.translatable("gui.marketblocks.inventory_title");
            graphics.drawString(font, title, 8, 6, 4210752, false);
        }

        // Spieler Inventar Label
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);

        if (showOffers) {
            SmallShopBlockEntity blockEntity = menu.getBlockEntity();
            if (blockEntity.hasOffer() && !creatingOffer) {
                renderOfferTooltips(graphics, x, y, blockEntity);
            }
        }
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Tab-Wechsel mit E für Owner
        if (menu.isOwner() && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (hasShiftDown()) {
                // Shift+E schließt GUI
                minecraft.player.closeContainer();
                return true;
            } else {
                // E wechselt Tab
                if (showOffers) {
                    switchToInventory();
                } else {
                    switchToOffers();
                }
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        // Aktualisiere UI wenn sich Angebot ändert
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            // Prüfe ob sich Angebots-Status geändert hat
            boolean currentHasOffer = blockEntity.hasOffer();
            boolean currentOfferAvailable = blockEntity.isOfferAvailable();

            // Könnte hier weitere Live-Updates implementieren
        }
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