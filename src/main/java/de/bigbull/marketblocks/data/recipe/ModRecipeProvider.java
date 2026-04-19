package de.bigbull.marketblocks.data.recipe;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.init.RegistriesInit;
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

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Aktueller Shop-Block (trade_stand)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RegistriesInit.TRADE_STAND_BLOCK.get(), 1)
                .pattern("#D#")
                .pattern("IEI")
                .pattern("#S#")
                .define('#', ItemTags.PLANKS)
                .define('D', Items.DEEPSLATE_TILE_SLAB)
                .define('E', Items.EMERALD)
                .define('I', Items.IRON_INGOT)
                .define('S', ItemTags.SIGNS)
                .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                .save(recipeOutput, getModId("trade_stand"));
    }

    public ResourceLocation getModId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, path);
    }
}
