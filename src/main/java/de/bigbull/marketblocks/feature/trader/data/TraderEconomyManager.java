package de.bigbull.marketblocks.feature.trader.data;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TraderEconomyManager {
    private static final TraderEconomyManager INSTANCE = new TraderEconomyManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<Item, Double> baseValues = new HashMap<>();
    private final Set<Item> blacklist = new HashSet<>();
    private final Map<Item, Double> calculatedCache = new HashMap<>();
    private final List<String> traderNames = new ArrayList<>();

    private Path configDir;

    private TraderEconomyManager() {
        this.configDir = FMLPaths.CONFIGDIR.get().resolve("marketblocks");
    }

    public static TraderEconomyManager get() {
        return INSTANCE;
    }

    public void load() {
        try {
            Files.createDirectories(configDir);
            loadValues();
            loadBlacklist();
            loadTraderNames();
            calculatedCache.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadValues() throws IOException {
        Path file = configDir.resolve("trader_item_values.json");
        if (!Files.exists(file)) {
            JsonObject defaultObj = new JsonObject();

            // --- Valuables & Ores ---
            defaultObj.addProperty("minecraft:netherite_ingot", 100.0);
            defaultObj.addProperty("minecraft:netherite_scrap", 25.0);
            defaultObj.addProperty("minecraft:diamond", 15.0);
            defaultObj.addProperty("minecraft:emerald", 10.0);
            defaultObj.addProperty("minecraft:gold_ingot", 5.0);
            defaultObj.addProperty("minecraft:iron_ingot", 2.0);
            defaultObj.addProperty("minecraft:copper_ingot", 0.5);
            defaultObj.addProperty("minecraft:lapis_lazuli", 0.5);
            defaultObj.addProperty("minecraft:redstone", 0.2);
            defaultObj.addProperty("minecraft:coal", 0.5);
            defaultObj.addProperty("minecraft:quartz", 0.5);
            defaultObj.addProperty("minecraft:amethyst_shard", 1.0);

            // --- Rare & Special ---
            defaultObj.addProperty("minecraft:totem_of_undying", 150.0);
            defaultObj.addProperty("minecraft:enchanted_golden_apple", 200.0);
            defaultObj.addProperty("minecraft:golden_apple", 15.0);
            defaultObj.addProperty("minecraft:shulker_shell", 25.0);
            defaultObj.addProperty("minecraft:saddle", 10.0);
            defaultObj.addProperty("minecraft:name_tag", 10.0);
            defaultObj.addProperty("minecraft:ender_pearl", 2.0);
            defaultObj.addProperty("minecraft:blaze_rod", 3.0);
            defaultObj.addProperty("minecraft:ghast_tear", 10.0);
            defaultObj.addProperty("minecraft:phantom_membrane", 2.0);

            // --- Food & Farming ---
            defaultObj.addProperty("minecraft:cooked_beef", 0.1);
            defaultObj.addProperty("minecraft:cooked_porkchop", 0.1);
            defaultObj.addProperty("minecraft:cooked_mutton", 0.08);
            defaultObj.addProperty("minecraft:cooked_chicken", 0.06);
            defaultObj.addProperty("minecraft:bread", 0.05);
            defaultObj.addProperty("minecraft:apple", 0.05);
            defaultObj.addProperty("minecraft:carrot", 0.02);
            defaultObj.addProperty("minecraft:potato", 0.02);
            defaultObj.addProperty("minecraft:wheat", 0.01);
            defaultObj.addProperty("minecraft:sugar_cane", 0.02);
            defaultObj.addProperty("minecraft:leather", 0.2);
            defaultObj.addProperty("minecraft:feather", 0.02);
            defaultObj.addProperty("minecraft:slime_ball", 1.0);
            defaultObj.addProperty("minecraft:honey_bottle", 1.0);

            // --- Monster Loot ---
            defaultObj.addProperty("minecraft:gunpowder", 0.5);
            defaultObj.addProperty("minecraft:bone", 0.1);
            defaultObj.addProperty("minecraft:string", 0.05);
            defaultObj.addProperty("minecraft:spider_eye", 0.1);
            defaultObj.addProperty("minecraft:rotten_flesh", 0.01);

            // --- Blocks & Nature ---
            defaultObj.addProperty("minecraft:oak_log", 0.1);
            defaultObj.addProperty("minecraft:spruce_log", 0.1);
            defaultObj.addProperty("minecraft:birch_log", 0.1);
            defaultObj.addProperty("minecraft:jungle_log", 0.1);
            defaultObj.addProperty("minecraft:acacia_log", 0.1);
            defaultObj.addProperty("minecraft:dark_oak_log", 0.1);
            defaultObj.addProperty("minecraft:cherry_log", 0.15);
            defaultObj.addProperty("minecraft:mangrove_log", 0.15);
            defaultObj.addProperty("minecraft:obsidian", 2.0);
            defaultObj.addProperty("minecraft:glass", 0.05);
            defaultObj.addProperty("minecraft:cobblestone", 0.01);
            defaultObj.addProperty("minecraft:stone", 0.02);
            defaultObj.addProperty("minecraft:deepslate", 0.02);

            Files.writeString(file, GSON.toJson(defaultObj));
        }

        baseValues.clear();
        JsonObject obj = GSON.fromJson(Files.readString(file), JsonObject.class);
        for (String key : obj.keySet()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(key));
            if (item != Items.AIR) {
                baseValues.put(item, obj.get(key).getAsDouble());
            }
        }
    }

    private void loadBlacklist() throws IOException {
        Path file = configDir.resolve("trader_blacklist.json");
        if (!Files.exists(file)) {
            JsonArray defaultArr = new JsonArray();

            // --- Admin / Creative Blocks ---
            defaultArr.add("minecraft:bedrock");
            defaultArr.add("minecraft:barrier");
            defaultArr.add("minecraft:command_block");
            defaultArr.add("minecraft:chain_command_block");
            defaultArr.add("minecraft:repeating_command_block");
            defaultArr.add("minecraft:structure_block");
            defaultArr.add("minecraft:structure_void");
            defaultArr.add("minecraft:jigsaw");
            defaultArr.add("minecraft:light");

            // --- Common / Spam Blocks ---
            defaultArr.add("minecraft:dirt");
            defaultArr.add("minecraft:coarse_dirt");
            defaultArr.add("minecraft:rooted_dirt");
            defaultArr.add("minecraft:grass_block");
            defaultArr.add("minecraft:podzol");
            defaultArr.add("minecraft:mycelium");
            defaultArr.add("minecraft:sand");
            defaultArr.add("minecraft:red_sand");
            defaultArr.add("minecraft:gravel");
            defaultArr.add("minecraft:netherrack");
            defaultArr.add("minecraft:end_stone");
            defaultArr.add("minecraft:tuff");
            defaultArr.add("minecraft:calcite");

            // --- Worthless Vegetation ---
            defaultArr.add("minecraft:dead_bush");
            defaultArr.add("minecraft:fern");
            defaultArr.add("minecraft:tall_grass");
            defaultArr.add("minecraft:seagrass");
            defaultArr.add("minecraft:kelp");

            // --- Miscellaneous ---
            defaultArr.add("minecraft:poisonous_potato");

            Files.writeString(file, GSON.toJson(defaultArr));
        }

        blacklist.clear();
        JsonArray arr = GSON.fromJson(Files.readString(file), JsonArray.class);
        for (JsonElement el : arr) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(el.getAsString()));
            if (item != Items.AIR) {
                blacklist.add(item);
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(configDir);
            JsonObject valObj = new JsonObject();
            for (Map.Entry<Item, Double> entry : baseValues.entrySet()) {
                valObj.addProperty(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), entry.getValue());
            }
            Files.writeString(configDir.resolve("trader_item_values.json"), GSON.toJson(valObj));

            JsonArray blArr = new JsonArray();
            for (Item item : blacklist) {
                blArr.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
            Files.writeString(configDir.resolve("trader_blacklist.json"), GSON.toJson(blArr));

            calculatedCache.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTraderNames() throws IOException {
        Path file = configDir.resolve("trader_names.json");
        if (!Files.exists(file)) {
            JsonArray defaultArr = new JsonArray();
            defaultArr.add("Gilbert");
            defaultArr.add("Martha");
            defaultArr.add("Ezra");
            defaultArr.add("Finn");
            defaultArr.add("Iris");
            defaultArr.add("Hugo");
            defaultArr.add("Clara");
            defaultArr.add("Oscar");
            defaultArr.add("Petra");
            defaultArr.add("Emil");
            defaultArr.add("Rufus");
            defaultArr.add("Mabel");
            defaultArr.add("Jasper");
            defaultArr.add("Ada");
            defaultArr.add("Felix");
            defaultArr.add("Nora");
            defaultArr.add("Silas");
            defaultArr.add("Elara");
            defaultArr.add("Otto");
            defaultArr.add("Hazel");
            Files.writeString(file, GSON.toJson(defaultArr));
        }

        traderNames.clear();
        JsonArray arr = GSON.fromJson(Files.readString(file), JsonArray.class);
        for (JsonElement el : arr) {
            String name = el.getAsString().trim();
            if (!name.isEmpty()) {
                traderNames.add(name);
            }
        }
    }

    /**
     * Returns a random trader name from the configured list, or null if names are empty.
     */
    public String getRandomName(java.util.Random random) {
        if (traderNames.isEmpty()) return null;
        return traderNames.get(random.nextInt(traderNames.size()));
    }

    public boolean isBlacklisted(Item item) {
        return blacklist.contains(item);
    }

    public void setBlacklisted(Item item, boolean blacklisted) {
        if (blacklisted)
            blacklist.add(item);
        else
            blacklist.remove(item);
        save();
    }

    public Double getBaseValue(Item item) {
        return baseValues.get(item);
    }

    public void setValue(Item item, double value) {
        baseValues.put(item, value);
        save();
    }

    public void removeValue(Item item) {
        baseValues.remove(item);
        save();
    }

    /**
     * Tries to evaluate the value of an item.
     * Returns null if no value can be found or calculated.
     */
    public Double evaluateItem(Item item, RecipeManager recipeManager) {
        if (isBlacklisted(item))
            return null;
        if (baseValues.containsKey(item))
            return baseValues.get(item);
        if (calculatedCache.containsKey(item))
            return calculatedCache.get(item);

        if (recipeManager != null) {
            Double calc = calculateFromRecipe(item, recipeManager, new HashSet<>());
            if (calc != null) {
                calculatedCache.put(item, calc);
                return calc;
            }
        }
        return null;
    }

    private Double calculateFromRecipe(Item target, RecipeManager recipeManager, Set<Item> visited) {
        if (visited.contains(target))
            return null; // Prevent infinite loops
        visited.add(target);

        // Find a crafting recipe that produces this item
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof CraftingRecipe recipe) {
                ItemStack resultItem = recipe.getResultItem(null);
                if (resultItem != null && resultItem.getItem() == target) {
                    double totalValue = 0;
                    boolean valid = true;

                    for (Ingredient ingredient : recipe.getIngredients()) {
                        if (ingredient.isEmpty())
                            continue;

                        ItemStack[] items = ingredient.getItems();
                        if (items.length == 0) {
                            valid = false;
                            break;
                        }

                        // Just take the first item of the ingredient as representative
                        Item ingItem = items[0].getItem();

                        Double ingValue = baseValues.get(ingItem);
                        if (ingValue == null) {
                            // Recursive calculation
                            ingValue = calculateFromRecipe(ingItem, recipeManager, visited);
                        }

                        if (ingValue == null) {
                            valid = false;
                            break; // Cannot calculate value of this ingredient
                        }
                        totalValue += ingValue;
                    }

                    if (valid && totalValue > 0) {
                        return totalValue / Math.max(1, resultItem.getCount());
                    }
                }
            }
        }

        visited.remove(target);
        return null;
    }
}
