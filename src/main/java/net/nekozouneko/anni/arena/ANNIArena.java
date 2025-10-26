package net.nekozouneko.anni.arena;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.manager.BossbarManager;
import net.nekozouneko.anni.arena.manager.ScoreboardManager;
import net.nekozouneko.anni.arena.spectator.SpectatorManager;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.game.Nexus;
import net.nekozouneko.anni.game.save.SaveDataRepository;
import net.nekozouneko.anni.game.team.TeamManager;
import net.nekozouneko.anni.item.DefenseArtifact;
import net.nekozouneko.anni.kit.ANNIKit;
import net.nekozouneko.anni.kit.Kit;
import net.nekozouneko.anni.listener.PlayerDamageListener;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.task.RechargeManager;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ANNIArena extends BukkitRunnable {

    private static final Random rand = new Random();

    private final ANNIPlugin plugin;
    private final MessageManager mm;
    private final TranslationManager tm;
    @Getter
    private final BossbarManager bossbarManager;
    @Getter
    private VoteManager voteManager;
    private final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
    @Getter
    private RechargeManager rechargeManager = null;
    @Getter
    private final String id;

    private final Set<Player> players = new HashSet<>();

    @Getter @Setter
    private ArenaState state = ArenaState.WAITING;

    @Getter @Setter
    private ANNIMap map = null;
    private World copy = null;

    @Getter
    private boolean enabledTimer = false;
    @Getter @Setter
    private long timer = 0;
    private int fireworkTimer = 0;

    private final Map<UUID, String> kit = new HashMap<>();

    @Getter
    private final TeamManager teamManager;
    private final SaveDataRepository saveDataRepository;

    public ANNIArena(ANNIPlugin plugin, String id, TeamManager teamManager, SaveDataRepository saveDataRepository) {
        Objects.requireNonNull(plugin);
        Preconditions.checkArgument(id.length() < 9, "Id length limit is 8! (" + id.length() + ")");

        this.plugin = plugin;
        this.teamManager = teamManager;
        this.saveDataRepository = saveDataRepository;
        this.mm = plugin.getMessageManager();
        this.tm = plugin.getTranslationManager();
        this.id = id;
        this.voteManager = new VoteManager(plugin.getMapManager().getMaps().stream()
                .filter(ANNIMap::canUseOnArena)
                .map(ANNIMap::getId)
                .collect(Collectors.toSet())
        );

        this.bossbarManager = new BossbarManager(this);
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

        ANNITeam color;
        if (!saveDataRepository.canLoad(player.getUniqueId())) {
            color = assignTeam();

            teamManager.leave(player.getUniqueId());
            teamManager.join(color, player.getUniqueId());

            Players.clearPotionEffects(player);
            initPlayer(player);
            player.getInventory().setContents(ANNIKit.teamColor(getKit(player), player.locale(), color));
            player.teleport(map.getSpawnOrDefault(color).toLocation(copy));
        }
        else {
            saveDataRepository.load(player.getUniqueId());
            saveDataRepository.remove(player.getUniqueId());

            color = teamManager.getTeamColorByPlayer(player.getUniqueId());

            if (teamManager.getTeam(color).isLost()) {
                Players.clearPotionEffects(player);
                initPlayer(player);
                SpectatorManager.add(player);
                player.teleport(map.getSpawnOrDefault(color).toLocation(copy));
            }
        }

        player.setGameMode(GameMode.SURVIVAL);

        player.sendMessage(mm.buildBigChar(CmnUtil.numberToChar(state.getId()), Character.toString(color.getCCChar()),
                (Object[]) mm.buildArray("notify.big.mid_join", LegacyComponentSerializer.legacyAmpersand().serialize(tm.component(color.getNameKey())))
        ));
    }

    public void leave(Player player) {
        players.remove(player);

        if (state.getId() > 0 && teamManager.getTeamColorByPlayer(player.getUniqueId()) != null) {
            if (PlayerDamageListener.isFighting(player)) {
                Arrays.stream(player.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .filter(is -> {
                            PersistentDataContainer pdc = is.getItemMeta().getPersistentDataContainer();

                            return pdc.getOrDefault(
                                    new NamespacedKey(ANNIPlugin.getInstance(), "kit-item"),
                                    PersistentDataType.INTEGER, 0
                            ) == 0;
                        })
                        .forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            }
            else saveDataRepository.save(player.getUniqueId());

            player.getInventory().clear();
        }

        teamManager.leave(player.getUniqueId());

        player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public boolean isJoined(Player player) {
        return players.contains(player);
    }

    private void deleteTeams() {
        teamManager.getTeams().keySet().forEach(teamManager::disable);
    }

    public Set<Player> getTeamPlayers(ANNITeam team) {
        return teamManager.getTeam(team).getPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
        return teamManager.getTeam(team).getNexus().getHealth();
    }

    public void damageNexusHealth(ANNITeam team, int damage, Player player) {
        if (damage <= 0) return;

        Nexus nexus = teamManager.getTeam(team).getNexus();

        if (!teamManager.getTeam(team).getNexus().isDestroyed()) {
            nexus.damage(damage);
            int health = nexus.getHealth();

            if (player != null) {
                bossbarManager.damageNexus(team, player, health);
                getTeamPlayers(team).forEach(p1 -> {
                    p1.sendActionBar(tm.component("actionbar.nexus_alert", player.name()));
                    p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 2);
                });

                VaultUtil.ifAvail((eco) -> {
                    if (teamManager.getTeamColorByPlayer(player.getUniqueId()) == null) return;

                    getTeamPlayers(teamManager.getTeamColorByPlayer(player.getUniqueId())).forEach(teammate -> {
                        ANNIPlugin.getInstance().getPointManager().givePoint(teammate, 3);
                        teammate.sendMessage(tm.component(teammate, "money.deposit", "3"));
                    });
                });

                if (ANNIKit.get(getKit(player)) == ANNIKit.WORKER && !nexus.isDestroyed()) {
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

                    if (isPass) teamManager.getTeamByPlayer(player.getUniqueId()).getNexus().heal(1);
                }
            }
            else {
                getTeamPlayers(team).forEach(p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 2));
            }

            if ((nexus.isDestroyed())) {
                for (String s :
                        mm.buildBigChar(
                                team.getBigChar(),
                                Character.toString(teamManager.getTeamColorByPlayer(player.getUniqueId()).getCCChar()),
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

    public void restoreNexus(ANNITeam team, Integer health) {
        if (teamManager.getTeam(team).getNexus().isDestroyed()) {
            teamManager.getTeam(team).getNexus().setHealth(health);
            BukkitAdapter.adapt(copy, map.getNexus(team).getLocation())
                    .getBlock().setType(Material.END_STONE);
        }
        else throw new IllegalStateException("Nexus is now active");
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
                if (voteManager != null && !voteManager.isEmpty()) {
                    map = plugin.getMapManager().getMap(voteManager.getResult());
                    if (map != null && !map.canUseOnArena()) map = null;
                    voteManager = null;
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
                                pr.getMinimumPoint().y(),
                                pr.getMaximumPoint().y()
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

            for (ANNITeam color : ANNITeam.values()) {
                Block nexusBlock = BukkitAdapter.adapt(copy, map.getNexus(color).getLocation()).getBlock();
                if (!teamManager.isEnabled(color))
                    nexusBlock.setType(Material.BEDROCK);
                else {
                    teamManager.getTeam(color).getNexus().setHealth(ANNIConfig.getDefaultHealth());
                    nexusBlock.setType(Material.END_STONE);
                }
            }

            log.info("Assigning players...");
            saveDataRepository.clear();
            players.forEach(player -> teamManager.join(assignTeam(), player.getUniqueId()));
            teamManager.getTeams().forEach((color, team) -> {
                team.getNexus().setHealth(ANNIConfig.getDefaultHealth());
                team.getNexus().setMaxHealth(ANNIConfig.getDefaultHealth());

                Location teamSpawn = map.getSpawnOrDefault(color).toLocation(copy);
                team.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(player -> {
                            player.teleport(teamSpawn);
                            player.setGameMode(GameMode.SURVIVAL);
                            initPlayer(player);
                            player.getInventory().setContents(ANNIKit.teamColor(getKit(player), player.locale(), color));
                        });
                for (String s : mm.buildBigChar('1', Character.toString(color.getCCChar()),
                        (Object[]) mm.buildArray("notify.big.started", color.getTeamName())
                )) broadcast(s, color);
            });

            rechargeManager = new RechargeManager();
            rechargeManager.runTaskTimer(plugin, 0, 10);

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
            saveDataRepository.clear();
            ANNIPlugin.getInstance().getFurnaceManager().clear();
            plugin.getCooldownManager().clear();
            players.forEach(player -> {
                player.spigot().respawn();
                initPlayer(player);
                Players.clearPotionEffects(player);
                player.teleport(plugin.getLobby());
                player.setFlying(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
            });
            log.info("Removing player from team...");
            teamManager.getTeams().forEach((color, team) -> {
                team.getPlayers().forEach(teamManager::leave);
            });
            log.info("Cancelling tasks...");
            DefenseArtifact.cancelAllTasks();
            if (rechargeManager != null && !rechargeManager.isCancelled()) rechargeManager.cancel();
            rechargeManager = null;
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
            voteManager = null;
        }
        catch (IOException e) { e.printStackTrace(); }

        log.info("Clean up complete.");
    }

    public void broadcast(String message) {
        players.forEach(p -> p.sendMessage(message));
        plugin.getLogger().info(message);
    }

    public void broadcastTranslated(String key, Object... args) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(tm.component(player, key, args)));
        plugin.getComponentLogger().info(tm.component(key, args));
    }

    public void broadcast(String message, ANNITeam team) {
        getTeamPlayers(team).forEach(p -> p.sendMessage(message));
        plugin.getLogger().info(message);
    }

    public void broadcastTranslated(ANNITeam team, String key, Object... args) {
        getTeamPlayers(team).forEach(player -> player.sendMessage(tm.component(player, key, args)));
        plugin.getComponentLogger().info(tm.component(key, args));
    }

    public void setKit(Player player, Kit ki) {
        plugin.getFurnaceManager().remove(player);
        kit.put(player.getUniqueId(), ki.getId());
    }

    public Kit getKit(Player player) {
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
                    if (voteManager != null) {
                        if (voteManager.isEmpty()) {
                            List<ANNIMap> maps = plugin.getMapManager().getMaps().stream()
                                    .filter(ANNIMap::canUseOnArena)
                                    .collect(Collectors.toList());
                            map = maps.get(rand.nextInt(maps.size()));
                        }
                        else map = plugin.getMapManager().getMap(voteManager.getResult());
                    }
                }
                else {
                    Set<String> choices = plugin.getMapManager().getMaps().stream()
                            .filter(ANNIMap::canUseOnArena)
                            .map(ANNIMap::getId)
                            .collect(Collectors.toSet());

                    if (voteManager != null) {
                        voteManager.updateChoices(choices);
                    }
                    else voteManager = new VoteManager(choices);
                }
            }
            else voteManager = null;
        }

        bossbarManager.update();
        scoreboardManager.update();
        updatePhase();

        if (state == ArenaState.GAME_OVER) launchFireworkRocket();

        if (state.isInArena()) {
            if (state.getId() > 0) {
                // プレイヤー数が0のチームを退場させる
                teamManager.getTeams().keySet().forEach(color -> {
                    var team = teamManager.getTeam(color);
                    if (!team.getNexus().isDestroyed() && team.getPlayers().isEmpty()) {
                        team.getNexus().setHealth(0);
                        broadcastTranslated("notify.no_player_team", tm.component(color.getNameKey()));
                    }
                });

                if (state == ArenaState.PHASE_FIVE) {
                    teamManager.getTeams().keySet().forEach((at) -> {
                        // (ネクサスを失ったもしくは、ネクサスの体力が1以下) ではないなら
                        if (!(teamManager.getTeam(at).isLost() || getNexusHealth(at) <= 1)) {
                            damageNexusHealth(at, 1, null);
                        }
                    });
                }

                // ネクサスを失っていないチーム数を調べる
                List<ANNITeam> living = teamManager.getTeams().keySet().stream()
                        .filter(team -> !teamManager.getTeam(team).isLost())
                        .toList();

                // もし1以下なら
                if (living.size() <= 1) {
                    // もし1なら
                    if (living.size() == 1) {
                        ANNITeam won = living.getFirst();

                        for (String s : mm.buildBigChar(
                                'e', Character.toString(won.getCCChar()),
                                (Object[]) mm.buildArray("notify.big.won",
                                        won.getColoredName()
                                ))
                        )
                            broadcast(s);
                        getTeamPlayers(won).forEach(p -> {
                            ANNIPlugin.getInstance().getPointManager().givePoint(p, 3000);
                            p.sendMessage(tm.component(p, "money.deposit", "3000"));
                        });
                    } else { // ではない (0 ~ (Integer.MIN_VALUE)) なら
                        broadcastTranslated("notify.draw");
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
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, false, false, true));
                }
            });
        }
    }

    @Override
    public void cancel() {
        super.cancel();

        cleanUp();
        deleteTeams();
        bossbarManager.delete();
    }

    private void updatePhase() {
        switch (state) {
            case WAITING: {
                if (players.size() >= teamManager.getTeams().size() * ANNIConfig.getTeamMinPlayers()) {
                    enableTimer();
                    setTimer(ArenaState.STARTING.nextPhaseIn());
                    setState(ArenaState.STARTING);
                }
                break;
            }
            case STARTING: {
                if (!(players.size() >= teamManager.getTeams().size() * ANNIConfig.getTeamMinPlayers())) {
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
                    teamManager.getTeams().forEach((at, t) -> {
                        for (String s : mm.buildBigChar(
                                CmnUtil.numberToChar(state.nextPhase().getId()),
                                Character.toString(at.getCCChar()),
                                (Object[]) mm.buildArray("notify.big.next_phase",
                                    tm.component(state.nextPhase().getName()),
                                    tm.componentLines(state.nextPhase().getDescription())
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

        List<ANNITeam> living = teamManager.getTeams().keySet().stream()
                .filter(team -> !teamManager.getTeam(team).isLost())
                .toList();

        if (living.size() != 1) return;

        getTeamPlayers(living.getFirst()).stream()
                .filter(p -> !SpectatorManager.isSpectating(p.getUniqueId()))
                .forEach(winner -> {
                        if (winner.getWorld() != copy) return;

                        Firework fw = (Firework) copy.spawnEntity(winner.getLocation(), EntityType.FIREWORK_ROCKET);

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

    private void initPlayer(Player player) {
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0);
        player.setCompassTarget(player.getWorld().getSpawnLocation());
    }

    private ANNITeam assignTeam() {
        Map<ANNITeam, Integer> sizeOfTeam = new HashMap<>();
        teamManager.getTeams().keySet().stream()
                .filter(t -> !state.isInArena() || !teamManager.getTeam(t).isLost())
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
