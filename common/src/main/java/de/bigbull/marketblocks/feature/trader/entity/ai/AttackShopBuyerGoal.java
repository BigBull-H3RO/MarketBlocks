package de.bigbull.marketblocks.feature.trader.entity.ai;

import de.bigbull.marketblocks.feature.trader.entity.ShopBuyerEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.raid.Raider;

import java.lang.reflect.Field;

/**
 * Target goal added to hostile mobs (Zombies, Raiders, Vexes, Zoglins) so that
 * they naturally target and hunt Wandering Shop Buyers like they do with vanilla
 * Villagers and Wandering Traders.
 */
public class AttackShopBuyerGoal extends NearestAttackableTargetGoal<ShopBuyerEntity> {

    private static Field targetSelectorField;

    static {
        try {
            targetSelectorField = Mob.class.getDeclaredField("targetSelector");
            targetSelectorField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // In obfuscated production environments, locate the second GoalSelector field on Mob
            int count = 0;
            for (Field field : Mob.class.getDeclaredFields()) {
                if (GoalSelector.class.isAssignableFrom(field.getType())) {
                    count++;
                    if (count == 2) {
                        field.setAccessible(true);
                        targetSelectorField = field;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public AttackShopBuyerGoal(Mob mob) {
        super(mob, ShopBuyerEntity.class, 10, true, false,
                (target, level) -> target instanceof ShopBuyerEntity buyer && !buyer.isInvisible());
    }

    public static GoalSelector getTargetSelector(Mob mob) {
        if (targetSelectorField != null) {
            try {
                return (GoalSelector) targetSelectorField.get(mob);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Attaches this targeting goal to the given mob if it is a hostile mob that hunts traders
     * and does not already have the goal registered.
     */
    public static void addHostileTargetGoal(Mob mob) {
        if (mob instanceof Zombie || mob instanceof Raider || mob instanceof Vex || mob instanceof Zoglin) {
            GoalSelector targetSelector = getTargetSelector(mob);
            if (targetSelector != null) {
                boolean alreadyHasGoal = targetSelector.getAvailableGoals().stream()
                        .anyMatch(wg -> wg.getGoal() instanceof AttackShopBuyerGoal);
                if (!alreadyHasGoal) {
                    targetSelector.addGoal(3, new AttackShopBuyerGoal(mob));
                }
            }
        }
    }
}
