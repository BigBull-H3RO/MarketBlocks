package de.bigbull.marketblocks.shop.singleoffer.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShopOwnerManager {
    private static final String NBT_OWNER_ID = "OwnerId";
    private static final String NBT_OWNER_NAME = "OwnerName";
    private static final String NBT_ADDITIONAL_OWNERS = "AdditionalOwners";
    private static final String NBT_ADDITIONAL_OWNER_ID = "Id";
    private static final String NBT_ADDITIONAL_OWNER_NAME = "Name";

    private final SingleOfferShopBlockEntity blockEntity;
    private UUID ownerId = null;
    private String ownerName = "";
    private final Map<UUID, String> additionalOwners = new HashMap<>();

    public ShopOwnerManager(SingleOfferShopBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void setOwner(Player player) {
        this.ownerId = player.getUUID();
        this.ownerName = player.getName().getString();
        blockEntity.sync();
    }

    public void addOwner(UUID id, String name) {
        additionalOwners.put(id, name);
        blockEntity.sync();
    }

    public void addOwnerClient(UUID id, String name) {
        additionalOwners.put(id, name);
    }

    public void removeOwner(UUID id) {
        if (additionalOwners.remove(id) != null) {
            blockEntity.sync();
        }
    }

    public Set<UUID> getOwners() {
        Set<UUID> owners = new HashSet<>(additionalOwners.keySet());
        if (ownerId != null) {
            owners.add(ownerId);
        }
        return owners;
    }

    public Map<UUID, String> getAdditionalOwners() {
        return additionalOwners;
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        additionalOwners.clear();
        additionalOwners.putAll(owners);
        blockEntity.sync();
    }

    public boolean isOwner(Player player) {
        UUID id = player.getUUID();
        return (ownerId != null && ownerId.equals(id)) || additionalOwners.containsKey(id);
    }

    public boolean isPrimaryOwner(Player player) {
        if (player == null) {
            return false;
        }
        return ownerId != null && ownerId.equals(player.getUUID());
    }

    public void ensureOwner(Player player) {
        if (!player.level().isClientSide() && ownerId == null) {
            setOwner(player);
        }
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void load(CompoundTag tag) {
        ownerId = tag.hasUUID(NBT_OWNER_ID) ? tag.getUUID(NBT_OWNER_ID) : null;
        ownerName = tag.getString(NBT_OWNER_NAME);
        additionalOwners.clear();
        if (tag.contains(NBT_ADDITIONAL_OWNERS, 9)) { // 9 = ListTag
            ListTag list = tag.getList(NBT_ADDITIONAL_OWNERS, 10); // 10 = CompoundTag
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                UUID id = entry.getUUID(NBT_ADDITIONAL_OWNER_ID);
                String name = entry.getString(NBT_ADDITIONAL_OWNER_NAME);
                additionalOwners.put(id, name);
            }
        }
    }

    public void save(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID(NBT_OWNER_ID, ownerId);
        }
        tag.putString(NBT_OWNER_NAME, ownerName);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, String> e : additionalOwners.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(NBT_ADDITIONAL_OWNER_ID, e.getKey());
            entry.putString(NBT_ADDITIONAL_OWNER_NAME, e.getValue());
            list.add(entry);
        }
        tag.put(NBT_ADDITIONAL_OWNERS, list);
    }
}

