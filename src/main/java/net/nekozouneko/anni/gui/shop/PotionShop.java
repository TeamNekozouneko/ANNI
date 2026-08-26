package net.nekozouneko.anni.gui.shop;

import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.gui.AbstractGui;
import net.nekozouneko.anni.message.MessageManager;
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

public class PotionShop extends AbstractGui {

    private final MessageManager mm = plugin.getMessageManager();

    public PotionShop(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(
                    this, 54, ANNIPlugin.getInstance().getTranslationManager()
                            .component(player, "gui.potion_shop.title")
            );

        ItemStack background = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
        background.editMeta(meta -> {
            meta.displayName(Component.space());
        });

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, background);

        // 基本アイテム
        inventory.setItem(10, shopItem(Material.BREWING_STAND, 1, 1500));
        inventory.setItem(11, shopItem(Material.GLASS_BOTTLE, 3, 300));
        inventory.setItem(12, shopItem(Material.NETHER_WART, 1, 700));
        inventory.setItem(13, shopItem(Material.GLOWSTONE_DUST, 1, 500));
        inventory.setItem(14, shopItem(Material.REDSTONE, 1, 500));
        inventory.setItem(15, shopItem(Material.GUNPOWDER, 1, 600));
        inventory.setItem(16, shopItem(Material.DRAGON_BREATH, 1, 700));

        // 材料
        inventory.setItem(28, shopItem(Material.FERMENTED_SPIDER_EYE, 1, 700));
        inventory.setItem(29, shopItem(Material.BLAZE_POWDER, 1, 700));
        inventory.setItem(30, shopItem(Material.SUGAR, 1, 600));
        inventory.setItem(31, shopItem(Material.RABBIT_FOOT, 1, 600));
        inventory.setItem(32, shopItem(Material.GLISTERING_MELON_SLICE, 1, 600));
        inventory.setItem(33, shopItem(Material.SPIDER_EYE, 1, 600));
        inventory.setItem(34, shopItem(Material.PUFFERFISH, 1, 600));
        inventory.setItem(37, shopItem(Material.MAGMA_CREAM, 1, 600));
        inventory.setItem(38, shopItem(Material.GOLDEN_CARROT, 1, 600));
        inventory.setItem(39, shopItem(Material.GHAST_TEAR, 1, 600));
        inventory.setItem(40, shopItem(Material.TURTLE_HELMET, 1, 600));
        inventory.setItem(41, shopItem(Material.PHANTOM_MEMBRANE, 1, 600));
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
