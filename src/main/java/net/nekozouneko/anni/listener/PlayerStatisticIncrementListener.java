package net.nekozouneko.anni.listener;

import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.game.StatisticChangeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

@RequiredArgsConstructor
public class PlayerStatisticIncrementListener implements Listener {

    private final StatisticChangeService service;

    @EventHandler
    public void onIncrement(PlayerStatisticIncrementEvent event) {
        if (!service.shouldChangeValues()) event.setCancelled(true);
    }
}
