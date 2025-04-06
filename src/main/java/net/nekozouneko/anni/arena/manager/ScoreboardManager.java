package net.nekozouneko.anni.arena.manager;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.board.BoardManager;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.message.MessageManager;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.vote.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ScoreboardManager {

    private final ANNIArena arena;

    public ScoreboardManager(ANNIArena arena) {
        this.arena = arena;
    }

    /*public void update() {
        BoardManager bm = ANNIPlugin.getInstance().getBoardManager();
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

        for (Player pl : Bukkit.getOnlinePlayers()) {
            try {
                BoardManager.ANNIFastBoard fb = bm.get(pl);
                SimpleDateFormat df = new SimpleDateFormat(mm.build("scoreboard.dateformat"));

                switch (arena.getState()) {
                    case WAITING:
                    case STARTING: {
                        fb.updateTitle(mm.build("scoreboard.title"));
                        String news = arena.getState() == ArenaState.STARTING ?
                                mm.build("scoreboard.waiting.2.starting", CmnUtil.secminTimer(arena.getTimer())) :
                                mm.build("scoreboard.waiting.2.more_player", (
                                        arena.getEnabledTeams().entrySet().stream()
                                                .filter(Map.Entry::getValue)
                                                .count() * ANNIConfig.getTeamMinPlayers() - Bukkit.getOnlinePlayers().size())
                                );

                        if (arena.getMap() != null) {
                            fb.updateLines(mm.buildList("scoreboard.waiting",
                                    df.format(Calendar.getInstance().getTime()),
                                    news,
                                    Bukkit.getOnlinePlayers().size() + " / 120",
                                    arena.getMap().getName()
                            ));
                        } else {
                            List<Map.Entry<String, Integer>> nowRes = arena.getVoteManager().getSortedResults();
                            fb.updateLines(mm.buildList("scoreboard.waiting_voting",
                                    df.format(Calendar.getInstance().getTime()),
                                    news,
                                    Bukkit.getOnlinePlayers().size() + " / " + (ANNIConfig.getTeamMaxPlayers() * arena.getEnabledTeams().size()),
                                    !nowRes.isEmpty() ? nowRes.get(0).getKey() + " - " + nowRes.get(0).getValue() : "",
                                    nowRes.size() > 1 ? nowRes.get(1).getKey() + " - " + nowRes.get(1).getValue() : "",
                                    nowRes.size() > 2 ? nowRes.get(2).getKey() + " - " + nowRes.get(2).getValue() : ""
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
                            if (arena.getMap().getName() != null)
                                fb.updateTitle(mm.build("scoreboard.playing.short.title", arena.getMap().getName()));
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
                                    arena.getMap() != null ? arena.getMap().getName() : "-----"
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
    }*/

    public void update() {
        BoardManager bm = ANNIPlugin.getInstance().getBoardManager();
        TranslationManager tm = ANNIPlugin.getInstance().getTranslationManager();
        ViaAPI<Player> viaApi = Via.getAPI();

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                FastBoard fb = bm.get(player); //TODO
                SimpleDateFormat df = new SimpleDateFormat(tm.string(player, "format.scoreboard_datetime"));
                Component datetime = Component.text(df.format(Calendar.getInstance().getTime()));

                switch (arena.getState()) {
                    case WAITING -> {
                        fb.updateTitle(tm.component(player, "scoreboard.title"));

                        ANNIMap map = arena.getMap();
                        long enabledTeams = arena.getEnabledTeams().entrySet().stream()
                                .filter(Map.Entry::getValue)
                                .count();
                        long requiredPlayers = (enabledTeams * ANNIConfig.getTeamMinPlayers()) - Bukkit.getOnlinePlayers().size();

                        if (map != null)
                            fb.updateLines(tm.componentList(player, "scoreboard.not_enough.map_selected",
                                    datetime,
                                    requiredPlayers,
                                    map.getName()
                            ));
                        else {
                            fb.updateLines(tm.componentList(player, "scoreboard.not_enough",
                                    datetime,
                                    requiredPlayers,
                                    mapEntry(player, 0),
                                    mapEntry(player, 1),
                                    mapEntry(player, 2)
                            ));
                        }
                    }
                    case STARTING -> {
                        fb.updateTitle(tm.component(player, "scoreboard.title"));

                        fb.updateLines(tm.componentList(player, "scoreboard.begins_soon",
                                datetime,
                                arena.getTimer(),
                                arena.getMap().getName()
                        ));
                    }
                    case PHASE_ONE, PHASE_TWO, PHASE_THREE, PHASE_FOUR, PHASE_FIVE, GAME_OVER -> {
                        if (viaApi.getPlayerVersion(player) < ProtocolVersion.v1_13.getVersion()) {
                            if (arena.getMap() != null && arena.getMap().getName() != null)
                                fb.updateTitle(tm.component(player, "scoreboard.playing.short.title", arena.getMap().getName()));
                            else fb.updateTitle(tm.component(player, "scoreboard.title"));

                            for (ANNITeam team : ANNITeam.values()) {
                                Integer health = arena.getNexusHealth(team);
                                fb.updateScore(health != null ? health : 0, tm.component(player, team.getNameKey()));
                            }
                        }
                        else {
                            fb.updateTitle(tm.component(player, "scoreboard.title"));
                            fb.updateLines(tm.componentList(player, "scoreboard.playing",
                                    datetime,
                                    nexusState(player, ANNITeam.RED),
                                    nexusState(player, ANNITeam.BLUE),
                                    nexusState(player, ANNITeam.GREEN),
                                    nexusState(player, ANNITeam.YELLOW),
                                    nexusHealth(player, ANNITeam.RED),
                                    nexusHealth(player, ANNITeam.BLUE),
                                    nexusHealth(player, ANNITeam.GREEN),
                                    nexusHealth(player, ANNITeam.YELLOW),
                                    arena.getMap().getName()
                            ));
                        }
                    }
                    default -> fb.updateLines(tm.componentList(player, "scoreboard.stopped", datetime));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String sbNexusHealth(ANNITeam team) {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();
        Integer nh = arena.getNexusHealth(team);

        if (nh == null) nh = 0;
        return arena.isEnabledTeam(team) ? // teamが無効化されていないなら
                String.format(mm.build("nexus.health.format"), nh) // フォーマット適応済みの体力
                : mm.build("nexus.health.none"); // 体力なしのメッセージ
    }

    private String sbNexusState(ANNITeam team) {
        MessageManager mm = ANNIPlugin.getInstance().getMessageManager();
        return arena.isNexusLost(team) ? mm.build("nexus.status.lost") : mm.build("nexus.status.active");
    }

    private int shortNexusHealth(ANNITeam team) {
        return arena.getNexusHealth(team) != null ? arena.getNexusHealth(team) : 0;
    }

    private Component nexusState(Player player, ANNITeam team) {
        TranslationManager translation = ANNIPlugin.getInstance().getTranslationManager();

        return translation.component(player, arena.isNexusLost(team) ? "scoreboard.nexus.state.lost" : "scoreboard.nexus.state.active");
    }

    private Component nexusHealth(Player player, ANNITeam team) {
        TranslationManager translation = ANNIPlugin.getInstance().getTranslationManager();
        Integer health = ANNIPlugin.getInstance().getCurrentGame().getNexusHealth(team);

        return health != null ?
                Component.text(String.format(translation.string(player, "format.nexus_health"), health))
                : translation.component(player, "scoreboard.nexus.health.none");
    }

    private Component mapEntry(Player player, int index) {
        TranslationManager translation = ANNIPlugin.getInstance().getTranslationManager();
        VoteManager vote = ANNIPlugin.getInstance().getCurrentGame().getVoteManager();
        List<Map.Entry<String, Integer>> results = vote.getSortedResults();

        if (results.isEmpty() || results.size() <= index)
            return translation.component(player, "scoreboard.entry", "-", "-");

        Map.Entry<String, Integer> entry = results.get(index);

        return translation.component(player, "scoreboard.entry", entry.getKey(), entry.getValue());
    }
}
