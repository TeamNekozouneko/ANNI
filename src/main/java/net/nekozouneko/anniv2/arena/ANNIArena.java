package net.nekozouneko.anniv2.arena;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.sk89q.worldedit.math.BlockVector3;
import fr.mrmicky.fastboard.FastBoard;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.board.BoardManager;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.ANNIMessage;
import net.nekozouneko.anniv2.util.CmnUtil;
import net.nekozouneko.anniv2.util.FileUtil;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.text.SimpleDateFormat;
import java.util.*;

public class ANNIArena extends BukkitRunnable {

    private static final Random rand = new Random();

    private final ANNIPlugin plugin;
    private final String id;

    private final Set<Player> players = new HashSet<>();

    private final BiMap<ANNITeam, Team> teams = HashBiMap.create(4);
    private final Map<ANNITeam, Boolean> enabledTeams = new EnumMap<>(ANNITeam.class);

    private ArenaState state = ArenaState.WAITING;

    private ANNIMap map = null;
    private World copy = null;

    private boolean enableTimer = false;
    private long timer = 0;

    private final Map<ANNITeam, Integer> nexus = new HashMap<>(4);

    private final KeyedBossBar bb;

    public ANNIArena(ANNIPlugin plugin, String id) {
        Preconditions.checkArgument(plugin != null, "Argument 'plugin' cannot be null.");
        Preconditions.checkArgument(id.length() < 9, "Id length limit is 8! (" + id.length() + ")");

        this.plugin = plugin;
        this.id = id;

        this.bb = Bukkit.createBossBar(
                new NamespacedKey(plugin, id),
                "",
                BarColor.BLUE,
                BarStyle.SOLID
        );

        createTeams();
    }

    public String getId() {
        return id;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public void join(Player player) {
        plugin.getArenaManager().leave(player);

        players.add(player);
        player.setScoreboard(plugin.getServer().getScoreboardManager().getNewScoreboard());
    }

    public void leave(Player player) {
        players.remove(player);

        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        bb.removePlayer(player);
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    // Team

    private void createTeams() {
        final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        final ANNIMessage mm = plugin.getMessageManager();

        Team r = sb.registerNewTeam(id + "-red");
        Team b = sb.registerNewTeam(id + "-blue");
        Team g = sb.registerNewTeam(id + "-green");
        Team y = sb.registerNewTeam(id + "-yellow");

        r.setDisplayName(mm.build("team.red.display"));
        r.setPrefix("team.red.prefix");
        r.setColor(ChatColor.RED);
        r.setAllowFriendlyFire(false);
        r.setCanSeeFriendlyInvisibles(true);

        b.setDisplayName(mm.build("team.blue.display"));
        b.setPrefix("team.blue.prefix");
        b.setColor(ChatColor.BLUE);
        b.setAllowFriendlyFire(false);
        b.setCanSeeFriendlyInvisibles(true);

        g.setDisplayName(mm.build("team.green.display"));
        g.setPrefix(mm.build("team.green.prefix"));
        g.setColor(ChatColor.GREEN);
        g.setAllowFriendlyFire(false);
        g.setCanSeeFriendlyInvisibles(true);

        y.setDisplayName(mm.build("team.yellow.display"));
        y.setPrefix(mm.build("team.yellow.prefix"));
        y.setColor(ChatColor.YELLOW);
        y.setAllowFriendlyFire(false);
        y.setCanSeeFriendlyInvisibles(true);

        teams.put(ANNITeam.RED, r);
        teams.put(ANNITeam.BLUE, b);
        teams.put(ANNITeam.GREEN, g);
        teams.put(ANNITeam.YELLOW, y);
        teams.keySet().forEach((t) -> enabledTeams.put(t, true));
    }

    private void deleteTeams() {
        teams.values().forEach(Team::unregister);
        teams.values().clear();
    }

    public void enableTeam(ANNITeam team) {
        enabledTeams.put(team, true);
    }

    public void disableTeam(ANNITeam team) {
        enabledTeams.put(team, false);
    }

    public boolean isEnabledTeam(ANNITeam team) {
        return enabledTeams.getOrDefault(team, true);
    }

    public Map<ANNITeam, Boolean> getEnabledTeams() {
        return Collections.unmodifiableMap(enabledTeams);
    }

    public boolean setTeam() {
        return true;
    }

    public Team getTeam(ANNITeam team) {
        return teams.get(team);
    }

    public ANNITeam getTeam(Team team) {
        return teams.inverse().get(team);
    }

    public BiMap<ANNITeam, Team> getTeams() {
        BiMap<ANNITeam, Team> res = EnumHashBiMap.create(ANNITeam.class);

        teams.entrySet().stream()
                .filter(t -> enabledTeams.getOrDefault(t.getKey(), true))
                .forEach(e -> res.put(e.getKey(), e.getValue()));

        return res;
    }

    public Set<Player> getTeamPlayers(ANNITeam team) {
        Set<Player> ps = new HashSet<>();

        teams.get(team).getPlayers().stream()
                .filter(OfflinePlayer::isOnline)
                .forEach((off) -> {
                    Player p = Bukkit.getPlayer(off.getUniqueId());
                    if (p != null && players.contains(p)) ps.add(p);
                });

        return ps;
    }

    // Timer

    public boolean isEnabledTimer() {
        return enableTimer;
    }

    public void enableTimer() {
        enableTimer = true;
    }

    public void disableTimer() {
        enableTimer = false;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long l) {
        timer = l;
    }

    // Nexus

    public Integer getNexusHealth(ANNITeam team) {
        return nexus.get(team);
    }

    public void setNexusHealth(ANNITeam team, int health) {
        nexus.put(team, health);
    }

    public void healNexusHealth(ANNITeam team, int heal) {
        if (!isNexusLost(team)) {
            nexus.put(team, nexus.get(team) + heal);
        }
        else throw new IllegalStateException("Team" + team.name() + " is nexus lost.");
    }

    public void damageNexusHealth(ANNITeam team, int damage) {
        if (!isNexusLost(team)) {
            nexus.put(team, Math.max(nexus.get(team) - damage, 0));
        }
        else throw new IllegalStateException("Team" + team.name() + " is nexus lost.");
    }

    public boolean isNexusLost(ANNITeam team) {
        return nexus.get(team) == null;
    }

    public void restoreNexus(ANNITeam team, Integer health) {
        if (isNexusLost(team)) {
            nexus.put(team, health == null ? 100 : health);
        }
        else throw new IllegalStateException("Nexus is now active");
    }

    public void start() {
        try {

            copy = FileUtil.copyWorld(map.getBukkitWorld(), id + "-anni");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        updateBossBar();
        updateScoreboard();
        tickTimer();
    }

    @Override
    public void cancel() {
        super.cancel();

        deleteTeams();
        bb.setVisible(false);
        bb.removeAll();
        Bukkit.removeBossBar(bb.getKey());
    }

    private void updateBossBar() {
        ANNIMessage mm = plugin.getMessageManager();
        players.forEach(bb::addPlayer);

        switch (state) {
            case PHASE_ONE:
            case PHASE_TWO:
            case PHASE_THREE:
            case PHASE_FOUR:
            case PHASE_FIVE:
            case GAME_OVER: {
                bb.setTitle(
                        mm.build("bossbar.timer",
                                mm.build(state.getTimerStateKey()),
                                CmnUtil.secminTimer(timer)
                        )
                );
                break;
            }
            default: {
                bb.setVisible(false);
                break;
            }
        }
    }

    private void updateScoreboard() {
        ANNIMessage mm = plugin.getMessageManager();
        BoardManager bm = plugin.getBoardManager();

        for (Player pl : players) {
            FastBoard fb = bm.get(pl);
            SimpleDateFormat df = new SimpleDateFormat(mm.build("scoreboard.dateformat"));
            fb.updateTitle(mm.build("scoreboard.title"));

            switch (state) {
                case WAITING:
                case STARTING: {
                    String mapname = map != null ? map.getName() : mm.build("scoreboard.waiting.5.vote");
                    String news = state == ArenaState.STARTING ?
                            mm.build("scoreboard.waiting.2.starting") :
                            mm.build("scoreboard.waiting.2.more_player", (
                                    enabledTeams.entrySet().stream()
                                            .filter(Map.Entry::getValue)
                                            .count() * 2
                                            - players.size())
                            );

                    fb.updateLines(mm.buildList("scoreboard.waiting",
                            df.format(Calendar.getInstance().getTime()),
                            news,
                            players.size() + " / 120",
                            mapname
                    ));
                    break;
                }
                case PHASE_ONE:
                case PHASE_TWO:
                case PHASE_THREE:
                case PHASE_FOUR:
                case PHASE_FIVE:
                case GAME_OVER: {
                    fb.updateLines(mm.buildList("scoreboard.playing",
                            df.format(Calendar.getInstance().getTime()),
                            mm.build("nexus.status.active"),
                            mm.build("nexus.status.active"),
                            mm.build("nexus.status.lost"),
                            mm.build("nexus.status.lost"),
                            String.format(mm.build("nexus.health.format"), 100),
                            String.format(mm.build("nexus.health.format"), 100),
                            mm.build("nexus.health.none"),
                            mm.build("nexus.health.none"),
                            "マップなんかない"
                    ));
                    break;
                }
                default: {
                    fb.updateLines(mm.buildList("scoreboard.stopping",
                            df.format(Calendar.getInstance().getTime())
                    ));
                    break;
                }
            }
        }
    }

    private void tickTimer() {
        if (isEnabledTimer() && timer > 0) {
            timer--;
        }
    }
}
