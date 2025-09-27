package net.nekozouneko.anni.gui.shop;

import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.VaultUtil;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
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

import java.util.Collections;

public class CombatShop extends AbstractGui {

    public CombatShop(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(
                    this, 54, ANNIPlugin.getInstance().getTranslationManager()
                            .component(player, "gui.combat_shop.title")
            );

        ItemStack background = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        background.editMeta(meta -> {
            meta.displayName(Component.space());
        });

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, background);

        // 鉄装備
        inventory.setItem(10, shopItem(Material.IRON_HELMET, 1, 1200));
        inventory.setItem(19, shopItem(Material.IRON_CHESTPLATE, 1, 1700));
        inventory.setItem(28, shopItem(Material.IRON_LEGGINGS, 1, 1500));
        inventory.setItem(37, shopItem(Material.IRON_LEGGINGS, 1, 1200));

        // 武器/ツール
        inventory.setItem(12, shopItem(Material.IRON_SWORD, 1, 1500));
        inventory.setItem(13, shopItem(Material.BOW, 1, 1700));
        inventory.setItem(21, shopItem(Material.IRON_PICKAXE, 1, 1200));
        inventory.setItem(22, shopItem(Material.CROSSBOW, 1, 1700));

        inventory.setItem(39, shopItem(Material.SHIELD, 1, 1500));
        inventory.setItem(40, shopItem(Material.ARROW, 1, 800));

        // 食料
        inventory.setItem(15, shopItem(Material.COOKED_BEEF, 16, 500));
        inventory.setItem(16, shopItem(Material.CAKE, 1, 700));
        inventory.setItem(24, shopItem(Material.BREAD, 16, 500));
        inventory.setItem(25, shopItem(Material.MILK_BUCKET, 1, 800));

        // その他
        inventory.setItem(42, shopItem(Material.FISHING_ROD, 1, 1200));
        inventory.setItem(43, shopItem(Material.ENDER_PEARL, 2, 1500));
        inventory.setItem(34, shopItem(Material.EXPERIENCE_BOTTLE, 4, 700));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        ItemStack item = e.getCurrentItem();
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        var price = new NamespacedKey(plugin, "price");
        var translation = ANNIPlugin.getInstance().getTranslationManager();

        if (pdc.has(price, PersistentDataType.DOUBLE)) {
            double val = pdc.get(price, PersistentDataType.DOUBLE);
            if (VaultUtil.getEco().has(player, val)) {
                ItemStack clone = ItemStackBuilder.of(item)
                        .clearPersistentData()
                        .lore(Collections.emptyList())
                        .build();
                VaultUtil.getEco().withdrawPlayer(player, val);
                CmnUtil.giveOrDrop(player, clone);
                player.sendMessage(translation.component(player, "gui.shop.purchased", VaultUtil.getEco().format(val)));
            }
            else {
                player.sendMessage(translation.component(player, "gui.shop.more_points",
                        VaultUtil.getEco().format(val - VaultUtil.getEco().getBalance(player))
                ));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(((Player) e.getPlayer()));
    }

    private ItemStack shopItem(Material material, int amount, double price) {
        ItemStack item = ItemStack.of(material);
        item.setAmount(amount);

        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(ANNIPlugin.getInstance(), "price"),
                    PersistentDataType.DOUBLE, price
            );

            meta.lore(Collections.singletonList(
                    ANNIPlugin.getInstance().getTranslationManager().component(
                            player, "gui.shop.price",
                            VaultUtil.getEco().format(price)
                    )
            ));
        });

        return item;
    }

}
