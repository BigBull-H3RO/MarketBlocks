package de.bigbull.marketblocks.util.custom.screen.marketplace;

import de.bigbull.marketblocks.util.custom.screen.gui.OfferTemplateButton;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceClientState;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceOffer;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceOfferViewState;
import de.bigbull.marketblocks.shop.marketplace.MarketplaceRuntimeMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Renders offer list/status overlays for MarketplaceScreen without owning screen state.
 */
public final class MarketplaceOverlayRenderer {

    public void render(GuiGraphics guiGraphics, Context context) {
        renderEmptyState(guiGraphics, context);
        renderOfferRows(guiGraphics, context);
        renderSelectedOfferStatus(guiGraphics, context);
    }

    private void renderEmptyState(GuiGraphics guiGraphics, Context context) {
        if (!context.hasPages()) {
            Component noPagesText = Component.translatable("gui.marketblocks.marketplace.no_pages");
            int textX = context.previewX() + 10;
            int textY = context.previewY() + 34;
            guiGraphics.drawString(context.font(), noPagesText, textX, textY, 0x555555, false);
        } else if (context.visibleOffers().isEmpty()) {
            Component noOffersText = Component.translatable("gui.marketblocks.marketplace.no_offers");
            int textX = context.listStartX() + (context.listWidth() - context.font().width(noOffersText)) / 2;
            int textY = context.listStartY() + context.listHeight() / 2 - 4;
            guiGraphics.drawString(context.font(), noOffersText, textX, textY, 0x555555, false);
        }
    }

    private void renderOfferRows(GuiGraphics guiGraphics, Context context) {
        int listStartX = context.listStartX();
        int listStartY = context.listStartY();
        guiGraphics.enableScissor(listStartX, listStartY, listStartX + context.listWidth() + 2, listStartY + context.listVisibleHeight());

        int end = Math.min(context.scrollOffset() + context.maxVisibleRows(), context.visibleOffers().size());
        int currentY = listStartY;
        for (int i = context.scrollOffset(); i < end; i++) {
            MarketplaceOffer offer = context.visibleOffers().get(i);
            boolean isSelected = offer.id().equals(context.selectedOfferId());
            renderOfferRow(guiGraphics, context, offer, listStartX, currentY, isSelected);
            currentY += context.rowHeight();
        }
        guiGraphics.disableScissor();

        renderHoveredRowTooltip(guiGraphics, context, listStartX, listStartY);
    }

    private void renderSelectedOfferStatus(GuiGraphics guiGraphics, Context context) {
        UUID selectedOfferId = context.selectedOfferId();
        if (selectedOfferId == null) {
            return;
        }
        MarketplaceOffer offer = context.offerLookup().apply(selectedOfferId);
        if (offer == null) {
            return;
        }

        MarketplaceOfferViewState viewState = MarketplaceClientState.offerViewState(selectedOfferId);
        int textX = context.previewX() + 56;
        int textY = context.previewY() + 60;

        renderAvailabilityIcon(guiGraphics, context, viewState, textX, textY);
        int nextRowY = renderPriceAndDailyStatus(guiGraphics, context, viewState, textX, textY);
        nextRowY = renderStockStatus(guiGraphics, context, viewState, textX, nextRowY);
        renderRestockStatus(guiGraphics, context, viewState, textX, nextRowY);
    }

    private void renderAvailabilityIcon(GuiGraphics guiGraphics, Context context, MarketplaceOfferViewState viewState, int textX, int textY) {
        ResourceLocation availabilityIcon = context.unavailableIconResolver().apply(viewState);
        if (availabilityIcon == null) {
            return;
        }

        int iconX = textX + 72;
        int iconY = textY - 2;
        guiGraphics.blit(availabilityIcon, iconX, iconY, 0, 0, 16, 16, 16, 16);
        if (!context.suppressInteractions() && isPointWithinIcon(context.mouseX(), context.mouseY(), iconX, iconY)) {
            guiGraphics.renderTooltip(context.font(), buildUnavailableOfferTooltip(context, viewState), context.mouseX(), context.mouseY());
        }
    }

    private int renderPriceAndDailyStatus(GuiGraphics guiGraphics, Context context, MarketplaceOfferViewState viewState, int textX, int textY) {
        Component priceText = Component.translatable(
                "gui.marketblocks.marketplace.status.price_short",
                String.format(java.util.Locale.ROOT, "%.2f", viewState.priceMultiplier())
        );
        guiGraphics.drawString(context.font(), priceText, textX, textY, 0x404040, false);
        if (!context.suppressInteractions() && isPointWithinStatusLine(context, textX, textY, priceText)) {
            guiGraphics.renderTooltip(context.font(), Component.translatable("gui.marketblocks.marketplace.tooltip.price_multiplier"), context.mouseX(), context.mouseY());
        }

        int compactX = textX + context.font().width(priceText) + 8;
        if (viewState.remainingDailyPurchases().isPresent()) {
            int remainingDaily = viewState.remainingDailyPurchases().get();
            Component dailyText = Component.translatable("gui.marketblocks.marketplace.status.daily_short", remainingDaily);
            int dailyColor = remainingDaily == 0 ? 0xAA3333 : 0x404040;
            guiGraphics.drawString(context.font(), dailyText, compactX, textY, dailyColor, false);
            if (!context.suppressInteractions() && isPointWithinStatusLine(context, compactX, textY, dailyText)) {
                Component tooltip = remainingDaily == 0
                        ? Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_daily_empty")
                        : Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_daily");
                guiGraphics.renderTooltip(context.font(), tooltip, context.mouseX(), context.mouseY());
            }
        }

        return textY + context.statusLineHeight();
    }

    private int renderStockStatus(GuiGraphics guiGraphics, Context context, MarketplaceOfferViewState viewState, int textX, int textY) {
        if (viewState.remainingStock().isPresent()) {
            int remainingStock = viewState.remainingStock().get();
            int stockColor = remainingStock == 0 ? 0xAA3333 : 0x404040;
            Component stockText = Component.translatable("gui.marketblocks.marketplace.status.stock_short", remainingStock);
            guiGraphics.drawString(context.font(), stockText, textX, textY, stockColor, false);
            if (!context.suppressInteractions() && isPointWithinStatusLine(context, textX, textY, stockText)) {
                Component tooltip = remainingStock == 0
                        ? Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_stock_empty")
                        : Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_stock");
                guiGraphics.renderTooltip(context.font(), tooltip, context.mouseX(), context.mouseY());
            }
            return textY + context.statusLineHeight();
        }
        return textY;
    }

    private void renderRestockStatus(GuiGraphics guiGraphics, Context context, MarketplaceOfferViewState viewState, int textX, int textY) {
        Optional<Integer> displayRestockSeconds = context.displayRestockSecondsResolver().apply(viewState);
        if (displayRestockSeconds.isPresent()) {
            int restockSeconds = displayRestockSeconds.get();
            String restockValue = restockSeconds > 0
                    ? MarketplaceRuntimeMath.formatSecondsAsTimer(restockSeconds)
                    : "0:00";
            Component restockText = Component.translatable("gui.marketblocks.marketplace.status.restock_short", restockValue);
            int restockColor = restockSeconds > 0 ? 0x406080 : 0x2E8B57;
            guiGraphics.drawString(context.font(), restockText, textX, textY, restockColor, false);
            if (!context.suppressInteractions() && isPointWithinStatusLine(context, textX, textY, restockText)) {
                Component tooltip = restockSeconds > 0
                        ? Component.translatable("gui.marketblocks.marketplace.tooltip.restock_in")
                        : Component.translatable("gui.marketblocks.marketplace.tooltip.restock_ready");
                guiGraphics.renderTooltip(context.font(), tooltip, context.mouseX(), context.mouseY());
            }
        }
    }

    private void renderOfferRow(GuiGraphics graphics, Context context, MarketplaceOffer offer, int x, int y, boolean isSelected) {
        MarketplaceOfferViewState viewState = MarketplaceClientState.offerViewState(offer.id());
        ItemStack[] payments = context.paymentNormalizer().apply(offer.effectivePayments());
        ItemStack p1 = payments[0];
        ItemStack p2 = payments[1];

        boolean offerAvailable = viewState.maxPurchasable() > 0;
        OfferTemplateButton rowButton = new OfferTemplateButton(x, y, ignored -> {});
        rowButton.update(p1, p2, offer.result(), offerAvailable);
        int renderMouseX = isSelected ? x + 1 : context.mouseX();
        int renderMouseY = isSelected ? y + 1 : context.mouseY();
        rowButton.render(graphics, renderMouseX, renderMouseY, 0.0F);
    }

    private void renderHoveredRowTooltip(GuiGraphics graphics, Context context, int listStartX, int listStartY) {
        if (context.suppressInteractions()) {
            return;
        }

        int mouseX = context.mouseX();
        int mouseY = context.mouseY();
        if (mouseX < listStartX
                || mouseX >= listStartX + context.listWidth()
                || mouseY < listStartY
                || mouseY >= listStartY + context.listVisibleHeight()) {
            return;
        }

        int interactiveHeight = context.maxVisibleRows() * context.rowHeight();
        if (mouseY >= listStartY + interactiveHeight) {
            return;
        }

        int hoveredRow = (mouseY - listStartY) / context.rowHeight();
        int offerIndex = context.scrollOffset() + hoveredRow;
        if (offerIndex < 0 || offerIndex >= context.visibleOffers().size()) {
            return;
        }

        MarketplaceOffer offer = context.visibleOffers().get(offerIndex);
        MarketplaceOfferViewState viewState = MarketplaceClientState.offerViewState(offer.id());
        ItemStack[] payments = context.paymentNormalizer().apply(offer.effectivePayments());

        int payment1X = listStartX + 4;
        int resultX = listStartX + 70;
        renderRowTooltips(graphics, context, payments[0], payments[1], offer.result(), viewState, payment1X, resultX);
    }

    private void renderRowTooltips(GuiGraphics graphics, Context context, ItemStack p1, ItemStack p2, ItemStack result,
                                   MarketplaceOfferViewState viewState,
                                   int startX, int resultX) {
        int mouseX = context.mouseX();
        int mouseY = context.mouseY();

        if (mouseX >= startX && mouseX <= startX + 16 && !p1.isEmpty()) {
            graphics.renderTooltip(context.font(), p1, mouseX, mouseY);
        } else if (mouseX >= startX + 24 && mouseX <= startX + 40 && !p2.isEmpty()) {
            graphics.renderTooltip(context.font(), p2, mouseX, mouseY);
        } else if (mouseX >= resultX && mouseX <= resultX + 16 && !result.isEmpty()) {
            graphics.renderTooltip(context.font(), result, mouseX, mouseY);
        } else if (viewState.maxPurchasable() <= 0) {
            graphics.renderTooltip(context.font(), buildUnavailableOfferTooltip(context, viewState), mouseX, mouseY);
        }
    }

    private boolean isPointWithinStatusLine(Context context, int textX, int textY, Component text) {
        int width = context.font().width(text);
        return context.mouseX() >= textX
                && context.mouseX() <= textX + width
                && context.mouseY() >= textY
                && context.mouseY() <= textY + context.statusLineHeight();
    }

    private boolean isPointWithinIcon(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private Component buildUnavailableOfferTooltip(Context context, MarketplaceOfferViewState viewState) {
        if (viewState.remainingDailyPurchases().isPresent() && viewState.remainingDailyPurchases().get() <= 0) {
            return Component.translatable("gui.marketblocks.marketplace.tooltip.unavailable_daily");
        }
        if (viewState.remainingStock().isPresent() && viewState.remainingStock().get() <= 0) {
            if (context.displayRestockSecondsResolver().apply(viewState).isPresent()) {
                return Component.translatable("gui.marketblocks.marketplace.tooltip.unavailable_restock");
            }
            return Component.translatable("gui.marketblocks.marketplace.tooltip.unavailable_stock");
        }
        return Component.translatable("gui.marketblocks.marketplace.tooltip.unavailable_generic");
    }

    public record Context(
            Font font,
            int mouseX,
            int mouseY,
            boolean suppressInteractions,
            int leftPos,
            int topPos,
            boolean hasPages,
            int listXOffset,
            int listYOffset,
            int listWidth,
            int listHeight,
            int rowHeight,
            int maxVisibleRowsLimit,
            int previewXOffset,
            int previewYOffset,
            int statusLineHeight,
            int scrollOffset,
            int maxVisibleRows,
            List<MarketplaceOffer> visibleOffers,
            UUID selectedOfferId,
            WidgetSprites buttonSprites,
            ResourceLocation arrowIcon,
            Function<UUID, MarketplaceOffer> offerLookup,
            Function<List<ItemStack>, ItemStack[]> paymentNormalizer,
            Function<MarketplaceOfferViewState, ResourceLocation> unavailableIconResolver,
            Function<MarketplaceOfferViewState, Optional<Integer>> displayRestockSecondsResolver
    ) {
        int listStartX() {
            return leftPos + listXOffset;
        }

        int listStartY() {
            return topPos + listYOffset;
        }

        int listVisibleHeight() {
            return listHeight;
        }

        int previewX() {
            return leftPos + previewXOffset;
        }

        int previewY() {
            return topPos + previewYOffset;
        }
    }
}



