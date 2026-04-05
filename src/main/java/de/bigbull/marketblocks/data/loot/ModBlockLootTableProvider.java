package de.bigbull.marketblocks.data.loot;

import de.bigbull.marketblocks.util.RegistriesInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(RegistriesInit.SMALL_SHOP_BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return RegistriesInit.BLOCKS.getEntries().stream().map(entry -> (Block) entry.get())::iterator;
    }
}
