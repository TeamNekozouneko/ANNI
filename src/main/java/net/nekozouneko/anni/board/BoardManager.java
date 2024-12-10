package net.nekozouneko.anni.board;

import fr.mrmicky.fastboard.FastBoard;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class BoardManager implements Listener {

    public static class ANNIFastBoard extends FastBoard {

        public ANNIFastBoard(Player player) {
            super(player);
        }

        @Override
        public boolean hasLinesMaxLength() {
            return super.hasLinesMaxLength();
        }
    }

    private final ANNIPlugin plugin;

    private final Map<Player, ANNIFastBoard> boards = new HashMap<>();

    public BoardManager(ANNIPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ANNIFastBoard create(Player player) {
        delete(player);
        return boards.put(player, new ANNIFastBoard(player));
    }

    public ANNIFastBoard get(Player player) {
        return boards.getOrDefault(player, new ANNIFastBoard(player));
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
