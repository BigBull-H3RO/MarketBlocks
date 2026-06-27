package de.bigbull.marketblocks.feature.trader.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import de.bigbull.marketblocks.core.config.Config;
import de.bigbull.marketblocks.feature.trader.entity.ai.FindShopGoal;
import de.bigbull.marketblocks.feature.trader.entity.ai.LeaveAndDespawnGoal;
import de.bigbull.marketblocks.feature.trader.entity.ai.MoveToShopGoal;
import de.bigbull.marketblocks.feature.trader.entity.ai.TradeWithShopGoal;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class ShopBuyerEntity extends PathfinderMob {

    private int budget = Config.TRADER_MAX_BUDGET.get();
    private BlockPos targetShop = null;
    private int despawnDelay;
    private final Set<BlockPos> visitedShops = new HashSet<>();
    private long nextShopSearchTime = 0;

    /** How many more shops this trader wants to visit before leaving. */
    private int shopsToVisit;

    /** Tracks the number of successful purchases for context-dependent interaction. */
    private int successfulPurchases = 0;

    /** Messages 1-3: general, 4-6: post-purchase, 7-8: searching, 9-10: browsing */
    private static final int GENERAL_MSG_START = 1;
    private static final int GENERAL_MSG_END = 3;
    private static final int PURCHASE_MSG_START = 4;
    private static final int PURCHASE_MSG_END = 6;
    private static final int SEARCHING_MSG_START = 7;
    private static final int SEARCHING_MSG_END = 8;
    private static final int BROWSING_MSG_START = 9;
    private static final int BROWSING_MSG_END = 10;

    public ShopBuyerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.despawnDelay = Config.TRADER_DESPAWN_TICKS.get();
        int maxShops = Config.TRADER_MAX_SHOPS_PER_VISIT.get();
        this.shopsToVisit = maxShops > 1 ? 1 + this.random.nextInt(maxShops) : 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new UseItemGoal<>(
                this,
                PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY),
                SoundEvents.WANDERING_TRADER_DISAPPEARED,
                mob -> this.level().isNight() && !mob.isInvisible()));
        this.goalSelector.addGoal(0, new UseItemGoal<>(
                this,
                new ItemStack(Items.MILK_BUCKET),
                SoundEvents.WANDERING_TRADER_REAPPEARED,
                mob -> this.level().isDay() && mob.isInvisible()));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));

        this.goalSelector.addGoal(2, new LeaveAndDespawnGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TradeWithShopGoal(this, 2.5f));
        this.goalSelector.addGoal(4, new MoveToShopGoal(this, 0.85D, 1.25f));
        this.goalSelector.addGoal(5, new FindShopGoal(this, 48));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Budget", this.budget);
        compound.putInt("DespawnDelay", this.despawnDelay);
        compound.putLong("NextShopSearchTime", this.nextShopSearchTime);
        compound.putInt("ShopsToVisit", this.shopsToVisit);
        compound.putInt("SuccessfulPurchases", this.successfulPurchases);
        if (this.targetShop != null) {
            compound.put("TargetShop", NbtUtils.writeBlockPos(this.targetShop));
        }

        // Persist visited shops
        if (!this.visitedShops.isEmpty()) {
            ListTag visitedList = new ListTag();
            for (BlockPos pos : this.visitedShops) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", pos.getX());
                posTag.putInt("Y", pos.getY());
                posTag.putInt("Z", pos.getZ());
                visitedList.add(posTag);
            }
            compound.put("VisitedShops", visitedList);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Budget")) {
            this.budget = compound.getInt("Budget");
        }
        if (compound.contains("DespawnDelay")) {
            this.despawnDelay = compound.getInt("DespawnDelay");
        }
        if (compound.contains("NextShopSearchTime")) {
            this.nextShopSearchTime = compound.getLong("NextShopSearchTime");
        }
        if (compound.contains("ShopsToVisit")) {
            this.shopsToVisit = compound.getInt("ShopsToVisit");
        }
        if (compound.contains("SuccessfulPurchases")) {
            this.successfulPurchases = compound.getInt("SuccessfulPurchases");
        }
        NbtUtils.readBlockPos(compound, "TargetShop").ifPresent(pos -> this.targetShop = pos);

        // Load visited shops
        this.visitedShops.clear();
        if (compound.contains("VisitedShops", Tag.TAG_LIST)) {
            ListTag visitedList = compound.getList("VisitedShops", Tag.TAG_COMPOUND);
            for (int i = 0; i < visitedList.size(); i++) {
                CompoundTag posTag = visitedList.getCompound(i);
                if (posTag.contains("X") && posTag.contains("Y") && posTag.contains("Z")) {
                    this.visitedShops.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
                }
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.despawnDelay > 0) {
                this.despawnDelay--;
                if (this.despawnDelay <= 0) {
                    this.budget = 0; // Force despawn via LeaveAndDespawnGoal
                }
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    // --- Budget ---

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public void reduceBudget(int amount) {
        this.budget = Math.max(0, this.budget - amount);
    }

    // --- Target Shop ---

    public BlockPos getTargetShop() {
        return targetShop;
    }

    public void setTargetShop(BlockPos targetShop) {
        this.targetShop = targetShop;
    }

    // --- Visited Shops ---

    /**
     * Marks a shop position as visited so the trader won't revisit it during this lifecycle.
     */
    public void addVisitedShop(BlockPos pos) {
        this.visitedShops.add(pos);
    }

    /**
     * Checks whether a shop was already visited by this trader.
     */
    public boolean hasVisited(BlockPos pos) {
        return this.visitedShops.contains(pos);
    }

    /**
     * Returns the number of shops this trader has visited.
     */
    public int getVisitedShopCount() {
        return this.visitedShops.size();
    }

    // --- Shopping Tour ---

    /**
     * Returns how many more shops this trader wants to visit.
     */
    public int getShopsToVisit() {
        return shopsToVisit;
    }

    /**
     * Decrements the shops-to-visit counter after a successful trade.
     * When it reaches 0, triggers the despawn by setting budget to 0.
     */
    public void onShopVisitComplete() {
        this.shopsToVisit--;
        if (this.shopsToVisit <= 0) {
            this.budget = 0; // Trigger LeaveAndDespawnGoal
        }
    }

    // --- Purchases ---

    public int getSuccessfulPurchases() {
        return successfulPurchases;
    }

    public void incrementSuccessfulPurchases() {
        this.successfulPurchases++;
    }

    // --- Search Delay ---

    public void delayNextShopSearch(long currentTime, int delayTicks) {
        this.nextShopSearchTime = currentTime + delayTicks;
    }

    public boolean canSearchForShop(long currentTime) {
        return currentTime >= this.nextShopSearchTime;
    }

    // --- Player Interaction ---

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            int messageIndex = selectInteractMessage();
            player.sendSystemMessage(Component
                    .translatable("message.marketblocks.shop_buyer.interact." + messageIndex));
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            this.playSound(SoundEvents.WANDERING_TRADER_NO, this.getSoundVolume(), this.getVoicePitch());
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * Selects a context-dependent interaction message index.
     * Messages 1-3: general, 4-6: post-purchase, 7-8: searching, 9-10: browsing
     */
    private int selectInteractMessage() {
        if (successfulPurchases > 0 && this.getRandom().nextInt(100) < 60) {
            // 60% chance for a post-purchase message when the trader has bought something
            return PURCHASE_MSG_START + this.getRandom().nextInt(PURCHASE_MSG_END - PURCHASE_MSG_START + 1);
        }
        if (targetShop != null && this.getRandom().nextInt(100) < 70) {
            // 70% chance for a searching message when the trader is heading to a shop
            return SEARCHING_MSG_START + this.getRandom().nextInt(SEARCHING_MSG_END - SEARCHING_MSG_START + 1);
        }
        if (successfulPurchases == 0 && this.getRandom().nextInt(100) < 30) {
            // 30% chance for a browsing message when the trader hasn't bought anything yet
            return BROWSING_MSG_START + this.getRandom().nextInt(BROWSING_MSG_END - BROWSING_MSG_START + 1);
        }
        // Default: general message
        return GENERAL_MSG_START + this.getRandom().nextInt(GENERAL_MSG_END - GENERAL_MSG_START + 1);
    }
}
