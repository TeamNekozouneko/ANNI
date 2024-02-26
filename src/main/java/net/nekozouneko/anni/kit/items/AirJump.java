package net.nekozouneko.anni.kit.items;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.task.CooldownManager;
import net.nekozouneko.commons.spigot.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AirJump implements Listener {

    public static final long DEFAULT_COOLTIME = 10000;

    public static ItemStackBuilder builder() {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();
        return ItemStackBuilder.of(Material.FEATHER)
                .name(mm.build("item.airjump.name"))
                .lore(mm.buildList("item.airjump.lore"))
                .persistentData(
                        new NamespacedKey(ANNIPlugin.getInstance(), "special-item"),
                        PersistentDataType.STRING, "air-jump"
                );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null) {
            PersistentDataContainer c = e.getItem().getItemMeta().getPersistentDataContainer();
            if (
                    c.getOrDefault(new NamespacedKey(ANNIPlugin.getInstance(), "special-item"), PersistentDataType.STRING, "")
                            .equals("air-jump")
            ) {
                e.setCancelled(true);

                CooldownManager cm = ANNIPlugin.getInstance().getCooldownManager();

                if (cm.isCooldownEnd(e.getPlayer().getUniqueId(), CooldownManager.Type.AIR_JUMP)) {
                    cm.set(e.getPlayer().getUniqueId(), CooldownManager.Type.AIR_JUMP, DEFAULT_COOLTIME);
                    e.getPlayer().getWorld().playSound(
                            e.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 2
                    );
                    e.getPlayer().getWorld().spawnParticle(
                            Particle.CLOUD, e.getPlayer().getLocation(), 50, .5, .5, .5, .1
                    );
                    e.getPlayer().getWorld().spawnParticle(
                            Particle.SMOKE_NORMAL, e.getPlayer().getLocation(), 50, .5, .5, .5, .1
                    );
                    e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().setY(1));
                }
                else {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ANNIPlugin.getInstance().getMessageManager()
                                    .build("actionbar.cooldown_stats", cm.getTimeLeftFormatted(e.getPlayer().getUniqueId(), CooldownManager.Type.AIR_JUMP))
                            )
                    );
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                }
            }
        }
    }

}
