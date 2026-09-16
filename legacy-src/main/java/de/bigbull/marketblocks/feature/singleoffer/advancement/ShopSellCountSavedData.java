package de.bigbull.marketblocks.feature.singleoffer.advancement;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent per-player sell count tracker for the "Trade Tycoon" advancement.
 * Stored per-world so the count survives server restarts.
 */
public class ShopSellCountSavedData extends SavedData {
    public static final String DATA_NAME = MarketBlocks.MODID + "_sell_counts";

    private final Map<UUID, Integer> sellCounts = new HashMap<>();

    public ShopSellCountSavedData() {
    }

    public static ShopSellCountSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ShopSellCountSavedData::new, ShopSellCountSavedData::load, null),
                DATA_NAME);
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : sellCounts.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            entryTag.putInt("Count", entry.getValue());
            list.add(entryTag);
        }
        tag.put("SellCounts", list);
        return tag;
    }

    public static ShopSellCountSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ShopSellCountSavedData data = new ShopSellCountSavedData();
        if (tag.contains("SellCounts", Tag.TAG_LIST)) {
            ListTag list = tag.getList("SellCounts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                if (entryTag.hasUUID("UUID")) {
                    data.sellCounts.put(entryTag.getUUID("UUID"), entryTag.getInt("Count"));
                }
            }
        }
        return data;
    }
}
