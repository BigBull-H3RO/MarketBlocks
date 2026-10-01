package de.bigbull.marketblocks.feature.log;

import de.bigbull.marketblocks.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Central transaction-log storage for all shop types.
 * Persisted as SavedData (world/data/*.dat), not inside chunk NBT.
 */
public final class ShopTransactionLogSavedData extends SavedData {
    private static final String NBT_SHOPS = "Shops";

    public static final String SINGLE_OFFER_SHOP_TYPE = "single_offer_shop";
    public static final int DEFAULT_MAX_ENTRIES_PER_SHOP = 100;
    public static final Identifier DATA_NAME = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "shop_logs");

    public static final Codec<ShopTransactionLogSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, TransactionLogEntry.CODEC.listOf())
                    .optionalFieldOf(NBT_SHOPS, Map.of())
                    .forGetter(data -> {
                        Map<String, List<TransactionLogEntry>> map = new HashMap<>();
                        data.logsByShop.forEach((k, v) -> map.put(k, new ArrayList<>(v)));
                        return map;
                    })
    ).apply(instance, map -> {
        ShopTransactionLogSavedData data = new ShopTransactionLogSavedData();
        map.forEach((k, v) -> data.logsByShop.put(k, new ArrayDeque<>(v)));
        return data;
    }));

    public static final SavedDataType<ShopTransactionLogSavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            ShopTransactionLogSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<String, ArrayDeque<TransactionLogEntry>> logsByShop = new HashMap<>();

    public ShopTransactionLogSavedData() {
    }

    public static ShopTransactionLogSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
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

        TransactionLogEntry last = deque.peekFirst();
        if (last != null && last.canMergeWith(entry)) {
            deque.pollFirst();
            deque.addFirst(last.mergeWith(entry));
        } else {
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
        return normalizedShopType + "|" + dimension.identifier() + "|" + pos.asLong();
    }

    private static String normalizeShopType(String shopType) {
        if (shopType == null || shopType.isBlank()) {
            return "unknown";
        }
        return shopType.trim().toLowerCase(Locale.ROOT);
    }
}
