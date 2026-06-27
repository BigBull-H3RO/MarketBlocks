package de.bigbull.marketblocks.data.tag;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Generates block tags for all MarketBlocks blocks.
 * Ensures correct tool behaviour and cross-mod compatibility.
 */
public class ModBlockTagProvider extends BlockTagsProvider {
        public static final ResourceLocation SHOP_BLOCKS_TAG_ID = ResourceLocation
                        .fromNamespaceAndPath(MarketBlocks.MODID, "shop_blocks");

        public static final TagKey<Block> FTBCHUNKS_INTERACT_WHITELIST = TagKey.create(Registries.BLOCK,
                        ResourceLocation.fromNamespaceAndPath("ftbchunks", "interact_whitelist"));

        public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                        @Nullable ExistingFileHelper existingFileHelper) {
                super(output, lookupProvider, MarketBlocks.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
                tag(BlockTags.MINEABLE_WITH_AXE)
                                .add(RegistriesInit.TRADE_STAND_BLOCK.get())
                                .add(RegistriesInit.TRADE_STAND_BLOCK_TOP.get())
                                .add(RegistriesInit.MARKETCRATE_BLOCK.get());

                tag(TagKey.create(
                                Registries.BLOCK,
                                SHOP_BLOCKS_TAG_ID))
                                .add(RegistriesInit.MARKETCRATE_BLOCK.get())
                                .add(RegistriesInit.TRADE_STAND_BLOCK.get());

                tag(FTBCHUNKS_INTERACT_WHITELIST)
                                .add(RegistriesInit.TRADE_STAND_BLOCK.get())
                                .add(RegistriesInit.MARKETCRATE_BLOCK.get());
        }
}

