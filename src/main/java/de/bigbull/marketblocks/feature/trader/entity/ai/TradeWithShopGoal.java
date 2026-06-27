package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.feature.singleoffer.entity.SingleOfferShopBlockEntity;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TradeWithShopGoal extends Goal {
    private final ShopBuyerEntity entity;
    private final double tradeDistanceSq;
    private int tradeDelay;

    /** Whether the trader is currently in browsing/window-shopping mode at a closed shop. */
    private boolean browsingPhase;
    /** Extra time the trader spends "looking around" at a closed/empty shop before reacting. */
    private int browsingTimer;

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
        
        BlockPos frontPos = getFrontPos(target);
        if (entity.distanceToSqr(Vec3.atCenterOf(frontPos)) > tradeDistanceSq) {
            return false;
        }

        return true;
    }

    private BlockPos getFrontPos(BlockPos shopPos) {
        BlockState state = entity.level().getBlockState(shopPos);
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return shopPos.relative(facing);
        }
        return shopPos;
    }

    @Override
    public void start() {
        this.tradeDelay = 20 + entity.getRandom().nextInt(20); // Wait 1-2 seconds before trading
        this.browsingPhase = false;
        this.browsingTimer = 0;
        entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        BlockPos target = entity.getTargetShop();
        if (target == null) return;

        entity.getLookControl().setLookAt(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 10.0F, (float) entity.getMaxHeadXRot());

        if (tradeDelay > 0) {
            tradeDelay--;
            return;
        }

        // If in browsing phase (window shopping at empty/closed shop), wait it out
        if (browsingPhase) {
            browsingTimer--;
            // Occasionally look around while browsing
            if (browsingTimer % 20 == 0 && entity.getRandom().nextInt(3) == 0) {
                double rx = target.getX() + 0.5D + (entity.getRandom().nextDouble() - 0.5D) * 3.0D;
                double rz = target.getZ() + 0.5D + (entity.getRandom().nextDouble() - 0.5D) * 3.0D;
                entity.getLookControl().setLookAt(rx, target.getY() + 1.0D, rz, 10.0F, (float) entity.getMaxHeadXRot());
            }
            if (browsingTimer <= 0) {
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
            if (shop.getGeneralSettings().isClosed() || !shop.hasOffer()) {
                // Start window-shopping: browse for 2-4 seconds, then react
                browsingPhase = true;
                browsingTimer = 40 + entity.getRandom().nextInt(40); // 2-4 seconds of browsing

                // Play a curious ambient sound as they start inspecting
                serverLevel.playSound(null, entity.blockPosition(), SoundEvents.WANDERING_TRADER_AMBIENT, SoundSource.NEUTRAL, 0.8F, 1.0F);
                return;
            }

            ItemStack p1 = shop.getOfferPayment1();
            ItemStack p2 = shop.getOfferPayment2();

            TraderEconomyManager eco = TraderEconomyManager.get();
            double paymentValue = 0;
            if (!p1.isEmpty()) {
                Double v = eco.evaluateItem(p1.getItem(), serverLevel.getRecipeManager());
                if (v != null) paymentValue += v * p1.getCount();
            }
            if (!p2.isEmpty()) {
                Double v = eco.evaluateItem(p2.getItem(), serverLevel.getRecipeManager());
                if (v != null) paymentValue += v * p2.getCount();
            }

            int budgetCost = (int) Math.ceil(paymentValue);
            
            if (entity.getBudget() >= budgetCost) {
                int bought = shop.getOfferManager().processNpcPurchase();
                if (bought > 0) {
                    entity.reduceBudget(budgetCost);
                    entity.incrementSuccessfulPurchases();
                    
                    // Happy reaction: sound + particles
                    serverLevel.playSound(null, entity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    for (int i = 0; i < 5; i++) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                                entity.getX() + (entity.getRandom().nextDouble() - 0.5D), 
                                entity.getY() + 1.5D + (entity.getRandom().nextDouble() - 0.5D), 
                                entity.getZ() + (entity.getRandom().nextDouble() - 0.5D), 
                                1, 0.0, 0.0, 0.0, 0.0);
                    }

                    // Head nod: briefly look down then back up to simulate nodding
                    entity.getLookControl().setLookAt(
                            entity.getX() + entity.getLookAngle().x,
                            entity.getY() - 0.5D,
                            entity.getZ() + entity.getLookAngle().z,
                            30.0F, 30.0F);
                } else {
                    serverLevel.playSound(null, entity.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            } else {
                serverLevel.playSound(null, entity.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }

        finishAndLeave(target);
    }

    /**
     * Handles cleanup after trading or browsing at a shop.
     * Marks the shop as visited, updates the shopping-tour counter, and walks away.
     */
    private void finishAndLeave(BlockPos target) {
        if (entity.level() instanceof ServerLevel sl) {
            // Mark this shop as visited
            entity.addVisitedShop(target);
            
            // Decrement the shopping-tour counter (triggers despawn when it reaches 0)
            entity.onShopVisitComplete();
            
            // If still shopping, add a small delay before looking for the next shop
            if (entity.getBudget() > 0) {
                // 35% chance to take a break (10-30 seconds). 65% chance to immediately look for the next shop
                if (entity.getRandom().nextInt(100) < 35) {
                    entity.delayNextShopSearch(sl.getGameTime(), 200 + entity.getRandom().nextInt(400));
                }
            }
            
            // Walk away from the shop so the trader doesn't stand frozen in front of it
            Vec3 randomPos = DefaultRandomPos.getPos(entity, 8, 4);
            if (randomPos != null) {
                entity.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, 0.6D);
            }
        }

        // Clear target, whether successful or not
        entity.setTargetShop(null);
    }
}
