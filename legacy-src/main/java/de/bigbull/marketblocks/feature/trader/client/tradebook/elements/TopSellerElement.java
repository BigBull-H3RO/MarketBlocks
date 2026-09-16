package de.bigbull.marketblocks.feature.trader.client.tradebook.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;

public class TopSellerElement implements ITradeBookElement {

    @Override
    public boolean canHandle(String insertion) {
        return insertion != null && insertion.startsWith("TOP_SELLER_ENTRY||");
    }

    @Override
    public int getExtraHeight(String insertion) {
        return 14;
    }

    @Override
    public void render(GuiGraphics graphics, String insertion, int startX, int startY, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        String[] parts = insertion.split("\\|\\|");
        if (parts.length < 5) return;

        int rankIndex = Integer.parseInt(parts[1]);
        String playerNameFull = parts[2];
        String shops = parts[3];
        String sales = parts[4];

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
        graphics.drawString(context.getFont(), rankPrefix, 0, 0, isTopThree ? 0xFFAA00 : 0x0000AA, false);
        graphics.pose().popPose();

        int rankWidth = (int) (context.getFont().width(rankPrefix) * rankScale);

        TradeBookLayoutUtils.renderPlayerHead(graphics, playerNameFull, startX + rankWidth + 2, startY - 1, scale, 8);

        String displayName = TradeBookLayoutUtils.truncate(playerNameFull, 12);
        graphics.drawString(context.getFont(), displayName, startX + rankWidth + 14, startY, isTopThree ? 0xFFAA00 : 0x0000AA, false);

        Component statsComp = Component.translatable("gui.marketblocks.trade_book.shops.player_stats", shops, sales);

        int rightMargin = startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale);
        int salesWidth = context.getFont().width(sales);
        int salesX = rightMargin - salesWidth;
        graphics.drawString(context.getFont(), sales, salesX, startY, 0x555555, false);

        int salesIconWidth = 10;
        int salesIconX = salesX - salesIconWidth;
        TradeBookLayoutUtils.renderScaledIcon(graphics, new ItemStack(Items.EMERALD), salesIconX - 1, startY - 1, 0.5f / scale);

        int spacer = 5;
        int shopsWidth = context.getFont().width(shops);
        int shopsX = salesIconX - spacer - shopsWidth;
        graphics.drawString(context.getFont(), shops, shopsX, startY, 0x555555, false);

        int shopsIconWidth = 10;
        int shopsIconX = shopsX - shopsIconWidth;
        TradeBookLayoutUtils.renderScaledIcon(graphics, new ItemStack(RegistriesInit.TRADE_STAND_BLOCK.get()), shopsIconX - 1, startY - 1, 0.5f / scale);

        double scaledMouseX = mouseX / scale;
        double scaledMouseY = mouseY / scale;

        if (scaledMouseX >= shopsIconX && scaledMouseX <= rightMargin && scaledMouseY >= startY && scaledMouseY <= startY + 12) {
            context.setNextHoveredObject("stats_" + playerNameFull);
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), statsComp, mouseX, mouseY));
        }

        if (startY < 170) {
            int lineY = startY + 12;
            graphics.fill(startX, lineY, startX + (int) (TradeBookLayoutUtils.TEXT_WIDTH / scale), lineY + 1, 0x33000000);
        }
    }
}
