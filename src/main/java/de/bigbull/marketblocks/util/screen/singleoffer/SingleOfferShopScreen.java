package de.bigbull.marketblocks.util.screen.singleoffer;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.singleOfferShop.*;
import de.bigbull.marketblocks.shop.log.TransactionLogEntry;
import de.bigbull.marketblocks.shop.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.shop.singleoffer.block.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.shop.singleoffer.menu.ShopTab;
import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.shop.visual.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.shop.visual.ShopVisualSettings;
import de.bigbull.marketblocks.shop.visual.VillagerVisualProfession;
import de.bigbull.marketblocks.shop.visual.VisualNpcPlacementResult;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.*;

public class SingleOfferShopScreen extends AbstractSingleOfferShopScreen<SingleOfferShopMenu> {
    private static final ResourceLocation OFFERS_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand_offers.png");
    private static final ResourceLocation INVENTORY_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand_inventory.png");
    private static final ResourceLocation SETTINGS_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand_settings.png");
    private static final ResourceLocation OWNER_LIST_PANEL_BG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/trade_stand/owner_list_panel.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/out_of_stock.png");
    private static final ResourceLocation OUTPUT_FULL_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/singleoffer/output_full.png");
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation CLEAR_LOG = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/clear_log.png");
    private static final ResourceLocation INPUT_OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/singleoffer/input_output.png");
    private static final ResourceLocation TRADE_ARROW_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");

    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");

    private static final int LOG_LIST_X_OFFSET = 7;
    private static final int LOG_LIST_Y_OFFSET = 19;
    private static final int LOG_LIST_WIDTH = 154;
    private static final int LOG_LIST_HEIGHT = 124;

    // Pixel-basiertes Layout für aufklappbare Zeilen
    private static final int ROW_HEIGHT_COLLAPSED = 18;
    private static final int ROW_HEIGHT_EXPANDED = 44;

    private static final int LOG_SCROLLER_X_OFFSET = 163;
    private static final int LOG_SCROLLER_WIDTH = 6;
    private static final int LOG_SCROLLER_HEIGHT = 27;
    private static final int LOG_HEAD_SIZE = 8;
    private static final int LOG_HEAD_U = 8;
    private static final int LOG_HEAD_V = 8;
    private static final int LOG_HAT_U = 40;
    private static final int LOG_HAT_V = 8;
    private static final int LOG_SKIN_TEX_SIZE = 64;

    private record IconRect(int x, int y, int width, int height) { }
    private static final IconRect STATUS_ICON_RECT = new IconRect(82, 50, 28, 21);

    private ShopTab lastTab;
    private boolean lastHasOffer;
    private SettingsCategory activeSettingsCategory = SettingsCategory.GENERAL;

    private OfferTemplateButton offerButton;
    private EditBox nameField;
    private EditBox npcNameField;
    private boolean emitRedstoneEnabled;
    private Direction leftDir, rightDir, bottomDir, backDir;
    private boolean saved;
    private String originalName;
    private String draftShopName;
    private Boolean draftEmitRedstone;
    private Boolean draftVisualNpcEnabled;
    private String draftVisualNpcName;
    private VillagerVisualProfession draftVisualNpcProfession;
    private Boolean draftVisualPurchaseParticles;
    private Boolean draftVisualPurchaseSounds;
    private Boolean draftVisualPaymentSlotSounds;
    private Boolean draftPurchaseXpFeedbackSound;
    private VisualNpcPlacementResult visualPlacementResult = VisualNpcPlacementResult.OK;

    // Neue Variablen für Expandable UI & Pixel-Scroll
    private int logScrollPixelOffset = 0;
    private int expandedLogIndex = -1;
    private boolean logDragging;

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
                    () -> switchTab(ShopTab.SETTINGS),
                    () -> switchTab(ShopTab.LOG));
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

    private void rebuildUI() {
        clearWidgets();
        if (menu.isOwner()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, menu.getActiveTab(),
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS),
                    () -> switchTab(ShopTab.LOG));
        }
        buildCurrentTabUI();
    }

    private void buildCurrentTabUI() {
        switch (menu.getActiveTab()) {
            case OFFERS -> buildOffersUI();
            case INVENTORY -> buildInventoryUI();
            case SETTINGS -> buildSettingsUI();
            case LOG -> buildLogUI();
        }
    }

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

    private void buildInventoryUI() { }

    private void buildLogUI() {
        clampLogScroll();
        if (menu.isPrimaryOwner()) {
            addRenderableWidget(new IconButton(
                    leftPos + imageWidth - 171,
                    topPos + 141,
                    20,
                    20,
                    BUTTON_SPRITES,
                    CLEAR_LOG,
                    ignored -> clearTransactionLog(),
                    Component.translatable("gui.marketblocks.log.clear"),
                    () -> false
            ));
        }
    }

    private void buildSettingsUI() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        Direction facing = be.getBlockState().getValue(BaseShopBlock.FACING);
        leftDir = facing.getCounterClockWise();
        rightDir = facing.getClockWise();
        bottomDir = Direction.DOWN;
        backDir = facing.getOpposite();

        if (originalName == null) originalName = be.getShopName();
        if (draftShopName == null) draftShopName = be.getShopName();
        if (draftEmitRedstone == null) draftEmitRedstone = be.isEmitRedstone();

        ShopVisualSettings visualSettings = be.getVisualSettings();
        if (draftVisualNpcEnabled == null) draftVisualNpcEnabled = visualSettings.npcEnabled();
        if (draftVisualNpcName == null) draftVisualNpcName = visualSettings.npcName();
        if (draftVisualNpcProfession == null) draftVisualNpcProfession = visualSettings.profession();
        if (draftVisualPurchaseParticles == null) draftVisualPurchaseParticles = visualSettings.purchaseParticlesEnabled();
        if (draftVisualPurchaseSounds == null) draftVisualPurchaseSounds = visualSettings.purchaseSoundsEnabled();
        if (draftVisualPaymentSlotSounds == null) draftVisualPaymentSlotSounds = visualSettings.paymentSlotSoundsEnabled();
        if (draftPurchaseXpFeedbackSound == null) draftPurchaseXpFeedbackSound = be.isPurchaseXpFeedbackSound();

        emitRedstoneEnabled = draftEmitRedstone;
        visualPlacementResult = resolveVisualPlacementResult(be);

        SingleOfferSettingsSections.buildCategoryButtons(this, activeSettingsCategory, this::switchSettingsCategory);
        buildSaveButton(be);

        switch (activeSettingsCategory) {
            case GENERAL -> nameField = SingleOfferSettingsSections.buildGeneralSection(
                    this, draftShopName, emitRedstoneEnabled, Boolean.TRUE.equals(draftPurchaseXpFeedbackSound),
                    value -> { emitRedstoneEnabled = value; draftEmitRedstone = value; saved = false; },
                    s -> { draftShopName = s; saved = false; },
                    value -> { draftPurchaseXpFeedbackSound = value; saved = false; }
            );
            case IO -> SingleOfferSettingsSections.buildIoSection(
                    this, menu, leftDir, rightDir, bottomDir, backDir, () -> saved = false
            );
            case VISUALS -> {
                SingleOfferSettingsSections.VisualSectionWidgets widgets = SingleOfferSettingsSections.buildVisualSection(
                        this, Boolean.TRUE.equals(draftVisualNpcEnabled), draftVisualNpcName, draftVisualNpcProfession,
                        Boolean.TRUE.equals(draftVisualPurchaseParticles), Boolean.TRUE.equals(draftVisualPurchaseSounds),
                        Boolean.TRUE.equals(draftVisualPaymentSlotSounds), visualPlacementResult,
                        () -> { draftVisualNpcEnabled = !Boolean.TRUE.equals(draftVisualNpcEnabled); saved = false; },
                        value -> { draftVisualNpcName = value; saved = false; },
                        () -> { draftVisualNpcProfession = (draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession).next(); saved = false; },
                        value -> { draftVisualPurchaseParticles = value; saved = false; },
                        value -> { draftVisualPurchaseSounds = value; saved = false; },
                        value -> { draftVisualPaymentSlotSounds = value; saved = false; },
                        this::getNpcToggleLabel, this::getProfessionLabel
                );
                npcNameField = widgets.npcNameField();
            }
            case ACCESS -> buildSettingsAccessSection(be);
        }
    }

    private void buildSaveButton(SingleOfferShopBlockEntity be) {
        addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            if (nameField != null) draftShopName = nameField.getValue();
            if (npcNameField != null) draftVisualNpcName = npcNameField.getValue();

            String name = draftShopName != null ? draftShopName : "";
            boolean emit = emitRedstoneEnabled;
            ShopVisualSettings visuals = new ShopVisualSettings(
                    Boolean.TRUE.equals(draftVisualNpcEnabled), draftVisualNpcName,
                    draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession,
                    Boolean.TRUE.equals(draftVisualPurchaseParticles), Boolean.TRUE.equals(draftVisualPurchaseSounds),
                    Boolean.TRUE.equals(draftVisualPaymentSlotSounds)
            );

            be.setShopNameClient(name);
            be.setEmitRedstoneClient(emit);
            be.setPurchaseXpFeedbackSoundClient(Boolean.TRUE.equals(draftPurchaseXpFeedbackSound));
            be.setVisualSettingsClient(visuals);
            be.setModeClient(leftDir, menu.getMode(leftDir));
            be.setModeClient(rightDir, menu.getMode(rightDir));
            be.setModeClient(bottomDir, menu.getMode(bottomDir));
            be.setModeClient(backDir, menu.getMode(backDir));

            List<UUID> selectedOwners = Collections.emptyList();
            if (menu.isPrimaryOwner()) {
                selectedOwners = new ArrayList<>();
                be.getAdditionalOwners().clear();
                Map<UUID, String> stored = menu.getAdditionalOwners();

                for (UUID id : ownerListPanel.collectSelectedOwners()) {
                    selectedOwners.add(id);
                    String n = ownerListPanel.resolveName(id, stored);
                    be.addOwnerClient(id, n);
                }
            }

            NetworkHandler.sendToServer(new UpdateSettingsPacket(
                    be.getBlockPos(), menu.getMode(leftDir), menu.getMode(rightDir), menu.getMode(bottomDir), menu.getMode(backDir),
                    name, emit, visuals.npcEnabled(), visuals.npcName(), visuals.profession().serializedName(),
                    visuals.purchaseParticlesEnabled(), visuals.purchaseSoundsEnabled(), visuals.paymentSlotSoundsEnabled(),
                    Boolean.TRUE.equals(draftPurchaseXpFeedbackSound)
            ));
            if (menu.isPrimaryOwner()) {
                NetworkHandler.sendToServer(new UpdateOwnersPacket(be.getBlockPos(), selectedOwners));
            }

            originalName = name; draftShopName = name; draftEmitRedstone = emit;
            draftVisualNpcEnabled = visuals.npcEnabled(); draftVisualNpcName = visuals.npcName();
            draftVisualNpcProfession = visuals.profession(); draftVisualPurchaseParticles = visuals.purchaseParticlesEnabled();
            draftVisualPurchaseSounds = visuals.purchaseSoundsEnabled(); draftVisualPaymentSlotSounds = visuals.paymentSlotSoundsEnabled();
            draftPurchaseXpFeedbackSound = Boolean.TRUE.equals(draftPurchaseXpFeedbackSound);
            saved = true;
        }).bounds(leftPos + imageWidth - 50, topPos + imageHeight - 24, 44, 18).build());
    }

    private void buildSettingsAccessSection(SingleOfferShopBlockEntity be) {
        ownerListPanel.prepareAndRender(this, menu, be, topPos + 20, menu.isPrimaryOwner(), () -> saved = false);
    }

    private void switchSettingsCategory(SettingsCategory category) {
        if (activeSettingsCategory == category) return;
        if (nameField != null) draftShopName = nameField.getValue();
        if (npcNameField != null) draftVisualNpcName = npcNameField.getValue();
        draftEmitRedstone = emitRedstoneEnabled;
        activeSettingsCategory = category;
        rebuildUI();
    }

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
        } else if (menu.getActiveTab() == ShopTab.LOG) {
            renderLogHoverTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField != null && nameField.isFocused()) {
            if (nameField.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (nameField != null && nameField.isFocused() && nameField.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersBg(graphics);
            case INVENTORY -> renderInventoryBg(graphics);
            case SETTINGS -> renderSettingsBg(graphics);
            case LOG -> renderLogBg(graphics);
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
        } else if (outputBlocked || (be.hasOffer() && be.isOfferAvailable() && be.isOutputAlmostFull())) {
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

    private void renderLogBg(GuiGraphics graphics) {
        graphics.blit(SETTINGS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
        int listX = leftPos + LOG_LIST_X_OFFSET;
        int listY = topPos + LOG_LIST_Y_OFFSET;

        clampLogScroll();

        if (entries.isEmpty()) {
            Component empty = Component.translatable("gui.marketblocks.log.empty");
            int textX = listX + (LOG_LIST_WIDTH - font.width(empty)) / 2;
            int textY = listY + (LOG_LIST_HEIGHT - font.lineHeight) / 2;
            graphics.drawString(font, empty, textX, textY, 0x777777, false);
        } else {
            graphics.enableScissor(listX, listY, listX + LOG_LIST_WIDTH, listY + LOG_LIST_HEIGHT);

            int currentY = listY - logScrollPixelOffset;
            for (int i = 0; i < entries.size(); i++) {
                boolean isExpanded = (i == expandedLogIndex);
                int rowHeight = isExpanded ? ROW_HEIGHT_EXPANDED : ROW_HEIGHT_COLLAPSED;

                // Nur rendern, was sichtbar ist
                if (currentY + rowHeight > listY && currentY < listY + LOG_LIST_HEIGHT) {
                    renderLogRow(graphics, entries.get(i), listX, currentY, i, isExpanded);
                }
                currentY += rowHeight;
            }
            graphics.disableScissor();
        }

        renderLogScroller(graphics);
    }

    private void renderLogRow(GuiGraphics graphics, TransactionLogEntry entry, int x, int y, int index, boolean isExpanded) {
        int rowHeight = isExpanded ? ROW_HEIGHT_EXPANDED : ROW_HEIGHT_COLLAPSED;
        int rowColor = (index % 2 == 0) ? 0x1AFFFFFF : 0x12000000;

        // Background & Hover Effect
        boolean isHovered = isHoveringLog(x, y, rowHeight);
        if (isHovered) rowColor = 0x2AFFFFFF;

        graphics.fill(x, y, x + LOG_LIST_WIDTH, y + rowHeight - 1, rowColor);

        // Player Head
        int headX = x + 4;
        int headY = y + 4;
        ResourceLocation skinTexture = resolveLogSkinTexture(entry);
        graphics.blit(skinTexture, headX, headY, 0, LOG_HEAD_U, LOG_HEAD_V, LOG_HEAD_SIZE, LOG_HEAD_SIZE, LOG_SKIN_TEX_SIZE, LOG_SKIN_TEX_SIZE);
        graphics.blit(skinTexture, headX, headY, 0, LOG_HAT_U, LOG_HAT_V, LOG_HEAD_SIZE, LOG_HEAD_SIZE, LOG_SKIN_TEX_SIZE, LOG_SKIN_TEX_SIZE);

        // Expand/Collapse Indicator [+] / [-]
        String expandChar = isExpanded ? "v" : ">";
        int expandX = x + LOG_LIST_WIDTH - 12;
        graphics.drawString(font, expandChar, expandX, headY, 0x888888, false);

        // Time
        Component timeText = formatRelativeTime(entry.epochSecond());
        int timeX = expandX - 6 - font.width(timeText);
        graphics.drawString(font, timeText, timeX, headY, 0x6F6F6F, false);

        // Player Name
        int detailsX = headX + LOG_HEAD_SIZE + 6;
        String buyerName = entry.buyerName().isBlank() ? "Unknown" : entry.buyerName();
        int maxNameWidth = timeX - detailsX - 4;
        if (maxNameWidth > 0 && font.width(buyerName) > maxNameWidth) {
            buyerName = font.plainSubstrByWidth(buyerName, maxNameWidth - font.width("...")) + "...";
        }
        graphics.drawString(font, buyerName, detailsX, headY, 0x303030, false);

        // --- EXPANDED VIEW: ITEM RENDERING ---
        if (isExpanded) {
            int itemY = y + 22;
            int currentX = detailsX;

            // Bezahlte Items
            if (entry.paidStacks().isEmpty()) {
                graphics.drawString(font, Component.translatable("gui.marketblocks.log.none"), currentX, itemY + 4, 0x777777, false);
                currentX += font.width(Component.translatable("gui.marketblocks.log.none")) + 4;
            } else {
                for (ItemStack stack : entry.paidStacks()) {
                    graphics.renderItem(stack, currentX, itemY);
                    graphics.renderItemDecorations(font, stack, currentX, itemY);
                    currentX += 18;
                }
            }

            // Tausch Pfeil
            graphics.blit(TRADE_ARROW_ICON, currentX, itemY + 2, 0, 0, 16, 16, 16, 16);
            currentX += 18;

            // Erhaltene Items
            for (ItemStack stack : entry.boughtStacks()) {
                graphics.renderItem(stack, currentX, itemY);
                graphics.renderItemDecorations(font, stack, currentX, itemY);
                currentX += 18;
            }
        }
    }

    private boolean isHoveringLog(int x, int y, int height) {
        double mx = minecraft.mouseHandler.xpos() * (double) this.width / (double) this.minecraft.getWindow().getScreenWidth();
        double my = minecraft.mouseHandler.ypos() * (double) this.height / (double) this.minecraft.getWindow().getScreenHeight();
        return mx >= x && mx < x + LOG_LIST_WIDTH && my >= y && my < y + height;
    }

    private void renderLogScroller(GuiGraphics graphics) {
        int maxScroll = getMaxLogScroll();
        int scrollerX = leftPos + LOG_SCROLLER_X_OFFSET;
        int scrollerY = topPos + LOG_LIST_Y_OFFSET;

        if (maxScroll <= 0) {
            graphics.blitSprite(SCROLLER_DISABLED_SPRITE, scrollerX, scrollerY, LOG_SCROLLER_WIDTH, LOG_SCROLLER_HEIGHT);
            return;
        }

        int travel = Math.max(0, LOG_LIST_HEIGHT - LOG_SCROLLER_HEIGHT);
        float progress = (float) logScrollPixelOffset / maxScroll;
        int handleY = scrollerY + Mth.floor(progress * travel);
        graphics.blitSprite(SCROLLER_SPRITE, scrollerX, handleY, LOG_SCROLLER_WIDTH, LOG_SCROLLER_HEIGHT);
    }

    private void clearTransactionLog() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new ClearTransactionLogPacket(be.getBlockPos()));
        menu.setTransactionLogEntries(List.of());
        logScrollPixelOffset = 0;
        expandedLogIndex = -1;
        playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    private Component formatRelativeTime(long epochSecond) {
        long deltaSeconds = Math.max(0L, Instant.now().getEpochSecond() - Math.max(0L, epochSecond));
        if (deltaSeconds < 5L) return Component.translatable("gui.marketblocks.log.time.just_now");
        if (deltaSeconds < 60L) return Component.translatable("gui.marketblocks.log.time.seconds", deltaSeconds);
        if (deltaSeconds < 3600L) return Component.translatable("gui.marketblocks.log.time.minutes", deltaSeconds / 60L);
        if (deltaSeconds < 86400L) return Component.translatable("gui.marketblocks.log.time.hours", deltaSeconds / 3600L);
        return Component.translatable("gui.marketblocks.log.time.days", deltaSeconds / 86400L);
    }

    /**
     * Nativer Skin Loading Weg ohne Reflection oder asynchrone Threads!
     */
    private ResourceLocation resolveLogSkinTexture(TransactionLogEntry entry) {
        UUID buyerId = entry.buyerUuid();
        String name = entry.buyerName();
        if (buyerId == null || (buyerId.getLeastSignificantBits() == 0L && buyerId.getMostSignificantBits() == 0L)) {
            buyerId = net.minecraft.Util.NIL_UUID;
        }

        Minecraft client = Minecraft.getInstance();
        GameProfile profile = new GameProfile(buyerId, name);
        // SkinManager löst Caching und Asynchrone Downloads nativ über Minecraft.
        return client.getSkinManager().getInsecureSkin(profile).texture();
    }

    private int getTotalLogHeight() {
        int count = menu.getTransactionLogEntries().size();
        if (count == 0) return 0;
        int height = count * ROW_HEIGHT_COLLAPSED;
        if (expandedLogIndex >= 0 && expandedLogIndex < count) {
            height += (ROW_HEIGHT_EXPANDED - ROW_HEIGHT_COLLAPSED);
        }
        return height;
    }

    private int getMaxLogScroll() {
        return Math.max(0, getTotalLogHeight() - LOG_LIST_HEIGHT);
    }

    private void clampLogScroll() {
        logScrollPixelOffset = Mth.clamp(logScrollPixelOffset, 0, getMaxLogScroll());
    }

    /**
     * Zeigt jetzt statt langweiligem Text die ECHTEN Tooltips für die gezeichneten ItemStacks an.
     */
    private void renderLogHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (expandedLogIndex == -1) return; // Nichts aufgeklappt

        List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
        if (expandedLogIndex >= entries.size()) return;

        int listX = leftPos + LOG_LIST_X_OFFSET;
        int listY = topPos + LOG_LIST_Y_OFFSET;

        int currentY = listY - logScrollPixelOffset;
        for (int i = 0; i < expandedLogIndex; i++) {
            currentY += ROW_HEIGHT_COLLAPSED;
        }

        int itemY = currentY + 22;
        int detailsX = listX + 4 + LOG_HEAD_SIZE + 6;

        // Prüfen, ob die Maus im Item-Rendering Bereich der aufgeklappten Zeile ist
        if (mouseY >= itemY && mouseY < itemY + 16 && currentY > listY - ROW_HEIGHT_EXPANDED && currentY < listY + LOG_LIST_HEIGHT) {
            int currentX = detailsX;
            TransactionLogEntry entry = entries.get(expandedLogIndex);

            for (ItemStack stack : entry.paidStacks()) {
                if (mouseX >= currentX && mouseX < currentX + 16) {
                    graphics.renderTooltip(font, stack, mouseX, mouseY);
                    return;
                }
                currentX += 18;
            }

            currentX += 18; // Pfeil überspringen

            for (ItemStack stack : entry.boughtStacks()) {
                if (mouseX >= currentX && mouseX < currentX + 16) {
                    graphics.renderTooltip(font, stack, mouseX, mouseY);
                    return;
                }
                currentX += 18;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersLabels(graphics);
            case INVENTORY -> renderInventoryLabels(graphics);
            case SETTINGS -> renderSettingsLabels(graphics);
            case LOG -> renderLogLabels(graphics);
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
            graphics.drawString(font, Component.translatable("gui.marketblocks.shop_name"), 10, 20, 4210752, false);
        }
        if (activeSettingsCategory == SettingsCategory.VISUALS) {
            graphics.drawString(font, Component.translatable("gui.marketblocks.visuals.npc_name"), 48, 18, 4210752, false);
            if (!visualPlacementResult.canSpawn()) {
                graphics.drawString(font, Component.translatable(visualPlacementResult.translationKey()), 8, 84, 0xCC3333, false);
            }
        }
        if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.settings_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        } else if (activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner() && ownerListPanel.noPlayers()) {
            Component info = Component.translatable("gui.marketblocks.no_players_available");
            graphics.drawString(font, info, 10, ownerListPanel.listBaseY() - topPos + 16, 0x808080, false);
        }
    }

    private void renderLogLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.log_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);

        Component count = Component.translatable("gui.marketblocks.log.count", menu.getTransactionLogEntries().size());
        int countX = imageWidth - 8 - font.width(count);
        graphics.drawString(font, count, countX, imageHeight - 14, 0x6F6F6F, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getActiveTab() == ShopTab.LOG && button == 0) {
            int scrollerX = leftPos + LOG_SCROLLER_X_OFFSET;
            int listY = topPos + LOG_LIST_Y_OFFSET;
            if (mouseX >= scrollerX && mouseX < scrollerX + LOG_SCROLLER_WIDTH && mouseY >= listY && mouseY < listY + LOG_LIST_HEIGHT) {
                logDragging = true;
                return true;
            }

            // Aufklapp-Funktion per Klick auf die Reihe
            int listX = leftPos + LOG_LIST_X_OFFSET;
            if (mouseX >= listX && mouseX < listX + LOG_LIST_WIDTH) {
                int currentY = listY - logScrollPixelOffset;
                List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
                for (int i = 0; i < entries.size(); i++) {
                    int rowHeight = (i == expandedLogIndex) ? ROW_HEIGHT_EXPANDED : ROW_HEIGHT_COLLAPSED;
                    if (mouseY >= currentY && mouseY < currentY + rowHeight) {
                        expandedLogIndex = (expandedLogIndex == i) ? -1 : i; // Toggle
                        playSound(SoundEvents.UI_BUTTON_CLICK);
                        return true;
                    }
                    currentY += rowHeight;
                }
            }
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseClicked(mouseX, mouseY, leftPos)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.getActiveTab() == ShopTab.LOG && logDragging) {
            int maxScroll = getMaxLogScroll();
            int travel = Math.max(1, LOG_LIST_HEIGHT - LOG_SCROLLER_HEIGHT);
            double relative = (mouseY - (topPos + LOG_LIST_Y_OFFSET) - (LOG_SCROLLER_HEIGHT / 2.0D)) / travel;
            relative = Mth.clamp(relative, 0.0D, 1.0D);
            logScrollPixelOffset = Mth.clamp((int) Math.floor(relative * maxScroll + 0.5D), 0, maxScroll);
            return true;
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseDragged(mouseY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) logDragging = false;
        ownerListPanel.onMouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.getActiveTab() == ShopTab.LOG) {
            if (getMaxLogScroll() > 0) {
                logScrollPixelOffset -= (int) (scrollY * 16); // Scroll-Geschwindigkeit (16 Pixel pro Raster)
                clampLogScroll();
                return true;
            }
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseScrolled(scrollY)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

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
            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
        } catch (Exception e) {
            MarketBlocks.LOGGER.error("Error creating offer", e);
            playSound(SoundEvents.ITEM_BREAK);
        }
    }

    private void deleteOffer() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new DeleteOfferPacket(be.getBlockPos()));
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
            for (int i = 0; i < 3; i++) menu.slots.get(i).set(ItemStack.EMPTY);
            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    private Pair<ItemStack, ItemStack> normalizePayments(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty()) return Pair.of(p2, ItemStack.EMPTY);
        return Pair.of(p1, p2);
    }

    @Override
    protected boolean isOwner() {
        return menu.isOwner();
    }

    @Override
    public void onClose() {
        if (!saved && menu.getActiveTab() == ShopTab.SETTINGS) menu.resetModes();
        String shopNameDraft = nameField != null ? nameField.getValue() : (draftShopName != null ? draftShopName : "");
        if (menu.getActiveTab() == ShopTab.SETTINGS && shopNameDraft.trim().isEmpty()) {
            menu.getBlockEntity().setShopNameClient(originalName);
        }
        super.onClose();
    }

    private Component getNpcToggleLabel() {
        return Component.literal(Boolean.TRUE.equals(draftVisualNpcEnabled) ? "ON" : "OFF");
    }

    private Component getProfessionLabel() {
        VillagerVisualProfession profession = draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession;
        return Component.translatable("gui.marketblocks.visuals.profession").append(": ")
                .append(Component.translatable(profession.translationKey()));
    }

    private VisualNpcPlacementResult resolveVisualPlacementResult(SingleOfferShopBlockEntity be) {
        if (be.getLevel() == null) return VisualNpcPlacementResult.OK;
        return ShopVisualPlacementValidator.validate(be.getLevel(), be.getBlockPos(), be.getBlockState().getValue(BaseShopBlock.FACING)).result();
    }

    <T extends net.minecraft.client.gui.components.AbstractWidget> T addSettingsWidget(T widget) {
        return addRenderableWidget(widget);
    }

    void removeSettingsWidget(net.minecraft.client.gui.components.AbstractWidget widget) {
        removeWidget(widget);
    }

    int settingsLeftPos() { return leftPos; }
    int settingsTopPos() { return topPos; }
    net.minecraft.client.gui.Font settingsFont() { return font; }
}