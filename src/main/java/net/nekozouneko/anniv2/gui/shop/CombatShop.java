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

public class CombatShop extends AbstractGui {

    private final MessageManager mm = plugin.getMessageManager();

    public CombatShop(ANNIPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void update() {
        if (inventory == null)
            inventory = Bukkit.createInventory(this, 54, mm.build("gui.combat_shop.title"));

        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, ItemStackBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build()
            );

        NamespacedKey price = new NamespacedKey(plugin, "price");

        // 鉄装備
        inventory.setItem(10,
                ItemStackBuilder.of(Material.IRON_HELMET)
                        .persistentData(price, PersistentDataType.DOUBLE, 1200.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1200", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(19,
                ItemStackBuilder.of(Material.IRON_CHESTPLATE)
                        .persistentData(price, PersistentDataType.DOUBLE, 1700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(28,
                ItemStackBuilder.of(Material.IRON_LEGGINGS)
                        .persistentData(price, PersistentDataType.DOUBLE, 1500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(37,
                ItemStackBuilder.of(Material.IRON_BOOTS)
                        .persistentData(price, PersistentDataType.DOUBLE, 1200.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1200", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );

        // 武器/ツール
        inventory.setItem(12,
                ItemStackBuilder.of(Material.IRON_SWORD)
                        .persistentData(price, PersistentDataType.DOUBLE, 1500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(13,
                ItemStackBuilder.of(Material.BOW)
                        .persistentData(price, PersistentDataType.DOUBLE, 1700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(21,
                ItemStackBuilder.of(Material.IRON_PICKAXE)
                        .persistentData(price, PersistentDataType.DOUBLE, 1200.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1200", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(22,
                ItemStackBuilder.of(Material.CROSSBOW)
                        .persistentData(price, PersistentDataType.DOUBLE, 1700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );

        inventory.setItem(39,
                ItemStackBuilder.of(Material.SHIELD)
                        .persistentData(price, PersistentDataType.DOUBLE, 1500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(40,
                ItemStackBuilder.of(Material.ARROW)
                        .amount(16)
                        .persistentData(price, PersistentDataType.DOUBLE, 800.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "800", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );

        // 食料
        inventory.setItem(15,
                ItemStackBuilder.of(Material.COOKED_BEEF)
                        .amount(16)
                        .persistentData(price, PersistentDataType.DOUBLE, 500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "500", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(16,
                ItemStackBuilder.of(Material.CAKE)
                        .persistentData(price, PersistentDataType.DOUBLE, 700.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "700", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(24,
                ItemStackBuilder.of(Material.BREAD)
                        .amount(16)
                        .persistentData(price, PersistentDataType.DOUBLE, 300.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "300", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(25,
                ItemStackBuilder.of(Material.MILK_BUCKET)
                        .persistentData(price, PersistentDataType.DOUBLE, 800.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "800", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );

        // その他
        inventory.setItem(42,
                ItemStackBuilder.of(Material.FISHING_ROD)
                        .persistentData(price, PersistentDataType.DOUBLE, 1200.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1200", mm.build("gui.shop.ext"))
                        ))
                        .build()
        );
        inventory.setItem(43,
                ItemStackBuilder.of(Material.ENDER_PEARL)
                        .amount(4)
                        .persistentData(price, PersistentDataType.DOUBLE, 1500.)
                        .lore(Collections.singletonList(
                                mm.build("gui.shop.price", "1500", mm.build("gui.shop.ext"))
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
