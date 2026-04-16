package de.bigbull.marketblocks.data.loot;

import de.bigbull.marketblocks.util.RegistriesInit;
import de.bigbull.marketblocks.util.custom.block.SmallShopBlock;
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

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(RegistriesInit.SMALL_SHOP_BLOCK_TEST.get());

        // 1. Top-Block droppt überhaupt nichts (das Item wird vom Base-Block gemanagt)
        this.add(RegistriesInit.SMALL_SHOP_BLOCK_TOP.get(), noDrop());

        // 2. Custom Loot-Logik für den Small Shop Block
        this.add(RegistriesInit.SMALL_SHOP_BLOCK.get(), block -> LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                                // applyExplosionCondition fügt automatisch die "survives_explosion" Condition hinzu
                                this.applyExplosionCondition(block,
                                        // WENN: Behutsamkeit + Vitrine aktiv
                                        LootItem.lootTableItem(block)
                                                .when(this.hasSilkTouch())
                                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                                .hasProperty(SmallShopBlock.HAS_SHOWCASE, true)))
                                                .apply(CopyBlockState.copyState(block).copy(SmallShopBlock.HAS_SHOWCASE))

                                                // ANSONSTEN: Standard-Drop (ohne NBT/Vitrine)
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
