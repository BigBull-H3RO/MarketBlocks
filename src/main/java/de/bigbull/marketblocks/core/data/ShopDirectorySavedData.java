package de.bigbull.marketblocks.core.data;

import net.minecraft.core.HolderLookup;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import de.bigbull.marketblocks.feature.singleoffer.settings.ShopCategory;

import java.util.Random;
import net.minecraft.network.chat.Component;
import de.bigbull.marketblocks.core.config.Config;

/**
 * Maintains a global registry of all placed single-offer shops (Trade Stands, Market Crates) across the server.
 * This is used for commands (like /marketblocks list) and other global lookup features.
 */
public class ShopDirectorySavedData extends SavedData {
    private static final String DATA_NAME = "marketblocks_shop_directory";

    private final List<ShopEntry> shops = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public record ShopEntry(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, String shopId, boolean isClosed, ShopCategory shopCategory, ItemStack payment1, ItemStack payment2, ItemStack result, int totalSales, boolean isAdminShop, boolean isMarketCrate, boolean hasShowcase, boolean isOutOfStock, boolean isOutputFull) {}

    public static ShopDirectorySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ShopDirectorySavedData::new, ShopDirectorySavedData::load),
                DATA_NAME
        );
    }

    public List<ShopEntry> getShops() {
        return List.copyOf(shops);
    }

    private String generateUniqueShopId() {
        String id;
        boolean unique;
        do {
            int num = RANDOM.nextInt(0x10000); // 0 to 65535
            id = String.format("%04X", num);
            String finalId = id;
            unique = shops.stream().noneMatch(s -> finalId.equals(s.shopId()));
        } while (!unique);
        return id;
    }

    public void registerOrUpdateShop(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, boolean isClosed, ShopCategory shopCategory, ItemStack payment1, ItemStack payment2, ItemStack result, int totalSales, boolean isAdminShop, boolean isMarketCrate, boolean hasShowcase, boolean isOutOfStock, boolean isOutputFull) {
        String existingId = null;
        for (ShopEntry s : shops) {
            if (s.pos().equals(pos)) {
                existingId = s.shopId();
                break;
            }
        }
        
        String shopId = existingId != null ? existingId : generateUniqueShopId();
        
        shops.removeIf(s -> s.pos().equals(pos));
        shops.add(new ShopEntry(pos, ownerUUID, ownerName, shopName, shopId, isClosed, shopCategory, payment1, payment2, result, totalSales, isAdminShop, isMarketCrate, hasShowcase, isOutOfStock, isOutputFull));
        setDirty();
    }

    public void unregisterShop(GlobalPos pos) {
        if (shops.removeIf(s -> s.pos().equals(pos))) {
            setDirty();
        }
    }

    public static String formatShopName(String customName, String shopId) {
        if (customName == null || customName.isBlank()) {
            return Component.translatable("gui.marketblocks.shop.default_name", shopId).getString();
        } else if (Config.SHOW_SHOP_ID_WITH_NAME.get()) {
            return Component.translatable("gui.marketblocks.shop.named_format", customName, shopId).getString();
        }
        return customName;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
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
            shopTag.putString("ShopId", entry.shopId() == null ? "" : entry.shopId());
            shopTag.putBoolean("IsClosed", entry.isClosed());
            shopTag.putString("ShopCategory", entry.shopCategory() != null ? entry.shopCategory().getId() : ShopCategory.NONE.getId());
            shopTag.putInt("TotalSales", entry.totalSales());
            shopTag.putBoolean("IsAdminShop", entry.isAdminShop());
            shopTag.putBoolean("IsMarketCrate", entry.isMarketCrate());
            shopTag.putBoolean("HasShowcase", entry.hasShowcase());
            shopTag.putBoolean("IsOutOfStock", entry.isOutOfStock());
            shopTag.putBoolean("IsOutputFull", entry.isOutputFull());

            if (!entry.payment1().isEmpty()) {
                shopTag.put("Payment1", entry.payment1().save(registries));
            }
            if (!entry.payment2().isEmpty()) {
                shopTag.put("Payment2", entry.payment2().save(registries));
            }
            if (!entry.result().isEmpty()) {
                shopTag.put("Result", entry.result().save(registries));
            }

            list.add(shopTag);
        }
        tag.put("Shops", list);
        return tag;
    }

    public static ShopDirectorySavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ShopDirectorySavedData data = new ShopDirectorySavedData();
        ListTag list = tag.getList("Shops", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag shopTag = list.getCompound(i);
            if (shopTag.contains("Pos")) {
                GlobalPos.CODEC.parse(NbtOps.INSTANCE, shopTag.get("Pos")).result().ifPresent(pos -> {
                    UUID ownerUUID = shopTag.contains("OwnerUUID") ? shopTag.getUUID("OwnerUUID") : null;
                    String ownerName = shopTag.getString("OwnerName");
                    String shopName = shopTag.getString("ShopName");
                    String shopId = shopTag.contains("ShopId") && !shopTag.getString("ShopId").isEmpty() ? shopTag.getString("ShopId") : data.generateUniqueShopId();
                    boolean isClosed = shopTag.getBoolean("IsClosed");
                    ShopCategory shopCategory = shopTag.contains("ShopCategory") ? ShopCategory.fromId(shopTag.getString("ShopCategory")) : ShopCategory.NONE;
                    boolean isAdminShop = shopTag.getBoolean("IsAdminShop");
                    boolean isMarketCrate = shopTag.getBoolean("IsMarketCrate");
                    boolean hasShowcase = shopTag.getBoolean("HasShowcase");
                    boolean isOutOfStock = shopTag.getBoolean("IsOutOfStock");
                    boolean isOutputFull = shopTag.getBoolean("IsOutputFull");

                    ItemStack payment1 = shopTag.contains("Payment1") ? ItemStack.parseOptional(registries, shopTag.getCompound("Payment1")) : ItemStack.EMPTY;
                    ItemStack payment2 = shopTag.contains("Payment2") ? ItemStack.parseOptional(registries, shopTag.getCompound("Payment2")) : ItemStack.EMPTY;
                    ItemStack result = shopTag.contains("Result") ? ItemStack.parseOptional(registries, shopTag.getCompound("Result")) : ItemStack.EMPTY;
                    int totalSales = shopTag.getInt("TotalSales");

                    data.shops.add(new ShopEntry(pos, ownerUUID, ownerName, shopName, shopId, isClosed, shopCategory, payment1, payment2, result, totalSales, isAdminShop, isMarketCrate, hasShowcase, isOutOfStock, isOutputFull));
                });
            }
        }
        return data;
    }
}
