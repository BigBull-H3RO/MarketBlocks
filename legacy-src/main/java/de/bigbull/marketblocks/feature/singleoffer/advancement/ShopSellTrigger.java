package de.bigbull.marketblocks.feature.singleoffer.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Trigger fired when a player sells an item in their shop.
 * Used for advancement criteria.
 */
public class ShopSellTrigger extends SimpleCriterionTrigger<ShopSellTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    /**
     * Triggers with the current cumulative sell count for the player.
     */
    public void trigger(ServerPlayer player, int totalSellCount) {
        this.trigger(player, instance -> instance.matches(totalSellCount));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, int minSellCount) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                                Codec.INT.optionalFieldOf("min_sell_count", 1).forGetter(TriggerInstance::minSellCount)
                        )
                        .apply(instance, TriggerInstance::new)
        );

        public boolean matches(int totalSellCount) {
            return totalSellCount >= minSellCount;
        }

        /**
         * Criterion that triggers on any sale (minimum 1).
         */
        public static Criterion<TriggerInstance> soldItem() {
            return RegistriesInit.SHOP_SELL_TRIGGER.get().createCriterion(new TriggerInstance(Optional.empty(), 1));
        }

        /**
         * Criterion that triggers when the player has sold at least {@code minCount} items total.
         */
        public static Criterion<TriggerInstance> soldItems(int minCount) {
            return RegistriesInit.SHOP_SELL_TRIGGER.get().createCriterion(new TriggerInstance(Optional.empty(), minCount));
        }
    }
}
