package net.nekozouneko.anniv2.arena;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anniv2.ANNIConfig;
import net.nekozouneko.anniv2.ANNIPlugin;
import net.nekozouneko.anniv2.arena.spectator.SpectatorManager;
import net.nekozouneko.anniv2.arena.team.ANNITeam;
import net.nekozouneko.anniv2.board.BoardManager;
import net.nekozouneko.anniv2.kit.ANNIKit;
import net.nekozouneko.anniv2.kit.AbsANNIKit;
import net.nekozouneko.anniv2.map.ANNIMap;
import net.nekozouneko.anniv2.message.MessageManager;
import net.nekozouneko.anniv2.util.CmnUtil;
import net.nekozouneko.anniv2.util.FileUtil;
import net.nekozouneko.anniv2.util.VaultUtil;
import net.nekozouneko.anniv2.vote.VoteManager;
import net.nekozouneko.commons.lang.collect.Collections3;
import net.nekozouneko.commons.spigot.world.Worlds;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ANNIArena extends BukkitRunnable {

    private static final Random rand = new Random();

    private final ANNIPlugin plugin;
    private final MessageManager mm;
    private final String id;

    private final Set<Player> players = new HashSet<>();

    private final BiMap<ANNITeam, Team> teams = HashBiMap.create(4);
    private final Map<ANNITeam, Boolean> enabledTeams = new EnumMap<>(ANNITeam.class);

    private ArenaState state = ArenaState.WAITING;

    private ANNIMap map = null;
    private World copy = null;

    private boolean enableTimer = false;
    private long timer = 0;

    private final Map<ANNITeam, Integer> nexus = new EnumMap<>(ANNITeam.class);
    private final Map<UUID, String> kit = new HashMap<>();

    private final KeyedBossBar bb;

    public ANNIArena(ANNIPlugin plugin, String id) {
        Preconditions.checkArgument(plugin != null, "Argument 'plugin' cannot be null.");
        Preconditions.checkArgument(id.length() < 9, "Id length limit is 8! (" + id.length() + ")");

        this.plugin = plugin;
        this.mm = plugin.getMessageManager();
        this.id = id;

        this.bb = Bukkit.createBossBar(
                new NamespacedKey(plugin, id),
                "",
                BarColor.BLUE,
                BarStyle.SOLID
        );

        createTeams();

        for (ANNITeam at : ANNITeam.values()) {
            if (!ANNIConfig.isTeamEnabled(at))
                disableTeam(at);
        }
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

        if (getState().getId() > 0) {
            assignAtEquality(player);
            if (!isNexusLost(getTeamByPlayer(player))) {
                player.teleport(map.getSpawnOrDefault(getTeamByPlayer(player)).toLocation(copy));
                ANNITeam at = getTeamByPlayer(player);
                player.sendMessage(mm.buildBigChar(CmnUtil.numberToChar(state.getId()), Character.toString(at.getCCChar()),
                        (Object[]) mm.buildArray("notify.big.mid_join", at.getTeamName())
                ));
                player.getInventory().setContents(ANNIKit.teamColor(getKit(player), at));
            }
            else {
                player.teleport(map.getSpawnOrDefault(getTeamByPlayer(player)).toLocation(copy));
                SpectatorManager.add(player);
            }
        }
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

        Team r = sb.registerNewTeam(id + "-red");
        Team b = sb.registerNewTeam(id + "-blue");
        Team g = sb.registerNewTeam(id + "-green");
        Team y = sb.registerNewTeam(id + "-yellow");

        r.setDisplayName(mm.build("team.red.display"));
        r.setPrefix(mm.build("team.red.prefix"));
        r.setColor(org.bukkit.ChatColor.RED);
        r.setAllowFriendlyFire(false);
        r.setCanSeeFriendlyInvisibles(true);

        b.setDisplayName(mm.build("team.blue.display"));
        b.setPrefix(mm.build("team.blue.prefix"));
        b.setColor(org.bukkit.ChatColor.BLUE);
        b.setAllowFriendlyFire(false);
        b.setCanSeeFriendlyInvisibles(true);

        g.setDisplayName(mm.build("team.green.display"));
        g.setPrefix(mm.build("team.green.prefix"));
        g.setColor(org.bukkit.ChatColor.GREEN);
        g.setAllowFriendlyFire(false);
        g.setCanSeeFriendlyInvisibles(true);

        y.setDisplayName(mm.build("team.yellow.display"));
        y.setPrefix(mm.build("team.yellow.prefix"));
        y.setColor(org.bukkit.ChatColor.YELLOW);
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

    public void setTeam(Player player, ANNITeam team) {
        if (player.getScoreboard() != plugin.getPluginBoard()) player.setScoreboard(plugin.getPluginBoard());

        getTeams(false).inverse().keySet().forEach(team1 -> team1.removePlayer(player));
        if (team != null) {
            getTeam(team).addPlayer(player);
        }
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
        if (damage <= 0) return;
        if (!isNexusLost(team)) {
            int health = nexus.get(team) - damage;
            nexus.put(team, health <= 0 ? null : health);

            if (player != null) {
                bb.setProgress(1);
                bb.setTitle(ANNIPlugin.getInstance().getMessageManager().build(
                        "bossbar.damaged_nexus",
                        player.getName(), team.getTeamName()
                ));
                getTeamPlayers(team).forEach(p1 -> {
                    p1.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            mm.build("actionbar.nexus_alert", player.getName())
                    ));
                    p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
                });
            }
            else {
                getTeamPlayers(team).forEach(p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 2));
            }

            if (isNexusLost(team)) {
                for (String s :
                        mm.buildBigChar(
                                team.getBigChar(),
                                Character.toString(getTeamByPlayer(player).getCCChar()),
                                (Object[]) mm.buildArray("notify.big.lost_nexus",
                                        team.getColorCode() + team.getTeamName(),
                                        player != null ? player.getName() : "-----"
                                )
                        )
                ) {
                    broadcast(s);
                }
            }
        }
        else throw new IllegalStateException("Team " + team.name() + " is already nexus lost.");
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
        if (plugin.getMapManager().getMaps().isEmpty()) return false;

        Logger log = plugin.getLogger();

        log.info("Starting game... (ID: " + id + ")");
        try {
            if (map == null) {
                if (VoteManager.isNowVoting(id) && !VoteManager.getResult(id).isEmpty()) {
                    map = getMapRanking(VoteManager.endVote(id)).get(0).getKey();
                }
                else map = plugin.getMapManager().getMaps()
                        .get(rand.nextInt(plugin.getMapManager().getMaps().size()));
            }
            log.info("Map: " + map.getId());

            log.info("Copying map...");
            copy = Worlds.copyWorld(map.getBukkitWorld(), id + "-anni");
            if (copy == null) {
                log.warning("Copy map failed.");
                return false;
            }
            log.info("Copy complete.");

            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager copyrm = rc.get(BukkitAdapter.adapt(copy));
            RegionManager origrm = rc.get(BukkitAdapter.adapt(map.getBukkitWorld()));

            if (copyrm != null && origrm != null) {
                // 保護領域のコピー
                origrm.getRegions().forEach((s, pr) -> {
                    log.info("Copying region: " + s + " / " + pr.getId());
                    ProtectedRegion newpr;
                    if (pr.getType() == RegionType.GLOBAL) {
                        log.info(s + " is global region");
                        newpr = new GlobalProtectedRegion(s);
                    }
                    else if (pr.getType() == RegionType.POLYGON) {
                        log.info(s + " is polygon region.");
                        newpr = new ProtectedPolygonalRegion(s,
                                pr.getPoints(),
                                pr.getMinimumPoint().getY(),
                                pr.getMaximumPoint().getY()
                        );
                    }
                    else {
                        log.info(s + " is cuboid region.");
                        newpr = new ProtectedCuboidRegion(s, pr.getMinimumPoint(), pr.getMaximumPoint());
                    }

                    log.info("Setting flag: " + pr.getFlags());
                    newpr.setFlags(pr.getFlags());

                    if (s.startsWith("anni-wood")) {
                        log.info(s + " is wood region.");
                        newpr.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
                        newpr.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
                    }
                    copyrm.addRegion(newpr);
                    log.info("Copy region of '" + s+ "' is complete.");
                });
                // コピー終了後親保護領域などの設定
                origrm.getRegions().forEach((idd, prr) -> {
                    if (copyrm.hasRegion(idd)) {
                        log.info("Checking region's parent: " + idd);
                        if (prr.getParent() != null) {
                            log.info("Region has parent.");
                            try {
                                copyrm.getRegion(idd).setParent(prr.getParent());
                            } catch (ProtectedRegion.CircularInheritanceException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                log.info("Setting nexus protection.");
                // ネクサスの保護領域の設定
                map.getNexuses().forEach((at, nexus) -> {
                    log.info(at.name());
                    ProtectedRegion reg = new ProtectedCuboidRegion(
                            id+"-"+at.name().toLowerCase()+"-nexus",
                            nexus.getLocation(),
                            nexus.getLocation()
                    );
                    reg.setPriority(91217);
                    copyrm.addRegion(reg);
                    log.info("complete");
                });
            }

            getTeams(false).keySet()
                    .forEach(at -> {
                        Block bl = BukkitAdapter.adapt(copy, map.getNexus(at).getLocation()).getBlock();
                        if (!isEnabledTeam(at))
                            bl.setType(Material.BEDROCK);
                        else bl.setType(Material.END_STONE);
                    });

            log.info("Assigning players...");
            players.forEach(this::assignAtEquality);
            nexus.clear();
            getTeams().forEach((at, team) -> {
                setNexusHealth(at, ANNIConfig.getDefaultHealth());
                Location sl = map.getSpawnOrDefault(at).toLocation(copy);
                team.getPlayers().stream()
                        .filter(OfflinePlayer::isOnline)
                        .map(offp -> Bukkit.getPlayer(offp.getUniqueId()))
                        .forEach(p -> {
                            p.teleport(sl);
                            initPlayer(p);
                            p.getInventory().setContents(ANNIKit.teamColor(getKit(p), at));
                        });
                for (String s : mm.buildBigChar('1', Character.toString(at.getCCChar()),
                        (Object[]) mm.buildArray("notify.big.started", at.getTeamName())
                )) broadcast(s, at);
            });

            setState(ArenaState.PHASE_ONE);
            setTimer(ArenaState.PHASE_ONE.nextPhaseIn());
            log.info("Started game.");
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void cleanUp() {
        Logger log = plugin.getLogger();
        log.info("Starting clean up.");
        try {
            log.info("Initializing players...");
            players.forEach(player -> {
                initPlayer(player);
                player.teleport(plugin.getLobby());
            });
            log.info("Removing player from team...");
            teams.values().forEach(team -> team.getPlayers().forEach(team::removePlayer));
            log.info("Showing spectators...");
            SpectatorManager.clear();
            log.info("Initializing nexus...");
            nexus.clear();
            log.info("Initializing map...");
            map = null;
            if (copy != null) {
                log.info("Copy map is now available. clean up now.");
                RegionManager rm = WorldGuard.getInstance().getPlatform()
                                .getRegionContainer().get(
                                        BukkitAdapter.adapt(copy)
                        );
                try {
                    log.info("Removing worldguard regions...");
                    rm.getRegions().values().forEach((pr) -> {
                        log.info("Preparing remove region of '" + pr.getId() + "'...");
                        if (pr.getType() != RegionType.GLOBAL) {
                            rm.removeRegion(pr.getId());
                            log.info("Removed region:" + pr.getId());
                        }
                        else {
                            pr.setFlags(new HashMap<>());
                            log.info("Reset global region: " + pr.getId());
                        }
                    });
                }
                catch (NullPointerException e) { e.printStackTrace(); }
                log.info("Deleting map...");
                FileUtil.deleteWorld(copy);
                copy = null;
            }
            if (VoteManager.isNowVoting(id)) VoteManager.endVote(id);
        }
        catch (IOException e) { e.printStackTrace(); }

        log.info("Clean up complete.");
    }

    public void broadcast(String message) {
        players.forEach(p -> p.sendMessage(message));
        plugin.getLogger().info(message);
    }

    public void broadcast(String message, ANNITeam team) {
        getTeamPlayers(team).forEach(p -> p.sendMessage(message));
        plugin.getLogger().info(message);
    }

    public void setKit(Player player, ANNIKit ki) {
        kit.put(player.getUniqueId(), ki.getKit().getId());
    }

    public AbsANNIKit getKit(Player player) {
        String id = kit.get(player.getUniqueId());
        return ANNIKit.getKitById(id).getKit();
    }

    @Override
    public void run() {
        if (state == ArenaState.WAITING || state == ArenaState.STARTING) {
            if (map == null) {
                if (state == ArenaState.STARTING && getTimer() <= 10) {
                    if (VoteManager.isNowVoting(id)) {
                        List<Map.Entry<ANNIMap, Integer>> l = getMapRanking(VoteManager.endVote(id));
                        if (l.isEmpty() || Collections3.allValueEquals(l, l.get(0))) {
                            map = plugin.getMapManager().getMaps().get(rand.nextInt(plugin.getMapManager().getMaps().size()));
                        }
                        else map = l.get(0).getKey();
                    }
                }
                else {
                    Set<Object> choices = plugin.getMapManager().getMaps().stream()
                            .filter(ANNIMap::canUseOnArena)
                            .map(m -> (Object) m.getId())
                            .collect(Collectors.toSet());

                    if (VoteManager.isNowVoting(id)) {
                        VoteManager.updateChoices(
                                id, choices
                        );
                    } else VoteManager.startVote(id, choices);
                }
            }
            else if (VoteManager.isNowVoting(id)) VoteManager.endVote(id);
        }

        updateBossBar();
        updateScoreboard();
        tickTimer();

        if (state.getId() > 0) {
            // プレイヤー数が0のチームを退場させる
            getTeams().keySet().forEach(at -> {
                if (!isNexusLost(at) && getTeamPlayers(at).isEmpty()) {
                    nexus.put(at, null);
                    broadcast(plugin.getMessageManager().build("notify.no_player_team", at.getTeamName()));
                }
            });

            if (state == ArenaState.PHASE_FIVE) {
                getTeams().keySet().forEach((at) -> {
                    // (ネクサスを失ったもしくは、ネクサスの体力が1以下) ではないなら
                    if (!(isNexusLost(at) || getNexusHealth(at) <= 1)) {
                        damageNexusHealth(at, 1, null);
                    }
                });
            }

            // ネクサスを失っていないチーム数を調べる
            List<ANNITeam> living = getTeams().keySet().stream()
                    .filter(team -> !isNexusLost(team))
                    .collect(Collectors.toList());

            // もし1以下なら
            if (living.size() <= 1) {
                // もし1なら
                if (living.size() == 1) {
                    ANNITeam won = living.get(0);

                    for (String s : mm.buildBigChar(
                            'e', Character.toString(won.getCCChar()),
                            (Object[]) mm.buildArray("notify.big.won",
                                    won.getColoredName()
                            ))
                    ) broadcast(s);
                    VaultUtil.ifAvail((eco) -> getTeamPlayers(won).forEach(p -> {
                            eco.depositPlayer(p, 3000);
                            p.sendMessage(mm.build("notify.deposit_points", "3000", mm.build("gui.shop.full_ext")));
                    }));
                } else { // ではない (0 ~ (Integer.MIN_VALUE)) なら
                    broadcast(mm.build("notify.draw"));
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
                                mm.build(state.getName()),
                                CmnUtil.secminTimer(timer)
                        )
                );
                bb.setProgress(state.nextPhaseIn() > 0 ? CmnUtil.bossBarProgress(state.nextPhaseIn(), timer) : 1);
                break;
            }
            case PHASE_FIVE: {
                bb.setVisible(true);
                bb.setTitle(mm.build(state.getName()));
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
        BoardManager bm = plugin.getBoardManager();

        for (Player pl : players) {
            FastBoard fb = bm.get(pl);
            SimpleDateFormat df = new SimpleDateFormat(mm.build("scoreboard.dateformat"));
            fb.updateTitle(mm.build("scoreboard.title"));

            switch (state) {
                case WAITING:
                case STARTING: {
                    String news = state == ArenaState.STARTING ?
                            mm.build("scoreboard.waiting.2.starting", CmnUtil.secminTimer(getTimer())) :
                            mm.build("scoreboard.waiting.2.more_player", (
                                    enabledTeams.entrySet().stream()
                                            .filter(Map.Entry::getValue)
                                            .count() * ANNIConfig.getTeamMinPlayers()
                                            - players.size())
                            );

                    if (map != null) {
                        fb.updateLines(mm.buildList("scoreboard.waiting",
                                df.format(Calendar.getInstance().getTime()),
                                news,
                                players.size() + " / 120",
                                map.getName()
                        ));
                    }
                    else {
                        List<Map.Entry<ANNIMap, Integer>> nowRes = getMapRanking(VoteManager.getResult(id));
                        fb.updateLines(mm.buildList("scoreboard.waiting_voting",
                                df.format(Calendar.getInstance().getTime()),
                                news,
                                players.size() + " / " + (ANNIConfig.getTeamMaxPlayers() * getEnabledTeams().size()),
                                nowRes.size() > 0 ? nowRes.get(0).getKey().getName() + " - " + nowRes.get(0).getValue(): "",
                                nowRes.size() > 1 ? nowRes.get(1).getKey().getName() + " - " + nowRes.get(1).getValue(): "",
                                nowRes.size() > 2 ? nowRes.get(2).getKey().getName() + " - " + nowRes.get(2).getValue(): ""
                        ));
                    }
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
        if (nh == null) nh = 0;
        return isEnabledTeam(team) ? // teamが無効化されていないなら
                String.format(mm.build("nexus.health.format"), nh) // フォーマット適応済みの体力
                : mm.build("nexus.health.none"); // 体力なしのメッセージ
    }

    private String sbNexusState(ANNITeam team) {
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
                    getTeams().forEach((at, t) -> {
                        for (String s : mm.buildBigChar(
                                CmnUtil.numberToChar(state.nextPhase().getId()),
                                Character.toString(at.getCCChar()),
                                (Object[]) mm.buildArray("notify.big.next_phase",
                                    mm.build(state.nextPhase().getName()),
                                    mm.build(state.nextPhase().getDescription())
                                )
                        )) {
                            broadcast(s, at);
                        }
                    });
                    players.forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 2));
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

    private void assignAtEquality(Player player) {
        if (player.getScoreboard() != plugin.getPluginBoard())
            player.setScoreboard(plugin.getPluginBoard());
        if (CmnUtil.getJoinedTeam(player) != null) return;

        Map<Team, Integer> teamSize = new HashMap<>();
        getTeams().entrySet().stream()
                .filter(ent -> state.getId() < 0 || !isNexusLost(ent.getKey()))
                .map(Map.Entry::getValue)
                .forEach(t -> teamSize.put(t, t.getSize()));

        if ( // 全部の値が一緒なら
                Collections3.allValueEquals(
                        teamSize.values(),
                        teamSize.values().iterator().next() // チーム人数リストの最初の要素
                )
        ) teams.values().iterator().next().addPlayer(player); // 最初の要素 (チーム)に参加させる
        else { // 一緒じゃなければ均等に分散させる
            Map.Entry<Team, Integer> minTeam = null; // 人数が少ないチーム

            for (Map.Entry<Team, Integer> entry : teamSize.entrySet()) {
                if (minTeam == null || minTeam.getValue() > entry.getValue())
                    minTeam = entry; // minTeamがnullもしくはminTeamの人数より少なければentryに置き換える
            }

            minTeam.getKey().addPlayer(player);
        }
    }

    private List<Map.Entry<ANNIMap, Integer>> getMapRanking(Multimap<Object, OfflinePlayer> multimap) {
        Map<ANNIMap, Integer> rank = new HashMap<>();
        multimap.keySet().forEach((obj) -> {
            if (!(obj instanceof String)) return;
            ANNIMap ma = plugin.getMapManager().getMap((String) obj);
            if (ma != null)
                rank.put(ma, multimap.get(obj).size());
        });


        LinkedList<Map.Entry<ANNIMap, Integer>> ll = new LinkedList<>(rank.entrySet());
        ll.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        return ll;
    }

    private void initPlayer(Player player) {
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setLevel(0);
        player.setExp(0);
    }
}
