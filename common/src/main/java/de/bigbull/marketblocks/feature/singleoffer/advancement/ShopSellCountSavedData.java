package de.bigbull.marketblocks.feature.singleoffer.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.Constants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent per-player sell count tracker for the "Trade Tycoon" advancement.
 * Stored per-world so the count survives server restarts.
 */
public class ShopSellCountSavedData extends SavedData {
    public static final String DATA_NAME = Constants.MOD_ID + "_sell_counts";

    private final Map<UUID, Integer> sellCounts = new HashMap<>();

    public static final Codec<ShopSellCountSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT)
                    .optionalFieldOf("SellCounts", Map.of())
                    .forGetter(data -> data.sellCounts)
    ).apply(instance, map -> {
        ShopSellCountSavedData data = new ShopSellCountSavedData();
        data.sellCounts.putAll(map);
        return data;
    }));

    public static final SavedDataType<ShopSellCountSavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            ShopSellCountSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public ShopSellCountSavedData() {
    }

    public static ShopSellCountSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Increments the sell count for the given player and returns the new total.
     */
    public int incrementAndGet(UUID playerId, int amount) {
        int newCount = sellCounts.merge(playerId, amount, Integer::sum);
        setDirty();
        return newCount;
    }

    public int getCount(UUID playerId) {
        return sellCounts.getOrDefault(playerId, 0);
    }
}
