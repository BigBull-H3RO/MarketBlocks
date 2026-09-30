package de.bigbull.marketblocks.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.singleoffer.settings.ShopCategory;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Maintains a global registry of all placed single-offer shops (Trade Stands, Market Crates) across the server.
 * This is used for commands (like /marketblocks list) and other global lookup features.
 * Uses a LinkedHashMap for O(1) lookups by position while preserving insertion order.
 */
public class ShopDirectorySavedData extends SavedData {
    public static final String DATA_NAME = "marketblocks_shop_directory";

    private final Map<GlobalPos, ShopEntry> shopsByPos = new LinkedHashMap<>();
    private static final Random RANDOM = new Random();

    public record ShopEntry(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, String shopId, boolean isClosed, ShopCategory shopCategory, ItemStack payment1, ItemStack payment2, ItemStack result, int totalSales, boolean isAdminShop, boolean isMarketCrate, boolean hasShowcase, boolean isOutOfStock, boolean isOutputFull) {}

    public static final Codec<ShopEntry> SHOP_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("Pos").forGetter(ShopEntry::pos),
            UUIDUtil.CODEC.optionalFieldOf("OwnerUUID").forGetter(e -> Optional.ofNullable(e.ownerUUID())),
            Codec.STRING.optionalFieldOf("OwnerName", "").forGetter(ShopEntry::ownerName),
            Codec.STRING.optionalFieldOf("ShopName", "").forGetter(ShopEntry::shopName),
            Codec.STRING.optionalFieldOf("ShopId", "").forGetter(ShopEntry::shopId),
            Codec.BOOL.optionalFieldOf("IsClosed", false).forGetter(ShopEntry::isClosed),
            ShopCategory.CODEC.optionalFieldOf("ShopCategory", ShopCategory.NONE).forGetter(ShopEntry::shopCategory),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("Payment1", ItemStack.EMPTY).forGetter(ShopEntry::payment1),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("Payment2", ItemStack.EMPTY).forGetter(ShopEntry::payment2),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("Result", ItemStack.EMPTY).forGetter(ShopEntry::result),
            Codec.INT.optionalFieldOf("TotalSales", 0).forGetter(ShopEntry::totalSales),
            Codec.BOOL.optionalFieldOf("IsAdminShop", false).forGetter(ShopEntry::isAdminShop),
            Codec.BOOL.optionalFieldOf("IsMarketCrate", false).forGetter(ShopEntry::isMarketCrate),
            Codec.BOOL.optionalFieldOf("HasShowcase", false).forGetter(ShopEntry::hasShowcase),
            Codec.BOOL.optionalFieldOf("IsOutOfStock", false).forGetter(ShopEntry::isOutOfStock),
            Codec.BOOL.optionalFieldOf("IsOutputFull", false).forGetter(ShopEntry::isOutputFull)
    ).apply(instance, (pos, ownerUuid, ownerName, shopName, shopId, isClosed, category, p1, p2, res, sales, admin, crate, showcase, outOfStock, outputFull) ->
            new ShopEntry(pos, ownerUuid.orElse(null), ownerName, shopName, shopId, isClosed, category, p1, p2, res, sales, admin, crate, showcase, outOfStock, outputFull)
    ));

    public static final Codec<ShopDirectorySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SHOP_ENTRY_CODEC.listOf().optionalFieldOf("Shops", List.of())
                    .forGetter(data -> new ArrayList<>(data.shopsByPos.values()))
    ).apply(instance, list -> {
        ShopDirectorySavedData data = new ShopDirectorySavedData();
        for (ShopEntry entry : list) {
            data.shopsByPos.put(entry.pos(), entry);
        }
        return data;
    }));

    public static final SavedDataType<ShopDirectorySavedData> TYPE = new SavedDataType<>(
            DATA_NAME,
            ShopDirectorySavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public ShopDirectorySavedData() {
    }

    public static ShopDirectorySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public List<ShopEntry> getShops() {
        return List.copyOf(shopsByPos.values());
    }

    public ShopEntry getShopById(String shopId) {
        if (shopId == null) return null;
        for (ShopEntry s : shopsByPos.values()) {
            if (shopId.equals(s.shopId())) {
                return s;
            }
        }
        return null;
    }

    private String generateUniqueShopId() {
        String id;
        boolean unique;
        do {
            int num = RANDOM.nextInt(0x10000); // 0 to 65535
            id = String.format("%04X", num);
            String finalId = id;
            unique = shopsByPos.values().stream().noneMatch(s -> finalId.equals(s.shopId()));
        } while (!unique);
        return id;
    }

    public String registerOrUpdateShop(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, boolean isClosed, ShopCategory shopCategory, ItemStack payment1, ItemStack payment2, ItemStack result, int totalSales, boolean isAdminShop, boolean isMarketCrate, boolean hasShowcase, boolean isOutOfStock, boolean isOutputFull, String preferredShopId) {
        ShopEntry existing = shopsByPos.get(pos);
        String shopId;
        if (existing != null && existing.shopId() != null && !existing.shopId().isEmpty()) {
            shopId = existing.shopId();
        } else if (preferredShopId != null && !preferredShopId.isEmpty() && isShopIdAvailable(pos, preferredShopId)) {
            shopId = preferredShopId;
        } else {
            shopId = generateUniqueShopId();
        }

        shopsByPos.put(pos, new ShopEntry(pos, ownerUUID, ownerName, shopName, shopId, isClosed, shopCategory, payment1, payment2, result, totalSales, isAdminShop, isMarketCrate, hasShowcase, isOutOfStock, isOutputFull));
        setDirty();
        return shopId;
    }

    public String registerOrUpdateShop(GlobalPos pos, UUID ownerUUID, String ownerName, String shopName, boolean isClosed, ShopCategory shopCategory, ItemStack payment1, ItemStack payment2, ItemStack result, int totalSales, boolean isAdminShop, boolean isMarketCrate, boolean hasShowcase, boolean isOutOfStock, boolean isOutputFull) {
        return registerOrUpdateShop(pos, ownerUUID, ownerName, shopName, isClosed, shopCategory, payment1, payment2, result, totalSales, isAdminShop, isMarketCrate, hasShowcase, isOutOfStock, isOutputFull, null);
    }

    private boolean isShopIdAvailable(GlobalPos pos, String id) {
        ShopEntry match = getShopById(id);
        return match == null || match.pos().equals(pos);
    }

    public void unregisterShop(GlobalPos pos) {
        if (shopsByPos.remove(pos) != null) {
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
}
