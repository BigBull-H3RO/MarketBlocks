package de.bigbull.marketblocks.data;

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

        if (event.includeClient()) {
            generator.addProvider(true, new ModEnLangProvider(output));
            generator.addProvider(true, new ModDeLangProvider(output));
        }
        if (event.includeServer()) {
            generator.addProvider(true, new ModRecipeProvider(output, event.getLookupProvider()));
        }
    }
}
