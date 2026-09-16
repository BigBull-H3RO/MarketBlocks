package de.bigbull.marketblocks.core.data;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.feature.marketplace.network.LinkedBlocksSyncPacket;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the persistence of physical blocks that are linked to the Marketplace.
 * This allows players to click a physical block in the world to open the global Marketplace UI.
 * 
 * Data is stored globally in the overworld to ensure it persists regardless of the dimension it's accessed from.
 */
public class MarketplaceLinkSavedData extends SavedData {

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

    private final Map<GlobalPos, LinkInfo> linkedBlocks = new HashMap<>();

    public static MarketplaceLinkSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MarketplaceLinkSavedData::new, MarketplaceLinkSavedData::load, null),
                "marketblocks_marketplace_links"
        );
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (LinkInfo info : linkedBlocks.values()) {
            CompoundTag itemTag = new CompoundTag();
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, info.blockPos).result().ifPresent(posTag -> {
                itemTag.put("BlockPos", posTag);
            });
            if (info.name != null) {
                itemTag.putString("Name", info.name);
            }
            if (info.tpPos != null) {
                itemTag.putDouble("TpX", info.tpPos.x);
                itemTag.putDouble("TpY", info.tpPos.y);
                itemTag.putDouble("TpZ", info.tpPos.z);
            }
            if (info.tpYaw != null) {
                itemTag.putFloat("TpYaw", info.tpYaw);
            }
            if (info.tpPitch != null) {
                itemTag.putFloat("TpPitch", info.tpPitch);
            }
            list.add(itemTag);
        }
        tag.put("LinkedBlocks", list);
        return tag;
    }

    public static MarketplaceLinkSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        MarketplaceLinkSavedData data = new MarketplaceLinkSavedData();
        ListTag list = tag.getList("LinkedBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            if (itemTag.contains("BlockPos")) {
                GlobalPos.CODEC.parse(NbtOps.INSTANCE, itemTag.get("BlockPos")).result().ifPresent(pos -> {
                    String name = itemTag.contains("Name") ? itemTag.getString("Name") : null;
                    Vec3 tpPos = null;
                    Float tpYaw = null;
                    Float tpPitch = null;
                    if (itemTag.contains("TpX")) {
                        tpPos = new Vec3(itemTag.getDouble("TpX"), itemTag.getDouble("TpY"), itemTag.getDouble("TpZ"));
                    }
                    if (itemTag.contains("TpYaw")) {
                        tpYaw = itemTag.getFloat("TpYaw");
                    }
                    if (itemTag.contains("TpPitch")) {
                        tpPitch = itemTag.getFloat("TpPitch");
                    }
                    data.linkedBlocks.put(pos, new LinkInfo(pos, name, tpPos, tpYaw, tpPitch));
                });
            }
        }
        return data;
    }
}

