package de.bigbull.marketblocks.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.singleoffer.settings.ShopCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Handles the {@code /marketblocks shop list} command with pagination and filtering
 * by name, owner, and category.
 */
public final class ShopListCommand {

    private ShopListCommand() {
    }

    /**
     * Builds the Brigadier command node for {@code shop list}.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("shop")
                .then(Commands.literal("list")
                        .executes(c -> executeList(c, 1, null, null, null))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(c -> executeList(c, IntegerArgumentType.getInteger(c, "page"), null, null,
                                        null)))
                        .then(Commands.literal("owner")
                                .then(Commands.argument("player", StringArgumentType.string())
                                        .executes(c -> executeList(c, 1, null,
                                                StringArgumentType.getString(c, "player"), null))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(c -> executeList(c,
                                                        IntegerArgumentType.getInteger(c, "page"), null,
                                                        StringArgumentType.getString(c, "player"), null)))))
                        .then(Commands.literal("name")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(c -> executeList(c, 1,
                                                StringArgumentType.getString(c, "name"), null, null))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(c -> executeList(c,
                                                        IntegerArgumentType.getInteger(c, "page"),
                                                        StringArgumentType.getString(c, "name"), null, null)))))
                        .then(Commands.literal("category")
                                .then(Commands.argument("category", StringArgumentType.string())
                                        .executes(c -> executeList(c, 1, null, null,
                                                StringArgumentType.getString(c, "category")))
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(c -> executeList(c,
                                                        IntegerArgumentType.getInteger(c, "page"), null, null,
                                                        StringArgumentType.getString(c, "category")))))))
                .then(ShopStatsCommand.buildShopStats());
    }

    private static int executeList(CommandContext<CommandSourceStack> context, int page, String nameFilter,
            String ownerFilter, String categoryFilter) {
        CommandSourceStack source = context.getSource();
        ShopDirectorySavedData data = ShopDirectorySavedData.get(source.getLevel());
        List<ShopDirectorySavedData.ShopEntry> shops = new ArrayList<>(data.getShops());

        if (nameFilter != null) {
            String lowerName = nameFilter.toLowerCase(Locale.ROOT);
            shops = shops.stream()
                    .filter(s -> s.shopName() != null && s.shopName().toLowerCase(Locale.ROOT).contains(lowerName))
                    .toList();
        }
        if (ownerFilter != null) {
            String lowerOwner = ownerFilter.toLowerCase(Locale.ROOT);
            shops = shops.stream()
                    .filter(s -> s.ownerName() != null && s.ownerName().toLowerCase(Locale.ROOT).contains(lowerOwner))
                    .toList();
        }
        if (categoryFilter != null) {
            String lowerCategory = categoryFilter.toLowerCase(Locale.ROOT);
            shops = shops.stream()
                    .filter(s -> s.shopCategory() != null && s.shopCategory().getId().equals(lowerCategory))
                    .toList();
        }

        if (shops.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.marketblocks.shoplist.no_shops"), false);
            return 1;
        }

        String baseCmd = "/marketblocks shop list";
        if (nameFilter != null)
            baseCmd += " name " + nameFilter;
        if (ownerFilter != null)
            baseCmd += " owner " + ownerFilter;
        if (categoryFilter != null)
            baseCmd += " category " + categoryFilter;
        baseCmd += " ";

        if (nameFilter == null && ownerFilter == null && categoryFilter == null) {
            MutableComponent categoryHeader = Component.literal(" ");
            ShopCategory[] categories = ShopCategory.values();
            boolean first = true;
            for (ShopCategory cat : categories) {
                if (cat == ShopCategory.NONE) continue;
                if (!first) {
                    categoryHeader.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY));
                }
                String catId = cat.getId();
                Component catComp = Component.literal("[")
                        .append(Component.translatable("gui.marketblocks.category." + catId)).append("]")
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/marketblocks shop list category " + catId))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gui.marketblocks.category.tooltip"))));
                categoryHeader.append(catComp);
                first = false;
            }
            source.sendSuccess(() -> categoryHeader, false);
        }

        displayPaginatedShops(source, shops, page, baseCmd, "command.marketblocks.list.page_header");

        return 1;
    }

    /**
     * Renders a paginated list of shops with hover-previews and action buttons.
     * Also used by {@link ShopSearchCommand}.
     */
    public static void displayPaginatedShops(CommandSourceStack source, List<ShopDirectorySavedData.ShopEntry> shops,
            int page, String baseCmd, String headerKey) {
        int totalPages = CommandUtils.totalPages(shops.size());
        page = CommandUtils.clampPage(page, totalPages);

        final int currentPage = page;
        final int finalTotalPages = totalPages;
        source.sendSuccess(
                () -> Component.translatable(headerKey, currentPage, finalTotalPages).withStyle(ChatFormatting.GOLD),
                false);

        int start = (page - 1) * CommandUtils.ITEMS_PER_PAGE;
        int end = Math.min(start + CommandUtils.ITEMS_PER_PAGE, shops.size());

        for (int i = start; i < end; i++) {
            ShopDirectorySavedData.ShopEntry shop = shops.get(i);
            String shopName = shop.shopName();
            if (shopName == null || shopName.isEmpty())
                shopName = "Unnamed Shop";
            String owner = shop.ownerName() != null ? shop.ownerName() : "Unknown";

            Component status = shop.isClosed()
                    ? Component.translatable("command.marketblocks.shoplist.closed").withStyle(ChatFormatting.RED)
                    : Component.translatable("command.marketblocks.shoplist.open").withStyle(ChatFormatting.GREEN);

            MutableComponent hoverText = Component.translatable("command.marketblocks.shoplist.hover.shop", shopName)
                    .append("\n")
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
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));

            CommandUtils.appendWaypointsAndTp(source, text, shop.pos(), shopName);
            source.sendSuccess(() -> text, false);
        }

        CommandUtils.sendPaginationFooter(source, page, totalPages, baseCmd);
    }
}
