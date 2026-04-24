package de.bigbull.marketblocks.shop.log;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central transaction-log storage for all shop types.
 * Persisted as SavedData (world/data/*.dat), not inside chunk NBT.
 */
public final class ShopTransactionLogSavedData extends SavedData {
    private static final String NBT_SHOPS = "Shops";
    private static final String NBT_KEY = "Key";
    private static final String NBT_ENTRIES = "Entries";

    public static final String SINGLE_OFFER_SHOP_TYPE = "single_offer_shop";
    public static final int DEFAULT_MAX_ENTRIES_PER_SHOP = 100;
    public static final String DATA_NAME = MarketBlocks.MODID + "_shop_logs";

    public static final SavedData.Factory<ShopTransactionLogSavedData> FACTORY =
            new SavedData.Factory<>(ShopTransactionLogSavedData::new, ShopTransactionLogSavedData::load);

    private final Map<String, ArrayDeque<TransactionLogEntry>> logsByShop = new HashMap<>();

    public ShopTransactionLogSavedData() {
    }

    private static ShopTransactionLogSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ShopTransactionLogSavedData data = new ShopTransactionLogSavedData();
        if (!tag.contains(NBT_SHOPS, Tag.TAG_LIST)) {
            return data;
        }

        ListTag shops = tag.getList(NBT_SHOPS, Tag.TAG_COMPOUND);
        for (int i = 0; i < shops.size(); i++) {
            CompoundTag shopTag = shops.getCompound(i);
            String key = shopTag.getString(NBT_KEY);
            if (key == null || key.isBlank()) {
                continue;
            }

            ArrayDeque<TransactionLogEntry> deque = new ArrayDeque<>();
            ListTag entriesTag = shopTag.getList(NBT_ENTRIES, Tag.TAG_COMPOUND);
            for (int j = 0; j < entriesTag.size(); j++) {
                TransactionLogEntry entry = TransactionLogEntry.fromTag(entriesTag.getCompound(j), registries);
                deque.addLast(entry);
            }

            if (!deque.isEmpty()) {
                data.logsByShop.put(key, deque);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag shops = new ListTag();
        logsByShop.forEach((key, deque) -> {
            if (deque == null || deque.isEmpty()) {
                return;
            }

            CompoundTag shopTag = new CompoundTag();
            shopTag.putString(NBT_KEY, key);
            ListTag entriesTag = new ListTag();
            for (TransactionLogEntry entry : deque) {
                entriesTag.add(entry.toTag(registries));
            }
            shopTag.put(NBT_ENTRIES, entriesTag);
            shops.add(shopTag);
        });

        tag.put(NBT_SHOPS, shops);
        return tag;
    }

    public static ShopTransactionLogSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public List<TransactionLogEntry> getEntries(String shopType, ResourceKey<Level> dimension, BlockPos pos, int limit) {
        String key = shopKey(shopType, dimension, pos);
        ArrayDeque<TransactionLogEntry> deque = logsByShop.get(key);
        if (deque == null || deque.isEmpty()) {
            return List.of();
        }

        int effectiveLimit = limit <= 0 ? deque.size() : Math.min(limit, deque.size());
        List<TransactionLogEntry> result = new ArrayList<>(effectiveLimit);
        int count = 0;
        for (TransactionLogEntry entry : deque) {
            if (count >= effectiveLimit) {
                break;
            }
            result.add(entry);
            count++;
        }
        return List.copyOf(result);
    }

    public void appendEntry(String shopType, ResourceKey<Level> dimension, BlockPos pos, TransactionLogEntry entry, int maxEntries) {
        if (entry == null) {
            return;
        }

        String key = shopKey(shopType, dimension, pos);
        ArrayDeque<TransactionLogEntry> deque = logsByShop.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        // --- Smart Stacking Logik ---
        TransactionLogEntry last = deque.peekFirst();
        if (last != null && last.canMergeWith(entry)) {
            // Nimm den alten Eintrag raus und füge den kombinierten hinzu
            deque.pollFirst();
            deque.addFirst(last.mergeWith(entry));
        } else {
            // Regulär hinzufügen
            deque.addFirst(entry);
        }

        int effectiveMaxEntries = maxEntries <= 0 ? DEFAULT_MAX_ENTRIES_PER_SHOP : maxEntries;
        while (deque.size() > effectiveMaxEntries) {
            deque.removeLast();
        }

        setDirty();
    }

    public boolean clearEntries(String shopType, ResourceKey<Level> dimension, BlockPos pos) {
        String key = shopKey(shopType, dimension, pos);
        boolean removed = logsByShop.remove(key) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    private static String shopKey(String shopType, ResourceKey<Level> dimension, BlockPos pos) {
        String normalizedShopType = normalizeShopType(shopType);
        return normalizedShopType + "|" + dimension.location() + "|" + pos.asLong();
    }

    private static String normalizeShopType(String shopType) {
        if (shopType == null || shopType.isBlank()) {
            return "unknown";
        }
        return shopType.trim().toLowerCase(Locale.ROOT);
    }
}
