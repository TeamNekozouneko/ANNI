package net.nekozouneko.anni.arena.spectator;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekozouneko.anni.ANNIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (SpectatorManager.isSpectating(p)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 255, false, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1, false, false, true));
                p.setAllowFlight(true);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ANNIPlugin.getInstance().getMessageManager()
                        .build("actionbar.spectator_mode")
                ));

                Bukkit.getOnlinePlayers().forEach(p2 -> {
                    if (!p2.equals(p)) {
                        p2.hidePlayer(ANNIPlugin.getInstance(), p);
                    }
                    if (SpectatorManager.isWatchable(p2)) {
                        p2.showPlayer(ANNIPlugin.getInstance(), p);
                    }
                });
            }
            else {
                Bukkit.getOnlinePlayers().forEach(p2 -> {
                    p2.showPlayer(ANNIPlugin.getInstance(), p);
                });
            }
        }
    }

}
