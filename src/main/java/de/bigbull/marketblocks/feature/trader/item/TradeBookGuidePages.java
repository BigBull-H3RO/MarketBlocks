package de.bigbull.marketblocks.feature.trader.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class TradeBookGuidePages {

    public static void addGuidePages(List<Component> pages) {
        addIntroAndShopTypes(pages);
    }

    public static void addIntroAndShopTypes(List<Component> pages) {
        // Introduction
        MutableComponent introPage = Component.translatable("gui.marketblocks.trade_book.guide.intro.title")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("\n\n"))
                .append(Component.translatable("gui.marketblocks.trade_book.guide.intro.text")
                        .withStyle(ChatFormatting.BLACK));
        pages.add(introPage);

        // Shop Types - Trade Stand (text + recipe on same page)
        MutableComponent tradeStandPage = Component.translatable("gui.marketblocks.trade_book.guide.tradestand.title")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("gui.marketblocks.trade_book.guide.tradestand.text")
                        .withStyle(ChatFormatting.BLACK))
                .append(Component.literal("\n  ").withStyle(style -> style.withInsertion("RECIPE:TRADESTAND")));
        pages.add(tradeStandPage);

        // Shop Types - Market Crate (text + recipe on same page)
        MutableComponent marketCratePage = Component.translatable("gui.marketblocks.trade_book.guide.marketcrate.title")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("gui.marketblocks.trade_book.guide.marketcrate.text")
                        .withStyle(ChatFormatting.BLACK))
                .append(Component.literal("\n  ").withStyle(style -> style.withInsertion("RECIPE:MARKETCRATE")));
        pages.add(marketCratePage);
    }
}
