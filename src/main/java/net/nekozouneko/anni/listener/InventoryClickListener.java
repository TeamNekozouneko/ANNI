package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class InventoryClickListener implements Listener {

    private static final List<ClickType> dropTypes = Arrays.asList(
            ClickType.DROP, ClickType.CONTROL_DROP,
            ClickType.WINDOW_BORDER_LEFT, ClickType.WINDOW_BORDER_RIGHT
    );
    private static final List<InventoryType> whitelistTypes = Arrays.asList(
            InventoryType.CREATIVE, InventoryType.PLAYER,
            InventoryType.CRAFTING
    );
    private static final List<ClickType> swapTypes = Arrays.asList(
            ClickType.NUMBER_KEY,
            ClickType.SWAP_OFFHAND
    );

    private final NamespacedKey noRemove = new NamespacedKey(ANNIPlugin.getInstance(), "no-remove");
    private final NamespacedKey kitItem = new NamespacedKey(ANNIPlugin.getInstance(), "kit-item");

    /*@EventHandler
    public void onClick(InventoryClickEvent e) {
        ANNIPlugin plugin = ANNIPlugin.getInstance();

        if (SpectatorManager.isSpectating((Player) e.getWhoClicked()) && e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            if (e.getInventory().getType() != InventoryType.PLAYER) {
                e.setCancelled(true);
                return;
            }
        }

        if (plugin.getCurrentGame().getState().isInArena()) {
            /*if (dropTypes.contains(e.getClick())) {
                if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
                if (CmnUtil.hasPersistentContainer(e.getCurrentItem()) && isTrue(
                        e.getCurrentItem().getItemMeta().getPersistentDataContainer(), noRemove
                )) {
                    e.setCancelled(true);

                }
                return;
            }/

            if (e.getCurrentItem() != null) {
                Bukkit.broadcastMessage("Current: " + e.getCurrentItem());
            }
            if (e.getCursor() != null) {
                Bukkit.broadcastMessage("Cursor: " + e.getCursor());
            }

            if (whitelistTypes.contains(e.getInventory().getType())) {
                if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
                if (CmnUtil.hasPersistentContainer(e.getCurrentItem()) && isTrue(
                        e.getCurrentItem().getItemMeta().getPersistentDataContainer(), kitItem
                ))
                    e.setCancelled(true);
                return;
            }

            if (e.getClick() == ClickType.NUMBER_KEY) {
                ItemStack item = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());

                if (item != null && !item.getType().isAir()) {
                    PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                    if (isTrue(pdc, noRemove) || isTrue(pdc, kitItem)) {
                        e.setCancelled(true);
                    }
                }

                return;
            }
            else if (CmnUtil.hasPersistentContainer(e.getCurrentItem())) return;

            PersistentDataContainer pdc = e.getCurrentItem().getItemMeta().getPersistentDataContainer();

            if (isTrue(pdc, noRemove) || isTrue(pdc, kitItem)) {
                if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE && e.getInventory() != null) {
                    if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
                        /*
                        e.setCurrentItem(null);
                        ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        return;
                         /
                    }
                    else if (
                            !(e.getInventory().getType() == InventoryType.CRAFTING ||
                                    e.getInventory().getType() == InventoryType.PLAYER ||
                                    e.getInventory().getType() == InventoryType.CREATIVE
                            )
                                    && pdc.getOrDefault(kitItem, PersistentDataType.INTEGER, 0) == 1
                    ) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }*/

    @EventHandler
    public void onClick2(InventoryClickEvent e) {
        ANNIPlugin plugin = ANNIPlugin.getInstance();

        if (SpectatorManager.isSpectating((Player) e.getWhoClicked()) && e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            if (e.getInventory().getType() != InventoryType.PLAYER) {
                e.setCancelled(true);
                return;
            }
        }

        if (plugin.getCurrentGame().getState().isInArena()) {
            // getCurrentItem等だけで入手できない用
            if (swapTypes.contains(e.getClick())) {
                ItemStack pos1;
                ItemStack item = e.getCurrentItem();

                switch (e.getClick()) {
                    case NUMBER_KEY: {
                        pos1 = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
                        break;
                    }
                    case SWAP_OFFHAND: {
                        pos1 = e.getWhoClicked().getInventory().getItemInOffHand();
                        break;
                    }
                    default: return;
                }

                boolean checkPos1 = false;
                boolean checkItem = false;

                if (CmnUtil.hasPersistentContainer(pos1)) {
                    PersistentDataContainer pos1pdc = pos1.getItemMeta().getPersistentDataContainer();
                    checkPos1 = isTrue(pos1pdc, kitItem) || isTrue(pos1pdc, noRemove);
                }

                if (CmnUtil.hasPersistentContainer(item)) {
                    PersistentDataContainer itemPdc = item.getItemMeta().getPersistentDataContainer();
                    checkItem = isTrue(itemPdc, kitItem) || isTrue(itemPdc, noRemove);
                }

                if (checkPos1 || checkItem) e.setCancelled(true);

                return;
            }

            if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

            PersistentDataContainer c = e.getCurrentItem().getItemMeta().getPersistentDataContainer();

            if (isTrue(c, noRemove) || isTrue(c, kitItem)) {
                if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE && e.getInventory() != null) {
                    if (dropTypes.contains(e.getClick())) {
                        e.setCurrentItem(null);
                        ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                        return;
                    }
                    else if (
                            !(whitelistTypes.contains(e.getInventory().getType())) && isTrue(c, kitItem)
                    ) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    private static boolean isTrue(PersistentDataContainer c, NamespacedKey key) {
        return ((Integer) 1).equals(c.get(key, PersistentDataType.INTEGER));
    }

}
