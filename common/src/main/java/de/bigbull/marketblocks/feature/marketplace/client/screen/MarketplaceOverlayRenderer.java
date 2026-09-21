package de.bigbull.marketblocks.feature.marketplace.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.client.gui.OfferTemplateButton;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceClientState;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOffer;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOfferViewState;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceRuntimeMath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Renders offer list/status overlays for MarketplaceScreen without owning
 * screen state.
 */
public final class MarketplaceOverlayRenderer {

    public void render(GuiGraphics guiGraphics, Context context) {
        renderEmptyState(guiGraphics, context);
        renderOfferRows(guiGraphics, context);
        renderSelectedOfferStatus(guiGraphics, context);
    }

    public void renderTooltips(GuiGraphics guiGraphics, Context context) {
        if (context.suppressInteractions()) {
            return;
        }
        int listStartX = context.listStartX();
        int listStartY = context.listStartY();
        renderHoveredRowTooltip(guiGraphics, context, listStartX, listStartY);
        renderSelectedOfferStatusTooltips(guiGraphics, context);
    }

    private void renderEmptyState(GuiGraphics guiGraphics, Context context) {
        if (!context.hasPages()) {
            Component noPagesText = Component.translatable("gui.marketblocks.marketplace.no_pages");
            int textX = context.previewX() - 118;
            int textY = context.previewY() + 55;
            guiGraphics.drawWordWrap(context.font(), noPagesText, textX, textY, 200, 0x555555);
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
        guiGraphics.enableScissor(listStartX, listStartY, listStartX + context.listWidth() + 2,
                listStartY + context.listVisibleHeight());

        int end = Math.min(context.scrollOffset() + context.maxVisibleRows(), context.visibleOffers().size());
        int currentY = listStartY;
        for (int i = context.scrollOffset(); i < end; i++) {
            MarketplaceOffer offer = context.visibleOffers().get(i);
            boolean isSelected = offer.id().equals(context.selectedOfferId());
            renderOfferRow(guiGraphics, context, offer, listStartX, currentY, isSelected);
            currentY += context.rowHeight();
        }
        guiGraphics.disableScissor();
    }

    private static final ResourceLocation OUT_OF_STOCK_ICON = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/icon/out_of_stock.png");
    private static final int STATUS_ICON_X = 182;
    private static final int STATUS_ICON_Y = 76;
    private static final int STATUS_ICON_WIDTH = 28;
    private static final int STATUS_ICON_HEIGHT = 21;

    private record StatusItem(Component text, int color, Component tooltip, int x, int y) {
        StatusItem withPos(int newX, int newY) {
            return new StatusItem(text, color, tooltip, newX, newY);
        }
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

        // 1. Render 28x21 out of stock icon between trade slots when offer is
        // unavailable
        if (viewState.maxPurchasable() <= 0) {
            int slotIconX = context.leftPos() + STATUS_ICON_X;
            int slotIconY = context.topPos() + STATUS_ICON_Y;
            guiGraphics.blit(OUT_OF_STOCK_ICON, slotIconX, slotIconY, 0, 0, STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT,
                    STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT);
        }

        // 2. Render dynamic status indicators in compact 2-row grid below trade slots
        List<StatusItem> items = buildStatusItems(context, offer, viewState);
        for (StatusItem item : items) {
            guiGraphics.drawString(context.font(), item.text(), item.x(), item.y(), item.color(), false);
        }
    }

    private void renderSelectedOfferStatusTooltips(GuiGraphics guiGraphics, Context context) {
        UUID selectedOfferId = context.selectedOfferId();
        if (selectedOfferId == null) {
            return;
        }
        MarketplaceOffer offer = context.offerLookup().apply(selectedOfferId);
        if (offer == null) {
            return;
        }

        MarketplaceOfferViewState viewState = MarketplaceClientState.offerViewState(selectedOfferId);

        // 1. Tooltip for 28x21 out of stock icon between trade slots
        if (viewState.maxPurchasable() <= 0) {
            int slotIconX = context.leftPos() + STATUS_ICON_X;
            int slotIconY = context.topPos() + STATUS_ICON_Y;
            if (context.mouseX() >= slotIconX && context.mouseX() <= slotIconX + STATUS_ICON_WIDTH
                    && context.mouseY() >= slotIconY && context.mouseY() <= slotIconY + STATUS_ICON_HEIGHT) {
                guiGraphics.renderTooltip(context.font(), buildUnavailableOfferTooltip(context, viewState),
                        context.mouseX(), context.mouseY());
            }

            int previewArrowX = context.previewX() + OfferTemplateButton.ARROW_X_OFFSET;
            int previewArrowY = context.previewY() + OfferTemplateButton.ARROW_Y_OFFSET;
            if (context.mouseX() >= previewArrowX - 1 && context.mouseX() <= previewArrowX + 11
                    && context.mouseY() >= previewArrowY - 1 && context.mouseY() <= previewArrowY + 10) {
                guiGraphics.renderTooltip(context.font(), buildUnavailableOfferTooltip(context, viewState),
                        context.mouseX(), context.mouseY());
            }
        }

        // 2. Tooltip for dynamic status indicators
        List<StatusItem> items = buildStatusItems(context, offer, viewState);
        for (StatusItem item : items) {
            if (isPointWithinStatusItem(context, item)) {
                guiGraphics.renderTooltip(context.font(), item.tooltip(), context.mouseX(), context.mouseY());
            }
        }
    }

    private boolean isPointWithinStatusItem(Context context, StatusItem item) {
        int width = context.font().width(item.text());
        return context.mouseX() >= item.x()
                && context.mouseX() <= item.x() + width
                && context.mouseY() >= item.y()
                && context.mouseY() <= item.y() + context.font().lineHeight;
    }

    private List<StatusItem> buildStatusItems(Context context, MarketplaceOffer offer,
            MarketplaceOfferViewState viewState) {
        List<StatusItem> items = new ArrayList<>();
        int row1Y = context.topPos() + 102;
        int row2Y = context.topPos() + 113;
        int colLeftX = context.leftPos() + 196;
        int colRightX = context.leftPos() + 230;

        boolean hasPricing = offer.pricing() != null && offer.pricing().enabled();
        boolean hasDaily = viewState.remainingDailyPurchases().isPresent();
        boolean hasStock = viewState.remainingStock().isPresent();
        Optional<Integer> displayRestockSeconds = context.displayRestockSecondsResolver().apply(viewState);
        boolean hasRestock = displayRestockSeconds.isPresent();

        StatusItem priceItem = null;
        if (hasPricing) {
            Component priceText = Component.translatable(
                    "gui.marketblocks.marketplace.status.price_short",
                    String.format(Locale.ROOT, "%.2f", viewState.priceMultiplier()));
            Component priceTooltip = Component.translatable("gui.marketblocks.marketplace.tooltip.price_multiplier");
            priceItem = new StatusItem(priceText, 0x404040, priceTooltip, 0, 0);
        }

        StatusItem dailyItem = null;
        if (hasDaily) {
            int remainingDaily = viewState.remainingDailyPurchases().get();
            Component dailyText = Component.translatable("gui.marketblocks.marketplace.status.daily_short",
                    remainingDaily);
            Component dailyTooltip = remainingDaily == 0
                    ? Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_daily_empty")
                    : Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_daily");
            int dailyColor = remainingDaily == 0 ? 0xAA3333 : 0x404040;
            dailyItem = new StatusItem(dailyText, dailyColor, dailyTooltip, 0, 0);
        }

        StatusItem stockItem = null;
        if (hasStock) {
            int remainingStock = viewState.remainingStock().get();
            Component stockText = Component.translatable("gui.marketblocks.marketplace.status.stock_short",
                    remainingStock);
            Component stockTooltip = remainingStock == 0
                    ? Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_stock_empty")
                    : Component.translatable("gui.marketblocks.marketplace.tooltip.remaining_stock");
            int stockColor = remainingStock == 0 ? 0xAA3333 : 0x404040;
            stockItem = new StatusItem(stockText, stockColor, stockTooltip, 0, 0);
        }

        StatusItem restockItem = null;
        if (hasRestock) {
            int restockSeconds = displayRestockSeconds.get();
            String restockValue = restockSeconds > 0
                    ? MarketplaceRuntimeMath.formatSecondsAsTimer(restockSeconds)
                    : "0:00";
            Component restockText = Component.translatable("gui.marketblocks.marketplace.status.restock_short",
                    restockValue);
            Component restockTooltip = restockSeconds > 0
                    ? Component.translatable("gui.marketblocks.marketplace.tooltip.restock_in")
                    : Component.translatable("gui.marketblocks.marketplace.tooltip.restock_ready");
            int restockColor = restockSeconds > 0 ? 0x406080 : 0x2E8B57;
            restockItem = new StatusItem(restockText, restockColor, restockTooltip, 0, 0);
        }

        if (hasPricing) {
            items.add(priceItem.withPos(colLeftX, row1Y));
            if (hasDaily) {
                items.add(dailyItem.withPos(colRightX, row1Y));
                if (hasStock) {
                    items.add(stockItem.withPos(colLeftX, row2Y));
                }
                if (hasRestock) {
                    items.add(restockItem.withPos(colRightX, row2Y));
                }
            } else {
                if (hasStock) {
                    items.add(stockItem.withPos(colRightX, row1Y));
                    if (hasRestock) {
                        items.add(restockItem.withPos(colRightX, row2Y));
                    }
                }
            }
        } else {
            // Pricing disabled
            if (hasDaily) {
                items.add(dailyItem.withPos(colLeftX, row1Y));
                if (hasStock) {
                    items.add(stockItem.withPos(colRightX, row1Y));
                    if (hasRestock) {
                        items.add(restockItem.withPos(colLeftX, row2Y));
                    }
                }
            } else {
                if (hasStock) {
                    items.add(stockItem.withPos(colLeftX, row1Y));
                    if (hasRestock) {
                        items.add(restockItem.withPos(colRightX, row1Y));
                    }
                }
            }
        }

        return items;
    }

    private void renderOfferRow(GuiGraphics graphics, Context context, MarketplaceOffer offer, int x, int y,
            boolean isSelected) {
        MarketplaceOfferViewState viewState = MarketplaceClientState.offerViewState(offer.id());
        ItemStack[] payments = context.paymentNormalizer().apply(offer.effectivePayments());
        ItemStack[] originalPayments = context.paymentNormalizer().apply(offer.originalPayments());
        ItemStack p1 = payments[0];
        ItemStack p2 = payments[1];
        ItemStack origP1 = originalPayments[0];
        ItemStack origP2 = originalPayments[1];

        boolean offerAvailable = viewState.maxPurchasable() > 0;
        OfferTemplateButton rowButton = new OfferTemplateButton(x, y, ignored -> {
        });
        rowButton.update(p1, p2, origP1, origP2, offer.result(), offerAvailable, !offerAvailable);
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
        ItemStack[] originalPayments = context.paymentNormalizer().apply(offer.originalPayments());

        ItemStack p1 = payments[0];
        ItemStack p2 = payments[1];
        ItemStack origP1 = originalPayments[0];
        ItemStack origP2 = originalPayments[1];

        boolean hasDiscount1 = !origP1.isEmpty() && p1.getCount() != origP1.getCount();
        boolean hasDiscount2 = !origP2.isEmpty() && p2.getCount() != origP2.getCount();

        int payment1X = listStartX + (hasDiscount1 ? OfferTemplateButton.PAYMENT_1_X_OFFSET_DISCOUNTED
                : OfferTemplateButton.PAYMENT_1_X_OFFSET);
        int payment2X = listStartX + (hasDiscount2 ? OfferTemplateButton.PAYMENT_2_X_OFFSET_DISCOUNTED
                : OfferTemplateButton.PAYMENT_2_X_OFFSET);
        int resultX = listStartX + OfferTemplateButton.RESULT_X_OFFSET;

        int rowY = listStartY + (hoveredRow * context.rowHeight());
        int arrowX = listStartX + OfferTemplateButton.ARROW_X_OFFSET;
        int arrowY = rowY + OfferTemplateButton.ARROW_Y_OFFSET;

        renderRowTooltips(graphics, context, p1, p2, offer.result(), viewState, payment1X, payment2X, resultX, arrowX,
                arrowY);
    }

    private void renderRowTooltips(GuiGraphics graphics, Context context, ItemStack p1, ItemStack p2, ItemStack result,
            MarketplaceOfferViewState viewState,
            int p1X, int p2X, int resultX, int arrowX, int arrowY) {
        int mouseX = context.mouseX();
        int mouseY = context.mouseY();

        boolean hoveringArrow = mouseX >= arrowX - 1 && mouseX <= arrowX + 11 && mouseY >= arrowY - 1
                && mouseY <= arrowY + 10;

        if (mouseX >= p1X && mouseX <= p1X + 16 && !p1.isEmpty()) {
            graphics.renderTooltip(context.font(), p1, mouseX, mouseY);
        } else if (mouseX >= p2X && mouseX <= p2X + 16 && !p2.isEmpty()) {
            graphics.renderTooltip(context.font(), p2, mouseX, mouseY);
        } else if (mouseX >= resultX && mouseX <= resultX + 16 && !result.isEmpty()) {
            graphics.renderTooltip(context.font(), result, mouseX, mouseY);
        } else if (hoveringArrow && viewState.maxPurchasable() <= 0) {
            graphics.renderTooltip(context.font(), buildUnavailableOfferTooltip(context, viewState), mouseX, mouseY);
        }
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
            Function<MarketplaceOfferViewState, Optional<Integer>> displayRestockSecondsResolver) {
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
