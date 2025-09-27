package net.nekozouneko.anni.gui.map;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.map.MapManager;
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
        
        var translation = ANNIPlugin.getInstance().getTranslationManager();

        if (inventory == null)
            inventory = Bukkit.createInventory(this, 36,
                translation.component(player, "gui.map_selector.title",
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
            ItemStack random = ItemStack.of(Material.PAPER);
            random.editMeta(meta -> {
                meta.displayName(translation.component(player, "gui.random"));
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "map"), PersistentDataType.STRING, "@random"
                );
            });
            inventory.setItem(31, random);
        }

        if (page < getTotalPageCount()) {
            ItemStack next = ItemStack.of(Material.ARROW);
            next.editMeta(meta -> {
                meta.displayName(translation.component(player, "gui.next_page"));
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "page"),
                        PersistentDataType.INTEGER,
                        page + 1
                );
            });

            inventory.setItem(35, next);
        }
        if (page > 1) {
            ItemStack prev = ItemStack.of(Material.ARROW);
            prev.editMeta(meta -> {
                meta.displayName(translation.component(player, "gui.prev_page"));
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "page"),
                        PersistentDataType.INTEGER,
                        page - 1
                );
            });

            inventory.setItem(27, prev);
        }

        List<List<ANNIMap>> partitions = Lists.partition(new ArrayList<>(mapm.getMaps()), 27);

        if (!partitions.isEmpty()) {
            List<ItemStack> part = partitions.get(page - 1).stream()
                    .map((map) -> {
                        ItemStack item = ItemStack.of(Material.MAP);
                        item.editMeta(meta -> {
                            meta.displayName(Component.text(map.getName()));
                            meta.lore(translation
                                    .componentList(player,
                                            "gui.map_selector.map_lore",
                                            map.getId(),
                                            map.getWorld()
                                    ));
                            meta.getPersistentDataContainer()
                                    .set(new NamespacedKey(plugin, "map"),
                                            PersistentDataType.STRING, map.getId());
                        });
                        return item;
                    })
                    .toList();

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
