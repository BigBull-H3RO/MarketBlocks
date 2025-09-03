package de.bigbull.marketblocks.data.recipe;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.util.RegistriesInit;
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
 * Generates recipes for the mod.
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /**
     * Defines all the recipes for the mod.
     * @param recipeOutput The output consumer for the generated recipes.
     */
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RegistriesInit.SMALL_SHOP_BLOCK.get(), 1)
                .pattern("#R#")
                .pattern("#E#")
                .pattern("###")
                .define('#', ItemTags.PLANKS)
                .define('R', Items.RED_WOOL)
                .define('E', Items.EMERALD)
                .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                .save(recipeOutput, getModId("small_shop_block"));
    }

    /**
     * Helper method to create a ResourceLocation with the mod's ID.
     * @param path The path for the resource location.
     * @return A new ResourceLocation.
     */
    private ResourceLocation getModId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, path);
    }
}
