package de.bigbull.marketblocks.shop.server;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.config.Config;
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
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.AtomicMoveNotSupportedException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Zentrale Verwaltungsinstanz für den blocklosen Server-Shop.
 */
public final class ServerShopManager {
    private static final Logger LOGGER = MarketBlocks.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final LevelResource SHOP_DIR = new LevelResource("marketblocks");
    private static final String FILE_NAME = "server_shop.json";
    private static final String BACKUP_FILE_SUFFIX = ".bak";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final int AUTO_SAVE_TICKS = 20 * 60;
    private static final int MAX_PAGE_NAME_LENGTH = 64;
    private static final int DEMAND_DECAY_PER_DAY = 1;

    private static final ServerShopManager INSTANCE = new ServerShopManager();

    public static ServerShopManager get() {
        return INSTANCE;
    }

    private final Object lock = new Object();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private ServerShopData data = ServerShopData.empty();
    private RegistryAccess registryAccess;
    private MinecraftServer server;
    private Path configFile;
    private boolean dirty;
    private boolean initialized;
    private int ticksSinceSave;

    private ServerShopManager() {
    }

    public void initialize(MinecraftServer server) {
        synchronized (lock) {
            this.server = server;
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
                ioExecutor.shutdown();
                data = ServerShopData.empty();
                registryAccess = null;
                server = null;
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

            boolean runtimeChanged = applyRuntimeUpkeepToAllOffers(currentGameTime(), currentDay());
            if (runtimeChanged) {
                markDirty();
                refreshOpenViewersLocked();
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

    /**
     * Variante für GUI-basierte Käufe (Items liegen in Slots, nicht im Inventar).
     * Hier prüfen wir Limits, Restock und Preiszustand; die Bezahlung muss vom Caller (Menu) abgezogen werden.
     */
    public boolean processPurchaseTransactionSlotBased(ServerPlayer player, UUID offerId, int amount) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopOffer offer = findOffer(offerId);
            if (offer == null || amount <= 0 || player == null) {
                return false;
            }

            long gameTime = currentGameTime();
            long day = currentDay();
            boolean runtimeChanged = applyRuntimeUpkeep(offer, gameTime, day);

            int maxPurchasable = getMaximumPurchasableFromLimits(offer, player.getUUID());
            if (amount > maxPurchasable) {
                if (runtimeChanged) {
                    markDirty();
                }
                if (!offer.limits().isUnlimited() && offer.limits().dailyLimit().isPresent()
                        && getPurchasedToday(offer.runtimeState(), player.getUUID()) >= offer.limits().dailyLimit().get()) {
                    player.sendSystemMessage(Component.translatable("message.marketblocks.server_shop.daily_limit_reached"));
                } else {
                    player.sendSystemMessage(Component.translatable("gui.marketblocks.out_of_stock"));
                }
                return false;
            }

            ServerShopOfferRuntimeState state = offer.runtimeState();
            OfferLimit limit = offer.limits();

            if (!limit.isUnlimited()) {
                if (limit.stockLimit().isPresent()) {
                    int remainingStock = state.stockRemaining().orElse(limit.stockLimit().get()) - amount;
                    state = state.withStockRemaining(Math.max(0, remainingStock));
                    if (limit.restockSeconds().isPresent()) {
                        state = state.withLastRestockGameTime(gameTime);
                    }
                }
                if (limit.dailyLimit().isPresent()) {
                    if (isGlobalDailyLimit()) {
                        state = state.withPurchasedTodayGlobal(state.purchasedTodayGlobal() + amount);
                    } else {
                        state = state.withPurchasedTodayForPlayer(player.getUUID(), state.purchasedTodayForPlayer(player.getUUID()) + amount);
                    }
                    state = state.withLastDailyResetDay(day);
                }
            }

            if (offer.pricing().enabled()) {
                state = state.withDemandPurchases(state.demandPurchases() + amount);
            }

            offer.setRuntimeState(state);
            markDirty();
            syncOpenViewers(player);
            return true;
        }
    }

    public int getMaximumPurchasableNow(UUID offerId, UUID playerId) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopOffer offer = findOffer(offerId);
            if (offer == null) {
                return 0;
            }
            if (applyRuntimeUpkeep(offer, currentGameTime(), currentDay())) {
                markDirty();
            }
            return getMaximumPurchasableFromLimits(offer, playerId);
        }
    }

    public record MutationResult<T>(T value, Component errorMessage) {
        public static <T> MutationResult<T> success(T value) {
            return new MutationResult<>(value, null);
        }

        public static <T> MutationResult<T> failure(Component errorMessage) {
            return new MutationResult<>(null, Objects.requireNonNull(errorMessage, "errorMessage"));
        }

        public boolean isSuccess() {
            return errorMessage == null;
        }
    }

    public MutationResult<Void> renamePage(String oldName, String newName) {
        synchronized (lock) {
            ensureInitialized();
            ServerShopPage page = getPage(oldName);
            if (page == null) {
                return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_not_found"));
            }

            MutationResult<String> validation = validatePageName(newName, page.name());
            if (!validation.isSuccess()) {
                return MutationResult.failure(validation.errorMessage());
            }

            String normalizedNewName = validation.value();
            if (!page.name().equals(normalizedNewName)) {
                page.rename(normalizedNewName);
                markDirty();
            }
            return MutationResult.success(null);
        }
    }

    public boolean canEdit(ServerPlayer player) {
        return player != null && player.hasPermissions(2);
    }

    public boolean isGlobalEditModeEnabled() {
        return Config.SERVER_SHOP_EDIT_MODE_ENABLED.get();
    }

    public void setGlobalEditModeEnabled(boolean enabled) {
        synchronized (lock) {
            boolean changed = isGlobalEditModeEnabled() != enabled;
            Config.SERVER_SHOP_EDIT_MODE_ENABLED.set(enabled);
            if (!initialized || !changed) {
                return;
            }

            if (!enabled && server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.containerMenu instanceof ServerShopMenu menu && menu.isEditor()) {
                        menu.setEditMode(false);
                    }
                }
            }
            refreshOpenViewersLocked();
        }
    }

    public boolean isOfferOnPage(UUID offerId, int pageIndex) {
        synchronized (lock) {
            ensureInitialized();
            if (offerId == null || pageIndex < 0 || pageIndex >= data.internalPages().size()) {
                return false;
            }
            return data.internalPages().get(pageIndex).findOfferIndex(offerId) >= 0;
        }
    }

    public MutationResult<ServerShopPage> createPage(String name) {
        synchronized (lock) {
            ensureInitialized();
            MutationResult<String> validation = validatePageName(name, null);
            if (!validation.isSuccess()) {
                return MutationResult.failure(validation.errorMessage());
            }

            ServerShopPage page = new ServerShopPage(validation.value(), Optional.empty(), Collections.emptyList());
            data.internalPages().add(page);
            markDirty();
            return MutationResult.success(page.copy());
        }
    }

    public MutationResult<Void> removePage(String name) {
        synchronized (lock) {
            ensureInitialized();
            int index = findPageIndex(name);
            if (index < 0) {
                return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_not_found"));
            }
            data.internalPages().remove(index);
            markDirty();
            return MutationResult.success(null);
        }
    }

    public MutationResult<ServerShopOffer> addOffer(String pageName, ServerShopOffer offer) {
        synchronized (lock) {
            ensureInitialized();
            String normalizedPageName = normalizePageName(pageName);
            if (normalizedPageName.isEmpty()) {
                return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_name_blank"));
            }

            ServerShopPage page = getPage(normalizedPageName);
            if (page == null) {
                return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_not_found"));
            }

            if (offer == null) {
                return MutationResult.failure(Component.translatable("gui.marketblocks.error.invalid_offer"));
            }

            DataResult<Void> validation = offer.validate();
            if (validation.error().isPresent()) {
                LOGGER.warn("Ungültiges Angebot: {}", validation.error().get().message());
                return MutationResult.failure(Component.translatable("gui.marketblocks.error.invalid_offer"));
            }
            page.internalOffers().add(offer.copy());
            markDirty();
            return MutationResult.success(offer.copy());
        }
    }

    public boolean moveOffer(UUID offerId, String targetPageName, int direction) {
        synchronized (lock) {
            ensureInitialized();
            if (offerId == null) return false;

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

            if (offer == null) return false;
            if (targetPageName != null && !sourcePage.name().equalsIgnoreCase(targetPageName)) {
                return false;
            }

            int newIndex = oldIndex + direction;
            if (newIndex < 0 || newIndex >= sourcePage.internalOffers().size()) {
                return false;
            }

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
            if (applyRuntimeUpkeep(offer, currentGameTime(), currentDay())) {
                markDirty();
            }
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

    public boolean resetLimitsForPlayer(UUID playerId) {
        synchronized (lock) {
            ensureInitialized();
            boolean changed = false;
            for (ServerShopPage page : data.internalPages()) {
                for (ServerShopOffer offer : page.internalOffers()) {
                    ServerShopOfferRuntimeState state = offer.runtimeState();
                    if (state.purchasedTodayForPlayer(playerId) > 0) {
                        offer.setRuntimeState(state.withPurchasedTodayForPlayer(playerId, 0));
                        changed = true;
                    }
                }
            }
            if (changed) {
                markDirty();
            }
            return changed;
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
        String normalizedName = normalizePageName(name);
        if (normalizedName.isEmpty()) {
            return null;
        }
        for (ServerShopPage page : data.internalPages()) {
            if (page.name().equalsIgnoreCase(normalizedName)) {
                return page;
            }
        }
        return null;
    }

    public void openShop(ServerPlayer player) {
        if (player == null) return;
        ServerShopData snapshot;
        Map<UUID, ServerShopOfferViewState> offerViewStates;
        boolean playerCanEdit;
        boolean globalEditModeEnabled;
        synchronized (lock) {
            ensureInitialized();
            long gameTime = currentGameTime();
            long day = currentDay();
            if (applyRuntimeUpkeepToAllOffers(gameTime, day)) {
                markDirty();
            }
            snapshot = data.copy();
            offerViewStates = buildOfferViewStates(player, gameTime);
            globalEditModeEnabled = isGlobalEditModeEnabled();
            playerCanEdit = canEdit(player);
        }
        ServerShopMenuProvider provider = new ServerShopMenuProvider(playerCanEdit, globalEditModeEnabled);
        player.openMenu(provider, (RegistryFriendlyByteBuf buf) -> {
            buf.writeBoolean(playerCanEdit);
            buf.writeBoolean(globalEditModeEnabled);
        });
        sendSnapshot(player, snapshot, offerViewStates, playerCanEdit, globalEditModeEnabled);
    }

    public void syncOpenViewers(ServerPlayer source) {
        if (source == null) return;
        synchronized (lock) {
            long gameTime = currentGameTime();
            long day = currentDay();
            if (applyRuntimeUpkeepToAllOffers(gameTime, day)) {
                markDirty();
            }
            ServerShopData snapshot = data.copy();
            int pageCount = snapshot.size();
            if (source.containerMenu instanceof ServerShopMenu menu) {
                menu.clampSelectedPage(pageCount);
                menu.slotsChanged(menu.templateContainer());
            }
            boolean globalEditModeEnabled = isGlobalEditModeEnabled();
            sendSnapshot(source, snapshot, buildOfferViewStates(source, gameTime), canEdit(source), globalEditModeEnabled);
            for (ServerPlayer player : source.server.getPlayerList().getPlayers()) {
                if (player == source) continue;
                if (player.containerMenu instanceof ServerShopMenu menu) {
                    menu.clampSelectedPage(pageCount);
                    menu.slotsChanged(menu.templateContainer());
                    sendSnapshot(player, snapshot, buildOfferViewStates(player, gameTime), canEdit(player), globalEditModeEnabled);
                }
            }
        }
    }

    private void sendSnapshot(ServerPlayer player, ServerShopData snapshot, Map<UUID, ServerShopOfferViewState> offerViewStates,
                              boolean canEditFlag, boolean globalEditModeEnabled) {
        if (player == null || snapshot == null || registryAccess == null) return;
        DataResult<CompoundTag> encoded = ServerShopSerialization.encodeData(snapshot, registryAccess);
        if (encoded.error().isPresent()) {
            LOGGER.error("Fehler beim Serialisieren: {}", encoded.error().get().message());
            return;
        }
        CompoundTag tag = encoded.result().orElseGet(CompoundTag::new);
        NetworkHandler.sendToPlayer(player, new ServerShopSyncPacket(
                tag,
                ServerShopSyncPacket.encodeOfferViewStates(offerViewStates),
                canEditFlag,
                globalEditModeEnabled));
    }

    private int findPageIndex(String name) {
        String normalizedName = normalizePageName(name);
        if (normalizedName.isEmpty()) {
            return -1;
        }
        List<ServerShopPage> pages = data.internalPages();
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).name().equalsIgnoreCase(normalizedName)) {
                return i;
            }
        }
        return -1;
    }

    public void reload() {
        synchronized (lock) {
            loadFromDisk();
            refreshOpenViewersLocked();
        }
    }

    private void loadFromDisk() {
        if (configFile == null || registryAccess == null) {
            return;
        }

        Path backupFile = backupFile();
        if (!Files.exists(configFile) && !Files.exists(backupFile)) {
            data = ServerShopData.empty();
            return;
        }

        if (Files.exists(configFile)) {
            Optional<ServerShopData> primary = parseDataFile(configFile);
            if (primary.isPresent()) {
                data = primary.get();
                dirty = false;
                return;
            }
        }

        if (Files.exists(backupFile)) {
            Optional<ServerShopData> backup = parseDataFile(backupFile);
            if (backup.isPresent()) {
                LOGGER.warn("Primary server-shop file is invalid. Restoring from backup {}", backupFile);
                data = backup.get();
                dirty = false;
                try {
                    Files.copy(backupFile, configFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.error("Could not restore backup {} to {}", backupFile, configFile, ex);
                }
                return;
            }
        }

        LOGGER.error("No valid server-shop data found. Starting with empty data.");
        data = ServerShopData.empty();
    }

    private void saveNow() {
        if (configFile == null || registryAccess == null) {
            return;
        }

        final ServerShopData dataSnapshot;
        final RegistryAccess ra;
        final Path targetFile;
        final Path tempFile;
        final Path backupFile;

        synchronized (lock) {
            dataSnapshot = data.copy();
            ra = registryAccess;
            targetFile = configFile;
            tempFile = temporaryFile();
            backupFile = backupFile();
            dirty = false;
            ticksSinceSave = 0;
        }

        ioExecutor.submit(() -> {
            boolean success = false;
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, ra);
                DataResult<JsonElement> result = ServerShopData.CODEC.encodeStart(ops, dataSnapshot);
                if (result.error().isPresent()) {
                    LOGGER.error("Fehler beim Serialisieren der Server-Shop-Daten: {}", result.error().get().message());
                } else {
                    JsonElement json = result.result().orElseGet(JsonObject::new);
                    GSON.toJson(json, writer);
                    writer.flush();
                    success = true;
                }
            } catch (IOException ex) {
                LOGGER.error("Konnte temporaere Server-Shop-Datei {} nicht schreiben", tempFile, ex);
            }

            if (success) {
                try {
                    if (Files.exists(targetFile)) {
                        Files.copy(targetFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    try {
                        Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    } catch (AtomicMoveNotSupportedException ignored) {
                        Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    LOGGER.error("Konnte Server-Shop-Datei {} nicht atomar speichern", targetFile, ex);
                    success = false;
                }
            }

            if (!success) {
                safeDelete(tempFile);
                synchronized (lock) {
                    dirty = true;
                }
            }
        });
    }

    private Optional<ServerShopData> parseDataFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            DataResult<ServerShopData> result = ServerShopData.CODEC.parse(ops, element);
            if (result.result().isPresent()) {
                return result.result();
            }
            LOGGER.error("Fehler beim Laden der Server-Shop-Datei {}: {}", file,
                    result.error().map(DataResult.Error::message).orElse("unknown parse error"));
            return Optional.empty();
        } catch (IOException ex) {
            LOGGER.error("Konnte Server-Shop-Datei {} nicht lesen", file, ex);
            return Optional.empty();
        }
    }

    private Path backupFile() {
        return configFile.resolveSibling(FILE_NAME + BACKUP_FILE_SUFFIX);
    }

    private Path temporaryFile() {
        return configFile.resolveSibling(FILE_NAME + TEMP_FILE_SUFFIX + "." + System.currentTimeMillis());
    }

    private void safeDelete(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
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

    private MutationResult<String> validatePageName(String name, String currentNameToIgnore) {
        String normalizedName = normalizePageName(name);
        if (normalizedName.isEmpty()) {
            return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_name_blank"));
        }
        if (normalizedName.length() > MAX_PAGE_NAME_LENGTH) {
            return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_name_too_long", MAX_PAGE_NAME_LENGTH));
        }
        if (currentNameToIgnore == null || !normalizedName.equalsIgnoreCase(normalizePageName(currentNameToIgnore))) {
            if (findPageIndex(normalizedName) >= 0) {
                return MutationResult.failure(Component.translatable("message.marketblocks.server_shop.page_name_duplicate", normalizedName));
            }
        }
        return MutationResult.success(normalizedName);
    }

    private String normalizePageName(String name) {
        return name == null ? "" : name.trim();
    }

    private long currentGameTime() {
        if (server == null) {
            return 0L;
        }
        return server.overworld().getGameTime();
    }

    private long currentDay() {
        return System.currentTimeMillis() / 86400000L;
    }

    private boolean isGlobalDailyLimit() {
        return Config.SERVER_SHOP_GLOBAL_DAILY_LIMIT.get();
    }

    private int getMaximumPurchasableFromLimits(ServerShopOffer offer, UUID playerId) {
        if (offer == null) {
            return 0;
        }
        return ServerShopRuntimeMath.computeRemainingPurchases(offer.limits(), offer.runtimeState(), playerId, isGlobalDailyLimit());
    }

    private int getPurchasedToday(ServerShopOfferRuntimeState state, UUID playerId) {
        return isGlobalDailyLimit() ? state.purchasedTodayGlobal() : state.purchasedTodayForPlayer(playerId);
    }

    private boolean applyRuntimeUpkeepToAllOffers(long gameTime, long day) {
        boolean changed = false;
        for (ServerShopPage page : data.internalPages()) {
            for (ServerShopOffer offer : page.internalOffers()) {
                changed |= applyRuntimeUpkeep(offer, gameTime, day);
            }
        }
        return changed;
    }

    /**
     * Updates an offer's runtime state by processing temporal upkeep mechanics such as
     * daily purchase limit resets, stock replenishment (restocks), and demand pricing decays.
     * This method evaluates changes strictly via timestamps/day counters and ensures
     * idempotency by returning whether changes were actually necessary.
     */
    private boolean applyRuntimeUpkeep(ServerShopOffer offer, long gameTime, long day) {
        if (offer == null) {
            return false;
        }

        OfferLimit limit = offer.limits();
        ServerShopOfferRuntimeState state = offer.runtimeState();
        ServerShopOfferRuntimeState updated = state;

        if (!limit.isUnlimited() && limit.dailyLimit().isPresent()) {
            if (updated.lastDailyResetDay() != day) {
                updated = updated.withPurchasedTodayGlobal(0)
                        .withClearedPlayerPurchases()
                        .withLastDailyResetDay(day);
            }
        } else if (updated.purchasedTodayGlobal() != 0 || !updated.purchasedTodayByPlayer().isEmpty() || updated.lastDailyResetDay() != 0L) {
            updated = updated.withPurchasedTodayGlobal(0)
                    .withClearedPlayerPurchases()
                    .withLastDailyResetDay(0L);
        }

        if (!limit.isUnlimited() && limit.stockLimit().isPresent()) {
            int maxStock = limit.stockLimit().get();
            int currentStock = updated.stockRemaining().orElse(maxStock);
            if (currentStock > maxStock) {
                updated = updated.withStockRemaining(maxStock);
                currentStock = maxStock;
            } else if (updated.stockRemaining().isEmpty()) {
                updated = updated.withStockRemaining(currentStock);
            }

            if (limit.restockSeconds().isPresent() && currentStock < maxStock) {
                ServerShopRuntimeMath.RestockResult restockResult = ServerShopRuntimeMath.applyRestock(
                        currentStock,
                        maxStock,
                        updated.lastRestockGameTime(),
                        gameTime,
                        limit.restockSeconds().get());
                if (restockResult.changed()) {
                    updated = updated.withStockRemaining(restockResult.stockRemaining())
                            .withLastRestockGameTime(restockResult.lastRestockGameTime());
                }
            }
        } else {
            if (updated.stockRemaining().isPresent()) {
                updated = updated.withStockRemaining(null);
            }
            if (updated.lastRestockGameTime() != 0L) {
                updated = updated.withLastRestockGameTime(0L);
            }
        }

        if (offer.pricing().enabled()) {
            int decayedDemand = ServerShopRuntimeMath.computeDemandPurchasesAfterDailyDecay(
                    updated.demandPurchases(),
                    updated.lastDemandDecayDay(),
                    day,
                    DEMAND_DECAY_PER_DAY);
            if (decayedDemand != updated.demandPurchases()) {
                updated = updated.withDemandPurchases(decayedDemand);
            }
            if (updated.lastDemandDecayDay() != day) {
                updated = updated.withLastDemandDecayDay(day);
            }
        } else {
            if (updated.demandPurchases() != 0) {
                updated = updated.withDemandPurchases(0);
            }
            if (updated.lastDemandDecayDay() != 0L) {
                updated = updated.withLastDemandDecayDay(0L);
            }
        }

        if (!updated.equals(state)) {
            offer.setRuntimeState(updated);
            return true;
        }
        return false;
    }

    private Map<UUID, ServerShopOfferViewState> buildOfferViewStates(ServerPlayer player, long gameTime) {
        Map<UUID, ServerShopOfferViewState> states = new HashMap<>();
        UUID playerId = player == null ? null : player.getUUID();
        for (ServerShopPage page : data.internalPages()) {
            for (ServerShopOffer offer : page.internalOffers()) {
                states.put(offer.id(), buildOfferViewState(offer, playerId, gameTime));
            }
        }
        return states;
    }

    private ServerShopOfferViewState buildOfferViewState(ServerShopOffer offer, UUID playerId, long gameTime) {
        if (offer == null) {
            return ServerShopOfferViewState.empty();
        }
        OfferLimit limit = offer.limits();
        ServerShopOfferRuntimeState state = offer.runtimeState();
        Optional<Integer> remainingDaily = limit.isUnlimited() || limit.dailyLimit().isEmpty()
                ? Optional.empty()
                : Optional.of(ServerShopRuntimeMath.computeRemainingDailyPurchases(limit, state, playerId, isGlobalDailyLimit()));
        Optional<Integer> remainingStock = limit.isUnlimited() || limit.stockLimit().isEmpty()
                ? Optional.empty()
                : Optional.of(state.stockRemaining().orElse(limit.stockLimit().get()));
        Optional<Integer> restockSecondsRemaining = ServerShopRuntimeMath.computeSecondsUntilNextRestock(limit, state, gameTime);
        return new ServerShopOfferViewState(
                getMaximumPurchasableFromLimits(offer, playerId),
                remainingDaily,
                remainingStock,
                restockSecondsRemaining,
                offer.currentPriceMultiplier());
    }

    private void refreshOpenViewersLocked() {
        if (server == null || registryAccess == null) {
            return;
        }
        long gameTime = currentGameTime();
        ServerShopData snapshot = data.copy();
        int pageCount = snapshot.size();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof ServerShopMenu menu) {
                menu.clampSelectedPage(pageCount);
                menu.slotsChanged(menu.templateContainer());
                sendSnapshot(player, snapshot, buildOfferViewStates(player, gameTime), canEdit(player), isGlobalEditModeEnabled());
            }
        }
    }
}
