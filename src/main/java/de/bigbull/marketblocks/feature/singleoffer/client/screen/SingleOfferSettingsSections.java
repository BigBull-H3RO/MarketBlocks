package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import de.bigbull.marketblocks.client.gui.FloatSlider;
import de.bigbull.marketblocks.client.gui.IconButton;
import de.bigbull.marketblocks.client.gui.IntSlider;
import de.bigbull.marketblocks.client.gui.SideModeButton;
import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopVisualType;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.visual.npc.ShopVisualSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcPlacementResult;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Builds the category-dependent settings widgets for the single-offer shop screen.
 */
public final class SingleOfferSettingsSections {
    private static final int SETTINGS_CATEGORY_BUTTON_Y_OFFSET = -24;
    private static final int SETTINGS_CATEGORY_BUTTON_X_OFFSET = 4;
    private static final int SETTINGS_CATEGORY_BUTTON_WIDTH = 22;
    private static final int SETTINGS_CATEGORY_BUTTON_HEIGHT = 22;
    private static final int SETTINGS_CATEGORY_BUTTON_GAP = 4;

    private SingleOfferSettingsSections() {
    }

    public static void buildCategoryButtons(SingleOfferShopScreen host, SettingsCategory activeCategory, Consumer<SettingsCategory> onSwitch) {
        int x = host.settingsLeftPos() + SETTINGS_CATEGORY_BUTTON_X_OFFSET;
        int y = host.settingsTopPos() + SETTINGS_CATEGORY_BUTTON_Y_OFFSET;
        for (SettingsCategory category : SettingsCategory.values()) {
            host.addSettingsWidget(new IconButton(
                    x,
                    y,
                    SETTINGS_CATEGORY_BUTTON_WIDTH,
                    SETTINGS_CATEGORY_BUTTON_HEIGHT,
                    AbstractSingleOfferShopScreen.BUTTON_SPRITES,
                    category.icon(),
                    b -> {
                        if (category != activeCategory) {
                            onSwitch.accept(category);
                        }
                    },
                    category.title(),
                    () -> category == activeCategory
            ));
            x += SETTINGS_CATEGORY_BUTTON_WIDTH + SETTINGS_CATEGORY_BUTTON_GAP;
        }
    }

    public static EditBox buildGeneralSection(SingleOfferShopScreen host,
                                              ShopVisualSettings.Draft draft,
                                              Runnable onDirty) {
        EditBox nameField = host.addSettingsWidget(new EditBox(host.settingsFont(), host.settingsLeftPos() + 8, host.settingsTopPos() + 28, 120, 18,
                Component.translatable("gui.marketblocks.shop_name")));
        nameField.setMaxLength(32);
        nameField.setValue(draft.shopName());
        nameField.setResponder(value -> {
            draft.setShopName(value);
            onDirty.run();
        });

        Checkbox emitCheckbox = host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.emit_redstone"),
                        host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 50)
                .selected(draft.emitRedstoneEnabled())
                .onValueChange((checkbox, value) -> {
                    draft.setEmitRedstoneEnabled(value);
                    onDirty.run();
                })
                .build());
        emitCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")));

        Checkbox xpSoundCheckbox = host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.purchase_xp_sound"),
                        host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 70)
                .selected(draft.purchaseXpFeedbackSound())
                .onValueChange((checkbox, value) -> {
                    draft.setPurchaseXpFeedbackSound(value);
                    onDirty.run();
                })
                .build());
        xpSoundCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.purchase_xp_sound.tooltip")));

        return nameField;
    }

    public static void buildIoSection(SingleOfferShopScreen host,
                                      SingleOfferShopMenu menu,
                                      Direction leftDir,
                                      Direction rightDir,
                                      Direction bottomDir,
                                      Direction backDir,
                                      Runnable onDirty) {
        int sideCenterX = host.settingsLeftPos() + 28;
        int sideBaseY = host.settingsTopPos() + 65;

        SideModeButton leftButton = host.addSettingsWidget(new SideModeButton(sideCenterX - 20, sideBaseY, 16, 16, menu.getMode(leftDir), m -> {
            menu.setMode(leftDir, m);
            onDirty.run();
        }));
        leftButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.left")));
        leftButton.setMessage(Component.translatable("gui.marketblocks.side.left"));

        SideModeButton bottomButton = host.addSettingsWidget(new SideModeButton(sideCenterX, sideBaseY, 16, 16, menu.getMode(bottomDir), m -> {
            menu.setMode(bottomDir, m);
            onDirty.run();
        }));
        bottomButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.bottom")));
        bottomButton.setMessage(Component.translatable("gui.marketblocks.side.bottom"));

        SideModeButton rightButton = host.addSettingsWidget(new SideModeButton(sideCenterX + 20, sideBaseY, 16, 16, menu.getMode(rightDir), m -> {
            menu.setMode(rightDir, m);
            onDirty.run();
        }));
        rightButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.right")));
        rightButton.setMessage(Component.translatable("gui.marketblocks.side.right"));

        SideModeButton backButton = host.addSettingsWidget(new SideModeButton(sideCenterX, sideBaseY + 20, 16, 16, menu.getMode(backDir), m -> {
            menu.setMode(backDir, m);
            onDirty.run();
        }));
        backButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.back")));
        backButton.setMessage(Component.translatable("gui.marketblocks.side.back"));
    }

    public static VisualSectionWidgets buildVisualSection(
            SingleOfferShopScreen host,
            ShopVisualSettings.Draft draft,
            VisualNpcPlacementResult placementResult,
            Runnable onDirty
    ) {
        Button npcToggle = host.addSettingsWidget(Button.builder(npcToggleLabel(draft), b -> {
            draft.toggleNpcEnabled();
            onDirty.run();
            b.setMessage(npcToggleLabel(draft));
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 26, 32, 16).build());
        npcToggle.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.npc_enabled")));

        EditBox npcNameField = host.addSettingsWidget(new EditBox(host.settingsFont(), host.settingsLeftPos() + 46, host.settingsTopPos() + 26, 120, 16,
                Component.translatable("gui.marketblocks.visuals.npc_name")));
        npcNameField.setMaxLength(32);
        npcNameField.setValue(draft.npcName());
        npcNameField.setResponder(value -> {
            draft.setNpcName(value);
            onDirty.run();
        });

        Button professionButton = host.addSettingsWidget(Button.builder(professionLabel(draft), b -> {
            draft.cycleProfession();
            onDirty.run();
            b.setMessage(professionLabel(draft));
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 48, 158, 16).build());
        professionButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.profession")));

        Checkbox particlesCheckbox = host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.visuals.purchase_particles"),
                        host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 68)
                .selected(draft.purchaseParticlesEnabled())
                .onValueChange((checkbox, value) -> {
                    draft.setPurchaseParticlesEnabled(value);
                    onDirty.run();
                })
                .build());
        particlesCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_particles")));

        Checkbox soundsCheckbox = host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.visuals.purchase_sounds"),
                        host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 88)
                .selected(draft.purchaseSoundsEnabled())
                .onValueChange((checkbox, value) -> {
                    draft.setPurchaseSoundsEnabled(value);
                    onDirty.run();
                })
                .build());
        soundsCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_sounds")));

        Checkbox paymentSoundsCheckbox = host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.visuals.payment_sounds"),
                        host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 108)
                .selected(draft.paymentSlotSoundsEnabled())
                .onValueChange((checkbox, value) -> {
                    draft.setPaymentSlotSoundsEnabled(value);
                    onDirty.run();
                })
                .build());
        paymentSoundsCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.payment_sounds")));

        boolean blocked = placementResult != null && !placementResult.canSpawn() && !draft.npcEnabled();
        if (blocked) {
            npcToggle.setTooltip(Tooltip.create(Component.translatable(placementResult.translationKey())));
        }

        return new VisualSectionWidgets(npcNameField);
    }

    public record VisualSectionWidgets(EditBox npcNameField) {
    }

    private static float mapToDisplayScale(float internal) {
        return 0.5f + (internal - 0.75f) / (3.5f - 0.75f) * 1.0f;
    }

    private static float mapToInternalScale(float display) {
        return 0.75f + (display - 0.5f) / 1.0f * (3.5f - 0.75f);
    }

    public static void buildOfferItemSection(
            SingleOfferShopScreen host,
            ShopVisualType visualType,
            ShopVisualSettings.Draft draft,
            boolean offerItemRenderingGloballyEnabled,
            Runnable onDirty,
            Runnable onRebuild
    ) {
        int y = host.settingsTopPos() + 26;
        int leftX = host.settingsLeftPos() + 8;

        Button visibleButton = host.addSettingsWidget(Button.builder(
                        Component.literal(draft.offerItemVisible() ? "ON" : "OFF"),
                        b -> {
                            boolean next = !draft.offerItemVisible();
                            draft.setOfferItemVisible(next);
                            onDirty.run();
                            b.setMessage(Component.literal(next ? "ON" : "OFF"));
                        })
                .bounds(leftX, y, 35, 16)
                .build());

        if (!offerItemRenderingGloballyEnabled) {
            visibleButton.active = false;
            visibleButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_disabled_global")));
        } else {
            visibleButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_visible.tooltip")));
        }

        Checkbox fullbrightCheckbox = host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.visuals.offer_item_fullbright"),
                        host.settingsFont())
                .pos(leftX + 40, y)
                .selected(draft.offerItemFullbright())
                .onValueChange((checkbox, value) -> {
                    draft.setOfferItemFullbright(value);
                    onDirty.run();
                })
                .build());
        fullbrightCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_fullbright.tooltip")));
        y += 20;

        switch (visualType) {
            case TRADE_STAND -> {
                host.addSettingsWidget(new FloatSlider(leftX, y, 80, 16, Component.translatable("gui.marketblocks.visuals.scale"), 0.5f, 2.0f, draft.offerItemScale(), value -> {
                    draft.setOfferItemScale(value);
                    onDirty.run();
                }));
                host.addSettingsWidget(new FloatSlider(leftX + 85, y, 80, 16, Component.translatable("gui.marketblocks.visuals.speed"), 0.0f, 10.0f, draft.offerItemSpeed(), value -> {
                    draft.setOfferItemSpeed(value);
                    onDirty.run();
                }));
                y += 20;

                host.addSettingsWidget(new FloatSlider(leftX, y, 80, 16, Component.translatable("gui.marketblocks.visuals.height"), -0.5f, 1.5f, draft.offerItemHeightOffset(), value -> {
                    draft.setOfferItemHeightOffset(value);
                    onDirty.run();
                }));
                host.addSettingsWidget(Checkbox.builder(
                            Component.translatable("gui.marketblocks.visuals.bobbing"),
                            host.settingsFont())
                    .pos(leftX + 85, y)
                    .selected(draft.offerItemBobbing())
                    .onValueChange((checkbox, value) -> {
                        draft.setOfferItemBobbing(value);
                        onDirty.run();
                    })
                    .build());
            }
            case MARKET_CRATE -> {
                // Zeile 1: Count Slider + Dynamic Fill Checkbox
                IntSlider countSlider = host.addSettingsWidget(new IntSlider(leftX, y, 80, 16, Component.translatable("gui.marketblocks.visuals.count"), 1, ShopVisualSettings.MAX_OFFER_ITEM_COUNT, draft.offerItemCount(), value -> {
                    draft.setOfferItemCount(value);
                    onDirty.run();
                }));

                host.addSettingsWidget(Checkbox.builder(
                            Component.translatable("gui.marketblocks.visuals.dynamic_fill_level"),
                            host.settingsFont())
                    .pos(leftX + 85, y)
                    .selected(draft.dynamicFillLevel())
                    .onValueChange((checkbox, value) -> {
                        draft.setDynamicFillLevel(value);
                        onDirty.run();
                    })
                    .build());
                y += 20;

                // Zeile 2: Layout Mode Button
                CrateLayoutMode currentMode = draft.offerItemLayoutMode();
                Button layoutModeButton = host.addSettingsWidget(Button.builder(
                        Component.translatable(currentMode.translationKey()),
                        b -> {
                            CrateLayoutMode nextMode = draft.offerItemLayoutMode().next();
                            draft.setOfferItemLayoutMode(nextMode);
                            onDirty.run();
                            onRebuild.run();
                        }
                ).bounds(leftX, y, 158, 16).build());
                layoutModeButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.layout_mode")));
                y += 20;

                // Zeile 3: Base Rotation & Scale nebeneinander
                host.addSettingsWidget(new FloatSlider(leftX, y, 76, 16, Component.translatable("gui.marketblocks.visuals.rotation"), 0.0f, 360.0f, draft.offerItemRotation(), value -> {
                    draft.setOfferItemRotation(value);
                    onDirty.run();
                }));
                host.addSettingsWidget(new FloatSlider(leftX + 82, y, 76, 16, Component.translatable("gui.marketblocks.visuals.scale"), 0.5f, 1.5f, mapToDisplayScale(draft.offerItemScale()), value -> {
                    draft.setOfferItemScale(mapToInternalScale(value));
                    onDirty.run();
                }));
                y += 20;

                // Zeile 5 (DYNAMISCH): spacing OR chaos rotation depending on mode
                if (currentMode == CrateLayoutMode.GESTAPELT) {
                    // Zwei Slider nebeneinander (halbe Breite) für XZ und Y Spacing
                    host.addSettingsWidget(new FloatSlider(leftX, y, 76, 16, Component.translatable("gui.marketblocks.visuals.spacing_xz"), -0.5f, 0.5f, draft.offerItemSpacingXZ(), value -> {
                        draft.setOfferItemSpacingXZ(value);
                        onDirty.run();
                    }));
                    host.addSettingsWidget(new FloatSlider(leftX + 82, y, 76, 16, Component.translatable("gui.marketblocks.visuals.spacing_y"), 0.0f, 2.0f, draft.offerItemSpacingY(), value -> {
                        draft.setOfferItemSpacingY(value);
                        onDirty.run();
                    }));
                } else {
                    host.addSettingsWidget(new FloatSlider(leftX, y, 76, 16, Component.translatable("gui.marketblocks.visuals.chaos_rotation"), 0.0f, 1.0f, draft.offerItemChaosRotation(), value -> {
                        draft.setOfferItemChaosRotation(value);
                        onDirty.run();
                    }));
                    host.addSettingsWidget(new FloatSlider(leftX + 82, y, 76, 16, Component.translatable("gui.marketblocks.visuals.spacing_y"), 0.0f, 2.0f, draft.offerItemSpacingY(), value -> {
                        draft.setOfferItemSpacingY(value);
                        onDirty.run();
                    }));
                }
            }
            case UNKNOWN -> {
                // Keine visuellen Optionen für unbekannte Shop-Typen
            }
        }
    }

    private static Component npcToggleLabel(ShopVisualSettings.Draft draft) {
        return Component.literal(draft.npcEnabled() ? "ON" : "OFF");
    }

    private static Component professionLabel(ShopVisualSettings.Draft draft) {
        return Component.translatable("gui.marketblocks.visuals.profession").append(": ")
                .append(Component.translatable(draft.profession().translationKey()));
    }
}