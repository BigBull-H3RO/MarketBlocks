package de.bigbull.marketblocks.feature.singleoffer.entity;

import de.bigbull.marketblocks.feature.singleoffer.settings.AccessMode;
import de.bigbull.marketblocks.feature.singleoffer.settings.AccessSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShopAccessManager {
    private final SingleOfferShopBlockEntity shop;
    @Nullable
    public UUID purchaseContextBuyerId;
    public String purchaseContextBuyerName = "";

    public ShopAccessManager(SingleOfferShopBlockEntity shop) {
        this.shop = shop;
    }

    public void setOwner(Player player) {
        shop.getSettingsManager().setAccessSettings(
                shop.getSettingsManager().getAccessSettings().withOwner(player.getUUID(), player.getName().getString()),
                true);
        if (shop.getLevel() != null && !shop.getLevel().isClientSide) {
            shop.updateShopDirectory();
        }
    }

    public void addOwner(UUID id, String name) {
        Map<UUID, String> newOwners = new HashMap<>(shop.getSettingsManager().getAccessSettings().additionalOwners());
        newOwners.put(id, name);
        shop.getSettingsManager()
                .setAccessSettings(shop.getSettingsManager().getAccessSettings().withAdditionalOwners(newOwners), true);
    }

    @ApiStatus.Internal
    public void addOwnerClient(UUID id, String name) {
        Map<UUID, String> newOwners = new HashMap<>(shop.getSettingsManager().getAccessSettings().additionalOwners());
        newOwners.put(id, name);
        shop.getSettingsManager().setAccessSettings(
                shop.getSettingsManager().getAccessSettings().withAdditionalOwners(newOwners), false);
    }

    public void removeOwner(UUID id) {
        Map<UUID, String> newOwners = new HashMap<>(shop.getSettingsManager().getAccessSettings().additionalOwners());
        if (newOwners.remove(id) != null) {
            shop.getSettingsManager().setAccessSettings(
                    shop.getSettingsManager().getAccessSettings().withAdditionalOwners(newOwners), true);
        }
    }

    public Set<UUID> getOwners() {
        Set<UUID> set = new HashSet<>();
        UUID ownerId = shop.getSettingsManager().getAccessSettings().ownerId();
        if (ownerId != null)
            set.add(ownerId);
        set.addAll(shop.getSettingsManager().getAccessSettings().additionalOwners().keySet());
        return set;
    }

    public Map<UUID, String> getAdditionalOwners() {
        return new HashMap<>(shop.getSettingsManager().getAccessSettings().additionalOwners());
    }

    public void setAdditionalOwners(Map<UUID, String> owners) {
        shop.getSettingsManager()
                .setAccessSettings(shop.getSettingsManager().getAccessSettings().withAdditionalOwners(owners), true);
    }

    public boolean isOwner(Player player) {
        if (shop.isAdminShopEnabled() && player.hasPermissions(2))
            return true;
        return isOwnerByUUID(player.getUUID());
    }

    public boolean isOwnerByUUID(UUID uuid) {
        AccessSettings acc = shop.getSettingsManager().getAccessSettings();
        return uuid.equals(acc.ownerId()) || acc.additionalOwners().containsKey(uuid);
    }

    public boolean canPlayerBuy(Player player) {
        if (player == null)
            return !shop.getGeneralSettings().isClosed();
        if (shop.isAdminShopEnabled() && player.hasPermissions(2))
            return true;
        return canPlayerBuyByUUID(player.getUUID());
    }

    public boolean canPlayerBuyByUUID(UUID uuid) {
        if (shop.getGeneralSettings().isClosed()) {
            return false;
        }
        if (uuid == null || isOwnerByUUID(uuid)) {
            return true;
        }
        AccessSettings acc = shop.getAccessSettings();
        AccessMode mode = acc.accessMode();
        if (mode == AccessMode.WHITELIST) {
            return acc.accessList().containsKey(uuid);
        } else if (mode == AccessMode.BLACKLIST) {
            return !acc.accessList().containsKey(uuid);
        }
        return true;
    }

    public boolean isPrimaryOwner(Player player) {
        if (shop.isAdminShopEnabled() && player.hasPermissions(2))
            return true;
        return player.getUUID().equals(shop.getSettingsManager().getAccessSettings().ownerId());
    }

    public void ensureOwner(Player player) {
        if (shop.getSettingsManager().getAccessSettings().ownerId() == null) {
            setOwner(player);
        }
    }

    public UUID getOwnerId() {
        return shop.getSettingsManager().getAccessSettings().ownerId();
    }

    public String getOwnerName() {
        return shop.getSettingsManager().getAccessSettings().ownerName();
    }

    public void beginPurchaseContext(@Nullable Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }
        purchaseContextBuyerId = player.getUUID();
        purchaseContextBuyerName = player.getGameProfile().getName();
    }

    public void clearPurchaseContext() {
        purchaseContextBuyerId = null;
        purchaseContextBuyerName = "";
    }

    public ContainerData createMenuFlags(Player player) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    int flags = 0;
                    if (shop.hasOffer())
                        flags |= SingleOfferShopBlockEntity.HAS_OFFER_FLAG;
                    if (shop.isOfferAvailable())
                        flags |= SingleOfferShopBlockEntity.OFFER_AVAILABLE_FLAG;
                    if (isOwner(player))
                        flags |= SingleOfferShopBlockEntity.OWNER_FLAG;
                    if (isPrimaryOwner(player))
                        flags |= SingleOfferShopBlockEntity.PRIMARY_OWNER_FLAG;
                    if (player != null && player.hasPermissions(2))
                        flags |= SingleOfferShopBlockEntity.OPERATOR_FLAG;
                    if (shop.isGlobalAdminModeEnabled())
                        flags |= SingleOfferShopBlockEntity.GLOBAL_ADMIN_MODE_FLAG;
                    return flags;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }
}
