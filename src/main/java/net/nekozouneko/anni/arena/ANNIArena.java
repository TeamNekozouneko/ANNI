package net.nekozouneko.anni.arena;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.board.BoardManager;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.AbstractKit;
import net.nekozouneko.anni.item.DefenseArtifact;
import net.nekozouneko.anni.listener.PlayerDamageListener;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.util.CmnUtil;
import net.nekozouneko.anni.util.FileUtil;
import net.nekozouneko.anni.util.VaultUtil;
import net.nekozouneko.anni.vote.VoteManager;
import net.nekozouneko.commons.lang.collect.Collections3;
import net.nekozouneko.commons.spigot.entity.Players;
import net.nekozouneko.commons.spigot.world.Worlds;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ANNIArena extends BukkitRunnable {

    private static final Random rand = new Random();

    @Getter @AllArgsConstructor
    private static class SaveData {
        private final boolean isSpectator;
        private final ANNITeam team;
        private final ItemStack[] inventory;
        private final float exp;
        private final int level;
        private final double health;
    }

    private final ANNIPlugin plugin;
    private final MessageManager mm;
    @Getter
    private final String id;

    private final Set<Player> players = new HashSet<>();

    private final BiMap<ANNITeam, Team> teams = HashBiMap.create(4);
    private final Map<ANNITeam, Boolean> enabledTeams = new EnumMap<>(ANNITeam.class);

    @Getter @Setter
    private ArenaState state = ArenaState.WAITING;

    private ANNIMap map = null;
    private World copy = null;

    @Getter
    private boolean enabledTimer = false;
    @Getter @Setter
    private long timer = 0;
    private int fireworkTimer = 0;

    private final Map<ANNITeam, Integer> nexus = new EnumMap<>(ANNITeam.class);
    private final Map<UUID, String> kit = new HashMap<>();
    private final Map<UUID, SaveData> savedData = new HashMap<>();

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

    public void join(Player player) {
        if (players.contains(player)) return;

        players.add(player);
        player.setScoreboard(plugin.getPluginBoard());

        if (state.getId() <= 0) {
            initPlayer(player);
            if (plugin.getLobby() != null) player.teleport(plugin.getLobby());
            return;
        }

        SaveData data = savedData.remove(player.getUniqueId());
        ANNITeam team = data == null ? assignTeam() : data.getTeam();
        setTeam(player, team);

        if (isNexusLost(team)) {
            Players.clearPotionEffects(player);
            initPlayer(player);
            SpectatorManager.add(player);
            player.teleport(map.getSpawnOrDefault(team).toLocation(copy));
            return;
        }

        player.setGameMode(GameMode.SURVIVAL);
        if (data != null) {
            player.getInventory().setContents(data.getInventory());
            player.setExp(data.getExp());
            player.setLevel(data.getLevel());
            player.setHealth(data.getHealth());
        }
        else {
            Players.clearPotionEffects(player);
            initPlayer(player);
            player.getInventory().setContents(ANNIKit.teamColor(getKit(player), team));
            player.teleport(map.getSpawnOrDefault(team).toLocation(copy));
        }


        player.sendMessage(mm.buildBigChar(CmnUtil.numberToChar(state.getId()), Character.toString(team.getCCChar()),
                (Object[]) mm.buildArray("notify.big.mid_join", team.getTeamName())
        ));
    }

    public void leave(Player player) {
        players.remove(player);

        if (state.getId() > 0 && getTeamByPlayer(player) != null && !PlayerDamageListener.isFighting(player)) {
            savedData.put(
                    player.getUniqueId(),
                    new SaveData(
                            isNexusLost(getTeamByPlayer(player)) && SpectatorManager.isSpectating(player),
                            getTeamByPlayer(player),
                            player.getInventory().getContents(),
                            player.getExp(),
                            player.getLevel(),
                            player.getHealth()
                    )
            );
            player.getInventory().clear();
        }

        if (getTeamByPlayer(player) != null) getTeam(getTeamByPlayer(player)).removePlayer(player);

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

    public void enableTimer() {
        enabledTimer = true;
    }

    public void disableTimer() {
        enabledTimer = false;
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
                double leftHealth = CmnUtil.bossBarProgress(ANNIConfig.getDefaultHealth(), health);
                if (leftHealth <= 0.2) bb.setColor(BarColor.RED);
                else if (leftHealth <= 0.5) bb.setColor(BarColor.YELLOW);
                else bb.setColor(BarColor.GREEN);

                bb.setProgress(leftHealth);

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

                VaultUtil.ifAvail((eco) -> {
                    if (getTeamByPlayer(player) == null) return;

                    getTeamPlayers(getTeamByPlayer(player)).forEach(teammate -> {
                        eco.depositPlayer(teammate, 3);
                        teammate.sendMessage(
                                mm.build("notify.deposit_points", "3", mm.build("gui.shop.full_ext"))
                        );
                    });
                });

                if (ANNIKit.get(getKit(player)) == ANNIKit.WORKER && !isNexusLost(getTeamByPlayer(player))) {
                    boolean isPass;
                    switch (getState()) {
                        case PHASE_TWO: {
                            isPass = rand.nextDouble() >= 0.70;
                            break;
                        }
                        case PHASE_THREE: {
                            isPass = rand.nextDouble() >= 0.80;
                            break;
                        }
                        case PHASE_FOUR: {
                            isPass = rand.nextDouble() >= 0.85;
                            break;
                        }
                        default: {
                            isPass = false;
                            break;
                        }
                    }

                    if (isPass) healNexusHealth(getTeamByPlayer(player), 1);
                }
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
        return nexus.get(team) == null || nexus.get(team) <= 0;
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
                    map = getMapRanking(VoteManager.endVote(id)).stream()
                            .filter(ent -> ent.getKey().canUseOnArena())
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);
                }

                if (map == null) {
                    List<ANNIMap> filtered = plugin.getMapManager().getMaps().stream()
                            .filter(ANNIMap::canUseOnArena)
                            .collect(Collectors.toList());
                    if (filtered.isEmpty()) return false;
                    map = filtered.get(rand.nextInt(filtered.size()));
                }
            }
            log.info("Map: " + map.getId());
            if (map == null) return false;

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
                    newpr.setFlags(new HashMap<>(pr.getFlags()));

                    log.info("Original Priority: " + pr.getPriority());
                    if (s.startsWith("anni-wood")) {
                        log.info(s + " is wood region.");
                        newpr.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
                        newpr.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
                        newpr.setPriority(10);
                    }
                    else newpr.setPriority(pr.getPriority());
                    log.info("Priority: " + pr.getPriority());

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
                    reg.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
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
            savedData.clear();
            players.forEach(player -> setTeam(player, assignTeam()));
            nexus.clear();
            getTeams().forEach((at, team) -> {
                setNexusHealth(at, ANNIConfig.getDefaultHealth());
                Location sl = map.getSpawnOrDefault(at).toLocation(copy);
                team.getPlayers().stream()
                        .filter(OfflinePlayer::isOnline)
                        .map(offp -> Bukkit.getPlayer(offp.getUniqueId()))
                        .forEach(p -> {
                            p.teleport(sl);
                            p.setGameMode(GameMode.SURVIVAL);
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
            cleanUp();
            return false;
        }

        return true;
    }

    public void cleanUp() {
        Logger log = plugin.getLogger();
        log.info("Starting clean up.");
        try {
            log.info("Initializing players...");
            SpectatorManager.clear();
            savedData.clear();
            plugin.getCooldownManager().clear();
            players.forEach(player -> {
                player.spigot().respawn();
                initPlayer(player);
                Players.clearPotionEffects(player);
                player.teleport(plugin.getLobby());
            });
            log.info("Removing player from team...");
            teams.values().forEach(team -> {
                    team.getEntries().forEach(team::removeEntry);
                    log.info(team.getEntries().toString());
            });
            log.info("Cancelling tasks...");
            DefenseArtifact.cancelAllTasks();
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

                fireworkTimer = 0;
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

    public void setKit(Player player, AbstractKit ki) {
        kit.put(player.getUniqueId(), ki.getId());
    }

    public AbstractKit getKit(Player player) {
        String id = kit.get(player.getUniqueId());
        return ANNIKit.getAbsKitOrCustomById(id);
    }

    @Override
    public void run() {
        if (isEnabledTimer() && timer > 0) {
            timer--;
        }

        if (state == ArenaState.WAITING || state == ArenaState.STARTING) {
            players.forEach(player -> {
                Players.healExhaustion(player);
                Players.healSaturation(player);
                Players.healFoodLevel(player);
            });

            if (map == null) {
                if (state == ArenaState.STARTING && getTimer() <= 10) {
                    if (VoteManager.isNowVoting(id)) {
                        List<Map.Entry<ANNIMap, Integer>> l = getMapRanking(VoteManager.endVote(id));
                        if (l.isEmpty() || Collections3.allValueEquals(l, l.get(0))) {
                            List<ANNIMap> filtered = plugin.getMapManager().getMaps().stream()
                                    .filter(ANNIMap::canUseOnArena)
                                    .collect(Collectors.toList());
                            map = filtered.get(rand.nextInt(filtered.size()));
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
        updatePhase();

        if (state == ArenaState.GAME_OVER) launchFireworkRocket();

        if (state.getId() >= 0) {
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
                        )
                            broadcast(s);
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

            players.forEach((p) -> {
                ItemStack mainHand = p.getInventory().getItemInMainHand();
                ItemStack offHand = p.getInventory().getItemInOffHand();
                boolean hasBow = (mainHand != null && mainHand.getType() == Material.BOW) ||
                        (offHand != null && offHand.getType() == Material.BOW);

                if (getKit(p).equals(ANNIKit.ASSAULT.getKit())) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false, true));
                }
                else if (getKit(p).equals(ANNIKit.BOW.getKit()) || hasBow) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, false, true));
                }
            });
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

        bb.setColor(BarColor.BLUE);
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
            try {
                BoardManager.ANNIFastBoard fb = bm.get(pl);
                SimpleDateFormat df = new SimpleDateFormat(mm.build("scoreboard.dateformat"));

                switch (state) {
                    case WAITING:
                    case STARTING: {
                        fb.updateTitle(mm.build("scoreboard.title"));
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
                        } else {
                            List<Map.Entry<ANNIMap, Integer>> nowRes = getMapRanking(VoteManager.getResult(id));
                            fb.updateLines(mm.buildList("scoreboard.waiting_voting",
                                    df.format(Calendar.getInstance().getTime()),
                                    news,
                                    players.size() + " / " + (ANNIConfig.getTeamMaxPlayers() * getEnabledTeams().size()),
                                    !nowRes.isEmpty() ? nowRes.get(0).getKey().getName() + " - " + nowRes.get(0).getValue() : "",
                                    nowRes.size() > 1 ? nowRes.get(1).getKey().getName() + " - " + nowRes.get(1).getValue() : "",
                                    nowRes.size() > 2 ? nowRes.get(2).getKey().getName() + " - " + nowRes.get(2).getValue() : ""
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
                        if (fb.hasLinesMaxLength()) {
                            if (map.getName() != null)
                                fb.updateTitle(mm.build("scoreboard.playing.short.title", map.getName()));
                            else fb.updateTitle(mm.build("scoreboard.title"));
                            fb.updateLines(
                                    mm.buildList(
                                            "scoreboard.playing.short",
                                            shortNexusHealth(ANNITeam.RED),
                                            shortNexusHealth(ANNITeam.BLUE),
                                            shortNexusHealth(ANNITeam.GREEN),
                                            shortNexusHealth(ANNITeam.YELLOW)
                                    )
                            );
                        }
                        else {
                            fb.updateTitle(mm.build("scoreboard.title"));
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
                        }
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
            catch (Exception e) {
                e.printStackTrace();
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

    private int shortNexusHealth(ANNITeam team) {
        return getNexusHealth(team) != null ? getNexusHealth(team) : 0;
    }

    private void updatePhase() {
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

    private void launchFireworkRocket() {
        if (fireworkTimer <= 0) fireworkTimer = 3;
        else {
            fireworkTimer--;
            return;
        }

        Map<ANNITeam, Color> colorMap = new HashMap<>();
        colorMap.put(ANNITeam.RED, Color.RED);
        colorMap.put(ANNITeam.BLUE, Color.BLUE);
        colorMap.put(ANNITeam.GREEN, Color.GREEN);
        colorMap.put(ANNITeam.YELLOW, Color.YELLOW);

        List<ANNITeam> living = getTeams().keySet().stream()
                .filter(team -> !isNexusLost(team))
                .collect(Collectors.toList());

        if (living.size() != 1) return;

        getTeamPlayers(living.get(0)).stream()
                .filter(p -> !SpectatorManager.isSpectating(p.getUniqueId()))
                .forEach(winner -> {
                        if (winner.getWorld() != copy) return;

                        Firework fw = (Firework) copy.spawnEntity(winner.getLocation(), EntityType.FIREWORK);

                        fw.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, "winner-rocket"),
                                PersistentDataType.INTEGER, 1
                        );

                        FireworkMeta fm = fw.getFireworkMeta();

                        fm.addEffect(
                                rand.nextBoolean() ?
                                        FireworkEffect.builder()
                                                .with(FireworkEffect.Type.BALL)
                                                .withColor(colorMap.get(living.get(0)))
                                                .build()
                                        :
                                        FireworkEffect.builder()
                                                .with(FireworkEffect.Type.BALL)
                                                .withFlicker()
                                                .withColor(colorMap.get(living.get(0)))
                                                .build()
                        );
                        fm.setPower(1);

                        fw.setFireworkMeta(fm);
                });
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
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0);
        player.setCompassTarget(player.getWorld().getSpawnLocation());
    }

    private ANNITeam assignTeam() {
        Map<ANNITeam, Integer> sizeOfTeam = new HashMap<>();
        getTeams().keySet().stream()
                .filter(t -> !state.isInArena() || !isNexusLost(t))
                .forEach(t ->
                    sizeOfTeam.put(t, getTeamPlayers(t).size())
                );

        ANNITeam assigned;

        if (Collections3.allValueEquals(sizeOfTeam.values(), sizeOfTeam.values().iterator().next())) {
            List<ANNITeam> list = new ArrayList<>(sizeOfTeam.keySet());
            assigned = list.get(rand.nextInt(list.size()));
        }
        else {
            Entry<ANNITeam, Integer> least = null;
            for (Entry<ANNITeam, Integer> entry : sizeOfTeam.entrySet()) {
                if (least == null) {
                    least = entry;
                    continue;
                }

                boolean update = rand.nextBoolean();
                if ((least.getValue().equals(entry.getValue()) && update) || least.getValue() > entry.getValue())
                    least = entry;
            }

            assigned = least.getKey();
        }

        return assigned;
    }
}
