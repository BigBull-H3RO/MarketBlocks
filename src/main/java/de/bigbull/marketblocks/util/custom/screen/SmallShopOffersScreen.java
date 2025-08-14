package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
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
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmallShopOffersScreen extends AbstractSmallShopScreen<SmallShopOffersMenu> implements ContainerListener {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/small_shop_offers.png");
    private static final ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    // Button Sprites
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    // Icons
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");

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

        // Listener registrieren
        menu.removeSlotListener(this);
        menu.addSlotListener(this);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();
        this.lastIsOwner = isOwner;

        // Clear existing buttons
        clearWidgets();

        // OfferTemplateButton - immer sichtbar und klickbar
        this.offerButton = addRenderableWidget(new OfferTemplateButton(
                leftPos + 46, topPos + 20,
                button -> onOfferClicked()
        ));

        // Button ist immer aktiv und sichtbar
        this.offerButton.active = true;
        this.offerButton.visible = true;

        // Tab-Buttons (nur für Owner sichtbar)
        if (isOwner) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, true, () -> {}, this::switchToInventory);
        }

        // Einfache Angebots-Buttons für Owner
        if (isOwner) {
            if (!blockEntity.hasOffer()) {
                // Erstellen Button wenn kein Angebot existiert
                IconButton createOfferButton = addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 17, 20, 20,
                        BUTTON_SPRITES, CREATE_ICON,
                        button -> createOffer(),
                        Component.translatable("gui.marketblocks.create_offer"),
                        () -> false
                ));
            } else {
                // Löschen Button wenn Angebot existiert
                IconButton deleteOfferButton = addRenderableWidget(new IconButton(
                        leftPos + 148, topPos + 17, 20, 20,
                        BUTTON_SPRITES, DELETE_ICON,
                        button -> deleteOffer(),
                        Component.translatable("gui.marketblocks.delete_offer"),
                        () -> false
                ));
            }
        }
    }

    private void switchToInventory() {
        if (menu.isOwner()) {
            SmallShopBlockEntity blockEntity = menu.getBlockEntity();
            NetworkHandler.sendToServer(new SwitchTabPacket(blockEntity.getBlockPos(), false));
            playClickSound();
        }
    }

    private void createOffer() {
        try {
            // Hole Items aus Payment Slots
            ItemStack payment1 = menu.slots.get(0).getItem().copy();
            ItemStack payment2 = menu.slots.get(1).getItem().copy();
            ItemStack result = menu.slots.get(2).getItem().copy();

            // Normalisierung: leerer payment1 und payment2 mit Inhalt tauschen
            if (payment1.isEmpty() && !payment2.isEmpty()) {
                payment1 = payment2;
                payment2 = ItemStack.EMPTY;
            }

            // Validation
            if (result.isEmpty()) {
                minecraft.gui.getChat().addMessage(
                        Component.translatable("gui.marketblocks.error.no_result_item")
                                .withStyle(ChatFormatting.RED)
                );
                playErrorSound();
                return;
            }

            if (payment1.isEmpty() && payment2.isEmpty()) {
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

            menu.getBlockEntity().setHasOfferClient(true);
            playSuccessSound();
            init(); // UI neu initialisieren

        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error creating offer", e);
            playErrorSound();
        }
    }

    private void deleteOffer() {
        NetworkHandler.sendToServer(new DeleteOfferPacket(menu.getBlockEntity().getBlockPos()));
        playClickSound();
    }

    public void onOfferDeleted() {
        menu.getBlockEntity().setHasOfferClient(false);
        init(); // UI neu initialisieren
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(BACKGROUND, i, j, 0, 0, imageWidth, imageHeight);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        // Update OfferTemplateButton basierend auf aktuellem Zustand
        if (blockEntity.hasOffer()) {
            // Zeige bestehendes Angebot
            offerButton.update(
                    blockEntity.getOfferPayment1(),
                    blockEntity.getOfferPayment2(),
                    blockEntity.getOfferResult(),
                    blockEntity.isOfferAvailable()
            );
        } else {
            // Zeige Live-Preview der aktuellen Slots
            ItemStack p1 = menu.slots.get(0).getItem();
            ItemStack p2 = menu.slots.get(1).getItem();
            if (p1.isEmpty() && !p2.isEmpty()) {
                p1 = p2;
                p2 = ItemStack.EMPTY;
            }
            offerButton.update(
                    p1,
                    p2,
                    menu.slots.get(2).getItem(),
                    true // Preview ist immer "verfügbar"
            );
        }

        // Render Handels-Pfeil
        boolean arrowActive = blockEntity.hasOffer() && blockEntity.isOfferAvailable();
        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, leftPos + 88, topPos + 35, 0, 0, 24, 16);
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
        } else {
            // Hinweis für Owner beim Erstellen
            Component createHint = Component.translatable("gui.marketblocks.create_hint");
            graphics.drawString(font, createHint, 8, 84, 0x808080, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        super.renderTooltip(graphics, x, y);

        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        // Tooltip für Handels-Pfeil
        if (isMouseOver(x, y, leftPos + 88, topPos + 35, 24, 16)) {
            if (blockEntity.hasOffer()) {
                Component tooltip = blockEntity.isOfferAvailable()
                        ? Component.translatable("gui.marketblocks.trade_available")
                        : Component.translatable("gui.marketblocks.trade_unavailable");
                graphics.renderTooltip(font, tooltip, x, y);
            }
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        // Live-Update wird automatisch durch renderBg() gemacht
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // Keine zusätzlichen Aktionen nötig
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

    private void onOfferClicked() {
        SmallShopBlockEntity blockEntity = menu.getBlockEntity();

        if (menu.isOwner()) {
            // Owner kann auf Preview klicken um Slots zu leeren (Reset-Funktion)
            if (!blockEntity.hasOffer()) {
                // Leere alle Slots
                for (int i = 0; i < 3; i++) {
                    menu.slots.get(i).set(ItemStack.EMPTY);
                }
                playClickSound();
            }
            return;
        }

        if (!blockEntity.hasOffer()) {
            return; // Kein Angebot vorhanden
        }

        ItemStack first = blockEntity.getOfferPayment1();
        ItemStack second = blockEntity.getOfferPayment2();

        // Normalisierung wie im CreateOffer
        if (first.isEmpty() && !second.isEmpty()) {
            first = second;
            second = ItemStack.EMPTY;
        }

        // Fülle Payment-Slots automatisch
        fillPaymentSlot(first, 0);
        if (!second.isEmpty()) {
            fillPaymentSlot(second, 1);
        }

        playClickSound();
    }

    private void fillPaymentSlot(ItemStack required, int slotIndex) {
        if (required.isEmpty()) {
            return;
        }

        // Mehrere Durchläufe um genug Items zu sammeln
        for (int iteration = 0; iteration < 64; iteration++) {
            Slot target = menu.slots.get(slotIndex);
            ItemStack current = target.getItem();
            int max = Math.min(target.getMaxStackSize(), required.getMaxStackSize());

            // Prüfe ob Slot voll ist oder falsches Item enthält
            if (!current.isEmpty() &&
                    (!ItemStack.isSameItemSameComponents(current, required) || current.getCount() >= max)) {
                break;
            }

            boolean moved = false;
            // Suche passendes Item im Spieler-Inventar
            for (int i = 3; i < menu.slots.size(); i++) {
                Slot invSlot = menu.slots.get(i);
                ItemStack invStack = invSlot.getItem();
                if (ItemStack.isSameItemSameComponents(invStack, required)) {
                    // Shift-Click simulieren
                    minecraft.gameMode.handleInventoryMouseClick(
                            menu.containerId, i, 0, ClickType.QUICK_MOVE, minecraft.player);
                    moved = true;
                    break;
                }
            }

            if (!moved) {
                break; // Keine passenden Items mehr gefunden
            }
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