package net.nekozouneko.anniv2.gui;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.kit.ANNIKit;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import net.nekozouneko.commons.spigot.inventory.special.SkullBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class KitSelector extends AbstractGui {

    private final MessageManager mm = plugin.getMessageManager();

    public KitSelector(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 27, mm.build("gui.kit_selector.title"));

        NamespacedKey kin = new NamespacedKey(plugin, "kit");

        inventory.setItem(0,
                ItemStackBuilder.of(Material.DIRT)
                        .name("§r" + ANNIKit.DEFAULT.getKit().getName())
                        .persistentData(kin, PersistentDataType.STRING, ANNIKit.DEFAULT.name())
                        .build()
        );

        inventory.setItem(1,
                ItemStackBuilder.of(Material.STONE_SWORD)
                        .name("§r" + ANNIKit.ASSAULT.getKit().getName())
                        .persistentData(kin, PersistentDataType.STRING, ANNIKit.ASSAULT.name())
                        .build()
        );

        inventory.setItem(2,
                ItemStackBuilder.of(Material.SHIELD)
                        .name("§r" + ANNIKit.DEFENSE.getKit().getName())
                        .persistentData(kin, PersistentDataType.STRING, ANNIKit.DEFENSE.name())
                        .build()
        );

        inventory.setItem(3,
                SkullBuilder.of(Material.PLAYER_HEAD)
                        .name("§r" + ANNIKit.MOCHI_MOCHI.getKit().getName())
                        .persistentData(kin, PersistentDataType.STRING, ANNIKit.MOCHI_MOCHI.name())
                        .owningPlayer(Bukkit.getOfflinePlayer("kinakomoti019"))
                        .build()
        );
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        PersistentDataContainer pdc = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        NamespacedKey kin = new NamespacedKey(plugin, "kit");

        if (pdc.has(kin, PersistentDataType.STRING)) {
            ANNIKit k;
            switch (pdc.getOrDefault(kin, PersistentDataType.STRING, "")) {
                case "ASSAULT":
                    k = ANNIKit.ASSAULT;
                    break;
                case "DEFAULT":
                    k = ANNIKit.DEFAULT;
                    break;
                case "DEFENSE":
                    k = ANNIKit.DEFENSE;
                    break;
                case "MOCHI_MOCHI":
                    k = ANNIKit.MOCHI_MOCHI;
                    break;
                default: {
                    k = ANNIKit.DEFAULT;
                    break;
                }
            }

            plugin.getCurrentGame().setKit(player, k);
            player.closeInventory();
            if (plugin.getCurrentGame().getState().getId() > 0) {
                player.setHealth(0);
            }
            else player.sendMessage(mm.build("gui.kit_selector.using", k.getKit().getName()));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(((Player) e.getPlayer()));
    }
}
