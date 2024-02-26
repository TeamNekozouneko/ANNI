package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() == null && !(e.getEntity().getShooter() instanceof Player)) return;

        Player shooter = (Player) e.getEntity().getShooter();

        if (SpectatorManager.isSpectating(shooter)) e.setCancelled(true);
    }

}
