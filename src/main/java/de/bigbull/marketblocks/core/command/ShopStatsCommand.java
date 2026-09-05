package de.bigbull.marketblocks.core.command;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOffer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Handles the {@code /marketblocks stats [shops|marketplace]} command.
 * Displays top-10 leaderboards sorted by total sales.
 */
public final class ShopStatsCommand {

    private ShopStatsCommand() {
    }

    /**
     * Builds the Brigadier command node for {@code /marketblocks stats [shops|marketplace]}.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("stats")
                .executes(context -> {
                    executeShopStats(context);
                    executeMarketplaceStats(context);
                    return 1;
                })
                .then(Commands.literal("shops")
                        .executes(ShopStatsCommand::executeShopStats))
                .then(Commands.literal("marketplace")
                        .executes(ShopStatsCommand::executeMarketplaceStats));
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
                String name = ShopDirectorySavedData.formatShopName(shop.shopName(), shop.shopId());
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
