package net.nekozouneko.anniv2.arena;

import net.nekozouneko.anniv2.ANNIPlugin;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArenaManager {

    private static final Random rand = new Random();

    private final ANNIPlugin plugin;
    private final Map<String, ANNIArena> arenas = new HashMap<>();

    public ArenaManager(ANNIPlugin plugin) {
        this.plugin = plugin;
    }

    public ANNIArena create() {
        return create(null);
    }

    public ANNIArena create(String id) {
        if (id != null && arenas.containsKey(id)) return null;

        while (id == null || arenas.containsKey(id)) {
            id = String.format("%08x", rand.nextInt(Integer.MAX_VALUE) + 1);
        }

        ANNIArena aren = new ANNIArena(plugin, id);
        arenas.put(id, aren);

        return aren;
    }

    public ANNIArena getArena(String id) {
        return arenas.get(id);
    }

    public ANNIArena getArenaByPlayer(Player player) {
        for (ANNIArena aren : arenas.values()) {
            if (aren.getPlayers().contains(player)) return aren;
        }

        return null;
    }

    public Map<String, ANNIArena> getArenas() {
        return Collections.unmodifiableMap(arenas);
    }

    public void leave(Player player) {
        arenas.values().forEach((arena) -> arena.leave(player));
    }

}
