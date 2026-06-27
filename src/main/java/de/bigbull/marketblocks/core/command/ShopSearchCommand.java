package de.bigbull.marketblocks.core.command;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;

/**
 * Handles the {@code /marketblocks search <item> [page]} command.
 * Searches both SingleOfferShops and Marketplace links for a given item.
 */
public final class ShopSearchCommand {

    private ShopSearchCommand() {
    }

    /**
     * Builds the Brigadier command node for {@code search}.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> build(
            net.minecraft.commands.CommandBuildContext buildContext) {
        return Commands.literal("search")
                .then(Commands.argument("item", ItemArgument.item(buildContext))
                        .executes(c -> executeSearch(c, ItemArgument.getItem(c, "item").getItem(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(c -> executeSearch(c, ItemArgument.getItem(c, "item").getItem(),
                                        IntegerArgumentType.getInteger(c, "page")))));
    }

    private static int executeSearch(CommandContext<CommandSourceStack> context, Item item, int page) {
        CommandSourceStack source = context.getSource();

        ShopDirectorySavedData data = ShopDirectorySavedData.get(source.getLevel());
        List<ShopDirectorySavedData.ShopEntry> shops = data.getShops().stream()
                .filter(s -> s.result() != null && s.result().is(item))
                .toList();

        MarketplaceLinkSavedData marketplaceData = MarketplaceLinkSavedData.get(source.getLevel());
        boolean marketplaceHasItem = MarketplaceManager.get().snapshot().pages().stream()
                .flatMap(p -> p.offers().stream())
                .anyMatch(o -> o.result() != null && o.result().is(item));

        List<MarketplaceLinkSavedData.LinkInfo> marketplaces = marketplaceHasItem
                ? new ArrayList<>(marketplaceData.getLinkedBlocks().values())
                : List.of();

        if (shops.isEmpty() && marketplaces.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.marketblocks.search.no_shops",
                    Component.translatable(item.getDescriptionId())), false);
            return 1;
        }

        String baseCmd = "/marketblocks search "
                + BuiltInRegistries.ITEM.getKey(item).toString() + " ";

        displayPaginatedSearchResults(source, shops, marketplaces, page, baseCmd, item);

        return 1;
    }

    private static void displayPaginatedSearchResults(CommandSourceStack source,
            List<ShopDirectorySavedData.ShopEntry> shops, List<MarketplaceLinkSavedData.LinkInfo> marketplaces,
            int page, String baseCmd, Item item) {
        int totalItems = shops.size() + marketplaces.size();
        int totalPages = CommandUtils.totalPages(totalItems);
        page = CommandUtils.clampPage(page, totalPages);

        final int currentPage = page;
        final int finalTotalPages = totalPages;
        source.sendSuccess(
                () -> Component
                        .translatable("command.marketblocks.search.header",
                                Component.translatable(item.getDescriptionId()), currentPage, finalTotalPages)
                        .withStyle(ChatFormatting.GOLD),
                false);

        int start = (page - 1) * CommandUtils.ITEMS_PER_PAGE;
        int end = Math.min(start + CommandUtils.ITEMS_PER_PAGE, totalItems);

        for (int i = start; i < end; i++) {
            if (i < shops.size()) {
                ShopDirectorySavedData.ShopEntry shop = shops.get(i);
                String shopName = shop.shopName();
                if (shopName == null || shopName.isEmpty())
                    shopName = "Unnamed Shop";
                String owner = shop.ownerName() != null ? shop.ownerName() : "Unknown";

                Component status = shop.isClosed()
                        ? Component.translatable("command.marketblocks.shoplist.closed").withStyle(ChatFormatting.RED)
                        : Component.translatable("command.marketblocks.shoplist.open").withStyle(ChatFormatting.GREEN);

                MutableComponent hoverText = Component
                        .translatable("command.marketblocks.shoplist.hover.shop", shopName).append("\n")
                        .append(Component.translatable("command.marketblocks.shoplist.hover.owner", owner)).append("\n")
                        .append(Component.translatable("command.marketblocks.shoplist.hover.status", status));

                if (shop.result() != null && !shop.result().isEmpty()) {
                    hoverText.append("\n-------------------\n")
                            .append(Component.translatable("command.marketblocks.shoplist.hover.offer")
                                    .withStyle(ChatFormatting.YELLOW))
                            .append("\n");

                    if (shop.payment1() != null && !shop.payment1().isEmpty()) {
                        hoverText.append(Component.literal(shop.payment1().getCount() + "x ")
                                .append(shop.payment1().getHoverName()));
                    }
                    if (shop.payment2() != null && !shop.payment2().isEmpty()) {
                        hoverText.append(Component.literal(" + " + shop.payment2().getCount() + "x ")
                                .append(shop.payment2().getHoverName()));
                    }
                    hoverText.append(Component.literal(" ")
                            .append(Component.translatable("command.marketblocks.shoplist.hover.arrow"))
                            .append(" " + shop.result().getCount() + "x ").append(shop.result().getHoverName()));
                }

                MutableComponent text = Component
                        .translatable("command.marketblocks.shoplist.entry", status, shopName, owner)
                        .withStyle(
                                style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));

                CommandUtils.appendWaypointsAndTp(source, text, shop.pos(), shopName);
                source.sendSuccess(() -> text, false);
            } else {
                int mpIndex = i - shops.size();
                MarketplaceLinkSavedData.LinkInfo info = marketplaces.get(mpIndex);
                GlobalPos pos = info.blockPos;
                String name = info.name != null && !info.name.isEmpty() ? info.name
                        : pos.dimension().location().toString();

                MutableComponent text = Component
                        .translatable("command.marketblocks.marketplacelist.entry", (i + 1), name)
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("container.marketblocks.marketplace"))));

                CommandUtils.appendWaypointsAndTp(source, text, pos, name);
                source.sendSuccess(() -> text, false);
            }
        }

        CommandUtils.sendPaginationFooter(source, page, totalPages, baseCmd);
    }
}
