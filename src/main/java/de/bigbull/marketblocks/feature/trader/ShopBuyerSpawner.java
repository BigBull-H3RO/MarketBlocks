package de.bigbull.marketblocks.feature.trader;

import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public class ShopBuyerSpawner {

    public static void tick(ServerLevel level) {
        if (!Config.ENABLE_TRADER_SPAWNING.get())
            return;
        // Only spawn in the Overworld (like vanilla Wandering Trader)
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD)
            return;

        RandomSource random = level.getRandom();
        int chance = Config.TRADER_SPAWN_CHANCE.get();
        if (chance <= 0)
            return;

        if (random.nextInt(chance) == 0) {
            spawnTrader(level, random);
        }
    }

    private static void spawnTrader(ServerLevel level, RandomSource random) {
        // Check daytime preference: only spawn during day (tick 0-12000)
        if (Config.TRADER_PREFER_DAYTIME_SPAWN.get()) {
            long dayTime = level.getDayTime() % 24000;
            if (dayTime >= 12000) {
                return; // Don't spawn at night
            }
        }

        // Check max trader limit per dimension
        int maxPerDimension = Config.TRADER_MAX_PER_DIMENSION.get();
        long currentTraderCount = level.getEntities(RegistriesInit.SHOP_BUYER.get(), entity -> true).size();
        if (currentTraderCount >= maxPerDimension) {
            return;
        }

        ShopDirectorySavedData data = ShopDirectorySavedData.get(level);
        boolean allowAdminShops = Config.TRADER_ALLOW_ADMIN_SHOPS.get();
        List<ShopDirectorySavedData.ShopEntry> shops = data.getShops().stream()
                .filter(s -> s.pos().dimension().equals(level.dimension()) && !s.isClosed()
                        && (allowAdminShops || !s.isAdminShop()))
                .toList();

        boolean spawnNearPlayer = shops.isEmpty()
                || random.nextInt(100) < Config.TRADER_SPAWN_NEAR_PLAYER_CHANCE_PERCENT.get();

        BlockPos targetPos = null;

        if (spawnNearPlayer) {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty())
                return;
            ServerPlayer player = players.get(random.nextInt(players.size()));
            targetPos = player.blockPosition();
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
                    int minBudget = Config.TRADER_MIN_BUDGET.get();
                    int maxBudget = Config.TRADER_MAX_BUDGET.get();
                    if (minBudget > maxBudget)
                        minBudget = maxBudget;
                    int budget = minBudget + (maxBudget > minBudget ? random.nextInt(maxBudget - minBudget + 1) : 0);
                    entity.setBudget(budget);

                    // Assign a random name if enabled
                    if (Config.TRADER_NAMES_ENABLED.get()) {
                        String name = TraderEconomyManager.get().getRandomName(new java.util.Random(random.nextLong()));
                        if (name != null) {
                            entity.setCustomName(Component.literal(name));
                            entity.setCustomNameVisible(true);
                        }
                    }

                    level.addFreshEntity(entity);
                    return;
                }
            }
        }
    }
}
