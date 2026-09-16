package de.bigbull.marketblocks.feature.singleoffer.entity;

import net.minecraft.core.BlockPos;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.SingleOfferConfig;
import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.log.ShopTransactionLogSavedData;
import de.bigbull.marketblocks.feature.log.TransactionLogEntry;
import de.bigbull.marketblocks.feature.singleoffer.advancement.ShopSellCountSavedData;
import de.bigbull.marketblocks.feature.singleoffer.network.OfferStatusPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import de.bigbull.marketblocks.platform.inventory.ICommonItemHandler;
import de.bigbull.marketblocks.network.NetworkHandler;
import org.jetbrains.annotations.Nullable;
import de.bigbull.marketblocks.feature.notification.PendingNotificationsSavedData;
import de.bigbull.marketblocks.feature.singleoffer.settings.NotificationSettings;
import de.bigbull.marketblocks.feature.trader.data.NpcEconomySavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages the offer logic for a single-offer shop block entity.
 * Handles offer creation, validation, affordability checks, and bulk purchase
 * execution.
 */
public class OfferManager {
    private final SingleOfferShopBlockEntity shopEntity;

    public OfferManager(SingleOfferShopBlockEntity shopEntity) {
        this.shopEntity = shopEntity;
    }

    public SingleOfferShopBlockEntity shopEntity() {
        return shopEntity;
    }

    private static final int PAYMENT_SLOT_COUNT = 2;
    private static final int RESULT_SLOT_INDEX = 2;
    private static final int TOTAL_OFFER_SLOTS = 3;

    private static final int BATCH_WINDOW_TICKS = 40; // 2 seconds delay
    private static final int MAX_BATCH_TICKS = 100;   // 5 seconds max window

    private static class PendingPurchaseNotification {
        private final UUID buyerId;
        private final String buyerName;
        private final ItemStack resultItem;
        private int totalCount;
        private int remainingTicks;
        private int totalElapsedTicks;
        private final boolean notifyCoOwners;

        public PendingPurchaseNotification(UUID buyerId, String buyerName, ItemStack resultItem, int count, boolean notifyCoOwners) {
            this.buyerId = buyerId;
            this.buyerName = buyerName;
            this.resultItem = resultItem.copy();
            this.totalCount = count;
            this.remainingTicks = BATCH_WINDOW_TICKS;
            this.totalElapsedTicks = 0;
            this.notifyCoOwners = notifyCoOwners;
        }

        public boolean canMerge(UUID otherBuyerId, String otherBuyerName, ItemStack otherItem, boolean otherNotifyCoOwners) {
            if (this.notifyCoOwners != otherNotifyCoOwners) return false;
            if (this.buyerId != null && otherBuyerId != null) {
                if (!this.buyerId.equals(otherBuyerId)) return false;
            } else if (!java.util.Objects.equals(this.buyerName, otherBuyerName)) {
                return false;
            }
            return ItemStack.isSameItemSameComponents(this.resultItem, otherItem);
        }

        public void merge(int additionalCount) {
            this.totalCount += additionalCount;
            this.remainingTicks = BATCH_WINDOW_TICKS;
        }
    }

    private PendingPurchaseNotification pendingPurchase;

    public record OfferValidation(boolean valid, String errorKey) {
        public static final OfferValidation VALID = new OfferValidation(true, null);
    }

    public boolean applyOffer(ServerPlayer player, ItemStack payment1, ItemStack payment2, ItemStack result) {
        OfferValidation validation = validateOffer(payment1, payment2, result);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.translatable(validation.errorKey()));
            return false;
        }

        if (shopEntity.getOwnerId() == null) {
            shopEntity.setOwner(player);
            shopEntity.lockAdjacentChests();
        }

        ItemStack[] slotCopies = copyOfferSlotsFromShop();
        ItemStack[] extractedItems = extractItemsFromOfferSlots(slotCopies);

        shopEntity.createOffer(slotCopies[0], slotCopies[1], slotCopies[RESULT_SLOT_INDEX]);

        if (player.level() instanceof ServerLevel serverLevel) {
            NetworkHandler.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(shopEntity.getBlockPos()),
                new OfferStatusPacket(shopEntity.getBlockPos(), true));
        }

        returnStacksToPlayer(player, extractedItems);
        Constants.LOG.info("Player {} created offer at {}", player.getName().getString(),
                shopEntity.getBlockPos());
        return true;
    }

    private OfferValidation validateOffer(ItemStack payment1, ItemStack payment2, ItemStack result) {
        if (result.isEmpty())
            return new OfferValidation(false, "gui.marketblocks.error.no_result_item");
        if (payment1.isEmpty() && payment2.isEmpty())
            return new OfferValidation(false, "gui.marketblocks.error.no_payment_items");
        ItemStack[] itemsInSlots = copyOfferSlotsFromShop();
        ItemStack[] expectedItems = new ItemStack[] { payment1, payment2, result };
        if (!areOfferSlotsConsistent(expectedItems, itemsInSlots)) {
            return new OfferValidation(false, "gui.marketblocks.error.invalid_offer");
        }
        return OfferValidation.VALID;
    }

    private ItemStack[] copyOfferSlotsFromShop() {
        ItemStack[] slots = new ItemStack[TOTAL_OFFER_SLOTS];
        copyRange(shopEntity.getPaymentHandler(), slots, 0, 0, PAYMENT_SLOT_COUNT);
        copyRange(shopEntity.getOfferHandler(), slots, RESULT_SLOT_INDEX, 0, 1);
        return slots;
    }

    private void copyRange(ICommonItemHandler handler, ItemStack[] dest, int destStart, int handlerStart, int length) {
        for (int i = 0; i < length; i++) {
            ItemStack stack = handler.getStackInSlot(handlerStart + i);
            dest[destStart + i] = stack.copy();
        }
    }

    private boolean areOfferSlotsConsistent(ItemStack[] expected, ItemStack[] actual) {
        if (!isItemBasicallyEqual(expected[RESULT_SLOT_INDEX], actual[RESULT_SLOT_INDEX]))
            return false;
        boolean[] matched = new boolean[PAYMENT_SLOT_COUNT];
        for (int i = 0; i < PAYMENT_SLOT_COUNT; i++) {
            ItemStack exp = expected[i];
            if (exp.isEmpty())
                continue;
            boolean foundMatch = false;
            for (int j = 0; j < PAYMENT_SLOT_COUNT; j++) {
                if (!matched[j] && isItemBasicallyEqual(exp, actual[j])) {
                    matched[j] = true;
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch)
                return false;
        }
        for (int j = 0; j < PAYMENT_SLOT_COUNT; j++) {
            if (!matched[j] && !actual[j].isEmpty())
                return false;
        }
        return true;
    }

    private ItemStack[] extractItemsFromOfferSlots(ItemStack[] slotContents) {
        ItemStack[] extracted = new ItemStack[slotContents.length];
        extractRange(shopEntity.getPaymentHandler(), slotContents, extracted, 0, 0, PAYMENT_SLOT_COUNT);
        extractRange(shopEntity.getOfferHandler(), slotContents, extracted, 0, RESULT_SLOT_INDEX, 1);
        return extracted;
    }

    private void extractRange(ICommonItemHandler handler, ItemStack[] slots, ItemStack[] dest, int handlerStart,
            int destStart, int length) {
        for (int i = 0; i < length; i++) {
            int index = destStart + i;
            ItemStack stack = slots[index];
            dest[index] = stack.isEmpty() ? ItemStack.EMPTY
                    : handler.extractItem(handlerStart + i, stack.getCount(), false);
        }
    }

    private boolean isItemBasicallyEqual(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty())
            return actual.isEmpty();
        return !actual.isEmpty() && ItemStack.isSameItemSameComponents(actual, expected)
                && actual.getCount() == expected.getCount();
    }

    private void returnStacksToPlayer(ServerPlayer player, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                if (!stack.isEmpty())
                    Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            }
        }
    }

    public boolean canAfford() {
        ItemStack p1 = shopEntity.getOfferPayment1();
        ItemStack p2 = shopEntity.getOfferPayment2();
        if (p1.isEmpty() && p2.isEmpty())
            return true;

        ShopInventoryManager inv = shopEntity.getInventoryManager();

        if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
            int required = p1.getCount() + p2.getCount();
            return inv.countMatchingPayment(p1) >= required;
        }

        return (p1.isEmpty() || inv.countMatchingPayment(p1) >= p1.getCount()) &&
                (p2.isEmpty() || inv.countMatchingPayment(p2) >= p2.getCount());
    }

    public boolean hasResultItemInInput(boolean checkNeighbors) {
        ItemStack result = shopEntity.getOfferResult();
        if (result.isEmpty())
            return false;
        if (shopEntity.isAdminShopEnabled())
            return true;
        return shopEntity.getInventoryManager().countMatchingInput(result, checkNeighbors) >= result.getCount();
    }

    public int processBulkPurchase(int maxAmount, @Nullable Player buyer, boolean shiftPurchase) {
        if (maxAmount <= 0)
            return 0;

        BuyerIdentity buyerIdentity = resolveBuyerIdentity(buyer);
        if (buyerIdentity != null) {
            if (!shopEntity.canPlayerBuyByUUID(buyerIdentity.uuid()))
                return 0;
        } else if (shopEntity.getGeneralSettings().isClosed()) {
            return 0;
        }

        boolean adminShop = shopEntity.isAdminShopEnabled();

        ShopInventoryManager inv = shopEntity.getInventoryManager();

        if (!adminShop && SingleOfferConfig.ENABLE_CHEST_EXTENSION.get()) {
            inv.pullFromInputChest(shopEntity.getInputHandler());
        }

        if (!shopEntity.hasOffer())
            return 0;
        ItemStack p1 = shopEntity.getOfferPayment1();
        ItemStack p2 = shopEntity.getOfferPayment2();
        ItemStack result = shopEntity.getOfferResult();
        if (result.isEmpty())
            return 0;

        int affordable = Integer.MAX_VALUE;
        if (!p1.isEmpty())
            affordable = Math.min(affordable, inv.countMatchingPayment(p1) / p1.getCount());
        if (!p2.isEmpty()) {
            if (!p1.isEmpty() && ItemStack.isSameItemSameComponents(p1, p2)) {
                int totalReqPerUnit = p1.getCount() + p2.getCount();
                affordable = inv.countMatchingPayment(p1) / totalReqPerUnit;
            } else {
                affordable = Math.min(affordable, inv.countMatchingPayment(p2) / p2.getCount());
            }
        }
        if (p1.isEmpty() && p2.isEmpty())
            affordable = maxAmount;

        int inStock = adminShop ? Integer.MAX_VALUE : inv.countMatchingInput(result, true) / result.getCount();

        int actualAmount = Math.min(maxAmount, Math.min(affordable, inStock));
        if (actualAmount <= 0)
            return 0;

        int validAmount = adminShop ? actualAmount
                : (actualAmount == 1 ? (inv.hasOutputSpace(p1, p2) ? 1 : 0)
                        : inv.simulateOutputSpace(p1, p2, actualAmount));
        actualAmount = validAmount;

        if (actualAmount <= 0) {
            if (!adminShop)
                inv.updateOutputFullness();
            return 0;
        }

        executeTrades(p1, p2, result, actualAmount, adminShop, inv);

        shopEntity.incrementTotalSales(actualAmount);
        shopEntity.incrementVisualPurchaseCounter(actualAmount);
        shopEntity.playPurchaseXpSound(actualAmount);

        appendTransactionEntry(resolveBuyerIdentity(buyer), p1, p2, result, actualAmount, shiftPurchase);

        shopEntity.sync();
        shopEntity.triggerRedstonePulse();
        shopEntity.updateOfferSlot();

        triggerNotifications(buyerIdentity, result, actualAmount, adminShop, inv, p1, p2);

        if (shopEntity.getLevel() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList()
                    .getPlayer(shopEntity.getAccessSettings().ownerId());
            if (owner != null) {
                int totalSellCount = ShopSellCountSavedData
                        .get(serverLevel).incrementAndGet(owner.getUUID(), actualAmount);
                RegistriesInit.SHOP_SELL_TRIGGER.get().trigger(owner, totalSellCount);
                if (!adminShop && inv.countMatchingInput(result, true) < result.getCount()) {
                    RegistriesInit.SHOP_OUT_OF_STOCK_TRIGGER.get().trigger(owner);
                }
            }
        }

        if (buyer instanceof ServerPlayer serverBuyer && actualAmount >= 64) {
            RegistriesInit.SHOP_WHOLESALER_TRIGGER.get().trigger(serverBuyer);
        }

        if (buyer instanceof ServerPlayer serverBuyer && SingleOfferConfig.BUYER_CHAT_MESSAGE.get()) {
            if (SingleOfferConfig.BROADCAST_PURCHASE_TO_ALL.get()) {
                Component msg = Component.translatable("message.marketblocks.purchase_success.global",
                        serverBuyer.getDisplayName(), actualAmount, result.getHoverName())
                        .withStyle(ChatFormatting.GREEN);
                serverBuyer.server.getPlayerList().broadcastSystemMessage(msg, false);
            } else {
                Component msg = Component
                        .translatable("message.marketblocks.purchase_success", actualAmount, result.getHoverName())
                        .withStyle(ChatFormatting.GREEN);
                serverBuyer.sendSystemMessage(msg);
            }
        }

        return actualAmount;
    }

    public int processNpcPurchase() {
        return processNpcPurchase(1, "Shop Buyer");
    }

    public int processNpcPurchase(int maxQuantity, String buyerName) {
        if (shopEntity.getGeneralSettings().isClosed() || !shopEntity.hasOffer() || maxQuantity <= 0)
            return 0;

        boolean adminShop = shopEntity.isAdminShopEnabled();
        if (adminShop && !TraderConfig.ALLOW_ADMIN_SHOPS.get())
            return 0;
        ShopInventoryManager inv = shopEntity.getInventoryManager();

        ItemStack p1 = shopEntity.getOfferPayment1();
        ItemStack p2 = shopEntity.getOfferPayment2();
        ItemStack result = shopEntity.getOfferResult();

        if (result.isEmpty())
            return 0;

        int inStock = adminShop ? maxQuantity : inv.countMatchingInput(result, true) / result.getCount();
        int desiredAmount = Math.min(maxQuantity, inStock);
        if (desiredAmount <= 0)
            return 0;

        int actualAmount = adminShop ? desiredAmount
                : (desiredAmount == 1 ? (inv.hasOutputSpace(p1, p2) ? 1 : 0)
                        : inv.simulateOutputSpace(p1, p2, desiredAmount));
        if (actualAmount <= 0) {
            if (!adminShop)
                inv.updateOutputFullness();
            return 0;
        }

        if (!adminShop) {
            ItemStack totalResult = multiplyStackForTrades(result, actualAmount);
            inv.removeFromInput(totalResult);
            inv.addToOutputBatched(p1, actualAmount);
            inv.addToOutputBatched(p2, actualAmount);
        }

        shopEntity.incrementTotalSales(actualAmount);
        shopEntity.incrementVisualPurchaseCounter(actualAmount);
        shopEntity.playPurchaseXpSound(actualAmount);

        String finalBuyerName = (buyerName != null && !buyerName.isBlank()) ? buyerName : "Shop Buyer";
        BuyerIdentity buyerIdentity = new BuyerIdentity(new UUID(0L, 0L), finalBuyerName);
        appendTransactionEntry(buyerIdentity, p1, p2, result, actualAmount, false);

        shopEntity.sync();
        shopEntity.triggerRedstonePulse();
        shopEntity.updateOfferSlot();

        triggerNotifications(buyerIdentity, result, actualAmount, adminShop, inv, p1, p2);

        if (shopEntity.getLevel() instanceof ServerLevel serverLevel) {
            NpcEconomySavedData.get(serverLevel).registerSale(result.getItem(), actualAmount, serverLevel);
        }

        return actualAmount;
    }

    private void triggerNotifications(@Nullable BuyerIdentity buyer, ItemStack result, int tradeCount,
            boolean adminShop, ShopInventoryManager inv, ItemStack p1, ItemStack p2) {
        if (!(shopEntity.getLevel() instanceof ServerLevel serverLevel))
            return;
        NotificationSettings notifSettings = shopEntity.getNotificationSettings();

        String shopName = shopEntity.getGeneralSettings().shopName();
        if (shopName == null || shopName.isEmpty())
            shopName = "MarketBlocks";
        Component shopPrefix = Component.literal("[" + shopName + "] ");

        if (notifSettings.notifyOnPurchase() && tradeCount > 0) {
            queuePurchaseNotification(serverLevel, buyer, result, tradeCount, notifSettings.notifyCoOwners());
        }

        if (adminShop)
            return;

        boolean isOutOfStock = inv.countMatchingInput(result, true) < result.getCount();
        if (isOutOfStock && notifSettings.notifyOnOutOfStock()) {
            sendCooldownNotification(serverLevel, notifSettings, shopPrefix,
                    Component.translatable("message.marketblocks.notifications.out_of_stock"),
                    shopEntity.getLastOutOfStockNotifyTime(),
                    shopEntity::setLastOutOfStockNotifyTime,
                    (data, owner, pos) -> data.addOutOfStock(owner, pos));
        }

        boolean isOutputFull = !inv.hasOutputSpace(p1, p2);
        if (isOutputFull && notifSettings.notifyOnOutputFull()) {
            sendCooldownNotification(serverLevel, notifSettings, shopPrefix,
                    Component.translatable("message.marketblocks.notifications.output_full"),
                    shopEntity.getLastOutputFullNotifyTime(),
                    shopEntity::setLastOutputFullNotifyTime,
                    (data, owner, pos) -> data.addOutputFull(owner, pos));
        }
    }

    private void queuePurchaseNotification(ServerLevel serverLevel, @Nullable BuyerIdentity buyer, ItemStack result,
            int tradeCount, boolean notifyCoOwners) {
        UUID buyerId = buyer != null ? buyer.uuid() : null;
        String buyerName = buyer != null ? buyer.name() : "Unbekannt";

        if (pendingPurchase != null) {
            if (pendingPurchase.canMerge(buyerId, buyerName, result, notifyCoOwners)) {
                pendingPurchase.merge(tradeCount);
                return;
            }
            flushPendingPurchaseNotification();
        }

        pendingPurchase = new PendingPurchaseNotification(buyerId, buyerName, result, tradeCount, notifyCoOwners);
    }

    public void tick() {
        if (pendingPurchase != null) {
            pendingPurchase.remainingTicks--;
            pendingPurchase.totalElapsedTicks++;
            if (pendingPurchase.remainingTicks <= 0 || pendingPurchase.totalElapsedTicks >= MAX_BATCH_TICKS) {
                flushPendingPurchaseNotification();
            }
        }
    }

    public void flushPendingPurchaseNotification() {
        if (pendingPurchase == null)
            return;
        if (!(shopEntity.getLevel() instanceof ServerLevel serverLevel)) {
            pendingPurchase = null;
            return;
        }

        String shopName = shopEntity.getGeneralSettings().shopName();
        if (shopName == null || shopName.isEmpty())
            shopName = "MarketBlocks";
        Component shopPrefix = Component.literal("[" + shopName + "] ");

        Component msg = shopPrefix.copy().append(Component.translatable(
                "message.marketblocks.notifications.purchase", pendingPurchase.buyerName, pendingPurchase.totalCount,
                pendingPurchase.resultItem.getHoverName()));
        sendToOwners(serverLevel, pendingPurchase.notifyCoOwners, msg);
        pendingPurchase = null;
    }

    @FunctionalInterface
    private interface PendingNotificationAction {
        void add(PendingNotificationsSavedData data, UUID owner, BlockPos pos);
    }

    private void sendCooldownNotification(ServerLevel level, NotificationSettings settings, Component prefix,
            Component message, long lastNotify, java.util.function.LongConsumer updateTime,
            PendingNotificationAction addPending) {
        long currentTime = level.getGameTime();
        int cooldown = SingleOfferConfig.NOTIFICATION_COOLDOWN.get();

        if (lastNotify == -1 || (currentTime - lastNotify) >= cooldown) {
            updateTime.accept(currentTime);
            Component msg = prefix.copy().append(message);
            boolean online = sendToOwners(level, settings.notifyCoOwners(), msg);
            if (!online) {
                PendingNotificationsSavedData data = PendingNotificationsSavedData.get(level);
                addPending.add(data, shopEntity.getAccessSettings().ownerId(), shopEntity.getBlockPos());
                if (settings.notifyCoOwners()) {
                    for (UUID coOwner : shopEntity.getAccessSettings().additionalOwners().keySet()) {
                        addPending.add(data, coOwner, shopEntity.getBlockPos());
                    }
                }
            }
        }
    }

    private boolean sendToOwners(ServerLevel level, boolean notifyCoOwners, Component message) {
        boolean primaryOnline = false;
        ServerPlayer primary = level.getServer().getPlayerList().getPlayer(shopEntity.getAccessSettings().ownerId());
        if (primary != null) {
            primary.sendSystemMessage(message);
            primaryOnline = true;
        }

        if (notifyCoOwners) {
            for (UUID coOwnerId : shopEntity.getAccessSettings().additionalOwners().keySet()) {
                ServerPlayer coOwner = level.getServer().getPlayerList().getPlayer(coOwnerId);
                if (coOwner != null) {
                    coOwner.sendSystemMessage(message);
                }
            }
        }
        return primaryOnline;
    }

    private void executeTrades(ItemStack p1, ItemStack p2, ItemStack result, int tradeCount, boolean adminShop,
            ShopInventoryManager inv) {
        if (tradeCount <= 0)
            return;

        ItemStack totalP1 = multiplyStackForTrades(p1, tradeCount);
        ItemStack totalP2 = multiplyStackForTrades(p2, tradeCount);
        ItemStack totalResult = multiplyStackForTrades(result, tradeCount);

        if (!totalP1.isEmpty())
            inv.removePayment(totalP1);
        if (!totalP2.isEmpty())
            inv.removePayment(totalP2);
        if (!adminShop && !totalResult.isEmpty())
            inv.removeFromInput(totalResult);

        if (!adminShop) {
            inv.addToOutputBatched(p1, tradeCount);
            inv.addToOutputBatched(p2, tradeCount);
        }
    }

    private ItemStack multiplyStackForTrades(ItemStack stack, int tradeCount) {
        if (stack == null || stack.isEmpty() || tradeCount <= 0)
            return ItemStack.EMPTY;
        long total = (long) stack.getCount() * tradeCount;
        if (total <= 0L)
            return ItemStack.EMPTY;
        ItemStack multiplied = stack.copy();
        multiplied.setCount((int) Math.min(Integer.MAX_VALUE, total));
        return multiplied;
    }

    private void appendTransactionEntry(@Nullable BuyerIdentity buyer, ItemStack payment1, ItemStack payment2,
            ItemStack result, int tradeCount, boolean shiftPurchase) {
        if (!(shopEntity.getLevel() instanceof ServerLevel serverLevel) || tradeCount <= 0)
            return;

        List<ItemStack> paidStacks = new ArrayList<>(2);
        ItemStack paidOne = TransactionLogEntry.scaleStack(payment1, tradeCount);
        ItemStack paidTwo = TransactionLogEntry.scaleStack(payment2, tradeCount);
        if (!paidOne.isEmpty())
            paidStacks.add(paidOne);
        if (!paidTwo.isEmpty())
            paidStacks.add(paidTwo);

        List<ItemStack> boughtStacks = new ArrayList<>(1);
        ItemStack bought = TransactionLogEntry.scaleStack(result, tradeCount);
        if (!bought.isEmpty())
            boughtStacks.add(bought);

        UUID buyerId = buyer != null ? buyer.uuid() : new UUID(0L, 0L);
        String buyerName = buyer != null ? buyer.name() : "";

        TransactionLogEntry entry = TransactionLogEntry.now(
                buyerId, buyerName, paidStacks, boughtStacks,
                shiftPurchase ? TransactionLogEntry.PurchaseKind.SHIFT : TransactionLogEntry.PurchaseKind.SINGLE);

        ShopTransactionLogSavedData.get(serverLevel).appendEntry(
                ShopTransactionLogSavedData.SINGLE_OFFER_SHOP_TYPE,
                serverLevel.dimension(),
                shopEntity.getBlockPos(),
                entry,
                100);
    }

    private record BuyerIdentity(UUID uuid, String name) {
    }

    private @Nullable BuyerIdentity resolveBuyerIdentity(@Nullable Player directBuyer) {
        if (directBuyer != null) {
            return new BuyerIdentity(directBuyer.getUUID(), directBuyer.getGameProfile().getName());
        }
        if (shopEntity.getAccessManager().purchaseContextBuyerId != null) {
            return new BuyerIdentity(shopEntity.getAccessManager().purchaseContextBuyerId,
                    shopEntity.getAccessManager().purchaseContextBuyerName);
        }
        return null;
    }
}
