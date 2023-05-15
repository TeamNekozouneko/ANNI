package net.nekozouneko.anniv2.arena;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.HashBiMap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionType;
import fr.mrmicky.fastboard.FastBoard;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.board.BoardManager;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmnUtil;
import net.nekozouneko.anniv2.util.FileUtil;
import net.nekozouneko.commons.spigot.world.Worlds;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        if (players.contains(player)) return;

        players.add(player);
        player.setScoreboard(plugin.getPluginBoard());
    }

    public void leave(Player player) {
        players.remove(player);

        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        bb.removePlayer(player);
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public boolean isJoined(Player player) {
        return players.contains(player);
    }

    // Team

    private void createTeams() {
        final Scoreboard sb = plugin.getPluginBoard();
        final MessageManager mm = plugin.getMessageManager();

        Team r = sb.registerNewTeam(id + "-red");
        Team b = sb.registerNewTeam(id + "-blue");
        Team g = sb.registerNewTeam(id + "-green");
        Team y = sb.registerNewTeam(id + "-yellow");

        r.setDisplayName(mm.build("team.red.display"));
        r.setPrefix(mm.build("team.red.prefix"));
        r.setColor(ChatColor.RED);
        r.setAllowFriendlyFire(false);
        r.setCanSeeFriendlyInvisibles(true);

        b.setDisplayName(mm.build("team.blue.display"));
        b.setPrefix(mm.build("team.blue.prefix"));
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

    public boolean setTeam(Player player, ANNITeam team) {
        if (team != null)
            getTeam(team).addPlayer(player);
        else getTeams(false).inverse().forEach((team1, ant) -> team1.removePlayer(player));
        return true;
    }

    public Team getTeam(ANNITeam team) {
        return teams.get(team);
    }

    public ANNITeam getTeam(Team team) {
        return teams.inverse().get(team);
    }

    public ANNITeam getTeamByPlayer(Player player) {
        Team t = plugin.getPluginBoard().getPlayerTeam(player);
        return t != null ? getTeam(t) : null;
    }

    public BiMap<ANNITeam, Team> getTeams() {
        return getTeams(true);
    }

    public BiMap<ANNITeam, Team> getTeams(boolean enabledOnly) {
        BiMap<ANNITeam, Team> res = EnumHashBiMap.create(ANNITeam.class);

        teams.entrySet().stream()
                .filter(t -> !enabledOnly || enabledTeams.getOrDefault(t.getKey(), true))
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

    public void damageNexusHealth(ANNITeam team, int damage, Player player) {
        if (!isNexusLost(team)) {
            int health = nexus.get(team) - damage;
            nexus.put(team, health <= 0 ? null : health);

            if (player != null) {
                bb.setProgress(1);
                bb.setTitle(ANNIPlugin.getInstance().getMessageManager().build(
                        "bossbar.damaged_nexus",
                        player.getName(), team.getTeamName()
                ));
            }

            if (isNexusLost(team)) {
                Bukkit.broadcastMessage(team.name() + " was nexus lost");
            }
        }
        else throw new IllegalStateException("Team " + team.name() + " is nexus lost.");
    }

    public boolean isNexusLost(ANNITeam team) {
        return nexus.get(team) == null;
    }

    public void restoreNexus(ANNITeam team, Integer health) {
        if (isNexusLost(team)) {
            nexus.put(team, health == null ? 100 : health);
            BukkitAdapter.adapt(copy, map.getNexus(team).getLocation())
                    .getBlock().setType(Material.END_STONE);
        }
        else throw new IllegalStateException("Nexus is now active");
    }

    public ANNIMap getMap() {
        return map;
    }

    public void setMap(ANNIMap map) {
        this.map = map;
    }

    public World getCopyWorld() {
        return copy;
    }

    public boolean start() {
        if (plugin.getLobby() == null) return false;

        try {
            if (map == null) map = plugin.getMapManager().getMaps().iterator().next();

            copy = Worlds.copyWorld(map.getBukkitWorld(), id + "-anni");
            if (copy == null) return false;

            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager copyrm = rc.get(BukkitAdapter.adapt(copy));
            RegionManager origrm = rc.get(BukkitAdapter.adapt(map.getBukkitWorld()));

            if (copyrm != null && origrm != null) {
                origrm.getRegions().forEach((s, pr) -> {
                    if (pr.getType() == RegionType.GLOBAL)
                        copyrm.getRegion(s).setFlags(pr.getFlags());
                    else {
                        copyrm.addRegion(pr);
                    }
                });
                map.getNexuses().forEach((at, nexus) -> {
                    ProtectedRegion reg = new ProtectedCuboidRegion(
                            id+"-"+at.name().toLowerCase()+"-nexus",
                            nexus.getLocation(),
                            nexus.getLocation()
                    );
                    reg.setPriority(100);
                    copyrm.addRegion(reg);
                });
            }

            getTeams(false).keySet()
                    .forEach(at -> {
                        Block bl = BukkitAdapter.adapt(copy, map.getNexus(at).getLocation()).getBlock();
                        if (!isEnabledTeam(at))
                            bl.setType(Material.BEDROCK);
                        else bl.setType(Material.END_STONE);
                    });

            CmnUtil.assignAtEquality(players, getTeams().values(), true);
            nexus.clear();
            getTeams().forEach((at, team) -> {
                setNexusHealth(at, 100);
                Location sl = map.getSpawnOrDefault(at).toLocation(copy);
                team.getPlayers().stream()
                        .filter(OfflinePlayer::isOnline)
                        .map(offp -> Bukkit.getPlayer(offp.getUniqueId()))
                        .forEach(p -> p.teleport(sl));
            });

            setState(ArenaState.PHASE_ONE);
            setTimer(ArenaState.PHASE_ONE.nextPhaseIn());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void cleanUp() {
        try {
            players.forEach(player -> player.teleport(plugin.getLobby()));
            teams.values().forEach(team -> team.getPlayers().forEach(team::removePlayer));
            nexus.clear();
            map = null;
            if (copy != null) {
                FileUtil.deleteWorld(copy);
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void broadcast(String message) {
        players.forEach(p -> p.sendMessage(message));
    }

    public void broadcast(String message, ANNITeam team) {
        getTeamPlayers(team).forEach(p -> p.sendMessage(message));
    }

    @Override
    public void run() {
        updateBossBar();
        updateScoreboard();
        tickTimer();

        if (state.getId() > 0) {
            // ネクサスを失っていないチーム数を調べる
            List<ANNITeam> living = getTeams().keySet().stream()
                    .filter(team -> !isNexusLost(team))
                    .collect(Collectors.toList());

            // もし1以下なら
            if (living.size() <= 1) {
                // もし1なら
                if (living.size() == 1) {
                    ANNITeam won = living.get(0);

                    Bukkit.broadcastMessage(won.getTeamName() + "が勝利"); //TODO 翻訳可能なメッセージ化
                } else { // ではない (0 ~ (Integer.MIN_VALUE)) なら
                    Bukkit.broadcastMessage("すべてのチームのネクサスが破壊されたため引き分け的ななにか"); //TODO 翻訳可能なメッセージ化
                }

                setTimer(ArenaState.GAME_OVER.nextPhaseIn());
                setState(ArenaState.GAME_OVER);
            }
        }
    }

    @Override
    public void cancel() {
        super.cancel();

        cleanUp();
        deleteTeams();
        bb.setVisible(false);
        bb.removeAll();
        Bukkit.removeBossBar(bb.getKey());
    }

    private void updateBossBar() {
        MessageManager mm = plugin.getMessageManager();
        players.forEach(bb::addPlayer);

        switch (state) {
            case PHASE_ONE:
            case PHASE_TWO:
            case PHASE_THREE:
            case PHASE_FOUR:
            case GAME_OVER: {
                bb.setVisible(true);
                bb.setTitle(
                        mm.build("bossbar.timer",
                                mm.build(state.getTimerStateKey()),
                                CmnUtil.secminTimer(timer)
                        )
                );
                bb.setProgress(state.nextPhaseIn() > 0 ? CmnUtil.bossBarProgress(state.nextPhaseIn(), timer) : 1);
                break;
            }
            case PHASE_FIVE: {
                bb.setVisible(true);
                bb.setTitle(mm.build(state.getTimerStateKey()));
                bb.setProgress(1);
                break;
            }
            default: {
                bb.setVisible(false);
                break;
            }
        }
    }

    private void updateScoreboard() {
        MessageManager mm = plugin.getMessageManager();
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
                            sbNexusState(ANNITeam.RED),
                            sbNexusState(ANNITeam.BLUE),
                            sbNexusState(ANNITeam.GREEN),
                            sbNexusState(ANNITeam.YELLOW),
                            sbNexusHealth(ANNITeam.RED),
                            sbNexusHealth(ANNITeam.BLUE),
                            sbNexusHealth(ANNITeam.GREEN),
                            sbNexusHealth(ANNITeam.YELLOW),
                            map != null ? map.getName() : "-----"
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

    private String sbNexusHealth(ANNITeam team) {
        Integer nh = getNexusHealth(team);
        MessageManager mm = plugin.getMessageManager();
        return nh != null ? // ネクサスの体力が0なら
                String.format(mm.build("nexus.health.format"), nh) // フォーマット適応済みの体力
                : mm.build("nexus.health.none"); // 体力なしのメッセージ
    }

    private String sbNexusState(ANNITeam team) {
        MessageManager mm = plugin.getMessageManager();
        return isNexusLost(team) ? mm.build("nexus.status.lost") : mm.build("nexus.status.active");
    }

    private void tickTimer() {
        if (isEnabledTimer() && timer > 0) {
            timer--;
        }

        switch (state) {
            case WAITING: {
                if (players.size() >= getTeams().size() * 2) {
                    enableTimer();
                    setTimer(ArenaState.STARTING.nextPhaseIn());
                    setState(ArenaState.STARTING);
                }
                break;
            }
            case STARTING: {
                if (!(players.size() >= getTeams().size() * 2)) {
                    disableTimer();
                    setState(ArenaState.WAITING);
                }
                else {
                    enableTimer();
                    if (getTimer() <= 5) {
                        if (getTimer() <= 0) {
                            if (!start()) setState(ArenaState.STOPPED);
                        } else {
                            players.forEach(player -> {
                                player.sendTitle(String.valueOf(getTimer()), null, 3, 18, 3);
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            });
                        }
                    }
                }
                break;
            }
            case PHASE_ONE:
            case PHASE_TWO:
            case PHASE_THREE:
            case PHASE_FOUR: {
                enableTimer();
                if (timer <= 0) {
                    setTimer(state.nextPhaseIn());
                    setState(state.nextPhase());
                }
                break;
            }
            case PHASE_FIVE: {
                disableTimer();
                break;
            }
            case GAME_OVER:
                enableTimer();
                if (timer <= 0) {
                    disableTimer();
                    cleanUp();
                    setState(ArenaState.WAITING);
                }
                break;
        }
    }
}
