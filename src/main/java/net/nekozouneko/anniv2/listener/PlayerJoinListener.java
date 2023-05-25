package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler( priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        ANNIPlugin.getInstance().getCurrentGame().join(e.getPlayer());
    }

}
