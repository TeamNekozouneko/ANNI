package net.nekozouneko.anniv2.gui.map;

import com.google.common.collect.Lists;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapSelector extends AbstractGui {

    private final MapManager mapm;
    private final Consumer<ANNIMap> onClose;

    private int page;
    private boolean continu;
    private ANNIMap map = null;

    public MapSelector(ANNIPlugin plugin, Player player, int page, Consumer<ANNIMap> onClose) {
        super(plugin, player);

        this.mapm = plugin.getMapManager();
        this.onClose = onClose;
        this.page = page < 1 || getTotalPageCount() < page ? 1 : page;
        continu = false;
    }

    @Override
    public void open() {
        update();
        player.openInventory(inventory);
    }

    @Override
    public void update() {
        page = page < 1 || getTotalPageCount() < page ? 1 : page;

        if (inventory == null)
            inventory = Bukkit.createInventory(this, 36,
                plugin.getMessageManager().build("gui.map_selector.title",
                        Objects.toString(page),
                        Objects.toString(getTotalPageCount())
                )
            );

        ItemStack back = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(" ");
        back.setItemMeta(backMeta);

        for (int i = 27; i < 36; i++) inventory.setItem(i, back);

        if (page < getTotalPageCount()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName(plugin.getMessageManager().build("gui.next_page"));
            nextMeta.getPersistentDataContainer()
                    .set(
                            new NamespacedKey(plugin, "page"),
                            PersistentDataType.INTEGER,
                            page + 1);
            next.setItemMeta(nextMeta);

            inventory.setItem(27, next);
        }
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName(plugin.getMessageManager().build("gui.prev_page"));
            prevMeta.getPersistentDataContainer()
                            .set(
                                    new NamespacedKey(plugin, "page"),
                                    PersistentDataType.INTEGER,
                                    page - 1);
            prev.setItemMeta(prevMeta);
        }

        List<List<ANNIMap>> partitions = Lists.partition(new ArrayList<>(mapm.getMaps()), 27);

        if (!partitions.isEmpty()) {
            List<ItemStack> part = partitions.get(page - 1).stream()
                    .map((map) -> {
                        ItemStack mi = new ItemStack(Material.MAP);
                        ItemMeta mim = mi.getItemMeta();
                        mim.setDisplayName("§f" + map.getName());
                        mim.setLore(plugin.getMessageManager()
                                .buildList(
                                        "gui.map_selector.map_lore",
                                        map.getId(),
                                        map.getWorld()
                                )
                        );
                        mim.getPersistentDataContainer()
                                .set(
                                        new NamespacedKey(plugin, "map"),
                                        PersistentDataType.STRING, map.getId()
                                );

                        mi.setItemMeta(mim);

                        return mi;
                    })
                    .collect(Collectors.toList());

            for (int i = 0; i < part.size() && i < 27; i++) {
                inventory.setItem(i, part.get(i));
            }
        }
    }

    @EventHandler
    @SuppressWarnings({"null", "DataFlowIssue"})
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        ItemStack item = e.getCurrentItem();

        if (item == null) return;

        e.setCancelled(true);

        if (item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "map"), PersistentDataType.STRING
        )) {
            String mid = item.getItemMeta().getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "map"), PersistentDataType.STRING
            );

            map = mapm.getMap(mid);
            player.closeInventory();
        }
        else if (item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "page"), PersistentDataType.INTEGER
        )) {
            page = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "page"), PersistentDataType.INTEGER);
            continu = true;
            player.closeInventory();
            inventory = null;
            open();
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        if (!continu) {
            unregisterAllGuiListeners(player);
            if (onClose != null) onClose.accept(map);
        }
        else continu = false;
    }

    private int getTotalPageCount() {
        return (int) Math.max((long) Math.ceil(mapm.getMaps().size() / 27d), 1L);
    }

}
