package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.message.MessageManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public class PlayerDamageListener implements Listener {

    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    private final MessageManager mm = plugin.getMessageManager();

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = ((Player) e.getEntity());

            if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 2);
                p.sendMessage(mm.build("notify.removed_invisibility"));
            }


        }
    }

}
