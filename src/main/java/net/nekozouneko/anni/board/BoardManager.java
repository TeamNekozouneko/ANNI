package net.nekozouneko.anni.board;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class BoardManager implements Listener {

    private final Map<Player, FastBoard> boards = new HashMap<>();

    public BoardManager(ANNIPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public FastBoard create(Player player) {
        delete(player);
        return boards.put(player, new FastBoard(player));
    }

    public FastBoard get(Player player) {
        return boards.getOrDefault(player, new FastBoard(player));
    }

    public void delete(Player player) {
        FastBoard fb = boards.remove(player);
        if (fb != null && !fb.isDeleted()) {
            fb.delete();
        }
    }

    public boolean has(Player player) {
        return boards.get(player) != null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        delete(e.getPlayer());
    }
}
