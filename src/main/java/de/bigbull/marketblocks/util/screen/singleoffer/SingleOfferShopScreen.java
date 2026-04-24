package de.bigbull.marketblocks.util.screen.singleoffer;

import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.shop.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.singleOfferShop.*;
import de.bigbull.marketblocks.shop.log.TransactionLogEntry;
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
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final ResourceLocation OUTPUT_FULL_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/singleoffer/output_full.png");
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/delete.png");
    private static final ResourceLocation INPUT_OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/singleoffer/input_output.png");
    private static final ResourceLocation DEFAULT_SKIN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    // Stonecutter-Scroller-Sprites wiederverwenden
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/stonecutter/scroller_disabled");

    private static final int LOG_LIST_X_OFFSET = 7;
    private static final int LOG_LIST_Y_OFFSET = 19;
    private static final int LOG_LIST_WIDTH = 154;
    private static final int LOG_ROW_HEIGHT = 22;
    private static final int LOG_VISIBLE_ROWS = 6;
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

    // Offers widgets
    private OfferTemplateButton offerButton;

    // Settings widgets
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
    private int logScrollOffset;
    private boolean logDragging;
    private final Map<UUID, ResourceLocation> logSkinCache = new ConcurrentHashMap<>();
    private final Set<UUID> pendingLogSkinRequests = ConcurrentHashMap.newKeySet();

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

    private void buildLogUI() {
        logScrollOffset = Mth.clamp(logScrollOffset, 0, Math.max(0, menu.getTransactionLogEntries().size() - LOG_VISIBLE_ROWS));
        if (menu.isPrimaryOwner()) {
            addRenderableWidget(new IconButton(
                    leftPos + imageWidth - 28,
                    topPos + 4,
                    20,
                    20,
                    BUTTON_SPRITES,
                    DELETE_ICON,
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

        if (originalName == null) {
            originalName = be.getShopName();
        }
        if (draftShopName == null) {
            draftShopName = be.getShopName();
        }
        if (draftEmitRedstone == null) {
            draftEmitRedstone = be.isEmitRedstone();
        }
        ShopVisualSettings visualSettings = be.getVisualSettings();
        if (draftVisualNpcEnabled == null) {
            draftVisualNpcEnabled = visualSettings.npcEnabled();
        }
        if (draftVisualNpcName == null) {
            draftVisualNpcName = visualSettings.npcName();
        }
        if (draftVisualNpcProfession == null) {
            draftVisualNpcProfession = visualSettings.profession();
        }
        if (draftVisualPurchaseParticles == null) {
            draftVisualPurchaseParticles = visualSettings.purchaseParticlesEnabled();
        }
        if (draftVisualPurchaseSounds == null) {
            draftVisualPurchaseSounds = visualSettings.purchaseSoundsEnabled();
        }
        if (draftVisualPaymentSlotSounds == null) {
            draftVisualPaymentSlotSounds = visualSettings.paymentSlotSoundsEnabled();
        }
        if (draftPurchaseXpFeedbackSound == null) {
            draftPurchaseXpFeedbackSound = be.isPurchaseXpFeedbackSound();
        }
        emitRedstoneEnabled = draftEmitRedstone;
        visualPlacementResult = resolveVisualPlacementResult(be);

        SingleOfferSettingsSections.buildCategoryButtons(this, activeSettingsCategory, this::switchSettingsCategory);
        buildSaveButton(be);

        switch (activeSettingsCategory) {
            case GENERAL -> nameField = SingleOfferSettingsSections.buildGeneralSection(
                    this,
                    draftShopName,
                    emitRedstoneEnabled,
                    Boolean.TRUE.equals(draftPurchaseXpFeedbackSound),
                    value -> {
                        emitRedstoneEnabled = value;
                        draftEmitRedstone = value;
                        saved = false;
                    },
                    s -> {
                        draftShopName = s;
                        saved = false;
                    },
                    value -> {
                        draftPurchaseXpFeedbackSound = value;
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
            case VISUALS -> {
                SingleOfferSettingsSections.VisualSectionWidgets widgets = SingleOfferSettingsSections.buildVisualSection(
                        this,
                        Boolean.TRUE.equals(draftVisualNpcEnabled),
                        draftVisualNpcName,
                        draftVisualNpcProfession,
                        Boolean.TRUE.equals(draftVisualPurchaseParticles),
                        Boolean.TRUE.equals(draftVisualPurchaseSounds),
                        Boolean.TRUE.equals(draftVisualPaymentSlotSounds),
                        visualPlacementResult,
                        () -> {
                            draftVisualNpcEnabled = !Boolean.TRUE.equals(draftVisualNpcEnabled);
                            saved = false;
                        },
                        value -> {
                            draftVisualNpcName = value;
                            saved = false;
                        },
                        () -> {
                            draftVisualNpcProfession = (draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession).next();
                            saved = false;
                        },
                        value -> {
                            draftVisualPurchaseParticles = value;
                            saved = false;
                        },
                        value -> {
                            draftVisualPurchaseSounds = value;
                            saved = false;
                        },
                        value -> {
                            draftVisualPaymentSlotSounds = value;
                            saved = false;
                        },
                        this::getNpcToggleLabel,
                        this::getProfessionLabel
                );
                npcNameField = widgets.npcNameField();
            }
            case ACCESS -> buildSettingsAccessSection(be);
        }
    }

    private void buildSaveButton(SingleOfferShopBlockEntity be) {
        addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            if (nameField != null) {
                draftShopName = nameField.getValue();
            }
            if (npcNameField != null) {
                draftVisualNpcName = npcNameField.getValue();
            }
            String name = draftShopName != null ? draftShopName : "";
            boolean emit = emitRedstoneEnabled;
            ShopVisualSettings visuals = new ShopVisualSettings(
                    Boolean.TRUE.equals(draftVisualNpcEnabled),
                    draftVisualNpcName,
                    draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession,
                    Boolean.TRUE.equals(draftVisualPurchaseParticles),
                    Boolean.TRUE.equals(draftVisualPurchaseSounds),
                    Boolean.TRUE.equals(draftVisualPaymentSlotSounds)
            );

            // Clientseitig UI-State anwenden
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
                    emit,
                    visuals.npcEnabled(),
                    visuals.npcName(),
                    visuals.profession().serializedName(),
                    visuals.purchaseParticlesEnabled(),
                    visuals.purchaseSoundsEnabled(),
                    visuals.paymentSlotSoundsEnabled(),
                    Boolean.TRUE.equals(draftPurchaseXpFeedbackSound)
            ));
            if (menu.isPrimaryOwner()) {
                NetworkHandler.sendToServer(new UpdateOwnersPacket(be.getBlockPos(), selectedOwners));
            }

            originalName = name;
            draftShopName = name;
            draftEmitRedstone = emit;
            draftVisualNpcEnabled = visuals.npcEnabled();
            draftVisualNpcName = visuals.npcName();
            draftVisualNpcProfession = visuals.profession();
            draftVisualPurchaseParticles = visuals.purchaseParticlesEnabled();
            draftVisualPurchaseSounds = visuals.purchaseSoundsEnabled();
            draftVisualPaymentSlotSounds = visuals.paymentSlotSoundsEnabled();
            draftPurchaseXpFeedbackSound = Boolean.TRUE.equals(draftPurchaseXpFeedbackSound);
            saved = true;
        }).bounds(leftPos + imageWidth - 50, topPos + imageHeight - 24, 44, 18).build());
    }

    private void buildSettingsAccessSection(SingleOfferShopBlockEntity be) {
        ownerListPanel.prepareAndRender(this, menu, be, topPos + 20, menu.isPrimaryOwner(), () -> saved = false);
    }

    private void switchSettingsCategory(SettingsCategory category) {
        if (activeSettingsCategory == category) {
            return;
        }
        if (nameField != null) {
            draftShopName = nameField.getValue();
        }
        if (npcNameField != null) {
            draftVisualNpcName = npcNameField.getValue();
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
        } else if (menu.getActiveTab() == ShopTab.LOG) {
            renderLogHoverTooltip(graphics, mouseX, mouseY);
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

    private void renderLogBg(GuiGraphics graphics) {
        graphics.blit(SETTINGS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
        int listX = logListStartX();
        int listY = logListStartY();
        int listHeight = logListHeight();

        clampLogScroll(entries.size());

        if (entries.isEmpty()) {
            Component empty = Component.translatable("gui.marketblocks.log.empty");
            int textX = listX + (LOG_LIST_WIDTH - font.width(empty)) / 2;
            int textY = listY + (listHeight - font.lineHeight) / 2;
            graphics.drawString(font, empty, textX, textY, 0x777777, false);
        } else {
            graphics.enableScissor(listX, listY, listX + LOG_LIST_WIDTH, listY + listHeight);
            int end = Math.min(logScrollOffset + LOG_VISIBLE_ROWS, entries.size());
            int y = listY;
            for (int i = logScrollOffset; i < end; i++) {
                renderLogRow(graphics, entries.get(i), listX, y, i % 2 == 0);
                y += LOG_ROW_HEIGHT;
            }
            graphics.disableScissor();
        }

        renderLogScroller(graphics, entries.size());

    }

    private void renderLogRow(GuiGraphics graphics, TransactionLogEntry entry, int x, int y, boolean evenRow) {
        int rowColor = evenRow ? 0x1AFFFFFF : 0x12000000;
        graphics.fill(x, y, x + LOG_LIST_WIDTH, y + LOG_ROW_HEIGHT - 1, rowColor);

        int headX = x + 4;
        int headY = y + 7;
        ResourceLocation skinTexture = resolveLogSkinTexture(entry);
        graphics.blit(skinTexture, headX, headY, 0, LOG_HEAD_U, LOG_HEAD_V, LOG_HEAD_SIZE, LOG_HEAD_SIZE, LOG_SKIN_TEX_SIZE, LOG_SKIN_TEX_SIZE);
        graphics.blit(skinTexture, headX, headY, 0, LOG_HAT_U, LOG_HAT_V, LOG_HEAD_SIZE, LOG_HEAD_SIZE, LOG_SKIN_TEX_SIZE, LOG_SKIN_TEX_SIZE);

        Component timeText = formatRelativeTime(entry.epochSecond());
        int timeX = x + LOG_LIST_WIDTH - 4 - font.width(timeText);
        int textY = y + 7;
        graphics.drawString(font, timeText, timeX, textY, 0x6F6F6F, false);

        int detailsX = headX + LOG_HEAD_SIZE + 6;
        int maxDetailsWidth = Math.max(0, timeX - detailsX - 4);
        String summary = ellipsizeToWidth(buildLogTradeSummary(entry), maxDetailsWidth);
        graphics.drawString(font, summary, detailsX, textY, 0x303030, false);
    }

    private void renderLogScroller(GuiGraphics graphics, int totalEntries) {
        int scrollerX = logScrollerX();
        int scrollerY = logListStartY();
        if (!isLogScrollActive(totalEntries)) {
            graphics.blitSprite(SCROLLER_DISABLED_SPRITE, scrollerX, scrollerY, LOG_SCROLLER_WIDTH, LOG_SCROLLER_HEIGHT);
            return;
        }

        int maxScroll = Math.max(0, totalEntries - LOG_VISIBLE_ROWS);
        int travel = Math.max(0, logListHeight() - LOG_SCROLLER_HEIGHT);
        float progress = maxScroll == 0 ? 0.0F : (float) logScrollOffset / (float) maxScroll;
        int handleY = scrollerY + Mth.floor(progress * travel);
        graphics.blitSprite(SCROLLER_SPRITE, scrollerX, handleY, LOG_SCROLLER_WIDTH, LOG_SCROLLER_HEIGHT);
    }

    private void clearTransactionLog() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new ClearTransactionLogPacket(be.getBlockPos()));
        menu.setTransactionLogEntries(List.of());
        logScrollOffset = 0;
        playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    private String buildLogTradeSummary(TransactionLogEntry entry) {
        return "[" + formatStackList(entry.paidStacks()) + "] -> [" + formatStackList(entry.boughtStacks()) + "]";
    }

    private String formatStackList(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return Component.translatable("gui.marketblocks.log.none").getString();
        }

        List<String> parts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            parts.add(stack.getCount() + "x " + stack.getHoverName().getString());
        }
        if (parts.isEmpty()) {
            return Component.translatable("gui.marketblocks.log.none").getString();
        }
        return String.join(" + ", parts);
    }

    private Component formatRelativeTime(long epochSecond) {
        long deltaSeconds = Math.max(0L, Instant.now().getEpochSecond() - Math.max(0L, epochSecond));
        if (deltaSeconds < 5L) {
            return Component.translatable("gui.marketblocks.log.time.just_now");
        }
        if (deltaSeconds < 60L) {
            return Component.translatable("gui.marketblocks.log.time.seconds", deltaSeconds);
        }
        if (deltaSeconds < 3600L) {
            return Component.translatable("gui.marketblocks.log.time.minutes", deltaSeconds / 60L);
        }
        if (deltaSeconds < 86400L) {
            return Component.translatable("gui.marketblocks.log.time.hours", deltaSeconds / 3600L);
        }
        return Component.translatable("gui.marketblocks.log.time.days", deltaSeconds / 86400L);
    }

    private String ellipsizeToWidth(String value, int maxWidth) {
        if (value == null || value.isEmpty() || maxWidth <= 0) {
            return "";
        }
        if (font.width(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        if (maxWidth <= ellipsisWidth) {
            return ellipsis;
        }
        return font.plainSubstrByWidth(value, maxWidth - ellipsisWidth) + ellipsis;
    }

    private ResourceLocation resolveLogSkinTexture(TransactionLogEntry entry) {
        UUID buyerId = entry.buyerUuid();
        if (buyerId == null || buyerId.getLeastSignificantBits() == 0L && buyerId.getMostSignificantBits() == 0L) {
            return DEFAULT_SKIN_TEXTURE;
        }

        ResourceLocation cached = logSkinCache.get(buyerId);
        if (cached != null) {
            return cached;
        }

        if (pendingLogSkinRequests.add(buyerId)) {
            CompletableFuture.supplyAsync(() -> resolveSkinFromPlayerInfo(buyerId))
                    .thenAccept(resolved -> Minecraft.getInstance().execute(() -> {
                        logSkinCache.put(buyerId, resolved == null ? DEFAULT_SKIN_TEXTURE : resolved);
                        pendingLogSkinRequests.remove(buyerId);
                    }));
        }

        return DEFAULT_SKIN_TEXTURE;
    }

    private ResourceLocation resolveSkinFromPlayerInfo(UUID buyerId) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) {
            return DEFAULT_SKIN_TEXTURE;
        }

        PlayerInfo info = client.getConnection().getPlayerInfo(buyerId);
        if (info == null) {
            return DEFAULT_SKIN_TEXTURE;
        }

        try {
            Object skinData = info.getSkin();
            if (skinData == null) {
                return DEFAULT_SKIN_TEXTURE;
            }

            Object texture = skinData.getClass().getMethod("texture").invoke(skinData);
            if (texture instanceof ResourceLocation location) {
                return location;
            }
            if (texture != null) {
                try {
                    Object nestedTexture = texture.getClass().getMethod("texturePath").invoke(texture);
                    if (nestedTexture instanceof ResourceLocation location) {
                        return location;
                    }
                } catch (ReflectiveOperationException ignored) {
                    // Fallback below.
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Fallback below.
        }

        return DEFAULT_SKIN_TEXTURE;
    }

    private void clampLogScroll(int totalEntries) {
        int maxScroll = Math.max(0, totalEntries - LOG_VISIBLE_ROWS);
        logScrollOffset = Mth.clamp(logScrollOffset, 0, maxScroll);
    }

    private boolean isLogScrollActive(int totalEntries) {
        return totalEntries > LOG_VISIBLE_ROWS;
    }

    private int logListStartX() {
        return leftPos + LOG_LIST_X_OFFSET;
    }

    private int logListStartY() {
        return topPos + LOG_LIST_Y_OFFSET;
    }

    private int logListHeight() {
        return LOG_VISIBLE_ROWS * LOG_ROW_HEIGHT;
    }

    private int logScrollerX() {
        return leftPos + LOG_SCROLLER_X_OFFSET;
    }

    private boolean isMouseOverLogScroller(double mouseX, double mouseY) {
        int x = logScrollerX();
        int y = logListStartY();
        int h = logListHeight();
        return mouseX >= x && mouseX < x + LOG_SCROLLER_WIDTH && mouseY >= y && mouseY < y + h;
    }

    private int hoveredLogEntryIndex(int mouseX, int mouseY, int totalEntries) {
        if (mouseX < logListStartX() || mouseX >= logListStartX() + LOG_LIST_WIDTH) {
            return -1;
        }
        if (mouseY < logListStartY() || mouseY >= logListStartY() + logListHeight()) {
            return -1;
        }
        int row = (mouseY - logListStartY()) / LOG_ROW_HEIGHT;
        int index = logScrollOffset + row;
        return index >= 0 && index < totalEntries ? index : -1;
    }

    private void renderLogHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
        int hoveredIndex = hoveredLogEntryIndex(mouseX, mouseY, entries.size());
        if (hoveredIndex < 0 || hoveredIndex >= entries.size()) {
            return;
        }

        TransactionLogEntry entry = entries.get(hoveredIndex);
        List<Component> tooltip = new ArrayList<>(3);
        String buyer = entry.buyerName().isBlank() ? entry.buyerUuid().toString() : entry.buyerName();
        tooltip.add(Component.translatable("gui.marketblocks.log.buyer", buyer));
        tooltip.add(Component.literal(buildLogTradeSummary(entry)));
        tooltip.add(formatRelativeTime(entry.epochSecond()));
        List<FormattedCharSequence> lines = tooltip.stream().map(Component::getVisualOrderText).toList();
        graphics.renderTooltip(font, lines, mouseX, mouseY);
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
        graphics.drawString(font, count, countX, imageHeight - 12, 0x6F6F6F, false);
    }

    // --- Maus-Events fÃ¼r den Owner-Scroller ---
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getActiveTab() == ShopTab.LOG && button == 0) {
            List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
            if (isLogScrollActive(entries.size()) && isMouseOverLogScroller(mouseX, mouseY)) {
                logDragging = true;
                return true;
            }
        }

        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseClicked(mouseX, mouseY, leftPos)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.getActiveTab() == ShopTab.LOG && logDragging) {
            List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
            int maxScroll = Math.max(0, entries.size() - LOG_VISIBLE_ROWS);
            int travel = Math.max(1, logListHeight() - LOG_SCROLLER_HEIGHT);
            double relative = (mouseY - logListStartY() - (LOG_SCROLLER_HEIGHT / 2.0D)) / travel;
            relative = Mth.clamp(relative, 0.0D, 1.0D);
            logScrollOffset = Mth.clamp((int) Math.floor(relative * maxScroll + 0.5D), 0, maxScroll);
            return true;
        }

        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseDragged(mouseY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            logDragging = false;
        }
        ownerListPanel.onMouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.getActiveTab() == ShopTab.LOG) {
            List<TransactionLogEntry> entries = menu.getTransactionLogEntries();
            if (isLogScrollActive(entries.size())) {
                int direction = (int) Math.signum(scrollY);
                if (direction != 0) {
                    int maxScroll = Math.max(0, entries.size() - LOG_VISIBLE_ROWS);
                    logScrollOffset = Mth.clamp(logScrollOffset - direction, 0, maxScroll);
                    return true;
                }
            }
        }

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


    private Component getNpcToggleLabel() {
        return Component.literal(Boolean.TRUE.equals(draftVisualNpcEnabled) ? "ON" : "OFF");
    }

    private Component getProfessionLabel() {
        VillagerVisualProfession profession = draftVisualNpcProfession == null ? VillagerVisualProfession.NONE : draftVisualNpcProfession;
        return Component.translatable("gui.marketblocks.visuals.profession").append(": ")
                .append(Component.translatable(profession.translationKey()));
    }


    private VisualNpcPlacementResult resolveVisualPlacementResult(SingleOfferShopBlockEntity be) {
        if (be.getLevel() == null) {
            return VisualNpcPlacementResult.OK;
        }
        return ShopVisualPlacementValidator.validate(be.getLevel(), be.getBlockPos(), be.getBlockState().getValue(BaseShopBlock.FACING)).result();
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


