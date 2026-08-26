package net.nekozouneko.anni.gui.kit;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.Kit;
import net.nekozouneko.anni.util.CmnUtil;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitSelector extends AbstractGui {

    private int page;
    private boolean continu = false;

    public KitSelector(ANNIPlugin plugin, Player player, int page) {
        super(plugin, player);

        List<List<Kit>> part = getPartedKits();

        this.page = Math.max(page <= part.size() ? page : 1, 1);
    }

    @Override
    public void update() {
        var translation = ANNIPlugin.getInstance().getTranslationManager();
        
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 36,
                    translation.component(player, 
                            "gui.kit_selector.title",
                            String.valueOf(page), String.valueOf(getMaxPage())
                    )
            );
        inventory.clear();

        NamespacedKey pageKey = new NamespacedKey(plugin, "page");

        ItemStack disabled = ItemStack.of(Material.STICK);
        disabled.editMeta(meta -> meta.displayName(Component.space()));

        if (page > 1) {
            ItemStack prev = ItemStack.of(Material.ARROW);
            prev.editMeta(meta -> meta.displayName(translation.component(player, "gui.prev_page")));
            CmnUtil.editPDC(prev, c -> c.set(pageKey, PersistentDataType.INTEGER, page - 1));

            inventory.setItem(27, prev);
        }
        else inventory.setItem(27, disabled);

        // next page
        if (getMaxPage() > page) {
            ItemStack next = ItemStack.of(Material.ARROW);
            next.editMeta(meta -> meta.displayName(translation.component(player, "gui.next_page")));
            CmnUtil.editPDC(next, c -> c.set(pageKey, PersistentDataType.INTEGER, page + 1));

            inventory.setItem(35, next);
        }
        else inventory.setItem(35, disabled);

        ItemStack background = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        background.editMeta(meta -> meta.displayName(Component.space()));

        for (int i = 28; i < 35; i++) inventory.setItem(i, background);

        NamespacedKey kin = new NamespacedKey(plugin, "kit");

        List<List<Kit>> part = getPartedKits();

        if (!part.isEmpty()) {
            List<Kit> p = part.get(page - 1);
            for (int i = 0;i < p.size() && i < 27; i++) {
                Kit kit = p.get(i);
                ItemStack kitButton = new ItemStack(p.get(i).getIcon());

                kitButton.editMeta(meta -> {
                    meta.customName(kit.getName(player));
                    meta.lore(kit.getLore(player));
                    meta.getPersistentDataContainer().set(kin, PersistentDataType.STRING, kit.getId());
                });
                inventory.setItem(i, kitButton);
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
            Kit kit = ANNIKit.getAbsKitOrCustomById(
                    pdc.getOrDefault(kin, PersistentDataType.STRING, "default")
            );

            plugin.getCurrentGame().setKit(player, kit);
            player.closeInventory();
            if (plugin.getCurrentGame().getState().getId() > 0) {
                player.setHealth(0);
            }
            else player.sendMessage(ANNIPlugin.getInstance().getTranslationManager().component(player, "gui.kit_selector.using", kit.getName()));
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

    private List<List<Kit>> getPartedKits() {
        List<Kit> kits = new ArrayList<>();
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
