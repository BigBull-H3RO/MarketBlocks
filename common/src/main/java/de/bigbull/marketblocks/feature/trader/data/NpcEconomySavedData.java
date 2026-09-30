package de.bigbull.marketblocks.feature.trader.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.core.config.TraderConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistently tracks NPC market saturation (supply/demand dynamics) per world.
 * Automatically handles time-based decay of saturation based on world game time.
 */
public class NpcEconomySavedData extends SavedData {
    public static final String DATA_NAME = Constants.MOD_ID + "_npc_economy";

    private final Map<Item, Double> itemSaturation = new HashMap<>();
    private long lastDecayGameTime = 0L;

    private record ItemSaturationEntry(Item item, double saturation) {
        public static final Codec<ItemSaturationEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("Item").forGetter(ItemSaturationEntry::item),
                Codec.DOUBLE.fieldOf("Saturation").forGetter(ItemSaturationEntry::saturation)
        ).apply(instance, ItemSaturationEntry::new));
    }

    public static final Codec<NpcEconomySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemSaturationEntry.CODEC.listOf().optionalFieldOf("SaturationPool", List.of())
                    .forGetter(data -> {
                        List<ItemSaturationEntry> list = new ArrayList<>();
                        data.itemSaturation.forEach((k, v) -> list.add(new ItemSaturationEntry(k, v)));
                        return list;
                    }),
            Codec.LONG.optionalFieldOf("LastDecayGameTime", 0L).forGetter(data -> data.lastDecayGameTime)
    ).apply(instance, (entries, lastDecay) -> {
        NpcEconomySavedData data = new NpcEconomySavedData();
        for (ItemSaturationEntry entry : entries) {
            data.itemSaturation.put(entry.item(), entry.saturation());
        }
        data.lastDecayGameTime = lastDecay;
        return data;
    }));

    public static final SavedDataType<NpcEconomySavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            NpcEconomySavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public NpcEconomySavedData() {
    }

    public static NpcEconomySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
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
}
