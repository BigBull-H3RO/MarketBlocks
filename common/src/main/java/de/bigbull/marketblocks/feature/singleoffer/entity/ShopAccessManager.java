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
    public Player purchaseContextPlayer;
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
        if (player == null) return false;
        boolean adminEditMode = player.hasPermissions(2) && de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager.get().isEditModeEnabled(player);

        // If it's an Admin Shop:
        // ONLY an OP player with active Edit Mode can manage/edit it!
        if (shop.isAdminShopEnabled()) {
            return adminEditMode;
        }

        // If it's a normal Player Shop:
        // Admin with active Edit Mode can bypass ownership
        if (adminEditMode) {
            return true;
        }

        // Normal owner / co-owner
        return isOwnerByUUID(player.getUUID());
    }

    public boolean isOwnerByUUID(UUID uuid) {
        AccessSettings acc = shop.getSettingsManager().getAccessSettings();
        return uuid.equals(acc.ownerId()) || acc.additionalOwners().containsKey(uuid);
    }

    public boolean canPlayerBuy(Player player) {
        if (player == null)
            return !shop.getGeneralSettings().isClosed();
        if (player.hasPermissions(2) && de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager.get().isEditModeEnabled(player))
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
            if (acc.accessList().isEmpty()) {
                return true;
            }
            return acc.accessList().containsKey(uuid);
        } else if (mode == AccessMode.BLACKLIST) {
            return !acc.accessList().containsKey(uuid);
        }
        return true;
    }

    public boolean isPrimaryOwner(Player player) {
        if (player == null) return false;
        boolean adminEditMode = player.hasPermissions(2) && de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager.get().isEditModeEnabled(player);

        if (shop.isAdminShopEnabled()) {
            return adminEditMode;
        }

        if (adminEditMode) {
            return true;
        }

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
        purchaseContextPlayer = player;
        purchaseContextBuyerId = player.getUUID();
        purchaseContextBuyerName = player.getGameProfile().getName();
    }

    public void clearPurchaseContext() {
        purchaseContextPlayer = null;
        purchaseContextBuyerId = null;
        purchaseContextBuyerName = "";
    }

    public boolean canManageOffer(Player player) {
        if (shop.getOwnerId() == null) {
            return true;
        }
        return isOwner(player);
    }

    public ContainerData createMenuFlags(Player player) {
        return new ContainerData() {
            private int clientSyncedFlags = 0;

            @Override
            public int get(int index) {
                if (index == 0) {
                    if (shop.getLevel() != null && shop.getLevel().isClientSide()) {
                        return clientSyncedFlags;
                    }
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
                    if (player != null && player.hasPermissions(2) && de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager.get().isEditModeEnabled(player))
                        flags |= SingleOfferShopBlockEntity.GLOBAL_ADMIN_MODE_FLAG;
                    if (canPlayerBuy(player))
                        flags |= SingleOfferShopBlockEntity.CAN_BUY_FLAG;
                    if (shop.getGeneralSettings().isClosed())
                        flags |= SingleOfferShopBlockEntity.CLOSED_FLAG;
                    if (canManageOffer(player))
                        flags |= SingleOfferShopBlockEntity.CAN_MANAGE_OFFER_FLAG;
                    if (!shop.hasOffer() && player != null && !player.isCreative()
                            && !(player.hasPermissions(2) && de.bigbull.marketblocks.feature.marketplace.data.MarketplaceManager.get().isEditModeEnabled(player))) {
                        int maxShops = de.bigbull.marketblocks.core.config.SingleOfferConfig.MAX_SHOPS_PER_PLAYER.get();
                        if (maxShops >= 0 && shop.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            long activeShops = de.bigbull.marketblocks.core.data.ShopDirectorySavedData.get(serverLevel).getShops().stream()
                                    .filter(s -> player.getUUID().equals(s.ownerUUID()) && !s.result().isEmpty())
                                    .count();
                            if (activeShops >= maxShops) {
                                flags |= SingleOfferShopBlockEntity.SHOP_LIMIT_REACHED_FLAG;
                            }
                        }
                    }
                    return flags;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    this.clientSyncedFlags = value;
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }
}
