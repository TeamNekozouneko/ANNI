package net.nekozouneko.anni.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class RechargeManager extends BukkitRunnable {

    public enum Type {
        STUN_GRENADE
    }

    private final Map<Type, Map<UUID, Long>> recharges = new EnumMap<>(Type.class);

    @Override
    public void run() {

    }

    public void clear() {

    }

    public void clear(Player player) {

    }
}
