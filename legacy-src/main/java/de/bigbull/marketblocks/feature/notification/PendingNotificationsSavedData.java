package de.bigbull.marketblocks.feature.notification;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Saved data for tracking pending shop notifications such as out of stock or output full.
 */
public class PendingNotificationsSavedData extends SavedData {
    public static final String DATA_NAME = MarketBlocks.MODID + "_pending_notifications";

    private final Map<UUID, Set<BlockPos>> outOfStockShops = new HashMap<>();
    private final Map<UUID, Set<BlockPos>> outputFullShops = new HashMap<>();

    public PendingNotificationsSavedData() {
    }

    public static PendingNotificationsSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PendingNotificationsSavedData::new, PendingNotificationsSavedData::load, null),
                DATA_NAME);
    }

    public void addOutOfStock(UUID player, BlockPos pos) {
        outOfStockShops.computeIfAbsent(player, k -> new HashSet<>()).add(pos);
        setDirty();
    }

    public void addOutputFull(UUID player, BlockPos pos) {
        outputFullShops.computeIfAbsent(player, k -> new HashSet<>()).add(pos);
        setDirty();
    }

    public Set<BlockPos> getAndClearOutOfStock(UUID player) {
        Set<BlockPos> pos = outOfStockShops.remove(player);
        if (pos != null)
            setDirty();
        return pos == null ? Set.of() : pos;
    }

    public Set<BlockPos> getAndClearOutputFull(UUID player) {
        Set<BlockPos> pos = outputFullShops.remove(player);
        if (pos != null)
            setDirty();
        return pos == null ? Set.of() : pos;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("OutOfStock", saveMap(outOfStockShops));
        tag.put("OutputFull", saveMap(outputFullShops));
        return tag;
    }

    public static PendingNotificationsSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        PendingNotificationsSavedData data = new PendingNotificationsSavedData();
        loadMap(tag, "OutOfStock", data.outOfStockShops);
        loadMap(tag, "OutputFull", data.outputFullShops);
        return data;
    }

    private static ListTag saveMap(Map<UUID, Set<BlockPos>> map) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Set<BlockPos>> entry : map.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());
            ListTag posList = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", pos.getX());
                posTag.putInt("Y", pos.getY());
                posTag.putInt("Z", pos.getZ());
                posList.add(posTag);
            }
            entryTag.put("Positions", posList);
            list.add(entryTag);
        }
        return list;
    }

    private static void loadMap(CompoundTag parentTag, String key, Map<UUID, Set<BlockPos>> map) {
        if (!parentTag.contains(key, Tag.TAG_LIST))
            return;
        ListTag list = parentTag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            if (entryTag.hasUUID("UUID")) {
                UUID uuid = entryTag.getUUID("UUID");
                Set<BlockPos> posSet = new HashSet<>();
                ListTag posList = entryTag.getList("Positions", Tag.TAG_COMPOUND);
                for (int j = 0; j < posList.size(); j++) {
                    CompoundTag posTag = posList.getCompound(j);
                    posSet.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
                }
                map.put(uuid, posSet);
            }
        }
    }
}
