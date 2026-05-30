package de.bigbull.marketblocks.data.tag;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Generates block tags for all MarketBlocks blocks.
 * Ensures correct tool behaviour and cross-mod compatibility.
 */
public class ModBlockTagProvider extends BlockTagsProvider {
        public static final net.minecraft.resources.ResourceLocation SHOP_BLOCKS_TAG_ID = net.minecraft.resources.ResourceLocation
                        .fromNamespaceAndPath(MarketBlocks.MODID, "shop_blocks");

        public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                        @Nullable ExistingFileHelper existingFileHelper) {
                super(output, lookupProvider, MarketBlocks.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
                // All shop blocks are mineable with an axe (wood-based)
                tag(BlockTags.MINEABLE_WITH_AXE)
                                .add(RegistriesInit.TRADE_STAND_BLOCK.get())
                                .add(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())
                                .add(RegistriesInit.MARKETCRATE_BLOCK.get())
                                .add(RegistriesInit.MARKETPLACE_BLOCK.get());

                // Custom tag for cross-mod compatibility
                tag(net.minecraft.tags.TagKey.create(
                                net.minecraft.core.registries.Registries.BLOCK,
                                SHOP_BLOCKS_TAG_ID))
                                .add(RegistriesInit.TRADE_STAND_BLOCK.get())
                                .add(RegistriesInit.MARKETCRATE_BLOCK.get())
                                .add(RegistriesInit.MARKETPLACE_BLOCK.get());
        }
}
