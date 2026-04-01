package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopAutoFillPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopSelectPagePacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopSetOfferPacket;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopToggleEditModePacket;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.IconButton;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import de.bigbull.marketblocks.util.custom.screen.gui.VanillaIconButton;
import de.bigbull.marketblocks.shop.server.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
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

/**
 * Main shop UI orchestration.
 * Navigation guide:
 * - Lifecycle/Data sync: init(), containerTick(), rebuildUi()
 * - Selection/preview state: updatePreview(), findOfferOnSelectedPage(), sanitizeSelectionState()
 * - Edit controls wiring: buildEditorControls(), createEditorControlsContext(), ScreenEditorCallbacks
 * - Rendering pipeline: render(), renderModalBackdrop(), renderContent(), renderStaticOverlay()
 * - Input handling: mouseClicked(), mouseScrolled(), mouseDragged(), mouseReleased()
 * - Modal entry points: openOfferLimitsEditor(), openOfferPricingEditor(), openTextInput()
**/
public class ServerShopScreen extends AbstractContainerScreen<ServerShopMenu> {
    private static final int BACKDROP_MOUSE_OFFSCREEN = -10000;
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/server_shop.png");
    private static final int STATUS_LINE_HEIGHT = 12;
    private static final int RIGHT_SIDE_GAP = 2;
    private static final int RIGHT_BUTTON_SIZE = 20;
    private static final int RIGHT_BUTTON_GAP = 4;

    // List
    private static final int ROW_HEIGHT = 20;
    private static final int MAX_VISIBLE_ROWS = 9;
    private static final int LIST_X_OFFSET = 5;
    private static final int LIST_Y_OFFSET = 19;
    private static final int LIST_WIDTH = 88;
    private static final int LIST_HEIGHT = ROW_HEIGHT * MAX_VISIBLE_ROWS + 7;

    // Scroller
    private static final int SCROLLER_X_OFFSET = 94;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_BOTTOM_INSET = 7;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller_disabled");

    // Preview
    private static final int PREVIEW_X_OFFSET = 144;
    private static final int PREVIEW_Y_OFFSET = 43;

    // Controls (Buttons unter den Slots)
    private static final int CONTROLS_X_START = 160; // Beginnt linksbündig mit Slot 1
    private static final int CONTROLS_Y_START = 102; // Unterhalb der Slots (Slots sind bei Y=78, Höhe 18)

    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_disabled.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_highlighted.png"),
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/button/button_selected.png")
    );

    private static final ResourceLocation ADD_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation SETTINGS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/settings.png");
    private static final ResourceLocation ADD_PAGE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/add_page.png");
    private static final ResourceLocation DELETE_PAGE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete_page.png");
    private static final ResourceLocation RENAME_PAGE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/rename_page.png");
    private static final ResourceLocation CLEAR_SELECTION_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/clear_selection.png");
    private static final ResourceLocation MOVE_UP_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/move_up_mini.png");
    private static final ResourceLocation MOVE_DOWN_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/move_down_mini.png");
    // Reuse existing icons to avoid missing-texture placeholders when dedicated assets are absent.
    private static final ResourceLocation LIMITS_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/edit_limits.png");
    private static final ResourceLocation PRICING_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/edit_pricing.png");
    private static final ResourceLocation ARROW_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/out_of_stock.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    private final List<AbstractWidget> dynamicWidgets = new ArrayList<>();
    private final List<AbstractWidget> foregroundWidgets = new ArrayList<>();
    private final List<ServerShopOffer> visibleOffers = new ArrayList<>();

    private int scrollOffset;
    private int maxVisibleRows = MAX_VISIBLE_ROWS;
    private boolean isDragging;

    private ServerShopData cachedData = ServerShopClientState.data();
    private boolean isLocalEditMode;
    private int lastSelectedPage = -1;

    private OfferTemplateButton offerPreviewButton;
    private UUID selectedOfferId = null;
    private final ServerShopEditorControls editorControls = new ServerShopEditorControls();
    private final ServerShopEditorControls.Callbacks editorCallbacks = new ScreenEditorCallbacks();
    private final ServerShopPageSidebar pageSidebar = new ServerShopPageSidebar();
    private final ServerShopPageSidebar.Callbacks pageSidebarCallbacks = new ScreenPageSidebarCallbacks();
    private final ServerShopOverlayRenderer overlayRenderer = new ServerShopOverlayRenderer();

    // Lifecycle / bootstrapping
    public ServerShopScreen(ServerShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 276;
        this.imageHeight = 207;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 108;

        initializeLocalEditMode();
    }

    private void initializeLocalEditMode() {
        boolean initialMode = menu.canUseEditMode() && ServerShopClientState.getLastEditMode();
        this.isLocalEditMode = initialMode;
        if (!initialMode) {
            return;
        }

        menu.setEditMode(true);
        NetworkHandler.sendToServer(new ServerShopToggleEditModePacket(true));
    }

    @Override
    protected void init() {
        super.init();
        rebuildUi();
    }

    // Runtime sync (client cache, permissions, page changes)
    @Override
    public void containerTick() {
        super.containerTick();
        ServerShopData current = ServerShopClientState.data();
        boolean canUseEditMode = menu.canUseEditMode();
        boolean dataChanged = current != cachedData;

        if (!canUseEditMode && isLocalEditMode) {
            setEditMode(false);
        }

        if (dataChanged) {
            cachedData = current;
        }

        boolean pageChanged = menu.selectedPage() != lastSelectedPage;
        if (dataChanged || pageChanged) {
            scrollOffset = 0;
            rebuildUi();
        } else {
            sanitizeSelectionState();
        }
        updatePreview();
    }

    // Selection + preview state
    private void updatePreview() {
        if (this.offerPreviewButton == null) {
            return;
        }

        ServerShopOffer offer = selectedOfferId != null ? findOfferOnSelectedPage(selectedOfferId) : null;
        if (offer != null) {
            displayOfferInPreview(offer);
            return;
        }

        if (selectedOfferId != null) {
            clearSelectedOffer();
        }

        if (isLocalEditMode) {
            showTemplateSlotsInPreview();
        } else {
            this.offerPreviewButton.visible = false;
        }
    }

    private void displayOfferInPreview(ServerShopOffer offer) {
        ItemStack[] payments = normalizePaymentPair(offer.effectivePayments());
        ItemStack p1 = payments[0];
        ItemStack p2 = payments[1];

        this.offerPreviewButton.visible = true;
        this.offerPreviewButton.update(p1, p2, offer.result(), true);
    }

    private void showTemplateSlotsInPreview() {
        ItemStack[] payments = normalizePaymentPair(menu.getTemplateStack(0), menu.getTemplateStack(1));
        ItemStack p1 = payments[0];
        ItemStack p2 = payments[1];
        ItemStack res = menu.getTemplateStack(2);

        if (!p1.isEmpty() || !res.isEmpty()) {
            this.offerPreviewButton.visible = true;
            this.offerPreviewButton.update(p1, p2, res, true);
        } else {
            this.offerPreviewButton.visible = false;
        }
    }

    private ItemStack[] normalizePaymentPair(List<ItemStack> effectivePayments) {
        ItemStack p1 = effectivePayments.isEmpty() ? ItemStack.EMPTY : effectivePayments.get(0);
        ItemStack p2 = effectivePayments.size() > 1 ? effectivePayments.get(1) : ItemStack.EMPTY;
        return normalizePaymentPair(p1, p2);
    }

    private ItemStack[] normalizePaymentPair(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty()) {
            return new ItemStack[]{p2, ItemStack.EMPTY};
        }
        return new ItemStack[]{p1, p2};
    }

    private void sanitizeSelectionState() {
        menu.clampSelectedPage(cachedData.pages().size());

        if (selectedOfferId != null && !hasOfferOnSelectedPage(selectedOfferId)) {
            clearSelectedOffer();
        } else if (!isLocalEditMode && selectedOfferId == null) {
            menu.setCurrentTradingOffer(null);
        }
    }

    private void clearSelectedOffer() {
        this.selectedOfferId = null;
        if (!isLocalEditMode) {
            menu.setCurrentTradingOffer(null);
        }
    }

    private ServerShopOffer findOfferOnSelectedPage(UUID offerId) {
        if (offerId == null || cachedData == null) {
            return null;
        }

        List<ServerShopPage> pages = cachedData.pages();
        if (pages.isEmpty()) {
            return null;
        }

        int pageIndex = Math.min(Math.max(menu.selectedPage(), 0), pages.size() - 1);
        for (ServerShopOffer offer : pages.get(pageIndex).offers()) {
            if (offer.id().equals(offerId)) {
                return offer;
            }
        }
        return null;
    }

    private boolean hasOfferOnSelectedPage(UUID offerId) {
        return findOfferOnSelectedPage(offerId) != null;
    }

    private static String normalizeTextInput(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Rebuilds all dynamic widgets for current mode/page/selection.
     */
    private void setEditMode(boolean active) {
        if (active && !menu.canUseEditMode()) {
            return;
        }
        this.isLocalEditMode = active;
        menu.setEditMode(active);
        ServerShopClientState.setLastEditMode(active);
        clearSelectedOffer();
        rebuildUi();
    }

    private void rebuildUi() {
        resetDynamicUi();
        sanitizeSelectionState();
        updateVisibleOffersForSelectedPage();
        buildPageSidebar();
        buildPreviewButton();
        buildEditModeToggleButton();

        if (isLocalEditMode) {
            buildEditorControls();
        }

        updateScrollLimits();
        lastSelectedPage = menu.selectedPage();
    }

    private void resetDynamicUi() {
        dynamicWidgets.forEach(this::removeWidget);
        dynamicWidgets.clear();
        foregroundWidgets.clear();
        visibleOffers.clear();
        pageSidebar.reset();
    }

    private void updateVisibleOffersForSelectedPage() {
        if (cachedData.pages().isEmpty()) {
            return;
        }
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
        visibleOffers.addAll(page.offers());
    }

    private void buildPreviewButton() {
        this.offerPreviewButton = new OfferTemplateButton(this.leftPos + PREVIEW_X_OFFSET, this.topPos + PREVIEW_Y_OFFSET, ignored -> {
            if (!isLocalEditMode && selectedOfferId != null && minecraft != null && minecraft.player != null) {
                NetworkHandler.sendToServer(new ServerShopAutoFillPacket(selectedOfferId));
            }
        });
        this.offerPreviewButton.active = !isLocalEditMode;
        this.offerPreviewButton.visible = false;
        addDynamic(this.offerPreviewButton);
    }

    private void buildEditModeToggleButton() {
        if (!menu.canUseEditMode()) {
            return;
        }

        int toggleX = leftPos + imageWidth - 26;
        int toggleY = topPos + 6;
        IconButton toggleBtn = new IconButton(toggleX, toggleY, 20, 20, BUTTON_SPRITES, SETTINGS_ICON, ignored -> {
            NetworkHandler.sendToServer(new ServerShopToggleEditModePacket(!isLocalEditMode));
            setEditMode(!isLocalEditMode);
        }, Component.translatable(isLocalEditMode
                ? "gui.marketblocks.server_shop.mode.view"
                : "gui.marketblocks.server_shop.mode.edit"), () -> isLocalEditMode);
        addDynamic(toggleBtn);
    }

    // Edit controls wiring
    private void buildEditorControls() {
        editorControls.build(
                createEditorControlsContext(),
                editorCallbacks
        );
    }

    private ServerShopEditorControls.Context createEditorControlsContext() {
        return new ServerShopEditorControls.Context(
                this.leftPos,
                this.topPos,
                menu.selectedPage(),
                this.cachedData.pages(),
                this.selectedOfferId,
                LIST_X_OFFSET,
                LIST_Y_OFFSET,
                LIST_WIDTH,
                ROW_HEIGHT,
                this.scrollOffset,
                this.maxVisibleRows,
                PREVIEW_X_OFFSET,
                PREVIEW_Y_OFFSET,
                CONTROLS_X_START,
                CONTROLS_Y_START,
                RIGHT_BUTTON_SIZE,
                RIGHT_BUTTON_GAP,
                BUTTON_SPRITES,
                ADD_ICON,
                DELETE_ICON,
                ADD_PAGE_ICON,
                DELETE_PAGE_ICON,
                RENAME_PAGE_ICON,
                CLEAR_SELECTION_ICON,
                MOVE_UP_ICON,
                MOVE_DOWN_ICON,
                LIMITS_ICON,
                PRICING_ICON
        );
    }

    private void openOfferLimitsEditor(ServerShopOffer offer) {
        if (minecraft != null) {
            minecraft.setScreen(new OfferLimitsEditor(this, offer.id(), offer.limits()));
        }
    }

    private void openOfferPricingEditor(ServerShopOffer offer) {
        if (minecraft != null) {
            minecraft.setScreen(new OfferPricingEditor(this, offer.id(), offer.pricing()));
        }
    }

    private int rightEditorButtonsX() {
        int desiredX = leftPos + imageWidth + RIGHT_SIDE_GAP;
        return Math.min(desiredX, this.width - RIGHT_BUTTON_SIZE - 8);
    }

    private int rightEditorButtonsY() {
        int totalButtonsHeight = (RIGHT_BUTTON_SIZE * 2) + RIGHT_BUTTON_GAP;
        return topPos + (imageHeight - totalButtonsHeight) / 2 - 16;
    }

    private void buildPageSidebar() {
        pageSidebar.buildButtons(
                createPageSidebarContext(),
                pageSidebarCallbacks
        );
    }

    private ServerShopPageSidebar.Context createPageSidebarContext() {
        return new ServerShopPageSidebar.Context(
                this.leftPos,
                this.topPos,
                menu.selectedPage(),
                this.cachedData.pages(),
                this.font
        );
    }

    private void onPageSelected(int index) {
        if (menu.selectedPage() == index) {
            return;
        }

        menu.setSelectedPageClient(index);
        menu.setCurrentTradingOffer(null);
        clearSelectedOffer();
        scrollOffset = 0;
        rebuildUi();
        NetworkHandler.sendToServer(new ServerShopSelectPagePacket(index));
    }

    private void openTextInput(Component title, String initial, boolean allowEmpty, Consumer<String> onConfirm) {
        if (minecraft != null) {
            minecraft.setScreen(new TextInputScreen(this, title, initial, allowEmpty, onConfirm));
        }
    }

    // Render pipeline
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderContent(guiGraphics, mouseX, mouseY, partialTick, false);
        pageSidebar.renderDelayedTooltip(createPageSidebarContext(), guiGraphics, mouseX, mouseY);
    }

    /**
     * Renders a static backdrop for modal overlays without using AbstractContainerScreen#render.
     */
    public void renderModalBackdrop(GuiGraphics guiGraphics, float partialTick) {
        // Keep the vanilla menu texture/background so the backdrop matches normal ServerShop visuals.
        this.renderMenuBackground(guiGraphics);
        // Keep the exact same visual pipeline as the regular screen, but disable interactions.
        renderContent(guiGraphics, BACKDROP_MOUSE_OFFSCREEN, BACKDROP_MOUSE_OFFSCREEN, partialTick, true);
    }

    /**
     * Shared render path for normal screen and modal backdrop mode.
     */
    private void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, boolean suppressInteractions) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderStaticOverlay(guiGraphics, mouseX, mouseY, suppressInteractions);
        renderForegroundWidgets(guiGraphics, mouseX, mouseY, partialTick);

        if (!suppressInteractions) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderForegroundWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
        for (AbstractWidget widget : foregroundWidgets) {
            if (widget.visible) {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
        guiGraphics.pose().popPose();
    }

    private void renderStaticOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean suppressInteractions) {
        overlayRenderer.render(guiGraphics, createOverlayContext(mouseX, mouseY, suppressInteractions));
        renderScroller(guiGraphics);
    }

    private ServerShopOverlayRenderer.Context createOverlayContext(int mouseX, int mouseY, boolean suppressInteractions) {
        return new ServerShopOverlayRenderer.Context(
                this.font,
                mouseX,
                mouseY,
                suppressInteractions,
                this.leftPos,
                this.topPos,
                !this.cachedData.pages().isEmpty(),
                LIST_X_OFFSET,
                LIST_Y_OFFSET,
                LIST_WIDTH,
                LIST_HEIGHT,
                ROW_HEIGHT,
                MAX_VISIBLE_ROWS,
                PREVIEW_X_OFFSET,
                PREVIEW_Y_OFFSET,
                STATUS_LINE_HEIGHT,
                this.scrollOffset,
                this.maxVisibleRows,
                this.visibleOffers,
                this.selectedOfferId,
                BUTTON_SPRITES,
                ARROW_ICON,
                this::findOfferOnSelectedPage,
                this::normalizePaymentPair,
                this::getUnavailableStateIcon,
                this::getDisplayRestockSeconds
        );
    }

    private ResourceLocation getUnavailableStateIcon(ServerShopOfferViewState viewState) {
        if (viewState.remainingDailyPurchases().isPresent() && viewState.remainingDailyPurchases().get() <= 0) {
            return TRADE_ARROW_DISABLED_ICON;
        }
        if (viewState.remainingStock().isPresent() && viewState.remainingStock().get() <= 0) {
            return OUT_OF_STOCK_ICON;
        }
        if (viewState.maxPurchasable() <= 0) {
            return TRADE_ARROW_DISABLED_ICON;
        }
        return null;
    }

    private java.util.Optional<Integer> getDisplayRestockSeconds(ServerShopOfferViewState viewState) {
        if (viewState == null || viewState.restockSecondsRemaining().isEmpty()) {
            return java.util.Optional.empty();
        }
        long elapsedMillis = Math.max(0L, System.currentTimeMillis() - ServerShopClientState.lastSyncTimeMillis());
        int elapsedSeconds = (int) (elapsedMillis / 1000L);
        return java.util.Optional.of(Math.max(0, viewState.restockSecondsRemaining().get() - elapsedSeconds));
    }

    private int listStartX() {
        return leftPos + LIST_X_OFFSET;
    }

    private int listStartY() {
        return topPos + LIST_Y_OFFSET;
    }

    private int listVisibleHeight() {
        return LIST_HEIGHT;
    }

    private int listInteractiveHeight() {
        return maxVisibleRows * ROW_HEIGHT;
    }

    private int scrollerX() {
        return leftPos + SCROLLER_X_OFFSET;
    }

    private int scrollerY() {
        return topPos + LIST_Y_OFFSET;
    }

    private int scrollerHeight() {
        return Math.max(SCROLLER_HEIGHT, listVisibleHeight() - SCROLLER_BOTTOM_INSET);
    }

    private boolean isWithinOfferList(double mouseX, double mouseY) {
        int listStartX = listStartX();
        int listStartY = listStartY();
        return mouseX >= listStartX
                && mouseX < listStartX + LIST_WIDTH
                && mouseY >= listStartY
                && mouseY < listStartY + listInteractiveHeight();
    }

    private int offerIndexAt(double mouseY) {
        int clickedOffset = (int) ((mouseY - listStartY()) / ROW_HEIGHT);
        return scrollOffset + clickedOffset;
    }

    private boolean trySelectOfferAt(double mouseX, double mouseY) {
        if (!isWithinOfferList(mouseX, mouseY)) {
            return false;
        }

        int index = offerIndexAt(mouseY);
        if (index < 0 || index >= visibleOffers.size()) {
            return false;
        }

        ServerShopOffer clickedOffer = visibleOffers.get(index);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));

        this.selectedOfferId = clickedOffer.id();
        if (!isLocalEditMode) {
            menu.setCurrentTradingOffer(clickedOffer);
            NetworkHandler.sendToServer(new ServerShopSetOfferPacket(clickedOffer.id()));
        }

        updatePreview();
        rebuildUi();
        return true;
    }

    // Input handling
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isScrollBarActive() && isWithinScroller(mouseX, mouseY)) {
            isDragging = true;
            return true;
        }
        if (trySelectOfferAt(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateScrollLimits() {
        int totalRows = visibleOffers.size();
        maxVisibleRows = Math.max(1, listVisibleHeight() / ROW_HEIGHT);
        int maxScroll = Math.max(0, totalRows - maxVisibleRows);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
    }

    private void updateScrollOffset(int newOffset) {
        int totalRows = visibleOffers.size();
        int maxScroll = Math.max(0, totalRows - maxVisibleRows);
        scrollOffset = Mth.clamp(newOffset, 0, maxScroll);
    }

    private void addDynamic(AbstractWidget widget) {
        addRenderableWidget(widget);
        dynamicWidgets.add(widget);
        if (widget instanceof VanillaIconButton) {
            foregroundWidgets.add(widget);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);

        if (isLocalEditMode) {
            guiGraphics.drawString(font, Component.translatable("gui.marketblocks.mode.edit_active"), leftPos + 166, topPos + 6, 0xFF5555, false);
        }
    }

    private void renderScroller(GuiGraphics guiGraphics) {
        int scrollerX = scrollerX();
        int scrollerY = scrollerY();
        int scrollerH = scrollerHeight();
        int maxScroll = Math.max(0, visibleOffers.size() - maxVisibleRows);

        if (maxScroll <= 0) {
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, scrollerX, scrollerY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
            return;
        }

        float progress = (float) scrollOffset / (float) maxScroll;
        int handleTravel = Math.max(0, scrollerH - SCROLLER_HEIGHT);
        int handleY = scrollerY + Mth.floor(progress * handleTravel);
        guiGraphics.blitSprite(SCROLLER_SPRITE, scrollerX, handleY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
    }

    private boolean isScrollBarActive() {
        return visibleOffers.size() > maxVisibleRows;
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
            int scrollerY = scrollerY();
            int handleTravel = scrollerHeight() - SCROLLER_HEIGHT;
            if (handleTravel <= 0) {
                return true;
            }
            double relative = (mouseY - scrollerY - (SCROLLER_HEIGHT / 2.0D)) / handleTravel;
            relative = Mth.clamp(relative, 0.0D, 1.0D);
            int maxScroll = visibleOffers.size() - maxVisibleRows;
            int newOffset = Mth.floor(relative * maxScroll + 0.5D);
            updateScrollOffset(newOffset);
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
        int scrollerX = scrollerX();
        int scrollerY = scrollerY();
        int scrollerHeight = scrollerHeight();
        return mouseX >= scrollerX && mouseX < scrollerX + SCROLLER_WIDTH && mouseY >= scrollerY && mouseY < scrollerY + scrollerHeight;
    }

    // Adapter between screen methods and external editor-controls builder.
    private final class ScreenEditorCallbacks implements ServerShopEditorControls.Callbacks {
        @Override
        public void addWidget(AbstractWidget widget) {
            addDynamic(widget);
        }

        @Override
        public void openTextInput(Component title, String initialValue, boolean allowEmpty, Consumer<String> onConfirm) {
            ServerShopScreen.this.openTextInput(title, initialValue, allowEmpty, onConfirm);
        }

        @Override
        public void openOfferLimitsEditor(ServerShopOffer offer) {
            ServerShopScreen.this.openOfferLimitsEditor(offer);
        }

        @Override
        public void openOfferPricingEditor(ServerShopOffer offer) {
            ServerShopScreen.this.openOfferPricingEditor(offer);
        }

        @Override
        public void clearSelectedOffer() {
            ServerShopScreen.this.clearSelectedOffer();
        }

        @Override
        public void updatePreview() {
            ServerShopScreen.this.updatePreview();
        }

        @Override
        public void rebuildUi() {
            ServerShopScreen.this.rebuildUi();
        }

        @Override
        public ServerShopOffer findOfferOnSelectedPage(UUID offerId) {
            return ServerShopScreen.this.findOfferOnSelectedPage(offerId);
        }

        @Override
        public int rightEditorButtonsX() {
            return ServerShopScreen.this.rightEditorButtonsX();
        }

        @Override
        public int rightEditorButtonsY() {
            return ServerShopScreen.this.rightEditorButtonsY();
        }
    }

    private final class ScreenPageSidebarCallbacks implements ServerShopPageSidebar.Callbacks {
        @Override
        public void addWidget(AbstractWidget widget) {
            addDynamic(widget);
        }

        @Override
        public void onPageSelected(int pageIndex) {
            ServerShopScreen.this.onPageSelected(pageIndex);
        }
    }

    // Shared simple text input modal used by page rename/create actions.
    private static class TextInputScreen extends BaseModalScreen {
        private static final int PANEL_WIDTH = 236;
        private static final int PANEL_HEIGHT = 98;
        private final String initialValue;
        private final boolean allowEmpty;
        private final Consumer<String> onConfirm;
        private EditBox input;
        private Button confirmButton;

        protected TextInputScreen(ServerShopScreen parent, Component title, String initialValue, boolean allowEmpty, Consumer<String> onConfirm) {
            super(title, parent, PANEL_WIDTH, PANEL_HEIGHT, -1, -1);
            this.initialValue = initialValue == null ? "" : initialValue;
            this.allowEmpty = allowEmpty;
            this.onConfirm = onConfirm;
        }

        @Override
        protected void init() {
            initModalBounds();

            int inputX = this.panelLeft + 18;
            int inputY = this.panelTop + 30;
            this.input = new EditBox(this.font, inputX, inputY, 200, 20, Component.empty());
            this.input.setMaxLength(64);
            this.input.setValue(initialValue);
            this.input.setResponder(ignored -> updateConfirmState());
            addRenderableWidget(input);
            this.confirmButton = Button.builder(CommonComponents.GUI_OK, ignored -> confirmInput())
                    .bounds(this.panelLeft + 18, this.panelTop + 56, 96, 20).build();
            addRenderableWidget(confirmButton);
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, ignored -> this.onClose())
                    .bounds(this.panelLeft + PANEL_WIDTH - 114, this.panelTop + 56, 96, 20).build());
            updateConfirmState();
            setInitialFocus(input);
        }

        private void updateConfirmState() {
            if (confirmButton != null) {
                confirmButton.active = allowEmpty || !ServerShopScreen.normalizeTextInput(input.getValue()).isEmpty();
            }
        }

        private void confirmInput() {
            String normalizedValue = ServerShopScreen.normalizeTextInput(input.getValue());
            if (normalizedValue.isEmpty() && !allowEmpty) {
                return;
            }
            onConfirm.accept(normalizedValue);
            this.onClose();
        }

        @Override
        protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // No extra foreground content needed for this modal.
        }

        @Override public boolean keyPressed(int k, int s, int m) {
            if (k == 257 || k == 335) {
                if (confirmButton != null && confirmButton.active) {
                    confirmInput();
                    return true;
                }
                return false;
            }
            return super.keyPressed(k, s, m);
        }
    }
}
