package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.spectator.SpectatorManager;
import net.nekozouneko.anniv2.message.MessageManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PlayerDamageListener implements Listener {

    private static final Map<UUID, Long> fighting = new HashMap<>();

    public static boolean isFighting(Player player) {
        new HashSet<>(fighting.keySet()).forEach(key -> {
            if (fighting.getOrDefault(key, 0L) <= System.currentTimeMillis())
                fighting.remove(key);
        });

        return !(fighting.getOrDefault(player.getUniqueId(), 0L) <= System.currentTimeMillis());
    }

    public static void setNotFighting(Player player) {
        fighting.remove(player.getUniqueId());
    }

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = ((Player) e.getEntity());

            if (SpectatorManager.isSpectating(p)) {
                e.setCancelled(true);
                return;
            }

            if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                p.sendMessage(mm.build("notify.removed_invisibility"));
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            fighting.put(e.getEntity().getUniqueId(), System.currentTimeMillis() + 10000);
        }
    }

}
