package de.bigbull.marketblocks.feature.trader.entity.ai;


import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.core.data.ShopDirectorySavedData;
import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class FindShopGoal extends Goal {
    private final ShopBuyerEntity entity;
    private final int searchRadius;

    public FindShopGoal(ShopBuyerEntity entity, int searchRadius) {
        this.entity = entity;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (entity.getBudget() <= 0)
            return false;
        if (entity.getTargetShop() != null)
            return false;
        if (entity.isInvisible())
            return false;
        if (entity.level() instanceof ServerLevel sl && !entity.canSearchForShop(sl.getGameTime()))
            return false;
        if (entity.getRandom().nextInt(reducedTickDelay(20)) != 0)
            return false;

        return findValidShop();
    }

    /**
     * Returns true with a 20% chance, allowing the trader to window-shop
     * at a shop even if it has no offer or the deal isn't attractive.
     */
    private boolean rollWindowShopping() {
        return entity.getRandom().nextInt(5) == 0;
    }

    private boolean findValidShop() {
        if (!(entity.level() instanceof ServerLevel serverLevel))
            return false;

        ShopDirectorySavedData data = ShopDirectorySavedData.get(serverLevel);

        // Pre-filter by dimension and distance, then shuffle for randomness
        boolean allowAdminShops = TraderConfig.ALLOW_ADMIN_SHOPS.get();
        List<ShopDirectorySavedData.ShopEntry> candidates = new ArrayList<>();
        for (ShopDirectorySavedData.ShopEntry shop : data.getShops()) {
            if (!allowAdminShops && shop.isAdminShop())
                continue;
            GlobalPos globalPos = shop.pos();
            if (globalPos.dimension() != serverLevel.dimension())
                continue;
            BlockPos pos = globalPos.pos();
            if (!pos.closerToCenterThan(entity.position(), searchRadius))
                continue;
            if (entity.hasVisited(pos))
                continue;
            candidates.add(shop);
        }

        // Shuffle so the trader doesn't always pick the same shop
        Collections.shuffle(candidates, new java.util.Random(entity.getRandom().nextLong()));

        for (ShopDirectorySavedData.ShopEntry shop : candidates) {
            BlockPos pos = shop.pos().pos();

            // Check if the block still exists (only for loaded chunks)
            boolean hasOffer = true;
            if (serverLevel.isLoaded(pos)) {
                BlockEntity be = serverLevel.getBlockEntity(pos);
                if (be instanceof SingleOfferShopBlockEntity shopBlock) {
                    hasOffer = shopBlock.hasOffer() && !shopBlock.getGeneralSettings().isClosed();
                } else if (be == null) {
                    // Block was removed, skip this shop
                    continue;
                }
            } else {
                hasOffer = !shop.isClosed();
            }

            boolean isWindowShopping = !hasOffer && rollWindowShopping();

            if (!hasOffer && !isWindowShopping)
                continue;

            if (isWindowShopping) {
                entity.setTargetShop(pos);
                return true;
            }

            ItemStack result = shop.result();
            ItemStack payment1 = shop.payment1();
            ItemStack payment2 = shop.payment2();

            if (result.isEmpty() || (payment1.isEmpty() && payment2.isEmpty()))
                continue;

            TraderEconomyManager eco = TraderEconomyManager.get();
            Double resultVal = eco.evaluateItem(result.getItem(), serverLevel.getRecipeManager(), serverLevel);
            if (resultVal == null)
                continue; // Unvalued item, fail-safe: don't buy

            double totalResultValue = resultVal * result.getCount();

            double totalPaymentValue = 0;
            boolean paymentValid = true;

            if (!payment1.isEmpty()) {
                Double p1Val = eco.evaluateItem(payment1.getItem(), serverLevel.getRecipeManager(), serverLevel);
                if (p1Val == null)
                    paymentValid = false;
                else
                    totalPaymentValue += p1Val * payment1.getCount();
            }
            if (!payment2.isEmpty() && paymentValid) {
                Double p2Val = eco.evaluateItem(payment2.getItem(), serverLevel.getRecipeManager(), serverLevel);
                if (p2Val == null)
                    paymentValid = false;
                else
                    totalPaymentValue += p2Val * payment2.getCount();
            }

            if (!paymentValid)
                continue;

            // Is it a good deal for the trader? Tolerance depends on rank.
            double tolerance = switch (entity.getTraderRank()) {
                case CITIZEN -> 0.85;   // Accepts up to 15% markup
                case WEALTHY -> 0.95;   // Accepts up to 5% markup
                case NOBLE -> 1.0;      // Only buys at or below market value
            };
            boolean isGoodDeal = totalResultValue >= totalPaymentValue * tolerance;
            boolean canAfford = false;
            
            if (isGoodDeal) {
                boolean interested = entity.isInterestedIn(shop.shopCategory());
                double allowedBudget = interested ? entity.getBudget() : entity.getBudget() * 0.20;
                canAfford = allowedBudget >= totalPaymentValue;
            }

            if (isGoodDeal && canAfford) {
                entity.setTargetShop(pos);
                return true;
            } else {
                // Not buying, but might still do window shopping (20% chance)
                if (rollWindowShopping()) {
                    entity.setTargetShop(pos);
                    return true;
                }
            }
        }
        return false;
    }
}
