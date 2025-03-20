package net.nekozouneko.anni.gui.kit;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.kit.custom.CustomKit;
import net.nekozouneko.anni.util.FileUtil;
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

import java.io.*;
import java.util.Arrays;

public class CustomKitEditor extends AbstractGui {

    private final CustomKit kit;

    public CustomKitEditor(ANNIPlugin plugin, Player player, CustomKit ckit) {
        super(plugin, player);

        this.kit = ckit;
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 54,
                    ANNIPlugin.getInstance().getMessageManager().build("gui.customkit_editor.title")
            );

        ItemStack[] arr = Arrays.copyOf(kit.getKitContents(), 41);

        int p1 = 0;
        for (int i = 9; i < 36; i++) {
            inventory.setItem(p1, arr[i]);
            p1++;
        }

        int p2 = 27;
        for (int i = 0; i < 9; i++) {
            inventory.setItem(p2, arr[i]);
            p2++;
        }

        for (int i = 36; i < 45; i++)
            inventory.setItem(i, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .persistentData(new NamespacedKey(plugin, "doNotClick"), PersistentDataType.INTEGER, 1)
                    .build()
            );

        inventory.setItem(45, arr[39]);
        inventory.setItem(46, arr[38]);
        inventory.setItem(47, arr[37]);
        inventory.setItem(48, arr[36]);
        inventory.setItem(49, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .persistentData(new NamespacedKey(plugin, "doNotClick"), PersistentDataType.INTEGER, 1)
                .build()
        );
        inventory.setItem(50, arr[40]);
        inventory.setItem(51, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .persistentData(new NamespacedKey(plugin, "doNotClick"), PersistentDataType.INTEGER, 1)
                .build()
        );
        inventory.setItem(52, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .persistentData(new NamespacedKey(plugin, "doNotClick"), PersistentDataType.INTEGER, 1)
                .build()
        );
        inventory.setItem(53, ItemStackBuilder.of(kit.getIcon()).build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        if (e.getCurrentItem() != null && !e.getCurrentItem().getType().isAir()) {
            if (e.getCurrentItem().getItemMeta().getPersistentDataContainer()
                    .getOrDefault(new NamespacedKey(plugin, "doNotClick"), PersistentDataType.INTEGER, 0) == 1
            ) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        ItemStack[] arr = new ItemStack[42];

        int p1 = 0;
        for (int i = 9; i < 36; i++) {
            arr[i] = e.getInventory().getItem(p1);
            p1++;
        }

        int p2 = 27;
        for (int i = 0; i < 9; i++) {
            arr[i] = e.getInventory().getItem(p2);
            p2++;
        }

        arr[36] = e.getInventory().getItem(48);
        arr[37] = e.getInventory().getItem(47);
        arr[38] = e.getInventory().getItem(46);
        arr[39] = e.getInventory().getItem(45);
        arr[40] = e.getInventory().getItem(50);

        kit.setKitContents(arr);
        kit.setIcon(e.getInventory().getItem(53) != null ? e.getInventory().getItem(53).getType() : Material.CHEST);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        new File(plugin.getDefaultKitsDir(), kit.getId() + ".json")
                ))
        )) {
            FileUtil.createGson().toJson(kit, CustomKit.class, writer);
            writer.flush();
            plugin.getCustomKitManager().reload();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }

        unregisterAllGuiListeners(player);
    }
}
