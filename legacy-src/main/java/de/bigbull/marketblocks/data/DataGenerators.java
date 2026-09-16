package de.bigbull.marketblocks.data;

import de.bigbull.marketblocks.data.blockstate.ModBlockStateProvider;
import de.bigbull.marketblocks.data.lang.ModDeLangProvider;
import de.bigbull.marketblocks.data.lang.ModEnLangProvider;
import de.bigbull.marketblocks.data.lang.ModFrLangProvider;
import de.bigbull.marketblocks.data.lang.ModEsLangProvider;
import de.bigbull.marketblocks.data.loot.ModLootTableProvider;
import de.bigbull.marketblocks.data.recipe.ModRecipeProvider;
import de.bigbull.marketblocks.data.advancement.ModAdvancementProvider;
import de.bigbull.marketblocks.data.tag.ModBlockTagProvider;
import de.bigbull.marketblocks.data.tag.ModItemTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import java.util.List;

/**
 * Entry point for NeoForge data generation.
 * Registers all providers for assets (lang, blockstates) and data (tags, recipes, loot tables, advancements).
 */
public class DataGenerators {
    @SuppressWarnings("deprecation")
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        if (event.includeClient()) {
            generator.addProvider(true, new ModEnLangProvider(output));
            generator.addProvider(true, new ModDeLangProvider(output));
            generator.addProvider(true, new ModFrLangProvider(output));
            generator.addProvider(true, new ModEsLangProvider(output));
            generator.addProvider(true, new ModBlockStateProvider(output, event.getExistingFileHelper()));
        }
        if (event.includeServer()) {
            ModBlockTagProvider blockTagProvider = new ModBlockTagProvider(output, event.getLookupProvider(),
                    event.getExistingFileHelper());
            generator.addProvider(true, blockTagProvider);
            generator.addProvider(true, new ModItemTagProvider(output, event.getLookupProvider(),
                    blockTagProvider.contentsGetter(), event.getExistingFileHelper()));
            generator.addProvider(true, new ModRecipeProvider(output, event.getLookupProvider()));
            generator.addProvider(true, ModLootTableProvider.create(output, event.getLookupProvider()));
            generator.addProvider(true, new AdvancementProvider(output, event.getLookupProvider(), List.<AdvancementSubProvider>of(new ModAdvancementProvider())));
        }
    }
}
