package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantItemListener implements Listener {

    private static final float ENCHANTER_LEVEL_UP_CHANCE = 0.01f;
    private static final int MAX_CHANCE_INCREASE = 9;

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        var arena = ANNIPlugin.getInstance().getCurrentGame();

        if (!arena.getState().isInArena()) return;

        if (ANNIKit.getKitById(arena.getKit(event.getEnchanter()).getId()) != ANNIKit.ENCHANTER) return;

        float chance = ENCHANTER_LEVEL_UP_CHANCE;
        int availableLevel = Math.min(event.getEnchanter().getLevel() - event.getLevelHint(), MAX_CHANCE_INCREASE);
        chance = Math.max(chance * (availableLevel + 1), 0);

        var rand = new Random();
        if (!(rand.nextFloat() >= 1 - chance)) return;

        List<Enchantment> upgradeable = new ArrayList<>();
        event.getEnchantsToAdd().forEach((enchant, level) -> {
            if (enchant.getMaxLevel() < level + 1) return;

            upgradeable.add(enchant);
        });

        if (upgradeable.isEmpty()) return;

        Enchantment target = upgradeable.get(rand.nextInt(upgradeable.size()));
        event.getEnchantsToAdd().put(target, event.getEnchantsToAdd().get(target) + 1);

        event.getEnchanter().setLevel(event.getEnchanter().getLevel() - availableLevel);
    }
}
