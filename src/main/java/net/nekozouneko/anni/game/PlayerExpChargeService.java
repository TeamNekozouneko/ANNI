package net.nekozouneko.anni.game;

import lombok.RequiredArgsConstructor;
import net.nekozouneko.anni.arena.ANNIArena;
import net.nekozouneko.anni.kit.ANNIKit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PlayerExpChargeService {

    private final ANNIArena game;

    public int getChargeAmount(Player player, int current) {
        if (!game.getState().isInArena()) return current;

        return ANNIKit.get(game.getKit(player)) == ANNIKit.ENCHANTER ? current * 2 : current;
    }

}
