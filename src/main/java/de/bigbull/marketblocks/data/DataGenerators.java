package de.bigbull.marketblocks.data;

import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.data.lang.ModDeLangProvider;
import de.bigbull.marketblocks.data.lang.ModEnLangProvider;
import de.bigbull.marketblocks.data.recipe.ModRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Main class for data generation. It registers all data providers for the mod.
 * This class is registered on the mod's event bus to listen for the GatherDataEvent.
 */
public class DataGenerators {

    /**
     * Event listener method that gathers and registers all data providers when data generation is run.
     * @param event The data gathering event.
     */
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // The data generation process can throw various exceptions.
        // A broad catch is used here to ensure that any single provider failure
        // is logged without crashing the entire data generation process.
        try {
            // Client-side data providers
            generator.addProvider(event.includeClient(), new ModEnLangProvider(output));
            generator.addProvider(event.includeClient(), new ModDeLangProvider(output));

            // Server-side data providers
            generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));

        } catch (RuntimeException e) {
            MarketBlocks.LOGGER.error("Failed to generate data", e);
        }
    }
}
