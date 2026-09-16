package de.bigbull.marketblocks.feature.trader.data;

import de.bigbull.marketblocks.MarketBlocks;

import de.bigbull.marketblocks.core.config.TraderConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistently tracks NPC market saturation (supply/demand dynamics) per world.
 * Automatically handles time-based decay of saturation based on world game time.
 */
public class NpcEconomySavedData extends SavedData {
    public static final String DATA_NAME = MarketBlocks.MODID + "_npc_economy";

    private final Map<Item, Double> itemSaturation = new HashMap<>();
    private long lastDecayGameTime = 0L;

    public NpcEconomySavedData() {
    }

    public static NpcEconomySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(NpcEconomySavedData::new, NpcEconomySavedData::load, null),
                DATA_NAME);
    }

    /**
     * Applies decay to all saturated items based on the elapsed world game ticks.
     */
    private void applyDecay(ServerLevel level) {
        long currentGameTime = level.getGameTime();
        if (lastDecayGameTime == 0L) {
            lastDecayGameTime = currentGameTime;
            setDirty();
            return;
        }

        long diff = currentGameTime - lastDecayGameTime;
        if (diff <= 0) {
            return;
        }

        double decayRate = TraderConfig.DYNAMIC_PRICING_DECAY_RATE.get();
        if (decayRate > 0 && !itemSaturation.isEmpty()) {
            double decayAmount = diff * decayRate;
            itemSaturation.entrySet().removeIf(entry -> {
                double newVal = entry.getValue() - decayAmount;
                if (newVal <= 0.0) {
                    return true;
                }
                entry.setValue(newVal);
                return false;
            });
        }

        lastDecayGameTime = currentGameTime;
        setDirty();
    }

    /**
     * Calculates the dynamic price multiplier for the given item.
     */
    public double getDemandMultiplier(Item item, ServerLevel level) {
        if (!TraderConfig.DYNAMIC_PRICING_ENABLED.get()) {
            return 1.0;
        }

        applyDecay(level);

        double saturation = itemSaturation.getOrDefault(item, 0.0);
        double multiplier = 1.0 - saturation;

        double min = TraderConfig.DYNAMIC_PRICING_MIN_MULTIPLIER.get();
        double max = TraderConfig.DYNAMIC_PRICING_MAX_MULTIPLIER.get();

        return Math.max(min, Math.min(max, multiplier));
    }

    /**
     * Returns a set of all items that currently have saturation > 0.
     */
    public java.util.Set<Item> getSaturatedItems() {
        return java.util.Collections.unmodifiableSet(itemSaturation.keySet());
    }

    /**
     * Registers a sale to an NPC, increasing the saturation and lowering future values.
     */
    public void registerSale(Item item, int amount, ServerLevel level) {
        if (!TraderConfig.DYNAMIC_PRICING_ENABLED.get() || amount <= 0) {
            return;
        }

        applyDecay(level);

        double extraSaturation = amount * TraderConfig.DYNAMIC_PRICING_SATURATION_PER_UNIT.get();
        // Cap saturation to prevent unbounded growth that would make decay take unreasonably long
        double maxSaturation = 1.0 - TraderConfig.DYNAMIC_PRICING_MIN_MULTIPLIER.get() + 0.1;
        double newVal = Math.min(maxSaturation, itemSaturation.getOrDefault(item, 0.0) + extraSaturation);
        itemSaturation.put(item, newVal);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<Item, Double> entry : itemSaturation.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
            entryTag.putString("Item", key.toString());
            entryTag.putDouble("Saturation", entry.getValue());
            list.add(entryTag);
        }
        tag.put("SaturationPool", list);
        tag.putLong("LastDecayGameTime", lastDecayGameTime);
        return tag;
    }

    public static NpcEconomySavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        NpcEconomySavedData data = new NpcEconomySavedData();
        if (tag.contains("SaturationPool", Tag.TAG_LIST)) {
            ListTag list = tag.getList("SaturationPool", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                if (entryTag.contains("Item", Tag.TAG_STRING)) {
                    ResourceLocation key = ResourceLocation.tryParse(entryTag.getString("Item"));
                    if (key != null && BuiltInRegistries.ITEM.containsKey(key)) {
                        Item item = BuiltInRegistries.ITEM.get(key);
                        data.itemSaturation.put(item, entryTag.getDouble("Saturation"));
                    }
                }
            }
        }
        if (tag.contains("LastDecayGameTime")) {
            data.lastDecayGameTime = tag.getLong("LastDecayGameTime");
        }
        return data;
    }
}
