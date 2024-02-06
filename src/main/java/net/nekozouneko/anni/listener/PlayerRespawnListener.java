package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.items.DefenseArtifact;
import org.bukkit.Bukkit;
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
                e.setRespawnLocation(current.getMap().getSpawnOrDefault(at).toLocation(current.getCopyWorld()));

                if (current.isNexusLost(at)) {
                    SpectatorManager.add(e.getPlayer());
                    return;
                }
            }
            else {
                if (plugin.getLobby() != null) e.setRespawnLocation(plugin.getLobby());
                return;
            }

            // リスキル対策で 耐性255 と スピード 1 をリスポーン時付与
            Bukkit.getScheduler().runTask(plugin, () -> {
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 255, false, false, true));
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false, true));
                    }
            );

            DefenseArtifact.addCooldown(e.getPlayer().getUniqueId(), 30000);

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
