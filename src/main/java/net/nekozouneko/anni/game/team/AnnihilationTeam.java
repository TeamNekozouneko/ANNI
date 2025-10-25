package net.nekozouneko.anni.game.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.arena.team.ANNITeam;
import net.nekozouneko.anni.game.Nexus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class AnnihilationTeam {

    private final ANNITeam color;
    private final Nexus nexus;
    private final Set<UUID> players = new HashSet<>();

    public boolean isLost() {
        return nexus.isDestroyed() || players.isEmpty();
    }

    void addPlayer(UUID player) {
        players.add(player);
    }

    void removePlayer(UUID player) {
        players.remove(player);
    }

    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

}
