package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import de.bigbull.marketblocks.client.gui.CompactCheckbox;
import de.bigbull.marketblocks.client.gui.CompactNumberBox;
import de.bigbull.marketblocks.client.gui.CustomSlider;
import de.bigbull.marketblocks.client.gui.FloatSlider;
import de.bigbull.marketblocks.client.gui.GroupBox;
import de.bigbull.marketblocks.client.gui.GuiConstants;
import de.bigbull.marketblocks.client.gui.IconButton;
import de.bigbull.marketblocks.client.gui.MiniArrowButton;
import de.bigbull.marketblocks.client.gui.SideModeButton;
import de.bigbull.marketblocks.client.gui.StatusToggleButton;
import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopVisualType;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcPlacementResult;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Builds the category-dependent settings widgets for the single-offer shop
 * screen.
 */
public final class SingleOfferSettingsSections {
    private static final int SETTINGS_CATEGORY_BUTTON_Y_OFFSET = -28;
    private static final int SETTINGS_CATEGORY_BUTTON_X_OFFSET = 0;
    private static final int SETTINGS_CATEGORY_BUTTON_WIDTH = 26;
    private static final int SETTINGS_CATEGORY_BUTTON_HEIGHT = 32;
    private static final int SETTINGS_CATEGORY_BUTTON_GAP = 1;

    private static WidgetSprites getSettingsTabSprites(int index, int total) {
        String suffix = (index == 0) ? "1" : "2";
        return new WidgetSprites(
                ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_" + suffix),
                ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_" + suffix),
                ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_" + suffix), // Hover
                                                                                                                    // equals
                                                                                                                    // unselected
                ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_" + suffix));
    }

    private SingleOfferSettingsSections() {
    }

    public static List<IconButton> buildCategoryButtons(SingleOfferShopScreen host, SettingsCategory activeCategory,
            Consumer<SettingsCategory> onSwitch) {
        List<IconButton> tabs = new java.util.ArrayList<>();
        int x = host.settingsLeftPos() + SETTINGS_CATEGORY_BUTTON_X_OFFSET;
        int y = host.settingsTopPos() + SETTINGS_CATEGORY_BUTTON_Y_OFFSET;

        int totalTabs = 0;
        for (SettingsCategory category : SettingsCategory.values()) {
            if (category.isEnabled())
                totalTabs++;
        }

        int index = 0;
        for (SettingsCategory category : SettingsCategory.values()) {
            if (!category.isEnabled())
                continue;

            boolean isSelected = category == activeCategory;
            int currentHeight = isSelected ? SETTINGS_CATEGORY_BUTTON_HEIGHT : 28;
            int currentY = isSelected ? y : y + 1;

            IconButton tabButton = new IconButton(
                    x,
                    currentY,
                    SETTINGS_CATEGORY_BUTTON_WIDTH,
                    currentHeight,
                    getSettingsTabSprites(index, totalTabs),
                    category.icon(),
                    b -> {
                        if (category != activeCategory) {
                            onSwitch.accept(category);
                        }
                    },
                    category.title(),
                    () -> category == activeCategory)
                    .withIconOffset(0, 2)
                    .withSelectedIconOffset(0, -2);
            tabs.add(tabButton);
            x += SETTINGS_CATEGORY_BUTTON_WIDTH + SETTINGS_CATEGORY_BUTTON_GAP;
            index++;
        }
        return tabs;
    }

    public static EditBox buildGeneralSection(SingleOfferShopScreen host,
            GeneralSettings.Draft draft,
            Runnable onDirty) {
        // Compact Icon-Only Status Toggle (top-right header: 14x14 at leftPos + 154, topPos + 5)
        host.addSettingsWidget(new StatusToggleButton(
                host.settingsLeftPos() + 154,
                host.settingsTopPos() + 5,
                14,
                14,
                host.settingsFont(),
                draft.isClosed(),
                (btn, paused) -> {
                    draft.setIsClosed(paused);
                    onDirty.run();
                }));

        // --- GroupBox 1: SHOP-PROFIL (y = 23, h = 46, ends at 69) ---
        // Row 1: Name field
        EditBox nameField = host.addSettingsWidget(
                new EditBox(host.settingsFont(), host.settingsLeftPos() + 42, host.settingsTopPos() + 30, 122, 14,
                        Component.translatable("gui.marketblocks.shop_name")));
        nameField.setMaxLength(32);
        nameField.setValue(draft.shopName());
        nameField.setResponder(value -> {
            draft.setShopName(value);
            onDirty.run();
        });
        nameField.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.shop_name.tooltip")));

        // Row 2: Category button
        Button categoryBtn = host.addSettingsWidget(Button.builder(
                Component.translatable("gui.marketblocks.category." + draft.shopCategory().getId()),
                b -> {
                    draft.setShopCategory(draft.shopCategory().next());
                    b.setMessage(Component.translatable("gui.marketblocks.category." + draft.shopCategory().getId()));
                    onDirty.run();
                })
                .bounds(host.settingsLeftPos() + 12, host.settingsTopPos() + 48, 152, 16)
                .build());
        categoryBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.category.tooltip")));

        // --- GroupBox 2: FUNKTIONEN & SIGNALE (y = 75, h = 40, exact 6px gap to Box 1) ---
        // Row 1: Redstone Signal Checkbox (7px top offset to border, identical to NPC settings)
        CompactCheckbox emitCheckbox = host.addSettingsWidget(new CompactCheckbox(
                host.settingsLeftPos() + 12, host.settingsTopPos() + 82,
                Component.translatable("gui.marketblocks.emit_redstone"),
                host.settingsFont(),
                draft.emitRedstone(),
                (checkbox, value) -> {
                    draft.setEmitRedstone(value);
                    onDirty.run();
                }).setCompactText(true).setBoxOnRightEdge(true).setCustomWidth(152));
        emitCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")));

        // Row 2: XP Sound Checkbox
        CompactCheckbox xpSoundCheckbox = host.addSettingsWidget(new CompactCheckbox(
                host.settingsLeftPos() + 12, host.settingsTopPos() + 98,
                Component.translatable("gui.marketblocks.purchase_sound"),
                host.settingsFont(),
                draft.purchaseXpFeedbackSound(),
                (checkbox, value) -> {
                    draft.setPurchaseXpFeedbackSound(value);
                    onDirty.run();
                }).setCompactText(true).setBoxOnRightEdge(true).setCustomWidth(152));
        xpSoundCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.purchase_sound.tooltip")));

        return nameField;
    }

    /**
     * Renders background group boxes and static labels for the General tab.
     */
    public static void renderGeneralBg(GuiGraphics graphics, Font font, int leftPos, int topPos) {
        Component statusLabel = GuiConstants.compact(Component.translatable("gui.marketblocks.general.status_label"));
        graphics.drawString(font, statusLabel, leftPos + 150 - font.width(statusLabel), topPos + 8, 0x404040, false);

        // GroupBox 1: Shop-Profil (h = 46, ends at 69)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.general.group.shop_profile"),
                leftPos + 7, topPos + 23, 162, 46);

        Component nameLabel = GuiConstants.compact(Component.translatable("gui.marketblocks.general.shop_name_label"));
        graphics.drawString(font, nameLabel, leftPos + 12, topPos + 33, 0x404040, false);

        // GroupBox 2: Funktionen & Signale (starts at 75, exact 6px gap, h = 40)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.general.group.features"),
                leftPos + 7, topPos + 75, 162, 40);
    }

    public static void buildIoSection(SingleOfferShopScreen host,
            IoSettings.Draft draft,
            Runnable onDirty) {
        int leftX = host.settingsLeftPos() + 12;
        int topBtnX = host.settingsLeftPos() + 134;
        int topBtnY = host.settingsTopPos() + 5;
        List<Consumer<Boolean>> sectionWidgets = new ArrayList<>();

        // Master Toggle Button (top right, 34x14)
        Button ioToggle = host.addSettingsWidget(Button.builder(
                toggleStateLabel(draft.allowIo()),
                b -> {
                    boolean next = !draft.allowIo();
                    draft.setAllowIo(next);
                    b.setMessage(toggleStateLabel(next));
                    for (Consumer<Boolean> updater : sectionWidgets) {
                        updater.accept(next);
                    }
                    onDirty.run();
                })
                .bounds(topBtnX, topBtnY, 34, 14)
                .tooltip(Tooltip.create(Component.translatable("gui.marketblocks.io.master_toggle.tooltip")))
                .build());

        boolean enabled = draft.allowIo();

        // --- GroupBox 1: BLOCK-SEITEN / TRICHTER (y = 23, h = 62) ---
        int cx = host.settingsLeftPos() + 7 + 40;
        int cy = host.settingsTopPos() + 23 + 24;

        // Top: Hinten (Direction.NORTH)
        SideModeButton backButton = host.addSettingsWidget(
                new SideModeButton(cx, cy - 17, 16, 16,
                        Component.translatable("gui.marketblocks.side.back"),
                        draft.getMode(Direction.NORTH), m -> {
                            draft.setMode(Direction.NORTH, m);
                            onDirty.run();
                        }));
        backButton.active = enabled;
        backButton.updateTooltip();

        // Left: Links (Direction.WEST)
        SideModeButton leftButton = host.addSettingsWidget(
                new SideModeButton(cx - 17, cy, 16, 16,
                        Component.translatable("gui.marketblocks.side.left"),
                        draft.getMode(Direction.WEST), m -> {
                            draft.setMode(Direction.WEST, m);
                            onDirty.run();
                        }));
        leftButton.active = enabled;
        leftButton.updateTooltip();

        // Right: Rechts (Direction.EAST)
        SideModeButton rightButton = host.addSettingsWidget(
                new SideModeButton(cx + 17, cy, 16, 16,
                        Component.translatable("gui.marketblocks.side.right"),
                        draft.getMode(Direction.EAST), m -> {
                            draft.setMode(Direction.EAST, m);
                            onDirty.run();
                        }));
        rightButton.active = enabled;
        rightButton.updateTooltip();

        // Bottom: Unten (Direction.DOWN)
        SideModeButton bottomButton = host.addSettingsWidget(
                new SideModeButton(cx, cy + 17, 16, 16,
                        Component.translatable("gui.marketblocks.side.bottom"),
                        draft.getMode(Direction.DOWN), m -> {
                            draft.setMode(Direction.DOWN, m);
                            onDirty.run();
                        }));
        bottomButton.active = enabled;
        bottomButton.updateTooltip();

        // --- GroupBox 2: AUTOMATISIERUNG & REDSTONE (y = 91, h = 45, 6px gap) ---
        int b2Y = host.settingsTopPos() + 91;

        // Row 1: Auto-IO Checkbox
        CompactCheckbox autoIoCb = host.addSettingsWidget(new CompactCheckbox(
                leftX, b2Y + 8,
                Component.translatable("gui.marketblocks.io.auto_io"), host.settingsFont(),
                draft.autoIo(),
                12,
                (c, v) -> {
                    draft.setAutoIo(v);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        autoIoCb.active = enabled;
        if (enabled) {
            autoIoCb.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.io.auto_io.tooltip")));
        }

        // Row 2: Redstone Control Button
        Button redstoneBtn = host.addSettingsWidget(Button.builder(
                Component.translatable(draft.redstoneControl().translationKey()),
                b -> {
                    draft.setRedstoneControl(draft.redstoneControl().next());
                    b.setMessage(Component.translatable(draft.redstoneControl().translationKey()));
                    onDirty.run();
                })
                .bounds(leftX, b2Y + 23, 152, 16)
                .build());
        redstoneBtn.active = enabled;
        if (enabled) {
            redstoneBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.io.redstone_control.tooltip")));
        }

        // Updater for when allowIo is toggled
        sectionWidgets.add(en -> {
            backButton.active = en;
            backButton.updateTooltip();
            leftButton.active = en;
            leftButton.updateTooltip();
            rightButton.active = en;
            rightButton.updateTooltip();
            bottomButton.active = en;
            bottomButton.updateTooltip();

            autoIoCb.active = en;
            autoIoCb.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.io.auto_io.tooltip")) : null);

            redstoneBtn.active = en;
            redstoneBtn.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.io.redstone_control.tooltip")) : null);
        });
    }

    /**
     * Renders background group boxes, labels, and schematic cross/legend for the I/O tab.
     */
    public static void renderIoBg(GuiGraphics graphics, Font font, int leftPos, int topPos, boolean enabled) {
        int groupTitleColor = enabled ? GroupBox.DEFAULT_TITLE_COLOR : 0x808080;
        int groupBorderColor = enabled ? GroupBox.DEFAULT_BORDER_COLOR : 0xFF888888;

        // Header Status Label: "I/O:"
        Component statusLabel = GuiConstants.compact(Component.translatable("gui.marketblocks.io.status_label"));
        graphics.drawString(font, statusLabel, leftPos + 130 - font.width(statusLabel), topPos + 8, 0x404040, false);

        // GroupBox 1: BLOCK-SEITEN / TRICHTER (y = 23, h = 62)
        int b1Y = topPos + 23;
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.io.group.sides"),
                leftPos + 7, b1Y, 162, 62, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);

        int cx = leftPos + 7 + 40;
        int cy = b1Y + 24;

        // Center decorative mini block (16x16)
        graphics.fill(cx, cy, cx + 16, cy + 16, 0xFF2A2A2A);
        graphics.fill(cx + 1, cy + 1, cx + 15, cy + 15, 0xFF181818);
        graphics.fill(cx + 3, cy + 3, cx + 13, cy + 13, 0xFF654321);
        graphics.renderOutline(cx + 4, cy + 4, 8, 8, 0xFF9A6F3C);

        // Micro labels around cross in compact 6px font (vertically centered on line with buttons)
        int microColor = enabled ? 0x404040 : 0x888888;
        Component hLbl = GuiConstants.compact(Component.translatable("gui.marketblocks.side.back.letter"));
        Component lLbl = GuiConstants.compact(Component.translatable("gui.marketblocks.side.left.letter"));
        Component rLbl = GuiConstants.compact(Component.translatable("gui.marketblocks.side.right.letter"));
        Component uLbl = GuiConstants.compact(Component.translatable("gui.marketblocks.side.bottom.letter"));

        // H to the left of Top/Hinten button (cx - 2 - font.width(hLbl))
        graphics.drawString(font, hLbl, cx - 2 - font.width(hLbl), cy - 17 + 4, microColor, false);
        // L to the left of Left/Links button
        graphics.drawString(font, lLbl, cx - 17 - 2 - font.width(lLbl), cy + 4, microColor, false);
        // R to the right of Right/Rechts button
        graphics.drawString(font, rLbl, cx + 17 + 16 + 2, cy + 4, microColor, false);
        // U to the left of Bottom/Unten button
        graphics.drawString(font, uLbl, cx - 2 - font.width(uLbl), cy + 17 + 4, microColor, false);

        // Right side legend: shifted right to leftPos + 100
        int legX = leftPos + 100;
        int legY = b1Y + 16;

        // Row 1: EINGANG (Green)
        graphics.blitSprite(SideModeButton.INPUT_ICON, legX, legY, 11, 11);
        Component inText = GuiConstants.compact(Component.translatable("gui.marketblocks.legend.input"));
        graphics.drawString(font, inText, legX + 14, legY + 2, enabled ? 0x008800 : 0x808080, false);

        // Row 2: AUSGANG (Red)
        legY += 14;
        graphics.blitSprite(SideModeButton.OUTPUT_ICON, legX, legY, 11, 11);
        Component outText = GuiConstants.compact(Component.translatable("gui.marketblocks.legend.output"));
        graphics.drawString(font, outText, legX + 14, legY + 2, enabled ? 0xBC0000 : 0x808080, false);

        // Row 3: DEAKTIVIERT (Dark grey)
        legY += 14;
        graphics.blitSprite(SideModeButton.DISABLED_ICON, legX, legY, 11, 11);
        Component disText = GuiConstants.compact(Component.translatable("gui.marketblocks.legend.disabled"));
        graphics.drawString(font, disText, legX + 14, legY + 2, enabled ? 0x505050 : 0x808080, false);

        // GroupBox 2: AUTOMATISIERUNG & REDSTONE (y = 91, h = 45, 6px gap)
        int b2Y = b1Y + 62 + 6;
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.io.group.automation"),
                leftPos + 7, b2Y, 162, 45, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);
    }

    public static VillagerSectionWidgets buildVillagerSection(
            SingleOfferShopScreen host,
            VillagerSettings.Draft draft,
            VisualNpcPlacementResult placementResult,
            Runnable onDirty,
            Runnable onRebuild) {
        int leftX = host.settingsLeftPos() + 12;
        int topBtnX = host.settingsLeftPos() + 134;
        int topBtnY = host.settingsTopPos() + 5;
        List<Consumer<Boolean>> sectionWidgets = new ArrayList<>();

        // Master Toggle (top right, 34x14)
        Button npcToggle = host.addSettingsWidget(Button.builder(
                toggleStateLabel(draft.npcEnabled()),
                b -> {
                    draft.toggleNpcEnabled();
                    boolean next = draft.npcEnabled();
                    b.setMessage(toggleStateLabel(next));
                    for (Consumer<Boolean> updater : sectionWidgets) {
                        updater.accept(next);
                    }
                    onDirty.run();
                })
                .bounds(topBtnX, topBtnY, 34, 14)
                .tooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.npc_enabled.tooltip")))
                .build());

        boolean blocked = placementResult != null && !placementResult.canSpawn() && !draft.npcEnabled();
        if (blocked) {
            npcToggle.setTooltip(Tooltip.create(Component.translatable(placementResult.translationKey())));
        }

        boolean enabled = draft.npcEnabled();

        // --- GroupBox 1: Erscheinungsbild (y = 23, h = 62) ---

        // Zeile 1: NPC-Name
        EditBox npcNameField = host.addSettingsWidget(
                new EditBox(host.settingsFont(), host.settingsLeftPos() + 42, host.settingsTopPos() + 30, 122, 14,
                        Component.translatable("gui.marketblocks.visuals.npc_name")));
        npcNameField.setMaxLength(32);
        npcNameField.setValue(draft.npcName());
        npcNameField.setEditable(enabled);
        npcNameField.active = enabled;
        npcNameField.setResponder(value -> {
            draft.setNpcName(value);
            onDirty.run();
        });
        if (enabled) {
            npcNameField.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.npc_name.tooltip")));
        }
        sectionWidgets.add(en -> {
            npcNameField.setEditable(en);
            npcNameField.active = en;
            npcNameField.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.npc_name.tooltip")) : null);
        });

        // Zeile 2: Beruf-Button (deaktiviert wenn Spieler-Skin aktiv)
        Button professionButton = host.addSettingsWidget(Button.builder(professionLabel(draft), b -> {
            draft.cycleProfession();
            onDirty.run();
            b.setMessage(professionLabel(draft));
        }).bounds(leftX, host.settingsTopPos() + 48, 152, 15).build());
        boolean profActive = enabled && !draft.usePlayerSkin();
        professionButton.active = profActive;
        if (profActive) {
            professionButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.profession.tooltip")));
        }

        // Zeile 3: Spieler-Skin Checkbox (links) + Skin-Namensfeld (rechts)
        // EditBox (14px) at y + 66, Checkbox (12px) at y + 67 -> perfectly vertically centered together!
        EditBox playerSkinNameField = host.addSettingsWidget(
                new EditBox(host.settingsFont(), host.settingsLeftPos() + 58, host.settingsTopPos() + 66, 106, 14,
                        Component.translatable("gui.marketblocks.visuals.player_skin_name")));
        playerSkinNameField.setMaxLength(36);
        playerSkinNameField.setValue(draft.playerSkinName());
        playerSkinNameField.setEditable(enabled && draft.usePlayerSkin());
        playerSkinNameField.active = enabled && draft.usePlayerSkin();
        playerSkinNameField.setResponder(value -> {
            draft.setPlayerSkinName(value);
            onDirty.run();
        });
        if (enabled && draft.usePlayerSkin()) {
            playerSkinNameField.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.player_skin_name.tooltip")));
        }

        CompactCheckbox usePlayerSkinCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 67,
                Component.translatable("gui.marketblocks.visuals.use_player_skin_short"),
                host.settingsFont(),
                draft.usePlayerSkin(),
                12,
                (checkbox, value) -> {
                    draft.setUsePlayerSkin(value);
                    boolean curEnabled = draft.npcEnabled();
                    boolean skinActive = curEnabled && value;
                    playerSkinNameField.setEditable(skinActive);
                    playerSkinNameField.active = skinActive;
                    playerSkinNameField.setTooltip(skinActive
                            ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.player_skin_name.tooltip"))
                            : null);
                    boolean pActive = curEnabled && !value;
                    professionButton.active = pActive;
                    professionButton.setTooltip(pActive
                            ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.profession.tooltip"))
                            : null);
                    onDirty.run();
                }).setTextOnLeft(true).setCompactText(true));
        usePlayerSkinCheckbox.setCustomWidth(42);
        usePlayerSkinCheckbox.active = enabled;
        if (enabled) {
            usePlayerSkinCheckbox
                    .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.use_player_skin.tooltip")));
        }

        sectionWidgets.add(en -> {
            usePlayerSkinCheckbox.active = en;
            usePlayerSkinCheckbox.setTooltip(en
                    ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.use_player_skin.tooltip"))
                    : null);
            boolean skinActive = en && draft.usePlayerSkin();
            playerSkinNameField.setEditable(skinActive);
            playerSkinNameField.active = skinActive;
            playerSkinNameField.setTooltip(skinActive
                    ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.player_skin_name.tooltip"))
                    : null);
            boolean pActive = en && !draft.usePlayerSkin();
            professionButton.active = pActive;
            professionButton.setTooltip(pActive
                    ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.profession.tooltip"))
                    : null);
        });

        // --- GroupBox 2: Feedback & Effekte (starts at 91, exact 6px gap to Box 1, h = 47) ---

        // Checkbox 1: Kauf-Partikel (starts closer to title line at 98)
        CompactCheckbox particlesCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 98,
                Component.translatable("gui.marketblocks.visuals.purchase_particles"),
                host.settingsFont(),
                draft.purchaseParticlesEnabled(),
                12,
                (checkbox, value) -> {
                    draft.setPurchaseParticlesEnabled(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        particlesCheckbox.active = enabled;
        if (enabled) {
            particlesCheckbox
                    .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_particles.tooltip")));
        }

        // Checkbox 2: Kauf-Sound
        CompactCheckbox purchaseSoundsCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 111,
                Component.translatable("gui.marketblocks.visuals.purchase_sounds"),
                host.settingsFont(),
                draft.purchaseSoundsEnabled(),
                12,
                (checkbox, value) -> {
                    draft.setPurchaseSoundsEnabled(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        purchaseSoundsCheckbox.active = enabled;
        if (enabled) {
            purchaseSoundsCheckbox
                    .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_sounds.tooltip")));
        }

        // Checkbox 3: Bezahl-Sound
        CompactCheckbox paymentSoundsCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 124,
                Component.translatable("gui.marketblocks.visuals.payment_sounds"),
                host.settingsFont(),
                draft.paymentSlotSoundsEnabled(),
                12,
                (checkbox, value) -> {
                    draft.setPaymentSlotSoundsEnabled(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        paymentSoundsCheckbox.active = enabled;
        if (enabled) {
            paymentSoundsCheckbox
                    .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.payment_sounds.tooltip")));
        }

        sectionWidgets.add(en -> {
            particlesCheckbox.active = en;
            particlesCheckbox.setTooltip(en
                    ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_particles.tooltip"))
                    : null);
            purchaseSoundsCheckbox.active = en;
            purchaseSoundsCheckbox.setTooltip(en
                    ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_sounds.tooltip"))
                    : null);
            paymentSoundsCheckbox.active = en;
            paymentSoundsCheckbox.setTooltip(en
                    ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.payment_sounds.tooltip"))
                    : null);
        });

        return new VillagerSectionWidgets(npcNameField, playerSkinNameField, professionButton);
    }

    public static void renderVillagerBg(GuiGraphics graphics, Font font, int leftPos, int topPos, boolean enabled) {
        int labelColor = enabled ? 0x404040 : 0x808080;
        int groupTitleColor = enabled ? GroupBox.DEFAULT_TITLE_COLOR : 0x808080;
        int groupBorderColor = enabled ? GroupBox.DEFAULT_BORDER_COLOR : 0xFF888888;

        Component npcLabel = GuiConstants.compact(Component.translatable("gui.marketblocks.visuals.npc_short"));
        graphics.drawString(font, npcLabel, leftPos + 130 - font.width(npcLabel), topPos + 8, 0x404040, false);

        // GroupBox 1: Erscheinungsbild (y = 23, h = 62, ends at 85)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.visuals.group.npc_appearance"),
                leftPos + 7, topPos + 23, 162, 62, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);

        Component nameLabel = GuiConstants.compact(Component.translatable("gui.marketblocks.visuals.npc_name_label"));
        graphics.drawString(font, nameLabel, leftPos + 12, topPos + 33, labelColor, false);

        // GroupBox 2: Feedback & Effekte (starts at 91, exact 6px gap, h = 47, ends at 138)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.visuals.group.npc_feedback"),
                leftPos + 7, topPos + 91, 162, 47, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);
    }

    public record VillagerSectionWidgets(EditBox npcNameField, EditBox playerSkinNameField, Button professionButton) {
    }

    /**
     * Renders background group boxes and static labels for the Visuals tab.
     */
    public static void renderVisualsBg(GuiGraphics graphics, Font font, ShopVisualType visualType, int leftPos,
            int topPos, boolean enabled) {
        int labelColor = enabled ? 0x404040 : 0x808080;
        int groupTitleColor = enabled ? GroupBox.DEFAULT_TITLE_COLOR : 0x808080;
        int groupBorderColor = enabled ? GroupBox.DEFAULT_BORDER_COLOR : 0xFF888888;

        Component displayLabel = GuiConstants.compact(Component.translatable("gui.marketblocks.visuals.display"));
        graphics.drawString(font, displayLabel, leftPos + 130 - font.width(displayLabel), topPos + 8, 0x404040, false);

        switch (visualType) {
            case MARKET_CRATE -> {
                GroupBox.render(graphics, font,
                        Component.translatable("gui.marketblocks.visuals.group.item_arrangement"),
                        leftPos + 7, topPos + 23, 162, 44, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);

                Component countLabel = GuiConstants
                        .compact(Component.translatable("gui.marketblocks.visuals.count_short"));
                graphics.drawString(font, countLabel, leftPos + 12, topPos + 51, labelColor, false);

                GroupBox.render(graphics, font,
                        Component.translatable("gui.marketblocks.visuals.group.visuals_transformations"),
                        leftPos + 7, topPos + 73, 162, 66, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);
            }
            case TRADE_STAND -> {
                GroupBox.render(graphics, font,
                        Component.translatable("gui.marketblocks.visuals.group.visuals_transformations"),
                        leftPos + 7, topPos + 23, 162, 74, groupBorderColor, groupTitleColor, GroupBox.DEFAULT_BG_COLOR);
            }
            case UNKNOWN -> {
            }
        }
    }

    public static void buildOfferItemSection(
            SingleOfferShopScreen host,
            ShopVisualType visualType,
            OfferItemSettings.Draft draft,
            Runnable onDirty,
            Runnable onRebuild) {
        int leftX = host.settingsLeftPos() + 12;

        int topBtnX = host.settingsLeftPos() + 134;
        int topBtnY = host.settingsTopPos() + 5;
        List<Consumer<Boolean>> sectionWidgets = new ArrayList<>();

        host.addSettingsWidget(Button.builder(
                toggleStateLabel(draft.visible()),
                b -> {
                    boolean next = !draft.visible();
                    draft.setVisible(next);
                    b.setMessage(toggleStateLabel(next));
                    for (Consumer<Boolean> updater : sectionWidgets) {
                        updater.accept(next);
                    }
                    onDirty.run();
                })
                .bounds(topBtnX, topBtnY, 34, 14)
                .tooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_visible.tooltip")))
                .build());

        boolean enabled = draft.visible();

        switch (visualType) {
            case MARKET_CRATE -> {
                CrateLayoutMode currentMode = draft.layoutMode();
                Button layoutModeButton = host.addSettingsWidget(Button.builder(
                        Component.translatable("gui.marketblocks.visuals.layout_mode").append(": ")
                                .append(Component.translatable(currentMode.translationKey())),
                        b -> {
                            CrateLayoutMode nextMode = draft.layoutMode().next();
                            draft.setLayoutMode(nextMode);
                            onDirty.run();
                            onRebuild.run();
                        }).bounds(leftX, host.settingsTopPos() + 29, 152, 14).build());
                layoutModeButton.active = enabled;
                if (enabled) {
                    layoutModeButton
                            .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.layout_mode.tooltip")));
                }
                sectionWidgets.add(en -> {
                    layoutModeButton.active = en;
                    layoutModeButton.setTooltip(en
                            ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.layout_mode.tooltip"))
                            : null);
                });

                Component countShort = GuiConstants.compact(Component.translatable("gui.marketblocks.visuals.count_short"));
                int labelWidth = host.settingsFont().width(countShort);
                int countBoxX = leftX + labelWidth + 3;
                int countBoxWidth = 18;
                int boxHeight = 16;

                CompactNumberBox countBox = host.addSettingsWidget(new CompactNumberBox(
                        host.settingsFont(),
                        countBoxX,
                        host.settingsTopPos() + 47,
                        countBoxWidth,
                        boxHeight,
                        Component.translatable("gui.marketblocks.visuals.count")));
                countBox.setMaxLength(2);
                countBox.setValue(String.valueOf(draft.count()));
                countBox.setEditable(enabled);
                countBox.active = enabled;
                if (enabled) {
                    countBox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.count.tooltip")));
                }
                countBox.setFilter(val -> val.isEmpty()
                        || (val.matches("\\d{1,2}") && Integer.parseInt(val) <= OfferItemSettings.MAX_COUNT));
                countBox.setResponder(val -> {
                    if (!val.isEmpty()) {
                        try {
                            int c = Math.clamp(Integer.parseInt(val), 1, OfferItemSettings.MAX_COUNT);
                            draft.setCount(c);
                            onDirty.run();
                        } catch (NumberFormatException ignored) {
                        }
                    }
                });

                int arrowX = countBoxX + countBoxWidth;
                int arrowWidth = 12;
                MiniArrowButton arrowBtn = host.addSettingsWidget(new MiniArrowButton(arrowX, host.settingsTopPos() + 47, arrowWidth, boxHeight,
                        () -> {
                            int next = Math.min(OfferItemSettings.MAX_COUNT, draft.count() + 1);
                            draft.setCount(next);
                            countBox.setValue(String.valueOf(next));
                            onDirty.run();
                        },
                        () -> {
                            int next = Math.max(1, draft.count() - 1);
                            draft.setCount(next);
                            countBox.setValue(String.valueOf(next));
                            onDirty.run();
                        }));
                arrowBtn.active = enabled;
                if (enabled) {
                    arrowBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.count.tooltip")));
                }

                sectionWidgets.add(en -> {
                    countBox.setEditable(en);
                    countBox.active = en;
                    arrowBtn.active = en;
                    countBox.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.count.tooltip")) : null);
                    arrowBtn.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.count.tooltip")) : null);
                });

                CompactCheckbox dynamicFillCheckbox = new CompactCheckbox(
                        0,
                        host.settingsTopPos() + 49,
                        Component.translatable("gui.marketblocks.visuals.dynamic_fill_level"),
                        host.settingsFont(),
                        draft.dynamicFillLevel(),
                        12,
                        (checkbox, value) -> {
                            draft.setDynamicFillLevel(value);
                            onDirty.run();
                        }).setTextOnLeft(true).setCompactText(true);
                int dfX = host.settingsLeftPos() + 164 - dynamicFillCheckbox.getWidth();
                dynamicFillCheckbox.setX(dfX);
                dynamicFillCheckbox.active = enabled;
                if (enabled) {
                    dynamicFillCheckbox.setTooltip(
                            Tooltip.create(Component.translatable("gui.marketblocks.visuals.dynamic_fill_level.tooltip")));
                }
                host.addSettingsWidget(dynamicFillCheckbox);

                sectionWidgets.add(en -> {
                    dynamicFillCheckbox.active = en;
                    dynamicFillCheckbox.setTooltip(en
                            ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.dynamic_fill_level.tooltip"))
                            : null);
                });

                CompactCheckbox fullbrightCheckbox = host.addSettingsWidget(new CompactCheckbox(
                        leftX,
                        host.settingsTopPos() + 80,
                        Component.translatable("gui.marketblocks.visuals.offer_item_fullbright"),
                        host.settingsFont(),
                        draft.fullbright(),
                        12,
                        (checkbox, value) -> {
                            draft.setFullbright(value);
                            onDirty.run();
                        }).setTextOnLeft(true).setCompactText(true));
                fullbrightCheckbox.active = enabled;
                if (enabled) {
                    fullbrightCheckbox.setTooltip(Tooltip
                            .create(Component.translatable("gui.marketblocks.visuals.offer_item_fullbright.tooltip")));
                }

                sectionWidgets.add(en -> {
                    fullbrightCheckbox.active = en;
                    fullbrightCheckbox.setTooltip(en
                            ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_fullbright.tooltip"))
                            : null);
                });

                CustomSlider scaleSlider = host.addSettingsWidget(new FloatSlider(host.settingsLeftPos() + 86, host.settingsTopPos() + 78, 78, 17,
                        Component.translatable("gui.marketblocks.visuals.scale"), 0.5f, 1.5f, draft.scale(), value -> {
                            draft.setScale(value);
                            onDirty.run();
                        }).setStringFormatter(v -> String.format(Locale.US, "%.2f", v)));
                scaleSlider.active = enabled;
                if (enabled) {
                    scaleSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.scale.tooltip")));
                }

                CustomSlider rotationSlider = host.addSettingsWidget(new FloatSlider(leftX, host.settingsTopPos() + 99, 72, 17,
                        Component.translatable("gui.marketblocks.visuals.rotation"), 0.0f, 360.0f, draft.rotation(),
                        value -> {
                            draft.setRotation(value);
                            onDirty.run();
                        }).setStringFormatter(v -> String.format(Locale.US, "%.0f°", v)));
                rotationSlider.active = enabled;
                if (enabled) {
                    rotationSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.rotation.tooltip")));
                }

                CustomSlider spacingYSlider = host.addSettingsWidget(new FloatSlider(host.settingsLeftPos() + 86, host.settingsTopPos() + 99, 78, 17,
                        Component.translatable("gui.marketblocks.visuals.spacing_y"), 0.0f, 2.0f, draft.spacingY(),
                        value -> {
                            draft.setSpacingY(value);
                            onDirty.run();
                        }).setStringFormatter(v -> String.format(Locale.US, "%.2f", v)));
                spacingYSlider.active = enabled;
                if (enabled) {
                    spacingYSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.spacing_y.tooltip")));
                }

                CustomSlider modeSlider;
                if (currentMode == CrateLayoutMode.STACKED) {
                    modeSlider = host.addSettingsWidget(new FloatSlider(leftX, host.settingsTopPos() + 120, 152, 17,
                            Component.translatable("gui.marketblocks.visuals.spacing_xz"), -0.25f, 0.25f,
                            draft.spacingXZ(), value -> {
                                draft.setSpacingXZ(value);
                                onDirty.run();
                            }).setStringFormatter(v -> String.format(Locale.US, "%+.2f", v)));
                    if (enabled) {
                        modeSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.spacing_xz.tooltip")));
                    }
                } else {
                    modeSlider = host.addSettingsWidget(new FloatSlider(leftX, host.settingsTopPos() + 120, 152, 17,
                            Component.translatable("gui.marketblocks.visuals.chaos_rotation"), 0.0f, 1.0f,
                            draft.chaosRotation(), value -> {
                                draft.setChaosRotation(value);
                                onDirty.run();
                            }).setStringFormatter(v -> String.format(Locale.US, "%.0f%%", v * 100.0f)));
                    if (enabled) {
                        modeSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.chaos_rotation.tooltip")));
                    }
                }
                modeSlider.active = enabled;

                sectionWidgets.add(en -> {
                    scaleSlider.active = en;
                    rotationSlider.active = en;
                    spacingYSlider.active = en;
                    modeSlider.active = en;
                    scaleSlider.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.scale.tooltip")) : null);
                    rotationSlider.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.rotation.tooltip")) : null);
                    spacingYSlider.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.spacing_y.tooltip")) : null);
                    modeSlider.setTooltip(en ? Tooltip.create(Component.translatable(currentMode == CrateLayoutMode.STACKED ? "gui.marketblocks.visuals.spacing_xz.tooltip" : "gui.marketblocks.visuals.chaos_rotation.tooltip")) : null);
                });
            }
            case TRADE_STAND -> {
                CompactCheckbox fullbrightCheckbox = host.addSettingsWidget(new CompactCheckbox(
                        leftX,
                        host.settingsTopPos() + 30,
                        Component.translatable("gui.marketblocks.visuals.offer_item_fullbright"),
                        host.settingsFont(),
                        draft.fullbright(),
                        12,
                        (checkbox, value) -> {
                            draft.setFullbright(value);
                            onDirty.run();
                        }).setTextOnLeft(true).setCompactText(true));
                fullbrightCheckbox.active = enabled;
                if (enabled) {
                    fullbrightCheckbox.setTooltip(Tooltip
                            .create(Component.translatable("gui.marketblocks.visuals.offer_item_fullbright.tooltip")));
                }

                CompactCheckbox bobbingCheckbox = host.addSettingsWidget(new CompactCheckbox(
                        host.settingsLeftPos() + 86,
                        host.settingsTopPos() + 30,
                        Component.translatable("gui.marketblocks.visuals.bobbing"),
                        host.settingsFont(),
                        draft.bobbing(),
                        12,
                        (checkbox, value) -> {
                            draft.setBobbing(value);
                            onDirty.run();
                        }).setTextOnLeft(true).setCompactText(true));
                bobbingCheckbox.active = enabled;
                if (enabled) {
                    bobbingCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.bobbing.tooltip")));
                }

                CustomSlider scaleSlider = host.addSettingsWidget(new FloatSlider(leftX, host.settingsTopPos() + 50, 72, 17,
                        Component.translatable("gui.marketblocks.visuals.scale"), 0.5f, 1.5f, draft.scale(), value -> {
                            draft.setScale(value);
                            onDirty.run();
                        }).setStringFormatter(v -> String.format(Locale.US, "%.2f", v)));
                scaleSlider.active = enabled;
                if (enabled) {
                    scaleSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.scale.tooltip")));
                }

                CustomSlider speedSlider = host.addSettingsWidget(new FloatSlider(host.settingsLeftPos() + 86, host.settingsTopPos() + 50, 78, 17,
                        Component.translatable("gui.marketblocks.visuals.speed"), 0.0f, 1.5f, draft.speed(), value -> {
                            draft.setSpeed(value);
                            onDirty.run();
                        }).setStringFormatter(v -> String.format(Locale.US, "%.2f", v)));
                speedSlider.active = enabled;
                if (enabled) {
                    speedSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.speed.tooltip")));
                }

                CustomSlider heightSlider = host.addSettingsWidget(new FloatSlider(leftX, host.settingsTopPos() + 71, 152, 17,
                        Component.translatable("gui.marketblocks.visuals.height"), -0.25f, 0.25f, draft.heightOffset(),
                        value -> {
                            draft.setHeightOffset(value);
                            onDirty.run();
                        }).setStringFormatter(v -> String.format(Locale.US, "%+.2f", v)));
                heightSlider.active = enabled;
                if (enabled) {
                    heightSlider.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.height.tooltip")));
                }

                sectionWidgets.add(en -> {
                    fullbrightCheckbox.active = en;
                    fullbrightCheckbox.setTooltip(en
                            ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_fullbright.tooltip"))
                            : null);
                    bobbingCheckbox.active = en;
                    bobbingCheckbox.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.bobbing.tooltip")) : null);
                    scaleSlider.active = en;
                    scaleSlider.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.scale.tooltip")) : null);
                    speedSlider.active = en;
                    speedSlider.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.speed.tooltip")) : null);
                    heightSlider.active = en;
                    heightSlider.setTooltip(en ? Tooltip.create(Component.translatable("gui.marketblocks.visuals.height.tooltip")) : null);
                });
            }
            case UNKNOWN -> {
            }
        }
    }

    private static Component toggleStateLabel(boolean enabled) {
        return Component.translatable(enabled ? "gui.marketblocks.toggle.on" : "gui.marketblocks.toggle.off");
    }

    private static Component professionLabel(VillagerSettings.Draft draft) {
        return Component.translatable(draft.profession().translationKey());
    }

    public static void buildNotificationSection(SingleOfferShopScreen host,
            NotificationSettings.Draft draft, Runnable onDirty) {
        int leftX = host.settingsLeftPos() + 12;

        // --- GroupBox 1: HANDELSAKTIVITÄT (y = 23, h = 40, ends at 63) ---
        // Row 1: Kaufbenachrichtigung (7px top offset: 23 + 7 = 30)
        CompactCheckbox purchaseCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 30,
                Component.translatable("gui.marketblocks.notifications.purchase"),
                host.settingsFont(),
                draft.notifyOnPurchase(),
                12,
                (checkbox, value) -> {
                    draft.setNotifyOnPurchase(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        purchaseCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.notifications.purchase.tooltip")));

        // Row 2: Mitbesitzer informieren (y = 45, ends at 57, 6px bottom gap to 63)
        CompactCheckbox coOwnersCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 45,
                Component.translatable("gui.marketblocks.notifications.co_owners"),
                host.settingsFont(),
                draft.notifyCoOwners(),
                12,
                (checkbox, value) -> {
                    draft.setNotifyCoOwners(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        coOwnersCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.notifications.co_owners.tooltip")));

        // --- GroupBox 2: STATUS-WARNUNGEN (starts at 69, exact 6px gap to Box 1, h = 40, ends at 109) ---
        // Row 1: Leer-Warnung (7px top offset: 69 + 7 = 76)
        CompactCheckbox outOfStockCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 76,
                Component.translatable("gui.marketblocks.notifications.out_of_stock"),
                host.settingsFont(),
                draft.notifyOnOutOfStock(),
                12,
                (checkbox, value) -> {
                    draft.setNotifyOnOutOfStock(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        outOfStockCheckbox.setTooltip(
                Tooltip.create(Component.translatable("gui.marketblocks.notifications.out_of_stock.tooltip")));

        // Row 2: Ausgabe-Voll-Warnung (y = 91, ends at 103, 6px bottom gap to 109)
        CompactCheckbox outputFullCheckbox = host.addSettingsWidget(new CompactCheckbox(
                leftX, host.settingsTopPos() + 91,
                Component.translatable("gui.marketblocks.notifications.output_full"),
                host.settingsFont(),
                draft.notifyOnOutputFull(),
                12,
                (checkbox, value) -> {
                    draft.setNotifyOnOutputFull(value);
                    onDirty.run();
                }).setBoxOnRightEdge(true).setCustomWidth(152).setCompactText(true));
        outputFullCheckbox.setTooltip(
                Tooltip.create(Component.translatable("gui.marketblocks.notifications.output_full.tooltip")));
    }

    /**
     * Renders background group boxes for the Notifications tab.
     */
    public static void renderNotificationsBg(GuiGraphics graphics, Font font, int leftPos, int topPos) {
        // GroupBox 1: Handelsaktivität (y = 23, h = 40, ends at 63)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.notifications.group.trade_activity"),
                leftPos + 7, topPos + 23, 162, 40);

        // GroupBox 2: Status-Warnungen (starts at 69, exact 6px gap, h = 40, ends at 109)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.notifications.group.status_warnings"),
                leftPos + 7, topPos + 69, 162, 40);
    }

    public static void buildAccessSection(SingleOfferShopScreen host,
            AccessSettings.Draft accessDraft,
            SingleOfferOwnerListPanel ownerListPanel,
            boolean isPrimaryOwner,
            Runnable saveListPanelToDraft,
            Runnable rebuildUI,
            Runnable onDirty) {
        int leftPos = host.getGuiLeft();
        int topPos = host.getGuiTop();

        // Row 1: Vanilla Buttons at topPos + 30 (y = 23 + 7, h = 14)
        boolean isOwnersMode = ownerListPanel.getListMode() == SingleOfferOwnerListPanel.ListMode.OWNERS;

        Button ownersBtn = host.addSettingsWidget(Button.builder(
                SingleOfferOwnerListPanel.ListMode.OWNERS.title(),
                b -> {
                    if (!isOwnersMode) {
                        saveListPanelToDraft.run();
                        ownerListPanel.setListMode(SingleOfferOwnerListPanel.ListMode.OWNERS);
                        rebuildUI.run();
                    }
                }).bounds(leftPos + 11, topPos + 30, 76, 14).build());
        ownersBtn.active = isPrimaryOwner && !isOwnersMode;
        ownersBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.access.edit_owners.tooltip")));

        Button accessBtn = host.addSettingsWidget(Button.builder(
                SingleOfferOwnerListPanel.ListMode.ACCESS_LIST.title(),
                b -> {
                    if (isOwnersMode) {
                        saveListPanelToDraft.run();
                        ownerListPanel.setListMode(SingleOfferOwnerListPanel.ListMode.ACCESS_LIST);
                        rebuildUI.run();
                    }
                }).bounds(leftPos + 89, topPos + 30, 76, 14).build());
        accessBtn.active = isPrimaryOwner && isOwnersMode;
        accessBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.access.edit_access_list.tooltip")));

        // Row 2: In ACCESS_LIST mode, Vanilla Filter Button at topPos + 46 (h = 14)
        if (!isOwnersMode && isPrimaryOwner) {
            boolean isWhitelist = accessDraft.accessMode() == AccessMode.WHITELIST;
            Component filterTitle = Component.translatable(isWhitelist
                    ? "gui.marketblocks.access.filter_whitelist"
                    : "gui.marketblocks.access.filter_blacklist");
            Button filterBtn = host.addSettingsWidget(Button.builder(
                    filterTitle,
                    b -> {
                        saveListPanelToDraft.run();
                        accessDraft.setAccessMode(accessDraft.accessMode().next());
                        onDirty.run();
                        rebuildUI.run();
                    }).bounds(leftPos + 11, topPos + 46, 154, 14).build());
            filterBtn.active = isPrimaryOwner;
            filterBtn.setTooltip(Tooltip.create(Component.translatable(isWhitelist
                    ? "gui.marketblocks.access.filter_whitelist.tooltip"
                    : "gui.marketblocks.access.filter_blacklist.tooltip")));
        }

        ownerListPanel.prepareAndRender(host, accessDraft, topPos + 77, isPrimaryOwner, onDirty);
    }

    /**
     * Renders background group boxes and status counter for the Access tab.
     */
    public static void renderAccessBg(GuiGraphics graphics, Font font, int leftPos, int topPos,
            SingleOfferOwnerListPanel.ListMode listMode, AccessMode accessMode, int selectedOwnersCount, int maxOwners) {
        // GroupBox 1: VERWALTUNG (y = 23, h = 40, ends at 63)
        GroupBox.render(graphics, font,
                Component.translatable("gui.marketblocks.access.group.management"),
                leftPos + 7, topPos + 23, 162, 40);

        // In OWNERS mode: Row 2 counter badge (y = 46, h = 14)
        if (listMode == SingleOfferOwnerListPanel.ListMode.OWNERS) {
            int badgeX = leftPos + 11;
            int badgeY = topPos + 46;
            int badgeW = 154;
            int badgeH = 14;

            graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 0xFF222222);
            graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 1, 0xFF373737);
            graphics.fill(badgeX, badgeY + badgeH - 1, badgeX + badgeW, badgeY + badgeH, 0xFF373737);
            graphics.fill(badgeX, badgeY, badgeX + 1, badgeY + badgeH, 0xFF373737);
            graphics.fill(badgeX + badgeW - 1, badgeY, badgeX + badgeW, badgeY + badgeH, 0xFF373737);

            Component counter = Component.translatable("gui.marketblocks.access.counter", selectedOwnersCount, maxOwners);
            int tw = font.width(counter);
            int textX = badgeX + (badgeW - tw) / 2;
            int textY = badgeY + (badgeH - font.lineHeight) / 2 + 1;
            int textColor = selectedOwnersCount >= maxOwners ? 0xFFAA00 : 0x55FF55;
            graphics.drawString(font, counter, textX, textY, textColor, false);
        }

        // GroupBox 2: SPIELERLISTE (starts at 69, exact 6px gap, h = 69, ends at 138)
        Component box2Title;
        if (listMode == SingleOfferOwnerListPanel.ListMode.OWNERS) {
            box2Title = Component.translatable("gui.marketblocks.access.group.players");
        } else if (accessMode == AccessMode.WHITELIST) {
            box2Title = Component.translatable("gui.marketblocks.access.group.whitelist");
        } else {
            box2Title = Component.translatable("gui.marketblocks.access.group.blacklist");
        }
        GroupBox.render(graphics, font, box2Title, leftPos + 7, topPos + 69, 162, 69);
    }
}
