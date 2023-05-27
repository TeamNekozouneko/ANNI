package net.nekozouneko.anniv2.gui.map;

import com.google.common.collect.Lists;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.map.MapManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
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
    private boolean rand;

    public MapSelector(ANNIPlugin plugin, Player player, int page, boolean random, Consumer<ANNIMap> onClose) {
        super(plugin, player);

        this.mapm = plugin.getMapManager();
        this.onClose = onClose;
        this.page = page < 1 || getTotalPageCount() < page ? 1 : page;
        this.rand = random;
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
        inventory.clear();

        ItemStack back = ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();

        for (int i = 27; i < 36; i++) inventory.setItem(i, back);

        if (rand) {
            inventory.setItem(31,
                    ItemStackBuilder.of(Material.PAPER)
                            .name(plugin.getMessageManager().build("gui.random"))
                            .persistentData(new NamespacedKey(plugin, "map"), PersistentDataType.STRING, "@random")
                            .build()
            );
        }

        if (page < getTotalPageCount()) {
            ItemStack next = ItemStackBuilder.of(Material.ARROW)
                    .name(plugin.getMessageManager().build("gui.next_page"))
                    .persistentData(
                            new NamespacedKey(plugin, "page"),
                            PersistentDataType.INTEGER,
                            page + 1
                    )
                    .build();

            inventory.setItem(35, next);
        }
        if (page > 1) {
            ItemStack prev = ItemStackBuilder.of(Material.ARROW)
                    .name(plugin.getMessageManager().build("gui.prev_page"))
                    .persistentData(
                            new NamespacedKey(plugin, "page"),
                            PersistentDataType.INTEGER,
                            page - 1
                    )
                    .build();

            inventory.setItem(27, prev);
        }

        List<List<ANNIMap>> partitions = Lists.partition(new ArrayList<>(mapm.getMaps()), 27);

        if (!partitions.isEmpty()) {
            List<ItemStack> part = partitions.get(page - 1).stream()
                    .map((map) -> ItemStackBuilder.of(Material.MAP)
                            .name("§f" + map.getName())
                            .lore(plugin.getMessageManager()
                                    .buildList(
                                            "gui.map_selector.map_lore",
                                            map.getId(),
                                            map.getWorld()
                                    )
                            )
                            .persistentData(
                                    new NamespacedKey(plugin, "map"),
                                    PersistentDataType.STRING, map.getId()
                            )
                            .build()
                    )
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

            if (onClose != null) Bukkit.getScheduler().runTask(plugin, () ->
                    onClose.accept(
                            mid.equals("@random") ? null : mapm.getMap(mid)
                    )
            );
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

        if (!continu) unregisterAllGuiListeners(player);
        else continu = false;
    }

    private int getTotalPageCount() {
        return (int) Math.max((long) Math.ceil(mapm.getMaps().size() / 27d), 1L);
    }

}
