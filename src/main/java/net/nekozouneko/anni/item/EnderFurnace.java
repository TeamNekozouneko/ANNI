package net.nekozouneko.anni.item;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.TranslationManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class EnderFurnace implements Listener {

    public static ItemStack get(Locale locale) {
        TranslationManager tm = ANNIPlugin.getInstance().getTranslationManager();

        ItemStack item = ItemStack.of(Material.FURNACE);
        item.editMeta(meta -> {
            meta.displayName(tm.component(locale, "item.ender_furnace.name"));
            meta.lore(tm.componentList(locale, "item.ender_furnace.lore"));
            meta.setEnchantmentGlintOverride(true);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.getPersistentDataContainer().set(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "ender-furnace");
        });

        return item;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        if (isEnderFurnace(event.getItem())) {
            ANNIPlugin.getInstance().getFurnaceManager().open(event.getPlayer());
            event.setCancelled(true);
        }
    }

    public static boolean isEnderFurnace(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        return item.getItemMeta().getPersistentDataContainer().getOrDefault(
                new NamespacedKey(ANNIPlugin.getInstance(), "special-item"),
                PersistentDataType.STRING, ""
        ).equals("ender-furnace");
    }

}
