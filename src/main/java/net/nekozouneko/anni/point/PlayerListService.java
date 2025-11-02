package net.nekozouneko.anni.point;

import java.util.UUID;

public interface PlayerListService {

    void reset(UUID player);

    void showPlayerLevels(UUID player);

}
