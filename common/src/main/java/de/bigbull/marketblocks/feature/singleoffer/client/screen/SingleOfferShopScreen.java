package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.Util;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.SingleOfferConfig;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.singleoffer.network.AutoFillPaymentPacket;
import de.bigbull.marketblocks.feature.singleoffer.network.ClearTransactionLogPacket;
import de.bigbull.marketblocks.feature.singleoffer.network.CreateOfferPacket;
import de.bigbull.marketblocks.feature.singleoffer.network.DeleteOfferPacket;
import de.bigbull.marketblocks.feature.singleoffer.network.UpdateSettingsPacket;
import de.bigbull.marketblocks.feature.singleoffer.block.BaseShopBlock;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.singleoffer.entity.ShopSettingsManager;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopVisualType;
import de.bigbull.marketblocks.feature.singleoffer.menu.ShopTab;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualPlacementValidator;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcPlacementResult;
import de.bigbull.marketblocks.client.gui.AdminModeToggleButton;
import de.bigbull.marketblocks.client.gui.GuiConstants;
import de.bigbull.marketblocks.client.gui.IconButton;
import de.bigbull.marketblocks.client.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Main GUI screen for the single-offer shop.
 * Handles the display of the offer, inventory, log, and settings tabs.
 */
public class SingleOfferShopScreen extends AbstractSingleOfferShopScreen<SingleOfferShopMenu> {
    private static final ResourceLocation OFFERS_BG = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/singleoffer/singleoffer_offers.png");
    private static final ResourceLocation INVENTORY_BG = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/singleoffer/singleoffer_inventory.png");
    private static final ResourceLocation SETTINGS_BG = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/singleoffer/singleoffer_settings.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/out_of_stock.png");
    private static final ResourceLocation OUTPUT_FULL_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/singleoffer/output_full.png");
    private static final ResourceLocation SHOP_PAUSED_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/singleoffer/shop_paused.png");
    private static final ResourceLocation CREATE_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/create.png");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/delete.png");
    private static final ResourceLocation CLEAR_LOG = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/clear_log.png");
    private static final ResourceLocation RESET_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/reset.png");
    private static final ResourceLocation INPUT_OUTPUT_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/singleoffer/input_output.png");
    private static final WidgetSprites BUTTON_SPRITES_18 = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "18x18/button"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "18x18/button_disabled"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "18x18/button_highlighted"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "18x18/button_selected"));

    private static final int OWNER_HEAD_X_OFFSET = 13;
    private static final int OWNER_HEAD_Y_OFFSET = 5;

    private record IconRect(int x, int y, int width, int height) {
    }

    private static final IconRect STATUS_ICON_RECT = new IconRect(82, 50, 28, 21);

    private ShopTab lastTab;
    private boolean lastHasOffer;
    private int lastMenuFlags;
    private boolean lastAdminShop;
    private SettingsCategory activeSettingsCategory = getFirstEnabledCategory();

    private static SettingsCategory getFirstEnabledCategory() {
        for (SettingsCategory cat : SettingsCategory.values()) {
            if (cat.isEnabled())
                return cat;
        }
        return SettingsCategory.GENERAL;
    }

    private OfferTemplateButton offerButton;
    private EditBox nameField;
    private EditBox npcNameField;
    private EditBox playerSkinNameField;

    private List<IconButton> categoryTabs = new ArrayList<>();

    private boolean saved;
    private String originalName;
    private GeneralSettings originalGeneral;
    private VillagerSettings originalVillager;
    private OfferItemSettings originalOfferItem;
    private IoSettings originalIo;
    private AccessSettings originalAccess;
    private NotificationSettings originalNotification;
    private Button saveButton;

    private GeneralSettings.Draft generalDraft;
    private VillagerSettings.Draft villagerDraft;
    private OfferItemSettings.Draft offerItemDraft;
    private IoSettings.Draft ioDraft;
    private AccessSettings.Draft accessDraft;
    private NotificationSettings.Draft notificationDraft;

    private VisualNpcPlacementResult visualPlacementResult = VisualNpcPlacementResult.OK;

    private final SingleOfferOwnerListPanel ownerListPanel = new SingleOfferOwnerListPanel();
    private final SingleOfferTransactionLogPanel logPanel = new SingleOfferTransactionLogPanel();
    private int lastSettingsVersion = -1;

    public SingleOfferShopScreen(SingleOfferShopMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GuiConstants.IMAGE_WIDTH;
        this.imageHeight = GuiConstants.IMAGE_HEIGHT;
        this.inventoryLabelY = GuiConstants.PLAYER_INV_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        discardUnsavedSettings();
        lastTab = menu.getActiveTab();
        if (!menu.canUseTab(lastTab)) {
            menu.setActiveTabClient(ShopTab.OFFERS);
            lastTab = ShopTab.OFFERS;
        }
        lastHasOffer = menu.getBlockEntity().hasOffer();
        lastMenuFlags = menu.getFlags();
        lastAdminShop = menu.getBlockEntity().isAdminShopEnabled();
        lastSettingsVersion = menu.getBlockEntity().getSettingsVersion();
        sideTabs.clear();
        if (canUseManagementTabs()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, lastTab,
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS),
                    () -> switchTab(ShopTab.LOG),
                    isInventoryTabEnabled(),
                    menu.canUseTab(ShopTab.SETTINGS),
                    menu.canUseTab(ShopTab.LOG));
        }
        buildCurrentTabUI();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        ShopTab current = menu.getActiveTab();
        boolean offerChanged = menu.getBlockEntity().hasOffer() != lastHasOffer;
        boolean flagsChanged = menu.getFlags() != lastMenuFlags;
        boolean adminShopChanged = menu.getBlockEntity().isAdminShopEnabled() != lastAdminShop;
        int currentSettingsVersion = menu.getBlockEntity().getSettingsVersion();
        boolean settingsVersionChanged = currentSettingsVersion != lastSettingsVersion;

        if (settingsVersionChanged) {
            lastSettingsVersion = currentSettingsVersion;
            lastAdminShop = menu.getBlockEntity().isAdminShopEnabled();
            if (current == ShopTab.SETTINGS) {
                syncServerSettings(menu.getBlockEntity());
            } else if (current == ShopTab.OFFERS) {
                rebuildUI();
            } else {
                discardUnsavedSettings();
            }
            return;
        }

        if (flagsChanged || adminShopChanged) {
            lastMenuFlags = menu.getFlags();
            lastAdminShop = menu.getBlockEntity().isAdminShopEnabled();
            if (!menu.canUseTab(current)) {
                switchTab(ShopTab.OFFERS);
            } else {
                rebuildUI();
            }
            return;
        }

        if (!menu.canUseTab(current)) {
            switchTab(ShopTab.OFFERS);
            return;
        }

        if (current != lastTab) {
            if (lastTab == ShopTab.SETTINGS && current != ShopTab.SETTINGS) {
                discardUnsavedSettings();
            }
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
        if (!menu.canUseTab(tab)) {
            return;
        }
        if (lastTab == ShopTab.SETTINGS && tab != ShopTab.SETTINGS) {
            discardUnsavedSettings();
        }
        menu.setActiveTabClient(tab);
        lastTab = tab;
        rebuildUI();
        super.switchTab(tab);
    }

    private void rebuildUI() {
        clearWidgets();
        sideTabs.clear();
        if (canUseManagementTabs()) {
            createTabButtons(leftPos + imageWidth + 4, topPos + 8, menu.getActiveTab(),
                    () -> switchTab(ShopTab.OFFERS),
                    () -> switchTab(ShopTab.INVENTORY),
                    () -> switchTab(ShopTab.SETTINGS),
                    () -> switchTab(ShopTab.LOG),
                    isInventoryTabEnabled(),
                    menu.canUseTab(ShopTab.SETTINGS),
                    menu.canUseTab(ShopTab.LOG));
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
        if (!be.hasOffer()) {
            if (menu.canManageOffer()) {
                addRenderableWidget(new IconButton(leftPos + 143, topPos + 17, 20, 20, BUTTON_SPRITES, CREATE_ICON,
                        b -> createOffer(), Component.translatable("gui.marketblocks.create_offer"), () -> false));
            }
        } else if (isOwner) {
            addRenderableWidget(new IconButton(leftPos + 143, topPos + 17, 20, 20, BUTTON_SPRITES, DELETE_ICON,
                    b -> deleteOffer(), Component.translatable("gui.marketblocks.delete_offer"), () -> false));
        }
    }

    private void buildInventoryUI() {
    }

    private void buildLogUI() {
        if (menu.isPrimaryOwner()) {
            addRenderableWidget(new IconButton(
                    leftPos + 7,
                    topPos + imageHeight - 23,
                    18,
                    18,
                    BUTTON_SPRITES_18,
                    CLEAR_LOG,
                    ignored -> clearTransactionLog(),
                    Component.translatable("gui.marketblocks.log.clear"),
                    () -> false));
        }
    }

    private void buildSettingsUI() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();

        if (originalName == null)
            originalName = be.getShopName();
        ensureSettingsDrafts(be);
        visualPlacementResult = resolveVisualPlacementResult(be);

        boolean isOwner = menu.isOwner();
        boolean canToggleAdminShop = canToggleAdminShop();

        if (isOwner) {
            if (activeSettingsCategory == SettingsCategory.ACCESS && !menu.isPrimaryOwner()) {
                activeSettingsCategory = SettingsCategory.GENERAL;
            }
            categoryTabs = SingleOfferSettingsSections.buildCategoryButtons(this, activeSettingsCategory,
                    this::switchSettingsCategory);
        } else {
            categoryTabs.clear();
            activeSettingsCategory = getFirstEnabledCategory();
        }

        switch (activeSettingsCategory) {
            case GENERAL -> {
                nameField = isOwner ? SingleOfferSettingsSections.buildGeneralSection(this, generalDraft, this::markDirty) : null;
                npcNameField = null;
                playerSkinNameField = null;
                if (canToggleAdminShop) {
                    buildAdminShopToggleButton(be);
                }
            }
            case IO -> {
                nameField = null;
                npcNameField = null;
                playerSkinNameField = null;
                if (isOwner) {
                    SingleOfferSettingsSections.buildIoSection(this, ioDraft, this::markDirty);
                }
            }
            case VILLAGER -> {
                nameField = null;
                if (isOwner) {
                    SingleOfferSettingsSections.VillagerSectionWidgets widgets = SingleOfferSettingsSections
                            .buildVillagerSection(this, villagerDraft, visualPlacementResult, this::markDirty,
                                    this::rebuildUI);
                    npcNameField = widgets.npcNameField();
                    playerSkinNameField = widgets.playerSkinNameField();
                } else {
                    npcNameField = null;
                    playerSkinNameField = null;
                }
            }
            case VISUALS -> {
                nameField = null;
                npcNameField = null;
                playerSkinNameField = null;
                if (isOwner) {
                    ShopVisualType visualType = ShopVisualType.from(be.getBlockState().getBlock());
                    if (visualType != ShopVisualType.UNKNOWN) {
                        SingleOfferSettingsSections.buildOfferItemSection(this, visualType, offerItemDraft,
                                this::markDirty, this::rebuildUI);
                    }
                }
            }
            case ACCESS -> {
                nameField = null;
                npcNameField = null;
                playerSkinNameField = null;
                if (menu.isPrimaryOwner()) {
                    SingleOfferSettingsSections.buildAccessSection(this, accessDraft, ownerListPanel,
                            menu.isPrimaryOwner(), this::saveListPanelToDraft, this::rebuildUI, this::markDirty);
                }
            }
            case NOTIFICATIONS -> {
                nameField = null;
                npcNameField = null;
                playerSkinNameField = null;
                buildSettingsNotificationSection(be);
            }
        }

        if (isOwner) {
            buildSaveButton(be);
            buildResetButton();
        }
    }

    private void buildResetButton() {
        addRenderableWidget(new IconButton(
                leftPos + 6,
                topPos + imageHeight - 24,
                18,
                18,
                BUTTON_SPRITES_18,
                RESET_ICON,
                ignored -> resetCurrentCategorySettings(),
                Component.translatable("gui.marketblocks.settings.reset"),
                () -> false));
    }

    private void resetCurrentCategorySettings() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        ShopSettingsManager sm = be != null ? be.getSettingsManager() : null;
        boolean isMarketCrate = sm != null ? sm.isMarketCrate()
                : (be != null && ShopVisualType.from(be.getBlockState().getBlock()) == ShopVisualType.MARKET_CRATE);

        switch (activeSettingsCategory) {
            case GENERAL -> {
                GeneralSettings def = sm != null ? sm.createDefaultGeneralSettings()
                        : ShopSettingsManager.createDefaultGeneralSettings(isMarketCrate);
                generalDraft = new GeneralSettings.Draft(def);
                if (nameField != null) {
                    nameField.setValue(def.shopName());
                }
                if (accessDraft != null && originalAccess != null) {
                    accessDraft.setAdminShopEnabled(originalAccess.adminShopEnabled());
                }
            }
            case IO -> {
                IoSettings def = sm != null ? sm.createDefaultIoSettings()
                        : ShopSettingsManager.createDefaultIoSettings(isMarketCrate);
                ioDraft = new IoSettings.Draft(def);
            }
            case VILLAGER -> {
                VillagerSettings def = sm != null ? sm.createDefaultVillagerSettings()
                        : ShopSettingsManager.createDefaultVillagerSettings(isMarketCrate);
                villagerDraft = new VillagerSettings.Draft(def);
                if (npcNameField != null) {
                    npcNameField.setValue(def.npcName());
                }
            }
            case VISUALS -> {
                OfferItemSettings def = sm != null ? sm.createDefaultOfferItemSettings()
                        : ShopSettingsManager.createDefaultOfferItemSettings(isMarketCrate);
                offerItemDraft = new OfferItemSettings.Draft(def);
            }
            case ACCESS -> {
                if (!menu.isPrimaryOwner()) {
                    return;
                }
                boolean adminEnabled = accessDraft != null && accessDraft.adminShopEnabled();
                UUID ownerId = accessDraft != null ? accessDraft.ownerId() : null;
                String ownerName = accessDraft != null ? accessDraft.ownerName() : "";
                accessDraft = new AccessSettings.Draft(new AccessSettings(
                        adminEnabled, ownerId, ownerName, Map.of(), AccessMode.WHITELIST, Map.of()));
                ownerListPanel.clearData();
            }
            case NOTIFICATIONS -> {
                NotificationSettings def = sm != null ? sm.createDefaultNotificationSettings()
                        : ShopSettingsManager.createDefaultNotificationSettings(isMarketCrate);
                notificationDraft = new NotificationSettings.Draft(def);
            }
        }
        markDirty();
        rebuildUI();
    }

    private void buildSaveButton(SingleOfferShopBlockEntity be) {
        saveButton = addRenderableWidget(Button.builder(Component.translatable("gui.marketblocks.save"), b -> {
            ensureSettingsDrafts(be);
            if (nameField != null)
                generalDraft.setShopName(nameField.getValue());
            if (npcNameField != null)
                villagerDraft.setNpcName(npcNameField.getValue());
            if (playerSkinNameField != null)
                villagerDraft.setPlayerSkinName(playerSkinNameField.getValue());

            GeneralSettings general = generalDraft.toSettings();
            VillagerSettings villager = villagerDraft.toSettings();
            OfferItemSettings offerItem = offerItemDraft.toSettings();
            IoSettings io = ioDraft.toSettings();

            if (menu.isPrimaryOwner() && activeSettingsCategory == SettingsCategory.ACCESS) {
                saveListPanelToDraft();
            }

            AccessSettings access = accessDraft.toSettings();
            NotificationSettings notifications = notificationDraft
                    .toSettings();

            be.setGeneralSettings(general, false);
            be.setVillagerSettings(villager, false);
            be.setOfferItemSettings(offerItem, false);
            be.setIoSettings(io, false);
            be.setAccessSettings(access, false);
            be.setNotificationSettings(notifications, false);

            NetworkHandler.sendToServer(new UpdateSettingsPacket(
                    be.getBlockPos(), io, general, villager, offerItem, access, notifications));

            originalName = general.shopName();
            originalGeneral = general;
            originalVillager = villager;
            originalOfferItem = offerItem;
            originalIo = io;
            originalAccess = access;
            originalNotification = notifications;
            saved = true;
            lastAdminShop = be.isAdminShopEnabled();
            lastSettingsVersion = be.getSettingsVersion();
            updateSaveButtonState();
            rebuildUI();
        }).bounds(leftPos + imageWidth - 76, topPos + imageHeight - 24, 70, 18).build());
        updateSaveButtonState();
    }

    private void saveListPanelToDraft() {
        if (!menu.isPrimaryOwner() || ownerListPanel == null || accessDraft == null)
            return;
        ownerListPanel.flushToDraft(accessDraft);
    }

    private void buildSettingsNotificationSection(SingleOfferShopBlockEntity be) {
        SingleOfferSettingsSections.buildNotificationSection(this, notificationDraft, this::markDirty);
    }

    private void switchSettingsCategory(SettingsCategory category) {
        if (!menu.isPrimaryOwner() && category == SettingsCategory.ACCESS) {
            return;
        }
        if (activeSettingsCategory == category)
            return;
        if (activeSettingsCategory == SettingsCategory.ACCESS) {
            saveListPanelToDraft();
        }
        if (generalDraft != null && nameField != null)
            generalDraft.setShopName(nameField.getValue());
        if (villagerDraft != null && npcNameField != null)
            villagerDraft.setNpcName(npcNameField.getValue());
        if (villagerDraft != null && playerSkinNameField != null)
            villagerDraft.setPlayerSkinName(playerSkinNameField.getValue());
        activeSettingsCategory = category;
        rebuildUI();
    }

    private void ensureSettingsDrafts(SingleOfferShopBlockEntity be) {
        if (generalDraft == null) {
            originalGeneral = be.getGeneralSettings();
            generalDraft = new GeneralSettings.Draft(originalGeneral);
        }
        if (villagerDraft == null) {
            originalVillager = be.getVillagerSettings();
            villagerDraft = new VillagerSettings.Draft(originalVillager);
        }
        if (offerItemDraft == null) {
            originalOfferItem = be.getOfferItemSettings();
            offerItemDraft = new OfferItemSettings.Draft(originalOfferItem);
        }
        if (ioDraft == null) {
            originalIo = be.getIoSettings();
            ioDraft = new IoSettings.Draft(originalIo);
        }
        if (accessDraft == null) {
            originalAccess = be.getAccessSettings();
            accessDraft = new AccessSettings.Draft(originalAccess);
        }
        if (notificationDraft == null) {
            originalNotification = be.getNotificationSettings();
            notificationDraft = new NotificationSettings.Draft(originalNotification);
        }
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChangesInGeneral()
                || hasUnsavedChangesInVillager()
                || hasUnsavedChangesInOfferItem()
                || hasUnsavedChangesInIo()
                || hasUnsavedChangesInAccess()
                || hasUnsavedChangesInNotification();
    }

    private boolean hasUnsavedChangesInGeneral() {
        return generalDraft != null && originalGeneral != null && !Objects.equals(generalDraft.toSettings(), originalGeneral);
    }

    private boolean hasUnsavedChangesInVillager() {
        return villagerDraft != null && originalVillager != null && !Objects.equals(villagerDraft.toSettings(), originalVillager);
    }

    private boolean hasUnsavedChangesInOfferItem() {
        return offerItemDraft != null && originalOfferItem != null && !Objects.equals(offerItemDraft.toSettings(), originalOfferItem);
    }

    private boolean hasUnsavedChangesInIo() {
        return ioDraft != null && originalIo != null && !Objects.equals(ioDraft.toSettings(), originalIo);
    }

    private boolean hasUnsavedChangesInAccess() {
        if (activeSettingsCategory == SettingsCategory.ACCESS) {
            saveListPanelToDraft();
        }
        return accessDraft != null && originalAccess != null && !Objects.equals(accessDraft.toSettings(), originalAccess);
    }

    private boolean hasUnsavedChangesInNotification() {
        return notificationDraft != null && originalNotification != null && !Objects.equals(notificationDraft.toSettings(), originalNotification);
    }

    private void syncServerSettings(SingleOfferShopBlockEntity be) {
        if (be == null)
            return;

        boolean nameFocused = nameField != null && nameField.isFocused();
        boolean npcNameFocused = npcNameField != null && npcNameField.isFocused();
        boolean playerSkinFocused = playerSkinNameField != null && playerSkinNameField.isFocused();

        boolean hadGeneralChanges = hasUnsavedChangesInGeneral();
        boolean hadVillagerChanges = hasUnsavedChangesInVillager();
        boolean hadOfferItemChanges = hasUnsavedChangesInOfferItem();
        boolean hadIoChanges = hasUnsavedChangesInIo();
        boolean hadAccessChanges = hasUnsavedChangesInAccess();
        boolean hadNotificationChanges = hasUnsavedChangesInNotification();

        // 1. General Settings
        GeneralSettings serverGeneral = be.getGeneralSettings();
        if (generalDraft == null || !hadGeneralChanges) {
            String typedName = nameFocused && generalDraft != null ? generalDraft.shopName() : null;
            generalDraft = new GeneralSettings.Draft(serverGeneral);
            if (typedName != null) {
                generalDraft.setShopName(typedName);
            } else if (nameField != null) {
                nameField.setValue(serverGeneral.shopName());
            }
        } else {
            if (!nameFocused) {
                generalDraft.setShopName(serverGeneral.shopName());
                if (nameField != null) {
                    nameField.setValue(serverGeneral.shopName());
                }
            }
        }
        originalGeneral = serverGeneral;
        originalName = serverGeneral.shopName();

        // 2. Villager Settings
        VillagerSettings serverVillager = be.getVillagerSettings();
        if (villagerDraft == null || !hadVillagerChanges) {
            String typedNpc = npcNameFocused && villagerDraft != null ? villagerDraft.npcName() : null;
            String typedSkin = playerSkinFocused && villagerDraft != null ? villagerDraft.playerSkinName() : null;
            villagerDraft = new VillagerSettings.Draft(serverVillager);
            if (typedNpc != null) {
                villagerDraft.setNpcName(typedNpc);
            } else if (npcNameField != null) {
                npcNameField.setValue(serverVillager.npcName());
            }
            if (typedSkin != null) {
                villagerDraft.setPlayerSkinName(typedSkin);
            } else if (playerSkinNameField != null) {
                playerSkinNameField.setValue(serverVillager.playerSkinName());
            }
        } else {
            if (!npcNameFocused) {
                villagerDraft.setNpcName(serverVillager.npcName());
                if (npcNameField != null) {
                    npcNameField.setValue(serverVillager.npcName());
                }
            }
            if (!playerSkinFocused) {
                villagerDraft.setPlayerSkinName(serverVillager.playerSkinName());
                if (playerSkinNameField != null) {
                    playerSkinNameField.setValue(serverVillager.playerSkinName());
                }
            }
        }
        originalVillager = serverVillager;

        // 3. OfferItem Settings
        originalOfferItem = be.getOfferItemSettings();
        if (offerItemDraft == null || !hadOfferItemChanges) {
            offerItemDraft = new OfferItemSettings.Draft(originalOfferItem);
        }

        // 4. Io Settings
        originalIo = be.getIoSettings();
        if (ioDraft == null || !hadIoChanges) {
            ioDraft = new IoSettings.Draft(originalIo);
        }

        // 5. Access Settings
        originalAccess = be.getAccessSettings();
        if (accessDraft == null || !hadAccessChanges) {
            accessDraft = new AccessSettings.Draft(originalAccess);
            ownerListPanel.syncWithDraft(accessDraft);
        }

        // 6. Notification Settings
        originalNotification = be.getNotificationSettings();
        if (notificationDraft == null || !hadNotificationChanges) {
            notificationDraft = new NotificationSettings.Draft(originalNotification);
        }

        visualPlacementResult = resolveVisualPlacementResult(be);
        updateSaveButtonState();

        if (!nameFocused && !npcNameFocused && !playerSkinFocused) {
            rebuildUI();
        }
    }

    public void updateSaveButtonState() {
        if (saveButton != null) {
            boolean active = hasUnsavedChanges();
            saveButton.active = active;
            saveButton.setTooltip(Tooltip.create(Component.translatable(active
                    ? "gui.marketblocks.save.active.tooltip"
                    : "gui.marketblocks.save.inactive.tooltip")));
        }
    }

    private void markDirty() {
        saved = false;
        updateSaveButtonState();
    }

    public void discardUnsavedSettings() {
        generalDraft = null;
        villagerDraft = null;
        offerItemDraft = null;
        ioDraft = null;
        accessDraft = null;
        notificationDraft = null;
        originalGeneral = null;
        originalVillager = null;
        originalOfferItem = null;
        originalIo = null;
        originalAccess = null;
        originalNotification = null;
        originalName = null;
        nameField = null;
        npcNameField = null;
        playerSkinNameField = null;
        ownerListPanel.clearData();
        saved = false;
        updateSaveButtonState();
    }

    private boolean canUseManagementTabs() {
        if (!menu.getBlockEntity().hasOffer()) {
            return false;
        }
        return menu.isOwner() || menu.canUseTab(ShopTab.SETTINGS) || menu.canUseTab(ShopTab.LOG)
                || menu.canUseTab(ShopTab.INVENTORY);
    }

    private boolean canToggleAdminShop() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        boolean hasAdminShop = (accessDraft != null ? accessDraft.adminShopEnabled()
                : (be != null && be.isAdminShopEnabled()));
        return menu.isOperator() && (menu.isGlobalAdminModeEnabled() || hasAdminShop);
    }

    private boolean isInventoryTabEnabled() {
        boolean adminActive = accessDraft != null ? accessDraft.adminShopEnabled()
                : menu.getBlockEntity().isAdminShopEnabled();
        return menu.canUseTab(ShopTab.INVENTORY) && !adminActive;
    }

    private void buildAdminShopToggleButton(SingleOfferShopBlockEntity be) {
        int x = leftPos + 12;
        int y = topPos + 120;
        int w = 152;
        int h = 18;
        boolean initial = accessDraft != null ? accessDraft.adminShopEnabled() : be.isAdminShopEnabled();
        addSettingsWidget(new AdminModeToggleButton(x, y, w, h, font, initial, (btn, next) -> {
            if (accessDraft != null) {
                accessDraft.setAdminShopEnabled(next);
            }
            markDirty();
            rebuildUI();
        }));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        for (IconButton tab : sideTabs) {
            if (tab.isMouseOver(mouseX, mouseY) && tab.getTooltipMessage() != null) {
                graphics.renderTooltip(font, tab.getTooltipMessage(), mouseX, mouseY);
            }
        }

        if (menu.getActiveTab() == ShopTab.SETTINGS) {
            for (IconButton tab : categoryTabs) {
                if (tab.isMouseOver(mouseX, mouseY) && tab.getTooltipMessage() != null) {
                    graphics.renderTooltip(font, tab.getTooltipMessage(), mouseX, mouseY);
                }
            }
        } else if (menu.getActiveTab() == ShopTab.OFFERS) {
            SingleOfferShopBlockEntity be = menu.getBlockEntity();
            boolean showAdminBadge = be.isAdminShopEnabled();
            boolean hasOwner = be.getOwnerId() != null && be.getOwnerName() != null && !be.getOwnerName().isBlank();

            if (!showAdminBadge && !menu.isOwner() && hasOwner
                    && isHovering(imageWidth - OWNER_HEAD_X_OFFSET - 1, OWNER_HEAD_Y_OFFSET - 1, 10, 10, mouseX,
                            mouseY)) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("gui.marketblocks.owner", be.getOwnerName()));
                if (!be.getAdditionalOwners().isEmpty()) {
                    tooltip.add(Component.translatable("gui.marketblocks.access.edit_owners")
                            .append(": ")
                            .append(String.join(", ", be.getAdditionalOwners().values())));
                }
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            } else {
                String name = be.getShopName();
                Component fullTitle = (name != null && !name.isEmpty()) ? Component.literal(name)
                        : be.getBlockState().getBlock().getName();
                int badgeW = font.width(Component.translatable("gui.marketblocks.admin_shop.badge")) + 8;
                int maxTitleW = showAdminBadge ? (imageWidth - badgeW - 8 - 8 - 4)
                        : (!menu.isOwner() && hasOwner ? (imageWidth - OWNER_HEAD_X_OFFSET - 8 - 4)
                                : (imageWidth - 16));
                if (font.width(fullTitle) > maxTitleW && isHovering(8, 6, maxTitleW, font.lineHeight, mouseX, mouseY)) {
                    graphics.renderTooltip(font, fullTitle, mouseX, mouseY);
                } else if (be.hasOffer()
                        && isHovering(STATUS_ICON_RECT.x(), STATUS_ICON_RECT.y(), STATUS_ICON_RECT.width(),
                                STATUS_ICON_RECT.height(), mouseX, mouseY)) {
                    if (be.isClosed()) {
                        graphics.renderTooltip(font, Component.translatable("gui.marketblocks.shop_closed"), mouseX,
                                mouseY);
                    } else if (!be.isAdminShopEnabled()) {
                        if (!be.hasResultItemInInput(false)) {
                            graphics.renderTooltip(font, Component.translatable("gui.marketblocks.out_of_stock"),
                                    mouseX,
                                    mouseY);
                        } else if (be.isOutputSpaceMissing()) {
                            graphics.renderTooltip(font, Component.translatable("gui.marketblocks.output_full"), mouseX,
                                    mouseY);
                        }
                    }
                }
            }
        } else if (menu.getActiveTab() == ShopTab.LOG) {
            logPanel.renderHoverTooltip(graphics, font, leftPos, topPos, mouseX, mouseY,
                    menu.getTransactionLogEntries());

            int count = menu.getTransactionLogEntries().size();
            int numW = font.width(String.valueOf(count));
            int badgeW = Math.max(18, numW + 8);
            int badgeH = 14;
            int badgeX = imageWidth - 7 - badgeW;
            int badgeY = imageHeight - 21;
            int labelW = font.width(Component.translatable("gui.marketblocks.log.entries_label"));
            int labelX = badgeX - 4 - labelW;

            if (isHovering(labelX, badgeY, labelW + 4 + badgeW, badgeH, mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable("gui.marketblocks.log.count_tooltip"), mouseX,
                        mouseY);
            }
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.getFocused() instanceof EditBox editBox
                && editBox.canConsumeInput()) {
            if (pKeyCode == 256) {
                this.setFocused(null);
                return true;
            }
            if (pKeyCode == 258) {
                return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
            editBox.keyPressed(pKeyCode, pScanCode, pModifiers);
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.getFocused() instanceof EditBox editBox && editBox.canConsumeInput()) {
            if (editBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        for (IconButton tab : sideTabs) {
            if (!tab.isSelected()) {
                tab.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
        }

        switch (menu.getActiveTab()) {
            case OFFERS -> renderOffersBg(graphics);
            case INVENTORY -> renderInventoryBg(graphics);
            case SETTINGS -> renderSettingsBg(graphics, mouseX, mouseY, partialTick);
            case LOG -> renderLogBg(graphics, mouseX, mouseY);
        }

        for (IconButton tab : sideTabs) {
            if (tab.isSelected()) {
                tab.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void renderOffersBg(GuiGraphics graphics) {
        graphics.blit(OFFERS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        offerButton.active = be.hasOffer();
        if (be.hasOffer()) {
            offerButton.update(be.getOfferPayment1(), be.getOfferPayment2(), be.getOfferResult(),
                    be.isAdminShopEnabled() || be.hasResultItemInInput(false));
        } else {
            ItemStack p1 = menu.slots.get(0).getItem();
            ItemStack p2 = menu.slots.get(1).getItem();
            Pair<ItemStack, ItemStack> norm = normalizePayments(p1, p2);
            offerButton.update(norm.getFirst(), norm.getSecond(), menu.slots.get(2).getItem(), true);
        }
        int iconX = leftPos + STATUS_ICON_RECT.x();
        int iconY = topPos + STATUS_ICON_RECT.y();

        boolean paused = be.isClosed();
        boolean outOfStock = !be.isAdminShopEnabled() && be.hasOffer() && !be.hasResultItemInInput(false);
        boolean outputBlocked = !be.isAdminShopEnabled() && be.hasOffer() && be.isOutputSpaceMissing();

        if (paused) {
            graphics.blit(SHOP_PAUSED_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(),
                    STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        } else if (outOfStock) {
            graphics.blit(OUT_OF_STOCK_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(),
                    STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        } else if (outputBlocked || (be.hasOffer() && be.isOfferAvailable() && be.isOutputAlmostFull())) {
            graphics.blit(OUTPUT_FULL_ICON, iconX, iconY, 0, 0, STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height(),
                    STATUS_ICON_RECT.width(), STATUS_ICON_RECT.height());
        }
    }

    private void renderInventoryBg(GuiGraphics graphics) {
        graphics.blit(INVENTORY_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(INPUT_OUTPUT_ICON, leftPos + 77, topPos + 33, 0, 0, 22, 22, 22, 22);
    }

    private void renderSettingsBg(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (IconButton tab : categoryTabs) {
            if (!tab.isSelected()) {
                tab.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
        }

        graphics.blit(SETTINGS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        for (IconButton tab : categoryTabs) {
            if (tab.isSelected()) {
                tab.renderWidget(graphics, mouseX, mouseY, partialTick);
            }
        }

        if (activeSettingsCategory == SettingsCategory.ACCESS && menu.isPrimaryOwner()) {
            SingleOfferSettingsSections.renderAccessBg(graphics, font, leftPos, topPos,
                    ownerListPanel.getListMode(),
                    accessDraft != null ? accessDraft.accessMode() : AccessMode.WHITELIST,
                    ownerListPanel.collectSelectedOwners().size(),
                    SingleOfferConfig.MAX_CO_OWNERS_PER_SHOP.get());
            ownerListPanel.renderBackground(graphics, font, leftPos, topPos, mouseX, mouseY);
        }

        if (menu.isOwner() && activeSettingsCategory == SettingsCategory.GENERAL) {
            SingleOfferSettingsSections.renderGeneralBg(graphics, font, leftPos, topPos);
        }

        if (menu.isOwner() && activeSettingsCategory == SettingsCategory.VILLAGER) {
            boolean enabled = villagerDraft != null && villagerDraft.npcEnabled();
            boolean canSpawn = visualPlacementResult == null || visualPlacementResult.canSpawn();
            SingleOfferSettingsSections.renderVillagerBg(graphics, font, leftPos, topPos, enabled, canSpawn);
        }

        if (menu.isOwner() && activeSettingsCategory == SettingsCategory.VISUALS) {
            ShopVisualType visualType = ShopVisualType.from(menu.getBlockEntity().getBlockState().getBlock());
            boolean enabled = offerItemDraft != null && offerItemDraft.visible();
            SingleOfferSettingsSections.renderVisualsBg(graphics, font, visualType, leftPos, topPos, enabled);
        }

        if (menu.isOwner() && activeSettingsCategory == SettingsCategory.NOTIFICATIONS) {
            SingleOfferSettingsSections.renderNotificationsBg(graphics, font, leftPos, topPos);
        }

        if (menu.isOwner() && activeSettingsCategory == SettingsCategory.IO) {
            boolean enabled = ioDraft != null && ioDraft.allowIo();
            SingleOfferSettingsSections.renderIoBg(graphics, font, leftPos, topPos, enabled);
        }
    }

    private void renderLogBg(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.blit(SETTINGS_BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        logPanel.renderBackground(graphics, font, leftPos, topPos, mouseX, mouseY, menu.getTransactionLogEntries());
    }

    private void clearTransactionLog() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new ClearTransactionLogPacket(be.getBlockPos()));
        menu.setTransactionLogEntries(List.of());
        logPanel.reset();
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
        boolean showAdminBadge = be.isAdminShopEnabled();

        Component badgeText = Component.translatable("gui.marketblocks.admin_shop.badge");
        int badgeW = font.width(badgeText) + 8;
        int badgeH = 11;
        int badgeX = imageWidth - badgeW - 8;
        int badgeY = 4;

        boolean hasOwner = be.getOwnerId() != null && be.getOwnerName() != null && !be.getOwnerName().isBlank();
        int maxTitleWidth;

        if (showAdminBadge) {
            graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 0xFF8A38D0);
            graphics.fill(badgeX + 1, badgeY + 1, badgeX + badgeW - 1, badgeY + badgeH - 1, 0xFF2E1840);
            graphics.drawString(font, badgeText, badgeX + 4, badgeY + 2, 0xFFE0A0FF, false);
            maxTitleWidth = badgeX - 8 - 4;
        } else if (!menu.isOwner() && hasOwner) {
            renderOwnerHead(graphics, be.getOwnerId(), be.getOwnerName(), imageWidth - OWNER_HEAD_X_OFFSET,
                    OWNER_HEAD_Y_OFFSET);
            maxTitleWidth = (imageWidth - OWNER_HEAD_X_OFFSET) - 8 - 4;
        } else {
            maxTitleWidth = imageWidth - 16;
        }

        String name = be.getShopName();
        Component fullTitle = (name != null && !name.isEmpty()) ? Component.literal(name)
                : be.getBlockState().getBlock().getName();
        Component displayTitle;
        if (font.width(fullTitle) > maxTitleWidth) {
            displayTitle = Component.literal(
                    font.plainSubstrByWidth(fullTitle.getString(), Math.max(0, maxTitleWidth - font.width("...")))
                            + "...");
        } else {
            displayTitle = fullTitle;
        }
        graphics.drawString(font, displayTitle, 8, 6, 4210752, false);

        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void renderOwnerHead(GuiGraphics graphics, UUID id, String name, int x, int y) {
        Minecraft client = Minecraft.getInstance();
        GameProfile profile = new GameProfile(id != null ? id : Util.NIL_UUID, name != null ? name : "");
        ResourceLocation skinTexture = client.getSkinManager().getInsecureSkin(profile).texture();

        graphics.fill(x - 1, y - 1, x + 9, y + 9, 0xFF2A2A2A);
        graphics.fill(x, y, x + 8, y + 8, 0xFF181818);
        graphics.blit(skinTexture, x, y, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
        graphics.blit(skinTexture, x, y, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    private void renderInventoryLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.input"), 8, 6, 4210752, false);
        graphics.drawString(font, Component.translatable("gui.marketblocks.output"), 98, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        if (be.isAdminShopEnabled()) {
            Component info = Component.translatable("gui.marketblocks.inventory_admin_disabled");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        } else if (!menu.isOwner()) {
            Component info = Component.translatable("gui.marketblocks.inventory_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, GuiConstants.PLAYER_INV_LABEL_Y, 4210752, false);
    }

    private void renderSettingsLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        Component headerTitle = activeSettingsCategory != null ? activeSettingsCategory.headerTitle()
                : Component.translatable("gui.marketblocks.settings_title");
        graphics.drawString(font, headerTitle, 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);
        if (!menu.isOwner() && !canToggleAdminShop()) {
            Component info = Component.translatable("gui.marketblocks.settings_owner_only");
            int w = font.width(info);
            graphics.drawString(font, info, (imageWidth - w) / 2, 84, 0x808080, false);
        }
    }

    private void renderLogLabels(GuiGraphics graphics) {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        graphics.drawString(font, Component.translatable("gui.marketblocks.log_title"), 8, 6, 4210752, false);
        renderOwnerInfo(graphics, be, menu.isOwner(), imageWidth);

        int count = menu.getTransactionLogEntries().size();
        Component label = Component.translatable("gui.marketblocks.log.entries_label");
        Component countComp = Component.literal(String.valueOf(count));

        int numW = font.width(countComp);
        int badgeW = Math.max(18, numW + 8);
        int badgeH = 14;
        int badgeX = imageWidth - 7 - badgeW;
        int badgeY = imageHeight - 21;

        // Label
        int labelW = font.width(label);
        int labelX = badgeX - 4 - labelW;
        int labelY = badgeY + (badgeH - font.lineHeight) / 2 + 1;
        graphics.drawString(font, label, labelX, labelY, 4210752, false);

        // Badge Inset & Border (Access Settings / Slider style)
        graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 0xFF222222);
        graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 1, 0xFF373737);
        graphics.fill(badgeX, badgeY + badgeH - 1, badgeX + badgeW, badgeY + badgeH, 0xFF373737);
        graphics.fill(badgeX, badgeY + 1, badgeX + 1, badgeY + badgeH - 1, 0xFF373737);
        graphics.fill(badgeX + badgeW - 1, badgeY + 1, badgeX + badgeW, badgeY + badgeH - 1, 0xFF373737);

        // Value
        int textX = badgeX + (badgeW - numW) / 2;
        int textY = badgeY + (badgeH - font.lineHeight) / 2 + 1;
        int textColor = count > 0 ? 0x55FF55 : 0x808080;
        graphics.drawString(font, countComp, textX, textY, textColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.getActiveTab() == ShopTab.SETTINGS) {
            for (IconButton tab : categoryTabs) {
                if (tab.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        if (menu.getActiveTab() == ShopTab.LOG && button == 0) {
            if (logPanel.onMouseClicked(mouseX, mouseY, leftPos, topPos, menu.getTransactionLogEntries())) {
                return true;
            }
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS
                && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseClicked(mouseX, mouseY, leftPos))
                return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (menu.getActiveTab() == ShopTab.LOG) {
            if (logPanel.onMouseDragged(mouseY, topPos, menu.getTransactionLogEntries())) {
                return true;
            }
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS
                && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseDragged(mouseY))
                return true;
        }
        if (this.getFocused() != null && this.isDragging() && button == 0) {
            if (this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        for (var child : this.children()) {
            if (child instanceof de.bigbull.marketblocks.client.gui.CustomSlider slider && slider.isDragging()) {
                if (slider.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            logPanel.onMouseReleased();
        }
        ownerListPanel.onMouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.getActiveTab() == ShopTab.LOG) {
            if (logPanel.onMouseScrolled(mouseX, mouseY, scrollY, leftPos, topPos, menu.getTransactionLogEntries())) {
                return true;
            }
        }
        if (menu.getActiveTab() == ShopTab.SETTINGS && activeSettingsCategory == SettingsCategory.ACCESS
                && menu.isPrimaryOwner()) {
            if (ownerListPanel.onMouseScrolled(mouseX, mouseY, scrollY, leftPos))
                return true;
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
                minecraft.gui.getChat().addMessage(
                        Component.translatable("gui.marketblocks.error.no_result_item").withStyle(ChatFormatting.RED));
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }
            if (p1.isEmpty() && p2.isEmpty()) {
                minecraft.gui.getChat().addMessage(Component.translatable("gui.marketblocks.error.no_payment_items")
                        .withStyle(ChatFormatting.RED));
                playSound(SoundEvents.ITEM_BREAK);
                return;
            }
            SingleOfferShopBlockEntity be = menu.getBlockEntity();
            NetworkHandler.sendToServer(new CreateOfferPacket(be.getBlockPos(), p1, p2, result));
            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
        } catch (Exception e) {
            Constants.LOG.error("Error creating offer", e);
            playSound(SoundEvents.ITEM_BREAK);
        }
    }

    private void deleteOffer() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        NetworkHandler.sendToServer(new DeleteOfferPacket(be.getBlockPos()));
        be.setHasOfferClient(false);
        rebuildUI();
    }

    private void onOfferClicked() {
        SingleOfferShopBlockEntity be = menu.getBlockEntity();
        if (be.hasOffer()) {
            if (be.isClosed() && !menu.isOwner() && !menu.isOperator()) {
                return;
            }
            NetworkHandler.sendToServer(new AutoFillPaymentPacket(be.getBlockPos()));
            return;
        }
        if (menu.canManageOffer()) {
            for (int i = 0; i < 3; i++)
                menu.slots.get(i).set(ItemStack.EMPTY);
        }
    }

    private Pair<ItemStack, ItemStack> normalizePayments(ItemStack p1, ItemStack p2) {
        if (p1.isEmpty() && !p2.isEmpty())
            return Pair.of(p2, ItemStack.EMPTY);
        return Pair.of(p1, p2);
    }

    @Override
    public void onClose() {
        if (!saved) {
            discardUnsavedSettings();
        }
        super.onClose();
    }

    private VisualNpcPlacementResult resolveVisualPlacementResult(SingleOfferShopBlockEntity be) {
        if (be.getLevel() == null)
            return VisualNpcPlacementResult.OK;
        return ShopVisualPlacementValidator
                .validate(be.getLevel(), be.getBlockPos(), be.getBlockState().getValue(BaseShopBlock.FACING)).result();
    }

    <T extends AbstractWidget> T addSettingsWidget(T widget) {
        return addRenderableWidget(widget);
    }

    void removeSettingsWidget(AbstractWidget widget) {
        removeWidget(widget);
    }

    int settingsLeftPos() {
        return leftPos;
    }

    int settingsTopPos() {
        return topPos;
    }

    Font settingsFont() {
        return font;
    }
}
