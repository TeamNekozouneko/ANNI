package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTask(ANNIPlugin.getInstance(), () ->
                ANNIPlugin.getInstance().getCurrentGame().join(e.getPlayer())
        );
    }

}
