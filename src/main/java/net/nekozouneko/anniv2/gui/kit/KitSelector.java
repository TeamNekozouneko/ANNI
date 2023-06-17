package net.nekozouneko.anniv2.gui.kit;

import com.google.common.collect.Lists;
import net.nekozouneko.anniv2.ANNIConfig;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.kit.ANNIKit;
import net.nekozouneko.anniv2.kit.AbsANNIKit;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitSelector extends AbstractGui {

    private final MessageManager mm = plugin.getMessageManager();
    private int page;
    private boolean continu = false;

    public KitSelector(ANNIPlugin plugin, Player player, int page) {
        super(plugin, player);

        List<List<AbsANNIKit>> part = getPartedKits();

        this.page = Math.max(page <= part.size() ? page : 1, 1);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 36,
                    mm.build(
                            "gui.kit_selector.title",
                            String.valueOf(page), String.valueOf(getMaxPage())
                    )
            );
        inventory.clear();

        NamespacedKey pagek = new NamespacedKey(plugin, "page");

        if (page > 1) {
            inventory.setItem(27, ItemStackBuilder.of(Material.ARROW)
                    .name(mm.build("gui.prev_page"))
                    .persistentData(pagek, PersistentDataType.INTEGER, page - 1)
                    .build()
            );
        }
        else {
            inventory.setItem(27, ItemStackBuilder.of(Material.STICK)
                    .name(" ")
                    .build()
            );
        }

        // next page
        if (getMaxPage() > page) {
            inventory.setItem(35, ItemStackBuilder.of(Material.ARROW)
                    .name(mm.build("gui.next_page"))
                    .persistentData(pagek, PersistentDataType.INTEGER, page + 1)
                    .build()
            );
        }
        else {
            inventory.setItem(35, ItemStackBuilder.of(Material.STICK)
                    .name(" ")
                    .build()
            );
        }

        for (int i = 28; i < 35; i++) inventory.setItem(i,
                ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                        .name(" ")
                        .build()
        );

        NamespacedKey kin = new NamespacedKey(plugin, "kit");

        List<List<AbsANNIKit>> part = getPartedKits();

        if (!part.isEmpty()) {
            List<AbsANNIKit> p = part.get(page - 1);
            for (int i = 0;i < p.size() && i < 27; i++) {
                inventory.setItem(i, ItemStackBuilder.of(p.get(i).getIcon())
                        .name("§f" + p.get(i).getName())
                        .lore(p.get(i).getLore())
                        .persistentData(kin, PersistentDataType.STRING, p.get(i).getId())
                        .build()
                );
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        PersistentDataContainer pdc = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        NamespacedKey kin = new NamespacedKey(plugin, "kit");

        if (pdc.has(new NamespacedKey(plugin, "page"), PersistentDataType.INTEGER)) {
            page = pdc.get(new NamespacedKey(plugin, "page"), PersistentDataType.INTEGER);
            continu = true;
            player.closeInventory();
        }
        else if (pdc.has(kin, PersistentDataType.STRING)) {
            AbsANNIKit kit = ANNIKit.getAbsKitOrCustomById(
                    pdc.getOrDefault(kin, PersistentDataType.STRING, "default")
            );

            plugin.getCurrentGame().setKit(player, kit);
            player.closeInventory();
            if (plugin.getCurrentGame().getState().getId() > 0) {
                player.setHealth(0);
            }
            else player.sendMessage(mm.build("gui.kit_selector.using", kit.getName()));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        if (!continu) unregisterAllGuiListeners(((Player) e.getPlayer()));
        else {
            inventory = null;
            continu = false;
            open();
        }
    }

    private int getMaxPage() {
        return getPartedKits().size();
    }

    private List<List<AbsANNIKit>> getPartedKits() {
        List<AbsANNIKit> kits = new ArrayList<>();
        if (ANNIConfig.isEnabledCustomKits()) {
            if (!ANNIConfig.isCustomKitOnly()) {
                kits.addAll(Arrays.stream(ANNIKit.values())
                        .map(ANNIKit::getKit)
                        .collect(Collectors.toList())
                );
            }
            kits.addAll(plugin.getCustomKitManager().getKits());
        }
        else {
            kits.addAll(Arrays.stream(ANNIKit.values())
                    .map(ANNIKit::getKit)
                    .collect(Collectors.toList())
            );
        }

        return Lists.partition(kits, 27);
    }
}
