package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TradeWithShopGoal extends Goal {
    private final ShopBuyerEntity entity;
    private final double tradeDistanceSq;
    private int tradeDelay;

    /** Whether the trader is currently in browsing/window-shopping mode at a closed or unsuitable shop. */
    private boolean browsingPhase;
    /** Extra time the trader spends looking around before reacting. */
    private int browsingTimer;
    /** Time the trader holds the purchased item in hand and celebrates before departing. */
    private int celebratingTimer;

    public TradeWithShopGoal(ShopBuyerEntity entity, float tradeDistance) {
        this.entity = entity;
        this.tradeDistanceSq = tradeDistance * tradeDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (entity.isInvisible()) return false;
        
        BlockPos target = entity.getTargetShop();
        if (target == null) return false;
        
        BlockPos standPos = MoveToShopGoal.getTargetStandingPos(entity.level(), target);
        double distToShopSq = entity.distanceToSqr(Vec3.atCenterOf(target));
        double distToStandSq = entity.distanceToSqr(Vec3.atCenterOf(standPos));

        // Reachable if close enough to the shop directly (e.g. over a counter) or at the target stand position
        return distToShopSq <= tradeDistanceSq || distToStandSq <= 2.5 * 2.5;
    }

    @Override
    public boolean canContinueToUse() {
        if (entity.isInvisible()) return false;
        if (entity.getTargetShop() == null) return false;
        if (celebratingTimer > 0 || browsingPhase) return true;
        return canUse();
    }

    @Override
    public void start() {
        // Natural inspection time: 4 to 8 seconds (80 to 160 ticks) before making a purchase decision
        this.tradeDelay = 80 + entity.getRandom().nextInt(81);
        this.browsingPhase = false;
        this.browsingTimer = 0;
        this.celebratingTimer = 0;
        entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        BlockPos target = entity.getTargetShop();
        if (target == null) return;

        // Celebrating phase after a successful purchase: hold item proudly for ~2.5s without erratic twitching
        if (celebratingTimer > 0) {
            celebratingTimer--;
            entity.getNavigation().stop();
            entity.getLookControl().setLookAt(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 10.0F, (float) entity.getMaxHeadXRot());
            if (celebratingTimer <= 0) {
                entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                finishAndLeave(target);
            }
            return;
        }

        // Keep looking at the shop display
        double lookY = target.getY() + 0.5D;
        if (tradeDelay < 40) {
            // Halfway through: tilt head slightly down to examine the counter / showcase
            lookY = target.getY() + 0.25D;
        }
        entity.getLookControl().setLookAt(target.getX() + 0.5D, lookY, target.getZ() + 0.5D, 10.0F, (float) entity.getMaxHeadXRot());

        if (tradeDelay > 0) {
            tradeDelay--;
            return;
        }

        // If in browsing phase (window shopping at empty/unsuitable shop), wait it out calmly
        if (browsingPhase) {
            browsingTimer--;
            if (browsingTimer % 30 == 0 && entity.getRandom().nextInt(3) == 0) {
                double rx = target.getX() + 0.5D + (entity.getRandom().nextDouble() - 0.5D) * 2.0D;
                double rz = target.getZ() + 0.5D + (entity.getRandom().nextDouble() - 0.5D) * 2.0D;
                entity.getLookControl().setLookAt(rx, target.getY() + 0.8D, rz, 10.0F, (float) entity.getMaxHeadXRot());
            }
            if (browsingTimer <= 0) {
                if (entity.level() instanceof ServerLevel sl) {
                    sl.playSound(null, entity.blockPosition(), SoundEvents.WANDERING_TRADER_NO, SoundSource.NEUTRAL, 0.8F, 1.0F);
                }
                finishAndLeave(target);
            }
            return;
        }

        executeTrade(target);
    }

    private void executeTrade(BlockPos target) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        BlockEntity be = serverLevel.getBlockEntity(target);
        if (be instanceof SingleOfferShopBlockEntity shop) {
            boolean canBuy = false;

            if (!shop.getGeneralSettings().isClosed() && shop.hasOffer()) {
                ItemStack p1 = shop.getOfferPayment1();
                ItemStack p2 = shop.getOfferPayment2();
                ItemStack result = shop.getOfferResult();

                TraderEconomyManager eco = TraderEconomyManager.get();
                double paymentValue = 0;
                double resultValue = 0;

                if (!result.isEmpty()) {
                    Double v = eco.evaluateItem(result.getItem(), serverLevel.getRecipeManager(), serverLevel);
                    if (v != null) resultValue = v * result.getCount();
                }

                if (!p1.isEmpty()) {
                    Double v = eco.evaluateItem(p1.getItem(), serverLevel.getRecipeManager(), serverLevel);
                    if (v != null) paymentValue += v * p1.getCount();
                }
                if (!p2.isEmpty()) {
                    Double v = eco.evaluateItem(p2.getItem(), serverLevel.getRecipeManager(), serverLevel);
                    if (v != null) paymentValue += v * p2.getCount();
                }

                int budgetCost = (int) Math.ceil(paymentValue);
                boolean categoryInterested = entity.isInterestedIn(shop.getGeneralSettings().shopCategory());
                boolean rankInterested = entity.isRankInterested(shop.getGeneralSettings().shopCategory(), resultValue);
                double allowedBudget = categoryInterested ? entity.getBudget() : entity.getBudget() * 0.20;

                // Rank-dependent tolerance: Citizens accept fair prices, Nobles want quality deals
                double tolerance = switch (entity.getTraderRank()) {
                    case CITIZEN -> 0.85;   // Accepts up to 15% markup
                    case WEALTHY -> 0.95;   // Accepts up to 5% markup
                    case NOBLE -> 1.0;      // Only buys at or below market value
                };

                if (resultValue > 0 && resultValue >= paymentValue * tolerance && allowedBudget >= budgetCost && rankInterested) {
                    // Determine how many units this rank wants to buy
                    int maxQty = switch (entity.getTraderRank()) {
                        case CITIZEN -> 1;
                        case WEALTHY -> Math.min(3, Math.max(1, (int) (allowedBudget / Math.max(1, budgetCost))));
                        case NOBLE -> Math.min(5, Math.max(1, (int) (allowedBudget / Math.max(1, budgetCost))));
                    };

                    String rankName = entity.getTraderRank().name().charAt(0)
                            + entity.getTraderRank().name().substring(1).toLowerCase();
                    String buyerName = entity.hasCustomName()
                            ? entity.getCustomName().getString() + " (" + rankName + ")"
                            : "Trader (" + rankName + ")";

                    int bought = shop.getOfferManager().processNpcPurchase(maxQty, buyerName);
                    if (bought > 0) {
                        canBuy = true;
                        entity.reduceBudget(budgetCost * bought);
                        entity.incrementSuccessfulPurchases();

                        // Happy Wandering Trader reaction sound (calm & cheerful, not obnoxious villager)
                        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.WANDERING_TRADER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        for (int i = 0; i < 6; i++) {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    entity.getX() + (entity.getRandom().nextDouble() - 0.5D),
                                    entity.getY() + 1.2D + (entity.getRandom().nextDouble() - 0.5D),
                                    entity.getZ() + (entity.getRandom().nextDouble() - 0.5D),
                                    1, 0.0, 0.0, 0.0, 0.0);
                        }

                        // Hold the purchased item visibly in hand for ~2.5 seconds
                        entity.setItemSlot(EquipmentSlot.MAINHAND, result.copyWithCount(1));
                        this.celebratingTimer = 50; // 2.5 seconds celebration
                        return;
                    }
                }
            }

            if (!canBuy) {
                // Window-shopping: browse for 2-3 seconds calmly, then react
                browsingPhase = true;
                browsingTimer = 40 + entity.getRandom().nextInt(30);
                return;
            }
        }

        finishAndLeave(target);
    }

    @Override
    public void stop() {
        // Always clean up held items when interrupted
        entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.celebratingTimer = 0;
        this.browsingPhase = false;
    }

    /**
     * Handles cleanup after trading or browsing at a shop.
     * Marks the shop as visited, updates the shopping-tour counter, and walks away.
     */
    private void finishAndLeave(BlockPos target) {
        if (entity.level() instanceof ServerLevel sl) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            // Mark this shop as visited
            entity.addVisitedShop(target);
            
            // Decrement the shopping-tour counter (triggers despawn when it reaches 0)
            entity.onShopVisitComplete();
            
            // If still shopping, add a pause before searching for the next shop
            if (entity.getBudget() > 0) {
                entity.delayNextShopSearch(sl.getGameTime(), 200 + entity.getRandom().nextInt(200)); // 10 to 20 seconds pause
            }
            
            // Walk away calmly from the shop
            Vec3 randomPos = DefaultRandomPos.getPos(entity, 10, 4);
            if (randomPos != null) {
                entity.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, 0.55D);
            }
        }

        // Clear target, whether successful or not
        entity.setTargetShop(null);
    }
}
