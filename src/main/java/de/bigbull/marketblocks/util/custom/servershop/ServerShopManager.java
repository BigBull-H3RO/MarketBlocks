package de.bigbull.marketblocks.util.custom.servershop;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.network.NetworkHandler;
import de.bigbull.marketblocks.network.packets.serverShop.ServerShopSyncPacket;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenu;
import de.bigbull.marketblocks.util.custom.menu.ServerShopMenuProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Zentrale Verwaltungsinstanz für den blocklosen Server-Shop.
 */
public final class ServerShopManager {
    private static final Logger LOGGER = MarketBlocks.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final LevelResource SHOP_DIR = new LevelResource("marketblocks");
    private static final String FILE_NAME = "server_shop.json";
    private static final int AUTO_SAVE_TICKS = 20 * 60;

    private static final ServerShopManager INSTANCE = new ServerShopManager();

    public static ServerShopManager get() {
        return INSTANCE;
    }

    private final Object lock = new Object();
    private ServerShopData data = ServerShopData.empty();
    private RegistryAccess registryAccess;
    private Path configFile;
    private boolean dirty;
    private boolean initialized;
    private int ticksSinceSave;

    private ServerShopManager() {
    }

    public void initialize(MinecraftServer server) {
        synchronized (lock) {
            this.registryAccess = server.registryAccess();
            Path dir = server.getWorldPath(SHOP_DIR);
            this.configFile = dir.resolve(FILE_NAME);
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    LOGGER.error("Konnte Verzeichnis {} nicht erstellen", dir, e);
                }
            }
            loadFromDisk();
            initialized = true;
            ticksSinceSave = 0;
        }
    }

    public void shutdown() {
        synchronized (lock) {
            if (initialized) {
                saveNow();
                data = ServerShopData.empty();
                registryAccess = null;
                configFile = null;
                initialized = false;
            }
        }
    }

    public void tick() {
        synchronized (lock) {
            if (!initialized) {
                return;
            }
            if (dirty) {
                ticksSinceSave++;
                if (ticksSinceSave >= AUTO_SAVE_TICKS) {
                    saveNow();
                }
            }
        }
    }

    public ServerShopData snapshot() {
        synchronized (lock) {
            return data.copy();
        }
    }

    public boolean purchaseOffer(ServerPlayer player, UUID offerId, int amount) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopOffer offer = findOffer(offerId);
            if (offer == null || amount <= 0) {
                return false;
            }

            // 1. LIMIT PRÜFUNG
            OfferLimit limit = offer.limits();
            if (!limit.isUnlimited()) {
                if (limit.stockLimit().isPresent()) {
                    int stock = limit.stockLimit().get();
                    if (stock < amount) {
                        player.sendSystemMessage(Component.translatable("gui.marketblocks.out_of_stock"));
                        return false;
                    }
                }
            }

            // 2. ITEM KOSTEN PRÜFUNG
            List<ItemStack> payments = offer.payments();
            for (ItemStack payment : payments) {
                if (payment.isEmpty()) continue;
                ItemStack required = payment.copy();
                required.setCount(payment.getCount() * amount);
                if (!player.getInventory().contains(required)) {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.insufficient_stock"));
                    return false;
                }
            }

            // 3. ZAHLUNG ABZIEHEN
            for (ItemStack payment : payments) {
                if (payment.isEmpty()) continue;
                ItemStack toRemove = payment.copy();
                toRemove.setCount(payment.getCount() * amount);
                player.getInventory().removeItem(toRemove);
            }

            // 4. LIMIT AKTUALISIEREN
            if (!limit.isUnlimited() && limit.stockLimit().isPresent()) {
                int newStock = limit.stockLimit().get() - amount;
                OfferLimit newLimit = limit.withStockLimit(newStock);
                offer.setLimits(newLimit);
                markDirty();
                // Wichtig: Syncen, damit alle Clients das neue Limit sehen
                syncOpenViewers(player);
            }

            // 5. WARE GEBEN
            ItemStack result = offer.result().copy();
            result.setCount(offer.result().getCount() * amount);
            player.getInventory().placeItemBackInInventory(result);

            return true;
        }
    }

    public boolean selectPage(int index) {
        synchronized (lock) {
            ensureInitialized();
            if (data.size() <= 0) return false;
            if (index < 0 || index >= data.size()) return false;
            data.setSelectedPage(index);
            markDirty();
            return true;
        }
    }

    public boolean renamePage(String oldName, String newName) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopPage page = getPage(oldName);
            if (page == null) return false;
            page.rename(newName);
            markDirty();
            return true;
        }
    }

    public boolean canEdit(ServerPlayer player) {
        return player != null && player.hasPermissions(2);
    }

    public ServerShopPage createPage(String name) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopPage page = new ServerShopPage(name, Optional.empty(), Collections.emptyList());
            data.internalPages().add(page);
            data.setSelectedPage(data.size() - 1);
            markDirty();
            return page.copy();
        }
    }

    public boolean removePage(String name) {
        synchronized (lock) {
            ensureInitialized();
            int index = findPageIndex(name);
            if (index < 0) return false;
            data.internalPages().remove(index);
            data.setSelectedPage(Math.min(data.selectedPage(), Math.max(0, data.size() - 1)));
            markDirty();
            return true;
        }
    }

    // --- ANGEBOTS LOGIK (Vereinfacht) ---

    public Optional<ServerShopOffer> addOffer(String pageName, ServerShopOffer offer) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopPage page = getPage(pageName);
            if (page == null) return Optional.empty();

            DataResult<Void> validation = offer.validate();
            if (validation.error().isPresent()) {
                LOGGER.warn("Ungültiges Angebot: {}", validation.error().get().message());
                return Optional.empty();
            }
            page.internalOffers().add(offer.copy());
            markDirty();
            return Optional.of(offer.copy());
        }
    }

    public boolean moveOffer(UUID offerId, String targetPageName, int direction) {
        synchronized (lock) {
            ensureInitialized();
            if (offerId == null) return false;

            // Finde das Angebot und entferne es
            ServerShopPage sourcePage = null;
            ServerShopOffer offer = null;
            int oldIndex = -1;

            for (ServerShopPage page : data.internalPages()) {
                int idx = page.findOfferIndex(offerId);
                if (idx >= 0) {
                    sourcePage = page;
                    oldIndex = idx;
                    offer = page.internalOffers().get(idx);
                    break;
                }
            }

            if (offer == null || sourcePage == null) return false;

            // Wir verschieben nur innerhalb der Seite (einfachheitshalber, oder wie gewünscht)
            // direction: -1 für hoch, +1 für runter
            int newIndex = oldIndex + direction;
            if (newIndex < 0 || newIndex >= sourcePage.internalOffers().size()) {
                return false; // Geht nicht weiter hoch/runter
            }

            // Swap
            Collections.swap(sourcePage.internalOffers(), oldIndex, newIndex);
            markDirty();
            return true;
        }
    }

    public boolean deleteOffer(UUID offerId) {
        synchronized (lock) {
            ensureInitialized();
            if (offerId == null) return false;
            for (ServerShopPage page : data.internalPages()) {
                int idx = page.findOfferIndex(offerId);
                if (idx >= 0) {
                    page.internalOffers().remove(idx);
                    markDirty();
                    return true;
                }
            }
            return false;
        }
    }

    public boolean updateOfferLimits(UUID offerId, OfferLimit limit) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopOffer offer = findOffer(offerId);
            if (offer == null) return false;
            offer.setLimits(limit);
            markDirty();
            return true;
        }
    }

    public boolean updateOfferPricing(UUID offerId, DemandPricing pricing) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopOffer offer = findOffer(offerId);
            if (offer == null) return false;
            offer.setPricing(pricing);
            markDirty();
            return true;
        }
    }

    public boolean replaceOfferStacks(UUID offerId, ItemStack result, List<ItemStack> payments) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopOffer offer = findOffer(offerId);
            if (offer == null) return false;
            offer.setResult(result);
            if (payments != null) {
                int max = Math.min(offer.payments().size(), payments.size());
                for (int i = 0; i < max; i++) {
                    offer.setPayment(i, payments.get(i));
                }
            }
            markDirty();
            return true;
        }
    }

    public ServerShopOffer findOffer(UUID offerId) {
        if (offerId == null) return null;
        for (ServerShopPage page : data.internalPages()) {
            int idx = page.findOfferIndex(offerId);
            if (idx >= 0) {
                return page.internalOffers().get(idx);
            }
        }
        return null;
    }

    private ServerShopPage getPage(String name) {
        for (ServerShopPage page : data.internalPages()) {
            if (page.name().equalsIgnoreCase(name)) {
                return page;
            }
        }
        return null;
    }

    public void openShop(ServerPlayer player) {
        if (player == null) return;
        ServerShopData snapshot;
        boolean editable;
        synchronized (lock) {
            ensureInitialized();
            snapshot = data.copy();
            editable = canEdit(player);
        }
        ServerShopMenuProvider provider = new ServerShopMenuProvider(editable, snapshot.selectedPage());
        player.openMenu(provider, (RegistryFriendlyByteBuf buf) -> {
            buf.writeBoolean(editable);
            buf.writeVarInt(snapshot.selectedPage());
        });
        sendSnapshot(player, snapshot, editable);
    }

    public void syncOpenViewers(ServerPlayer source) {
        if (source == null) return;
        ServerShopData snapshot = snapshot();
        sendSnapshot(source, snapshot, canEdit(source));
        if (source.server != null) {
            for (ServerPlayer player : source.server.getPlayerList().getPlayers()) {
                if (player == source) continue;
                if (player.containerMenu instanceof ServerShopMenu) {
                    sendSnapshot(player, snapshot, canEdit(player));
                }
            }
        }
    }

    private void sendSnapshot(ServerPlayer player, ServerShopData snapshot, boolean canEditFlag) {
        if (player == null || snapshot == null || registryAccess == null) return;
        DataResult<CompoundTag> encoded = ServerShopSerialization.encodeData(snapshot, registryAccess);
        if (encoded.error().isPresent()) {
            LOGGER.error("Fehler beim Serialisieren: {}", encoded.error().get().message());
            return;
        }
        CompoundTag tag = encoded.result().orElseGet(CompoundTag::new);
        NetworkHandler.sendToPlayer(player, new ServerShopSyncPacket(tag, canEditFlag));
    }

    private int findPageIndex(String name) {
        List<ServerShopPage> pages = data.internalPages();
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).name().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private void loadFromDisk() {
        if (configFile == null || registryAccess == null) return;
        if (!Files.exists(configFile)) {
            data = ServerShopData.empty();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            DataResult<ServerShopData> result = ServerShopData.CODEC.parse(ops, element);
            data = result.result().orElseGet(() -> {
                LOGGER.error("Fehler beim Laden der Server-Shop-Konfiguration");
                return ServerShopData.empty();
            });
            dirty = false;
        } catch (IOException ex) {
            LOGGER.error("Konnte Server-Shop-Datei {} nicht lesen", configFile, ex);
            data = ServerShopData.empty();
        }
    }

    private void saveNow() {
        if (configFile == null || registryAccess == null) return;
        try (BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            DataResult<JsonElement> result = ServerShopData.CODEC.encodeStart(ops, data);
            if (result.error().isPresent()) {
                LOGGER.error("Fehler beim Serialisieren der Server-Shop-Daten: {}", result.error().get().message());
                return;
            }
            JsonElement json = result.result().orElseGet(JsonObject::new);
            GSON.toJson(json, writer);
            writer.flush();
            dirty = false;
            ticksSinceSave = 0;
        } catch (IOException ex) {
            LOGGER.error("Konnte Server-Shop-Datei {} nicht speichern", configFile, ex);
        }
    }

    private void markDirty() {
        dirty = true;
        ticksSinceSave = 0;
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Server-Shop wurde noch nicht initialisiert");
        }
    }
}