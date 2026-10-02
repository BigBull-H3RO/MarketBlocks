package de.bigbull.marketblocks.feature.singleoffer.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Trigger fired when a player buys items in bulk (shift-click).
 * Used for advancement criteria.
 */
public class ShopWholesalerTrigger extends SimpleCriterionTrigger<ShopWholesalerTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
                        )
                        .apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> boughtBulk() {
            return RegistriesInit.SHOP_WHOLESALER_TRIGGER.get().createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}
