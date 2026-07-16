package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.InteractiveZone;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class TrendElement implements ITradeBookElement {

    @Override
    public boolean canHandle(String insertion) {
        return insertion != null && insertion.startsWith("TREND_ENTRY||");
    }

    @Override
    public int getExtraHeight(String insertion) {
        return 12;
    }

    @Override
    public void render(GuiGraphics graphics, String insertion, int startX, int startY, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        String[] parts = insertion.split("\\|\\|");
        if (parts.length < 7) return;

        String sign = parts[1];
        String itemName = parts[2];
        String percentText = parts[3] + "%";
        int color = Integer.parseInt(parts[4]);
        long roundBase = Long.parseLong(parts[5]);
        long roundCurrent = Long.parseLong(parts[6]);

        graphics.drawString(context.getFont(), sign, startX, startY, color, false);
        graphics.drawString(context.getFont(), itemName + ":", startX + 10, startY, 0x000000, false);

        int percentWidth = context.getFont().width(percentText);
        int rightX = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale) - percentWidth;
        graphics.drawString(context.getFont(), percentText, rightX, startY, color, false);

        int scaledX = (int) (rightX * scale);
        int scaledY = (int) (startY * scale);
        int scaledW = (int) (percentWidth * scale);
        context.addActiveZone(new InteractiveZone(scaledX, scaledY, scaledW, 9, () -> {
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), Component.translatable("gui.marketblocks.trade_book.trends.hover", roundBase, roundCurrent), mouseX, mouseY));
        }, null));
    }
}
