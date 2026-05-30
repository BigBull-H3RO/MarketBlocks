package de.bigbull.marketblocks.core.data;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopDirectorySavedData extends SavedData {
    private static final String DATA_NAME = "marketblocks_shop_directory";

    private final List<ShopEntry> shops = new ArrayList<>();

    public record ShopEntry(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, boolean isClosed) {}

    public static ShopDirectorySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ShopDirectorySavedData::new, ShopDirectorySavedData::load),
                DATA_NAME
        );
    }

    public List<ShopEntry> getShops() {
        return List.copyOf(shops);
    }

    public void registerOrUpdateShop(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, boolean isClosed) {
        shops.removeIf(s -> s.pos().equals(pos));
        shops.add(new ShopEntry(pos, ownerUUID, ownerName, shopName, isClosed));
        setDirty();
    }

    public void unregisterShop(GlobalPos pos) {
        if (shops.removeIf(s -> s.pos().equals(pos))) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (ShopEntry entry : shops) {
            CompoundTag shopTag = new CompoundTag();
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, entry.pos()).result().ifPresent(nbt -> shopTag.put("Pos", nbt));
            if (entry.ownerUUID() != null) {
                shopTag.putUUID("OwnerUUID", entry.ownerUUID());
            }
            if (entry.ownerName() != null) {
                shopTag.putString("OwnerName", entry.ownerName());
            }
            shopTag.putString("ShopName", entry.shopName() == null ? "" : entry.shopName());
            shopTag.putBoolean("IsClosed", entry.isClosed());
            list.add(shopTag);
        }
        tag.put("Shops", list);
        return tag;
    }

    public static ShopDirectorySavedData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ShopDirectorySavedData data = new ShopDirectorySavedData();
        ListTag list = tag.getList("Shops", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag shopTag = list.getCompound(i);
            if (shopTag.contains("Pos")) {
                GlobalPos.CODEC.parse(NbtOps.INSTANCE, shopTag.get("Pos")).result().ifPresent(pos -> {
                    UUID ownerUUID = shopTag.contains("OwnerUUID") ? shopTag.getUUID("OwnerUUID") : null;
                    String ownerName = shopTag.getString("OwnerName");
                    String shopName = shopTag.getString("ShopName");
                    boolean isClosed = shopTag.getBoolean("IsClosed");
                    data.shops.add(new ShopEntry(pos, ownerUUID, ownerName, shopName, isClosed));
                });
            }
        }
        return data;
    }
}
