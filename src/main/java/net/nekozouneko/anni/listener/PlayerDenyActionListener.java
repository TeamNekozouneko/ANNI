package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class PlayerDenyActionListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final NamespacedKey anniKit = new NamespacedKey(plugin, "kit-item");
    private final NamespacedKey noRemove = new NamespacedKey(plugin, "no-remove");

    private final List<Material> CRAFT_BLACKLIST = Arrays.asList(
            Material.FLINT_AND_STEEL
    );

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (SpectatorManager.isSpectating(e.getPlayer()) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (plugin.getCurrentGame().getState().getId() >= 0) {
            PersistentDataContainer pdc = e.getItemDrop().getItemStack()
                    .getItemMeta().getPersistentDataContainer();

            if (pdc.getOrDefault(noRemove, PersistentDataType.INTEGER, 0) == 1 || pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
                e.getPlayer().playSound(e.getItemDrop().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                e.getItemDrop().remove();
                return;
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (SpectatorManager.isSpectating(((Player) e.getWhoClicked())) && e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

        if (plugin.getCurrentGame().getState().getId() >= 0) {
            for (ItemStack is : e.getInventory().getMatrix()) {
                if (is == null || is.getType().isAir()) continue;
                PersistentDataContainer pdc = is.getItemMeta().getPersistentDataContainer();
                if (pdc.getOrDefault(noRemove, PersistentDataType.INTEGER, 0) == 1 || pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (CRAFT_BLACKLIST.contains(e.getRecipe().getResult().getType())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (SpectatorManager.isSpectating((Player) e.getWhoClicked()) && e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            if (e.getInventory().getType() != InventoryType.PLAYER) {
                e.setCancelled(true);
                return;
            }
        }

        if (plugin.getCurrentGame().getState().getId() >= 0) {
            if (e.getClick() == ClickType.NUMBER_KEY) {
                ItemStack item = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());

                if (item != null && !item.getType().isAir()) {
                    PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                    if (pdc.getOrDefault(noRemove, PersistentDataType.INTEGER, 0) == 1 || pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
                        e.setCancelled(true);
                    }
                }

                return;
            }
            else if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

            PersistentDataContainer pdc = e.getCurrentItem().getItemMeta().getPersistentDataContainer();

            if (pdc.getOrDefault(noRemove, PersistentDataType.INTEGER, 0) == 1 || pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
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
                            && pdc.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1
                    ) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (SpectatorManager.isSpectating(e.getPlayer()) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof ItemFrame) {
            ItemFrame ife = ((ItemFrame) e.getRightClicked());
            ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());
            if (ife.isEmpty() && item != null) {
                PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();
                if (c.getOrDefault(new NamespacedKey(plugin, "kit-item"), PersistentDataType.INTEGER, 0) == 1) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = ((Player) e.getEntity());

            if (SpectatorManager.isSpectating(p) && p.getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageByPlayer(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = ((Player) e.getDamager());

            if (SpectatorManager.isSpectating(p) && p.getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onManipulate(PlayerArmorStandManipulateEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE && ANNIPlugin.getInstance().getCurrentGame().getState().getId() >= 0) {
            ItemStack pi = e.getPlayerItem();
            if (/*pi != null && */!pi.getType().isAir()) {
                PersistentDataContainer c = pi.getItemMeta().getPersistentDataContainer();

                if (c.getOrDefault(noRemove, PersistentDataType.INTEGER, 0) == 1 || c.getOrDefault(anniKit, PersistentDataType.INTEGER, 0) == 1) {
                    e.setCancelled(true);
                }
            }
        }
    }

}
