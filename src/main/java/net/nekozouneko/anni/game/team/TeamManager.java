package net.nekozouneko.anni.game.team;

import com.google.common.base.Preconditions;
import net.nekozouneko.anni.ANNIConfig;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.game.Nexus;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {

    private final TeamRepository repository;
    private final Map<ANNITeam, AnnihilationTeam> teams = new EnumMap<>(ANNITeam.class);

    public TeamManager(TeamRepository repository) {
        this.repository = repository;

        for (ANNITeam color : ANNITeam.values())
            if (ANNIConfig.isTeamEnabled(color)) enable(color);
    }

    public void join(ANNITeam color, UUID player) {
        repository.join(color, player);
        teams.get(color).addPlayer(player);
    }

    public void leave(UUID player) {
        repository.leave(player);
        teams.values().forEach(team -> team.removePlayer(player));
    }

    public void enable(ANNITeam color) {
        Preconditions.checkArgument(!teams.containsKey(color));

        repository.create(color);
        var team = new AnnihilationTeam(color, new Nexus(ANNIConfig.getDefaultHealth()));
        teams.put(color, team);
    }

    public void disable(ANNITeam color) {
        teams.remove(color);
        repository.remove(color);
    }

    public boolean isEnabled(ANNITeam color) {
        return teams.containsKey(color);
    }

    public AnnihilationTeam getTeam(ANNITeam color) {
        return teams.get(color);
    }

    public ANNITeam getTeamColorByPlayer(UUID player) {
        return teams.keySet().stream().filter(color -> teams.get(color).getPlayers().contains(player)).findFirst().orElse(null);
    }

    public AnnihilationTeam getTeamByPlayer(UUID player) {
        return teams.values().stream().filter(team -> team.getPlayers().contains(player)).findFirst().orElse(null);
    }

    public Map<ANNITeam, AnnihilationTeam> getTeams() {
        return Map.copyOf(teams);
    }

}
