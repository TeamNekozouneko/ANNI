package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerPortalListener implements Listener {

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (ANNIPlugin.getInstance().getCurrentGame().getState().isInArena()) {
            event.setCancelled(true);
        }
    }
}
