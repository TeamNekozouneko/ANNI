package net.nekozouneko.anni.arena.manager;

import org.bukkit.Bukkit;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.view.FurnaceView;

import java.util.*;

public class FurnaceManager {

    public static class VirtualFurnace {

        public static void reloadRecipes() {
            Iterator<Recipe> recipes = Bukkit.recipeIterator();

            CACHED_FURNACE_RECIPES.clear();
            while (recipes.hasNext()) {
                Recipe recipe = recipes.next();

                if (!(recipe instanceof FurnaceRecipe)) continue;

                CACHED_FURNACE_RECIPES.add(((FurnaceRecipe) recipe));
            }
        }

        public static final int DEFAULT_COOK_TOTAL_TIME = 200;
        private static final List<FurnaceRecipe> CACHED_FURNACE_RECIPES = new ArrayList<>();

        private final FurnaceInventory inventory;

        public VirtualFurnace(FurnaceInventory inventory) {
            this.inventory = inventory;
        }

        private int cookTime = 0;
        private boolean isBurning = false;

        public void applyView(FurnaceView view) {
            view.setCookTime(cookTime, DEFAULT_COOK_TOTAL_TIME);
            view.setBurnTime(isBurning? 1:0, isBurning? 1:0);
        }

        public void update() {

            if (cookTime == DEFAULT_COOK_TOTAL_TIME) {

            }
        }

        private boolean isValidSmelting(ItemStack item) {
            if (item == null || item.getType().isAir()) return false;

            FurnaceRecipe applicableRecipe = null;
            for (FurnaceRecipe recipe: CACHED_FURNACE_RECIPES) {
                if (!recipe.getInputChoice().test(item)) continue;


                applicableRecipe = recipe;
            }

            return true;
        }

    }

    private final Map<UUID, FurnaceInventory> furnaces = new HashMap<>();

    public void update() {
        furnaces.forEach((id, furnace) -> {
        });
    }
}
