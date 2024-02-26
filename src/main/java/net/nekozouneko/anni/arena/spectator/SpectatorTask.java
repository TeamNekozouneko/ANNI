package net.nekozouneko.anni.arena.spectator;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorTask extends BukkitRunnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (SpectatorManager.isSpectating(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 255, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false, true));
                player.setAllowFlight(true);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ANNIPlugin.getInstance().getMessageManager()
                        .build("actionbar.spectator_mode")
                ));
            }

            Bukkit.getOnlinePlayers().forEach(watcher -> {
                if (player.equals(watcher)) return;

                if (SpectatorManager.isSpectating(player)) {
                    if (SpectatorManager.isWatchable(watcher)) {
                        if (watcher.canSee(player)) return;
                        watcher.showPlayer(ANNIPlugin.getInstance(), player);
                    } else {
                        if (!watcher.canSee(player)) return;
                        watcher.hidePlayer(ANNIPlugin.getInstance(), player);
                    }
                } else {
                    if (watcher.canSee(player)) return;
                    watcher.showPlayer(ANNIPlugin.getInstance(), player);
                }
            });
        });
    }

}
