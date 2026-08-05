package de.bigbull.marketblocks.feature.marketplace.client.screen;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceUpdateOfferLimitsPacket;
import de.bigbull.marketblocks.feature.marketplace.data.OfferLimit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * Modal dialog for editing the purchase limits of a single marketplace offer.
 */
public class OfferLimitsEditor extends BaseModalScreen {
    private static final int PANEL_WIDTH = 236;
    private static final int PANEL_HEIGHT = 138;
    private static final int INPUT_X_OFFSET = 154;
    private static final String POSITIVE_INT_PATTERN = "\\d{1,9}";

    private final UUID offerId;
    private final OfferLimit currentLimit;

    private EditBox dailyLimitInput;
    private EditBox stockLimitInput;
    private EditBox restockSecondsInput;

    public OfferLimitsEditor(Screen parent, UUID offerId, OfferLimit currentLimit) {
        this(parent, offerId, currentLimit, -1, -1);
    }

    public OfferLimitsEditor(Screen parent, UUID offerId, OfferLimit currentLimit, int preferredLeft,
            int preferredCenterY) {
        super(Component.translatable("gui.marketblocks.marketplace.editor.limits.title"), parent, PANEL_WIDTH,
                PANEL_HEIGHT, preferredLeft, preferredCenterY);
        this.offerId = offerId;
        this.currentLimit = currentLimit != null ? currentLimit : OfferLimit.unlimited();
    }

    @Override
    protected void init() {
        initModalBounds();

        createLimitInputs();
        createActionButtons();
        setInitialFocus(dailyLimitInput);
    }

    private void createLimitInputs() {
        int inputX = panelLeft + INPUT_X_OFFSET;
        int rowStartY = panelTop + INPUT_START_Y_OFFSET;

        this.dailyLimitInput = createPositiveIntInput(inputX, rowStartY,
                currentLimit.dailyLimit().map(String::valueOf).orElse(""));
        this.stockLimitInput = createPositiveIntInput(inputX, rowStartY + ROW_SPACING,
                currentLimit.stockLimit().map(String::valueOf).orElse(""));
        this.restockSecondsInput = createPositiveIntInput(inputX, rowStartY + (ROW_SPACING * 2),
                currentLimit.restockSeconds().map(String::valueOf).orElse(""));
    }

    private EditBox createPositiveIntInput(int x, int y, String value) {
        EditBox input = new EditBox(this.font, x, y, INPUT_WIDTH, INPUT_HEIGHT, Component.literal(""));
        input.setFilter(raw -> raw.isEmpty() || raw.matches(POSITIVE_INT_PATTERN));
        input.setValue(value);
        this.addRenderableWidget(input);
        return input;
    }

    private void createActionButtons() {
        Button saveLimitsButton = Button
                .builder(Component.translatable("gui.marketblocks.save"), button -> saveLimits())
                .bounds(panelLeft + 26, panelTop + PANEL_HEIGHT - 28, 84, 20)
                .build();
        this.addRenderableWidget(saveLimitsButton);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,
                button -> this.onClose())
                .bounds(panelLeft + PANEL_WIDTH - 110, panelTop + PANEL_HEIGHT - 28, 84, 20)
                .build());

    }

    private void saveLimits() {
        try {
            OfferLimit newLimit = buildUpdatedLimit();
            NetworkHandler.sendToServer(new MarketplaceUpdateOfferLimitsPacket(offerId, newLimit));
            this.onClose();
        } catch (NumberFormatException e) {
            notifyClient(Component.translatable("message.marketblocks.marketplace.limits.invalid_positive_int"));
        }
    }

    private OfferLimit buildUpdatedLimit() {
        Integer dailyLimit = parseOptionalPositiveInt(dailyLimitInput.getValue());
        Integer stockLimit = parseOptionalPositiveInt(stockLimitInput.getValue());
        Integer restockSeconds = parseOptionalPositiveInt(restockSecondsInput.getValue());
        return (dailyLimit == null && stockLimit == null && restockSeconds == null)
                ? OfferLimit.unlimited()
                : new OfferLimit(false, dailyLimit, stockLimit, restockSeconds);
    }

    private static Integer parseOptionalPositiveInt(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            return null;
        }
        int parsed = Integer.parseInt(value);
        return parsed > 0 ? parsed : null;
    }

    @Override
    protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int labelX = panelLeft + LABEL_X_OFFSET;
        int rowStartY = panelTop + LABEL_START_Y_OFFSET;
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.limits.daily"),
                labelX, rowStartY, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.limits.stock"),
                labelX, rowStartY + ROW_SPACING, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.limits.restock"),
                labelX, rowStartY + (ROW_SPACING * 2), 0xCFCFCF, false);

        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY,
                "gui.marketblocks.marketplace.editor.limits.daily.tooltip");
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY + ROW_SPACING,
                "gui.marketblocks.marketplace.editor.limits.stock.tooltip");
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY + (ROW_SPACING * 2),
                "gui.marketblocks.marketplace.editor.limits.restock.tooltip");
    }
}
