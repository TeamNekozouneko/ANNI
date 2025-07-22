package net.nekozouneko.anni.arena.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> offset = new HashMap<>();

    public void resetKills() {
        kills.clear();
    }

    public void death(Player player) {
        offset.put(player.getUniqueId(), kills.getOrDefault(player.getUniqueId(), 0));
    }

    public void kill(Player victim, Player killer) {

    }

}
