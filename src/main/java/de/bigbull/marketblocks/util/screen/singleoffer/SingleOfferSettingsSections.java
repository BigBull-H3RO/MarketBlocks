package de.bigbull.marketblocks.util.screen.singleoffer;

import de.bigbull.marketblocks.shop.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.shop.visual.VillagerVisualProfession;
import de.bigbull.marketblocks.shop.visual.VisualNpcPlacementResult;
import de.bigbull.marketblocks.util.screen.gui.IconButton;
import de.bigbull.marketblocks.util.screen.gui.SideModeButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
                                              String draftShopName,
                                              Supplier<Component> emitToggleLabel,
                                              Runnable onEmitToggle,
                                              Consumer<String> onNameChange) {
        EditBox nameField = host.addSettingsWidget(new EditBox(host.settingsFont(), host.settingsLeftPos() + 8, host.settingsTopPos() + 20, 120, 20,
                Component.translatable("gui.marketblocks.shop_name")));
        nameField.setMaxLength(32);
        nameField.setValue(draftShopName != null ? draftShopName : "");
        nameField.setResponder(onNameChange);

        Button emitToggle = host.addSettingsWidget(Button.builder(emitToggleLabel.get(), b -> {
            onEmitToggle.run();
            b.setMessage(emitToggleLabel.get());
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 45, 24, 16).build());
        emitToggle.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.emit_redstone.tooltip")));

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
            boolean npcEnabled,
            String npcName,
            VillagerVisualProfession profession,
            boolean purchaseParticlesEnabled,
            boolean purchaseSoundsEnabled,
            VisualNpcPlacementResult placementResult,
            Runnable onNpcToggle,
            Consumer<String> onNpcNameChanged,
            Runnable onProfessionCycle,
            Runnable onParticlesToggle,
            Runnable onSoundsToggle,
            Supplier<Component> npcToggleLabel,
            Supplier<Component> professionLabel,
            Supplier<Component> particlesToggleLabel,
            Supplier<Component> soundsToggleLabel
    ) {
        Button npcToggle = host.addSettingsWidget(Button.builder(npcToggleLabel.get(), b -> {
            onNpcToggle.run();
            b.setMessage(npcToggleLabel.get());
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 20, 50, 16).build());
        npcToggle.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.npc_enabled")));

        EditBox npcNameField = host.addSettingsWidget(new EditBox(host.settingsFont(), host.settingsLeftPos() + 63, host.settingsTopPos() + 20, 103, 16,
                Component.translatable("gui.marketblocks.visuals.npc_name")));
        npcNameField.setMaxLength(32);
        npcNameField.setValue(npcName == null ? "" : npcName);
        npcNameField.setResponder(onNpcNameChanged);

        Button professionButton = host.addSettingsWidget(Button.builder(professionLabel.get(), b -> {
            onProfessionCycle.run();
            b.setMessage(professionLabel.get());
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 42, 158, 16).build());
        professionButton.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.profession")));

        Button particlesToggle = host.addSettingsWidget(Button.builder(particlesToggleLabel.get(), b -> {
            onParticlesToggle.run();
            b.setMessage(particlesToggleLabel.get());
        }).bounds(host.settingsLeftPos() + 8, host.settingsTopPos() + 64, 50, 16).build());
        particlesToggle.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_particles")));

        Button soundsToggle = host.addSettingsWidget(Button.builder(soundsToggleLabel.get(), b -> {
            onSoundsToggle.run();
            b.setMessage(soundsToggleLabel.get());
        }).bounds(host.settingsLeftPos() + 63, host.settingsTopPos() + 64, 50, 16).build());
        soundsToggle.setTooltip(Tooltip.create(Component.translatable("gui.marketblocks.visuals.purchase_sounds")));

        boolean blocked = placementResult != null && !placementResult.canSpawn() && !npcEnabled;
        if (blocked) {
            npcToggle.setTooltip(Tooltip.create(Component.translatable(placementResult.translationKey())));
        }

        return new VisualSectionWidgets(npcNameField);
    }

    public record VisualSectionWidgets(EditBox npcNameField) {
    }
}



