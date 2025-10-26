package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class PlayerItemDamageListener implements Listener {

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        var arena = ANNIPlugin.getInstance().getCurrentGame();

        if (!arena.getState().isInArena()) return;

        if (ANNIKit.getKitById(arena.getKit(event.getPlayer()).getId()) != ANNIKit.ENCHANTER) return;

        int used = event.getPlayer().getTotalExperience() - event.getDamage() * 2;
        int damage;

        if (used < 0) {
            damage = (int) Math.ceil((double) used / -2);
            used = event.getPlayer().getTotalExperience() - damage * 2;
        }
        else damage = 0;

        event.getPlayer().setTotalExperience(used);
        if (damage != 0) event.setDamage(damage);
        else event.setCancelled(true);
    }
}
