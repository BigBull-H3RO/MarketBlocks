package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import de.bigbull.marketblocks.client.gui.FloatSlider;
import de.bigbull.marketblocks.client.gui.IconButton;
import de.bigbull.marketblocks.client.gui.IntSlider;
import de.bigbull.marketblocks.client.gui.SideModeButton;
import de.bigbull.marketblocks.feature.singleoffer.block.CrateLayoutMode;
import de.bigbull.marketblocks.feature.singleoffer.block.ShopVisualType;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.GeneralSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.OfferItemSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.VillagerSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.IoSettings;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.visual.npc.VisualNpcPlacementResult;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Builds the category-dependent settings widgets for the single-offer shop
 * screen.
 */
public final class SingleOfferSettingsSections {
    private static final int SETTINGS_CATEGORY_BUTTON_Y_OFFSET = -24;
    private static final int SETTINGS_CATEGORY_BUTTON_X_OFFSET = 4;
    private static final int SETTINGS_CATEGORY_BUTTON_WIDTH = 22;
    private static final int SETTINGS_CATEGORY_BUTTON_HEIGHT = 22;
    private static final int SETTINGS_CATEGORY_BUTTON_GAP = 4;

    private SingleOfferSettingsSections() {
    }

    public static void buildCategoryButtons(SingleOfferShopScreen host, SettingsCategory activeCategory,
            Consumer<SettingsCategory> onSwitch) {
        int x = host.settingsLeftPos() + SETTINGS_CATEGORY_BUTTON_X_OFFSET;
        int y = host.settingsTopPos() + SETTINGS_CATEGORY_BUTTON_Y_OFFSET;
        for (SettingsCategory category : SettingsCategory.values()) {
            if (!category.isEnabled())
                continue;
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
                    () -> category == activeCategory));
            x += SETTINGS_CATEGORY_BUTTON_WIDTH + SETTINGS_CATEGORY_BUTTON_GAP;
        }
    }

    public static EditBox buildGeneralSection(SingleOfferShopScreen host,
            GeneralSettings.Draft draft,
            Runnable onDirty) {
        EditBox nameField = host.addSettingsWidget(
                new EditBox(host.settingsFont(), host.settingsLeftPos() + 8, host.settingsTopPos() + 28, 120, 18,
                        Component.translatable("gui.marketblocks.shop_name")));
        nameField.setMaxLength(32);
        nameField.setValue(draft.shopName());
        nameField.setResponder(value -> {
            draft.setShopName(value);
            onDirty.run();
        });

        Button categoryBtn = host.addSettingsWidget(Button.builder(
                Component.translatable("gui.marketblocks.category").append(": ").append(Component.translatable("gui.marketblocks.category." + draft.shopCategory().getId())),
                b -> {
                    draft.setShopCategory(draft.shopCategory().next());
                    b.setMessage(Component.translatable("gui.marketblocks.category").append(": ").append(Component.translatable("gui.marketblocks.category." + draft.shopCategory().getId())));
                    onDirty.run();
                })
                .bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 50, 158, 16)
                .build());
        categoryBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.category.tooltip")));

        Checkbox closedCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.shop_closed"),
                host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 70)
                .selected(draft.isClosed())
                .onValueChange((checkbox, value) -> {
                    draft.setIsClosed(value);
                    onDirty.run();
                })
                .build());
        closedCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.shop_closed.tooltip")));

        Checkbox emitCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.emit_redstone"),
                host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 90)
                .selected(draft.emitRedstone())
                .onValueChange((checkbox, value) -> {
                    draft.setEmitRedstone(value);
                    onDirty.run();
                })
                .build());
        emitCheckbox.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")));

        Checkbox xpSoundCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.purchase_xp_sound"),
                host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 110)
                .selected(draft.purchaseXpFeedbackSound())
                .onValueChange((checkbox, value) -> {
                    draft.setPurchaseXpFeedbackSound(value);
                    onDirty.run();
                })
                .build());
        xpSoundCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.purchase_xp_sound.tooltip")));

        return nameField;
    }

    public static void buildIoSection(SingleOfferShopScreen host,
            IoSettings.Draft draft,
            Runnable onDirty) {
        int sideCenterX = host.settingsLeftPos() + 82;
        int sideBaseY = host.settingsTopPos() + 25;

        SideModeButton leftButton = host.addSettingsWidget(
                new SideModeButton(sideCenterX - 20, sideBaseY, 16, 16, draft.getMode(Direction.WEST), m -> {
                    draft.setMode(Direction.WEST, m);
                    onDirty.run();
                }));
        leftButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.left")));
        leftButton.setMessage(Component.translatable("gui.marketblocks.side.left"));

        SideModeButton bottomButton = host.addSettingsWidget(
                new SideModeButton(sideCenterX, sideBaseY, 16, 16, draft.getMode(Direction.DOWN), m -> {
                    draft.setMode(Direction.DOWN, m);
                    onDirty.run();
                }));
        bottomButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.bottom")));
        bottomButton.setMessage(Component.translatable("gui.marketblocks.side.bottom"));

        SideModeButton rightButton = host.addSettingsWidget(
                new SideModeButton(sideCenterX + 20, sideBaseY, 16, 16, draft.getMode(Direction.EAST), m -> {
                    draft.setMode(Direction.EAST, m);
                    onDirty.run();
                }));
        rightButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.right")));
        rightButton.setMessage(Component.translatable("gui.marketblocks.side.right"));

        SideModeButton backButton = host.addSettingsWidget(
                new SideModeButton(sideCenterX, sideBaseY + 20, 16, 16, draft.getMode(Direction.NORTH), m -> {
                    draft.setMode(Direction.NORTH, m);
                    onDirty.run();
                }));
        backButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.side.back")));
        backButton.setMessage(Component.translatable("gui.marketblocks.side.back"));

        int rightX = host.settingsLeftPos() + 8;
        int y = host.settingsTopPos() + 64;

        Button redstoneBtn = host.addSettingsWidget(Button.builder(
                Component.translatable(draft.redstoneControl().translationKey()),
                b -> {
                    draft.setRedstoneControl(draft.redstoneControl().next());
                    b.setMessage(Component.translatable(draft.redstoneControl().translationKey()));
                    onDirty.run();
                })
                .bounds(rightX, y, 158, 16)
                .build());
        redstoneBtn.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.io.redstone_control.tooltip")));
        y += 20;

        Checkbox allowIoCb = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.io.allow_io"), host.settingsFont())
                .pos(rightX, y)
                .selected(draft.allowIo())
                .onValueChange((c, v) -> {
                    draft.setAllowIo(v);
                    onDirty.run();
                })
                .build());
        allowIoCb.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.io.allow_io.tooltip")));
        y += 20;

        Checkbox autoIoCb = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.io.auto_io"), host.settingsFont())
                .pos(rightX, y)
                .selected(draft.autoIo())
                .onValueChange((c, v) -> {
                    draft.setAutoIo(v);
                    onDirty.run();
                })
                .build());
        autoIoCb.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.io.auto_io.tooltip")));
    }

    public static VillagerSectionWidgets buildVillagerSection(
            SingleOfferShopScreen host,
            VillagerSettings.Draft draft,
            VisualNpcPlacementResult placementResult,
            Runnable onDirty) {
        Button npcToggle = host.addSettingsWidget(Button.builder(npcToggleLabel(draft), b -> {
            draft.toggleNpcEnabled();
            onDirty.run();
            b.setMessage(npcToggleLabel(draft));
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 26, 32, 16).build());
        npcToggle.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.npc_enabled")));

        EditBox npcNameField = host.addSettingsWidget(
                new EditBox(host.settingsFont(), host.settingsLeftPos() + 46, host.settingsTopPos() + 26, 120, 16,
                        Component.translatable("gui.marketblocks.visuals.npc_name")));
        npcNameField.setMaxLength(32);
        npcNameField.setValue(draft.npcName());
        npcNameField.setResponder(value -> {
            draft.setNpcName(value);
            onDirty.run();
        });

        Checkbox usePlayerSkinCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.visuals.use_player_skin"),
                host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 48)
                .selected(draft.usePlayerSkin())
                .onValueChange((checkbox, value) -> {
                    draft.setUsePlayerSkin(value);
                    onDirty.run();
                })
                .build());
        usePlayerSkinCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.use_player_skin.tooltip")));

        EditBox playerSkinNameField = host.addSettingsWidget(
                new EditBox(host.settingsFont(), host.settingsLeftPos() + 88, host.settingsTopPos() + 48, 78, 16,
                        Component.translatable("gui.marketblocks.visuals.player_skin_name")));
        playerSkinNameField.setMaxLength(36);
        playerSkinNameField.setValue(draft.playerSkinName());
        playerSkinNameField.setResponder(value -> {
            draft.setPlayerSkinName(value);
            onDirty.run();
        });

        Button professionButton = host.addSettingsWidget(Button.builder(professionLabel(draft), b -> {
            draft.cycleProfession();
            onDirty.run();
            b.setMessage(professionLabel(draft));
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 68, 158, 16).build());
        professionButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.profession")));

        Checkbox particlesCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.visuals.purchase_particles"),
                host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 88)
                .selected(draft.purchaseParticlesEnabled())
                .onValueChange((checkbox, value) -> {
                    draft.setPurchaseParticlesEnabled(value);
                    onDirty.run();
                })
                .build());
        particlesCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_particles")));

        Checkbox soundsCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.visuals.purchase_sounds"),
                host.settingsFont())
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 108)
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
                .pos(host.settingsLeftPos() + 8, host.settingsTopPos() + 128)
                .selected(draft.paymentSlotSoundsEnabled())
                .onValueChange((checkbox, value) -> {
                    draft.setPaymentSlotSoundsEnabled(value);
                    onDirty.run();
                })
                .build());
        paymentSoundsCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.payment_sounds")));

        boolean blocked = placementResult != null && !placementResult.canSpawn() && !draft.npcEnabled();
        if (blocked) {
            npcToggle.setTooltip(Tooltip.create(Component.translatable(placementResult.translationKey())));
        }

        return new VillagerSectionWidgets(npcNameField, playerSkinNameField, professionButton);
    }

    public record VillagerSectionWidgets(EditBox npcNameField, EditBox playerSkinNameField, Button professionButton) {
    }

    public static void buildOfferItemSection(
            SingleOfferShopScreen host,
            ShopVisualType visualType,
            OfferItemSettings.Draft draft,
            boolean offerItemRenderingGloballyEnabled,
            Runnable onDirty,
            Runnable onRebuild) {
        int y = host.settingsTopPos() + 26;
        int leftX = host.settingsLeftPos() + 8;

        Button visibleButton = host.addSettingsWidget(Button.builder(
                toggleStateLabel(draft.visible()),
                b -> {
                    boolean next = !draft.visible();
                    draft.setVisible(next);
                    onDirty.run();
                    b.setMessage(toggleStateLabel(next));
                })
                .bounds(leftX, y, 35, 16)
                .build());

        if (!offerItemRenderingGloballyEnabled) {
            visibleButton.active = false;
            visibleButton.setTooltip(
                    Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_disabled_global")));
        } else {
            visibleButton.setTooltip(
                    Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_visible.tooltip")));
        }

        Checkbox fullbrightCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.visuals.offer_item_fullbright"),
                host.settingsFont())
                .pos(leftX + 40, y)
                .selected(draft.fullbright())
                .onValueChange((checkbox, value) -> {
                    draft.setFullbright(value);
                    onDirty.run();
                })
                .build());
        fullbrightCheckbox.setTooltip(
                Tooltip.create(Component.translatable("gui.marketblocks.visuals.offer_item_fullbright.tooltip")));
        y += 20;

        switch (visualType) {
            case TRADE_STAND -> {
                host.addSettingsWidget(new FloatSlider(leftX, y, 76, 16,
                        Component.translatable("gui.marketblocks.visuals.scale"), 0.5f, 1.5f, draft.scale(), value -> {
                            draft.setScale(value);
                            onDirty.run();
                        }));
                host.addSettingsWidget(new FloatSlider(leftX + 82, y, 76, 16,
                        Component.translatable("gui.marketblocks.visuals.speed"), 0.0f, 1.5f, draft.speed(), value -> {
                            draft.setSpeed(value);
                            onDirty.run();
                        }));
                y += 20;

                host.addSettingsWidget(
                        new FloatSlider(leftX, y, 76, 16, Component.translatable("gui.marketblocks.visuals.height"),
                                -0.25f, 0.25f, draft.heightOffset(), value -> {
                                    draft.setHeightOffset(value);
                                    onDirty.run();
                                }));
                host.addSettingsWidget(Checkbox.builder(
                        Component.translatable("gui.marketblocks.visuals.bobbing"),
                        host.settingsFont())
                        .pos(leftX + 82, y)
                        .selected(draft.bobbing())
                        .onValueChange((checkbox, value) -> {
                            draft.setBobbing(value);
                            onDirty.run();
                        })
                        .build());
            }
            case MARKET_CRATE -> {
                host.addSettingsWidget(
                        new IntSlider(leftX, y, 76, 16, Component.translatable("gui.marketblocks.visuals.count"), 1,
                                OfferItemSettings.MAX_COUNT, draft.count(), value -> {
                                    draft.setCount(value);
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

                CrateLayoutMode currentMode = draft.layoutMode();
                Button layoutModeButton = host.addSettingsWidget(Button.builder(
                        Component.translatable(currentMode.translationKey()),
                        b -> {
                            CrateLayoutMode nextMode = draft.layoutMode().next();
                            draft.setLayoutMode(nextMode);
                            onDirty.run();
                            onRebuild.run();
                        }).bounds(leftX, y, 158, 16).build());
                layoutModeButton
                        .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.layout_mode")));
                y += 20;

                host.addSettingsWidget(
                        new FloatSlider(leftX, y, 76, 16, Component.translatable("gui.marketblocks.visuals.rotation"),
                                0.0f, 360.0f, draft.rotation(), value -> {
                                    draft.setRotation(value);
                                    onDirty.run();
                                }));
                host.addSettingsWidget(
                        new FloatSlider(leftX + 82, y, 76, 16, Component.translatable("gui.marketblocks.visuals.scale"),
                                0.5f, 1.5f, draft.scale(), value -> {
                                    draft.setScale(value);
                                    onDirty.run();
                                }));
                y += 20;

                if (currentMode == CrateLayoutMode.STACKED) {
                    host.addSettingsWidget(new FloatSlider(leftX, y, 76, 16,
                            Component.translatable("gui.marketblocks.visuals.spacing_xz"), -0.25f, 0.25f,
                            draft.spacingXZ(), value -> {
                                draft.setSpacingXZ(value);
                                onDirty.run();
                            }));
                    host.addSettingsWidget(new FloatSlider(leftX + 82, y, 76, 16,
                            Component.translatable("gui.marketblocks.visuals.spacing_y"), 0.0f, 2.0f, draft.spacingY(),
                            value -> {
                                draft.setSpacingY(value);
                                onDirty.run();
                            }));
                } else {
                    host.addSettingsWidget(new FloatSlider(leftX, y, 76, 16,
                            Component.translatable("gui.marketblocks.visuals.chaos_rotation"), 0.0f, 1.0f,
                            draft.chaosRotation(), value -> {
                                draft.setChaosRotation(value);
                                onDirty.run();
                            }));
                    host.addSettingsWidget(new FloatSlider(leftX + 82, y, 76, 16,
                            Component.translatable("gui.marketblocks.visuals.spacing_y"), 0.0f, 2.0f, draft.spacingY(),
                            value -> {
                                draft.setSpacingY(value);
                                onDirty.run();
                            }));
                }
            }
            case UNKNOWN -> {
            }
        }
    }

    private static Component npcToggleLabel(VillagerSettings.Draft draft) {
        return toggleStateLabel(draft.npcEnabled());
    }

    private static Component toggleStateLabel(boolean enabled) {
        return Component.translatable(enabled ? "gui.marketblocks.toggle.on" : "gui.marketblocks.toggle.off");
    }

    private static Component professionLabel(VillagerSettings.Draft draft) {
        return Component.translatable("gui.marketblocks.visuals.profession").append(": ")
                .append(Component.translatable(draft.profession().translationKey()));
    }

    public static void buildNotificationSection(SingleOfferShopScreen host,
            NotificationSettings.Draft draft, Runnable onDirty) {
        int leftX = host.settingsLeftPos() + 8;
        int y = host.settingsTopPos() + 26;

        Checkbox purchaseCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.notifications.purchase"),
                host.settingsFont())
                .pos(leftX, y)
                .selected(draft.notifyOnPurchase())
                .onValueChange((checkbox, value) -> {
                    draft.setNotifyOnPurchase(value);
                    onDirty.run();
                })
                .build());
        purchaseCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.notifications.purchase.tooltip")));
        y += 20;

        Checkbox outOfStockCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.notifications.out_of_stock"),
                host.settingsFont())
                .pos(leftX, y)
                .selected(draft.notifyOnOutOfStock())
                .onValueChange((checkbox, value) -> {
                    draft.setNotifyOnOutOfStock(value);
                    onDirty.run();
                })
                .build());
        outOfStockCheckbox.setTooltip(
                Tooltip.create(Component.translatable("gui.marketblocks.notifications.out_of_stock.tooltip")));
        y += 20;

        Checkbox outputFullCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.notifications.output_full"),
                host.settingsFont())
                .pos(leftX, y)
                .selected(draft.notifyOnOutputFull())
                .onValueChange((checkbox, value) -> {
                    draft.setNotifyOnOutputFull(value);
                    onDirty.run();
                })
                .build());
        outputFullCheckbox.setTooltip(
                Tooltip.create(Component.translatable("gui.marketblocks.notifications.output_full.tooltip")));
        y += 20;

        Checkbox coOwnersCheckbox = host.addSettingsWidget(Checkbox.builder(
                Component.translatable("gui.marketblocks.notifications.co_owners"),
                host.settingsFont())
                .pos(leftX, y)
                .selected(draft.notifyCoOwners())
                .onValueChange((checkbox, value) -> {
                    draft.setNotifyCoOwners(value);
                    onDirty.run();
                })
                .build());
        coOwnersCheckbox
                .setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.notifications.co_owners.tooltip")));
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

        Button ownersBtn = host
                .addSettingsWidget(Button.builder(SingleOfferOwnerListPanel.ListMode.OWNERS.title(), b -> {
                    saveListPanelToDraft.run();
                    ownerListPanel.setListMode(SingleOfferOwnerListPanel.ListMode.OWNERS);
                    rebuildUI.run();
                }).bounds(leftPos + 8, topPos + 20, 78, 16).build());

        Button accessBtn = host.addSettingsWidget(
                Button.builder(SingleOfferOwnerListPanel.ListMode.ACCESS_LIST.title(), b -> {
                    saveListPanelToDraft.run();
                    ownerListPanel.setListMode(SingleOfferOwnerListPanel.ListMode.ACCESS_LIST);
                    rebuildUI.run();
                }).bounds(leftPos + 88, topPos + 20, 78, 16).build());

        if (ownerListPanel.getListMode() == SingleOfferOwnerListPanel.ListMode.OWNERS) {
            ownersBtn.active = false;
        } else {
            accessBtn.active = false;
            host.addSettingsWidget(Button.builder(accessDraft.accessMode().title(), b -> {
                saveListPanelToDraft.run();
                accessDraft.setAccessMode(accessDraft.accessMode().next());
                rebuildUI.run();
            }).bounds(leftPos + 8, topPos + 38, 158, 16).build());
        }

        ownerListPanel.prepareAndRender(host, accessDraft, topPos + 58, isPrimaryOwner, onDirty);
    }
}
