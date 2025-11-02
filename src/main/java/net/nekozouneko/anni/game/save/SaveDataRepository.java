package net.nekozouneko.anni.game.save;

import java.util.UUID;

public interface SaveDataRepository {

    void clear();

    void remove(UUID player);

    boolean load(UUID player);

    boolean canLoad(UUID player);

    void save(UUID player, boolean saveOnlyTeamColor);

}
