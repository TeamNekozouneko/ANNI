package net.nekozouneko.anniv2.gui.map;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        ItemStack tp = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = tp.getItemMeta();
        tpMeta.setDisplayName(mm.build("gui.map_editor.tp_to_world"));
        tp.setItemMeta(tpMeta);

        inventory.setItem(18, tp);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(player);
    }
}
