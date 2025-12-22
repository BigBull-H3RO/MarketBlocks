package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.*;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopClientState;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopData;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopOffer;
import de.bigbull.marketblocks.util.custom.servershop.ServerShopPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ServerShopScreen extends AbstractContainerScreen<ServerShopMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/server_shop.png");

    // --- Layout Konstanten ---
    private static final int LIST_X_OFFSET = 5;
    private static final int LIST_Y_OFFSET = 18;
    private static final int LIST_WIDTH = 88;
    private static final int LIST_HEIGHT = 140;
    private static final int ROW_HEIGHT = 22; // Etwas höher für den Button-Look
    private static final int MAX_VISIBLE_ROWS = 6;

    private static final int SCROLLER_X_OFFSET = 96;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLLER_HEIGHT = 27;

    private static final int PREVIEW_X_OFFSET = 136;
    private static final int PREVIEW_Y_OFFSET = 45;

    // Steuerungsbereich rechts
    private static final int CONTROLS_X_OFFSET = 120;
    private static final int CONTROLS_Y_START = 100;

    // --- Texturen ---
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation ADD_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation SETTINGS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/settings.png");
    private static final ResourceLocation UPDATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/input_output.png");
    private static final ResourceLocation ARROW_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");

    // Icon für den Modus-Wechsel (Editor <-> View)
    private static final ResourceLocation EDIT_MODE_ICON = ResourceLocation.withDefaultNamespace("textures/gui/sprites/icon/accessibility.png"); // Vanilla Icon als Platzhalter oder eigenes

    // --- State ---
    private final List<Button> dynamicWidgets = new ArrayList<>();
    private final List<ServerShopOffer> visibleOffers = new ArrayList<>();

    private int scrollOffset;
    private int maxVisibleRows = MAX_VISIBLE_ROWS;
    private boolean isDragging;

    private ServerShopData cachedData = ServerShopClientState.data();
    private boolean cachedEditorPermission = menu.isEditor();

    // Der "lokale" Editor Modus. Nur wenn true (und Permission da ist), werden Editor-Buttons angezeigt.
    private boolean isLocalEditMode = false;

    private OfferTemplateButton offerPreviewButton;
    private UUID selectedOfferId = null;

    public ServerShopScreen(ServerShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 276;
        this.imageHeight = 207;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 108;

        // Standardmäßig Editor-Modus an, wenn man Rechte hat
        this.isLocalEditMode = menu.isEditor();
    }

    @Override
    protected void init() {
        super.init();
        rebuildUi();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ServerShopData current = ServerShopClientState.data();
        boolean perm = menu.isEditor();

        if (current != cachedData || cachedEditorPermission != perm) {
            cachedData = current;
            cachedEditorPermission = perm;
            // Wenn Rechte verloren gehen, Editor-Modus aus
            if (!perm) isLocalEditMode = false;
            rebuildUi();
        }

        // Live-Update der Vorschau (Was liegt in den Slots?)
        if (this.offerPreviewButton != null) {
            ItemStack p1 = this.menu.getTemplateStack(0);
            ItemStack p2 = this.menu.getTemplateStack(1);
            ItemStack result = this.menu.getTemplateStack(2);
            this.offerPreviewButton.update(p1, p2, result, !result.isEmpty());
        }
    }

    private void rebuildUi() {
        dynamicWidgets.forEach(this::removeWidget);
        dynamicWidgets.clear();
        visibleOffers.clear();

        // Daten laden
        if (!cachedData.pages().isEmpty()) {
            ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
            visibleOffers.addAll(page.offers());
        }

        // Sidebar (Seitenwahl)
        buildPageSidebar();

        // Vorschau Button (Über den Slots)
        // Dient als Anzeige. Klickbar für Auto-Fill (als Komfort-Funktion)
        this.offerPreviewButton = new OfferTemplateButton(this.leftPos + PREVIEW_X_OFFSET, this.topPos + PREVIEW_Y_OFFSET, b -> {
            if (selectedOfferId != null && !isLocalEditMode) {
                // Im Kaufmodus: Klick auf Vorschau könnte Kauf auslösen oder Items nachfüllen?
                // Aktuell: Auto-Fill aus Inventar in Slots
                // NetworkHandler.sendToServer(new AutoFillPacket...); // Optional
            }
        });
        // Vorschau ist immer aktiv, damit man Tooltips sieht
        addDynamic(this.offerPreviewButton);

        // Editor Controls (Nur wenn Modus aktiv)
        if (isLocalEditMode && menu.isEditor()) {
            buildEditorControls();
        }

        // Toggle Button für Editor Modus (Oben Rechts im GUI, wenn man Rechte hat)
        if (menu.isEditor()) {
            int toggleX = leftPos + imageWidth - 24;
            int toggleY = topPos + 6;

            Component tooltip = isLocalEditMode ?
                    Component.translatable("gui.marketblocks.mode.view") :
                    Component.translatable("gui.marketblocks.mode.edit");

            IconButton toggleBtn = new IconButton(toggleX, toggleY, 16, 16, BUTTON_SPRITES, SETTINGS_ICON, b -> {
                isLocalEditMode = !isLocalEditMode;
                // Auswahl zurücksetzen beim Wechsel, um Verwirrung zu vermeiden
                selectedOfferId = null;
                // Slots leeren beim Wechsel (optional, aber sauberer)
                // NetworkHandler.sendToServer(new ClearTemplatePacket()); // Müsste man implementieren, hier erstmal egal
                rebuildUi();
            }, tooltip, () -> isLocalEditMode);
            addDynamic(toggleBtn);
        }

        updateScrollLimits();
    }

    private void buildPageSidebar() {
        List<ServerShopPage> pages = cachedData.pages();
        int baseX = leftPos - 105;
        int y = topPos + 10;

        for (int i = 0; i < pages.size(); i++) {
            int index = i;
            ServerShopPage page = pages.get(i);
            String name = page.name().isEmpty() ? "Page " + (i + 1) : page.name();

            Button button = Button.builder(Component.literal(name), b -> {
                if (menu.selectedPage() != index) {
                    menu.setSelectedPageClient(index);
                    NetworkHandler.sendToServer(new ServerShopSelectPagePacket(index));
                    this.selectedOfferId = null;
                }
            }).bounds(baseX, y, 100, 20).build();
            button.active = menu.selectedPage() != index;
            addDynamic(button);
            y += 22;
        }

        // Seite hinzufügen nur im Edit Mode
        if (isLocalEditMode) {
            Component tooltip = Component.translatable("gui.marketblocks.server_shop.add_page");
            IconButton addPage = new IconButton(baseX, y + 6, 20, 20, BUTTON_SPRITES, ADD_ICON,
                    b -> openTextInput(tooltip, "", name -> NetworkHandler.sendToServer(new ServerShopCreatePagePacket(name))),
                    tooltip, () -> false);
            addDynamic(addPage);
        }
    }

    private void buildEditorControls() {
        if (cachedData.pages().isEmpty()) return;
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));

        // Page Rename/Delete (Oben Links)
        int headerX = leftPos + 8;
        int headerY = topPos + 5;

        IconButton renamePage = new IconButton(headerX, headerY, 16, 16, BUTTON_SPRITES, SETTINGS_ICON,
                b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.rename_page"), page.name(),
                        name -> NetworkHandler.sendToServer(new ServerShopRenamePagePacket(page.name(), name))),
                Component.translatable("gui.marketblocks.server_shop.rename_page"), () -> false);
        addDynamic(renamePage);

        IconButton deletePage = new IconButton(headerX + 18, headerY, 16, 16, BUTTON_SPRITES, DELETE_ICON,
                b -> NetworkHandler.sendToServer(new ServerShopDeletePagePacket(page.name())),
                Component.translatable("gui.marketblocks.server_shop.delete_page"), () -> false);
        addDynamic(deletePage);


        // Offer Management (Rechts bei den Slots)
        int controlsX = leftPos + 136;
        int controlsY = topPos + 100; // Unter den Slots

        // 1. ADD OFFER (Immer da im Edit Mode)
        // Speichert den INHALT DER SLOTS als neues Angebot
        Component addTooltip = Component.translatable("gui.marketblocks.server_shop.add_offer");
        IconButton addOfferBtn = new IconButton(controlsX, controlsY, 20, 20, BUTTON_SPRITES, ADD_ICON,
                b -> NetworkHandler.sendToServer(new ServerShopAddOfferPacket(page.name())),
                addTooltip, () -> false);
        addDynamic(addOfferBtn);

        // 2. EXISTIERENDES ANGEBOT BEARBEITEN (Nur wenn eins ausgewählt ist)
        if (selectedOfferId != null) {
            int startX = controlsX + 24;

            // Update: Überschreibt das ausgewählte Angebot mit den Items aus den Slots
            IconButton updateBtn = new IconButton(startX, controlsY, 20, 20, BUTTON_SPRITES, UPDATE_ICON,
                    b -> NetworkHandler.sendToServer(new ServerShopReplaceOfferStacksPacket(selectedOfferId)),
                    Component.translatable("gui.marketblocks.server_shop.replace_offer"), () -> false);
            addDynamic(updateBtn);

            // Delete: Löscht das ausgewählte Angebot
            IconButton deleteBtn = new IconButton(startX + 22, controlsY, 20, 20, BUTTON_SPRITES, DELETE_ICON,
                    b -> {
                        NetworkHandler.sendToServer(new ServerShopDeleteOfferPacket(selectedOfferId));
                        selectedOfferId = null; // Auswahl aufheben
                    },
                    Component.translatable("gui.marketblocks.server_shop.delete_offer"), () -> false);
            addDynamic(deleteBtn);

            // Move Up/Down
            Button upBtn = Button.builder(Component.literal("↑"),
                            b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(selectedOfferId, page.name(), -1)))
                    .bounds(startX + 44, controlsY, 15, 20).build();
            addDynamic(upBtn);

            Button downBtn = Button.builder(Component.literal("↓"),
                            b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(selectedOfferId, page.name(), 1)))
                    .bounds(startX + 60, controlsY, 15, 20).build();
            addDynamic(downBtn);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (visibleOffers.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.marketblocks.server_shop.no_offers"), leftPos + 15, topPos + 40, 0xAAAAAA, false);
        }

        // --- LISTE RENDERN ---
        int listStartX = leftPos + LIST_X_OFFSET;
        int listStartY = topPos + LIST_Y_OFFSET;

        guiGraphics.enableScissor(listStartX, listStartY, listStartX + LIST_WIDTH + 2, listStartY + (MAX_VISIBLE_ROWS * ROW_HEIGHT));

        int end = Math.min(scrollOffset + maxVisibleRows, visibleOffers.size());
        int currentY = listStartY;

        for (int i = scrollOffset; i < end; i++) {
            ServerShopOffer offer = visibleOffers.get(i);
            boolean isSelected = offer.id().equals(selectedOfferId);

            // Wir zeichnen hier quasi einen "Fake" OfferTemplateButton
            renderListButton(guiGraphics, offer, listStartX, currentY, isSelected, mouseX, mouseY);

            currentY += ROW_HEIGHT;
        }

        guiGraphics.disableScissor();

        renderScroller(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderListButton(GuiGraphics graphics, ServerShopOffer offer, int x, int y, boolean isSelected, int mouseX, int mouseY) {
        // 1. Hintergrund (Button Style)
        boolean hovered = mouseX >= x && mouseX < x + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT;
        ResourceLocation sprite = BUTTON_SPRITES.get(true, isSelected || hovered);

        // Zeichne Button-Hintergrund über die ganze Breite der Liste
        // Wir nutzen blitSprite für skalierbare Button-Texturen (9-Slice wenn möglich, sonst strecken)
        graphics.blitSprite(sprite, x, y, LIST_WIDTH, ROW_HEIGHT - 2); // -2 für kleinen Abstand

        // 2. Items und Pfeil
        int itemY = y + (ROW_HEIGHT - 18) / 2; // Zentriert vertikal
        int startX = x + 4;

        // Payment 1
        if (!offer.payments().isEmpty()) {
            graphics.renderItem(offer.payments().get(0), startX, itemY);
            // Keine Decorations (Menge) rendern? Doch, ist wichtig.
            graphics.renderItemDecorations(font, offer.payments().get(0), startX, itemY);
        }

        // Payment 2 (kleiner versetzt oder daneben?)
        // Platz ist eng (88px).
        // 16px (Item) + 16px (Item) + 10px (Arrow) + 16px (Result) = 58px. Passt.
        if (offer.payments().size() > 1 && !offer.payments().get(1).isEmpty()) {
            graphics.renderItem(offer.payments().get(1), startX + 18, itemY);
            graphics.renderItemDecorations(font, offer.payments().get(1), startX + 18, itemY);
        }

        // Pfeil
        graphics.blit(ARROW_ICON, startX + 38, itemY + 4, 0, 0, 10, 9, 10, 9);

        // Result
        graphics.renderItem(offer.result(), startX + 54, itemY);
        graphics.renderItemDecorations(font, offer.result(), startX + 54, itemY);

        // 3. Tooltips (Nur wenn Hover)
        if (hovered) {
            if (mouseX >= startX && mouseX <= startX + 16)
                graphics.renderTooltip(font, offer.payments().get(0), mouseX, mouseY);
            else if (mouseX >= startX + 18 && mouseX <= startX + 34 && offer.payments().size() > 1)
                graphics.renderTooltip(font, offer.payments().get(1), mouseX, mouseY);
            else if (mouseX >= startX + 54 && mouseX <= startX + 70)
                graphics.renderTooltip(font, offer.result(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isScrollBarActive()) {
            if (isWithinScroller(mouseX, mouseY)) {
                isDragging = true;
                return true;
            }
        }

        int listStartX = leftPos + LIST_X_OFFSET;
        int listStartY = topPos + LIST_Y_OFFSET;
        int listWidth = LIST_WIDTH;
        int listHeight = MAX_VISIBLE_ROWS * ROW_HEIGHT;

        if (mouseX >= listStartX && mouseX < listStartX + listWidth && mouseY >= listStartY && mouseY < listStartY + listHeight) {
            int clickedOffset = (int) ((mouseY - listStartY) / ROW_HEIGHT);
            int index = scrollOffset + clickedOffset;

            if (index >= 0 && index < visibleOffers.size()) {
                ServerShopOffer clickedOffer = visibleOffers.get(index);
                this.selectedOfferId = clickedOffer.id();

                Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));

                // Server: Bitte fülle die Slots mit diesem Angebot
                NetworkHandler.sendToServer(new ServerShopFillRequestPacket(clickedOffer.id()));

                rebuildUi(); // Buttons aktualisieren
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // --- Standard Methods ---

    private void updateScrollLimits() {
        int totalRows = visibleOffers.size();
        maxVisibleRows = MAX_VISIBLE_ROWS;
        int maxScroll = Math.max(0, totalRows - maxVisibleRows);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
    }

    private void updateScrollOffset(int newOffset) {
        int totalRows = visibleOffers.size();
        int maxScroll = Math.max(0, totalRows - maxVisibleRows);
        scrollOffset = Mth.clamp(newOffset, 0, maxScroll);
    }

    private void addDynamic(Button button) {
        addRenderableWidget(button);
        dynamicWidgets.add(button);
    }

    private void openTextInput(Component title, String initial, Consumer<String> onConfirm) {
        minecraft.setScreen(new TextInputScreen(this, title, initial, onConfirm));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);

        // Titel: Modus anzeigen (für den Owner zur Info)
        if (isLocalEditMode) {
            guiGraphics.drawString(font, Component.translatable("gui.marketblocks.mode.edit_active"), leftPos + 160, topPos + 6, 0xFF5555, false);
        }
    }

    private void renderScroller(GuiGraphics guiGraphics) {
        if (!isScrollBarActive()) return;
        int scrollerX = leftPos + SCROLLER_X_OFFSET;
        int scrollerY = topPos + LIST_Y_OFFSET;

        int maxScroll = visibleOffers.size() - MAX_VISIBLE_ROWS;
        if (maxScroll <= 0) return;

        float progress = (float) scrollOffset / (float) maxScroll;
        int handleTravel = (MAX_VISIBLE_ROWS * ROW_HEIGHT) - SCROLLER_HEIGHT;
        int handleY = scrollerY + Mth.floor(progress * handleTravel);

        guiGraphics.fill(scrollerX, handleY, scrollerX + SCROLLER_WIDTH, handleY + SCROLLER_HEIGHT, isDragging ? 0xFFAAAAAA : 0xFF888888);
    }

    private boolean isScrollBarActive() {
        return visibleOffers.size() > MAX_VISIBLE_ROWS;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isScrollBarActive()) {
            int direction = (int) Math.signum(scrollY);
            if (direction != 0) updateScrollOffset(scrollOffset - direction);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && isScrollBarActive()) {
            int scrollerY = topPos + LIST_Y_OFFSET;
            int handleTravel = (MAX_VISIBLE_ROWS * ROW_HEIGHT) - SCROLLER_HEIGHT;
            if (handleTravel > 0) {
                double relative = (mouseY - scrollerY - (SCROLLER_HEIGHT / 2.0D)) / handleTravel;
                relative = Mth.clamp(relative, 0.0D, 1.0D);
                int maxScroll = visibleOffers.size() - MAX_VISIBLE_ROWS;
                int newOffset = Mth.floor(relative * maxScroll + 0.5D);
                updateScrollOffset(newOffset);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isWithinScroller(double mouseX, double mouseY) {
        int scrollerX = leftPos + SCROLLER_X_OFFSET;
        int scrollerY = topPos + LIST_Y_OFFSET;
        int scrollerHeight = MAX_VISIBLE_ROWS * ROW_HEIGHT;
        return mouseX >= scrollerX && mouseX < scrollerX + SCROLLER_WIDTH && mouseY >= scrollerY && mouseY < scrollerY + scrollerHeight;
    }

    private static class TextInputScreen extends Screen {
        private final ServerShopScreen parent;
        private final String initialValue;
        private final Consumer<String> onConfirm;
        private EditBox input;

        protected TextInputScreen(ServerShopScreen parent, Component title, String initialValue, Consumer<String> onConfirm) {
            super(title);
            this.parent = parent;
            this.initialValue = initialValue == null ? "" : initialValue;
            this.onConfirm = onConfirm;
        }

        @Override
        protected void init() {
            this.input = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Component.empty());
            this.input.setMaxLength(64);
            this.input.setValue(initialValue);
            addRenderableWidget(input);
            addRenderableWidget(Button.builder(CommonComponents.GUI_OK,
                            b -> { minecraft.setScreen(parent); onConfirm.accept(input.getValue()); })
                    .bounds(this.width / 2 - 100, this.height / 2 + 16, 96, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,
                    b -> minecraft.setScreen(parent)).bounds(this.width / 2 + 4, this.height / 2 + 16, 96, 20).build());
            setInitialFocus(input);
        }

        @Override public void onClose() { minecraft.setScreen(parent); }
        @Override public void render(GuiGraphics g, int x, int y, float p) {
            renderBackground(g, x, y, p);
            g.drawString(font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.height / 2 - 40, 0xFFFFFF, false);
            super.render(g, x, y, p);
        }
        @Override public boolean keyPressed(int k, int s, int m) {
            if (k == 257 || k == 335) { minecraft.setScreen(parent); onConfirm.accept(input.getValue()); return true; }
            return super.keyPressed(k, s, m);
        }
    }
}