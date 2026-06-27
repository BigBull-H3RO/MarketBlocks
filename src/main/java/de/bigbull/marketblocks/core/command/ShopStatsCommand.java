package de.bigbull.marketblocks.core.command;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.context.CommandContext;

import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOffer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Handles the {@code /marketblocks shop stats} and {@code /marketblocks marketplace stats} commands.
 * Displays a top-10 leaderboard sorted by total sales.
 */
public final class ShopStatsCommand {

    private ShopStatsCommand() {
    }

    /**
     * Builds the {@code stats} sub-node for {@code /marketblocks shop stats}.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildShopStats() {
        return Commands.literal("stats")
                .executes(ShopStatsCommand::executeShopStats);
    }

    /**
     * Builds the {@code stats} sub-node for {@code /marketblocks marketplace stats}.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildMarketplaceStats() {
        return Commands.literal("stats")
                .executes(ShopStatsCommand::executeMarketplaceStats);
    }

    private static int executeShopStats(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ShopDirectorySavedData data = ShopDirectorySavedData.get(source.getLevel());
        List<ShopDirectorySavedData.ShopEntry> shops = new ArrayList<>(data.getShops());
        shops.sort((a, b) -> Integer.compare(b.totalSales(), a.totalSales()));

        source.sendSuccess(
                () -> Component.translatable("command.marketblocks.stats.shop.header").withStyle(ChatFormatting.GOLD), false);
        int limit = Math.min(10, shops.size());
        if (limit == 0) {
            source.sendSuccess(() -> Component.translatable("command.marketblocks.stats.shop.empty"), false);
        } else {
            for (int i = 0; i < limit; i++) {
                ShopDirectorySavedData.ShopEntry shop = shops.get(i);
                String name = shop.shopName() != null && !shop.shopName().isEmpty() ? shop.shopName() : Component.translatable("command.marketblocks.stats.shop.unnamed").getString();
                int sales = shop.totalSales();
                final int rank = i + 1;
                source.sendSuccess(() -> Component.translatable("command.marketblocks.stats.shop.entry", rank, name, sales)
                        .withStyle(ChatFormatting.YELLOW), false);
            }
        }
        return 1;
    }

    private static int executeMarketplaceStats(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<MarketplaceOffer> offers = new ArrayList<>();
        MarketplaceManager.get().snapshot().pages().forEach(page -> offers.addAll(page.offers()));
        offers.sort(
                (a, b) -> Integer.compare(b.runtimeState().lifetimePurchases(), a.runtimeState().lifetimePurchases()));

        source.sendSuccess(
                () -> Component.translatable("command.marketblocks.stats.marketplace.header").withStyle(ChatFormatting.GOLD), false);
        int limit = Math.min(10, offers.size());
        if (limit == 0) {
            source.sendSuccess(() -> Component.translatable("command.marketblocks.stats.marketplace.empty"), false);
        } else {
            for (int i = 0; i < limit; i++) {
                MarketplaceOffer offer = offers.get(i);
                String resultName = offer.result().getHoverName().getString();
                int sales = offer.runtimeState().lifetimePurchases();
                final int rank = i + 1;
                source.sendSuccess(() -> Component.translatable("command.marketblocks.stats.marketplace.entry", rank, resultName, sales)
                        .withStyle(ChatFormatting.YELLOW), false);
            }
        }
        return 1;
    }
}
