package net.nekozouneko.anniv2.arena.spectator;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SpectatorManager {

    private static final List<UUID> spectating = new ArrayList<>();

    private SpectatorManager() {
        throw new ExceptionInInitializerError();
    }

    public static void add(Player player) {
        spectating.add(player.getUniqueId());

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
    }

    public static void remove(Player player) {
        spectating.remove(player.getUniqueId());
        if (player.getGameMode() == GameMode.ADVENTURE)
            player.setAllowFlight(false);
    }

    public static void remove(UUID uniqueId) {
        spectating.remove(uniqueId);
    }

    public static boolean isSpectating(Player player) {
        return spectating.contains(player.getUniqueId());
    }

    public static boolean isSpectating(UUID uniqueId) {
        return spectating.contains(uniqueId);
    }

    public static List<UUID> getPlayers() {
        return Collections.unmodifiableList(spectating);
    }

    public static void clear() {
        spectating.clear();
    }

}
