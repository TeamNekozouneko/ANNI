package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    
    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        ANNIArena current = plugin.getCurrentGame();
        
        if (current.getState().getId() > 0 && current.isJoined(e.getPlayer())) {
            ANNITeam at = current.getTeamByPlayer(e.getPlayer());

            if (at != null) {
                if (current.isNexusLost(at)) {
                    if (plugin.getLobby() != null) e.setRespawnLocation(plugin.getLobby());
                }
                else e.setRespawnLocation(current.getMap().getSpawnOrDefault(at).toLocation(current.getCopyWorld()));
            }
            else e.setRespawnLocation(current.getMap().getDefaultSpawn().toLocation(current.getCopyWorld()));
        }
        else {
            if (plugin.getLobby() != null) e.setRespawnLocation(plugin.getLobby());
        }
    }
}
