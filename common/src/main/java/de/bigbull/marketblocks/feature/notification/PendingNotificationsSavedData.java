package de.bigbull.marketblocks.feature.notification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Saved data for tracking pending shop notifications such as out of stock or output full.
 */
public class PendingNotificationsSavedData extends SavedData {
    public static final Identifier DATA_NAME = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pending_notifications");

    private final Map<UUID, Set<BlockPos>> outOfStockShops = new HashMap<>();
    private final Map<UUID, Set<BlockPos>> outputFullShops = new HashMap<>();

    public static final Codec<Set<BlockPos>> BLOCK_POS_SET_CODEC = BlockPos.CODEC.listOf().<Set<BlockPos>>xmap(HashSet::new, ArrayList::new);

    public static final Codec<PendingNotificationsSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BLOCK_POS_SET_CODEC)
                    .optionalFieldOf("OutOfStock", Map.of())
                    .forGetter(data -> data.outOfStockShops),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BLOCK_POS_SET_CODEC)
                    .optionalFieldOf("OutputFull", Map.of())
                    .forGetter(data -> data.outputFullShops)
    ).apply(instance, (outOfStock, outputFull) -> {
        PendingNotificationsSavedData data = new PendingNotificationsSavedData();
        data.outOfStockShops.putAll(outOfStock);
        data.outputFullShops.putAll(outputFull);
        return data;
    }));

    public static final SavedDataType<PendingNotificationsSavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            PendingNotificationsSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public PendingNotificationsSavedData() {
    }

    public static PendingNotificationsSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
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
}
