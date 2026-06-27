package de.bigbull.marketblocks.feature.marketplace.client.screen;

import java.util.Locale;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.marketplace.network.MarketplaceUpdateOfferPricingPacket;
import de.bigbull.marketblocks.feature.marketplace.data.DemandPricing;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceSerialization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.UUID;
import de.bigbull.marketblocks.feature.marketplace.data.Volatility;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;

/**
 * Modal dialog for editing the demand-based pricing configuration of a single marketplace offer.
 */
public class OfferPricingEditor extends BaseModalScreen {
    private static final int PANEL_WIDTH = 248;
    private static final int PANEL_HEIGHT = 166;
    private static final int INPUT_X_OFFSET = 164;

    private final UUID offerId;
    private final DemandPricing currentPricing;

    private boolean enablePricing;
    private Volatility volatility;
    private EditBox minMultiplierInput;
    private EditBox maxMultiplierInput;
    private Button volatilityButton;

    public OfferPricingEditor(Screen parent, UUID offerId, DemandPricing currentPricing) {
        this(parent, offerId, currentPricing, -1, -1);
    }

    public OfferPricingEditor(Screen parent, UUID offerId, DemandPricing currentPricing, int preferredLeft, int preferredCenterY) {
        super(Component.translatable("gui.marketblocks.marketplace.editor.pricing.title"), parent, PANEL_WIDTH, PANEL_HEIGHT, preferredLeft, preferredCenterY);
        this.offerId = offerId;
        this.currentPricing = currentPricing != null ? currentPricing : DemandPricing.disabled();
        this.enablePricing = this.currentPricing.enabled();
        this.volatility = this.currentPricing.volatility();
    }

    @Override
    protected void init() {
        initModalBounds();

        createPricingInputs();
        createActionButtons();
        setInitialFocus(minMultiplierInput);
    }

    private void createPricingInputs() {
        int inputX = panelLeft + INPUT_X_OFFSET;
        int rowStartY = panelTop + INPUT_START_Y_OFFSET;

        Button togglePricingButton = Button.builder(pricingToggleMessage(), button -> {
                    enablePricing = !enablePricing;
                    button.setMessage(pricingToggleMessage());
                })
                .bounds(inputX - 26, rowStartY - 2, 92, 20)
                .build();
        this.addRenderableWidget(togglePricingButton);

        this.minMultiplierInput = createIntegerInput(inputX, rowStartY + ROW_SPACING, (int) Math.round(currentPricing.minMultiplier() * 100));
        this.maxMultiplierInput = createIntegerInput(inputX, rowStartY + (ROW_SPACING * 2), (int) Math.round(currentPricing.maxMultiplier() * 100));

        this.volatilityButton = Button.builder(volatilityMessage(), button -> {
                    cycleVolatility();
                    button.setMessage(volatilityMessage());
                })
                .bounds(inputX, rowStartY + (ROW_SPACING * 3) - 1, INPUT_WIDTH, 20)
                .build();
        this.addRenderableWidget(volatilityButton);
    }

    private void cycleVolatility() {
        Volatility[] vals = Volatility.values();
        this.volatility = vals[(this.volatility.ordinal() + 1) % vals.length];
    }

    private Component volatilityMessage() {
        return Component.translatable("gui.marketblocks.marketplace.editor.pricing.volatility." + volatility.getSerializedName());
    }

    private EditBox createIntegerInput(int x, int y, int value) {
        EditBox input = new EditBox(this.font, x, y, INPUT_WIDTH, INPUT_HEIGHT, Component.literal(""));
        input.setFilter(OfferPricingEditor::isIntegerInput);
        input.setValue(String.valueOf(value));
        this.addRenderableWidget(input);
        return input;
    }

    private Component pricingToggleMessage() {
        return Component.translatable(enablePricing
                ? "gui.marketblocks.marketplace.editor.pricing.enabled"
                : "gui.marketblocks.marketplace.editor.pricing.disabled");
    }

    private void createActionButtons() {
        Button savePricingButton = Button.builder(Component.translatable("gui.marketblocks.save"), button -> savePricing())
                .bounds(panelLeft + 30, panelTop + PANEL_HEIGHT - 28, 84, 20)
                .build();
        this.addRenderableWidget(savePricingButton);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,
                button -> this.onClose())
                .bounds(panelLeft + PANEL_WIDTH - 114, panelTop + PANEL_HEIGHT - 28, 84, 20)
                .build());
    }

    private void savePricing() {
        try {
            int minPercent = parseIntegerOrDefault(minMultiplierInput.getValue(), (int) Math.round(currentPricing.minMultiplier() * 100));
            int maxPercent = parseIntegerOrDefault(maxMultiplierInput.getValue(), (int) Math.round(currentPricing.maxMultiplier() * 100));

            double minMultiplier = minPercent / 100.0;
            double maxMultiplier = maxPercent / 100.0;

            if (minMultiplier > maxMultiplier) {
                notifyClient(Component.translatable("message.marketblocks.marketplace.pricing.invalid_data"));
                return;
            }

            DemandPricing newPricing = new DemandPricing(enablePricing, 1.0d, volatility, minMultiplier, maxMultiplier);

            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
                notifyClient(Component.translatable("message.marketblocks.marketplace.pricing.no_connection"));
                return;
            }
            var encodeResult = MarketplaceSerialization.encodePricing(newPricing, connection.registryAccess());
            if (encodeResult.error().isEmpty()) {
                CompoundTag encoded = encodeResult.result().orElseThrow();
                NetworkHandler.sendToServer(new MarketplaceUpdateOfferPricingPacket(offerId, encoded));
                this.onClose();
            } else {
                notifyClient(Component.translatable("message.marketblocks.marketplace.pricing.invalid_data"));
            }
        } catch (NumberFormatException e) {
            notifyClient(Component.translatable("message.marketblocks.marketplace.pricing.invalid_number_format"));
        }
    }

    private static boolean isIntegerInput(String value) {
        return value.isEmpty() || value.matches("\\d{0,6}");
    }

    private static int parseIntegerOrDefault(String rawValue, int fallback) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int labelX = panelLeft + LABEL_X_OFFSET;
        int rowStartY = panelTop + LABEL_START_Y_OFFSET;
        
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.label"), labelX, rowStartY, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.min"), labelX, rowStartY + ROW_SPACING, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.max"), labelX, rowStartY + (ROW_SPACING * 2), 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.volatility"), labelX, rowStartY + (ROW_SPACING * 3), 0xCFCFCF, false);

        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY, "gui.marketblocks.marketplace.editor.pricing.label.tooltip");
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY + ROW_SPACING, "gui.marketblocks.marketplace.editor.pricing.min.tooltip");
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY + (ROW_SPACING * 2), "gui.marketblocks.marketplace.editor.pricing.max.tooltip");
        renderTooltipIfHovered(guiGraphics, mouseX, mouseY, labelX, rowStartY + (ROW_SPACING * 3), "gui.marketblocks.marketplace.editor.pricing.volatility.tooltip");
    }

    private void renderTooltipIfHovered(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, String translationKey) {
        int w = 80; // approximate hit width for the text
        if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + font.lineHeight) {
            List<FormattedCharSequence> lines = font.split(Component.translatable(translationKey), 200);
            guiGraphics.renderTooltip(this.font, lines, mouseX, mouseY);
        }
    }
}




