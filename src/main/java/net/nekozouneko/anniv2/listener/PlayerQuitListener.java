package net.nekozouneko.anniv2.listener;

import fr.mrmicky.fastboard.FastBoard;

import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ANNIPlugin.getInstance().getArenaManager().leave(e.getPlayer());
    }

}
