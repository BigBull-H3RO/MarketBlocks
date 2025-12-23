package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.*;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import de.bigbull.marketblocks.util.custom.servershop.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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

    private static final int LIST_X_OFFSET = 5;
    private static final int LIST_Y_OFFSET = 18;
    private static final int LIST_WIDTH = 88;
    private static final int LIST_HEIGHT = 140;
    private static final int ROW_HEIGHT = 22;
    private static final int MAX_VISIBLE_ROWS = 6;
    private static final int SCROLLER_X_OFFSET = 96;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int PREVIEW_X_OFFSET = 136;
    private static final int PREVIEW_Y_OFFSET = 45;
    private static final int CONTROLS_X_OFFSET = 136;
    private static final int CONTROLS_Y_START = 100;

    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation ADD_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation SETTINGS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/settings.png");
    private static final ResourceLocation ARROW_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");

    private final List<Button> dynamicWidgets = new ArrayList<>();
    private final List<ServerShopOffer> visibleOffers = new ArrayList<>();

    private int scrollOffset;
    private int maxVisibleRows = MAX_VISIBLE_ROWS;
    private boolean isDragging;

    private ServerShopData cachedData = ServerShopClientState.data();
    private boolean isLocalEditMode = false;

    private OfferTemplateButton offerPreviewButton;
    private UUID selectedOfferId = null;

    public ServerShopScreen(ServerShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 276;
        this.imageHeight = 207;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 108;

        this.isLocalEditMode = menu.isEditor() && ServerShopClientState.getLastEditMode();
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

        if (current != cachedData || (perm != menu.isEditor())) {
            cachedData = current;
            if (!perm && isLocalEditMode) setEditMode(false);
            rebuildUi();
        }
        updatePreview();
    }

    private void updatePreview() {
        if (this.offerPreviewButton == null) return;

        if (isLocalEditMode) {
            ItemStack p1 = menu.getTemplateStack(0);
            ItemStack p2 = menu.getTemplateStack(1);
            ItemStack res = menu.getTemplateStack(2);
            this.offerPreviewButton.update(p1, p2, res, !res.isEmpty());
        } else {
            if (selectedOfferId != null) {
                ServerShopOffer offer = ServerShopManager.get().findOffer(selectedOfferId);
                if (offer != null) {
                    ItemStack p1 = offer.payments().isEmpty() ? ItemStack.EMPTY : offer.payments().get(0);
                    ItemStack p2 = offer.payments().size() > 1 ? offer.payments().get(1) : ItemStack.EMPTY;
                    this.offerPreviewButton.update(p1, p2, offer.result(), true);
                }
            } else {
                this.offerPreviewButton.update(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, false);
            }
        }
    }

    private void setEditMode(boolean active) {
        this.isLocalEditMode = active;
        ServerShopClientState.setLastEditMode(active);
        this.selectedOfferId = null;
        rebuildUi();
    }

    private void rebuildUi() {
        dynamicWidgets.forEach(this::removeWidget);
        dynamicWidgets.clear();
        visibleOffers.clear();

        if (!cachedData.pages().isEmpty()) {
            ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
            visibleOffers.addAll(page.offers());
        }

        buildPageSidebar();

        this.offerPreviewButton = new OfferTemplateButton(this.leftPos + PREVIEW_X_OFFSET, this.topPos + PREVIEW_Y_OFFSET, b -> {
            if (!isLocalEditMode && selectedOfferId != null) {
                NetworkHandler.sendToServer(new ServerShopAutoFillPacket(selectedOfferId));
            }
        });
        this.offerPreviewButton.active = !isLocalEditMode;
        addDynamic(this.offerPreviewButton);

        if (menu.isEditor()) {
            int toggleX = leftPos + imageWidth - 24;
            int toggleY = topPos + 6;
            IconButton toggleBtn = new IconButton(toggleX, toggleY, 16, 16, BUTTON_SPRITES, SETTINGS_ICON, b -> {
                setEditMode(!isLocalEditMode);
            }, Component.literal(isLocalEditMode ? "View Mode" : "Edit Mode"), () -> isLocalEditMode);
            addDynamic(toggleBtn);
        }

        if (isLocalEditMode) {
            buildEditorControls();
        }

        updateScrollLimits();
    }

    private void buildEditorControls() {
        if (cachedData.pages().isEmpty()) return;
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));

        int headerX = leftPos + 8;
        int headerY = topPos + 5;
        addDynamic(new IconButton(headerX, headerY, 16, 16, BUTTON_SPRITES, SETTINGS_ICON,
                b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.rename_page"), page.name(),
                        name -> NetworkHandler.sendToServer(new ServerShopRenamePagePacket(page.name(), name))),
                Component.translatable("gui.marketblocks.server_shop.rename_page"), () -> false));
        addDynamic(new IconButton(headerX + 18, headerY, 16, 16, BUTTON_SPRITES, DELETE_ICON,
                b -> NetworkHandler.sendToServer(new ServerShopDeletePagePacket(page.name())),
                Component.translatable("gui.marketblocks.server_shop.delete_page"), () -> false));

        int controlsX = leftPos + CONTROLS_X_OFFSET;
        int controlsY = topPos + CONTROLS_Y_START;

        // Button: ERSTELLEN (+)
        addDynamic(new IconButton(controlsX, controlsY, 20, 20, BUTTON_SPRITES, ADD_ICON,
                b -> NetworkHandler.sendToServer(new ServerShopAddOfferPacket(page.name())),
                Component.translatable("gui.marketblocks.server_shop.add_offer"), () -> false));

        // Selected Offer Controls
        if (selectedOfferId != null) {
            int startX = controlsX + 24;

            // DELETE
            addDynamic(new IconButton(startX, controlsY, 20, 20, BUTTON_SPRITES, DELETE_ICON,
                    b -> {
                        NetworkHandler.sendToServer(new ServerShopDeleteOfferPacket(selectedOfferId));
                        selectedOfferId = null;
                        rebuildUi();
                    },
                    Component.translatable("gui.marketblocks.server_shop.delete_offer"), () -> false));

            // Move Arrows
            addDynamic(Button.builder(Component.literal("↑"),
                            b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(selectedOfferId, page.name(), -1)))
                    .bounds(startX + 24, controlsY, 15, 20).build());
            addDynamic(Button.builder(Component.literal("↓"),
                            b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(selectedOfferId, page.name(), 1)))
                    .bounds(startX + 40, controlsY, 15, 20).build());
        }
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
        if (isLocalEditMode) {
            IconButton addPage = new IconButton(baseX, y + 6, 20, 20, BUTTON_SPRITES, ADD_ICON,
                    b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.add_page"), "", name -> NetworkHandler.sendToServer(new ServerShopCreatePagePacket(name))),
                    Component.translatable("gui.marketblocks.server_shop.add_page"), () -> false);
            addDynamic(addPage);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (visibleOffers.isEmpty()) {
            Component noOffersText = Component.translatable("gui.marketblocks.server_shop.no_offers");
            int textX = leftPos + LIST_X_OFFSET + (LIST_WIDTH - font.width(noOffersText)) / 2;
            int textY = topPos + LIST_Y_OFFSET + LIST_HEIGHT / 2 - 4;
            guiGraphics.drawString(font, noOffersText, textX, textY, 0x555555, false);
        }

        int listStartX = leftPos + LIST_X_OFFSET;
        int listStartY = topPos + LIST_Y_OFFSET;

        guiGraphics.enableScissor(listStartX, listStartY, listStartX + LIST_WIDTH + 2, listStartY + (MAX_VISIBLE_ROWS * ROW_HEIGHT));
        int end = Math.min(scrollOffset + maxVisibleRows, visibleOffers.size());
        int currentY = listStartY;

        for (int i = scrollOffset; i < end; i++) {
            ServerShopOffer offer = visibleOffers.get(i);
            boolean isSelected = offer.id().equals(selectedOfferId);
            renderListButton(guiGraphics, offer, listStartX, currentY, isSelected, mouseX, mouseY);
            currentY += ROW_HEIGHT;
        }
        guiGraphics.disableScissor();

        renderScroller(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderListButton(GuiGraphics graphics, ServerShopOffer offer, int x, int y, boolean isSelected, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT;
        ResourceLocation sprite = BUTTON_SPRITES.get(true, isSelected || hovered);

        graphics.blitSprite(sprite, x, y, LIST_WIDTH, ROW_HEIGHT - 2);

        // Koordinaten exakt wie OfferTemplateButton angepasst
        int itemY = y + 1;
        int startX = x + 2;

        if (!offer.payments().isEmpty()) {
            graphics.renderItem(offer.payments().get(0), startX + 2, itemY);
            graphics.renderItemDecorations(font, offer.payments().get(0), startX + 2, itemY);
        }

        if (offer.payments().size() > 1 && !offer.payments().get(1).isEmpty()) {
            graphics.renderItem(offer.payments().get(1), startX + 26, itemY);
            graphics.renderItemDecorations(font, offer.payments().get(1), startX + 26, itemY);
        }

        graphics.blit(ARROW_ICON, startX + 52, itemY + 4, 0, 0, 10, 9, 10, 9);

        graphics.renderItem(offer.result(), startX + 66, itemY);
        graphics.renderItemDecorations(font, offer.result(), startX + 66, itemY);

        if (hovered) {
            if (mouseX >= startX + 2 && mouseX <= startX + 18)
                graphics.renderTooltip(font, offer.payments().get(0), mouseX, mouseY);
            else if (mouseX >= startX + 26 && mouseX <= startX + 42 && offer.payments().size() > 1)
                graphics.renderTooltip(font, offer.payments().get(1), mouseX, mouseY);
            else if (mouseX >= startX + 66 && mouseX <= startX + 82)
                graphics.renderTooltip(font, offer.result(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isScrollBarActive() && isWithinScroller(mouseX, mouseY)) {
            isDragging = true; return true;
        }

        int listStartX = leftPos + LIST_X_OFFSET;
        int listStartY = topPos + LIST_Y_OFFSET;
        if (mouseX >= listStartX && mouseX < listStartX + LIST_WIDTH && mouseY >= listStartY && mouseY < listStartY + (MAX_VISIBLE_ROWS * ROW_HEIGHT)) {
            int clickedOffset = (int) ((mouseY - listStartY) / ROW_HEIGHT);
            int index = scrollOffset + clickedOffset;

            if (index >= 0 && index < visibleOffers.size()) {
                ServerShopOffer clickedOffer = visibleOffers.get(index);
                this.selectedOfferId = clickedOffer.id();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));

                if (!isLocalEditMode) {
                    NetworkHandler.sendToServer(new ServerShopFillRequestPacket(clickedOffer.id()));
                }

                rebuildUi();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

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

        if (isLocalEditMode) {
            guiGraphics.drawString(font, Component.translatable("gui.marketblocks.mode.edit_active"), leftPos + 180, topPos + 6, 0xFF5555, false);
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