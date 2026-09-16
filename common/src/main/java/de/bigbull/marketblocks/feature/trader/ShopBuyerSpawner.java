package de.bigbull.marketblocks.feature.trader;

import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopBuyerSpawner {

    private static final Map<ServerLevel, Set<ShopBuyerEntity>> ACTIVE_TRADERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_SPAWN_PER_PLAYER = new ConcurrentHashMap<>();

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
     * Clears all tracked traders and player cooldowns. Must be called on server shutdown.
     */
    public static void clearAll() {
        ACTIVE_TRADERS.clear();
        LAST_SPAWN_PER_PLAYER.clear();
    }

    public static void tick(ServerLevel level) {
        if (!TraderConfig.SPAWN_ENABLED.get())
            return;
        // Only spawn in the Overworld (like vanilla Wandering Trader)
        if (level.dimension() != Level.OVERWORLD)
            return;

        // Check only once every 60 seconds (1200 ticks) instead of every tick
        if (level.getGameTime() % 1200 != 0)
            return;

        // Check daytime preference (tick 0-12000)
        if (TraderConfig.PREFER_DAYTIME_SPAWN.get()) {
            long dayTime = level.getDayTime() % 24000;
            if (dayTime >= 12000) {
                return; // Don't spawn at night
            }
        }

        // Check max trader limit per dimension
        int maxPerDimension = TraderConfig.MAX_PER_DIMENSION.get();
        if (getTraderCount(level) >= maxPerDimension) {
            return;
        }

        List<ServerPlayer> players = new ArrayList<>(level.players());
        if (players.isEmpty())
            return;

        // Shuffle so players take turns fairly
        Collections.shuffle(players, new java.util.Random(level.getRandom().nextLong()));

        long gameTime = level.getGameTime();
        long cooldownTicks = TraderConfig.SPAWN_COOLDOWN_TICKS.get();
        int spawnChancePercent = TraderConfig.SPAWN_CHANCE_PERCENT.get();
        int detectionRadius = TraderConfig.SHOP_DETECTION_RADIUS.get();
        double radiusSq = (double) detectionRadius * detectionRadius;
        boolean allowAdminShops = TraderConfig.ALLOW_ADMIN_SHOPS.get();

        ShopDirectorySavedData data = ShopDirectorySavedData.get(level);

        for (ServerPlayer player : players) {
            UUID playerId = player.getUUID();

            // 1. Check per-player cooldown (e.g. 24000 ticks = 1 in-game day)
            long lastSpawn = LAST_SPAWN_PER_PLAYER.getOrDefault(playerId, 0L);
            if (gameTime - lastSpawn < cooldownTicks) {
                continue;
            }

            // 2. Check if player already has an active trader nearby (within 80 blocks)
            Set<ShopBuyerEntity> currentTraders = ACTIVE_TRADERS.get(level);
            if (currentTraders != null && currentTraders.stream().anyMatch(t -> t.isAlive() && t.distanceToSqr(player) < 6400.0)) {
                continue;
            }

            // 3. Check if player is near at least one active, open shop with an offer
            BlockPos pPos = player.blockPosition();
            boolean hasShopNearby = data.getShops().stream().anyMatch(s -> {
                if (!s.pos().dimension().equals(level.dimension()) || s.isClosed())
                    return false;
                if (!allowAdminShops && s.isAdminShop())
                    return false;
                if (s.pos().pos().distSqr(pPos) > radiusSq)
                    return false;

                if (level.isLoaded(s.pos().pos())) {
                    var be = level.getBlockEntity(s.pos().pos());
                    if (be instanceof SingleOfferShopBlockEntity shopBlock) {
                        return shopBlock.hasOffer() && !shopBlock.getGeneralSettings().isClosed();
                    }
                }
                return !s.isClosed();
            });

            if (!hasShopNearby) {
                // Player is away from shops (e.g. mining in cave, exploring wilderness).
                // Do NOT waste their spawn cooldown!
                continue;
            }

            // 4. Roll chance once cooldown is satisfied (e.g. 25% per minute check)
            if (level.getRandom().nextInt(100) >= spawnChancePercent) {
                continue;
            }

            // 5. Find a safe surface spawn position near the player (between 12 and 28 blocks away)
            BlockPos spawnPos = findSafeSpawnPos(level, pPos, level.getRandom());
            if (spawnPos != null) {
                ShopBuyerEntity entity = RegistriesInit.SHOP_BUYER.get().create(level);
                if (entity != null) {
                    entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                    entity.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.MobSpawnType.NATURAL, null);
                    level.addFreshEntity(entity);

                    LAST_SPAWN_PER_PLAYER.put(playerId, gameTime);
                    return; // Spawn 1 trader at a time per tick cycle
                }
            }
        }
    }

    private static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos center, RandomSource random) {
        for (int i = 0; i < 20; i++) {
            int dx = (random.nextBoolean() ? 1 : -1) * (12 + random.nextInt(16));
            int dz = (random.nextBoolean() ? 1 : -1) * (12 + random.nextInt(16));

            BlockPos candidate = center.offset(dx, 0, dz);
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, candidate);

            BlockState floor = level.getBlockState(surfacePos.below());
            BlockState feet = level.getBlockState(surfacePos);
            BlockState head = level.getBlockState(surfacePos.above());

            if (floor.isSolidRender(level, surfacePos.below())
                    && !floor.is(BlockTags.LEAVES)
                    && feet.isAir()
                    && head.isAir()
                    && level.getFluidState(surfacePos).isEmpty()
                    && level.getFluidState(surfacePos.below()).isEmpty()) {
                return surfacePos;
            }
        }
        return null;
    }
}
