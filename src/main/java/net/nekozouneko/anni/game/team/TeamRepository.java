package net.nekozouneko.anni.game.team;

import net.nekozouneko.anni.arena.team.ANNITeam;

import java.util.UUID;

public interface TeamRepository {

    void create(ANNITeam color);

    void remove(ANNITeam color);

    void join(ANNITeam color, UUID player);

    void leave(UUID player);

}
