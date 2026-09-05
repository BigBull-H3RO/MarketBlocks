package de.bigbull.marketblocks.feature.trader;

import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;

public class ShopBuyerSpawner {

    private static final Map<ServerLevel, Set<ShopBuyerEntity>> ACTIVE_TRADERS = new ConcurrentHashMap<>();

    public static void onTraderAdded(ServerLevel level, ShopBuyerEntity entity) {
        ACTIVE_TRADERS.computeIfAbsent(level, k -> new HashSet<>()).add(entity);
    }

    public static void onTraderRemoved(ServerLevel level, ShopBuyerEntity entity) {
        Set<ShopBuyerEntity> set = ACTIVE_TRADERS.get(level);
        if (set != null) {
            set.remove(entity);
            if (set.isEmpty()) {
                ACTIVE_TRADERS.remove(level);
            }
        }
    }

    public static int getTraderCount(ServerLevel level) {
        Set<ShopBuyerEntity> set = ACTIVE_TRADERS.get(level);
        return set != null ? set.size() : 0;
    }

    /**
     * Clears all tracked traders. Must be called on server shutdown to prevent
     * stale ServerLevel references from persisting across restarts.
     */
    public static void clearAll() {
        ACTIVE_TRADERS.clear();
    }

    public static void tick(ServerLevel level) {
        if (!TraderConfig.SPAWN_ENABLED.get())
            return;
        // Only spawn in the Overworld (like vanilla Wandering Trader)
        if (level.dimension() != Level.OVERWORLD)
            return;

        RandomSource random = level.getRandom();
        int chance = TraderConfig.SPAWN_CHANCE.get();
        if (chance <= 0)
            return;

        if (random.nextInt(chance) == 0) {
            spawnTrader(level, random);
        }
    }

    private static void spawnTrader(ServerLevel level, RandomSource random) {
        // Check daytime preference: only spawn during day (tick 0-12000)
        if (TraderConfig.PREFER_DAYTIME_SPAWN.get()) {
            long dayTime = level.getDayTime() % 24000;
            if (dayTime >= 12000) {
                return; // Don't spawn at night
            }
        }

        // Check max trader limit per dimension
        int maxPerDimension = TraderConfig.MAX_PER_DIMENSION.get();
        long currentTraderCount = getTraderCount(level);
        if (currentTraderCount >= maxPerDimension) {
            return;
        }

        ShopDirectorySavedData data = ShopDirectorySavedData.get(level);
        boolean allowAdminShops = TraderConfig.ALLOW_ADMIN_SHOPS.get();
        List<ShopDirectorySavedData.ShopEntry> shops = data.getShops().stream()
                .filter(s -> s.pos().dimension().equals(level.dimension()) && !s.isClosed()
                        && (allowAdminShops || !s.isAdminShop()))
                .toList();

        boolean spawnNearPlayer = shops.isEmpty()
                || random.nextInt(100) < TraderConfig.SPAWN_NEAR_PLAYER_CHANCE_PERCENT.get();

        BlockPos targetPos = null;

        if (spawnNearPlayer) {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty())
                return;
            ServerPlayer player = players.get(random.nextInt(players.size()));
            targetPos = player.blockPosition();

            // Check if there are any active shops near the player (within 48 blocks)
            final BlockPos pPos = targetPos;
            boolean hasShopNearby = shops.stream()
                    .anyMatch(s -> s.pos().pos().closerToCenterThan(pPos.getCenter(), 48.0));

            if (!hasShopNearby) {
                // Wilderness / No Shop nearby: extremely rare spawn (5% chance of normal rate)
                if (random.nextInt(20) != 0) {
                    return;
                }
            }
        } else {
            ShopDirectorySavedData.ShopEntry targetShop = shops.get(random.nextInt(shops.size()));
            targetPos = targetShop.pos().pos();
        }

        // Try to find a spawn position near the target (between 10 and 35 blocks away)
        for (int i = 0; i < 10; i++) {
            int dx = random.nextInt(70) - 35;
            int dz = random.nextInt(70) - 35;

            if (Math.abs(dx) < 10 && Math.abs(dz) < 10)
                continue;

            BlockPos candidate = targetPos.offset(dx, 0, dz);
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, candidate);

            if (level.getFluidState(spawnPos).isEmpty() && level.getFluidState(spawnPos.below()).isEmpty()) {
                ShopBuyerEntity entity = RegistriesInit.SHOP_BUYER.get().create(level);
                if (entity != null) {
                    entity.moveTo(spawnPos, 0.0F, 0.0F);
                    entity.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.MobSpawnType.NATURAL, null);

                    level.addFreshEntity(entity);
                    return;
                }
            }
        }
    }
}
