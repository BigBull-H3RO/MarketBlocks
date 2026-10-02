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
 * Trigger fired when a shop runs out of stock.
 * Used for advancement criteria.
 */
public class ShopOutOfStockTrigger extends SimpleCriterionTrigger<ShopOutOfStockTrigger.TriggerInstance> {

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

        public static Criterion<TriggerInstance> wentOutOfStock() {
            return RegistriesInit.SHOP_OUT_OF_STOCK_TRIGGER.get().createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}
