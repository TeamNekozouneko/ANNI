package net.nekozouneko.anniv2.arena;

import fr.mrmicky.fastboard.FastBoard;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.message.ANNIMessage;
import net.nekozouneko.anniv2.util.CmnUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class ANNIArena extends BukkitRunnable {

    private static final Random rand = new Random();

    private final ANNIPlugin plugin;
    private final String id;
    private final Set<Player> players = new HashSet<>();

    private ArenaState state = ArenaState.WAITING;
    private boolean a = false;

    public ANNIArena(ANNIPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void join(Player player) {
        plugin.getArenaManager().leave(player);

        players.add(player);
        player.setScoreboard(plugin.getServer().getScoreboardManager().getNewScoreboard());
    }

    public void leave(Player player) {
        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        players.remove(player);
    }

    public boolean setTeam() {
        return true;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    @Override
    public void run() {
        updateScoreboard();
    }

    public void updateScoreboard() {
        ANNIMessage mm = plugin.getMessageManager();
        for (Player p : players) {
            FastBoard b = plugin.getBoardManager().get(p);

            b.updateTitle(mm.build("scoreboard.title"));
            b.updateLines(
                    mm.buildList("scoreboard.waiting",
                            CmnUtil.getFormattedDateNow(
                                    mm.build("scoreboard.dateformat")
                            ),
                            mm.build("scoreboard.waiting.2.more_player", 3 + ""),
                            Objects.toString(players.size()),
                            mm.build("scoreboard.waiting.5.map_vote")
                    )
            );

            a = !a;
        }
    }
}
