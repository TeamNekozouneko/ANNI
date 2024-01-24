package net.nekozouneko.anni.gui.shop;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.VaultUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PointCharger extends AbstractGui {

    public PointCharger(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

        if (inventory == null)
            Bukkit.createInventory(this, 54, mm.build("gui.point_charger.title"));

        ItemStack chargeButton = ItemStackBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                .name(mm.build("gui.point_charger.charge"))
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "action"), PersistentDataType.STRING, "start_charge")
                .build();

        for (int i = 45; i < inventory.getSize(); i++) inventory.setItem(i, chargeButton);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;

        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        PersistentDataContainer pdc = event.getCurrentItem().getItemMeta().getPersistentDataContainer();

        if (pdc.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "action"), PersistentDataType.STRING, "").equals("start_charge")) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(ANNIPlugin.getInstance(), () ->
                    event.getWhoClicked().closeInventory()
            );
            return;
        }

        if (event.getCurrentItem().getType() != Material.GOLD_INGOT) event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != this) return;

        int ingots = 0;
        List<ItemStack> returns = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            if (item.getType() == Material.GOLD_INGOT) {
                ingots += item.getAmount();
            }
            else {
                returns.add(item);
            }
        }

        VaultUtil.getEco().depositPlayer((OfflinePlayer) event.getPlayer(), 500 * ingots);
        CmnUtil.giveOrDrop(player, returns);
        unregisterAllGuiListeners((Player) event.getPlayer());
    }

}
