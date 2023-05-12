package net.nekozouneko.anniv2.gui.map;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MapEditor extends AbstractGui {

    private final ANNIMap map;

    public MapEditor(ANNIPlugin plugin, Player player, ANNIMap map) {
        super(plugin, player);

        this.map = map;
    }

    @Override
    public void update() {
        final MessageManager mm = plugin.getMessageManager();

        if (inventory == null)
            inventory = Bukkit.createInventory(
                    this, 27,
                    mm.build("gui.map_editor.title", map.getId())
            );

        ItemStack tp = ItemStackBuilder.of(Material.ENDER_PEARL)
                .name(mm.build("gui.map_editor.tp_to_world"))
                .persistentData(
                        new NamespacedKey(plugin, "action"),
                        PersistentDataType.STRING, "teleport"
                )
                .build();

        inventory.setItem(18, tp);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        ItemStack item = e.getCurrentItem();

        if (item == null) return;

        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();

        e.setCancelled(true);

        if (c.has(new NamespacedKey(plugin, "action"), PersistentDataType.STRING)) {
            switch (c.get(new NamespacedKey(plugin, "action"), PersistentDataType.STRING)) {
                case "teleport": {
                    player.closeInventory();
                    player.teleport(map.getBukkitWorld().getSpawnLocation());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(player);
    }
}
