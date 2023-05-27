package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerDenyActionListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final NamespacedKey anniKit = new NamespacedKey(plugin, "kit-item");

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getCurrentGame().getState().getId() >= 0) {
            PersistentDataContainer pdc = e.getItemDrop().getItemStack()
                    .getItemMeta().getPersistentDataContainer();

            if (pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
                e.getPlayer().playSound(e.getItemDrop().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                e.getItemDrop().remove();
                return;
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (plugin.getCurrentGame().getState().getId() >= 0) {
            for (ItemStack is : e.getInventory().getMatrix()) {
                if (is == null || is.getType().isAir()) continue;
                PersistentDataContainer pdc = is.getItemMeta().getPersistentDataContainer();
                if (pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (plugin.getCurrentGame().getState().getId() >= 0) {
            if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

            if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().getOrDefault(
                    anniKit, PersistentDataType.INTEGER, 0
            ) == 1) {
                if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE && e.getInventory() != null) {
                    if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
                        e.setCurrentItem(null);
                        ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        return;
                    }
                    else if (
                            !(e.getInventory().getType() == InventoryType.CRAFTING ||
                                    e.getInventory().getType() == InventoryType.PLAYER ||
                                    e.getInventory().getType() == InventoryType.CREATIVE
                            )
                    ) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

}
