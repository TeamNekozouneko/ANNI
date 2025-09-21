package net.nekozouneko.anni.arena.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.view.FurnaceView;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FurnaceManager extends BukkitRunnable {

    @SuppressWarnings("UnstableApiUsage")
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

        private static final List<FurnaceRecipe> CACHED_FURNACE_RECIPES = new ArrayList<>();

        private static FurnaceRecipe getRecipe(ItemStack item) {
            for (FurnaceRecipe recipe: CACHED_FURNACE_RECIPES) {
                if (recipe.getInputChoice().test(item.asOne()))
                    return recipe;
            }

            return null;
        }

        @Getter
        private FurnaceView view;

        VirtualFurnace(Player player) {
            view = MenuType.FURNACE.create(player);
        }

        private int cookTime = 0;
        private int cookDuration = 0;
        private int burnTime = 0;
        private int burnDuration = 0;
        private boolean isBurning = false;
        private FurnaceRecipe recipe = null;

        public void open() {
            if (view.getPlayer() != Bukkit.getPlayer(view.getPlayer().getUniqueId())) {
                ItemStack[] stacks = view.getTopInventory().getContents();
                view = MenuType.FURNACE.create(Bukkit.getPlayer(view.getPlayer().getUniqueId()));
                view.getTopInventory().setContents(stacks);
            }

            view.open();
        }
        
        void applyView() {
            view.setCookTime(cookTime, cookDuration);
            view.setBurnTime(burnTime, burnDuration);
        }

        void update() {
            if (isBurning) { //すでに燃えている場合、そのまま燃やす
                burnTime--;
                if (burnTime <= 0) {
                    burnDuration = 0;
                    isBurning = false;
                }
            }

            recipe = view.getTopInventory().getSmelting() == null || view.getTopInventory().getSmelting().getType().isAir() ? null : getRecipe(view.getTopInventory().getSmelting());
            if (recipe != null) cookDuration = recipe.getCookingTime();

            // 燃料補充
            if (recipe != null && !isBurning && (view.getTopInventory().getFuel() != null && view.getTopInventory().getFuel().getType().isFuel())) {
                burnDuration = view.getTopInventory().getFuel().getType().asItemType().getBurnDuration();

                if (view.getTopInventory().getFuel().getType() == Material.LAVA_BUCKET) {
                    view.getTopInventory().setFuel(ItemStack.of(Material.BUCKET));
                }
                else if (view.getTopInventory().getFuel().getAmount() == 1) view.getTopInventory().setFuel(null);
                else view.getTopInventory().getFuel().subtract();

                burnTime = burnDuration;
                isBurning = true;
            }

            if (recipe == null || !checkSmelting() || !isBurning) { // レシピに該当するものがない、精錬ができる状態にない、燃料がない場合は料理時間を減らす
                if (cookTime > 1) cookTime = Math.max(cookTime - 2, 0);

                if  (cookDuration <= 0)
                    cookDuration = 0;

                return;
            }

            if (cookTime >= recipe.getCookingTime()) {
                view.getTopInventory().getSmelting().subtract();

                if (view.getTopInventory().getResult() == null) {
                    view.getTopInventory().setResult(recipe.getResult().clone());
                }
                else view.getTopInventory().getResult().add(recipe.getResult().getAmount());

                cookTime = 0;
                return;
            }

            cookTime++;

            if (!view.getTopInventory().getViewers().isEmpty()) applyView();
        }

        private boolean checkSmelting() {
            ItemStack smelting = view.getTopInventory().getItem(0);

            boolean checkSmelting = !(smelting == null || smelting.getType().isAir()); // そもそも精錬するものがあるか
            // 精錬済みの場所にアイテムがあるか、あった場合は同じアイテムまた上限スタック数を超えていないか
            boolean checkResult = view.getTopInventory().getResult() == null || view.getTopInventory().getItem(2).getType().isAir() || (recipe != null && recipe.getResult().isSimilar(view.getTopInventory().getResult()) && view.getTopInventory().getItem(2).getAmount() <= view.getTopInventory().getItem(2).getMaxStackSize());

            return checkSmelting && checkResult;
        }

    }

    private final Map<UUID, VirtualFurnace> furnaces = new HashMap<>();

    public void clear() {
        furnaces.clear();
    }

    public void remove(Player player) {
        furnaces.remove(player.getUniqueId());
    }

    public void open(Player player) {
        furnaces.computeIfAbsent(player.getUniqueId(), (key) -> new VirtualFurnace(player)).open();
    }

    @Override
    public void run() {
        furnaces.forEach((id, furnace) -> furnace.update());
    }
}
