package de.bigbull.marketblocks.data;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModDeLangProvider;
import de.bigbull.marketblocks.data.lang.ModEnLangProvider;
import de.bigbull.marketblocks.data.recipe.ModRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        try {
            generator.addProvider(true, new ModEnLangProvider(output));
            generator.addProvider(true, new ModDeLangProvider(output));
            generator.addProvider(true, new ModRecipeProvider(output, event.getLookupProvider()));

        } catch (RuntimeException e) {
            MarketBlocks.LOGGER.error("Failed to generate data", e);
        }
    }
}
