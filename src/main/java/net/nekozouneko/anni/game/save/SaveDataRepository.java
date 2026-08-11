package net.nekozouneko.anni.game.save;

import java.util.UUID;

public interface SaveDataRepository {

    record SavedResult(boolean location, boolean inventory) {

    }

    void clear();

    void remove(UUID player);

    SavedResult load(UUID player);

    boolean canLoad(UUID player);

    void save(UUID player, boolean saveOnlyTeamColor, boolean resetPosition);

}
