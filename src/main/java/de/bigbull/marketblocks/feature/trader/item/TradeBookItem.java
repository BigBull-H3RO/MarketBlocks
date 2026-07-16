package de.bigbull.marketblocks.feature.trader.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOffer;
import de.bigbull.marketblocks.feature.trader.data.NpcEconomySavedData;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket;
import de.bigbull.marketblocks.network.NetworkHandler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TradeBookItem extends Item {
        public TradeBookItem(Properties properties) {
                super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
                ItemStack itemStack = player.getItemInHand(hand);
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                        Map<String, TradeBookOpenPacket.ShopOfferData> offers = new HashMap<>();
                        List<Component> pages = createDynamicPages(serverPlayer, offers);
                        NetworkHandler.sendToPlayer(serverPlayer, new TradeBookOpenPacket(pages, offers));
                        return InteractionResultHolder.success(itemStack);
                }
                return InteractionResultHolder.consume(itemStack);
        }

        private List<Component> createDynamicPages(ServerPlayer player,
                        Map<String, TradeBookOpenPacket.ShopOfferData> offers) {
                List<Component> pages = new ArrayList<>();

                // === Section 0: Guide ===
                int guideIntroStart = pages.size() + 2; // +2 because TOC will be inserted at index 0
                TradeBookGuidePages.addIntroAndShopTypes(pages);

                // Collect all shops (including those without offers, so "My Shops" can show
                // them)
                ShopDirectorySavedData shopData = ShopDirectorySavedData.get(player.serverLevel());
                List<ShopDirectorySavedData.ShopEntry> allShops = shopData.getShops();

                // Filter out closed shops for active listings and shops without offers
                List<ShopDirectorySavedData.ShopEntry> openShops = allShops.stream()
                                .filter(s -> !s.isClosed())
                                .filter(s -> !s.result().isEmpty())
                                .toList();

                // Leaderboard will be computed inside addShopLeaderboardPages
                List<ShopDirectorySavedData.ShopEntry> leaderboardShops = new ArrayList<>(openShops);

                boolean canTeleport = Config.ALLOW_NON_OP_TELEPORT.get() || player.hasPermissions(2);
                UUID playerUUID = player.getUUID();

                // === Section 1: My Shops (personal stats) ===
                int myShopsStartPage = pages.size() + 2;
                addMyShopsPages(pages, allShops, playerUUID, canTeleport, offers);

                // Ensure Shop Directory starts on a left page (even index in final book)
                // finalPages will have TOC at index 0. To make Shop Directory even,
                // pages.size() must be odd.
                if (pages.size() % 2 == 0) {
                        pages.add(Component.literal(""));
                }

                // === Section 3: Shop Directory (all open shops) ===
                int shopDirStartPage = pages.size() + 2;
                addShopDirectoryPages(pages, openShops, canTeleport, offers);

                // === Section 4: Shop Leaderboard ===
                int shopLeaderStartPage = pages.size() + 2;
                addShopLeaderboardPages(pages, leaderboardShops);

                // === Section 5: Marketplace Leaderboard ===
                int marketLeaderStartPage = pages.size() + 2;
                addMarketplaceLeaderboardPages(pages, offers);

                // === Section 6: NPC Trends ===
                int trendsStartPage = pages.size() + 2;
                addNpcTrendsPages(pages, player);

                MutableComponent toc = Component.translatable("gui.marketblocks.trade_book.toc.header")
                                .withStyle(style -> style.withColor(ChatFormatting.GOLD)
                                                .withInsertion("PAGE_SCALE:0.75"))
                                .append(Component.translatable("gui.marketblocks.trade_book.toc.subheader")
                                                .withStyle(ChatFormatting.BLACK, ChatFormatting.ITALIC));

                // Guide entries
                toc.append(Component.literal("   ").withStyle(s -> s.withInsertion("TOC_ICON:intro")))
                                .append(buildTocEntry(
                                                "gui.marketblocks.trade_book.toc.guide.intro",
                                                "gui.marketblocks.trade_book.toc.guide.intro.tooltip",
                                                guideIntroStart, ChatFormatting.DARK_GREEN));

                // My Shops entry
                toc.append(Component.literal("   ").withStyle(s -> s.withInsertion("TOC_ICON:my_shops")))
                                .append(buildTocEntry(
                                                "gui.marketblocks.trade_book.toc.my_shops",
                                                "gui.marketblocks.trade_book.toc.my_shops.tooltip",
                                                myShopsStartPage, ChatFormatting.DARK_AQUA));

                // Shop Directory entry
                toc.append(Component.literal("   ").withStyle(s -> s.withInsertion("TOC_ICON:active_shops")))
                                .append(buildTocEntry(
                                                "gui.marketblocks.trade_book.toc.active_shops",
                                                "gui.marketblocks.trade_book.toc.active_shops.tooltip",
                                                shopDirStartPage, ChatFormatting.BLUE));

                // Shop Leaderboard entry
                toc.append(Component.literal("   ").withStyle(s -> s.withInsertion("TOC_ICON:shop_stats")))
                                .append(buildTocEntry(
                                                "gui.marketblocks.trade_book.toc.shop_stats",
                                                "gui.marketblocks.trade_book.toc.shop_stats.tooltip",
                                                shopLeaderStartPage, ChatFormatting.DARK_BLUE));

                // Marketplace Leaderboard entry
                toc.append(Component.literal("   ").withStyle(s -> s.withInsertion("TOC_ICON:market_stats")))
                                .append(buildTocEntry(
                                                "gui.marketblocks.trade_book.toc.market_stats",
                                                "gui.marketblocks.trade_book.toc.market_stats.tooltip",
                                                marketLeaderStartPage, ChatFormatting.DARK_PURPLE));

                // NPC Trends entry
                toc.append(Component.literal("   ").withStyle(s -> s.withInsertion("TOC_ICON:trends")))
                                .append(buildTocEntry(
                                                "gui.marketblocks.trade_book.toc.trends",
                                                "gui.marketblocks.trade_book.toc.trends.tooltip",
                                                trendsStartPage, ChatFormatting.DARK_RED));

                pages.add(0, toc);
                return pages;
        }

        // ==========================================
        // TOC Helper
        // ==========================================

        private MutableComponent buildTocEntry(String textKey, String tooltipKey, int pageNum, ChatFormatting color) {
                return Component.translatable(textKey)
                                .withStyle(style -> style.withColor(color)
                                                .withUnderlined(true)
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE,
                                                                String.valueOf(pageNum)))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.translatable(tooltipKey))))
                                .append(Component.literal(" [" + pageNum + "]")
                                                .withStyle(s -> s.withColor(ChatFormatting.GRAY)
                                                                .withUnderlined(false)
                                                                .withClickEvent(new ClickEvent(
                                                                                ClickEvent.Action.CHANGE_PAGE,
                                                                                String.valueOf(pageNum)))
                                                                .withInsertion("SPACING:4")))
                                .append(Component.literal("\n"));
        }

        // ==========================================
        // Section 2: My Shops (personal)
        // ==========================================

        private void addMyShopsPages(List<Component> pages, List<ShopDirectorySavedData.ShopEntry> allShops,
                        UUID playerUUID, boolean canTeleport, Map<String, TradeBookOpenPacket.ShopOfferData> offers) {
                List<ShopDirectorySavedData.ShopEntry> myShops = allShops.stream()
                                .filter(s -> playerUUID.equals(s.ownerUUID()))
                                .toList();

                if (myShops.isEmpty()) {
                        MutableComponent emptyPage = Component
                                        .translatable("gui.marketblocks.trade_book.my_shops.title")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("gui.marketblocks.trade_book.my_shops.empty")
                                                        .withStyle(ChatFormatting.GRAY));
                        pages.add(emptyPage);
                        return;
                }

                MutableComponent page = Component.translatable("gui.marketblocks.trade_book.my_shops.title")
                                .withStyle(ChatFormatting.GOLD);

                for (ShopDirectorySavedData.ShopEntry shop : myShops) {
                        renderShopEntry(page, shop, canTeleport, offers, "my_shop");
                }
                pages.add(page);
        }

        // ==========================================
        // Section 3: Shop Directory (All Shops)
        // ==========================================

        private void addShopDirectoryPages(List<Component> pages, List<ShopDirectorySavedData.ShopEntry> openShops,
                        boolean canTeleport, Map<String, TradeBookOpenPacket.ShopOfferData> offers) {

                if (openShops.isEmpty()) {
                        MutableComponent emptyPage = Component
                                        .translatable("gui.marketblocks.trade_book.active.title")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("gui.marketblocks.trade_book.shops.empty")
                                                        .withStyle(ChatFormatting.GRAY));
                        pages.add(emptyPage);
                        return;
                }

                MutableComponent page = Component.translatable("gui.marketblocks.trade_book.active.title")
                                .withStyle(ChatFormatting.GOLD);

                for (ShopDirectorySavedData.ShopEntry shop : openShops) {
                        renderShopEntry(page, shop, canTeleport, offers, "dir_shop");
                }
                pages.add(page);
        }

        private void renderShopEntry(MutableComponent page, ShopDirectorySavedData.ShopEntry shop, boolean canTeleport,
                        Map<String, TradeBookOpenPacket.ShopOfferData> offers, String prefix) {
                String shopNameFull = ShopDirectorySavedData.formatShopName(shop.shopName(), shop.shopId());
                String ownerFull = shop.ownerName() != null && !shop.ownerName().isEmpty()
                                ? shop.ownerName()
                                : Component.translatable("gui.marketblocks.trade_book.active.unknown_owner")
                                                .getString();
                String compactSales = formatCompactNumber(shop.totalSales());

                boolean isClosed = shop.isClosed();

                String offerId = "NO_OFFER";
                String finalStatus = "NO_OFFER";

                if (!shop.result().isEmpty()) {
                        offerId = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
                        offers.put(offerId, new TradeBookOpenPacket.ShopOfferData(shop.payment1().copy(),
                                        shop.payment2().copy(), shop.result().copy()));
                        finalStatus = "OK";
                        if (shop.isOutOfStock())
                                finalStatus = "OUT_OF_STOCK";
                        else if (shop.isOutputFull())
                                finalStatus = "OUTPUT_FULL";
                }

                net.minecraft.core.BlockPos pos = shop.pos().pos();
                String dim = shop.pos().dimension().location().toString();

                // Construct the marker. We use "||" as delimiter.
                // Format:
                // SHOP_ENTRY||prefix||shopNameFull||ownerFull||compactSales||isClosed||offerId||finalStatus||x||y||z||dim||canTeleport
                String marker = String.join("||",
                                "SHOP_ENTRY",
                                prefix,
                                shopNameFull,
                                ownerFull,
                                compactSales,
                                String.valueOf(isClosed),
                                offerId,
                                finalStatus,
                                String.valueOf(pos.getX()),
                                String.valueOf(pos.getY()),
                                String.valueOf(pos.getZ()),
                                dim,
                                String.valueOf(canTeleport));

                page.append(Component.literal("\u00A0") // Non-breaking space
                                .withStyle(style -> style.withInsertion("BLOCK_START")));
                page.append(Component.literal("\u00A0") // Non-breaking space
                                .withStyle(style -> style.withInsertion(marker)));
                page.append(Component.literal("\u00A0\n") // Non-breaking space + newline
                                .withStyle(style -> style.withInsertion("DIVIDER")));
        }

        // ==========================================
        // Section 4: Shop Leaderboard
        // ==========================================

        private static class PlayerSales {
                String name;
                int sales;
                int shopCount;

                public PlayerSales(String name, int sales) {
                        this.name = name;
                        this.sales = sales;
                        this.shopCount = 0;
                }

                public void addSales(int amount) {
                        this.sales += amount;
                }

                public void incrementShopCount() {
                        this.shopCount++;
                }
        }

        private void addShopLeaderboardPages(List<Component> pages, List<ShopDirectorySavedData.ShopEntry> openShops) {
                int maxEntries = 20;

                Map<UUID, PlayerSales> salesMap = new HashMap<>();
                for (ShopDirectorySavedData.ShopEntry shop : openShops) {
                        UUID ownerId = shop.ownerUUID();
                        if (ownerId == null)
                                continue;
                        String ownerName = shop.ownerName() != null && !shop.ownerName().isEmpty() ? shop.ownerName()
                                        : "Unknown";
                        salesMap.computeIfAbsent(ownerId, id -> new PlayerSales(ownerName, 0))
                                        .addSales(shop.totalSales());
                        salesMap.get(ownerId).name = ownerName; // Update to most recent name
                        salesMap.get(ownerId).incrementShopCount();
                }

                List<PlayerSales> sortedPlayers = new ArrayList<>(salesMap.values());
                sortedPlayers.sort((a, b) -> Integer.compare(b.sales, a.sales));

                if (sortedPlayers.isEmpty()) {
                        MutableComponent emptyPage = Component.translatable("gui.marketblocks.trade_book.shops.title")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("gui.marketblocks.trade_book.shops.empty")
                                                        .withStyle(ChatFormatting.GRAY));
                        pages.add(emptyPage);
                        return;
                }

                int totalEntries = Math.min(maxEntries, sortedPlayers.size());
                MutableComponent page = Component.translatable("gui.marketblocks.trade_book.shops.title")
                                .withStyle(style -> style.withColor(ChatFormatting.GOLD)
                                                .withInsertion("PAGE_SCALE:0.75"));

                for (int j = 0; j < totalEntries; j++) {
                        PlayerSales player = sortedPlayers.get(j);
                        String displayNameFull = player.name;
                        String displayName = displayNameFull;
                        if (displayName.length() > 12)
                                displayName = displayName.substring(0, 10) + "..";
                        String compactShops = formatCompactNumber(player.shopCount);
                        String compactSales = formatCompactNumber(player.sales);
                        String payload = String.format("TOP_SELLER_ENTRY||%d||%s||%s||%s",
                                        j, displayNameFull, compactShops, compactSales);
                        page.append(Component.literal("\u00A0\n")
                                        .withStyle(style -> style.withInsertion(payload)));
                }
                pages.add(page);
        }

        // ==========================================
        // Section 5: Marketplace Leaderboard
        // ==========================================

        private void addMarketplaceLeaderboardPages(List<Component> pages,
                        Map<String, TradeBookOpenPacket.ShopOfferData> offers) {
                int maxEntries = 10;

                List<MarketplaceOffer> marketOffers = new ArrayList<>();
                MarketplaceManager.get().snapshot().pages().forEach(p -> {
                        for (MarketplaceOffer offer : p.offers()) {
                                if (offer.runtimeState().lifetimePurchases() > 0) {
                                        marketOffers.add(offer);
                                }
                        }
                });
                marketOffers.sort((a, b) -> Integer.compare(b.runtimeState().lifetimePurchases(),
                                a.runtimeState().lifetimePurchases()));

                if (marketOffers.isEmpty()) {
                        MutableComponent emptyPage = Component
                                        .translatable("gui.marketblocks.trade_book.marketplace.title")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("gui.marketblocks.trade_book.marketplace.empty")
                                                        .withStyle(ChatFormatting.GRAY));
                        pages.add(emptyPage);
                        return;
                }

                int totalEntries = Math.min(maxEntries, marketOffers.size());
                MutableComponent page = Component.translatable("gui.marketblocks.trade_book.marketplace.title")
                                .withStyle(ChatFormatting.GOLD);

                for (int j = 0; j < totalEntries; j++) {
                        MarketplaceOffer offer = marketOffers.get(j);
                        String resultName = offer.result().getHoverName().getString();
                        if (resultName.length() > 12)
                                resultName = resultName.substring(0, 10) + "..";

                        String offerId = "market_leader_" + UUID.randomUUID().toString().substring(0, 8);
                        List<ItemStack> payments = offer.effectivePayments();
                        ItemStack p1 = payments.size() > 0 ? payments.get(0) : ItemStack.EMPTY;
                        ItemStack p2 = payments.size() > 1 ? payments.get(1) : ItemStack.EMPTY;
                        offers.put(offerId, new TradeBookOpenPacket.ShopOfferData(p1.copy(), p2.copy(),
                                        offer.result().copy()));

                        boolean saleActive = offer.runtimeState().isSaleActive();
                        double salePercent = offer.runtimeState().salePercent().orElse(0.0);
                        String payload = String.format(java.util.Locale.US,
                                        "MARKET_TOP_ENTRY||%d||%s||%d||%s||%b||%.2f",
                                        j, resultName, offer.runtimeState().lifetimePurchases(), offerId, saleActive,
                                        salePercent);

                        page.append(Component.literal("\u00A0\n")
                                        .withStyle(style -> style.withInsertion(payload)));
                }
                pages.add(page);
        }

        // ==========================================
        // Section 6: NPC Trends
        // ==========================================

        private void addNpcTrendsPages(List<Component> pages, ServerPlayer player) {
                NpcEconomySavedData economyData = NpcEconomySavedData.get(player.serverLevel());
                Map<Item, Double> baseValues = TraderEconomyManager.get().getBaseValues();
                List<Item> allItems = new ArrayList<>(baseValues.keySet());

                Map<Item, Double> multiplierCache = new HashMap<>();
                List<Item> changedItems = new ArrayList<>();

                for (Item item : allItems) {
                        double mult = economyData.getDemandMultiplier(item, player.serverLevel());
                        multiplierCache.put(item, mult);
                        int percent = (int) Math.round(mult * 100);
                        if (percent != 100) {
                                changedItems.add(item);
                        }
                }

                // Sort items by multiplier descending
                changedItems.sort((a, b) -> Double.compare(multiplierCache.get(b), multiplierCache.get(a)));

                if (changedItems.isEmpty()) {
                        MutableComponent emptyPage = Component.translatable("gui.marketblocks.trade_book.trends.title")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.translatable("gui.marketblocks.trade_book.trends.stable")
                                                        .withStyle(ChatFormatting.DARK_GREEN));
                        pages.add(emptyPage);
                } else {
                        MutableComponent trendsPage = Component
                                        .translatable("gui.marketblocks.trade_book.trends.title")
                                        .withStyle(style -> style.withColor(ChatFormatting.GOLD)
                                                        .withInsertion("PAGE_SCALE:0.85"));

                        for (int j = 0; j < changedItems.size(); j++) {
                                Item item = changedItems.get(j);
                                double mult = multiplierCache.get(item);
                                int percent = (int) Math.round(mult * 100);

                                String sign;
                                if (percent >= 100) {
                                        sign = "▲";
                                } else {
                                        sign = "▼";
                                }

                                String itemName = item.getDescription().getString();
                                if (itemName.length() > 14) {
                                        itemName = itemName.substring(0, 12) + "..";
                                }

                                double baseVal = baseValues.get(item);
                                double currentVal = baseVal * mult;
                                long roundBase = Math.round(baseVal);
                                long roundCurrent = Math.round(currentVal);
                                int colorHex = percent >= 100 ? 0x00AA00 : 0xAA0000;
                                String payload = String.format("TREND_ENTRY||%s||%s||%d||%d||%d||%d",
                                                sign, itemName, percent, colorHex, roundBase, roundCurrent);
                                trendsPage.append(Component.literal("\u00A0\n")
                                                .withStyle(style -> style.withInsertion(payload)));
                        }
                        pages.add(trendsPage);
                }
        }

        // ==========================================
        // Utility Methods
        // ==========================================

        private String formatCompactNumber(int number) {
                if (number >= 1000000) {
                        return new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US)).format(number / 1000000.0)
                                        + "M";
                }
                if (number >= 1000) {
                        return new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US)).format(number / 1000.0)
                                        + "k";
                }
                return String.valueOf(number);
        }
}
