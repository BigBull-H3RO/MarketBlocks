package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class MarketTopElement implements ITradeBookElement {

    @Override
    public boolean canHandle(String insertion) {
        return insertion != null && insertion.startsWith("MARKET_TOP_ENTRY||");
    }

    @Override
    public int getExtraHeight(String insertion) {
        return 25;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, String insertion, int startX, int startY, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        String[] parts = insertion.split("\\|\\|");
        if (parts.length < 7) return;

        int rankIndex = Integer.parseInt(parts[1]);
        String itemName = parts[2];
        int lifetimePurchases = Integer.parseInt(parts[3]);
        String offerId = parts[4];
        boolean saleActive = Boolean.parseBoolean(parts[5]);
        double salePercent = Double.parseDouble(parts[6]);

        boolean isTopThree = rankIndex < 3;
        String rankPrefix = switch (rankIndex) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> (rankIndex + 1) + ".";
        };

        float rankScale = isTopThree ? 1.4f : 1.0f;
        graphics.pose().pushMatrix();
        int yOffset = isTopThree ? -2 : 0;
        graphics.pose().translate(startX, startY + yOffset);
        graphics.pose().scale(rankScale, rankScale);
        graphics.text(context.getFont(), rankPrefix, 0, 0, isTopThree ? 0xFFFFAA00 : 0xFFAA00AA, false);
        graphics.pose().popMatrix();

        int rankWidth = (int) (context.getFont().width(rankPrefix) * rankScale);

        graphics.text(context.getFont(), itemName, startX + rankWidth + 4, startY, isTopThree ? 0xFFFFAA00 : 0xFFAA00AA, false);

        Component salesComp = Component.translatable("gui.marketblocks.trade_book.marketplace.sales", lifetimePurchases);
        int salesWidth = context.getFont().width(salesComp);
        int rightX = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) - salesWidth;
        graphics.text(context.getFont(), salesComp.getVisualOrderText(), rightX, startY, 0xFF000000, false);

        var offer = context.getOffers().get(offerId);
        if (offer != null) {
            int offerWidth = 94;
            int centerX = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) / 2;
            int offerX = centerX - (int) ((offerWidth * 0.5f) / scale / 2);
            int offerY = startY + 16;

            graphics.pose().pushMatrix();
            float relScale = 0.5f / scale;
            graphics.pose().scale(relScale, relScale);
            TradeBookLayoutUtils.renderInlineOffer(graphics, offer, (int) (offerX / relScale), (int) (offerY / relScale), "OK", mouseX, mouseY, 0.5f, context);
            graphics.pose().popMatrix();

            if (saleActive) {
                String saleText = (salePercent < 0 ? "" : "+") + (int) salePercent + "%";
                Component saleComp = Component.translatable("gui.marketblocks.trade_book.marketplace.sale_active", saleText);
                int saleWidth = context.getFont().width(saleComp);
                graphics.text(context.getFont(), saleComp.getVisualOrderText(), centerX - saleWidth / 2, offerY + 14, 0xFF55FF55, false);
            }
        }

        if (startY < 170) {
            int lineY = startY + 28;
            graphics.fill(startX, lineY, startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale), lineY + 1, 0x33000000);
        }
    }
}
