package net.nekozouneko.anniv2.gui.shop;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.gui.AbstractGui;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmnUtil;
import net.nekozouneko.anniv2.util.VaultUtil;
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
            inventory = Bukkit.createInventory(this, 54, mm.build("gui.potion_shop.title"));

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build()
            );

        NamespacedKey price = new NamespacedKey(plugin, "price");

        // 基本アイテム
        inventory.setItem(10,
                ItemStackBuilder.of(Material.BREWING_STAND)
                        .persistentData(price, PersistentDataType.DOUBLE, 1500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(11,
                ItemStackBuilder.of(Material.GLASS_BOTTLE)
                        .amount(3)
                        .persistentData(price, PersistentDataType.DOUBLE, 300.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "300", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(12,
                ItemStackBuilder.of(Material.NETHER_WART)
                        .persistentData(price, PersistentDataType.DOUBLE, 700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(13,
                ItemStackBuilder.of(Material.GLOWSTONE_DUST)
                        .persistentData(price, PersistentDataType.DOUBLE, 500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(14,
                ItemStackBuilder.of(Material.REDSTONE)
                        .persistentData(price, PersistentDataType.DOUBLE, 500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(15,
                ItemStackBuilder.of(Material.GUNPOWDER)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(16,
                ItemStackBuilder.of(Material.DRAGON_BREATH)
                        .persistentData(price, PersistentDataType.DOUBLE, 700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );

        // 材料
        inventory.setItem(28,
                ItemStackBuilder.of(Material.FERMENTED_SPIDER_EYE)
                        .persistentData(price, PersistentDataType.DOUBLE, 700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(29,
                ItemStackBuilder.of(Material.BLAZE_POWDER)
                        .persistentData(price, PersistentDataType.DOUBLE, 700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(30,
                ItemStackBuilder.of(Material.SUGAR)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(31,
                ItemStackBuilder.of(Material.RABBIT_FOOT)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(32,
                ItemStackBuilder.of(Material.GLISTERING_MELON_SLICE)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(33,
                ItemStackBuilder.of(Material.SPIDER_EYE)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(34,
                ItemStackBuilder.of(Material.PUFFERFISH)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(37,
                ItemStackBuilder.of(Material.MAGMA_CREAM)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(38,
                ItemStackBuilder.of(Material.GOLDEN_CARROT)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(39,
                ItemStackBuilder.of(Material.GHAST_TEAR)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(40,
                ItemStackBuilder.of(Material.TURTLE_HELMET)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(41,
                ItemStackBuilder.of(Material.PHANTOM_MEMBRANE)
                        .persistentData(price, PersistentDataType.DOUBLE, 600.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "600", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        ItemStack item = e.getCurrentItem();
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey price = new NamespacedKey(plugin, "price");

        if (pdc.has(price, PersistentDataType.DOUBLE)) {
            double val = pdc.get(price, PersistentDataType.DOUBLE);
            if (VaultUtil.getEco().has(player, val)) {
                ItemStack clone = ItemStackBuilder.of(item)
                        .clearPersistentData()
                        .lore(Collections.emptyList())
                        .build();
                VaultUtil.getEco().withdrawPlayer(player, val);
                CmnUtil.giveOrDrop(player, clone);
                player.sendMessage(mm.build("gui.shop.purchased", String.valueOf(val), mm.build("gui.shop.ext")));
            }
            else {
                player.sendMessage(mm.build("gui.shop.more_points",
                        String.format("%,.1f", val - VaultUtil.getEco().getBalance(player)),
                        mm.build("gui.shop.ext")
                ));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) return;

        unregisterAllGuiListeners(((Player) e.getPlayer()));
    }
}
