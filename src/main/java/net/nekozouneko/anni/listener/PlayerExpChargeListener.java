package net.nekozouneko.anni.listener;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PlayerExpChargeListener implements Listener {

    @EventHandler
    public void onExpCharge(PlayerExpChangeEvent event) {
        var arena = ANNIPlugin.getInstance().getCurrentGame();

        if (!arena.getState().isInArena()) return;

        if (ANNIKit.getKitById(arena.getKit(event.getPlayer()).getId()) != ANNIKit.ENCHANTER) return;

        event.setAmount(event.getAmount() * 2);
    }
}
