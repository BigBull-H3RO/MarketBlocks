package de.bigbull.marketblocks.data.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Generates item tags for all MarketBlocks items.
 * Copies block tags to their item equivalents for cross-mod compatibility.
 */
public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTagLookup,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagLookup, MarketBlocks.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(TagKey.create(
                Registries.BLOCK,
                ModBlockTagProvider.SHOP_BLOCKS_TAG_ID),
                TagKey.create(
                        Registries.ITEM,
                        ModBlockTagProvider.SHOP_BLOCKS_TAG_ID));
    }
}

