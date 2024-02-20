package net.nekozouneko.anni.task;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CooldownManager extends BukkitRunnable {

    @AllArgsConstructor
    public enum Type {
        AIR_JUMP("item.airjump.name"),
        DEFENSE_ARTIFACT("item.defense_artifact.name"),
        GRAPPLING_HOOK("item.grappling_hook.name"),
        STUN_GRENADE("item.stun_grenade.name");

        @Getter
        private final String key;
    }

    private final Map<Type, Map<UUID, Long>> cooldown = new EnumMap<>(Type.class);

    public void set(UUID player, Type type, long time) {
        Preconditions.checkArgument(time > 0);

        Map<UUID, Long> map = cooldown.computeIfAbsent(type, k -> new HashMap<>());

        long newCooltime = System.currentTimeMillis() + time;
        long prevCooltime = map.getOrDefault(player, Long.MIN_VALUE);

        if (prevCooltime > newCooltime) return;

        map.put(player, newCooltime);
    }

    public boolean isCooldownEnd(UUID player, Type type) {
        Map<UUID, Long> map = cooldown.computeIfAbsent(type, k -> new HashMap<>());
        return map.get(player) == null || map.get(player) <= System.currentTimeMillis();
    }

    public long getTimeLeft(UUID player, Type type) {
        Map<UUID, Long> map = cooldown.computeIfAbsent(type, k -> new HashMap<>());
        return Math.max(0, map.getOrDefault(player, 0L) - System.currentTimeMillis());
    }

    public String getTimeLeftFormatted(UUID player, Type type) {
        Map<UUID, Long> map = cooldown.computeIfAbsent(type, k -> new HashMap<>());
        double left = Math.max(0, (map.getOrDefault(player, 0L) / 1000) - System.currentTimeMillis());

        return String.format("%.1f", left);
    }

    public void resetCooldown(UUID player, Type... types) {
        if (types == null || types.length == 0) {
            new HashSet<>(cooldown.keySet()).forEach(type -> {
                cooldown.get(type).remove(player);
            });

            return;
        }

        for (Type type : types) {
            Map<UUID, Long> map = cooldown.get(type);
            if (map == null) continue;

            map.remove(player);
        }
    }

    @Override
    public void run() {
        for (Type type : cooldown.keySet()) {
            new HashSet<>(cooldown.get(type).keySet()).forEach(player -> {
                if (!isCooldownEnd(player, type)) return;

                cooldown.get(type).remove(player);
                Player bukkitPlayer = Bukkit.getPlayer(player);
                if (bukkitPlayer == null) return;

                MessageManager mm = ANNIPlugin.getInstance().getMessageManager();

                bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(mm.build("actionbar.cooldown_ended", mm.build(type.getKey())))
                );
                bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 2);
            });
        }
    }
}
