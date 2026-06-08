package de.bigbull.marketblocks.data.loot;

import de.bigbull.marketblocks.core.init.RegistriesInit;
import de.bigbull.marketblocks.feature.singleoffer.block.TradeStandBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

/**
 * Data provider for generating block loot tables.
 * Specifies what items drop when blocks are broken, including complex logic
 * for preserving NBT data (like showcase state) when silk-touching Trade Stands.
 */
public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(RegistriesInit.MARKETCRATE_BLOCK.get());
        dropSelf(RegistriesInit.MARKETPLACE_BLOCK.get());

        this.add(RegistriesInit.TRADE_STAND_BLOCK_TOP.get(), noDrop());

        this.add(RegistriesInit.TRADE_STAND_BLOCK.get(), block -> LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                                this.applyExplosionCondition(block,
                                        LootItem.lootTableItem(block)
                                                .when(this.hasSilkTouch())
                                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                                .hasProperty(TradeStandBlock.HAS_SHOWCASE, true)))
                                                .apply(CopyBlockState.copyState(block).copy(TradeStandBlock.HAS_SHOWCASE))

                                                .otherwise(LootItem.lootTableItem(block))
                                )
                        )
                )
        );
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return RegistriesInit.BLOCKS.getEntries().stream().map(entry -> (Block) entry.get())::iterator;
    }
}
