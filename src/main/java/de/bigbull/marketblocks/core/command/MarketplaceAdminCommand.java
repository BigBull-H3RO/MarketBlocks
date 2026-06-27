package de.bigbull.marketblocks.core.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import de.bigbull.marketblocks.core.data.MarketplaceLinkSavedData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceData;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplaceOffer;
import de.bigbull.marketblocks.feature.marketplace.data.MarketplacePage;
import de.bigbull.marketblocks.feature.singleoffer.menu.SingleOfferShopMenu;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Handles all {@code /marketblocks admin ...} and
 * {@code /marketblocks marketplace ...} admin subcommands:
 * editmode, reload, resetlimits, link, unlink.
 */
public final class MarketplaceAdminCommand {

    private static final String EDIT_MODE_ENABLED_KEY = "message.marketblocks.marketplace.edit_mode_enabled";
    private static final String EDIT_MODE_DISABLED_KEY = "message.marketblocks.marketplace.edit_mode_disabled";

    private static final SuggestionProvider<CommandSourceStack> OFFER_SUGGESTIONS = (context, builder) -> {
        MarketplaceData data = MarketplaceManager.get().snapshot();
        Map<String, Integer> seenKeys = new HashMap<>();
        for (MarketplacePage page : data.pages()) {
            for (MarketplaceOffer offer : page.offers()) {
                String suggestionKey = getOfferKey(offer, page, seenKeys);
                StringBuilder cost = new StringBuilder();
                List<ItemStack> payments = offer.originalPayments();
                if (!payments.isEmpty() && !payments.get(0).isEmpty()) {
                    cost.append(payments.get(0).getCount()).append("x ")
                            .append(payments.get(0).getHoverName().getString());
                }
                if (payments.size() > 1 && !payments.get(1).isEmpty()) {
                    cost.append(", ").append(payments.get(1).getCount()).append("x ")
                            .append(payments.get(1).getHoverName().getString());
                }
                String tooltip = page.name() + " | " + offer.result().getCount() + "x "
                        + offer.result().getHoverName().getString() + " (Preis: " + cost.toString() + ")";
                builder.suggest("\"" + suggestionKey + "\"", new LiteralMessage(tooltip));
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SHOPSALE_SUGGESTIONS = (context, builder) -> {
        de.bigbull.marketblocks.core.data.ShopDirectorySavedData data = de.bigbull.marketblocks.core.data.ShopDirectorySavedData
                .get(context.getSource().getLevel());
        Map<String, Integer> seenKeys = new HashMap<>();
        for (de.bigbull.marketblocks.core.data.ShopDirectorySavedData.ShopEntry entry : data.getShops()) {
            if (entry.isAdminShop()) {
                String suggestionKey = getShopKey(entry, seenKeys);
                StringBuilder cost = new StringBuilder();
                if (!entry.payment1().isEmpty()) {
                    cost.append(entry.payment1().getCount()).append("x ")
                            .append(entry.payment1().getHoverName().getString());
                }
                if (!entry.payment2().isEmpty()) {
                    cost.append(", ").append(entry.payment2().getCount()).append("x ")
                            .append(entry.payment2().getHoverName().getString());
                }
                String tooltip = (entry.shopName() != null && !entry.shopName().isEmpty() ? entry.shopName()
                        : "AdminShop") + " | " + entry.result().getCount() + "x "
                        + entry.result().getHoverName().getString() + " (Preis: " + cost.toString() + ")";
                builder.suggest("\"" + suggestionKey + "\"", new LiteralMessage(tooltip));
            }
        }
        return builder.buildFuture();
    };

    private MarketplaceAdminCommand() {
    }

    /**
     * Builds the Brigadier command node for {@code admin} subcommands.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build(
            SuggestionProvider<CommandSourceStack> linkSuggestions,
            CommandBuildContext buildContext) {
        return Commands.literal("admin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("editmode")
                        .executes(context -> {
                            MarketplaceManager manager = MarketplaceManager.get();
                            boolean enabled = !manager.isGlobalEditModeEnabled();
                            setGlobalAdminModeAndNotify(enabled, context.getSource());
                            return 1;
                        })
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    setGlobalAdminModeAndNotify(enabled, context.getSource());
                                    return 1;
                                })))
                .then(Commands.literal("reload")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            MarketplaceManager.get().reload();
                            source.sendSuccess(
                                    () -> Component.translatable("command.marketblocks.reload.success"),
                                    true);
                            return 1;
                        }))
                .then(Commands.literal("resetlimits")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    try {
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        boolean changed = MarketplaceManager.get()
                                                .resetLimitsForPlayer(player.getUUID());
                                        if (changed) {
                                            source.sendSuccess(
                                                    () -> Component.translatable(
                                                            "command.marketblocks.resetlimits.success",
                                                            player.getName().getString()),
                                                    true);
                                        } else {
                                            source.sendSuccess(
                                                    () -> Component.translatable(
                                                            "command.marketblocks.resetlimits.no_changes",
                                                            player.getName().getString()),
                                                    true);
                                        }
                                    } catch (Exception e) {
                                        source.sendFailure(
                                                Component.translatable("command.marketblocks.player_not_found"));
                                        return 0;
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("marketplace")
                        .then(Commands.literal("link")
                                .executes(context -> linkBlock(context, null, null))
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(context -> linkBlock(context,
                                                StringArgumentType.getString(context, "name"), null))
                                        .then(Commands.argument("tp_pos", Vec3Argument.vec3())
                                                .executes(context -> linkBlock(context,
                                                        StringArgumentType.getString(context, "name"),
                                                        Vec3Argument.getVec3(context, "tp_pos"))))))
                        .then(Commands.literal("unlink")
                                .requires(source -> source.hasPermission(2))
                                .executes(MarketplaceAdminCommand::executeMarketplaceUnlink)
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests(linkSuggestions)
                                        .executes(MarketplaceAdminCommand::executeMarketplaceUnlinkByName))))
                .then(Commands.literal("trader")
                        .then(Commands.literal("value")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                        .executes(context -> {
                                                            Item item = ItemArgument.getItem(context, "item").getItem();
                                                            double val = DoubleArgumentType.getDouble(context, "value");
                                                            TraderEconomyManager.get().setValue(item, val);
                                                            context.getSource()
                                                                    .sendSuccess(() -> Component.translatable(
                                                                            "command.marketblocks.trader.value.set",
                                                                            Component.translatable(
                                                                                    item.getDescriptionId()),
                                                                            val),
                                                                            true);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> {
                                                    Item item = ItemArgument.getItem(context, "item").getItem();
                                                    TraderEconomyManager.get().removeValue(item);
                                                    context.getSource()
                                                            .sendSuccess(() -> Component.translatable(
                                                                    "command.marketblocks.trader.value.remove",
                                                                    Component.translatable(item.getDescriptionId())),
                                                                    true);
                                                    return 1;
                                                }))))
                        .then(Commands.literal("blacklist")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> {
                                                    Item item = ItemArgument.getItem(context, "item").getItem();
                                                    TraderEconomyManager.get().setBlacklisted(item, true);
                                                    context.getSource().sendSuccess(() -> Component.translatable(
                                                            "command.marketblocks.trader.blacklist.add",
                                                            Component.translatable(item.getDescriptionId())),
                                                            true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                                .executes(context -> {
                                                    Item item = ItemArgument.getItem(context, "item").getItem();
                                                    TraderEconomyManager.get().setBlacklisted(item, false);
                                                    context.getSource()
                                                            .sendSuccess(() -> Component.translatable(
                                                                    "command.marketblocks.trader.blacklist.remove",
                                                                    Component.translatable(item.getDescriptionId())),
                                                                    true);
                                                    return 1;
                                                })))))
                .then(Commands.literal("sale")
                        .then(Commands.literal("marketplace")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("offer", StringArgumentType.string())
                                                .suggests(OFFER_SUGGESTIONS)
                                                .then(Commands.argument("percent", DoubleArgumentType.doubleArg())
                                                        .then(Commands
                                                                .argument("duration_minutes",
                                                                        IntegerArgumentType.integer(1))
                                                                .executes(context -> executeSaleSet(context,
                                                                        StringArgumentType.getString(context, "offer"),
                                                                        DoubleArgumentType.getDouble(context,
                                                                                "percent"),
                                                                        IntegerArgumentType.getInteger(context,
                                                                                "duration_minutes")))))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("offer", StringArgumentType.string())
                                                .suggests(OFFER_SUGGESTIONS)
                                                .executes(context -> {
                                                    String offerStr = StringArgumentType.getString(context, "offer");
                                                    UUID offerId = resolveOfferId(offerStr);
                                                    if (offerId == null) {
                                                        context.getSource().sendFailure(
                                                                Component.translatable(
                                                                        "command.marketblocks.sale.not_found",
                                                                        offerStr));
                                                        return 1;
                                                    }
                                                    try {
                                                        MarketplaceOffer offer = MarketplaceManager.get()
                                                                .findOffer(offerId);
                                                        boolean success = MarketplaceManager.get().setOfferSale(offerId,
                                                                null, 0L);
                                                        if (success) {
                                                            String itemName = offer != null
                                                                    ? (offer.result().getCount() + "x "
                                                                            + offer.result().getHoverName().getString())
                                                                    : offerStr;
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.translatable(
                                                                            "command.marketblocks.sale.remove.success",
                                                                            itemName),
                                                                    true);
                                                        } else {
                                                            context.getSource().sendFailure(
                                                                    Component.translatable(
                                                                            "command.marketblocks.sale.not_found",
                                                                            offerStr));
                                                        }
                                                    } catch (Exception e) {
                                                        context.getSource().sendFailure(
                                                                Component.translatable(
                                                                        "command.marketblocks.sale.failed"));
                                                    }
                                                    return 1;
                                                }))))
                        .then(Commands.literal("shop")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("shop", StringArgumentType.string())
                                                .suggests(SHOPSALE_SUGGESTIONS)
                                                .then(Commands.argument("percent", DoubleArgumentType.doubleArg())
                                                        .then(Commands
                                                                .argument("duration_minutes",
                                                                        IntegerArgumentType.integer(1))
                                                                .executes(context -> executeShopSaleSet(context,
                                                                        StringArgumentType.getString(context, "shop"),
                                                                        DoubleArgumentType.getDouble(context,
                                                                                "percent"),
                                                                        IntegerArgumentType.getInteger(context,
                                                                                "duration_minutes")))))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("shop", StringArgumentType.string())
                                                .suggests(SHOPSALE_SUGGESTIONS)
                                                .executes(context -> {
                                                    String shopStr = StringArgumentType.getString(context, "shop");
                                                    de.bigbull.marketblocks.core.data.ShopDirectorySavedData.ShopEntry entry = resolveShopEntry(
                                                            context.getSource(), shopStr);
                                                    if (entry == null) {
                                                        context.getSource().sendFailure(Component.translatable(
                                                                "command.marketblocks.sale.not_found", shopStr));
                                                        return 1;
                                                    }
                                                    ServerLevel shopLevel = context.getSource().getServer()
                                                            .getLevel(entry.pos().dimension());
                                                    if (shopLevel != null) {
                                                        if (shopLevel.getBlockEntity(entry.pos()
                                                                .pos()) instanceof de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity be) {
                                                            be.setSale(null, 0L);
                                                            String itemName = entry.result().getCount() + "x "
                                                                    + entry.result().getHoverName().getString();
                                                            context.getSource()
                                                                    .sendSuccess(() -> Component.translatable(
                                                                            "command.marketblocks.sale.remove.success",
                                                                            itemName), true);
                                                            return 1;
                                                        }
                                                    }
                                                    context.getSource().sendFailure(
                                                            Component.translatable("command.marketblocks.sale.failed"));
                                                    return 1;
                                                })))));
    }

    private static String getOfferKey(MarketplaceOffer offer, MarketplacePage page, Map<String, Integer> seenKeys) {
        String baseKey = page.name() + " - " + offer.result().getCount() + "x "
                + offer.result().getHoverName().getString();
        int count = seenKeys.getOrDefault(baseKey, 0) + 1;
        seenKeys.put(baseKey, count);
        if (count > 1) {
            return baseKey + " (" + count + ")";
        }
        return baseKey;
    }

    private static UUID resolveOfferId(String offerStr) {
        try {
            return UUID.fromString(offerStr);
        } catch (Exception e) {
            MarketplaceData data = MarketplaceManager.get().snapshot();
            Map<String, Integer> seenKeys = new HashMap<>();
            for (MarketplacePage page : data.pages()) {
                for (MarketplaceOffer offer : page.offers()) {
                    String key = getOfferKey(offer, page, seenKeys);
                    if (key.equalsIgnoreCase(offerStr)) {
                        return offer.id();
                    }
                }
            }
            return null;
        }
    }

    private static String getShopKey(de.bigbull.marketblocks.core.data.ShopDirectorySavedData.ShopEntry entry,
            Map<String, Integer> seenKeys) {
        String shopName = entry.shopName() != null && !entry.shopName().isEmpty() ? entry.shopName() : "AdminShop";
        String baseKey = shopName + " - " + entry.result().getCount() + "x "
                + entry.result().getHoverName().getString();
        int count = seenKeys.getOrDefault(baseKey, 0) + 1;
        seenKeys.put(baseKey, count);
        if (count > 1) {
            return baseKey + " (" + count + ")";
        }
        return baseKey;
    }

    private static de.bigbull.marketblocks.core.data.ShopDirectorySavedData.ShopEntry resolveShopEntry(
            CommandSourceStack source, String shopStr) {
        de.bigbull.marketblocks.core.data.ShopDirectorySavedData data = de.bigbull.marketblocks.core.data.ShopDirectorySavedData
                .get(source.getLevel());
        Map<String, Integer> seenKeys = new HashMap<>();
        for (de.bigbull.marketblocks.core.data.ShopDirectorySavedData.ShopEntry entry : data.getShops()) {
            if (entry.isAdminShop()) {
                String key = getShopKey(entry, seenKeys);
                if (key.equalsIgnoreCase(shopStr)) {
                    return entry;
                }
            }
        }
        return null;
    }

    private static int executeShopSaleSet(CommandContext<CommandSourceStack> context, String shopStr, double percent,
            int durationMinutes) {
        de.bigbull.marketblocks.core.data.ShopDirectorySavedData.ShopEntry entry = resolveShopEntry(context.getSource(),
                shopStr);
        if (entry == null) {
            context.getSource().sendFailure(Component.translatable("command.marketblocks.sale.not_found", shopStr));
            return 1;
        }
        ServerLevel shopLevel = context.getSource().getServer().getLevel(entry.pos().dimension());
        if (shopLevel != null) {
            if (shopLevel.getBlockEntity(entry.pos()
                    .pos()) instanceof de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity be) {
                be.setSale(percent, durationMinutes * 60000L);
                String itemName = entry.result().getCount() + "x " + entry.result().getHoverName().getString();
                String percentStr = (percent > 0 ? "+" : "")
                        + String.format("%.1f", percent).replace(".0", "").replace(",0", "") + "%";
                context.getSource().sendSuccess(() -> Component.translatable("command.marketblocks.sale.set.success",
                        itemName, percentStr, durationMinutes), true);
                return 1;
            }
        }
        context.getSource().sendFailure(Component.translatable("command.marketblocks.sale.failed"));
        return 1;
    }

    private static int executeSaleSet(CommandContext<CommandSourceStack> context, String offerStr, double percent,
            int durationMinutes) {
        UUID offerId = resolveOfferId(offerStr);
        if (offerId == null) {
            context.getSource().sendFailure(Component.translatable("command.marketblocks.sale.not_found", offerStr));
            return 1;
        }
        try {
            MarketplaceOffer offer = MarketplaceManager.get().findOffer(offerId);
            if (offer == null) {
                context.getSource()
                        .sendFailure(Component.translatable("command.marketblocks.sale.not_found", offerStr));
                return 1;
            }
            boolean success = MarketplaceManager.get().setOfferSale(offerId, percent, durationMinutes * 60000L);
            if (success) {
                String itemName = offer.result().getCount() + "x " + offer.result().getHoverName().getString();
                String percentStr = (percent > 0 ? "+" : "")
                        + String.format("%.1f", percent).replace(".0", "").replace(",0", "") + "%";
                context.getSource().sendSuccess(() -> Component.translatable("command.marketblocks.sale.set.success",
                        itemName, percentStr, durationMinutes), true);
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("command.marketblocks.sale.failed"));
        }
        return 1;
    }

    private static void setGlobalAdminModeAndNotify(boolean enabled, CommandSourceStack source) {
        MarketplaceManager.get().setGlobalEditModeEnabled(enabled);
        refreshOpenSingleOfferMenus(source);
        source.sendSuccess(
                () -> Component.translatable(enabled ? EDIT_MODE_ENABLED_KEY : EDIT_MODE_DISABLED_KEY),
                true);
    }

    private static void refreshOpenSingleOfferMenus(CommandSourceStack source) {
        if (source.getServer() == null) {
            return;
        }
        for (var player : source.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof SingleOfferShopMenu menu) {
                menu.broadcastChanges();
            }
        }
    }

    private static int linkBlock(CommandContext<CommandSourceStack> context, String name, Vec3 tpPos)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);

            Vec3 finalTpPos = tpPos;
            Float finalTpYaw = null;
            Float finalTpPitch = null;
            if (finalTpPos == null) {
                finalTpPos = Vec3.atBottomCenterOf(player.blockPosition());
                finalTpYaw = Direction.fromYRot(player.getYRot()).toYRot();
                finalTpPitch = 0.0f;
            }

            if (MarketplaceLinkSavedData.get(player.serverLevel()).addLink(globalPos, name, finalTpPos, finalTpYaw,
                    finalTpPitch)) {
                MarketplaceLinkSavedData.get(player.serverLevel()).syncToAll(player.getServer());
                player.sendSystemMessage(Component.translatable("command.marketblocks.link.success"));
            } else {
                player.sendSystemMessage(Component.translatable("command.marketblocks.link.already_linked"));
            }
        } else {
            player.sendSystemMessage(Component.translatable("command.marketblocks.link.not_looking_at_block"));
        }
        return 1;
    }

    private static int executeMarketplaceUnlink(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            GlobalPos globalPos = GlobalPos.of(player.serverLevel().dimension(), pos);
            if (MarketplaceLinkSavedData.get(player.serverLevel()).removeLink(globalPos)) {
                MarketplaceLinkSavedData.get(player.serverLevel()).syncToAll(player.getServer());
                player.sendSystemMessage(Component.translatable("command.marketblocks.unlink.success"));
            } else {
                player.sendSystemMessage(Component.translatable("command.marketblocks.unlink.not_linked"));
            }
        } else {
            player.sendSystemMessage(Component.translatable("command.marketblocks.unlink.not_looking_at_block"));
        }
        return 1;
    }

    private static int executeMarketplaceUnlinkByName(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        ServerLevel level = context.getSource().getLevel();
        int removed = MarketplaceLinkSavedData.get(level).removeLinkByName(name);
        if (removed > 0) {
            MarketplaceLinkSavedData.get(level).syncToAll(level.getServer());
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.marketblocks.unlink.success_name", removed, name), true);
        } else {
            context.getSource().sendFailure(Component.translatable("command.marketblocks.unlink.not_found", name));
        }
        return 1;
    }
}
