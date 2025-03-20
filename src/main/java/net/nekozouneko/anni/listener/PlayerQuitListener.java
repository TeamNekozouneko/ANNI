package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.gui.AbstractGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ANNIArena arena = ANNIPlugin.getInstance().getCurrentGame();

        arena.leave(e.getPlayer());
        AbstractGui.unregisterAllGuiListeners(e.getPlayer());
        SpectatorManager.remove(e.getPlayer());

        if (arena.getPlayers().isEmpty()) {
            arena.getVoteManager().clear();
        }
    }

}
