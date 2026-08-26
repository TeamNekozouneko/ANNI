package net.nekozouneko.anni.listener;

import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.game.PlayerExpChargeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

@RequiredArgsConstructor
public class PlayerExpChargeListener implements Listener {

    private final PlayerExpChargeService service;

    @EventHandler
    public void onExpCharge(PlayerExpChangeEvent event) {
        event.setAmount(service.getChargeAmount(event.getPlayer(), event.getAmount()));
    }
}
