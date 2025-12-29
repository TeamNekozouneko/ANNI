package net.nekozouneko.anni.arena.manager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.board.BoardManager;
import net.nekozouneko.anni.map.ANNIMap;
import net.nekozouneko.anni.message.TranslationManager;
import net.nekozouneko.anni.vote.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

@RequiredArgsConstructor
public class ScoreboardManager {

    private final ANNIArena arena;
    private final BoardManager boardManager;
    private final TranslationManager translationManager;
    private final ViaAPI<Player> viaApi;

    private final Map<Locale, SimpleDateFormat> cachedFormat = new HashMap<>();

    private record BoardOutput(Component title, List<Component> lines) {}

    public void update() {
        Multimap<Locale, Player> localePlayerMap = HashMultimap.create();
        Multimap<Locale, Player> legacyLocalePlayerMap = HashMultimap.create();

        var loaded = translationManager.getLoadedLocales();
        Bukkit.getOnlinePlayers().forEach(player -> {
            boolean usingLoaded = loaded.contains(player.locale());

            if (arena.getState().isInArena() && viaApi.getPlayerVersion(player) < ProtocolVersion.v1_13.getVersion()) { //legacy
                if (usingLoaded)
                    legacyLocalePlayerMap.put(player.locale(), player);
                else legacyLocalePlayerMap.put(ANNIConfig.getDefaultLocale(), player);
            }
            else { // modern
                if (usingLoaded)
                    localePlayerMap.put(player.locale(), player);
                else localePlayerMap.put(ANNIConfig.getDefaultLocale(), player);
            }
        });

        final Date now = new Date();
        Map<Locale, BoardOutput> boards = new HashMap<>();

        if (!legacyLocalePlayerMap.isEmpty()) {
            Map<Locale, BoardOutput> legacy = new HashMap<>();

            legacyLocalePlayerMap.keySet().forEach(locale -> {
                legacy.put(locale, generateLegacyLocalized(locale));
            });

            new HashSet<>(localePlayerMap.keySet()).forEach(locale -> {
                final var output = legacy.get(locale);

                legacyLocalePlayerMap.get(locale).forEach(player -> {
                    var board = boardManager.get(player);

                    if (board.getTitle() == null || !board.getTitle().equals(output.title)) board.updateTitle(output.title());

                    board.updateLines(output.lines());
                });
            });
        }

        localePlayerMap.keySet().forEach(locale -> {
            boards.put(locale, generateLocalized(locale, now));
        });

        new HashSet<>(localePlayerMap.keySet()).forEach(locale -> {
            final var output = boards.get(locale);

            localePlayerMap.get(locale).forEach(player -> {
                var board = boardManager.get(player);

                if (board.getTitle() == null || !board.getTitle().equals(output.title)) board.updateTitle(output.title());

                board.updateLines(output.lines());
            });
        });
    }

    private BoardOutput generateLocalized(Locale locale, Date now) {
        SimpleDateFormat df = cachedFormat.computeIfAbsent(locale, (loc) -> new SimpleDateFormat(translationManager.string("format.scoreboard_datetime")));
        Component datetime = Component.text(df.format(now));

        Component title = translationManager.component(locale, "scoreboard.title");
        List<Component> lines;

        switch (arena.getState()) {
            case WAITING -> {
                ANNIMap map = arena.getMap();
                long enabledTeams = arena.getTeamManager().getTeams().size();
                long requiredPlayers = (enabledTeams * ANNIConfig.getTeamMinPlayers()) - Bukkit.getOnlinePlayers().size();

                if (map != null)
                    lines = translationManager.componentList(locale, "scoreboard.not_enough.map_selected",
                            datetime,
                            requiredPlayers,
                            map.getName()
                    );
                else {
                    lines = translationManager.componentList(locale, "scoreboard.not_enough",
                            datetime,
                            requiredPlayers,
                            mapEntry(locale, 0),
                            mapEntry(locale, 1),
                            mapEntry(locale, 2)
                    );
                }
            }
            case STARTING -> {
                var map = arena.getMap();
                if (map == null)
                    lines = translationManager.componentList(locale, "scoreboard.begins_soon",
                            datetime,
                            arena.getTimer(),
                            mapEntry(locale, 0),
                            mapEntry(locale, 1),
                            mapEntry(locale, 2)
                    );
                else lines = translationManager.componentList(locale, "scoreboard.begins_soon.map_selected",
                        datetime,
                        arena.getTimer(),
                        arena.getMap().getName()
                );
            }
            case PHASE_ONE, PHASE_TWO, PHASE_THREE, PHASE_FOUR, PHASE_FIVE, GAME_OVER -> {
                lines = translationManager.componentList(locale, "scoreboard.playing",
                        datetime,
                        nexusState(locale, ANNITeam.RED),
                        nexusState(locale, ANNITeam.BLUE),
                        nexusState(locale, ANNITeam.GREEN),
                        nexusState(locale, ANNITeam.YELLOW),
                        nexusHealth(locale, ANNITeam.RED),
                        nexusHealth(locale, ANNITeam.BLUE),
                        nexusHealth(locale, ANNITeam.GREEN),
                        nexusHealth(locale, ANNITeam.YELLOW),
                        arena.getMap().getName()
                );
            }
            default -> lines = translationManager.componentList(locale, "scoreboard.stopped", datetime);
        }

        return new BoardOutput(title, lines);
    }

    private BoardOutput generateLegacyLocalized(Locale locale) {
        Component title;

        if (arena.getMap() != null && arena.getMap().getName() != null)
            title = translationManager.component(locale, "scoreboard.playing.short.title", arena.getMap().getName());
        else title = translationManager.component(locale, "scoreboard.title");

        List<Component> lines = Arrays.asList(
                translationManager.component(locale, "scoreboard.playing.short.team_entry", translationManager.component(locale, ANNITeam.RED.getNameKey()), nexusHealth(locale, ANNITeam.RED)),
                translationManager.component(locale, "scoreboard.playing.short.team_entry", translationManager.component(locale, ANNITeam.BLUE.getNameKey()), nexusHealth(locale, ANNITeam.BLUE)),
                translationManager.component(locale, "scoreboard.playing.short.team_entry", translationManager.component(locale, ANNITeam.GREEN.getNameKey()), nexusHealth(locale, ANNITeam.GREEN)),
                translationManager.component(locale, "scoreboard.playing.short.team_entry", translationManager.component(locale, ANNITeam.YELLOW.getNameKey()), nexusHealth(locale, ANNITeam.YELLOW))
        );

        return new BoardOutput(title, lines);
    }

    private Component nexusState(Locale locale, ANNITeam color) {
        var team = arena.getTeamManager().getTeam(color);

        return translationManager.component(locale, team == null || team.isLost() ? "scoreboard.nexus.state.lost" : "scoreboard.nexus.state.active");
    }

    private Component nexusHealth(Locale locale, ANNITeam team) {
        var teamManager = arena.getTeamManager();

        Integer health = teamManager.isEnabled(team) ? teamManager.getTeam(team).getNexus().getHealth() : null;

        return health != null ?
                Component.text(String.format(translationManager.string(locale, "format.nexus_health"), health))
                : translationManager.component(locale, "scoreboard.nexus.health.none");
    }

    private Component mapEntry(Locale locale, int index) {
        VoteManager vote = ANNIPlugin.getInstance().getCurrentGame().getVoteManager();
        List<Map.Entry<String, Integer>> results = vote.getSortedResults();

        if (results.isEmpty() || results.size() <= index)
            return translationManager.component(locale, "scoreboard.entry", "-", "-");

        Map.Entry<String, Integer> entry = results.get(index);

        return translationManager.component(locale, "scoreboard.entry", entry.getKey(), entry.getValue());
    }
}
