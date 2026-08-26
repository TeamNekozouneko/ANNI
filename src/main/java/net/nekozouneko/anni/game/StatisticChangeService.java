package net.nekozouneko.anni.game;

import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.arena.ANNIArena;

@RequiredArgsConstructor
public class StatisticChangeService {

    private final ANNIArena arena;

    public boolean shouldChangeValues() {
        return arena.getState().getId() > 0;
    }

}
