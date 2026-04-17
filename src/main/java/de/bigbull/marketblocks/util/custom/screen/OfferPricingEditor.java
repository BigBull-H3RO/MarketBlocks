package de.bigbull.marketblocks.util.custom.screen;

import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.marketplace.MarketplaceUpdateOfferPricingPacket;
import de.bigbull.marketblocks.shop.marketplace.DemandPricing;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceSerialization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.UUID;

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
    private EditBox demandStepInput;
    private EditBox minMultiplierInput;
    private EditBox maxMultiplierInput;

    public OfferPricingEditor(Screen parent, UUID offerId, DemandPricing currentPricing) {
        this(parent, offerId, currentPricing, -1, -1);
    }

    public OfferPricingEditor(Screen parent, UUID offerId, DemandPricing currentPricing, int preferredLeft, int preferredCenterY) {
        super(Component.translatable("gui.marketblocks.marketplace.editor.pricing.title"), parent, PANEL_WIDTH, PANEL_HEIGHT, preferredLeft, preferredCenterY);
        this.offerId = offerId;
        this.currentPricing = currentPricing != null ? currentPricing : DemandPricing.disabled();
        this.enablePricing = this.currentPricing.enabled();
    }

    @Override
    protected void init() {
        initModalBounds();

        createPricingInputs();
        createActionButtons();
        setInitialFocus(demandStepInput);
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

        this.demandStepInput = createDecimalInput(inputX, rowStartY + ROW_SPACING, currentPricing.demandStep());
        this.minMultiplierInput = createDecimalInput(inputX, rowStartY + (ROW_SPACING * 2), currentPricing.minMultiplier());
        this.maxMultiplierInput = createDecimalInput(inputX, rowStartY + (ROW_SPACING * 3), currentPricing.maxMultiplier());
    }

    private EditBox createDecimalInput(int x, int y, double value) {
        EditBox input = new EditBox(this.font, x, y, INPUT_WIDTH, INPUT_HEIGHT, Component.literal(""));
        input.setFilter(OfferPricingEditor::isDecimalInput);
        input.setValue(String.format(java.util.Locale.ROOT, "%.2f", value));
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
            double demandStep = parseLocalizedDoubleOrDefault(demandStepInput.getValue(), currentPricing.demandStep());
            double minMultiplier = parseLocalizedDoubleOrDefault(minMultiplierInput.getValue(), currentPricing.minMultiplier());
            double maxMultiplier = parseLocalizedDoubleOrDefault(maxMultiplierInput.getValue(), currentPricing.maxMultiplier());

            if (!Double.isFinite(demandStep) || !Double.isFinite(minMultiplier) || !Double.isFinite(maxMultiplier)) {
                notifyClient(Component.translatable("message.marketblocks.marketplace.pricing.invalid_finite"));
                return;
            }

            DemandPricing newPricing = new DemandPricing(enablePricing, 1.0d, demandStep, minMultiplier, maxMultiplier);

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

    private static boolean isDecimalInput(String value) {
        return value.isEmpty() || value.matches("\\d{0,6}([.,]\\d{0,4})?");
    }

    private static double parseLocalizedDoubleOrDefault(String rawValue, double fallback) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            return fallback;
        }
        return Double.parseDouble(value.replace(',', '.'));
    }

    @Override
    protected void renderPanelForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int labelX = panelLeft + LABEL_X_OFFSET;
        int rowStartY = panelTop + LABEL_START_Y_OFFSET;
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.label"), labelX, rowStartY, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.step"), labelX, rowStartY + ROW_SPACING, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.min"), labelX, rowStartY + (ROW_SPACING * 2), 0xCFCFCF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.marketblocks.marketplace.editor.pricing.max"), labelX, rowStartY + (ROW_SPACING * 3), 0xCFCFCF, false);
    }
}

