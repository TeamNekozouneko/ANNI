package net.nekozouneko.anni.kit.items;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.task.CooldownManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FlyingBook implements Listener {

    public static ItemStackBuilder getBuilder() {
        return ItemStackBuilder.of(Material.BOOK)
                .name(ANNIPlugin.getInstance().getMessageManager().build("item.flying_book.name"))
                .lore(ANNIPlugin.getInstance().getMessageManager().buildList("item.flying_book.lore"))
                .enchant(Enchantment.DURABILITY, 1, false)
                .itemFlags(ItemFlag.HIDE_ENCHANTS)
                .persistentData(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "flying-book");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        if (event.getItem() == null || event.getItem().getType().isAir()) return;

        if (!isFlyingBook(event.getItem())) return;

        event.setCancelled(true);

        CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();

        if (!cm.isCooldownEnd(event.getPlayer().getUniqueId(), CooldownManager.Type.FLYING_BOOK)) {
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(
                            ANNIPlugin.getInstance().getMessageManager().build(
                                    "command.err.cooldown",
                                    cm.getTimeLeftFormatted(event.getPlayer().getUniqueId(), CooldownManager.Type.GRAPPLING_HOOK)

                            )
                    )
            );
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
            return;
        }

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, 4, false, true, true));
        cm.set(event.getPlayer().getUniqueId(), CooldownManager.Type.FLYING_BOOK, 60000);
    }

    public static boolean isFlyingBook(ItemStack item) {
        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();
        return c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "").equals("flying-book");
    }

}
