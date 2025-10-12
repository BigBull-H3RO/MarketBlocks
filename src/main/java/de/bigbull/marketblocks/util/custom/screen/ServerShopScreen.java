package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.*;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import de.bigbull.marketblocks.util.custom.servershop.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
import java.util.function.Consumer;

public class ServerShopScreen extends AbstractContainerScreen<ServerShopMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/server_shop.png");
    private static final int DEFAULT_MAX_VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 24;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLLER_HANDLE_HEIGHT = 12;
    private final List<Button> dynamicWidgets = new ArrayList<>();
    private final List<RowEntry> rowEntries = new ArrayList<>();
    private int scrollOffset;
    private int maxVisibleRows = DEFAULT_MAX_VISIBLE_ROWS;
    private boolean isDragging;
    private ServerShopData cachedData = ServerShopClientState.data();
    private boolean cachedEditor = menu.isEditor();
    private OfferTemplateButton offerPreviewButton;

    public ServerShopScreen(ServerShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 276;
        this.imageHeight = 207;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = 108;
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
        if (current != cachedData || cachedEditor != menu.isEditor()) {
            cachedData = current;
            cachedEditor = menu.isEditor();
            rebuildUi();
        }

        if (this.offerPreviewButton != null && this.menu.isEditor()) {
            ItemStack p1 = this.menu.getTemplateStack(0);
            ItemStack p2 = this.menu.getTemplateStack(1);
            ItemStack result = this.menu.getTemplateStack(2);
            // Pfeil ist aktiv, wenn ein Ergebnis-Item vorhanden ist
            this.offerPreviewButton.update(p1, p2, result, !result.isEmpty());
        }
    }

    private void rebuildUi() {
        dynamicWidgets.forEach(this::removeWidget);
        dynamicWidgets.clear();
        rowEntries.clear();
        this.offerPreviewButton = null;

        buildPageSidebar();
        buildEditorControls();
        buildCategoryControls();
    }

    private void buildPageSidebar() {
        List<ServerShopPage> pages = cachedData.pages();
        int baseX = leftPos - 110;
        int y = topPos + 20;
        for (int i = 0; i < pages.size(); i++) {
            int index = i;
            ServerShopPage page = pages.get(i);
            Component label = Component.literal(page.name().isEmpty() ? Component.translatable("gui.marketblocks.server_shop.unnamed_page", i + 1).getString() : page.name());
            Button button = Button.builder(label, b -> {
                if (menu.selectedPage() != index) {
                    menu.setSelectedPageClient(index);
                    NetworkHandler.sendToServer(new ServerShopSelectPagePacket(index));
                }
            }).bounds(baseX, y, 20, 20).build();
            button.active = menu.selectedPage() != index;
            addDynamic(button);
            y += 22;
        }

        if (menu.isEditor()) {
            Button addPage = Button.builder(Component.translatable("gui.marketblocks.server_shop.add_page"),
                    b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.add_page"), "",
                            name -> NetworkHandler.sendToServer(new ServerShopCreatePagePacket(name)))).bounds(baseX, y + 6, 20, 20).build();
            addDynamic(addPage);
        }
    }

    private void buildEditorControls() {
        if (!menu.isEditor()) {
            return;
        }
        if (cachedData.pages().isEmpty()) {
            return;
        }
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
        int baseX = leftPos + 8;
        int baseY = topPos + 6;
        addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.rename_page"),
                b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.rename_page"), page.name(),
                        name -> NetworkHandler.sendToServer(new ServerShopRenamePagePacket(page.name(), name)))).bounds(baseX, baseY, 20, 20).build());
        addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.delete_page"),
                b -> NetworkHandler.sendToServer(new ServerShopDeletePagePacket(page.name()))).bounds(baseX + 108, baseY, 20, 20).build());

        this.offerPreviewButton = new OfferTemplateButton(this.leftPos + 144, this.topPos + 43, b -> {});
        this.offerPreviewButton.active = false;
        this.addRenderableWidget(this.offerPreviewButton);
        this.dynamicWidgets.add(this.offerPreviewButton);
    }

    private void buildCategoryControls() {
        if (cachedData.pages().isEmpty()) {
            scrollOffset = 0;
            maxVisibleRows = DEFAULT_MAX_VISIBLE_ROWS;
            return;
        }
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
        List<ServerShopCategory> categories = page.categories();
        rowEntries.clear();

        if (menu.isEditor()) {
            rowEntries.add(RowEntry.addCategory(page));
        }

        for (ServerShopCategory category : categories) {
            rowEntries.add(RowEntry.category(page, category));
            if (!category.collapsed()) {
                if (menu.isEditor()) {
                    rowEntries.add(RowEntry.addOffer(page, category));
                }
                for (int index = 0; index < category.offers().size(); index++) {
                    rowEntries.add(RowEntry.offer(page, category, category.offers().get(index), index));
                }
            }
        }

        updateScrollLimits();
        renderVisibleRows(categories);
    }

    private void renderVisibleRows(List<ServerShopCategory> categories) {
        int listTop = topPos + 36;
        int end = Math.min(scrollOffset + maxVisibleRows, rowEntries.size());
        int y = listTop;
        for (int index = 0; index < end; index++) {
            RowEntry entry = rowEntries.get(index);
            if (index < scrollOffset) {
                continue;
            }
            switch (entry.type()) {
                case ADD_CATEGORY -> renderAddCategory(entry, y);
                case CATEGORY_HEADER -> renderCategoryRow(entry, y);
                case ADD_OFFER -> renderAddOffer(entry, y);
                case OFFER -> renderOfferRow(entry, y, categories);
            }
            y += ROW_HEIGHT;
        }

    }

    private void renderAddCategory(RowEntry entry, int y) {
        addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.add_category"),
                        b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.add_category"), "",
                                name -> NetworkHandler.sendToServer(new ServerShopAddCategoryPacket(entry.page().name(), name))))
                .bounds(leftPos + 8, y, 140, 20).build());
    }

    private void renderCategoryRow(RowEntry entry, int y) {
        ServerShopCategory category = entry.category();
        ServerShopPage page = entry.page();
        Button header = Button.builder(Component.literal(category.name()),
                        b -> NetworkHandler.sendToServer(new ServerShopToggleCategoryPacket(page.name(), category.name())))
                .bounds(leftPos + 8, y, 140, 20).build();
        addDynamic(header);

        if (menu.isEditor()) {
            addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.rename_category"),
                            b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.rename_category"), category.name(),
                                    name -> NetworkHandler.sendToServer(new ServerShopRenameCategoryPacket(page.name(), category.name(), name))))
                    .bounds(leftPos + 156, y, 110, 20).build());
            addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.delete_category"),
                            b -> NetworkHandler.sendToServer(new ServerShopDeleteCategoryPacket(page.name(), category.name())))
                    .bounds(leftPos + 270, y, 110, 20).build());
        }
    }

    private void renderAddOffer(RowEntry entry, int y) {
        ServerShopPage page = entry.page();
        ServerShopCategory category = entry.category();
        addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.add_offer"),
                        b -> NetworkHandler.sendToServer(new ServerShopAddOfferPacket(page.name(), category.name())))
                .bounds(leftPos + 20, y, 160, 20).build());
    }

    private void renderOfferRow(RowEntry entry, int y, List<ServerShopCategory> categories) {
        ServerShopPage page = entry.page();
        ServerShopCategory category = entry.category();
        ServerShopOffer offer = entry.offer();
        int offerIndex = entry.offerIndex();
        addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.delete_offer"),
                        b -> NetworkHandler.sendToServer(new ServerShopDeleteOfferPacket(offer.id())))
                .bounds(leftPos + 20, y, 100, 20).build());
        if (menu.isEditor()) {
            addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.replace_offer"),
                            b -> NetworkHandler.sendToServer(new ServerShopReplaceOfferStacksPacket(offer.id())))
                    .bounds(leftPos + 124, y, 110, 20).build());
            if (offerIndex > 0) {
                addDynamic(Button.builder(Component.literal("↑"),
                                b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(offer.id(), page.name(), category.name(), offerIndex - 1)))
                        .bounds(leftPos + 238, y, 24, 20).build());
            }
            if (offerIndex < category.offers().size() - 1) {
                addDynamic(Button.builder(Component.literal("↓"),
                                b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(offer.id(), page.name(), category.name(), offerIndex + 1)))
                        .bounds(leftPos + 266, y, 24, 20).build());
            }
            if (categories.size() > 1) {
                addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.move_offer"),
                        b -> {
                            int current = categories.indexOf(category);
                            int next = (current + 1) % categories.size();
                            ServerShopCategory target = categories.get(next);
                            NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(offer.id(), page.name(), target.name(), Integer.MAX_VALUE));
                        }).bounds(leftPos + 294, y, 110, 20).build());
            }
        }
    }

    private void updateScrollLimits() {
        int totalRows = rowEntries.size();
        maxVisibleRows = Math.min(DEFAULT_MAX_VISIBLE_ROWS, Math.max(totalRows, 1));
        int maxScroll = Math.max(0, totalRows - maxVisibleRows);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
    }

    private void updateScrollOffset(int newOffset) {
        int totalRows = rowEntries.size();
        int maxScroll = Math.max(0, totalRows - maxVisibleRows);
        int clamped = Mth.clamp(newOffset, 0, maxScroll);
        if (clamped != scrollOffset) {
            scrollOffset = clamped;
            rebuildUi();
        }
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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderScroller(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderScroller(GuiGraphics guiGraphics) {
        if (!isScrollBarActive()) {
            return;
        }
        int scrollerX = leftPos + imageWidth - SCROLLER_WIDTH - 4;
        int scrollerY = topPos + 36;
        int scrollerHeight = maxVisibleRows * ROW_HEIGHT;
        guiGraphics.fill(scrollerX, scrollerY, scrollerX + SCROLLER_WIDTH, scrollerY + scrollerHeight, 0x33000000);
        int maxScroll = rowEntries.size() - maxVisibleRows;
        if (maxScroll <= 0) {
            return;
        }
        float progress = (float) scrollOffset / (float) maxScroll;
        int handleTravel = scrollerHeight - SCROLLER_HANDLE_HEIGHT;
        int handleY = scrollerY + Mth.floor(progress * handleTravel);
        guiGraphics.fill(scrollerX, handleY, scrollerX + SCROLLER_WIDTH, handleY + SCROLLER_HANDLE_HEIGHT, isDragging ? 0xFFAAAAAA : 0xFF888888);
    }

    private boolean isScrollBarActive() {
        return rowEntries.size() > maxVisibleRows;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isScrollBarActive()) {
            int direction = (int) Math.signum(scrollY);
            if (direction != 0) {
                updateScrollOffset(scrollOffset - direction);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isScrollBarActive()) {
            if (isWithinScroller(mouseX, mouseY)) {
                isDragging = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && isScrollBarActive()) {
            int scrollerY = topPos + 36;
            int scrollerHeight = maxVisibleRows * ROW_HEIGHT;
            int handleTravel = scrollerHeight - SCROLLER_HANDLE_HEIGHT;
            if (handleTravel > 0) {
                double relative = (mouseY - scrollerY - (SCROLLER_HANDLE_HEIGHT / 2.0D)) / handleTravel;
                relative = Mth.clamp(relative, 0.0D, 1.0D);
                int maxScroll = rowEntries.size() - maxVisibleRows;
                int newOffset = Mth.floor(relative * maxScroll + 0.5D);
                updateScrollOffset(newOffset);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isWithinScroller(double mouseX, double mouseY) {
        int scrollerX = leftPos + imageWidth - SCROLLER_WIDTH - 4;
        int scrollerY = topPos + 36;
        int scrollerHeight = maxVisibleRows * ROW_HEIGHT;
        return mouseX >= scrollerX && mouseX < scrollerX + SCROLLER_WIDTH && mouseY >= scrollerY && mouseY < scrollerY + scrollerHeight;
    }

    private void renderData(GuiGraphics guiGraphics) {
        // Diese Methode wird nicht mehr benötigt, da die UI jetzt alles anzeigt.
        // Der Code wird zur Sicherheit hier belassen, falls er wieder benötigt wird.
        int x = leftPos + 10;
        int y = topPos + 74;
        if (cachedData.pages().isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.marketblocks.server_shop.no_pages"), x, y, 0xAAAAAA, false);
            return;
        }
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
        if (page.categories().isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.marketblocks.server_shop.no_categories"), x, y, 0xAAAAAA, false);
            return;
        }
        for (ServerShopCategory category : page.categories()) {
            guiGraphics.drawString(font, Component.literal(category.name()), x, y, 0xFFFFFF, false);
            y += 10;
            if (category.collapsed()) {
                guiGraphics.drawString(font, Component.translatable("gui.marketblocks.server_shop.collapsed"), x + 6, y, 0x888888, false);
                y += 12;
                continue;
            }
            if (category.offers().isEmpty()) {
                guiGraphics.drawString(font, Component.translatable("gui.marketblocks.server_shop.no_offers"), x + 6, y, 0x888888, false);
                y += 12;
                continue;
            }
            for (ServerShopOffer offer : category.offers()) {
                ItemStack result = offer.result();
                guiGraphics.drawString(font, result.getHoverName(), x + 6, y, 0xFFFFFF, false);
                y += 10;
            }
            y += 4;
        }
    }

    private record RowEntry(RowType type, ServerShopPage page, ServerShopCategory category, ServerShopOffer offer, int offerIndex) {
        static RowEntry addCategory(ServerShopPage page) {
            return new RowEntry(RowType.ADD_CATEGORY, page, null, null, -1);
        }

        static RowEntry category(ServerShopPage page, ServerShopCategory category) {
            return new RowEntry(RowType.CATEGORY_HEADER, page, category, null, -1);
        }

        static RowEntry addOffer(ServerShopPage page, ServerShopCategory category) {
            return new RowEntry(RowType.ADD_OFFER, page, category, null, -1);
        }

        static RowEntry offer(ServerShopPage page, ServerShopCategory category, ServerShopOffer offer, int offerIndex) {
            return new RowEntry(RowType.OFFER, page, category, offer, offerIndex);
        }
    }

    private enum RowType {
        ADD_CATEGORY,
        CATEGORY_HEADER,
        ADD_OFFER,
        OFFER
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
                    b -> {
                        minecraft.setScreen(parent);
                        onConfirm.accept(input.getValue());
                    }).bounds(this.width / 2 - 100, this.height / 2 + 16, 96, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,
                    b -> minecraft.setScreen(parent)).bounds(this.width / 2 + 4, this.height / 2 + 16, 96, 20).build());
            setInitialFocus(input);
        }

        @Override
        public void onClose() {
            minecraft.setScreen(parent);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.drawString(font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.height / 2 - 40, 0xFFFFFF, false);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == 257 || keyCode == 335) {
                minecraft.setScreen(parent);
                onConfirm.accept(input.getValue());
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}