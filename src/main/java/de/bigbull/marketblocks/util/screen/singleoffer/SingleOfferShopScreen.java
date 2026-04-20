package de.bigbull.marketblocks.util.screen.singleoffer;

import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.singleOfferShop.*;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.singleoffer.menu.ShopTab;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.util.screen.gui.GuiConstants;
import de.bigbull.marketblocks.util.screen.gui.IconButton;
import de.bigbull.marketblocks.util.screen.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unified screen for the Trade Stand. Rebuilds its UI dynamically when the tab
 * changes instead of opening new screens.
 */
public class SingleOfferShopScreen extends AbstractSingleOfferShopScreen<SingleOfferShopMenu> {
    private static final ResourceLocation OFFERS_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand_offers.png");
    private static final ResourceLocation INVENTORY_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand_inventory.png");
    private static final ResourceLocation SETTINGS_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand_settings.png");
    private static final ResourceLocation OWNER_LIST_PANEL_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand/owner_list_panel.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/out_of_stock.png");
    private static final ResourceLocation OUTPUT_FULL_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/output_full.png");
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation INPUT_OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/input_output.png");

    // Stonecutter-Scroller-Sprites wiederverwenden
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private record IconRect(int x, int y, int width, int height) { }
    private static final IconRect STATUS_ICON_RECT = new IconRect(82, 50, 28, 21);

    private ShopTab lastTab;
    private boolean lastHasOffer;
    private SettingsCategory activeSettingsCategory = SettingsCategory.GENERAL;

    // Offers widgets
    private OfferTemplateButton offerButton;

    // Settings widgets
    private EditBox nameField;
    private boolean emitRedstoneEnabled;
    private Direction leftDir, rightDir, bottomDir, backDir;
    private boolean saved;
    private String originalName;
    private String draftShopName;
    private Boolean draftEmitRedstone;

    private final SingleOfferOwnerListPanel ownerListPanel = new SingleOfferOwnerListPanel();

    public SingleOfferShopScreen(SingleOfferShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        lastTab = menu.getActiveTab();
        lastHasOffer = menu.getBlockEntity().hasOffer();
        if (menu.isOwner()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, lastTab,
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS));
        }
        buildCurrentTabUI();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        ShopTab current = menu.getActiveTab();
        boolean offerChanged = menu.getBlockEntity().hasOffer() != lastHasOffer;

        if (current != lastTab) {
            lastTab = current;
            lastHasOffer = menu.getBlockEntity().hasOffer();
            rebuildUI();
            return;
        }

        if (offerChanged) {
            lastHasOffer = menu.getBlockEntity().hasOffer();
            if (current == ShopTab.OFFERS) {
                rebuildUI();
            }
        }
    }

    @Override
    protected void switchTab(ShopTab tab) {
        if (menu.isOwner()) {
            menu.setActiveTabClient(tab);
            lastTab = tab;
            rebuildUI();
        }
        super.switchTab(tab);
    }

    /**
     * Rebuilds the UI when tab changes.
     *
     * Optimization: We clear ALL widgets and recreate everything. This is simple but has drawbacks:
     * - Focus is lost if player is typing in an EditBox
     * - Tab buttons are recreated unnecessarily (they don't change between tabs)
     *
     * Future improvement: Could track tab-specific widgets separately and only clear/rebuild
     * those, while keeping tab buttons persistent. However, current implementation is acceptable
     * because:
     * 1. Tab switches are infrequent
     * 2. Only owners have multiple tabs (regular players don't rebuild)
     * 3. EditBox state is restored by reading from BlockEntity
     */
    private void rebuildUI() {
        clearWidgets();
        if (menu.isOwner()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, menu.getActiveTab(),
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS));
        }
        buildCurrentTabUI();
    }

    private void buildCurrentTabUI() {
        switch (menu.getActiveTab()) {
            case OFFERS -> buildOffersUI();
            case INVENTORY -> buildInventoryUI();
            case SETTINGS -> buildSettingsUI();
        }
    }

    // --- Build tab UIs ---
    private void buildOffersUI() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        boolean isOwner = menu.isOwner();
        offerButton = addRenderableWidget(new OfferTemplateButton(leftPos + 44, topPos + 17, b -> onOfferClicked()));
        offerButton.active = be.hasOffer();
        if (isOwner) {
            if (!be.hasOffer()) {
                addRenderableWidget(new IconButton(leftPos + 143, topPos + 17, 20, 20, BUTTON_SPRITES, CREATE_ICON,
                        b -> createOffer(), Component.translatable("gui.marketblocks.create_offer"), () -> false));
            } else {
                addRenderableWidget(new IconButton(leftPos + 143, topPos + 17, 20, 20, BUTTON_SPRITES, DELETE_ICON,
                        b -> deleteOffer(), Component.translatable("gui.marketblocks.delete_offer"), () -> false));
            }
        }
    }

    private void buildInventoryUI() {
        // no special widgets besides tab buttons
    }

    private void buildSettingsUI() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        Direction facing = be.getBlockState().getValue(BaseShopBlock.FACING);
        leftDir = facing.getCounterClockWise();
        rightDir = facing.getClockWise();
        bottomDir = Direction.DOWN;
        backDir = facing.getOpposite();

        if (originalName == null) {
            originalName = be.getShopName();
        }
        if (draftShopName == null) {
            draftShopName = be.getShopName();
        }
        if (draftEmitRedstone == null) {
            draftEmitRedstone = be.isEmitRedstone();
        }
        emitRedstoneEnabled = draftEmitRedstone;

        SingleOfferSettingsSections.buildCategoryButtons(this, activeSettingsCategory, this::switchSettingsCategory);
        buildSaveButton(be);

        switch (activeSettingsCategory) {
            case GENERAL -> nameField = SingleOfferSettingsSections.buildGeneralSection(
                    this,
                    draftShopName,
                    this::getEmitRedstoneToggleLabel,
                    () -> {
                        emitRedstoneEnabled = !emitRedstoneEnabled;
                        draftEmitRedstone = emitRedstoneEnabled;
                        saved = false;
                    },
                    s -> {
                        draftShopName = s;
                        saved = false;
                    }
            );
            case IO -> SingleOfferSettingsSections.buildIoSection(
                    this,
                    menu,
                    leftDir,
                    rightDir,
                    bottomDir,
                    backDir,
                    () -> saved = false
            );
            case ACCESS -> buildSettingsAccessSection(be);
        }
    }

    private void buildSaveButton(SingleOfferShopBlockEntity be) {
        addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            if (nameField != null) {
                draftShopName = nameField.getValue();
            }
            String name = draftShopName != null ? draftShopName : "";
            boolean emit = emitRedstoneEnabled;

            // Clientseitig UI-State anwenden
            be.setShopNameClient(name);
            be.setEmitRedstoneClient(emit);
            be.setModeClient(leftDir, menu.getMode(leftDir));
            be.setModeClient(rightDir, menu.getMode(rightDir));
            be.setModeClient(bottomDir, menu.getMode(bottomDir));
            be.setModeClient(backDir, menu.getMode(backDir));

            List<UUID> selectedOwners = Collections.emptyList();
            if (menu.isPrimaryOwner()) {
                // Owners: gesamte (persistente) Auswahl auswerten (nur Haupt-Owner)
                selectedOwners = new ArrayList<>();
                be.getAdditionalOwners().clear();
                Map<UUID, String> stored = menu.getAdditionalOwners();

                for (UUID id : ownerListPanel.collectSelectedOwners()) {
                    selectedOwners.add(id);
                    String n = ownerListPanel.resolveName(id, stored);
                    be.addOwnerClient(id, n);
                }
            }

            // Pakete senden
            NetworkHandler.sendToServer(new UpdateSettingsPacket(
                    be.getBlockPos(),
                    menu.getMode(leftDir),
                    menu.getMode(rightDir),
                    menu.getMode(bottomDir),
                    menu.getMode(backDir),
                    name,
                    emit
            ));
            if (menu.isPrimaryOwner()) {
                NetworkHandler.sendToServer(new UpdateOwnersPacket(be.getBlockPos(), selectedOwners));
            }

            originalName = name;
            draftShopName = name;
            draftEmitRedstone = emit;
            saved = true;
        }).bounds(leftPos + imageWidth - 52, topPos + imageHeight - 22, 44, 18).build());
    }

    private void buildSettingsAccessSection(SingleOfferShopBlockEntity be) {
        ownerListPanel.prepareAndRender(this, menu, be, topPos + 100, menu.isPrimaryOwner(), () -> saved = false);
    }

    private void switchSettingsCategory(SettingsCategory category) {
        if (activeSettingsCategory == category) {
            return;
        }
        if (nameField != null) {
            draftShopName = nameField.getValue();
        }
        draftEmitRedstone = emitRedstoneEnabled;
        activeSettingsCategory = category;
        rebuildUI();
    }


    // --- Rendering ---
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (menu.getActiveTab() == ShopTab.OFFERS) {
            SingleOfferShopBlockEntity be = menu.getBlockEntity();
            if (be.hasOffer() && isHovering(STATUS_ICON_RECT.x(), STATUS_ICON_RECT.y(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), mouseX, mouseY)) {
                if (!be.hasResultItemInInput(false)) {
                    graphics.renderTooltip(font, Component.translatable("gui.marketblocks.out_of_stock"), mouseX, mouseY);
                } else if (be.isOutputSpaceMissing()) {
                    graphics.renderTooltip(font, Component.translatable("gui.marketblocks.output_full"), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.isFocused()) {
            if (nameField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }

            if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (nameField != null && nameField.isFocused() && nameField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersBg(graphics);
            case INVENTORY -> renderInventoryBg(graphics);
            case SETTINGS -> renderSettingsBg(graphics);
        }
    }

    private void renderOffersBg(GuiGraphics graphics) {
        graphics.blit(OFFERS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        offerButton.active = be.hasOffer();
        if (be.hasOffer()) {
            offerButton.update(be.getOfferPayment1(), be.getOfferPayment2(), be.getOfferResult(), be.hasResultItemInInput(false));
        } else {
            ItemStack p1 = menu.slots.get(0).getItem();
            ItemStack p2 = menu.slots.get(1).getItem();
            Pair<ItemStack, ItemStack> norm = normalizePayments(p1, p2);
            offerButton.update(norm.getFirst(), norm.getSecond(), menu.slots.get(2).getItem(), true);
        }
        int iconX = leftPos + STATUS_ICON_RECT.x();
        int iconY = topPos + STATUS_ICON_RECT.y();

        boolean outOfStock = be.hasOffer() && !be.hasResultItemInInput(false);
        boolean outputBlocked = be.hasOffer() && be.isOutputSpaceMissing();

        if (outOfStock) {
            graphics.blit(OUT_OF_STOCK_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        } else if (outputBlocked) {
            graphics.blit(OUTPUT_FULL_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        } else if (be.hasOffer() && be.isOfferAvailable() && be.isOutputAlmostFull()) {
            graphics.blit(OUTPUT_FULL_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(), STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        }
    }

    private void renderInventoryBg(GuiGraphics graphics) {
        graphics.blit(INVENTORY_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(INPUT_OUTPUT_ICON, leftPos + 77, topPos + 33, 0, 0, 22, 22, 22, 22);
    }

    private void renderSettingsBg(GuiGraphics graphics) {
        graphics.blit(SETTINGS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.isPrimaryOwner() && activeSettingsCategory == SettingsCategory.ACCESS) {
            ownerListPanel.renderBackground(graphics, leftPos, OWNER_LIST_PANEL_BG, SCROLLER_SPRITE, SCROLLER_DISABLED_SPRITE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersLabels(graphics);
            case INVENTORY -> renderInventoryLabels(graphics);
            case SETTINGS -> renderSettingsLabels(graphics);
        }
    }

    private void renderOffersLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        Component title;
        String name = be.getShopName();
        if (name != null && !name.isEmpty()) {
            title = Component.literal(name);
        } else {
            title = Component.translatable("gui.marketblocks.shop_title");
        }
        graphics.drawString(font, title, 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void renderInventoryLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.inventory_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.inventory_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void renderSettingsLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.settings_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        if (activeSettingsCategory == SettingsCategory.GENERAL) {
            graphics.drawString(font, Component.translatable("gui.marketblocks.emit_redstone"), 38, 49, 4210752, false);
        }
        if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.settings_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        } else if (activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner() && ownerListPanel.noPlayers()) {
            Component info = Component.translatable("gui.marketblocks.no_players_available");
            graphics.drawString(font, info, 8, ownerListPanel.listBaseY() - topPos + 6, 0x808080, false);
        }
    }

    // --- Maus-Events fÃ¼r den Owner-Scroller ---
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseClicked(mouseX, mouseY, leftPos)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseDragged(mouseY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ownerListPanel.onMouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseScrolled(scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // --- Helpers for offers ---
    private void createOffer() {
        try {
            ItemStack p1 = menu.slots.get(0).getItem().copy();
            ItemStack p2 = menu.slots.get(1).getItem().copy();
            ItemStack result = menu.slots.get(2).getItem().copy();
            Pair<ItemStack, ItemStack> norm = normalizePayments(p1, p2);
            p1 = norm.getFirst();
            p2 = norm.getSecond();
            if (result.isEmpty()) {
                minecraft.gui.getChat().addMessage(Component.translatable("gui.marketblocks.error.no_result_item").withStyle(ChatFormatting.RED));
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }
            if (p1.isEmpty() && p2.isEmpty()) {
                minecraft.gui.getChat().addMessage(Component.translatable("gui.marketblocks.error.no_payment_items").withStyle(ChatFormatting.RED));
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }
            SingleOfferShopBlockEntity be = menu.getBlockEntity();
            NetworkHandler.sendToServer(new CreateOfferPacket(be.getBlockPos(), p1, p2, result));

            // NOTE: We do NOT set hasOffer client-side here immediately.
            // The server will send an OfferStatusPacket if creation succeeds,
            // which will trigger setHasOfferClient() and rebuildUI() via packet handler.
            // This ensures client state only updates after server confirmation.

            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            // rebuildUI() will be called when OfferStatusPacket arrives
        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error creating offer", e);
            playSound(SoundEvents.ITEM_BREAK);
        }
    }

    private void deleteOffer() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new DeleteOfferPacket(be.getBlockPos()));

        // NOTE: We set hasOffer=false immediately on delete because it's safe
        // (worst case: UI shows no offer when there is one, which auto-corrects on next sync)
        be.setHasOfferClient(false);
        playSound(SoundEvents.UI_BUTTON_CLICK);
        rebuildUI();
    }

    private void onOfferClicked() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        if (be.hasOffer()) {
            NetworkHandler.sendToServer(new AutoFillPaymentPacket(be.getBlockPos()));
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return;
        }
        if (menu.isOwner()) {
            for (int i = 0; i < 3; i++) {
                menu.slots.get(i).set(ItemStack.EMPTY);
            }
            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    private Pair<ItemStack, ItemStack> normalizePayments(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty()) {
            return Pair.of(p2, ItemStack.EMPTY);
        }
        return Pair.of(p1, p2);
    }

    @Override
    protected boolean isOwner() {
        return menu.isOwner();
    }

    @Override
    public void onClose() {
        if (!saved && menu.getActiveTab() == ShopTab.SETTINGS) {
            menu.resetModes();
        }
        String shopNameDraft = nameField != null ? nameField.getValue() : (draftShopName != null ? draftShopName : "");
        if (menu.getActiveTab() == ShopTab.SETTINGS && shopNameDraft.trim().isEmpty()) {
            menu.getBlockEntity().setShopNameClient(originalName);
        }
        super.onClose();
    }

    private Component getEmitRedstoneToggleLabel() {
        return Component.literal(emitRedstoneEnabled ? "ON" : "OFF");
    }

    <T extends net.minecraft.client.gui.components.AbstractWidget> T addSettingsWidget(T widget) {
        return addRenderableWidget(widget);
    }

    void removeSettingsWidget(net.minecraft.client.gui.components.AbstractWidget widget) {
        removeWidget(widget);
    }

    int settingsLeftPos() {
        return leftPos;
    }

    int settingsTopPos() {
        return topPos;
    }

    net.minecraft.client.gui.Font settingsFont() {
        return font;
    }
}


