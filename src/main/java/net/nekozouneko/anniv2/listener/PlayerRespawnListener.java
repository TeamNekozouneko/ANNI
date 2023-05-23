package net.nekozouneko.anniv2.listener;

import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.ANNIArena;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.kit.ANNIKit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerRespawnListener implements Listener {
    
    private final ANNIPlugin plugin = ANNIPlugin.getInstance();
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        ANNIArena current = plugin.getCurrentGame();
        
        if (current.getState().getId() > 0 && current.isJoined(e.getPlayer())) {
            ANNITeam at = current.getTeamByPlayer(e.getPlayer());

            if (at != null) {
                if (current.isNexusLost(at)) {
                    e.getPlayer().getInventory().clear();
                    e.getPlayer().getEnderChest().clear();
                    e.getPlayer().setLevel(0);
                    e.getPlayer().setExp(0);

                    if (plugin.getLobby() != null)
                        e.setRespawnLocation(plugin.getLobby());
                }
                else e.setRespawnLocation(current.getMap().getSpawnOrDefault(at).toLocation(current.getCopyWorld()));
            }
            else e.setRespawnLocation(current.getMap().getDefaultSpawn().toLocation(current.getCopyWorld()));

            // リスキル対策で 耐性4 (80%カット) と スピード 1 をリスポーン時付与
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 4, false, false, true));
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false, true));

            e.getPlayer().setGameMode(GameMode.SURVIVAL);
            e.getPlayer().getInventory().setContents(
                    current.getTeamByPlayer(e.getPlayer()) != null ?
                        ANNIKit.teamColor(current.getKit(e.getPlayer()), current.getTeamByPlayer(e.getPlayer()))
                        : current.getKit(e.getPlayer()).getKitContents()
            );
        }
        else {
            if (plugin.getLobby() != null) e.setRespawnLocation(plugin.getLobby());
        }
    }
}
