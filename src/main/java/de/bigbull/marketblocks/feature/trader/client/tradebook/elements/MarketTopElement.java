package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics graphics, String insertion, int startX, int startY, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
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
        graphics.pose().pushPose();
        int yOffset = isTopThree ? -2 : 0;
        graphics.pose().translate(startX, startY + yOffset, 0);
        graphics.pose().scale(rankScale, rankScale, 1.0f);
        graphics.drawString(context.getFont(), rankPrefix, 0, 0, isTopThree ? 0xFFAA00 : 0xAA00AA, false);
        graphics.pose().popPose();

        int rankWidth = (int) (context.getFont().width(rankPrefix) * rankScale);

        graphics.drawString(context.getFont(), itemName, startX + rankWidth + 4, startY, isTopThree ? 0xFFAA00 : 0xAA00AA, false);

        Component salesComp = Component.translatable("gui.marketblocks.trade_book.marketplace.sales", lifetimePurchases);
        int salesWidth = context.getFont().width(salesComp);
        int rightX = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) - salesWidth;
        graphics.drawString(context.getFont(), salesComp.getVisualOrderText(), rightX, startY, 0x000000, false);

        var offer = context.getOffers().get(offerId);
        if (offer != null) {
            int offerWidth = 94;
            int centerX = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) / 2;
            int offerX = centerX - (int) ((offerWidth * 0.5f) / scale / 2);
            int offerY = startY + 16;

            graphics.pose().pushPose();
            float relScale = 0.5f / scale;
            graphics.pose().scale(relScale, relScale, 1.0f);
            TradeBookLayoutUtils.renderInlineOffer(graphics, offer, (int) (offerX / relScale), (int) (offerY / relScale), "OK", mouseX, mouseY, 0.5f, context);
            graphics.pose().popPose();

            if (saleActive) {
                String saleText = (salePercent < 0 ? "" : "+") + (int) salePercent + "%";
                Component saleComp = Component.translatable("gui.marketblocks.trade_book.marketplace.sale_active", saleText);
                int saleWidth = context.getFont().width(saleComp);
                graphics.drawString(context.getFont(), saleComp.getVisualOrderText(), centerX - saleWidth / 2, offerY + 14, 0x55FF55, false);
            }
        }

        if (startY < 170) {
            int lineY = startY + 28;
            graphics.fill(startX, lineY, startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale), lineY + 1, 0x33000000);
        }
    }
}
