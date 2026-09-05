package de.bigbull.marketblocks.feature.trader.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import de.bigbull.marketblocks.core.config.TraderConfig;
import de.bigbull.marketblocks.feature.singleoffer.settings.ShopCategory;
import de.bigbull.marketblocks.feature.trader.ShopBuyerSpawner;
import de.bigbull.marketblocks.feature.trader.entity.ai.FindShopGoal;
import de.bigbull.marketblocks.feature.trader.entity.ai.LeaveAndDespawnGoal;
import de.bigbull.marketblocks.feature.trader.entity.ai.MoveToShopGoal;
import de.bigbull.marketblocks.feature.trader.entity.ai.TradeWithShopGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import de.bigbull.marketblocks.feature.trader.data.TraderEconomyManager;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

public class ShopBuyerEntity extends PathfinderMob {

    public enum TraderRank {
        CITIZEN,
        WEALTHY,
        NOBLE
    }

    public enum InterestCategory {
        FARMER,
        ALCHEMIST,
        BLACKSMITH,
        VALUABLES,
        GENERAL
    }

    private static final EntityDataAccessor<Integer> DATA_TRADER_RANK = SynchedEntityData
            .defineId(ShopBuyerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_INTEREST_CATEGORY = SynchedEntityData
            .defineId(ShopBuyerEntity.class, EntityDataSerializers.INT);

    private int budget = 0;
    private BlockPos targetShop = null;
    private int despawnDelay;
    private final Set<BlockPos> visitedShops = new HashSet<>();
    private long nextShopSearchTime = 0;

    /** How many more shops this trader wants to visit before leaving. */
    private int shopsToVisit;

    /**
     * Tracks the number of successful purchases for context-dependent interaction.
     */
    private int successfulPurchases = 0;

    /** Dialog and interaction cooldown tracking (Anti-Spamming) */
    private final Map<UUID, Long> lastDialogTimes = new HashMap<>();

    /** Click timestamps for Rage Mode Easter Egg trigger */
    private final Map<UUID, List<Long>> clickTimestamps = new HashMap<>();

    /** Rage state */
    private boolean isRaging = false;

    /** UUID of the player this trader is angry at (for revenge upon return) */
    @Nullable
    private UUID angryTargetUUID = null;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TRADER_RANK, TraderRank.CITIZEN.ordinal());
        builder.define(DATA_INTEREST_CATEGORY, InterestCategory.GENERAL.ordinal());
    }

    /** Messages 1-3: general, 4-6: post-purchase, 7-8: searching, 9-10: browsing */
    private static final int GENERAL_MSG_START = 1;
    private static final int GENERAL_MSG_END = 3;
    private static final int PURCHASE_MSG_START = 4;
    private static final int PURCHASE_MSG_END = 6;
    private static final int SEARCHING_MSG_START = 7;
    private static final int SEARCHING_MSG_END = 8;
    private static final int BROWSING_MSG_START = 9;
    private static final int RAGE_CLICK_THRESHOLD = 3;
    private static final int RAGE_CLICK_WINDOW_TICKS = 60;
    private static final double REVENGE_DETECTION_RADIUS = 16.0;

    private static final int BROWSING_MSG_END = 10;

    public ShopBuyerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.despawnDelay = TraderConfig.DESPAWN_TICKS.get();
        int maxShops = TraderConfig.MAX_SHOPS_PER_VISIT.get();
        this.shopsToVisit = maxShops > 1 ? 1 + this.random.nextInt(maxShops) : 1;
        if (this.getNavigation() instanceof GroundPathNavigation groundNavigation) {
            groundNavigation.setCanFloat(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
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

        // Melee attack goal when raging (continuous pathing)
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true) {
            @Override
            public boolean canUse() {
                return ShopBuyerEntity.this.isRaging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return ShopBuyerEntity.this.isRaging() && super.canContinueToUse();
            }
        });

        // Panic goal when not raging
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D) {
            @Override
            public boolean canUse() {
                return !ShopBuyerEntity.this.isRaging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !ShopBuyerEntity.this.isRaging() && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(2, new LeaveAndDespawnGoal(this, 0.65D));
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
        compound.putString("TraderRank", this.getTraderRank().name());
        compound.putString("InterestCategory", this.getInterestCategory().name());
        compound.putBoolean("IsRaging", this.isRaging);
        if (this.angryTargetUUID != null) {
            compound.putUUID("AngryTargetUUID", this.angryTargetUUID);
        }
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
        if (compound.contains("TraderRank")) {
            try {
                this.setTraderRank(TraderRank.valueOf(compound.getString("TraderRank")));
            } catch (IllegalArgumentException e) {
                this.setTraderRank(TraderRank.CITIZEN);
            }
        }
        if (compound.contains("InterestCategory")) {
            try {
                this.setInterestCategory(InterestCategory.valueOf(compound.getString("InterestCategory")));
            } catch (IllegalArgumentException e) {
                this.setInterestCategory(InterestCategory.GENERAL);
            }
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
        if (compound.contains("IsRaging")) {
            this.isRaging = compound.getBoolean("IsRaging");
            if (this.isRaging) {
                equipRageWeapon();
            }
        }
        if (compound.hasUUID("AngryTargetUUID")) {
            this.angryTargetUUID = compound.getUUID("AngryTargetUUID");
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
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.level() instanceof ServerLevel serverLevel) {
            ShopBuyerSpawner.onTraderAdded(serverLevel, this);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level() instanceof ServerLevel serverLevel) {
            ShopBuyerSpawner.onTraderRemoved(serverLevel, this);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        // Ensure weapon is never dropped upon death
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        super.die(damageSource);
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

            // Handle Rage and Revenge logic
            if (TraderConfig.RAGE_MODE_ENABLED.get()) {
                if (this.isRaging) {
                    LivingEntity currentTarget = this.getTarget();
                    if (currentTarget == null || !currentTarget.isAlive() || (currentTarget instanceof Player p && (p.isDeadOrDying() || p.isRemoved()))) {
                        if (currentTarget instanceof Player p) {
                            this.angryTargetUUID = p.getUUID();
                        }
                        calmDown();
                    }
                } else if (this.angryTargetUUID != null) {
                    double radius = REVENGE_DETECTION_RADIUS;
                    Player player = this.level().getPlayerByUUID(this.angryTargetUUID);
                    if (player != null && player.isAlive() && !player.isSpectator() && !player.isCreative()
                            && this.distanceToSqr(player) < (radius * radius)) {
                        triggerRevenge(player);
                    }
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
     * Marks a shop position as visited so the trader won't revisit it during this
     * lifecycle.
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

    // --- Player Interaction & Rage Mode ---

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            long gameTime = this.level().getGameTime();
            UUID playerUuid = player.getUUID();

            // If already raging against this player, ignore peaceful interaction
            if (this.isRaging) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Track clicks for Rage Mode Easter Egg trigger
            if (TraderConfig.RAGE_MODE_ENABLED.get()) {
                List<Long> clicks = clickTimestamps.computeIfAbsent(playerUuid, k -> new ArrayList<>());
                clicks.add(gameTime);
                int windowTicks = RAGE_CLICK_WINDOW_TICKS;
                clicks.removeIf(t -> (gameTime - t) > windowTicks);

                int threshold = RAGE_CLICK_THRESHOLD;
                if (clicks.size() >= threshold) {
                    clicks.clear();
                    triggerRageMode(player);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }

            // Dialog cooldown (anti-spam): 30 ticks (1.5s) per player
            Long lastTime = lastDialogTimes.get(playerUuid);
            if (lastTime != null && (gameTime - lastTime) < 30) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            lastDialogTimes.put(playerUuid, gameTime);

            // Show rank and interest category so the player knows what this NPC is looking for
            Component rankInfo = Component.translatable(
                    "message.marketblocks.shop_buyer.info",
                    Component.translatable("entity.marketblocks.shop_buyer.rank." + getTraderRank().name().toLowerCase()),
                    Component.translatable("entity.marketblocks.shop_buyer.category." + getInterestCategory().name().toLowerCase())
            ).withStyle(ChatFormatting.GRAY);
            player.sendSystemMessage(rankInfo);

            int messageIndex = selectInteractMessage();
            player.sendSystemMessage(Component
                    .translatable("message.marketblocks.shop_buyer.interact." + messageIndex));
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            this.playSound(SoundEvents.WANDERING_TRADER_NO, this.getSoundVolume(), this.getVoicePitch());
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        // Provide extended melee attack reach (~2.4 blocks) for sword combat
        double reach = this.getBbWidth() * 2.0F + 1.2F;
        double reachSqr = reach * reach + target.getBbWidth();
        return this.distanceToSqr(target.getX(), target.getY(), target.getZ()) <= reachSqr;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean success = super.doHurtTarget(target);
        if (success) {
            this.swing(InteractionHand.MAIN_HAND);
            this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
        }
        return success;
    }

    public boolean isRaging() {
        return isRaging;
    }

    public void setRaging(boolean raging) {
        this.isRaging = raging;
    }

    public void triggerRageMode(Player target) {
        this.isRaging = true;
        this.targetShop = null;
        this.angryTargetUUID = target.getUUID();
        this.setTarget(target);

        this.playSound(SoundEvents.VINDICATOR_CELEBRATE, 1.0F, 1.0F);
        equipRageWeapon();

        int msgIndex = 1 + this.random.nextInt(3);
        String nameStr = this.hasCustomName() ? this.getCustomName().getString() : Component.translatable("entity.marketblocks.shop_buyer").getString();
        target.sendSystemMessage(Component.translatable("message.marketblocks.shop_buyer.rage." + msgIndex, nameStr));
    }

    public void triggerRevenge(Player player) {
        this.isRaging = true;
        this.targetShop = null;
        this.setTarget(player);
        this.playSound(SoundEvents.VINDICATOR_CELEBRATE, 1.0F, 1.0F);
        equipRageWeapon();

        String nameStr = this.hasCustomName() ? this.getCustomName().getString() : Component.translatable("entity.marketblocks.shop_buyer").getString();
        player.sendSystemMessage(Component.translatable("message.marketblocks.shop_buyer.revenge", nameStr));
    }

    public void calmDown() {
        this.isRaging = false;
        this.setTarget(null);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    private void equipRageWeapon() {
        ItemStack weapon;
        switch (this.getTraderRank()) {
            case NOBLE -> weapon = new ItemStack(Items.NETHERITE_SWORD);
            case WEALTHY -> weapon = new ItemStack(Items.DIAMOND_SWORD);
            case CITIZEN -> weapon = new ItemStack(Items.IRON_SWORD);
            default -> weapon = new ItemStack(Items.IRON_SWORD);
        }

        int enchantChance = switch (this.getTraderRank()) {
            case NOBLE -> 90;
            case WEALTHY -> 60;
            case CITIZEN -> 30;
        };

        if (this.random.nextInt(100) < enchantChance) {
            var registry = this.level().registryAccess().registry(Registries.ENCHANTMENT);
            if (registry.isPresent()) {
                var sharpnessHolder = registry.get().getHolder(Enchantments.SHARPNESS);
                if (sharpnessHolder.isPresent()) {
                    int level;
                    if (this.random.nextInt(100) == 0) {
                        level = 10; // Super rare Sharpness X
                    } else {
                        int maxLvl = switch (this.getTraderRank()) {
                            case NOBLE -> 5;
                            case WEALTHY -> 4;
                            case CITIZEN -> 3;
                        };
                        level = 1 + this.random.nextInt(maxLvl);
                    }
                    weapon.enchant(sharpnessHolder.get(), level);
                }
            }
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
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

    // --- Ranks & Categories ---

    public TraderRank getTraderRank() {
        int ordinal = this.entityData.get(DATA_TRADER_RANK);
        TraderRank[] values = TraderRank.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : TraderRank.CITIZEN;
    }

    public void setTraderRank(TraderRank rank) {
        this.entityData.set(DATA_TRADER_RANK, rank.ordinal());
    }

    public InterestCategory getInterestCategory() {
        int ordinal = this.entityData.get(DATA_INTEREST_CATEGORY);
        InterestCategory[] values = InterestCategory.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : InterestCategory.GENERAL;
    }

    public void setInterestCategory(InterestCategory category) {
        this.entityData.set(DATA_INTEREST_CATEGORY, category.ordinal());
    }

    public boolean isInterestedIn(ShopCategory category) {
        if (category == ShopCategory.NONE) {
            return false;
        }
        switch (this.getInterestCategory()) {
            case FARMER:
                return category == ShopCategory.FOOD_POTIONS || category == ShopCategory.BLOCKS
                        || category == ShopCategory.MISC;
            case ALCHEMIST:
                return category == ShopCategory.FOOD_POTIONS || category == ShopCategory.VALUABLES;
            case BLACKSMITH:
                return category == ShopCategory.WEAPONS_ARMOR || category == ShopCategory.TOOLS
                        || category == ShopCategory.BLOCKS;
            case VALUABLES:
                return category == ShopCategory.VALUABLES;
            case GENERAL:
            default:
                return true;
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        RandomSource random = level.getRandom();

        int configMin = TraderConfig.MIN_BUDGET.get();
        int configMax = TraderConfig.MAX_BUDGET.get();
        int roll = random.nextInt(100);
        ShopBuyerEntity.TraderRank rank;
        int budget;
        if (roll < 5) {
            rank = ShopBuyerEntity.TraderRank.NOBLE;
            budget = 1024 + random.nextInt(7169); // 1024 - 8192
        } else if (roll < 30) {
            rank = ShopBuyerEntity.TraderRank.WEALTHY;
            budget = 256 + random.nextInt(769); // 256 - 1024
        } else {
            rank = ShopBuyerEntity.TraderRank.CITIZEN;
            budget = 32 + random.nextInt(97); // 32 - 128
        }
        budget = Math.max(configMin, Math.min(configMax, budget));

        ShopBuyerEntity.InterestCategory category = ShopBuyerEntity.InterestCategory.values()[random
                .nextInt(ShopBuyerEntity.InterestCategory.values().length)];

        this.setTraderRank(rank);
        this.setInterestCategory(category);
        this.setBudget(budget);

        if (TraderConfig.NAMES_ENABLED.get()) {
            String name = TraderEconomyManager.get().getRandomName(random);
            if (name != null) {
                this.setCustomName(Component.literal(name));
                this.setCustomNameVisible(true);
            }
        }

        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
}
