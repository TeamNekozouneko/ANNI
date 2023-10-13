package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.gui.AbstractGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ANNIPlugin.getInstance().getCurrentGame().leave(e.getPlayer());
        AbstractGui.unregisterAllGuiListeners(e.getPlayer());
        SpectatorManager.remove(e.getPlayer());
    }

}
