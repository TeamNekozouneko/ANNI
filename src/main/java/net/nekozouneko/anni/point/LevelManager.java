package net.nekozouneko.anni.point;

import net.nekozouneko.anni.ANNIPlugin;
import net.nekozouneko.anni.util.CmnUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LevelManager {

    public static final int MAX_LEVEL = 50;

    public void giveExp(Player player, long exp) {
        int befLevel = getLevel(player);
        if (befLevel >= MAX_LEVEL) return;

        long currentExp = getExp(player) + exp;
        if (currentExp < calculateExpForNextLevel(befLevel)) {
            ANNIPlugin.getInstance().getDatabase().setExp(player.getUniqueId(), currentExp);
            return;
        }

        while (currentExp >= calculateExpForNextLevel(getLevel(player))) {
            currentExp = currentExp - calculateExpForNextLevel(getLevel(player));
            ANNIPlugin.getInstance().getDatabase().addLevel(player.getUniqueId(), 1);
            ANNIPlugin.getInstance().getDatabase().setExp(player.getUniqueId(), currentExp);

            if (getLevel(player) >= MAX_LEVEL) break;
        }

        int aftLevel = getLevel(player);
        double progress = (double) currentExp / calculateExpForNextLevel(aftLevel);

        player.sendMessage(ANNIPlugin.getInstance().getTranslationManager().componentLines(player, "notify.level_up",
                        befLevel, aftLevel, currentExp, calculateExpForNextLevel(aftLevel),
                        CmnUtil.progressBar(progress, 20)
                ));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 0);
    }

    public int getLevel(Player player) {
        return ANNIPlugin.getInstance().getDatabase().getLevel(player.getUniqueId());
    }

    public int getLevel(UUID player) {
        return ANNIPlugin.getInstance().getDatabase().getLevel(player);
    }

    public long getExp(Player player) {
        return ANNIPlugin.getInstance().getDatabase().getExp(player.getUniqueId());
    }

    public long getExp(UUID player) {
        return ANNIPlugin.getInstance().getDatabase().getExp(player);
    }

    public static long calculateExpForNextLevel(int level) {
        return (long) (2000 + (Math.pow(level, 1.5) * 1000));
    }
}
