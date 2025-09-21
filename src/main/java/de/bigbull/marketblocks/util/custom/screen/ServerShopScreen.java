package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.*;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.servershop.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerShopScreen extends AbstractContainerScreen<ServerShopMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/server_shop.png");
    private final List<Button> dynamicWidgets = new ArrayList<>();
    private ServerShopData cachedData = ServerShopClientState.data();
    private boolean cachedEditor = menu.isEditor();

    public ServerShopScreen(ServerShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 276;
        this.titleLabelY = 6;
        this.inventoryLabelY = 118;
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
    }

    private void rebuildUi() {
        dynamicWidgets.forEach(this::removeWidget);
        dynamicWidgets.clear();

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
            }).bounds(baseX, y, 100, 20).build();
            button.active = menu.selectedPage() != index;
            addDynamic(button);
            y += 22;
        }

        if (menu.isEditor()) {
            Button addPage = Button.builder(Component.translatable("gui.marketblocks.server_shop.add_page"),
                    b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.add_page"), "",
                            name -> NetworkHandler.sendToServer(new ServerShopCreatePagePacket(name)))).bounds(baseX, y + 6, 100, 20).build();
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
                        name -> NetworkHandler.sendToServer(new ServerShopRenamePagePacket(page.name(), name)))).bounds(baseX, baseY, 100, 20).build());
        addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.delete_page"),
                b -> NetworkHandler.sendToServer(new ServerShopDeletePagePacket(page.name()))).bounds(baseX + 108, baseY, 100, 20).build());
    }

    private void buildCategoryControls() {
        if (cachedData.pages().isEmpty()) {
            return;
        }
        ServerShopPage page = cachedData.pages().get(Math.min(menu.selectedPage(), cachedData.pages().size() - 1));
        List<ServerShopCategory> categories = page.categories();
        int y = topPos + 36;
        int baseX = leftPos + 8;

        if (categories.isEmpty()) {
            if (menu.isEditor()) {
                addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.add_category"),
                                b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.add_category"), "",
                                        name -> NetworkHandler.sendToServer(new ServerShopAddCategoryPacket(page.name(), name))))
                        .bounds(baseX, y, 140, 20).build());
            }
            return;
        }

        if (menu.isEditor()) {
            addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.add_category"),
                            b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.add_category"), "",
                                    name -> NetworkHandler.sendToServer(new ServerShopAddCategoryPacket(page.name(), name))))
                    .bounds(baseX, topPos + 36, 140, 20).build());
            y += 24;
        }

        for (ServerShopCategory category : categories) {
            Button header = Button.builder(Component.literal(category.name()),
                            b -> NetworkHandler.sendToServer(new ServerShopToggleCategoryPacket(page.name(), category.name())))
                    .bounds(baseX, y, 140, 20).build();
            addDynamic(header);
            if (menu.isEditor()) {
                addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.rename_category"),
                                b -> openTextInput(Component.translatable("gui.marketblocks.server_shop.rename_category"), category.name(),
                                        name -> NetworkHandler.sendToServer(new ServerShopRenameCategoryPacket(page.name(), category.name(), name))))
                        .bounds(baseX + 148, y, 110, 20).build());
                addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.delete_category"),
                                b -> NetworkHandler.sendToServer(new ServerShopDeleteCategoryPacket(page.name(), category.name())))
                        .bounds(baseX + 262, y, 110, 20).build());
            }
            y += 24;
            if (!category.collapsed()) {
                y = buildOfferControls(page, categories, category, y);
            }
            y += 6;
        }
    }

    private int buildOfferControls(ServerShopPage page, List<ServerShopCategory> categories, ServerShopCategory category, int startY) {
        int y = startY;
        List<ServerShopOffer> offers = category.offers();
        if (offers.isEmpty()) {
            if (menu.isEditor()) {
                addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.add_offer"),
                                b -> NetworkHandler.sendToServer(new ServerShopAddOfferPacket(page.name(), category.name())))
                        .bounds(leftPos + 20, y, 160, 20).build());
                y += 24;
            }
            return y;
        }
        if (menu.isEditor()) {
            addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.add_offer"),
                            b -> NetworkHandler.sendToServer(new ServerShopAddOfferPacket(page.name(), category.name())))
                    .bounds(leftPos + 20, y, 160, 20).build());
            y += 24;
        }
        List<ServerShopCategory> allCategories = categories;
        for (int index = 0; index < offers.size(); index++) {
            ServerShopOffer offer = offers.get(index);
            final int offerIndex = index;
            Button delete = Button.builder(Component.translatable("gui.marketblocks.server_shop.delete_offer"),
                            b -> NetworkHandler.sendToServer(new ServerShopDeleteOfferPacket(offer.id())))
                    .bounds(leftPos + 20, y, 100, 20).build();
            addDynamic(delete);
            if (menu.isEditor()) {
                Button update = Button.builder(Component.translatable("gui.marketblocks.server_shop.replace_offer"),
                                b -> NetworkHandler.sendToServer(new ServerShopReplaceOfferStacksPacket(offer.id())))
                        .bounds(leftPos + 124, y, 110, 20).build();
                addDynamic(update);
                if (offerIndex > 0) {
                    addDynamic(Button.builder(Component.literal("↑"),
                                    b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(offer.id(), page.name(), category.name(), offerIndex - 1)))
                            .bounds(leftPos + 238, y, 24, 20).build());
                }
                if (offerIndex < offers.size() - 1) {
                    addDynamic(Button.builder(Component.literal("↓"),
                                    b -> NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(offer.id(), page.name(), category.name(), offerIndex + 1)))
                            .bounds(leftPos + 266, y, 24, 20).build());
                }
                if (allCategories.size() > 1) {
                    addDynamic(Button.builder(Component.translatable("gui.marketblocks.server_shop.move_offer"),
                            b -> {
                                int current = allCategories.indexOf(category);
                                int next = (current + 1) % allCategories.size();
                                ServerShopCategory target = allCategories.get(next);
                                NetworkHandler.sendToServer(new ServerShopMoveOfferPacket(offer.id(), page.name(), target.name(), Integer.MAX_VALUE));
                            }).bounds(leftPos + 294, y, 110, 20).build());
                }
            }
            y += 22;
        }
        return y;
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
        guiGraphics.drawString(font, title, leftPos + 8, topPos + 8, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderData(guiGraphics);
    }

    private void renderData(GuiGraphics guiGraphics) {
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