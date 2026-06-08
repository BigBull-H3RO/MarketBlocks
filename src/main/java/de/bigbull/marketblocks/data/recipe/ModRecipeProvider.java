package de.bigbull.marketblocks.data.recipe;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.core.init.RegistriesInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * Data provider for generating crafting recipes.
 * Defines the recipes for crafting MarketBlocks items and blocks.
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RegistriesInit.TRADE_STAND_BLOCK.get(), 1)
                .pattern("#D#")
                .pattern("IEI")
                .pattern("#S#")
                .define('#', ItemTags.PLANKS)
                .define('D', Items.SMOOTH_STONE_SLAB)
                .define('E', Items.EMERALD)
                .define('I', Items.IRON_INGOT)
                .define('S', ItemTags.SIGNS)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .unlockedBy("has_chest", has(Items.CHEST))
                .save(recipeOutput, getModId("trade_stand"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RegistriesInit.MARKETCRATE_BLOCK.get(), 1)
                .pattern("#E#")
                .pattern("#C#")
                .pattern("BSB")
                .define('#', ItemTags.PLANKS)
                .define('C', Items.CHEST)
                .define('E', Items.EMERALD)
                .define('B', Items.BARREL)
                .define('S', ItemTags.SIGNS)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .unlockedBy("has_chest", has(Items.CHEST))
                .save(recipeOutput, getModId("marketcrate"));
    }

    public ResourceLocation getModId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, path);
    }
}
