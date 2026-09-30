package de.bigbull.marketblocks.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.feature.marketplace.network.LinkedBlocksSyncPacket;
import de.bigbull.marketblocks.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence of physical blocks that are linked to the Marketplace.
 * This allows players to click a physical block in the world to open the global Marketplace UI.
 * 
 * Data is stored globally in the overworld to ensure it persists regardless of the dimension it's accessed from.
 */
public class MarketplaceLinkSavedData extends SavedData {
    public static final String DATA_NAME = "marketblocks_marketplace_links";

    public static class LinkInfo {
        public final GlobalPos blockPos;
        public String name;
        public Vec3 tpPos;
        public Float tpYaw;
        public Float tpPitch;

        public LinkInfo(GlobalPos blockPos, String name, Vec3 tpPos, Float tpYaw, Float tpPitch) {
            this.blockPos = blockPos;
            this.name = name;
            this.tpPos = tpPos;
            this.tpYaw = tpYaw;
            this.tpPitch = tpPitch;
        }
    }

    public static final Codec<LinkInfo> LINK_INFO_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("BlockPos").forGetter(info -> info.blockPos),
            Codec.STRING.optionalFieldOf("Name").forGetter(info -> Optional.ofNullable(info.name)),
            Codec.DOUBLE.optionalFieldOf("TpX").forGetter(info -> info.tpPos != null ? Optional.of(info.tpPos.x) : Optional.empty()),
            Codec.DOUBLE.optionalFieldOf("TpY").forGetter(info -> info.tpPos != null ? Optional.of(info.tpPos.y) : Optional.empty()),
            Codec.DOUBLE.optionalFieldOf("TpZ").forGetter(info -> info.tpPos != null ? Optional.of(info.tpPos.z) : Optional.empty()),
            Codec.FLOAT.optionalFieldOf("TpYaw").forGetter(info -> Optional.ofNullable(info.tpYaw)),
            Codec.FLOAT.optionalFieldOf("TpPitch").forGetter(info -> Optional.ofNullable(info.tpPitch))
    ).apply(instance, (pos, name, tpx, tpy, tpz, yaw, pitch) -> {
        Vec3 tp = (tpx.isPresent() && tpy.isPresent() && tpz.isPresent()) ? new Vec3(tpx.get(), tpy.get(), tpz.get()) : null;
        return new LinkInfo(pos, name.orElse(null), tp, yaw.orElse(null), pitch.orElse(null));
    }));

    public static final Codec<MarketplaceLinkSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LINK_INFO_CODEC.listOf().optionalFieldOf("LinkedBlocks", List.of())
                    .forGetter(data -> new ArrayList<>(data.linkedBlocks.values()))
    ).apply(instance, list -> {
        MarketplaceLinkSavedData data = new MarketplaceLinkSavedData();
        for (LinkInfo info : list) {
            data.linkedBlocks.put(info.blockPos, info);
        }
        return data;
    }));

    public static final SavedDataType<MarketplaceLinkSavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            MarketplaceLinkSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<GlobalPos, LinkInfo> linkedBlocks = new HashMap<>();

    public MarketplaceLinkSavedData() {
    }

    public static MarketplaceLinkSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isLinked(GlobalPos pos) {
        return linkedBlocks.containsKey(pos);
    }

    public Map<GlobalPos, LinkInfo> getLinkedBlocks() {
        return Collections.unmodifiableMap(linkedBlocks);
    }

    public boolean addLink(GlobalPos pos, String name, Vec3 tpPos, Float tpYaw, Float tpPitch) {
        if (!linkedBlocks.containsKey(pos)) {
            linkedBlocks.put(pos, new LinkInfo(pos, name, tpPos, tpYaw, tpPitch));
            setDirty();
            return true;
        }
        return false;
    }

    public boolean removeLink(GlobalPos pos) {
        if (linkedBlocks.remove(pos) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public int removeLinkByName(String name) {
        int count = 0;
        Iterator<Map.Entry<GlobalPos, LinkInfo>> iterator = linkedBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<GlobalPos, LinkInfo> entry = iterator.next();
            String linkName = entry.getValue().name;
            BlockPos pos = entry.getKey().pos();
            String coordName = pos.getX() + "_" + pos.getY() + "_" + pos.getZ();

            if (name.equals(linkName) || name.equals(coordName)) {
                iterator.remove();
                count++;
            }
        }
        if (count > 0) {
            setDirty();
        }
        return count;
    }

    public void syncToAll(MinecraftServer server) {
        LinkedBlocksSyncPacket packet = new LinkedBlocksSyncPacket(new ArrayList<>(linkedBlocks.keySet()));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendToPlayer(player, packet);
        }
    }

    public void syncToPlayer(ServerPlayer player) {
        NetworkHandler.sendToPlayer(player, new LinkedBlocksSyncPacket(new ArrayList<>(linkedBlocks.keySet())));
    }
}
