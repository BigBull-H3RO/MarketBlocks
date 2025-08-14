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
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RegistriesInit.SMALL_SHOP_BLOCK.get(), 2)
                .pattern("###")
                .pattern("#D#")
                .pattern("###")
                .define('#', Items.OAK_PLANKS)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                .save(recipeOutput, getModId("small_shop_block"));
    }

    public ResourceLocation getModId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, path);
    }
}
