package net.nekozouneko.anni.arena.spectator;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public final class SpectatorManager {

    private static final List<UUID> spectating = new ArrayList<>();
    private static final List<UUID> spectatorsWatchable = new ArrayList<>();

    private SpectatorManager() {
        throw new ExceptionInInitializerError();
    }

    public static void add(Player player) {
        spectating.add(player.getUniqueId());

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
    }

    public static void addWatchable(Player player) {
        spectatorsWatchable.add(player.getUniqueId());
    }

    public static void remove(Player player) {
        spectating.remove(player.getUniqueId());
        if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)
            player.setAllowFlight(false);
    }

    public static void remove(UUID uniqueId) {
        spectating.remove(uniqueId);
    }

    public static void removeWatchable(Player player) {
        spectatorsWatchable.remove(player.getUniqueId());
    }

    public static void removeWatchable(UUID uniqueId) {
        spectatorsWatchable.remove(uniqueId);
    }

    public static boolean isSpectating(Player player) {
        return spectating.contains(player.getUniqueId());
    }

    public static boolean isSpectating(UUID uniqueId) {
        return spectating.contains(uniqueId);
    }

    public static boolean isWatchable(Player player) {
        return spectatorsWatchable.contains(player.getUniqueId());
    }

    public static boolean isWatchable(UUID uniqueId) {
        return spectatorsWatchable.contains(uniqueId);
    }

    public static List<UUID> getPlayers() {
        return Collections.unmodifiableList(spectating);
    }

    public static List<UUID> getWatchablePlayers() {
        return Collections.unmodifiableList(spectatorsWatchable);
    }

    public static void clear() {
        spectating.clear();
    }

    public static void clearWatchable() {
        spectatorsWatchable.clear();
    }

}
